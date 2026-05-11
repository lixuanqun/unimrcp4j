package io.github.javamrcp.sdp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Parser for the SDP subset needed by MRCPv2 SIP offer/answer.
 */
public final class SdpParser {
    public SdpSession parse(String text) {
        Objects.requireNonNull(text, "text");
        int version = -1;
        SdpOrigin origin = null;
        String sessionName = "-";
        SdpConnection sessionConnection = null;
        String timing = "0 0";
        List<SdpAttribute> sessionAttributes = new ArrayList<>();
        List<SdpMediaDescription> mediaDescriptions = new ArrayList<>();
        MediaBuilder currentMedia = null;

        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        for (String rawLine : lines) {
            if (rawLine.isBlank()) {
                continue;
            }
            if (rawLine.length() < 2 || rawLine.charAt(1) != '=') {
                throw new SdpParseException("Invalid SDP line: " + rawLine);
            }
            char type = rawLine.charAt(0);
            String value = rawLine.substring(2).trim();
            switch (type) {
                case 'v' -> version = parseInt(value, "version");
                case 'o' -> origin = parseOrigin(value);
                case 's' -> sessionName = value;
                case 'c' -> {
                    if (currentMedia == null) {
                        sessionConnection = parseConnection(value);
                    } else {
                        currentMedia.connection(parseConnection(value));
                    }
                }
                case 't' -> timing = value;
                case 'a' -> {
                    SdpAttribute attribute = parseAttribute(value);
                    if (currentMedia == null) {
                        sessionAttributes.add(attribute);
                    } else {
                        currentMedia.attribute(attribute);
                    }
                }
                case 'm' -> {
                    if (currentMedia != null) {
                        mediaDescriptions.add(currentMedia.build());
                    }
                    currentMedia = parseMedia(value);
                }
                default -> {
                    // Unknown session/media lines are ignored for now; MRCP setup only needs v/o/s/c/t/m/a.
                }
            }
        }

        if (currentMedia != null) {
            mediaDescriptions.add(currentMedia.build());
        }
        if (version < 0) {
            throw new SdpParseException("SDP version line is required");
        }
        if (origin == null) {
            throw new SdpParseException("SDP origin line is required");
        }
        return new SdpSession(version, origin, sessionName, sessionConnection, timing, sessionAttributes, mediaDescriptions);
    }

    private SdpOrigin parseOrigin(String value) {
        String[] parts = value.split("\\s+");
        if (parts.length != 6) {
            throw new SdpParseException("Invalid SDP origin line: " + value);
        }
        return new SdpOrigin(
                parts[0],
                parseLong(parts[1], "origin session id"),
                parseLong(parts[2], "origin session version"),
                parts[3],
                parts[4],
                parts[5]);
    }

    private SdpConnection parseConnection(String value) {
        String[] parts = value.split("\\s+");
        if (parts.length != 3) {
            throw new SdpParseException("Invalid SDP connection line: " + value);
        }
        return new SdpConnection(parts[0], parts[1], parts[2]);
    }

    private SdpAttribute parseAttribute(String value) {
        int separator = value.indexOf(':');
        if (separator < 0) {
            return SdpAttribute.of(value);
        }
        return SdpAttribute.of(value.substring(0, separator).trim(), value.substring(separator + 1).trim());
    }

    private MediaBuilder parseMedia(String value) {
        String[] parts = value.split("\\s+");
        if (parts.length < 3) {
            throw new SdpParseException("Invalid SDP media line: " + value);
        }
        Port port = parsePort(parts[1]);
        List<String> formats = new ArrayList<>();
        for (int index = 3; index < parts.length; index++) {
            formats.add(parts[index]);
        }
        return new MediaBuilder(
                SdpMediaType.fromWireValue(parts[0]),
                parts[0],
                port.port(),
                port.portCount(),
                parts[2],
                formats);
    }

    private Port parsePort(String value) {
        String[] parts = value.split("/", 2);
        int port = parseInt(parts[0], "media port");
        int portCount = parts.length == 2 ? parseInt(parts[1], "media port count") : 1;
        return new Port(port, portCount);
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new SdpParseException("Invalid SDP " + fieldName + ": " + value, ex);
        }
    }

    private long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new SdpParseException("Invalid SDP " + fieldName + ": " + value, ex);
        }
    }

    private record Port(int port, int portCount) {
    }

    private static final class MediaBuilder {
        private final SdpMediaType mediaType;
        private final String rawMediaType;
        private final int port;
        private final int portCount;
        private final String protocol;
        private final List<String> formats;
        private SdpConnection connection;
        private final List<SdpAttribute> attributes = new ArrayList<>();

        private MediaBuilder(
                SdpMediaType mediaType,
                String rawMediaType,
                int port,
                int portCount,
                String protocol,
                List<String> formats) {
            this.mediaType = mediaType;
            this.rawMediaType = rawMediaType;
            this.port = port;
            this.portCount = portCount;
            this.protocol = protocol;
            this.formats = formats;
        }

        void connection(SdpConnection connection) {
            this.connection = connection;
        }

        void attribute(SdpAttribute attribute) {
            attributes.add(attribute);
        }

        SdpMediaDescription build() {
            return new SdpMediaDescription(
                    mediaType,
                    rawMediaType,
                    port,
                    portCount,
                    protocol,
                    formats,
                    connection,
                    attributes);
        }
    }
}
