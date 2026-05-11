package io.github.javamrcp.core;

import java.util.Objects;

/**
 * MRCP/2.0 event start line: {@code MRCP/2.0 length EVENT request-id request-state}.
 */
public record MrcpEventLine(
        MrcpVersion version,
        int messageLength,
        String eventName,
        long requestId,
        MrcpRequestState requestState) implements MrcpStartLine {
    public MrcpEventLine {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(eventName, "eventName");
        Objects.requireNonNull(requestState, "requestState");
        if (messageLength < 0) {
            throw new IllegalArgumentException("messageLength must not be negative");
        }
        if (eventName.isBlank()) {
            throw new IllegalArgumentException("eventName must not be blank");
        }
        if (requestId < 0) {
            throw new IllegalArgumentException("requestId must not be negative");
        }
    }

    @Override
    public MrcpMessageType messageType() {
        return MrcpMessageType.EVENT;
    }

    public MrcpEventLine withMessageLength(int newMessageLength) {
        return new MrcpEventLine(version, newMessageLength, eventName, requestId, requestState);
    }
}
