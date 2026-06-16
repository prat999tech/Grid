package com.example.grid.exception;

/**
 * Thrown when a capture is not allowed (cooldown, unknown tile, unknown user...).
 * Carries a short machine-readable {@code code} plus a human message.
 */
public class ClaimRejectedException extends RuntimeException {

    private final String code;

    public ClaimRejectedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
