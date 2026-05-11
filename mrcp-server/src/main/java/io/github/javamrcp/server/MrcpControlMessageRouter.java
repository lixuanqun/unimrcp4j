package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpMessageType;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.core.MrcpSessionId;
import io.github.javamrcp.core.MrcpStatusCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Routes MRCP control messages to negotiated server sessions and resource state machines.
 */
public final class MrcpControlMessageRouter {
    private final MrcpSessionRegistry sessionRegistry;
    private final MrcpProviderOrchestrator providerOrchestrator;
    private final ConcurrentMap<String, MrcpResourceStateMachine> stateMachines = new ConcurrentHashMap<>();

    public MrcpControlMessageRouter(MrcpSessionRegistry sessionRegistry) {
        this(sessionRegistry, null);
    }

    public MrcpControlMessageRouter(
            MrcpSessionRegistry sessionRegistry,
            MrcpProviderOrchestrator providerOrchestrator) {
        this.sessionRegistry = Objects.requireNonNull(sessionRegistry, "sessionRegistry");
        this.providerOrchestrator = providerOrchestrator;
    }

    public List<MrcpMessage> route(MrcpMessage message) {
        Objects.requireNonNull(message, "message");
        if (message.messageType() != MrcpMessageType.REQUEST) {
            return List.of();
        }
        if (!(message.startLine() instanceof MrcpRequestLine requestLine)) {
            return List.of();
        }

        MrcpChannelIdentifier channelIdentifier;
        try {
            channelIdentifier = message.channelIdentifier()
                    .orElseThrow(() -> new IllegalArgumentException("Missing Channel-Identifier"));
        } catch (IllegalArgumentException ex) {
            return List.of(errorResponse(requestLine, MrcpStatusCode.UNSUPPORTED_HEADER_VALUE));
        }

        MrcpServerSession session = sessionRegistry.find(new MrcpSessionId(channelIdentifier.sessionId()))
                .orElse(null);
        if (session == null) {
            return List.of(errorResponse(requestLine, MrcpStatusCode.UNSUPPORTED_HEADER_VALUE, channelIdentifier));
        }

        MrcpResourceChannel channel = session.resourceChannels().stream()
                .filter(candidate -> candidate.channelIdentifier().equals(channelIdentifier))
                .findFirst()
                .orElse(null);
        if (channel == null) {
            return List.of(errorResponse(requestLine, MrcpStatusCode.UNSUPPORTED_HEADER_VALUE, channelIdentifier));
        }

        MrcpResourceStateMachine stateMachine = stateMachines.computeIfAbsent(
                channelIdentifier.wireValue(),
                ignored -> createStateMachine(channel.resourceType()));
        List<MrcpMessage> routedMessages = new ArrayList<>(stateMachine.onRequest(message));
        if (providerOrchestrator != null) {
            routedMessages.addAll(providerOrchestrator.maybeComplete(message, channel));
        }
        return routedMessages;
    }

    private MrcpResourceStateMachine createStateMachine(MrcpResourceType resourceType) {
        return switch (resourceType) {
            case SPEECH_RECOGNIZER -> new RecognizerStateMachine();
            case SPEECH_SYNTHESIZER -> new SynthesizerStateMachine();
        };
    }

    private MrcpMessage errorResponse(MrcpRequestLine requestLine, MrcpStatusCode statusCode) {
        return MrcpMessage.response(requestLine.requestId(), statusCode.code(), MrcpRequestState.COMPLETE).build();
    }

    private MrcpMessage errorResponse(
            MrcpRequestLine requestLine,
            MrcpStatusCode statusCode,
            MrcpChannelIdentifier channelIdentifier) {
        return MrcpMessage.response(requestLine.requestId(), statusCode.code(), MrcpRequestState.COMPLETE)
                .channelIdentifier(channelIdentifier)
                .build();
    }
}
