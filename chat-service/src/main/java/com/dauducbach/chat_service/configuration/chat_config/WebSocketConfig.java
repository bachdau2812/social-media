package com.dauducbach.chat_service.configuration.chat_config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
@EnableWebFlux
@RequiredArgsConstructor
public class WebSocketConfig {
    private final ChatHandler chatHandler;

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public HandlerMapping webSocketMapping() {
        return new SimpleUrlHandlerMapping(
                Map.of("/ws/chat", chatHandler),
                -1
        );
    }
}
