package shop.dodream.gatewayserver.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String token = extractToken(request);

        if(token != null){
            try{
                Claims claims = jwtTokenProvider.parseClaims(token);
                String role = claims.get("role", String.class);
                String userId = claims.getSubject();

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-USER-ID", userId)
                        .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();


                return chain.filter(modifiedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
            }catch (Exception e){
                log.warn("JWT 인증 실패: {}", e.getMessage());
            }
        }
        return chain.filter(exchange);
    }


    private String extractToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().getOrEmpty("Authorization");
        if (!authHeaders.isEmpty()) {
            String bearer = authHeaders.getFirst();
            if (bearer != null && bearer.startsWith(BEARER_PREFIX)) {
                return bearer.substring(BEARER_PREFIX.length());
            }
        }
        return Optional.ofNullable(request.getCookies().getFirst(ACCESS_TOKEN_COOKIE))
                .map(HttpCookie::getValue)
                .orElse(null);
    }
}
