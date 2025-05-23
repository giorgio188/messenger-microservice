package com.messenger.auth_service.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.messenger.auth_service.security.JWTUtil;
import com.messenger.auth_service.services.UserProfileDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter{

    private final JWTUtil jwtUtil;
    private final UserProfileDetailsService  userProfileDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isEmpty() && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if(token.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT token in bearer header");
                return; // Добавляем return, чтобы прервать обработку запроса
            } else {
                try {
                    Optional<String> username = jwtUtil.verifyToken(token);
                    if (username.isEmpty()) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username in JWT token");
                        return;
                    }

                    UserDetails userDetails = userProfileDetailsService.loadUserByUsername(username.get());
                    if (userDetails == null) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                        return;
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                    userDetails.getPassword(),
                                    userDetails.getAuthorities());

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (JWTVerificationException e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                    return;
                } catch (UsernameNotFoundException e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
