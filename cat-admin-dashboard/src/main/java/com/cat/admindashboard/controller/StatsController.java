package com.cat.admindashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StatsController {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @GetMapping("/stats")
    public Mono<Object> getClassificationStats() {
        Mono<Long> bots = redisTemplate.keys("classify:*")
                .flatMap(key -> redisTemplate.opsForValue().get(key))
                .filter(value -> value.equalsIgnoreCase("bot"))
                .count();

        Mono<Long> humans = redisTemplate.keys("classify:*")
                .flatMap(key -> redisTemplate.opsForValue().get(key))
                .filter(value -> value.equalsIgnoreCase("human"))
                .count();

        return Mono.zip(bots, humans)
                .map(tuple -> {
                    long botCount = tuple.getT1();
                    long humanCount = tuple.getT2();
                    return java.util.Map.of("bot", botCount, "human", humanCount);
                });
    }
}
