package io.github.javamrcp.sip;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Parses complete SIP/2.0 datagram payloads.
 */
public final class SipMessageParser {
    private static final byte CARRIAGE_RETURN = '\r';
    private static final byte LINE_FEED = '\n';

    public SipMessage parse(String datagram) {
        Objects.requireNonNull(datagram, "datagram");
        return parse(datagram.getBytes(StandardCharsets.UTF_8));
    }

    public SipMessage parse(byte[] datagram) {
        Objects.requireNonNull(datagram, "datagram");
        HeaderBoundary boundary = findHeaderBoundary(datagram);
        if (boundary == null) {
            throw new SipParseException("SIP message does not contain a header terminator");
        }

        String headerSection = new String(datagram, 0, boundary.separatorStart(), StandardCharsets.US_ASCII);
        String[] lines = headerSection.split("\\r?\\n", -1);
        if (lines.length == 0 || lines[0].isBlank()) {
            throw new SipParseException("SIP message is missing a start line");
        }

        SipStartLine startLine = parseStartLine(lines[0]);
        List<SipHeader> headers = parseHeaders(lines);
        int contentLength = contentLength(headers);
        int availableBodyLength = datagram.length - boundary.bodyStart();
        if (availableBodyLength != contentLength) {
            throw new SipParseException(
                    "Content-Length mismatch: header declares "
                            + contentLength
                            + " bytes but datagram contains "
                            + availableBodyLength);
        }

        byte[] body = Arrays.copyOfRange(datagram, boundary.bodyStart(), datagram.length);
        return new SipMessage(startLine, headers, body);
    }

    private SipStartLine parseStartLine(String line) {
        String[] parts = line.trim().split("\\s+", 3);
        if (parts.length < 3) {
            throw new SipParseException("Invalid SIP start line: " + line);
        }

        if (SipVersion.SIP_2_0.wireValue().equals(parts[0])) {
            return new SipResponseLine(SipVersion.SIP_2_0, parseStatusCode(parts[1]), parts[2]);
        }

        if (!SipVersion.SIP_2_0.wireValue().equals(parts[2])) {
            throw new SipParseException("Unsupported SIP version in start line: " + line);
        }
        SipMethod method = SipMethod.fromWireValue(parts[0])
                .orElseThrow(() -> new SipParseException("Unsupported SIP method: " + parts[0]));
        return new SipRequestLine(method, parseUri(parts[1]), SipVersion.SIP_2_0);
    }

    private List<SipHeader> parseHeaders(String[] lines) {
        List<SipHeader> headers = new ArrayList<>();
        SipHeader previous = null;
        for (int index = 1; index < lines.length; index++) {
            String line = lines[index];
            if (line.isEmpty()) {
                continue;
            }
            if ((line.charAt(0) == ' ' || line.charAt(0) == '\t') && previous != null) {
                headers.remove(headers.size() - 1);
                previous = new SipHeader(previous.name(), previous.value() + " " + line.trim());
                headers.add(previous);
                continue;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                throw new SipParseException("Invalid SIP header line: " + line);
            }
            previous = new SipHeader(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
            headers.add(previous);
        }
        return headers;
    }

    private int contentLength(List<SipHeader> headers) {
        int contentLength = 0;
        boolean seen = false;
        for (SipHeader header : headers) {
            if (header.hasName(SipMessage.CONTENT_LENGTH) || header.hasName("l")) {
                if (seen) {
                    throw new SipParseException("Duplicate Content-Length header");
                }
                contentLength = parseNonNegativeInt(header.value(), "Content-Length");
                seen = true;
            }
        }
        return contentLength;
    }

    private URI parseUri(String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException ex) {
            throw new SipParseException("Invalid SIP request URI: " + value, ex);
        }
    }

    private int parseStatusCode(String value) {
        int statusCode = parseNonNegativeInt(value, "status code");
        if (statusCode < 100 || statusCode > 699) {
            throw new SipParseException("SIP status code must be between 100 and 699");
        }
        return statusCode;
    }

    private int parseNonNegativeInt(String value, String fieldName) {
        try {
            int result = Integer.parseInt(value);
            if (result < 0) {
                throw new SipParseException(fieldName + " must not be negative");
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new SipParseException("Invalid " + fieldName + ": " + value, ex);
        }
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
