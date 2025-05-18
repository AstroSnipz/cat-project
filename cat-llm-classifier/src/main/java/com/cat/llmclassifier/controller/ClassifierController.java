package com.cat.llmclassifier.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ClassifierController {

    @GetMapping("/classify")
    public Mono<String> classifyUser(@RequestParam("userId") String userId) {
        // Simple mock logic:
        // if userId contains "bot" → classify as bot, else human
        if (userId.toLowerCase().contains("bot")) {
            return Mono.just("bot");
        }
        return Mono.just("human");
    }
}
