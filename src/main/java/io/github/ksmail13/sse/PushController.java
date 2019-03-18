package io.github.ksmail13.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
public class PushController {

    private Duration heartbeatPeriod = Duration.ofSeconds(1);

    @GetMapping(path = "/push")
    public Flux<ServerSentEvent<String>> testEventStream(@RequestParam String session) {
        log.debug("event handling with session : {}", session);
        Flux<ServerSentEvent<String>> pingFlux = Flux.interval(heartbeatPeriod).map(l -> newPingMessage(session));
        return Flux.merge(Flux.fromIterable(Arrays.asList(pingFlux))).log("test-event");
    }

    @PostMapping(path = "/push", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Mono<ServerSentEvent<String>> testLongPollingStream(@RequestParam String session) {
        log.debug("long polling handling with session : {}", session);
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ServerSentEvent.builder("Data").event(EventType.EVENT.toString()).build();
        }))
                .timeout(heartbeatPeriod)
                .onErrorResume(TimeoutException.class,
                        (TimeoutException e) -> Mono.just(newPingMessage(session)));
    }

    private ServerSentEvent<String> newPingMessage(String session) {
        log.debug("ping in session {}", session);
        String time = LocalDateTime.now().toString();
        String msg = String.format("{\"time\":\"%s\"}", time);
        return ServerSentEvent
                .builder(msg)
                .id("ping-"+session+"-"+time)
                .event(EventType.PING.toString())
                .build();
    }

    @Autowired
    public void setHeartbeatPeriod(Duration heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
    }
}
