package io.github.javamrcp.rtp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RtpPacketTest {
    @Test
    void encodesAndParsesPacket() {
        byte[] payload = new byte[] {1, 2, 3, 4};
        RtpPacket packet = new RtpPacket(true, 0, 65535, 0xFFFF_FFFEL, 0x1234_5678L, payload);

        RtpPacket parsed = RtpPacket.parse(packet.encode());

        assertTrue(parsed.marker());
        assertEquals(0, parsed.payloadType());
        assertEquals(65535, parsed.sequenceNumber());
        assertEquals(0xFFFF_FFFEL, parsed.timestamp());
        assertEquals(0x1234_5678L, parsed.ssrc());
        assertArrayEquals(payload, parsed.payload());
    }

    @Test
    void rejectsInvalidPackets() {
        assertThrows(RtpParseException.class, () -> RtpPacket.parse(new byte[1]));
        byte[] invalidVersion = new byte[RtpPacket.FIXED_HEADER_LENGTH];
        invalidVersion[0] = 0;
        assertThrows(RtpParseException.class, () -> RtpPacket.parse(invalidVersion));
    }
}
