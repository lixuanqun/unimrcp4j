package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.sdp.SdpParser;
import io.github.javamrcp.sdp.SdpSession;
import org.junit.jupiter.api.Test;

class MrcpSessionFactoryTest {
    private final MrcpSessionFactory factory = new MrcpSessionFactory();

    @Test
    void createsSessionFromMrcpOffer() {
        SdpSession offer = new SdpParser().parse("""
                v=0
                o=FreeSWITCH 1 1 IN IP4 127.0.0.1
                s=-
                c=IN IP4 127.0.0.1
                t=0 0
                m=application 9 TCP/MRCPv2 1
                a=resource:speechrecog
                a=channel:32AECB23433801@speechrecog
                m=audio 4000 RTP/AVP 0
                a=rtpmap:0 PCMU/8000
                a=sendonly
                """);

        MrcpServerSession session = factory.createFromOffer("call-1", offer);

        assertEquals("32AECB23433801", session.id().value());
        assertEquals("call-1", session.callId());
        assertEquals(MrcpSessionState.OFFER_RECEIVED, session.state());
        assertEquals(1, session.resourceChannels().size());
        assertTrue(session.firstChannel(MrcpResourceType.SPEECH_RECOGNIZER).isPresent());
        assertEquals(4000, session.resourceChannels().getFirst().audioMediaDescription().orElseThrow().port());
    }

    @Test
    void rejectsOffersWithoutMrcpControlMedia() {
        SdpSession offer = new SdpParser().parse("""
                v=0
                o=- 1 1 IN IP4 127.0.0.1
                s=-
                t=0 0
                m=audio 4000 RTP/AVP 0
                """);

        assertThrows(IllegalArgumentException.class, () -> factory.createFromOffer("call-1", offer));
    }
}
