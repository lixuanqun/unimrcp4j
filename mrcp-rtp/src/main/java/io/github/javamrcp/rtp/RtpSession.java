package io.github.javamrcp.rtp;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * Minimal RTP sender state for monotonically assigning sequence numbers and timestamps.
 */
public final class RtpSession {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int payloadType;
    private final long ssrc;
    private int nextSequenceNumber;
    private long nextTimestamp;

    public RtpSession(int payloadType, long ssrc, int initialSequenceNumber, long initialTimestamp) {
        if (payloadType < 0 || payloadType > 127) {
            throw new IllegalArgumentException("payloadType must be between 0 and 127");
        }
        this.payloadType = payloadType;
        this.ssrc = ssrc;
        this.nextSequenceNumber = initialSequenceNumber & 0xFFFF;
        this.nextTimestamp = initialTimestamp & 0xFFFF_FFFFL;
    }

    public static RtpSession forCodec(RtpAudioCodec codec) {
        Objects.requireNonNull(codec, "codec");
        return new RtpSession(
                codec.payloadType(),
                RANDOM.nextInt() & 0xFFFF_FFFFL,
                RANDOM.nextInt(0x1_0000),
                RANDOM.nextInt() & 0xFFFF_FFFFL);
    }

    public synchronized RtpPacket nextPacket(byte[] payload, int timestampIncrement, boolean marker) {
        RtpPacket packet = new RtpPacket(marker, payloadType, nextSequenceNumber, nextTimestamp, ssrc, payload);
        nextSequenceNumber = (nextSequenceNumber + 1) & 0xFFFF;
        nextTimestamp = (nextTimestamp + timestampIncrement) & 0xFFFF_FFFFL;
        return packet;
    }

    public int nextSequenceNumber() {
        return nextSequenceNumber;
    }

    public long nextTimestamp() {
        return nextTimestamp;
    }

    public long ssrc() {
        return ssrc;
    }
}
