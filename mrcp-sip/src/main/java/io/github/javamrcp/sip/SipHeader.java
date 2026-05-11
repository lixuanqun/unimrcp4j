package io.github.javamrcp.sip;

import java.util.Objects;

/**
 * A single SIP header field, preserving wire order in {@link SipMessage}.
 */
public record SipHeader(String name, String value) {
    public SipHeader {
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
