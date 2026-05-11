package io.github.javamrcp.core;

import java.util.Locale;
import java.util.Optional;

/**
 * MRCP request state values used by response and event start lines.
 */
public enum MrcpRequestState {
    PENDING("PENDING"),
    IN_PROGRESS("IN-PROGRESS"),
    COMPLETE("COMPLETE");

    private final String wireValue;

    MrcpRequestState(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static Optional<MrcpRequestState> fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.toUpperCase(Locale.ROOT);
        for (MrcpRequestState state : values()) {
            if (state.wireValue.equals(normalized)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
