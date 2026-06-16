package com.example.grid.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Hands out a lock per tile so that two worker threads can never capture the
 * SAME tile at the same instant — that's our core conflict-handling mechanism.
 *
 * <p>We use "lock striping": a fixed array of locks shared across all tiles
 * (tileId % STRIPES picks one). This bounds memory (we don't keep one lock per
 * tile forever) while still letting captures on <i>different</i> tiles run fully
 * in parallel. Two different tiles only wait on each other in the rare case they
 * map to the same stripe, which is harmless.
 *
 * <p>The database {@code @Version} column is a second safety net: even if locking
 * were bypassed, JPA would reject a concurrent stale write.
 */
@Component
public class TileLockRegistry {

    private static final int STRIPES = 64;
    private final ReentrantLock[] locks = new ReentrantLock[STRIPES];

    public TileLockRegistry() {
        for (int i = 0; i < STRIPES; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /** The lock guarding the given tile id. Always lock/unlock in a try/finally. */
    public ReentrantLock lockFor(long tileId) {
        return locks[(int) (Math.floorMod(tileId, STRIPES))];
    }
}
