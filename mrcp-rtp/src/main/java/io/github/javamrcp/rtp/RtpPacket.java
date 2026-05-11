package io.github.javamrcp.rtp;

import java.util.Arrays;
import java.util.Objects;

/**
 * RTP v2 packet with the fixed header fields used by audio streams.
 */
public final class RtpPacket {
    public static final int FIXED_HEADER_LENGTH = 12;

    private final boolean marker;
    private final int payloadType;
    private final int sequenceNumber;
    private final long timestamp;
    private final long ssrc;
    private final byte[] payload;

    public RtpPacket(boolean marker, int payloadType, int sequenceNumber, long timestamp, long ssrc, byte[] payload) {
        if (payloadType < 0 || payloadType > 127) {
            throw new IllegalArgumentException("payloadType must be between 0 and 127");
        }
        if (sequenceNumber < 0 || sequenceNumber > 0xFFFF) {
            throw new IllegalArgumentException("sequenceNumber must be between 0 and 65535");
        }
        if (timestamp < 0 || timestamp > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("timestamp must be an unsigned 32-bit value");
        }
        if (ssrc < 0 || ssrc > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("ssrc must be an unsigned 32-bit value");
        }
        this.marker = marker;
        this.payloadType = payloadType;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
        this.ssrc = ssrc;
        this.payload = Objects.requireNonNull(payload, "payload").clone();
    }

    public boolean marker() {
        return marker;
    }

    public int payloadType() {
        return payloadType;
    }

    public int sequenceNumber() {
        return sequenceNumber;
    }

    public long timestamp() {
        return timestamp;
    }

    public long ssrc() {
        return ssrc;
    }

    public byte[] payload() {
        return payload.clone();
    }

    public byte[] encode() {
        byte[] encoded = new byte[FIXED_HEADER_LENGTH + payload.length];
        encoded[0] = (byte) 0x80;
        encoded[1] = (byte) ((marker ? 0x80 : 0x00) | payloadType);
        writeUnsigned16(encoded, 2, sequenceNumber);
        writeUnsigned32(encoded, 4, timestamp);
        writeUnsigned32(encoded, 8, ssrc);
        System.arraycopy(payload, 0, encoded, FIXED_HEADER_LENGTH, payload.length);
        return encoded;
    }

    public static RtpPacket parse(byte[] packet) {
        Objects.requireNonNull(packet, "packet");
        if (packet.length < FIXED_HEADER_LENGTH) {
            throw new RtpParseException("RTP packet is shorter than fixed header");
        }
        int version = (packet[0] >> 6) & 0x03;
        if (version != 2) {
            throw new RtpParseException("Unsupported RTP version: " + version);
        }
        boolean padding = ((packet[0] >> 5) & 0x01) == 1;
        boolean extension = ((packet[0] >> 4) & 0x01) == 1;
        int csrcCount = packet[0] & 0x0F;
        if (extension) {
            throw new RtpParseException("RTP header extensions are not supported yet");
        }
        int headerLength = FIXED_HEADER_LENGTH + csrcCount * 4;
        if (packet.length < headerLength) {
            throw new RtpParseException("RTP packet is shorter than CSRC header length");
        }
        int payloadEnd = packet.length;
        if (padding) {
            int paddingLength = packet[packet.length - 1] & 0xFF;
            if (paddingLength == 0 || paddingLength > packet.length - headerLength) {
                throw new RtpParseException("Invalid RTP padding length");
            }
            payloadEnd -= paddingLength;
        }
        return new RtpPacket(
                (packet[1] & 0x80) != 0,
                packet[1] & 0x7F,
                readUnsigned16(packet, 2),
                readUnsigned32(packet, 4),
                readUnsigned32(packet, 8),
                Arrays.copyOfRange(packet, headerLength, payloadEnd));
    }

    private static void writeUnsigned16(byte[] target, int offset, int value) {
        target[offset] = (byte) ((value >>> 8) & 0xFF);
        target[offset + 1] = (byte) (value & 0xFF);
    }

    private static void writeUnsigned32(byte[] target, int offset, long value) {
        target[offset] = (byte) ((value >>> 24) & 0xFF);
        target[offset + 1] = (byte) ((value >>> 16) & 0xFF);
        target[offset + 2] = (byte) ((value >>> 8) & 0xFF);
        target[offset + 3] = (byte) (value & 0xFF);
    }

    private static int readUnsigned16(byte[] source, int offset) {
        return ((source[offset] & 0xFF) << 8) | (source[offset + 1] & 0xFF);
    }

    private static long readUnsigned32(byte[] source, int offset) {
        return ((long) (source[offset] & 0xFF) << 24)
                | ((long) (source[offset + 1] & 0xFF) << 16)
                | ((long) (source[offset + 2] & 0xFF) << 8)
                | (source[offset + 3] & 0xFFL);
    }
}
