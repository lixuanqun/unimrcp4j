package io.github.javamrcp.server;

/**
 * Raised when creating a session would exceed the configured session limit.
 */
public class MrcpSessionLimitExceededException extends RuntimeException {
    public MrcpSessionLimitExceededException(String message) {
        super(message);
    }
}
