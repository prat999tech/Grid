package com.example.grid.dto;

import java.util.List;

/** Full board sent once when a client loads the page (GET /api/grid). */
public record GridSnapshot(int cols, int rows, List<TileView> tiles) {
}
