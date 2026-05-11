package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpResponseLine;
import org.junit.jupiter.api.Test;

class SynthesizerStateMachineTest {
    @Test
    void speakMovesToSpeakingAndReturnsInProgress() {
        SynthesizerStateMachine stateMachine = new SynthesizerStateMachine();

        MrcpMessage response = stateMachine.onRequest(request("SPEAK", 1)).getFirst();

        MrcpResponseLine responseLine = (MrcpResponseLine) response.startLine();
        assertEquals(200, responseLine.statusCode());
        assertEquals(MrcpRequestState.IN_PROGRESS, responseLine.requestState());
        assertEquals(MrcpResourceChannelState.SPEAKING, stateMachine.state());
    }

    @Test
    void pauseAndResumeUpdateState() {
        SynthesizerStateMachine stateMachine = new SynthesizerStateMachine();
        stateMachine.onRequest(request("SPEAK", 1));

        MrcpMessage pause = stateMachine.onRequest(request("PAUSE", 2)).getFirst();
        MrcpMessage resume = stateMachine.onRequest(request("RESUME", 3)).getFirst();

        assertEquals(200, ((MrcpResponseLine) pause.startLine()).statusCode());
        assertEquals(200, ((MrcpResponseLine) resume.startLine()).statusCode());
        assertEquals(MrcpResourceChannelState.SPEAKING, stateMachine.state());
    }

    @Test
    void stopCompletesSpeaking() {
        SynthesizerStateMachine stateMachine = new SynthesizerStateMachine();
        stateMachine.onRequest(request("SPEAK", 1));

        MrcpMessage response = stateMachine.onRequest(request("STOP", 2)).getFirst();

        MrcpResponseLine responseLine = (MrcpResponseLine) response.startLine();
        assertEquals(200, responseLine.statusCode());
        assertEquals(MrcpRequestState.COMPLETE, responseLine.requestState());
        assertEquals(MrcpResourceChannelState.COMPLETED, stateMachine.state());
    }

    private MrcpMessage request(String method, long requestId) {
        return MrcpMessage.request(method, requestId)
                .channelIdentifier(new MrcpChannelIdentifier("s1", "speechsynth"))
                .build();
    }
}
