package com.cat.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
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
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId == null || userId.isEmpty()) {
                userId = exchange.getRequest().getQueryParams().getFirst("userId");
            }

            if (userId == null || userId.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }

            String key = "classify:" + userId;

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
                        if ("bot".equalsIgnoreCase(classification)) {
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    });
        };
    }
}
