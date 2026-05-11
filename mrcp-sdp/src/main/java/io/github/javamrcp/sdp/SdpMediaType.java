package io.github.javamrcp.sdp;

import java.util.Locale;
import java.util.Optional;

/**
 * Media types relevant to MRCPv2 sessions.
 */
public enum SdpMediaType {
    APPLICATION("application"),
    AUDIO("audio"),
    VIDEO("video"),
    UNKNOWN("unknown");

    private final String wireValue;

    SdpMediaType(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static SdpMediaType fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        for (SdpMediaType mediaType : values()) {
            if (mediaType != UNKNOWN && mediaType.wireValue.equals(normalized)) {
                return mediaType;
            }
        }
        return UNKNOWN;
    }

    public static Optional<SdpMediaType> knownFromWireValue(String value) {
        SdpMediaType mediaType = fromWireValue(value);
        return mediaType == UNKNOWN ? Optional.empty() : Optional.of(mediaType);
    }
}
