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

                // Auth API
                .route("auth", r -> r.path("/auth/**")
                        .uri("lb://AUTH"))

                // Auth Swagger
                .route("auth-swagger", r -> r.path("/swagger/auth/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://AUTH"))

                // User API
                .route("user", r -> r.path("/users/**",
                                "/admin/users/**", "/admin/user-grades/**", "/admin/point-policies/**",
                                "/public/users/**", "/public/user-grades/**", "/public/point-policies/**")
                        .uri("lb://USER"))

                // User Swagger
                .route("user-swagger", r -> r.path("/swagger/user/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://USER"))

                // Order API
                .route("order", r -> r.path("/orders/**", "/admin/orders/**")
                        .uri("lb://ORDER"))

                // Order Swagger
                .route("order-swagger", r -> r.path("/swagger/order/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://ORDER"))

                // Cart API
                .route("cart", r -> r.path("/carts/**", "/public/carts/**")
                        .uri("lb://CART"))

                // Cart Swagger
                .route("cart-swagger", r -> r.path("/swagger/cart/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://CART"))

                // Coupon API
                .route("coupon", r -> r.path("/coupons/**",
                                "/admin/coupons/**", "/admin/user-coupons/**", "/admin/coupon-policies/**")
                        .uri("lb://COUPON"))

                // Coupon Swagger
                .route("coupon-swagger", r -> r.path("/swagger/coupon/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://COUPON"))

                // Book, Review, Category, Tags API
                .route("book", r -> r.path("/books/**", "/categories/**", "/tags/**", "/likes/**", "/reviews/**",
                                "/admin/books/**", "/admin/categories/**", "/admin/tags/**", "/admin/reviews/**",
                                "/public/books/**", "/public/categories/**", "/public/tags/**", "/public/reviews/**")
                        .uri("lb://BOOK"))

                // Book Swagger
                .route("book-swagger", r -> r.path("/swagger/book/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://BOOK"))

                .build();
    }
}
