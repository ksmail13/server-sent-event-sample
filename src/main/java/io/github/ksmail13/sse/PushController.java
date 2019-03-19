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
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
public class PushController {

    private Duration heartbeatPeriod = Duration.ofSeconds(1);
    private Trigger trigger;

    @GetMapping(path = "/push")
    public Flux<ServerSentEvent<String>> testEventStream(@RequestParam String session) {
        log.debug("event handling with session : {}", session);

        // interval execution for ping message
        Flux<ServerSentEvent<String>> pingFlux = Flux
                .interval(heartbeatPeriod)
                .map(l -> newPingMessage(session));

        Flux<ServerSentEvent<String>> trigger = getTrigger(session);

        return Flux.merge(Flux.fromIterable(Arrays.asList(pingFlux, trigger))).log("test-event");
    }

    @PostMapping(path = "/push", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Mono<ServerSentEvent<String>> testLongPollingStream(@RequestParam String session) {
        log.debug("long polling handling with session : {}", session);
        return getTrigger(session).next()
                .timeout(heartbeatPeriod)
                .onErrorResume(TimeoutException.class,
                        (TimeoutException e) -> Mono.just(newPingMessage(session)));
    }

    private Flux<ServerSentEvent<String>> getTrigger(String session) {
        return this.trigger.getSessionProcessor()
                .filter(s -> s.equals(session))
                .map(this::newTriggeredMessage);
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

    private ServerSentEvent<String> newTriggeredMessage(String session) {
        log.debug("triggered in session {}", session);
        String time = LocalDateTime.now().toString();
        String msg = String.format("{\"time\":\"%s\", \"session\":\"%s\"}", time, session);
        return ServerSentEvent
                .builder(msg)
                .id("trigger-"+session+"-"+time)
                .build();
    }

    @Autowired
    public void setHeartbeatPeriod(Duration heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
    }

    @Autowired
    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
}
