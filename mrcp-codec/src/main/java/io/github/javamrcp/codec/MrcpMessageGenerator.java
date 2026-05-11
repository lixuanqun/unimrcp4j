package io.github.javamrcp.codec;

import io.github.javamrcp.core.MrcpEventLine;
import io.github.javamrcp.core.MrcpHeader;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpResponseLine;
import io.github.javamrcp.core.MrcpStartLine;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Serializes MRCP messages and normalizes message length and Content-Length fields.
 */
public final class MrcpMessageGenerator {
    private static final String CRLF = "\r\n";
    private static final int MAX_LENGTH_RECALCULATIONS = 8;

    public byte[] generate(MrcpMessage message) {
        Objects.requireNonNull(message, "message");
        byte[] body = message.body();
        List<MrcpHeader> normalizedHeaders = normalizeContentLength(message.headers(), body.length);

        int messageLength = Math.max(0, message.startLine().messageLength());
        byte[] encoded = new byte[0];
        for (int attempt = 0; attempt < MAX_LENGTH_RECALCULATIONS; attempt++) {
            encoded = encode(message.startLine(), messageLength, normalizedHeaders, body);
            if (encoded.length == messageLength) {
                return encoded;
            }
            messageLength = encoded.length;
        }
        throw new IllegalStateException("Failed to converge MRCP message length");
    }

    public String generateString(MrcpMessage message) {
        return new String(generate(message), StandardCharsets.UTF_8);
    }

    private byte[] encode(
            MrcpStartLine startLine,
            int messageLength,
            List<MrcpHeader> headers,
            byte[] body) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeAscii(output, formatStartLine(startLine, messageLength));
        writeAscii(output, CRLF);
        for (MrcpHeader header : headers) {
            writeAscii(output, header.name());
            writeAscii(output, ":");
            writeAscii(output, header.value());
            writeAscii(output, CRLF);
        }
        writeAscii(output, CRLF);
        output.writeBytes(body);
        return output.toByteArray();
    }

    private List<MrcpHeader> normalizeContentLength(List<MrcpHeader> headers, int bodyLength) {
        List<MrcpHeader> normalized = new ArrayList<>(headers.size() + 1);
        boolean contentLengthWritten = false;
        for (MrcpHeader header : headers) {
            if (header.hasName(MrcpMessage.CONTENT_LENGTH)) {
                if (!contentLengthWritten) {
                    normalized.add(new MrcpHeader(MrcpMessage.CONTENT_LENGTH, Integer.toString(bodyLength)));
                    contentLengthWritten = true;
                }
            } else {
                normalized.add(header);
            }
        }
        if (bodyLength > 0 && !contentLengthWritten) {
            normalized.add(new MrcpHeader(MrcpMessage.CONTENT_LENGTH, Integer.toString(bodyLength)));
        }
        return normalized;
    }

    private String formatStartLine(MrcpStartLine startLine, int messageLength) {
        if (startLine instanceof MrcpRequestLine requestLine) {
            return requestLine.version().wireValue()
                    + " "
                    + messageLength
                    + " "
                    + requestLine.methodName()
                    + " "
                    + requestLine.requestId();
        }
        if (startLine instanceof MrcpResponseLine responseLine) {
            return responseLine.version().wireValue()
                    + " "
                    + messageLength
                    + " "
                    + responseLine.requestId()
                    + " "
                    + responseLine.statusCode()
                    + " "
                    + responseLine.requestState().wireValue();
        }
        if (startLine instanceof MrcpEventLine eventLine) {
            return eventLine.version().wireValue()
                    + " "
                    + messageLength
                    + " "
                    + eventLine.eventName()
                    + " "
                    + eventLine.requestId()
                    + " "
                    + eventLine.requestState().wireValue();
        }
        throw new IllegalArgumentException("Unsupported MRCP start line type: " + startLine.getClass());
    }

    private void writeAscii(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }
}
