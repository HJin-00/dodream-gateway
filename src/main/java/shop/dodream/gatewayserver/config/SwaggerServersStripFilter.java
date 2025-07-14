package shop.dodream.gatewayserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SwaggerServersStripFilter implements GlobalFilter, Ordered {

    private static final Pattern SERVERS_FIELD_PATTERN = Pattern.compile("\\\"servers\\\"\\s*:\\s*\\[[^]]*],?");
    private final WebClient webClient;
    private final ReactiveDiscoveryClient discoveryClient;

    public SwaggerServersStripFilter(ReactiveDiscoveryClient discoveryClient) {
        this.webClient = WebClient.create();
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (path.matches("/swagger/[^/]+/v3/api-docs")) {
            String serviceName = extractServiceName(path);
            return discoveryClient.getInstances(serviceName.toUpperCase())
                    .next()
                    .flatMap(instance -> {
                        String targetUrl = instance.getUri().toString() + "/v3/api-docs";
                        return webClient.get()
                                .uri(targetUrl)
                                .retrieve()
                                .bodyToMono(String.class)
                                .map(this::stripServersField)
                                .flatMap(body -> {
                                    ServerHttpResponse response = exchange.getResponse();
                                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                                    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
                                });
                    });
        }

        return chain.filter(exchange);
    }

    private String extractServiceName(String path) {
        String[] parts = path.split("/");
        return parts.length >= 3 ? parts[2] : "unknown";
    }

    private String stripServersField(String body) {
        return SERVERS_FIELD_PATTERN.matcher(body).replaceAll("");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}