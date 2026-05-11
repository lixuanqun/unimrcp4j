package io.github.javamrcp.codec;

import io.github.javamrcp.core.MrcpEventLine;
import io.github.javamrcp.core.MrcpHeader;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpResponseLine;
import io.github.javamrcp.core.MrcpStartLine;
import io.github.javamrcp.core.MrcpVersion;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Parses complete MRCP/2.0 frames. TCP stream fragmentation is handled by {@link MrcpFrameDecoder}.
 */
public final class MrcpMessageParser {
    private static final byte CARRIAGE_RETURN = '\r';
    private static final byte LINE_FEED = '\n';

    public MrcpMessage parse(String frame) {
        Objects.requireNonNull(frame, "frame");
        return parse(frame.getBytes(StandardCharsets.UTF_8));
    }

    public MrcpMessage parse(byte[] frame) {
        Objects.requireNonNull(frame, "frame");
        HeaderBoundary boundary = findHeaderBoundary(frame);
        if (boundary == null) {
            throw new MrcpParseException("MRCP message does not contain a header terminator");
        }

        String headerSection = new String(frame, 0, boundary.separatorStart(), StandardCharsets.US_ASCII);
        String[] lines = headerSection.split("\\r?\\n", -1);
        if (lines.length == 0 || lines[0].isBlank()) {
            throw new MrcpParseException("MRCP message is missing a start line");
        }

        MrcpStartLine startLine = parseStartLine(lines[0]);
        List<MrcpHeader> headers = parseHeaders(lines);
        int contentLength = contentLength(headers);
        int availableBodyLength = frame.length - boundary.bodyStart();
        if (availableBodyLength != contentLength) {
            throw new MrcpParseException(
                    "Content-Length mismatch: header declares "
                            + contentLength
                            + " bytes but frame contains "
                            + availableBodyLength);
        }

        byte[] body = Arrays.copyOfRange(frame, boundary.bodyStart(), frame.length);
        return new MrcpMessage(startLine, headers, body);
    }

    private MrcpStartLine parseStartLine(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length < 4 || !MrcpVersion.MRCP_2_0.wireValue().equals(parts[0])) {
            throw new MrcpParseException("Unsupported MRCP start line: " + line);
        }

        int messageLength = parseInt(parts[1], "message length");
        if (parts.length == 4) {
            return new MrcpRequestLine(MrcpVersion.MRCP_2_0, messageLength, parts[2], parseLong(parts[3], "request id"));
        }

        if (parts.length != 5) {
            throw new MrcpParseException("Invalid MRCP start line token count: " + line);
        }

        if (isInteger(parts[2])) {
            return new MrcpResponseLine(
                    MrcpVersion.MRCP_2_0,
                    messageLength,
                    parseLong(parts[2], "request id"),
                    parseInt(parts[3], "status code"),
                    parseRequestState(parts[4]));
        }

        return new MrcpEventLine(
                MrcpVersion.MRCP_2_0,
                messageLength,
                parts[2],
                parseLong(parts[3], "request id"),
                parseRequestState(parts[4]));
    }

    private List<MrcpHeader> parseHeaders(String[] lines) {
        List<MrcpHeader> headers = new ArrayList<>();
        for (int index = 1; index < lines.length; index++) {
            String line = lines[index];
            if (line.isEmpty()) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new MrcpParseException("Invalid MRCP header line: " + line);
            }
            String name = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            headers.add(new MrcpHeader(name, value));
        }
        return headers;
    }

    private int contentLength(List<MrcpHeader> headers) {
        int contentLength = 0;
        boolean seen = false;
        for (MrcpHeader header : headers) {
            if (header.hasName(MrcpMessage.CONTENT_LENGTH)) {
                if (seen) {
                    throw new MrcpParseException("Duplicate Content-Length header");
                }
                contentLength = parseInt(header.value(), "Content-Length");
                if (contentLength < 0) {
                    throw new MrcpParseException("Content-Length must not be negative");
                }
                seen = true;
            }
        }
        return contentLength;
    }

    private MrcpRequestState parseRequestState(String value) {
        return MrcpRequestState.fromWireValue(value)
                .orElseThrow(() -> new MrcpParseException("Unknown MRCP request state: " + value));
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new MrcpParseException("Invalid " + fieldName + ": " + value, ex);
        }
    }

    private long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new MrcpParseException("Invalid " + fieldName + ": " + value, ex);
        }
    }

    private boolean isInteger(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private HeaderBoundary findHeaderBoundary(byte[] frame) {
        for (int index = 0; index < frame.length - 1; index++) {
            if (index < frame.length - 3
                    && frame[index] == CARRIAGE_RETURN
                    && frame[index + 1] == LINE_FEED
                    && frame[index + 2] == CARRIAGE_RETURN
                    && frame[index + 3] == LINE_FEED) {
                return new HeaderBoundary(index, index + 4);
            }
            if (frame[index] == LINE_FEED && frame[index + 1] == LINE_FEED) {
                return new HeaderBoundary(index, index + 2);
            }
        }
        return null;
    }

    private record HeaderBoundary(int separatorStart, int bodyStart) {
    }
}
