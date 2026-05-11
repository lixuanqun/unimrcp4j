package io.github.javamrcp.core;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable MRCP message consisting of a start line, ordered headers, and an optional MIME body.
 */
public final class MrcpMessage {
    public static final String CHANNEL_IDENTIFIER = "Channel-Identifier";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";

    private final MrcpStartLine startLine;
    private final List<MrcpHeader> headers;
    private final byte[] body;

    public MrcpMessage(MrcpStartLine startLine, List<MrcpHeader> headers, byte[] body) {
        this.startLine = Objects.requireNonNull(startLine, "startLine");
        this.headers = List.copyOf(Objects.requireNonNull(headers, "headers"));
        this.body = Objects.requireNonNull(body, "body").clone();
    }

    public static Builder request(String methodName, long requestId) {
        return new Builder(new MrcpRequestLine(MrcpVersion.MRCP_2_0, 0, methodName, requestId));
    }

    public static Builder response(long requestId, int statusCode, MrcpRequestState requestState) {
        return new Builder(new MrcpResponseLine(MrcpVersion.MRCP_2_0, 0, requestId, statusCode, requestState));
    }

    public static Builder event(String eventName, long requestId, MrcpRequestState requestState) {
        return new Builder(new MrcpEventLine(MrcpVersion.MRCP_2_0, 0, eventName, requestId, requestState));
    }

    public MrcpStartLine startLine() {
        return startLine;
    }

    public List<MrcpHeader> headers() {
        return headers;
    }

    public byte[] body() {
        return body.clone();
    }

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public MrcpMessageType messageType() {
        return startLine.messageType();
    }

    public Optional<String> firstHeaderValue(String name) {
        return headers.stream()
                .filter(header -> header.hasName(name))
                .map(MrcpHeader::value)
                .findFirst();
    }

    public Optional<MrcpChannelIdentifier> channelIdentifier() {
        return firstHeaderValue(CHANNEL_IDENTIFIER).map(MrcpChannelIdentifier::parse);
    }

    public MrcpMessage withStartLine(MrcpStartLine newStartLine) {
        return new MrcpMessage(newStartLine, headers, body);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MrcpMessage that)) {
            return false;
        }
        return startLine.equals(that.startLine)
                && headers.equals(that.headers)
                && Arrays.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(startLine, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

    @Override
    public String toString() {
        return "MrcpMessage{"
                + "startLine=" + startLine
                + ", headers=" + headers
                + ", bodyLength=" + body.length
                + '}';
    }

    public static final class Builder {
        private final MrcpStartLine startLine;
        private final List<MrcpHeader> headers = new ArrayList<>();
        private byte[] body = new byte[0];

        private Builder(MrcpStartLine startLine) {
            this.startLine = startLine;
        }

        public Builder header(String name, String value) {
            headers.add(new MrcpHeader(name, value));
            return this;
        }

        public Builder channelIdentifier(MrcpChannelIdentifier channelIdentifier) {
            return header(CHANNEL_IDENTIFIER, channelIdentifier.wireValue());
        }

        public Builder body(String contentType, String bodyText) {
            Objects.requireNonNull(bodyText, "bodyText");
            header(CONTENT_TYPE, contentType);
            this.body = bodyText.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = Objects.requireNonNull(body, "body").clone();
            return this;
        }

        public MrcpMessage build() {
            return new MrcpMessage(startLine, headers, body);
        }
    }
}
