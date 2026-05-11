package io.github.javamrcp.sdp;

import java.util.Objects;

/**
 * SDP connection data, usually encoded as {@code c=IN IP4 127.0.0.1}.
 */
public record SdpConnection(String networkType, String addressType, String address) {
    public SdpConnection {
        Objects.requireNonNull(networkType, "networkType");
        Objects.requireNonNull(addressType, "addressType");
        Objects.requireNonNull(address, "address");
        if (networkType.isBlank() || addressType.isBlank() || address.isBlank()) {
            throw new IllegalArgumentException("connection fields must not be blank");
        }
    }

    public static SdpConnection ip4(String address) {
        return new SdpConnection("IN", "IP4", address);
    }

    public String wireValue() {
        return networkType + " " + addressType + " " + address;
    }
}
