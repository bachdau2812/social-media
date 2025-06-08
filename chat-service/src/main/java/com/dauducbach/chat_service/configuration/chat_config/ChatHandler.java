package com.dauducbach.chat_service.configuration.chat_config;

import com.dauducbach.chat_service.dto.request.MessageRequest;
import com.dauducbach.chat_service.dto.request.PingRequest;
import com.dauducbach.chat_service.service.EncodingUtils;
import com.dauducbach.chat_service.service.MessageService;
import com.dauducbach.chat_service.service.PresenceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatHandler implements WebSocketHandler {
    WebSocketSessionRegistry sessionRegistry;
    EncodingUtils encodingUtils;
    MessageService messageService;
    PresenceService presenceService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String userId = getUserIdFromSession(session);
        sessionRegistry.addSession(userId, session);
        presenceService.setOnline(userId).subscribe();

        return session.receive()
                .doOnSubscribe(sub -> log.info("Start listening for messages"))
                .doOnNext(msg -> log.info("Raw WebSocketMessage: {}", msg))
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(payload -> log.info("Received payload: {}", payload))
                .map(payload -> encodingUtils.decode(payload, MessageRequest.class))
                .doOnError(ex -> log.info("Error while decode: {}", ex.getMessage()))
                .doOnNext(request -> log.info("Received message request {} from {}", request, request.getMessageTo()))
                .flatMap(this::handleIncomingMessage)
                .onErrorResume(e -> {
                    log.error("Failed to process message: {}", e.getMessage());
                    return Mono.empty();
                })
                .then(Mono.<Void>never())
                .doFinally(signalType -> {
                    log.info("WebSocket disconnected: {}", signalType);
                    sessionRegistry.removeSession(userId);
                    presenceService.setOffline(userId);
                });
    }

    public Mono<Void> handleIncomingMessage(MessageRequest request) {
        log.info("In handleIncomingMessage");
        return messageService.saveMessage(request)
                .flatMap(message -> {
                    log.info("Request: {}", request);
                    var responseJson = encodingUtils.encode(message);
                    log.info("Encode response: {}", responseJson);
                    WebSocketSession receiver = sessionRegistry.getSession(request.getMessageTo());

                    if (receiver != null && receiver.isOpen())  {
                        return receiver.send(Mono.just(receiver.textMessage(responseJson)))
                                .doOnSuccess(unused -> log.info("Send message to {} complete: {}", request.getMessageTo(), responseJson));
                    }

                    return Mono.empty();
                });
    }

    public String getUserIdFromSession(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        String query = uri.getQuery();
        if (query != null && query.startsWith("userId=")) {
            return query.substring(7);
        }

        return "unknown";
    }


}
