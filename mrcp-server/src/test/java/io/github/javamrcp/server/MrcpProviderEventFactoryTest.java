package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpEventLine;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.spi.RecognitionResult;
import org.junit.jupiter.api.Test;

class MrcpProviderEventFactoryTest {
    private final MrcpProviderEventFactory factory = new MrcpProviderEventFactory();

    @Test
    void mapsRecognitionResultToRecognitionCompleteEvent() {
        MrcpMessage request = MrcpMessage.request("RECOGNIZE", 10)
                .channelIdentifier(new MrcpChannelIdentifier("s1", "speechrecog"))
                .build();

        MrcpMessage event = factory.recognitionComplete(request, new RecognitionResult("a < b", true, 0.8));

        MrcpEventLine eventLine = (MrcpEventLine) event.startLine();
        assertEquals("RECOGNITION-COMPLETE", eventLine.eventName());
        assertEquals(10, eventLine.requestId());
        assertEquals(MrcpRequestState.COMPLETE, eventLine.requestState());
        assertEquals("000 success", event.firstHeaderValue("Completion-Cause").orElseThrow());
        assertTrue(event.bodyAsString().contains("a &lt; b"));
    }

    @Test
    void mapsSynthesisCompletionToSpeakCompleteEvent() {
        MrcpMessage request = MrcpMessage.request("SPEAK", 11)
                .channelIdentifier(new MrcpChannelIdentifier("s1", "speechsynth"))
                .build();

        MrcpMessage event = factory.speakComplete(request);

        MrcpEventLine eventLine = (MrcpEventLine) event.startLine();
        assertEquals("SPEAK-COMPLETE", eventLine.eventName());
        assertEquals("000 normal", event.firstHeaderValue("Completion-Cause").orElseThrow());
    }
}
