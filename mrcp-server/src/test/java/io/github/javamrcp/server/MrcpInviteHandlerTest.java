package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.sdp.SdpParser;
import io.github.javamrcp.sdp.SdpSession;
import io.github.javamrcp.sip.SipMessage;
import io.github.javamrcp.sip.SipMethod;
import io.github.javamrcp.sip.SipResponseLine;
import java.net.URI;
import org.junit.jupiter.api.Test;

class MrcpInviteHandlerTest {
    @Test
    void handlesInviteAndRegistersSessionWithSdpAnswer() {
        MrcpServerConfig config = MrcpServerConfig.builder()
                .advertisedHost("203.0.113.10")
                .mrcpPort(1544)
                .rtpPort(4000)
                .build();
        MrcpSessionRegistry registry = new MrcpSessionRegistry(10);
        MrcpInviteHandler handler = new MrcpInviteHandler(config, registry);
        SipMessage invite = invite("call-1", offerSdp());

        SipMessage response = handler.handleInvite(invite);

        SipResponseLine responseLine = assertInstanceOf(SipResponseLine.class, response.startLine());
        assertEquals(200, responseLine.statusCode());
        assertEquals("application/sdp", response.firstHeaderValue(SipMessage.CONTENT_TYPE).orElseThrow());
        assertTrue(registry.findByCallId("call-1").isPresent());
        MrcpServerSession session = registry.findByCallId("call-1").orElseThrow();
        assertEquals(MrcpSessionState.ANSWER_SENT, session.state());

        SdpSession answer = new SdpParser().parse(response.bodyAsString());
        assertEquals("203.0.113.10", answer.connection().orElseThrow().address());
        assertEquals(1544, answer.mrcpControlMedia().getFirst().port());
        assertEquals("passive", answer.mrcpControlMedia().getFirst().firstAttributeValue("setup").orElseThrow());
        assertEquals("speechrecog", answer.mrcpControlMedia().getFirst().resources().getFirst());
        assertEquals(4000, answer.audioMedia().getFirst().port());
        assertEquals("0 PCMU/8000", answer.audioMedia().getFirst().rtpMaps().getFirst());
    }

    @Test
    void rejectsInvalidOffer() {
        MrcpSessionRegistry registry = new MrcpSessionRegistry(10);
        MrcpInviteHandler handler = new MrcpInviteHandler(MrcpServerConfig.defaults(), registry);

        SipMessage response = handler.rejectInvite(invite("call-2", "not-sdp"), 488, "Not Acceptable Here");

        SipResponseLine responseLine = assertInstanceOf(SipResponseLine.class, response.startLine());
        assertEquals(488, responseLine.statusCode());
    }

    private SipMessage invite(String callId, String sdp) {
        return SipMessage.request(SipMethod.INVITE, URI.create("sip:mrcp@127.0.0.1"))
                .header(SipMessage.VIA, "SIP/2.0/UDP 127.0.0.1:5080;branch=z9hG4bK-invite")
                .header(SipMessage.FROM, "<sip:fs@127.0.0.1>;tag=from-tag")
                .header(SipMessage.TO, "<sip:mrcp@127.0.0.1>")
                .header(SipMessage.CALL_ID, callId)
                .header(SipMessage.CSEQ, "1 INVITE")
                .body("application/sdp", sdp)
                .build();
    }

    private String offerSdp() {
        return "v=0\r\n"
                + "o=FreeSWITCH 1 1 IN IP4 127.0.0.1\r\n"
                + "s=-\r\n"
                + "c=IN IP4 127.0.0.1\r\n"
                + "t=0 0\r\n"
                + "m=application 9 TCP/MRCPv2 1\r\n"
                + "a=resource:speechrecog\r\n"
                + "a=channel:32AECB23433801@speechrecog\r\n"
                + "m=audio 5000 RTP/AVP 0\r\n"
                + "a=rtpmap:0 PCMU/8000\r\n"
                + "a=sendonly\r\n";
    }
}
