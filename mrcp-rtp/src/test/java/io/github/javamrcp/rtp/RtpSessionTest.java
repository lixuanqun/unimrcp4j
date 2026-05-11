package io.github.javamrcp.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RtpSessionTest {
    @Test
    void advancesSequenceAndTimestamp() {
        RtpSession session = new RtpSession(RtpAudioCodec.PCMU.payloadType(), 100, 65535, 10);

        RtpPacket first = session.nextPacket(new byte[] {1}, 160, true);
        RtpPacket second = session.nextPacket(new byte[] {2}, 160, false);

        assertEquals(65535, first.sequenceNumber());
        assertEquals(10, first.timestamp());
        assertEquals(0, second.sequenceNumber());
        assertEquals(170, second.timestamp());
        assertEquals(1, session.nextSequenceNumber());
        assertEquals(330, session.nextTimestamp());
    }

    @Test
    void exposesCodecRtpMapValues() {
        assertEquals("0 PCMU/8000", RtpAudioCodec.PCMU.rtpMapValue());
        assertEquals("8 PCMA/8000", RtpAudioCodec.PCMA.rtpMapValue());
        assertEquals("96 L16/16000", RtpAudioCodec.L16_DYNAMIC.rtpMapValue());
    }
}
