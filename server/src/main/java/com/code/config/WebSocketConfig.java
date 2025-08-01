// WebSocketConfig.java
package com.code.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MailWebSocketHandler mailWebSocketHandler;
    private final AuthHandshakeInterceptor authInterceptor;

    public WebSocketConfig(MailWebSocketHandler mailWebSocketHandler,
                           AuthHandshakeInterceptor authInterceptor) {
        this.mailWebSocketHandler = mailWebSocketHandler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mailWebSocketHandler, "/ws/mail/{email}")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*");
    }
}
