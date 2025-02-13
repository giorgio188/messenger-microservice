package com.messenger.api_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.time.Instant;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.http.HttpHeaders;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final WebClient.Builder webClientBuilder;
    private final ReactiveRedisTemplate<String, Integer> redisTemplate;
    private static final String CACHE_PREFIX = "auth:";

    public AuthenticationFilter(WebClient.Builder webClientBuilder,
                                ReactiveRedisTemplate<String, Integer> redisTemplate) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.redisTemplate = redisTemplate;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return CACHE_PREFIX + Base64.getUrlEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing token", e);
            return null;
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            logger.info("Processing request: {} {}", request.getMethod(), request.getPath());

            String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (token == null || !token.startsWith("Bearer ")) {
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String tokenHash = hashToken(token);
            if (tokenHash == null) {
                return onError(exchange, "Error processing token", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Проверка токена
            return redisTemplate.opsForValue().get(tokenHash)
                    .switchIfEmpty(Mono.defer(() -> {
                        return webClientBuilder.build()
                                .post()
                                .uri("http://auth-service:8081/api/auth/verify")
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .retrieve()
                                .bodyToMono(TokenInfo.class)
                                .flatMap(tokenInfo -> {
                                    Duration ttl = Duration.between(
                                            Instant.now(),
                                            Instant.ofEpochSecond(tokenInfo.getExpirationTime())
                                    );

                                    if (ttl.isNegative() || ttl.isZero()) {
                                        return Mono.error(new RuntimeException("Token expired"));
                                    }

                                    return redisTemplate.opsForValue()
                                            .set(tokenHash, tokenInfo.getUserId(), ttl)
                                            .thenReturn(tokenInfo.getUserId());
                                });
                    }))
                    .flatMap(userId -> {
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .header("X-User-Id", String.valueOf(userId))
                                .build();
                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(modifiedRequest)
                                .build();

                        logger.debug("Request authorized, forwarding with headers");
                        return chain.filter(modifiedExchange);
                    })
                    .onErrorResume(error -> {
                        logger.error("Error verifying token", error);
                        return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                    });
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        String errorMessage = String.format("{\"error\": \"%s\"}", err);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorMessage.getBytes())));
    }

    public static class Config {
    }

    private static class TokenInfo {
        private Integer userId;
        private Long expirationTime;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(Long expirationTime) {
            this.expirationTime = expirationTime;
        }
    }
}


//@Override
//public GatewayFilter apply(Config config) {
//    return (exchange, chain) -> {
//        ServerHttpRequest request = exchange.getRequest();
//        logger.info("Processing request: {} {}", request.getMethod(), request.getPath());
//
//        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//        if (token == null || !token.startsWith("Bearer ")) {
//            return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
//        }
//        return webClientBuilder.build()
//                .post()
//                .uri("http://auth-service:8081/api/auth/verify")
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .retrieve()
//                .bodyToMono(Integer.class)
//                .flatMap(userId -> {
//                    ServerHttpRequest modifiedRequest = request.mutate()
//                            .header(HttpHeaders.AUTHORIZATION, token)
//                            .header("X-User-Id", String.valueOf(userId))
//                            .build();
//                    ServerWebExchange modifiedExchange = exchange.mutate()
//                            .request(modifiedRequest)
//                            .build();
//
//                    logger.debug("Token verified, forwarding request with Authorization and X-User-Id headers");
//                    return chain.filter(modifiedExchange);
//                })
//                .onErrorResume(error -> {
//                    logger.error("Error verifying token", error);
//                    return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
//                });
//    };
//}
