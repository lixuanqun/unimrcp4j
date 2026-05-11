package io.github.javamrcp.sip;

import java.util.Objects;

/**
 * Parsed SIP CSeq header value.
 */
public record SipCSeq(long sequence, SipMethod method) {
    public SipCSeq {
        Objects.requireNonNull(method, "method");
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must not be negative");
        }
    }

    public static SipCSeq parse(String value) {
        Objects.requireNonNull(value, "value");
        String[] parts = value.trim().split("\\s+");
        if (parts.length != 2) {
            throw new SipParseException("Invalid CSeq header: " + value);
        }
        try {
            long sequence = Long.parseLong(parts[0]);
            SipMethod method = SipMethod.fromWireValue(parts[1])
                    .orElseThrow(() -> new SipParseException("Unsupported CSeq method: " + parts[1]));
            return new SipCSeq(sequence, method);
        } catch (NumberFormatException ex) {
            throw new SipParseException("Invalid CSeq sequence: " + parts[0], ex);
        }
    }

    public String wireValue() {
        return sequence + " " + method.wireValue();
    }
}
