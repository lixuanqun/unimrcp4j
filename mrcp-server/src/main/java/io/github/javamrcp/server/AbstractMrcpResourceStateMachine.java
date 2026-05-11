package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpHeader;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpStatusCode;
import java.util.List;
import java.util.Objects;

abstract class AbstractMrcpResourceStateMachine implements MrcpResourceStateMachine {
    private MrcpResourceChannelState state = MrcpResourceChannelState.IDLE;

    @Override
    public MrcpResourceChannelState state() {
        return state;
    }

    protected void state(MrcpResourceChannelState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    protected MrcpRequestLine requestLine(MrcpMessage request) {
        if (request.startLine() instanceof MrcpRequestLine requestLine) {
            return requestLine;
        }
        throw new IllegalArgumentException("MRCP request message is required");
    }

    protected List<MrcpMessage> response(
            MrcpMessage request,
            MrcpStatusCode statusCode,
            MrcpRequestState requestState) {
        MrcpRequestLine requestLine = requestLine(request);
        MrcpMessage.Builder builder = MrcpMessage.response(requestLine.requestId(), statusCode.code(), requestState);
        request.firstHeaderValue(MrcpMessage.CHANNEL_IDENTIFIER)
                .ifPresent(value -> builder.channelIdentifier(MrcpChannelIdentifier.parse(value)));
        return List.of(builder.build());
    }

    protected List<MrcpMessage> responseWithHeaders(
            MrcpMessage request,
            MrcpStatusCode statusCode,
            MrcpRequestState requestState,
            List<MrcpHeader> headers) {
        MrcpRequestLine requestLine = requestLine(request);
        MrcpMessage.Builder builder = MrcpMessage.response(requestLine.requestId(), statusCode.code(), requestState);
        request.firstHeaderValue(MrcpMessage.CHANNEL_IDENTIFIER)
                .ifPresent(value -> builder.channelIdentifier(MrcpChannelIdentifier.parse(value)));
        for (MrcpHeader header : headers) {
            builder.header(header.name(), header.value());
        }
        return List.of(builder.build());
    }
}
