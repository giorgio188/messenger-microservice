package com.messenger.api_gateway;

import lombok.var;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component("JWTAuthenticationFilter")
public class JWTAuthenticationFilterFactory extends AbstractGatewayFilterFactory<JWTAuthenticationFilterFactory.Config> {

    private final WebClient.Builder webClientBuilder;

    public JWTAuthenticationFilterFactory(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            String token = request.getHeaders().getFirst("Authorization");

            if (token == null || token.isEmpty()) {
                return onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            return webClientBuilder.build()
                    .post()
                    .uri("http://auth-service/api/auth/verify")
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .toBodilessEntity()
                    .flatMap(response -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return chain.filter(exchange);
                        } else {
                            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                        }
                    })
                    .onErrorResume(e -> {
                        return onError(exchange, "Failed to validate token", HttpStatus.INTERNAL_SERVER_ERROR);
                    });
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus status) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
        // Конфигурация фильтра (если нужна)
    }
}