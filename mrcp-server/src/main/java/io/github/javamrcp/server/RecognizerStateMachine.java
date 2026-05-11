package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRecognizerMethod;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpStatusCode;
import java.util.List;

/**
 * Initial recognizer resource state machine. Provider callbacks will add completion events later.
 */
public final class RecognizerStateMachine extends AbstractMrcpResourceStateMachine {
    @Override
    public List<MrcpMessage> onRequest(MrcpMessage request) {
        MrcpRequestLine requestLine = requestLine(request);
        return MrcpRecognizerMethod.fromWireValue(requestLine.methodName())
                .map(method -> handle(request, method))
                .orElseGet(() -> response(
                        request,
                        MrcpStatusCode.METHOD_NOT_ALLOWED,
                        MrcpRequestState.COMPLETE));
    }

    private List<MrcpMessage> handle(MrcpMessage request, MrcpRecognizerMethod method) {
        return switch (method) {
            case RECOGNIZE -> {
                if (state() != MrcpResourceChannelState.IDLE && state() != MrcpResourceChannelState.COMPLETED) {
                    yield response(request, MrcpStatusCode.METHOD_NOT_VALID_IN_THIS_STATE, MrcpRequestState.COMPLETE);
                }
                state(MrcpResourceChannelState.RECOGNIZING);
                yield response(request, MrcpStatusCode.SUCCESS, MrcpRequestState.IN_PROGRESS);
            }
            case START_INPUT_TIMERS, DEFINE_GRAMMAR, SET_PARAMS, GET_PARAMS ->
                    response(request, MrcpStatusCode.SUCCESS, MrcpRequestState.COMPLETE);
            case STOP -> {
                state(MrcpResourceChannelState.COMPLETED);
                yield response(request, MrcpStatusCode.SUCCESS, MrcpRequestState.COMPLETE);
            }
        };
    }
}
