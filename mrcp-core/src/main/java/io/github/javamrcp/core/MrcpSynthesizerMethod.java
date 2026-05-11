package io.github.javamrcp.core;

import java.util.Locale;
import java.util.Optional;

/**
 * MRCP synthesizer methods in the subset needed for the first server milestones.
 */
public enum MrcpSynthesizerMethod {
    SET_PARAMS("SET-PARAMS"),
    GET_PARAMS("GET-PARAMS"),
    SPEAK("SPEAK"),
    STOP("STOP"),
    PAUSE("PAUSE"),
    RESUME("RESUME"),
    BARGE_IN_OCCURRED("BARGE-IN-OCCURRED");

    private final String wireValue;

    MrcpSynthesizerMethod(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static Optional<MrcpSynthesizerMethod> fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        for (MrcpSynthesizerMethod method : values()) {
            if (method.wireValue.equals(normalized)) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }
}
