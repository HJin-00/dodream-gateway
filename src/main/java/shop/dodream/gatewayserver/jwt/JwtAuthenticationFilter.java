package shop.dodream.gatewayserver.jwt;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import shop.dodream.gatewayserver.dto.SessionUser;
import shop.dodream.gatewayserver.repository.TokenRepository;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        if (path.startsWith("/auth/")
                || path.startsWith("/public/")
                || path.startsWith("/actuator/")) {
            return chain.filter(exchange);
        }
        String token = extractToken(request);

        if(token != null){
            log.debug("token: {}", token);
            try{
                String uuid = jwtTokenProvider.getUuidFromToken(token);
                Optional<SessionUser> optionalUser = tokenRepository.findSessionUser(uuid);
                if(optionalUser.isEmpty()){
                    log.warn("Redis에 세션정보 없음. uuid: {}", uuid);
                    return chain.filter(exchange);
                }
                SessionUser sessionUser = optionalUser.get();
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + sessionUser.getRole().name()));
                Authentication auth = new UsernamePasswordAuthenticationToken(sessionUser.getUserId(), null, authorities);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-USER-ID", sessionUser.getUserId())
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
        return null;
    }
}
