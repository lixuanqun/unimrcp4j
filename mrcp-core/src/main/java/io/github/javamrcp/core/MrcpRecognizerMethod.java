package io.github.javamrcp.core;

import java.util.Locale;
import java.util.Optional;

/**
 * MRCP recognizer methods in the subset needed for the first server milestones.
 */
public enum MrcpRecognizerMethod {
    SET_PARAMS("SET-PARAMS"),
    GET_PARAMS("GET-PARAMS"),
    DEFINE_GRAMMAR("DEFINE-GRAMMAR"),
    RECOGNIZE("RECOGNIZE"),
    START_INPUT_TIMERS("START-INPUT-TIMERS"),
    STOP("STOP");

    private final String wireValue;

    MrcpRecognizerMethod(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static Optional<MrcpRecognizerMethod> fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        for (MrcpRecognizerMethod method : values()) {
            if (method.wireValue.equals(normalized)) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }
}
