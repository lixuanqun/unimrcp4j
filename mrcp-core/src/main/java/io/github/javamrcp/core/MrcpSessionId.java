package io.github.javamrcp.core;

import java.util.Objects;
import java.util.UUID;

/**
 * Stable identifier for correlating SIP dialogs, MRCP channels, and RTP media streams.
 */
public record MrcpSessionId(String value) {
    public MrcpSessionId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("MRCP session id must not be blank");
        }
    }

    public static MrcpSessionId random() {
        return new MrcpSessionId(UUID.randomUUID().toString());
    }
}
