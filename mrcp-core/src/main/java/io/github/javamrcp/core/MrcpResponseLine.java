package io.github.javamrcp.core;

import java.util.Objects;

/**
 * MRCP/2.0 response start line: {@code MRCP/2.0 length request-id status-code request-state}.
 */
public record MrcpResponseLine(
        MrcpVersion version,
        int messageLength,
        long requestId,
        int statusCode,
        MrcpRequestState requestState) implements MrcpStartLine {
    public MrcpResponseLine {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(requestState, "requestState");
        if (messageLength < 0) {
            throw new IllegalArgumentException("messageLength must not be negative");
        }
        if (requestId < 0) {
            throw new IllegalArgumentException("requestId must not be negative");
        }
        if (statusCode < 100 || statusCode > 999) {
            throw new IllegalArgumentException("statusCode must be a three-digit MRCP status");
        }
    }

    @Override
    public MrcpMessageType messageType() {
        return MrcpMessageType.RESPONSE;
    }

    public MrcpResponseLine withMessageLength(int newMessageLength) {
        return new MrcpResponseLine(version, newMessageLength, requestId, statusCode, requestState);
    }
}
