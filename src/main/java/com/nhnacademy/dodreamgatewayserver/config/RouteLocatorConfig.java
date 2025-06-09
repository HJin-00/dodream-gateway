package com.nhnacademy.dodreamgatewayserver.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfig {


    @Bean
    public RouteLocator customRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("hello-api",r->r.path("/hello")
                        .uri("lb://hello-api"))
                .build();
    }
}
