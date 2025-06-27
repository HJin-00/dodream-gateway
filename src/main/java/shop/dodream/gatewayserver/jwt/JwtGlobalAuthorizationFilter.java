package shop.dodream.gatewayserver.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtGlobalAuthorizationFilter implements GlobalFilter, Ordered {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if(path.startsWith("/auth")||path.contains("login")||path.startsWith("/payco/callback")||path.contains("dormant") || path.startsWith("/accounts")){
            return chain.filter(exchange);
        }

        String token = extractTokenFromCookie(request, "accessToken");

        if (token == null) {
            return onUnauthorized(exchange, "Missing accessToken cookie");
        }

        try{
            Claims claims = jwtTokenProvider.parseClaims(token);
            String role = claims.get("role", String.class);
            String userId = claims.getSubject();

            if(path.startsWith("/admin") && !"ADMIN".equals(role)){
                return onUnauthorized(exchange,"Only ADMIN role required");
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-USER-ID", userId)
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }catch (Exception e){
            log.warn("JWT parsing failed: {}", e.getMessage());
            return onUnauthorized(exchange,"Invalid token");
        }
    }

    private String extractTokenFromCookie(ServerHttpRequest request, String cookieName) {
        return request.getCookies().getFirst(cookieName) != null ?
                request.getCookies().getFirst(cookieName).getValue() : null;
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> onUnauthorized(ServerWebExchange exchange, String message) {
        log.warn("[AUTH BLOCKED]: {}",message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }


}
