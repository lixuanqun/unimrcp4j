package io.github.javamrcp.sdp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * SDP media section beginning with an {@code m=} line.
 */
public final class SdpMediaDescription {
    private final SdpMediaType mediaType;
    private final String rawMediaType;
    private final int port;
    private final int portCount;
    private final String protocol;
    private final List<String> formats;
    private final SdpConnection connection;
    private final List<SdpAttribute> attributes;

    public SdpMediaDescription(
            SdpMediaType mediaType,
            String rawMediaType,
            int port,
            int portCount,
            String protocol,
            List<String> formats,
            SdpConnection connection,
            List<SdpAttribute> attributes) {
        this.mediaType = Objects.requireNonNull(mediaType, "mediaType");
        this.rawMediaType = Objects.requireNonNull(rawMediaType, "rawMediaType");
        this.port = port;
        this.portCount = portCount;
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.formats = List.copyOf(Objects.requireNonNull(formats, "formats"));
        this.connection = connection;
        this.attributes = List.copyOf(Objects.requireNonNull(attributes, "attributes"));
        if (rawMediaType.isBlank()) {
            throw new IllegalArgumentException("rawMediaType must not be blank");
        }
        if (port < 0 || port > 65_535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
        if (portCount < 1) {
            throw new IllegalArgumentException("portCount must be positive");
        }
        if (protocol.isBlank()) {
            throw new IllegalArgumentException("protocol must not be blank");
        }
    }

    public static Builder builder(SdpMediaType mediaType, int port, String protocol) {
        return new Builder(mediaType, mediaType.wireValue(), port, protocol);
    }

    public SdpMediaType mediaType() {
        return mediaType;
    }

    public String rawMediaType() {
        return rawMediaType;
    }

    public int port() {
        return port;
    }

    public int portCount() {
        return portCount;
    }

    public String protocol() {
        return protocol;
    }

    public List<String> formats() {
        return formats;
    }

    public Optional<SdpConnection> connection() {
        return Optional.ofNullable(connection);
    }

    public List<SdpAttribute> attributes() {
        return attributes;
    }

    public boolean isMrcpControlMedia() {
        return mediaType == SdpMediaType.APPLICATION && protocol.equalsIgnoreCase("TCP/MRCPv2");
    }

    public boolean isRtpAudioMedia() {
        return mediaType == SdpMediaType.AUDIO && protocol.toUpperCase().startsWith("RTP/");
    }

    public List<String> attributeValues(String name) {
        return attributes.stream()
                .filter(attribute -> attribute.hasName(name))
                .map(SdpAttribute::value)
                .filter(Objects::nonNull)
                .toList();
    }

    public Optional<String> firstAttributeValue(String name) {
        return attributeValues(name).stream().findFirst();
    }

    public List<String> resources() {
        return attributeValues("resource");
    }

    public Optional<String> channel() {
        return firstAttributeValue("channel");
    }

    public List<String> rtpMaps() {
        return attributeValues("rtpmap");
    }

    public String mediaLineValue() {
        String portValue = portCount == 1 ? Integer.toString(port) : port + "/" + portCount;
        String suffix = formats.isEmpty() ? "" : " " + String.join(" ", formats);
        return rawMediaType + " " + portValue + " " + protocol + suffix;
    }

    public static final class Builder {
        private final SdpMediaType mediaType;
        private final String rawMediaType;
        private final int port;
        private final String protocol;
        private int portCount = 1;
        private final List<String> formats = new ArrayList<>();
        private SdpConnection connection;
        private final List<SdpAttribute> attributes = new ArrayList<>();

        private Builder(SdpMediaType mediaType, String rawMediaType, int port, String protocol) {
            this.mediaType = mediaType;
            this.rawMediaType = rawMediaType;
            this.port = port;
            this.protocol = protocol;
        }

        public Builder portCount(int portCount) {
            this.portCount = portCount;
            return this;
        }

        public Builder format(String format) {
            formats.add(format);
            return this;
        }

        public Builder formats(List<String> formats) {
            this.formats.addAll(formats);
            return this;
        }

        public Builder connection(SdpConnection connection) {
            this.connection = connection;
            return this;
        }

        public Builder attribute(String name) {
            attributes.add(SdpAttribute.of(name));
            return this;
        }

        public Builder attribute(String name, String value) {
            attributes.add(SdpAttribute.of(name, value));
            return this;
        }

        public Builder attributes(List<SdpAttribute> attributes) {
            this.attributes.addAll(attributes);
            return this;
        }

        public SdpMediaDescription build() {
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
