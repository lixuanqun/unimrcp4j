package io.github.javamrcp.sip;

import java.net.URI;
import java.util.Objects;

/**
 * SIP request line: {@code METHOD request-uri SIP/2.0}.
 */
public record SipRequestLine(SipMethod method, URI requestUri, SipVersion version) implements SipStartLine {
    public SipRequestLine {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(requestUri, "requestUri");
        Objects.requireNonNull(version, "version");
    }

    @Override
    public SipMessageType messageType() {
        return SipMessageType.REQUEST;
    }
}
