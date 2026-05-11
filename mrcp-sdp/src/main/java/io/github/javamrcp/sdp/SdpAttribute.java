package io.github.javamrcp.sdp;

import java.util.Objects;

/**
 * SDP attribute line. Attributes may be name-only or name/value pairs.
 */
public record SdpAttribute(String name, String value) {
    public SdpAttribute {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("attribute name must not be blank");
        }
    }

    public static SdpAttribute of(String name) {
        return new SdpAttribute(name, null);
    }

    public static SdpAttribute of(String name, String value) {
        return new SdpAttribute(name, value);
    }

    public boolean hasName(String expectedName) {
        return name.equalsIgnoreCase(expectedName);
    }

    public String wireValue() {
        return value == null ? name : name + ":" + value;
    }
}
