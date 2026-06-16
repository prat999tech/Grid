package com.example.grid.service;

import com.example.grid.dto.ClaimMessage;
import com.example.grid.dto.ErrorMessage;
import com.example.grid.dto.TileView;
import com.example.grid.exception.ClaimRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Turns an incoming click into a capture, then broadcasts the result.
 *
 * <p>This is the asynchronous, multi-threaded heart of the app:
 * <ol>
 *   <li>The WebSocket controller hands a click here and immediately returns —
 *       it never waits for the database. {@code @Async("claimExecutor")} makes
 *       this method run on a worker thread from our pool.</li>
 *   <li>We take a per-tile lock so concurrent captures of the SAME tile are
 *       serialised (conflict handling). Different tiles run fully in parallel.</li>
 *   <li>{@link TileService#capture} does the transactional update <i>inside</i>
 *       the lock, so the commit is finished before we release it.</li>
 *   <li>On success we publish the change to everyone; on a rule rejection we
 *       reply privately to just that one user.</li>
 * </ol>
 */
@Service
public class ClaimProcessor {

    private static final Logger log = LoggerFactory.getLogger(ClaimProcessor.class);

    private final TileService tileService;
    private final BroadcastService broadcastService;
    private final TileLockRegistry lockRegistry;

    public ClaimProcessor(TileService tileService,
                          BroadcastService broadcastService,
                          TileLockRegistry lockRegistry) {
        this.tileService = tileService;
        this.broadcastService = broadcastService;
        this.lockRegistry = lockRegistry;
    }

    @Async("claimExecutor")
    public void process(ClaimMessage message, String sessionId) {
        if (message.tileId() == null || message.userId() == null) {
            return; // malformed message, ignore
        }

        ReentrantLock lock = lockRegistry.lockFor(message.tileId());
        lock.lock();
        try {
            TileView updated = tileService.capture(message.tileId(), message.userId());
            // Commit is done (transaction returned). Now tell the world.
            broadcastService.tileChanged(updated);
            broadcastService.leaderboard(tileService.leaderboard());
        } catch (ClaimRejectedException rejected) {
            // Expected, e.g. cooldown — reply only to the clicking user.
            broadcastService.errorToUser(sessionId,
                    new ErrorMessage(rejected.getCode(), rejected.getMessage()));
        } catch (Exception unexpected) {
            log.error("Failed to process capture {}", message, unexpected);
            broadcastService.errorToUser(sessionId,
                    new ErrorMessage("ERROR", "Something went wrong, please try again."));
        } finally {
            lock.unlock();
        }
    }
}
