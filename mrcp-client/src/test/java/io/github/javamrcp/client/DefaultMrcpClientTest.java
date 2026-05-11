package io.github.javamrcp.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.sdp.SdpParser;
import io.github.javamrcp.sdp.SdpSession;
import io.github.javamrcp.sip.SipMessage;
import io.github.javamrcp.sip.SipMethod;
import io.github.javamrcp.sip.SipRequestLine;
import org.junit.jupiter.api.Test;

class DefaultMrcpClientTest {
    @Test
    void createsRecognizerInviteWithSdpOffer() {
        DefaultMrcpClient client = new DefaultMrcpClient(MrcpClientConfig.builder()
                .localHost("192.0.2.10")
                .localSipPort(5090)
                .localRtpPort(4100)
                .serverHost("192.0.2.20")
                .serverSipPort(8060)
                .build());

        MrcpClient.ClientInvite invite = client.createInvite(MrcpResourceType.SPEECH_RECOGNIZER);
        SipMessage sipInvite = invite.sipInvite();

        SipRequestLine requestLine = assertInstanceOf(SipRequestLine.class, sipInvite.startLine());
        assertEquals(SipMethod.INVITE, requestLine.method());
        assertEquals("application/sdp", sipInvite.firstHeaderValue(SipMessage.CONTENT_TYPE).orElseThrow());
        assertEquals(MrcpClientState.INVITE_CREATED, invite.session().state());
        assertEquals(MrcpResourceType.SPEECH_RECOGNIZER, invite.session().resourceType());

        SdpSession offer = new SdpParser().parse(sipInvite.bodyAsString());
        assertEquals("192.0.2.10", offer.connection().orElseThrow().address());
        assertEquals("speechrecog", offer.mrcpControlMedia().getFirst().resources().getFirst());
        assertEquals(invite.session().channelIdentifier().wireValue(),
                offer.mrcpControlMedia().getFirst().channel().orElseThrow());
        assertEquals(4100, offer.audioMedia().getFirst().port());
        assertTrue(offer.audioMedia().getFirst().rtpMaps().contains("0 PCMU/8000"));
    }

    @Test
    void tracksAnswerAndEstablishedState() {
        MrcpClient.ClientInvite invite = new DefaultMrcpClient(MrcpClientConfig.builder().build())
                .createInvite(MrcpResourceType.SPEECH_SYNTHESIZER);
        SdpSession answer = new SdpParser().parse(invite.sipInvite().bodyAsString());

        invite.session().answerReceived(answer);
        assertEquals(MrcpClientState.ANSWER_RECEIVED, invite.session().state());
        invite.session().established();
        assertEquals(MrcpClientState.ESTABLISHED, invite.session().state());
        invite.session().terminated();
        assertEquals(MrcpClientState.TERMINATED, invite.session().state());
    }
}
