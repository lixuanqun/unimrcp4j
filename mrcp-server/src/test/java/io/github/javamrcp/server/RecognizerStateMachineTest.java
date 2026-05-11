package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpResponseLine;
import org.junit.jupiter.api.Test;

class RecognizerStateMachineTest {
    @Test
    void recognizeMovesToRecognizingAndReturnsInProgress() {
        RecognizerStateMachine stateMachine = new RecognizerStateMachine();

        MrcpMessage response = stateMachine.onRequest(request("RECOGNIZE", 1)).getFirst();

        MrcpResponseLine responseLine = (MrcpResponseLine) response.startLine();
        assertEquals(200, responseLine.statusCode());
        assertEquals(MrcpRequestState.IN_PROGRESS, responseLine.requestState());
        assertEquals(MrcpResourceChannelState.RECOGNIZING, stateMachine.state());
        assertEquals("s1@speechrecog", response.firstHeaderValue(MrcpMessage.CHANNEL_IDENTIFIER).orElseThrow());
    }

    @Test
    void stopCompletesRecognition() {
        RecognizerStateMachine stateMachine = new RecognizerStateMachine();
        stateMachine.onRequest(request("RECOGNIZE", 1));

        MrcpMessage response = stateMachine.onRequest(request("STOP", 2)).getFirst();

        MrcpResponseLine responseLine = (MrcpResponseLine) response.startLine();
        assertEquals(200, responseLine.statusCode());
        assertEquals(MrcpRequestState.COMPLETE, responseLine.requestState());
        assertEquals(MrcpResourceChannelState.COMPLETED, stateMachine.state());
    }

    @Test
    void secondRecognizeIsRejectedWhileRecognizing() {
        RecognizerStateMachine stateMachine = new RecognizerStateMachine();
        stateMachine.onRequest(request("RECOGNIZE", 1));

        MrcpMessage response = stateMachine.onRequest(request("RECOGNIZE", 2)).getFirst();

        assertEquals(402, ((MrcpResponseLine) response.startLine()).statusCode());
    }

    private MrcpMessage request(String method, long requestId) {
        return MrcpMessage.request(method, requestId)
                .channelIdentifier(new MrcpChannelIdentifier("s1", "speechrecog"))
                .build();
    }
}
