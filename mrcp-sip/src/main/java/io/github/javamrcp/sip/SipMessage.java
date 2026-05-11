package io.github.javamrcp.sip;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable SIP/2.0 message with ordered headers and an optional SDP body.
 */
public final class SipMessage {
    public static final String CALL_ID = "Call-ID";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CSEQ = "CSeq";
    public static final String FROM = "From";
    public static final String TO = "To";
    public static final String VIA = "Via";

    private final SipStartLine startLine;
    private final List<SipHeader> headers;
    private final byte[] body;

    public SipMessage(SipStartLine startLine, List<SipHeader> headers, byte[] body) {
        this.startLine = Objects.requireNonNull(startLine, "startLine");
        this.headers = List.copyOf(Objects.requireNonNull(headers, "headers"));
        this.body = Objects.requireNonNull(body, "body").clone();
    }

    public static Builder request(SipMethod method, URI requestUri) {
        return new Builder(new SipRequestLine(method, requestUri, SipVersion.SIP_2_0));
    }

    public static Builder response(int statusCode, String reasonPhrase) {
        return new Builder(new SipResponseLine(SipVersion.SIP_2_0, statusCode, reasonPhrase));
    }

    public SipStartLine startLine() {
        return startLine;
    }

    public List<SipHeader> headers() {
        return headers;
    }

    public byte[] body() {
        return body.clone();
    }

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public SipMessageType messageType() {
        return startLine.messageType();
    }

    public Optional<String> firstHeaderValue(String name) {
        return headers.stream()
                .filter(header -> header.hasName(name))
                .map(SipHeader::value)
                .findFirst();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SipMessage that)) {
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
        return "SipMessage{"
                + "startLine=" + startLine
                + ", headers=" + headers
                + ", bodyLength=" + body.length
                + '}';
    }

    public static final class Builder {
        private final SipStartLine startLine;
        private final List<SipHeader> headers = new ArrayList<>();
        private byte[] body = new byte[0];

        private Builder(SipStartLine startLine) {
            this.startLine = startLine;
        }

        public Builder header(String name, String value) {
            headers.add(new SipHeader(name, value));
            return this;
        }

        public Builder body(String contentType, String bodyText) {
            Objects.requireNonNull(contentType, "contentType");
            Objects.requireNonNull(bodyText, "bodyText");
            header(CONTENT_TYPE, contentType);
            this.body = bodyText.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = Objects.requireNonNull(body, "body").clone();
            return this;
        }

        public SipMessage build() {
            return new SipMessage(startLine, headers, body);
        }
    }
}
