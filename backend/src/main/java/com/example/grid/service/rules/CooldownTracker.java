package com.example.grid.service.rules;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers when each user last captured a tile, so we can enforce a cooldown.
 *
 * <p>Single responsibility: just the timestamps. The {@link CooldownRule} reads
 * from it; {@code TileService} writes to it after a successful capture.
 *
 * <p>A {@link ConcurrentHashMap} is used because several worker threads may
 * read/write it at the same time (see the async worker pool).
 */
@Component
public class CooldownTracker {

    private final ConcurrentHashMap<Long, Instant> lastClaimByUser = new ConcurrentHashMap<>();

    /** True if the user is still within their cooldown window. */
    public boolean isCoolingDown(Long userId, Instant now, Duration cooldown) {
        Instant last = lastClaimByUser.get(userId);
        return last != null && Duration.between(last, now).compareTo(cooldown) < 0;
    }

    /** Call after a capture succeeds to start the user's next cooldown. */
    public void record(Long userId, Instant when) {
        lastClaimByUser.put(userId, when);
    }
}
