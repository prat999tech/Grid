package com.example.grid.service.rules;

import com.example.grid.exception.ClaimRejectedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Rule: a user must wait {@code grid.cooldown-ms} between captures. Stops one
 * person from spam-clicking and flooding everyone else with updates.
 */
@Component
public class CooldownRule implements ClaimRule {

    private final CooldownTracker tracker;
    private final Duration cooldown;

    public CooldownRule(CooldownTracker tracker,
                        @Value("${grid.cooldown-ms}") long cooldownMs) {
        this.tracker = tracker;
        this.cooldown = Duration.ofMillis(cooldownMs);
    }

    @Override
    public void check(ClaimContext ctx) {
        if (tracker.isCoolingDown(ctx.user().getId(), ctx.now(), cooldown)) {
            throw new ClaimRejectedException(
                    "COOLDOWN",
                    "Slow down! Wait " + cooldown.toMillis() + "ms between captures.");
        }
    }
}
