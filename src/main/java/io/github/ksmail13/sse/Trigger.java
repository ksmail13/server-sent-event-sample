package io.github.ksmail13.sse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Optional;

@Component
public class Trigger implements HandlerFunction<ServerResponse> {
    private static final Logger logger = Loggers.getLogger(Trigger.class);

    private EmitterProcessor<String> sessionProcessor;

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        Optional<String> session = request.queryParam("session");
        logger.info("handle session {}", session);
        session.ifPresent(s -> sessionProcessor.onNext(s));
        return ServerResponse.noContent().build();
    }

    public EmitterProcessor<String> getSessionProcessor() {
        return sessionProcessor;
    }

    @Autowired
    public void setSessionProcessor(EmitterProcessor<String> sessionProcessor) {
        this.sessionProcessor = sessionProcessor;
    }
}
