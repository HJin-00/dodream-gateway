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
                //Auth
                .route("auth",r -> r.path("/auth/**")
                        .uri("lb://AUTH"))
                //User
                .route("user",r->r.path("/users/**",
                                "/admin/users/**","/admin/user-grades/**","/admin/point-policies/**",
                                "/public/users/**","/public/user-grades/**", "/public/point-policies/**")
                        .uri("lb://USER"))
                //Order
                .route("order",r->r.path("/orders/**",
                                "/admin/orders/**")
                        .uri("lb://ORDER"))
                //Cart
                .route("cart",r->r.path("/carts/**",
                        "/public/carts/**")
                        .uri("lb://CART"))
                //Coupon
                .route("coupon", r -> r.path(
                                "/coupons/**",
                                "/admin/coupons/**","/admin/user-coupons/**","/admin/coupon-policies/**")
                        .uri("lb://COUPON"))
                //book, review, category,tags
                .route("book",r->r.path("/books/**","/categories/**","/tags/**","/likes/**","/reviews/**",
                                "/admin/books/**","/admin/categories/**","/admin/tags/**","/admin/reviews/**",
                                "/public/books/**","/public/categories/**","/public/tags/**","/public/reviews/**")
                        .uri("lb://BOOK"))

                .build();
    }
}
