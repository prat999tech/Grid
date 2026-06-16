package com.example.grid.dto;

/**
 * One row of the leaderboard: a player and how many tiles they currently own.
 * Built straight from a database aggregate query (see TileRepository#leaderboard).
 */
public record LeaderboardEntry(Long userId, String name, String color, long tiles) {
}
