package com.example.grid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Sets up STOMP-over-WebSocket, our real-time layer.
 *
 * <p>How the messaging works:
 * <ul>
 *   <li>Browsers connect to <b>/ws</b> (SockJS adds fallbacks for old networks).</li>
 *   <li>Browsers <b>send</b> to destinations starting with <b>/app</b>
 *       (handled by @MessageMapping methods).</li>
 *   <li>The server <b>broadcasts</b> to destinations starting with <b>/topic</b>
 *       (every subscriber receives them) and <b>/queue</b> (per-user private).</li>
 * </ul>
 * This is the Observer / publish-subscribe pattern: the server publishes a tile
 * change once, and the broker delivers it to all subscribed clients.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory broker. /topic = broadcast, /queue = per-user.
        registry.enableSimpleBroker("/topic", "/queue");
        // Client -> server messages are prefixed with /app.
        registry.setApplicationDestinationPrefixes("/app");
        // Enables /user/** destinations for private messages (e.g. cooldown errors).
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // dev: allow the Angular dev server
                .withSockJS();
    }
}
