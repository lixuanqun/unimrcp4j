package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpStatusCode;
import io.github.javamrcp.core.MrcpSynthesizerMethod;
import java.util.List;

/**
 * Initial synthesizer resource state machine. Provider callbacks will add audio and completion events later.
 */
public final class SynthesizerStateMachine extends AbstractMrcpResourceStateMachine {
    @Override
    public List<MrcpMessage> onRequest(MrcpMessage request) {
        MrcpRequestLine requestLine = requestLine(request);
        return MrcpSynthesizerMethod.fromWireValue(requestLine.methodName())
                .map(method -> handle(request, method))
                .orElseGet(() -> response(
                        request,
                        MrcpStatusCode.METHOD_NOT_ALLOWED,
                        MrcpRequestState.COMPLETE));
    }

    private List<MrcpMessage> handle(MrcpMessage request, MrcpSynthesizerMethod method) {
        return switch (method) {
            case SPEAK -> {
                if (state() == MrcpResourceChannelState.SPEAKING) {
                    yield response(request, MrcpStatusCode.METHOD_NOT_VALID_IN_THIS_STATE, MrcpRequestState.COMPLETE);
                }
                state(MrcpResourceChannelState.SPEAKING);
                yield response(request, MrcpStatusCode.SUCCESS, MrcpRequestState.IN_PROGRESS);
            }
            case PAUSE -> {
                if (state() != MrcpResourceChannelState.SPEAKING) {
                    yield response(request, MrcpStatusCode.METHOD_NOT_VALID_IN_THIS_STATE, MrcpRequestState.COMPLETE);
                }
                state(MrcpResourceChannelState.PAUSED);
                yield response(request, MrcpStatusCode.SUCCESS, MrcpRequestState.COMPLETE);
            }
            case RESUME -> {
                if (state() != MrcpResourceChannelState.PAUSED) {
                    yield response(request, MrcpStatusCode.METHOD_NOT_VALID_IN_THIS_STATE, MrcpRequestState.COMPLETE);
                }
                state(MrcpResourceChannelState.SPEAKING);
                yield response(request, MrcpStatusCode.SUCCESS, MrcpRequestState.COMPLETE);
            }
            case STOP, BARGE_IN_OCCURRED -> {
                state(MrcpResourceChannelState.COMPLETED);
                yield response(request, MrcpStatusCode.SUCCESS, MrcpRequestState.COMPLETE);
            }
            case SET_PARAMS, GET_PARAMS -> response(request, MrcpStatusCode.SUCCESS, MrcpRequestState.COMPLETE);
        };
    }
}
