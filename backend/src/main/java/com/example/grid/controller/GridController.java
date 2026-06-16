package com.example.grid.controller;

import com.example.grid.dto.GridSnapshot;
import com.example.grid.dto.LeaderboardEntry;
import com.example.grid.service.TileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for reading the board. Live changes come over WebSocket;
 * these are used once on page load (and to refresh on reconnect).
 */
@RestController
@RequestMapping("/api")
public class GridController {

    private final TileService tileService;

    public GridController(TileService tileService) {
        this.tileService = tileService;
    }

    /** The whole board, sent when a client first loads the page. */
    @GetMapping("/grid")
    public GridSnapshot grid() {
        return tileService.snapshot();
    }

    /** Current standings (also pushed live over /topic/leaderboard). */
    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard() {
        return tileService.leaderboard();
    }
}
