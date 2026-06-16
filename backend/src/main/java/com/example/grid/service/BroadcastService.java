package com.example.grid.service;

import com.example.grid.dto.ErrorMessage;
import com.example.grid.dto.LeaderboardEntry;
import com.example.grid.dto.PresenceMessage;
import com.example.grid.dto.TileView;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The "publisher" side of our publish-subscribe setup. Its only job is to push
 * messages to clients through the STOMP broker (Single Responsibility Principle).
 * Nothing here knows about game rules or the database.
 */
@Service
public class BroadcastService {

    private final SimpMessagingTemplate messaging;

    public BroadcastService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /** Tell every connected client that one tile changed. */
    public void tileChanged(TileView tile) {
        messaging.convertAndSend("/topic/tiles", tile);
    }

    /** Push the refreshed leaderboard to everyone. */
    public void leaderboard(List<LeaderboardEntry> entries) {
        messaging.convertAndSend("/topic/leaderboard", entries);
    }

    /** Tell everyone how many players are currently online. */
    public void presence(int online) {
        messaging.convertAndSend("/topic/presence", new PresenceMessage(online));
    }

    /**
     * Send a private message to one user's session (e.g. cooldown rejection).
     *
     * <p>We have no login/principal, so we target the exact STOMP session by id.
     * Setting the session id in the message headers tells Spring to deliver to
     * just that one connection rather than treating the id as a username.
     */
    public void errorToUser(String sessionId, ErrorMessage error) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setSessionId(sessionId);
        headers.setLeaveMutable(true);
        messaging.convertAndSendToUser(sessionId, "/queue/errors", error, headers.getMessageHeaders());
    }
}
