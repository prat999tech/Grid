package com.example.grid.controller;

import com.example.grid.service.BroadcastService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks how many clients are connected and broadcasts the count whenever it
 * changes. Spring fires these events for every WebSocket connect/disconnect.
 *
 * <p>{@link AtomicInteger} is used because connects and disconnects can happen
 * on different threads concurrently.
 */
@Component
public class PresenceListener {

    private final AtomicInteger online = new AtomicInteger(0);
    private final BroadcastService broadcastService;

    public PresenceListener(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        broadcastService.presence(online.incrementAndGet());
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        broadcastService.presence(Math.max(0, online.decrementAndGet()));
    }
}
