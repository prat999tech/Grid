package com.example.grid.controller;

import com.example.grid.dto.ClaimMessage;
import com.example.grid.service.ClaimProcessor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * Receives clicks over WebSocket.
 *
 * <p>A browser sends a {@link ClaimMessage} to <b>/app/claim</b>. We grab the
 * STOMP session id (so a private reply can be sent if the capture is rejected)
 * and hand everything to {@link ClaimProcessor}, which does the work on a
 * background thread. This method returns instantly, keeping the messaging
 * layer fast even when many clicks arrive at once.
 */
@Controller
public class TileWebSocketController {

    private final ClaimProcessor claimProcessor;

    public TileWebSocketController(ClaimProcessor claimProcessor) {
        this.claimProcessor = claimProcessor;
    }

    @MessageMapping("/claim")
    public void claim(ClaimMessage message, SimpMessageHeaderAccessor headers) {
        claimProcessor.process(message, headers.getSessionId());
    }
}
