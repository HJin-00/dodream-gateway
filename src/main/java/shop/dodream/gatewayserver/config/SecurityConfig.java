package shop.dodream.gatewayserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import shop.dodream.gatewayserver.jwt.JwtAuthenticationFilter;
import shop.dodream.gatewayserver.jwt.JwtTokenProvider;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges-> exchanges
                        .pathMatchers("/auth/**","/actuator/**","/public/**").permitAll()
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt(new JwtAuthenticationFilter(jwtTokenProvider), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
