package com.example.grid.dto;

/** Incoming WebSocket message: "user {userId} clicked tile {tileId}". */
public record ClaimMessage(Long tileId, Long userId) {
}
