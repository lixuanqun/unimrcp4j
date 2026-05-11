package io.github.javamrcp.core;

import java.util.Locale;
import java.util.Optional;

/**
 * MRCPv2 resources targeted by the first server milestone.
 */
public enum MrcpResourceType {
    SPEECH_RECOGNIZER("speechrecog"),
    SPEECH_SYNTHESIZER("speechsynth");

    private final String wireValue;

    MrcpResourceType(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static Optional<MrcpResourceType> fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.toLowerCase(Locale.ROOT);
        for (MrcpResourceType resourceType : values()) {
            if (resourceType.wireValue.equals(normalized)) {
                return Optional.of(resourceType);
            }
        }
        return Optional.empty();
    }
}
