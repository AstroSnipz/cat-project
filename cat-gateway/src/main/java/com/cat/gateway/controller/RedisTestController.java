package com.cat.gateway.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class RedisTestController {

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisTestController(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis-test")
    public Mono<String> testRedis() {
        return redisTemplate.opsForValue()
                .set("cat:test", "working", Duration.ofSeconds(60))
                .then(redisTemplate.opsForValue().get("cat:test"));
    }
}
