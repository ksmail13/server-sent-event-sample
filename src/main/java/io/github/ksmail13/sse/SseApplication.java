package io.github.ksmail13.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Optional;

@SpringBootApplication
public class SseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SseApplication.class, args);
    }

    @Bean
    public Duration heartbeatPeriod() {
        return Duration.ofSeconds(5);
    }

    @Bean
    public RouterFunction<ServerResponse> routeTrigger(Trigger trigger) {
        return RouterFunctions.route()
                .GET("/trigger", request -> request.queryParams().containsKey("session"), trigger).build();
    }

    @Bean
    public EmitterProcessor<String> sessionTopicProcessor() {
        return EmitterProcessor.create();
    }
}
