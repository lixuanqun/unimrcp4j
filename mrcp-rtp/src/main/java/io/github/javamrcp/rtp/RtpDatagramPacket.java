package io.github.javamrcp.rtp;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * RTP packet with UDP transport addresses.
 */
public record RtpDatagramPacket(
        RtpPacket packet,
        InetSocketAddress sender,
        InetSocketAddress recipient) {
    public RtpDatagramPacket {
        Objects.requireNonNull(packet, "packet");
        Objects.requireNonNull(sender, "sender");
    }
}
