package io.github.javamrcp.sdp;

import java.util.Objects;

/**
 * SDP origin line.
 */
public record SdpOrigin(
        String username,
        long sessionId,
        long sessionVersion,
        String networkType,
        String addressType,
        String unicastAddress) {
    public SdpOrigin {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(networkType, "networkType");
        Objects.requireNonNull(addressType, "addressType");
        Objects.requireNonNull(unicastAddress, "unicastAddress");
        if (username.isBlank() || networkType.isBlank() || addressType.isBlank() || unicastAddress.isBlank()) {
            throw new IllegalArgumentException("origin fields must not be blank");
        }
    }

    public static SdpOrigin ip4(String username, long sessionId, long sessionVersion, String address) {
        return new SdpOrigin(username, sessionId, sessionVersion, "IN", "IP4", address);
    }

    public String wireValue() {
        return username
                + " "
                + sessionId
                + " "
                + sessionVersion
                + " "
                + networkType
                + " "
                + addressType
                + " "
                + unicastAddress;
    }
}
