package io.github.javamrcp.core;

import java.util.Objects;
import java.util.Optional;

/**
 * MRCP Channel-Identifier header value, formatted as {@code session@resource}.
 */
public record MrcpChannelIdentifier(String sessionId, String resourceName) {
    public MrcpChannelIdentifier {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(resourceName, "resourceName");
        if (sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        if (resourceName.isBlank()) {
            throw new IllegalArgumentException("resourceName must not be blank");
        }
    }

    public static MrcpChannelIdentifier parse(String value) {
        Objects.requireNonNull(value, "value");
        int separator = value.indexOf('@');
        if (separator <= 0 || separator == value.length() - 1) {
            throw new IllegalArgumentException("Channel-Identifier must be formatted as session@resource");
        }
        return new MrcpChannelIdentifier(value.substring(0, separator), value.substring(separator + 1));
    }

    public Optional<MrcpResourceType> resourceType() {
        return MrcpResourceType.fromWireValue(resourceName);
    }

    public String wireValue() {
        return sessionId + "@" + resourceName;
    }
}
