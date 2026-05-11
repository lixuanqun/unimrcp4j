package io.github.javamrcp.sip;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * A SIP UDP datagram with transport addresses preserved for response routing.
 */
public record SipDatagramMessage(
        SipMessage message,
        InetSocketAddress sender,
        InetSocketAddress recipient) {
    public SipDatagramMessage {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(sender, "sender");
    }
}
