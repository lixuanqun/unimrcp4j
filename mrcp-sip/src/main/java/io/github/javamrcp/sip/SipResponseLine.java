package io.github.javamrcp.sip;

import java.util.Objects;

/**
 * SIP response status line: {@code SIP/2.0 status-code reason-phrase}.
 */
public record SipResponseLine(SipVersion version, int statusCode, String reasonPhrase) implements SipStartLine {
    public SipResponseLine {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(reasonPhrase, "reasonPhrase");
        if (statusCode < 100 || statusCode > 699) {
            throw new IllegalArgumentException("SIP status code must be between 100 and 699");
        }
        if (reasonPhrase.isBlank()) {
            throw new IllegalArgumentException("reasonPhrase must not be blank");
        }
    }

    @Override
    public SipMessageType messageType() {
        return SipMessageType.RESPONSE;
    }
}
