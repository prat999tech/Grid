package com.example.grid.service;

import com.example.grid.dto.GridSnapshot;
import com.example.grid.dto.LeaderboardEntry;
import com.example.grid.dto.TileView;
import com.example.grid.exception.ClaimRejectedException;
import com.example.grid.model.AppUser;
import com.example.grid.model.Tile;
import com.example.grid.repository.TileRepository;
import com.example.grid.repository.UserRepository;
import com.example.grid.service.rules.ClaimContext;
import com.example.grid.service.rules.ClaimRule;
import com.example.grid.service.rules.CooldownTracker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Business logic for the board. This is the only place that changes tile
 * ownership, so all the rules live in one understandable spot.
 *
 * <p>It depends on the {@link ClaimRule} <i>interface</i> (a list of them),
 * not on concrete rules — so new rules plug in without editing this class
 * (Dependency Inversion + Open/Closed).
 */
@Service
public class TileService {

    private final TileRepository tileRepository;
    private final UserRepository userRepository;
    private final List<ClaimRule> rules;
    private final CooldownTracker cooldownTracker;
    private final int cols;
    private final int rows;

    public TileService(TileRepository tileRepository,
                       UserRepository userRepository,
                       List<ClaimRule> rules,
                       CooldownTracker cooldownTracker,
                       @Value("${grid.cols}") int cols,
                       @Value("${grid.rows}") int rows) {
        this.tileRepository = tileRepository;
        this.userRepository = userRepository;
        this.rules = rules;
        this.cooldownTracker = cooldownTracker;
        this.cols = cols;
        this.rows = rows;
    }

    /** Full board for a freshly loaded page. */
    @Transactional(readOnly = true)
    public GridSnapshot snapshot() {
        List<TileView> tiles = tileRepository.findAllByOrderByYAscXAsc()
                .stream().map(TileView::from).toList();
        return new GridSnapshot(cols, rows, tiles);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> leaderboard() {
        return tileRepository.leaderboard();
    }

    /**
     * Applies a capture and returns the new tile state.
     *
     * <p>Runs inside a database transaction. The caller ({@code ClaimProcessor})
     * holds a per-tile lock around this call, so two captures on the same tile are
     * processed one after another and can never corrupt each other. The tile's
     * {@code @Version} column is an extra safety net at the database level.
     *
     * @throws ClaimRejectedException if the user/tile is unknown or a rule blocks it.
     */
    @Transactional
    public TileView capture(Long tileId, Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ClaimRejectedException("NO_USER", "Unknown user — please log in again."));
        Tile tile = tileRepository.findById(tileId)
                .orElseThrow(() -> new ClaimRejectedException("NO_TILE", "That tile does not exist."));

        Instant now = Instant.now();
        ClaimContext context = new ClaimContext(user, tile, now);

        // Run every game rule (Strategy pattern). Any rule may veto the capture.
        for (ClaimRule rule : rules) {
            rule.check(context);
        }

        tile.captureBy(user.getId(), user.getName(), user.getColor(), now);
        Tile saved = tileRepository.save(tile);

        // Start this user's cooldown only after a successful capture.
        cooldownTracker.record(userId, now);
        return TileView.from(saved);
    }
}
