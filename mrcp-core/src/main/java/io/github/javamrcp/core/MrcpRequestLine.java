package io.github.javamrcp.core;

import java.util.Objects;

/**
 * MRCP/2.0 request start line: {@code MRCP/2.0 length METHOD request-id}.
 */
public record MrcpRequestLine(
        MrcpVersion version,
        int messageLength,
        String methodName,
        long requestId) implements MrcpStartLine {
    public MrcpRequestLine {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(methodName, "methodName");
        if (messageLength < 0) {
            throw new IllegalArgumentException("messageLength must not be negative");
        }
        if (methodName.isBlank()) {
            throw new IllegalArgumentException("methodName must not be blank");
        }
        if (requestId < 0) {
            throw new IllegalArgumentException("requestId must not be negative");
        }
    }

    @Override
    public MrcpMessageType messageType() {
        return MrcpMessageType.REQUEST;
    }

    public MrcpRequestLine withMessageLength(int newMessageLength) {
        return new MrcpRequestLine(version, newMessageLength, methodName, requestId);
    }
}
