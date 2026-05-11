package io.github.javamrcp.core;

import java.util.Objects;

/**
 * A single MRCP header field, preserving insertion order in {@link MrcpMessage}.
 */
public record MrcpHeader(String name, String value) {
    public MrcpHeader {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");
        if (name.isBlank()) {
            throw new IllegalArgumentException("header name must not be blank");
        }
        if (name.indexOf(':') >= 0) {
            throw new IllegalArgumentException("header name must not contain ':'");
        }
    }

    public boolean hasName(String expectedName) {
        return name.equalsIgnoreCase(expectedName);
    }
}
