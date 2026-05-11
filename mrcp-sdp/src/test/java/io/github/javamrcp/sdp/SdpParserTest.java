package io.github.javamrcp.sdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SdpParserTest {
    private final SdpParser parser = new SdpParser();

    @Test
    void parsesMrcpControlAndAudioMedia() {
        SdpSession session = parser.parse("""
                v=0\r
                o=FreeSWITCH 1234 5678 IN IP4 127.0.0.1\r
                s=-\r
                c=IN IP4 127.0.0.1\r
                t=0 0\r
                m=application 9 TCP/MRCPv2 1\r
                a=setup:active\r
                a=connection:new\r
                a=resource:speechrecog\r
                a=channel:32AECB23433801@speechrecog\r
                m=audio 4000 RTP/AVP 0 8 96\r
                a=rtpmap:0 PCMU/8000\r
                a=rtpmap:8 PCMA/8000\r
                a=rtpmap:96 L16/16000/1\r
                a=sendonly\r
                """);

        assertEquals(0, session.version());
        assertEquals("FreeSWITCH", session.origin().username());
        assertEquals("127.0.0.1", session.connection().orElseThrow().address());
        assertEquals(2, session.mediaDescriptions().size());

        SdpMediaDescription control = session.mrcpControlMedia().getFirst();
        assertTrue(control.isMrcpControlMedia());
        assertEquals("speechrecog", control.resources().getFirst());
        assertEquals("32AECB23433801@speechrecog", control.channel().orElseThrow());
        assertEquals("active", control.firstAttributeValue("setup").orElseThrow());

        SdpMediaDescription audio = session.audioMedia().getFirst();
        assertTrue(audio.isRtpAudioMedia());
        assertEquals(4000, audio.port());
        assertEquals("RTP/AVP", audio.protocol());
        assertEquals(3, audio.formats().size());
        assertEquals(3, audio.rtpMaps().size());
    }

    @Test
    void parsesMediaLevelConnectionAndPortCount() {
        SdpSession session = parser.parse("""
                v=0
                o=- 1 1 IN IP4 192.0.2.10
                s=MRCP
                t=0 0
                m=audio 5000/2 RTP/AVP 0
                c=IN IP4 192.0.2.20
                a=recvonly
                """);

        SdpMediaDescription audio = session.audioMedia().getFirst();
        assertEquals(5000, audio.port());
        assertEquals(2, audio.portCount());
        assertEquals("192.0.2.20", audio.connection().orElseThrow().address());
        assertTrue(audio.attributes().stream().anyMatch(attribute -> attribute.hasName("recvonly")));
    }

    @Test
    void rejectsMissingRequiredLines() {
        assertThrows(SdpParseException.class, () -> parser.parse("o=- 1 1 IN IP4 127.0.0.1\r\n"));
        assertThrows(SdpParseException.class, () -> parser.parse("v=0\r\ns=-\r\n"));
    }
}
