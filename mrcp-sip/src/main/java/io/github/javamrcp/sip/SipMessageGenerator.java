package io.github.javamrcp.sip;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Serializes SIP/2.0 messages and normalizes Content-Length.
 */
public final class SipMessageGenerator {
    private static final String CRLF = "\r\n";

    public byte[] generate(SipMessage message) {
        Objects.requireNonNull(message, "message");
        byte[] body = message.body();
        List<SipHeader> headers = normalizeContentLength(message.headers(), body.length);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeAscii(output, formatStartLine(message.startLine()));
        writeAscii(output, CRLF);
        for (SipHeader header : headers) {
            writeAscii(output, header.name());
            writeAscii(output, ":");
            writeAscii(output, header.value());
            writeAscii(output, CRLF);
        }
        writeAscii(output, CRLF);
        output.writeBytes(body);
        return output.toByteArray();
    }

    public String generateString(SipMessage message) {
        return new String(generate(message), StandardCharsets.UTF_8);
    }

    private List<SipHeader> normalizeContentLength(List<SipHeader> headers, int bodyLength) {
        List<SipHeader> normalized = new ArrayList<>(headers.size() + 1);
        boolean contentLengthWritten = false;
        for (SipHeader header : headers) {
            if (header.hasName(SipMessage.CONTENT_LENGTH) || header.hasName("l")) {
                if (!contentLengthWritten) {
                    normalized.add(new SipHeader(SipMessage.CONTENT_LENGTH, Integer.toString(bodyLength)));
                    contentLengthWritten = true;
                }
            } else {
                normalized.add(header);
            }
        }
        if (!contentLengthWritten) {
            normalized.add(new SipHeader(SipMessage.CONTENT_LENGTH, Integer.toString(bodyLength)));
        }
        return normalized;
    }

    private String formatStartLine(SipStartLine startLine) {
        if (startLine instanceof SipRequestLine requestLine) {
            return requestLine.method().wireValue()
                    + " "
                    + requestLine.requestUri()
                    + " "
                    + requestLine.version().wireValue();
        }
        if (startLine instanceof SipResponseLine responseLine) {
            return responseLine.version().wireValue()
                    + " "
                    + responseLine.statusCode()
                    + " "
                    + responseLine.reasonPhrase();
        }
        throw new IllegalArgumentException("Unsupported SIP start line type: " + startLine.getClass());
    }

    private void writeAscii(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }
}
