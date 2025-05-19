package com.cat.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class UserClassificationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final WebClient webClient;

    public UserClassificationGatewayFilterFactory(ReactiveStringRedisTemplate redisTemplate) {
        super(Object.class);
        this.redisTemplate = redisTemplate;
        this.webClient = WebClient.create("http://localhost:8081");
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            String userIdQuery = exchange.getRequest().getQueryParams().getFirst("userId");
            final String userId = (userIdHeader != null && !userIdHeader.isEmpty()) ? userIdHeader : userIdQuery;

            if (userId == null || userId.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }

            final String key = "classify:" + userId;

            return redisTemplate.opsForValue().get(key)
                    .switchIfEmpty(
                            webClient.get()
                                    .uri("/classify?userId={userId}", userId)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .flatMap(classification ->
                                            redisTemplate.opsForValue()
                                                    .set(key, classification, Duration.ofMinutes(5))
                                                    .thenReturn(classification)
                                    )
                    )
                    .flatMap(classification -> {
                        // Debug print
                        System.out.println("Classification for user " + userId + ": " + classification);

                        if ("bot".equalsIgnoreCase(classification)) {
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            return exchange.getResponse().setComplete();
                        }

                        return chain.filter(exchange);
                    });
        };
    }
}
