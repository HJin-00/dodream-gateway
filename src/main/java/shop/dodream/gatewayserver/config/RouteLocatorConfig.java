package shop.dodream.gatewayserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouteLocatorConfig {

    @Bean
    public RouteLocator customRoute(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("auth",r -> r.path("/auth/**")
                        .uri("lb://AUTH-SERVICE"))
                .build();
    }
}
