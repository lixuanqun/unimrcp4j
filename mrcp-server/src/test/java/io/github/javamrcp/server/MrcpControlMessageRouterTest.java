package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpResponseLine;
import io.github.javamrcp.provider.mock.MockSpeechRecognizerProvider;
import io.github.javamrcp.provider.mock.MockSpeechSynthesizerProvider;
import io.github.javamrcp.sdp.SdpParser;
import org.junit.jupiter.api.Test;

class MrcpControlMessageRouterTest {
    @Test
    void routesRecognizeToNegotiatedRecognizerStateMachine() {
        MrcpSessionRegistry registry = new MrcpSessionRegistry(10);
        MrcpServerSession session = new MrcpSessionFactory().createFromOffer("call-1", new SdpParser().parse("""
                v=0
                o=FreeSWITCH 1 1 IN IP4 127.0.0.1
                s=-
                t=0 0
                m=application 9 TCP/MRCPv2 1
                a=resource:speechrecog
                a=channel:session1@speechrecog
                m=audio 4000 RTP/AVP 0
                """));
        registry.register(session);
        MrcpControlMessageRouter router = new MrcpControlMessageRouter(registry);

        MrcpMessage response = router.route(MrcpMessage.request("RECOGNIZE", 1)
                .channelIdentifier(new MrcpChannelIdentifier("session1", "speechrecog"))
                .build()).getFirst();

        MrcpResponseLine responseLine = (MrcpResponseLine) response.startLine();
        assertEquals(200, responseLine.statusCode());
        assertEquals(MrcpRequestState.IN_PROGRESS, responseLine.requestState());
    }

    @Test
    void rejectsUnknownSession() {
        MrcpControlMessageRouter router = new MrcpControlMessageRouter(new MrcpSessionRegistry(10));

        MrcpMessage response = router.route(MrcpMessage.request("RECOGNIZE", 1)
                .channelIdentifier(new MrcpChannelIdentifier("missing", "speechrecog"))
                .build()).getFirst();

        assertEquals(407, ((MrcpResponseLine) response.startLine()).statusCode());
    }

    @Test
    void appendsProviderCompletionEventsWhenConfigured() {
        MrcpSessionRegistry registry = new MrcpSessionRegistry(10);
        MrcpServerSession session = new MrcpSessionFactory().createFromOffer("call-1", new SdpParser().parse("""
                v=0
                o=FreeSWITCH 1 1 IN IP4 127.0.0.1
                s=-
                t=0 0
                m=application 9 TCP/MRCPv2 1
                a=resource:speechrecog
                a=channel:session1@speechrecog
                m=audio 4000 RTP/AVP 0
                """));
        registry.register(session);
        MrcpControlMessageRouter router = new MrcpControlMessageRouter(
                registry,
                new MrcpProviderOrchestrator(
                        new MockSpeechRecognizerProvider("ok", 1.0),
                        new MockSpeechSynthesizerProvider()));

        var messages = router.route(MrcpMessage.request("RECOGNIZE", 1)
                .channelIdentifier(new MrcpChannelIdentifier("session1", "speechrecog"))
                .build());

        assertEquals(2, messages.size());
        assertEquals("RECOGNITION-COMPLETE",
                ((io.github.javamrcp.core.MrcpEventLine) messages.get(1).startLine()).eventName());
    }
}
