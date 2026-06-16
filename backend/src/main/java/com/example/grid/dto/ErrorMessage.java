package com.example.grid.dto;

/**
 * Sent privately to a single user's session (e.g. "you're on cooldown").
 * Other players never see it.
 */
public record ErrorMessage(String code, String message) {
}
