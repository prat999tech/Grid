package com.example.grid.service.rules;

/**
 * A single game rule (Strategy pattern).
 *
 * <p>Each rule decides whether a capture is allowed. The service runs every rule
 * before applying a capture. To add a new rule (e.g. "you can't capture a tile you
 * already own", or "area cooldown"), just create another {@code @Component}
 * implementing this interface — no existing code changes. That's the
 * Open/Closed Principle in action.
 */
public interface ClaimRule {

    /**
     * @throws com.example.grid.exception.ClaimRejectedException if the capture must be blocked.
     */
    void check(ClaimContext context);
}
