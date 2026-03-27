package com.finflow.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
public class JwtFilter implements GlobalFilter, Ordered {
    // GlobalFilter → har request pe run hoga
    // Ordered → filter ka order define karta hai

    @Value("${jwt.secret}")
    private String secret;

    // Open routes — JWT validate nahi hoga
    private static final String[] OPEN_ROUTES = {
            "/gateway/auth/signup",
            "/gateway/auth/login",
            "/swagger-ui",
            "/v3/api-docs",
            "/gateway/auth/v3/api-docs",
            "/gateway/applications/v3/api-docs",
            "/gateway/documents/v3/api-docs",
            "/gateway/admin/v3/api-docs"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();

        // Step 1: Open route hai toh seedha aage bhejo
        if (isOpenRoute(path)) {
            return chain.filter(exchange);
        }

        // Step 2: Authorization header check karo
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Token nahi hai → 401 return karo
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Step 3: Token validate karo
        try {
            String token = authHeader.substring(7);
            Claims claims = extractClaims(token);

            // Step 4: UserId aur Role headers mein inject karo
            // Downstream services yeh headers use karenge

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .headers(headers -> {
                                // Remove client-supplied headers first ← this is the key
                                headers.remove("X-User-Id");
                                headers.remove("X-User-Role");
                            })
                            .header("X-User-Id", claims.get("userId").toString())
                            .header("X-User-Role", claims.get("role").toString())
                            .build())
                    .build();
//            The .header() method in Spring WebFlux appends to existing headers, it does not replace them.
//            So if you send X-User-Role: ADMIN from Postman, and the Gateway also
//            adds X-User-Role: APPLICANT from your JWT, the downstream service receives both values.
//             When it calls request.getHeader("X-User-Role") it gets the first one — which is your manually injected ADMIN.
            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            // Token invalid ya expired → 401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    // Check if route is open
//    private boolean isOpenRoute(String path) {
//        for (String route : OPEN_ROUTES) {
//            if (path.equals(route)) {
//                return true;
//            }
//        }
//        return false;
//    }
    private boolean isOpenRoute(String path) {
        for (String route : OPEN_ROUTES) {
            if (path.equals(route) || path.startsWith(route)) {
                return true;
            }
        }
        return false;
    }

    // Extract claims from token
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Get signing key from secret
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public int getOrder() {
        return -1; // Sabse pehle run hoga
    }
}