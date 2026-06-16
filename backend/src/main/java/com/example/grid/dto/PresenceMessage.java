package com.example.grid.dto;

/** Broadcast on /topic/presence whenever someone connects or disconnects. */
public record PresenceMessage(int online) {
}
