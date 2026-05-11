package io.github.javamrcp.sip;

/**
 * Common contract for SIP/2.0 request and response start lines.
 */
public sealed interface SipStartLine permits SipRequestLine, SipResponseLine {
    SipVersion version();

    SipMessageType messageType();
}
