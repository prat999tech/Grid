package com.example.grid.service.rules;

import com.example.grid.model.AppUser;
import com.example.grid.model.Tile;

import java.time.Instant;

/** Everything a rule needs to decide whether a capture is allowed. */
public record ClaimContext(AppUser user, Tile tile, Instant now) {
}
