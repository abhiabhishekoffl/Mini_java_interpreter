package com.novalang.config;

import com.novalang.controller.ReplWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ReplWebSocketHandler replWebSocketHandler;

    public WebSocketConfig(ReplWebSocketHandler replWebSocketHandler) {
        this.replWebSocketHandler = replWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(replWebSocketHandler, "/ws/repl")
                .setAllowedOrigins("*");
    }
}
