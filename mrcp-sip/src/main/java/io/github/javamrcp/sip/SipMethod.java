package io.github.javamrcp.sip;

import java.util.Locale;
import java.util.Optional;

/**
 * SIP methods required by the MRCPv2 server call setup path.
 */
public enum SipMethod {
    INVITE,
    ACK,
    BYE,
    CANCEL,
    OPTIONS;

    public String wireValue() {
        return name();
    }

    public static Optional<SipMethod> fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
