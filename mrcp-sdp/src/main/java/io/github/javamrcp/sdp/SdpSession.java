package io.github.javamrcp.sdp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * SDP session description used by MRCPv2 SIP offer/answer.
 */
public final class SdpSession {
    private final int version;
    private final SdpOrigin origin;
    private final String sessionName;
    private final SdpConnection connection;
    private final String timing;
    private final List<SdpAttribute> attributes;
    private final List<SdpMediaDescription> mediaDescriptions;

    public SdpSession(
            int version,
            SdpOrigin origin,
            String sessionName,
            SdpConnection connection,
            String timing,
            List<SdpAttribute> attributes,
            List<SdpMediaDescription> mediaDescriptions) {
        this.version = version;
        this.origin = Objects.requireNonNull(origin, "origin");
        this.sessionName = Objects.requireNonNull(sessionName, "sessionName");
        this.connection = connection;
        this.timing = Objects.requireNonNull(timing, "timing");
        this.attributes = List.copyOf(Objects.requireNonNull(attributes, "attributes"));
        this.mediaDescriptions = List.copyOf(Objects.requireNonNull(mediaDescriptions, "mediaDescriptions"));
        if (version != 0) {
            throw new IllegalArgumentException("only SDP version 0 is supported");
        }
        if (timing.isBlank()) {
            throw new IllegalArgumentException("timing must not be blank");
        }
    }

    public static Builder builder(SdpOrigin origin) {
        return new Builder(origin);
    }

    public int version() {
        return version;
    }

    public SdpOrigin origin() {
        return origin;
    }

    public String sessionName() {
        return sessionName;
    }

    public Optional<SdpConnection> connection() {
        return Optional.ofNullable(connection);
    }

    public String timing() {
        return timing;
    }

    public List<SdpAttribute> attributes() {
        return attributes;
    }

    public List<SdpMediaDescription> mediaDescriptions() {
        return mediaDescriptions;
    }

    public List<SdpMediaDescription> mrcpControlMedia() {
        return mediaDescriptions.stream()
                .filter(SdpMediaDescription::isMrcpControlMedia)
                .toList();
    }

    public List<SdpMediaDescription> audioMedia() {
        return mediaDescriptions.stream()
                .filter(media -> media.mediaType() == SdpMediaType.AUDIO)
                .toList();
    }

    public static final class Builder {
        private final SdpOrigin origin;
        private int version = 0;
        private String sessionName = "-";
        private SdpConnection connection;
        private String timing = "0 0";
        private final List<SdpAttribute> attributes = new ArrayList<>();
        private final List<SdpMediaDescription> mediaDescriptions = new ArrayList<>();

        private Builder(SdpOrigin origin) {
            this.origin = origin;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Builder sessionName(String sessionName) {
            this.sessionName = sessionName;
            return this;
        }

        public Builder connection(SdpConnection connection) {
            this.connection = connection;
            return this;
        }

        public Builder timing(String timing) {
            this.timing = timing;
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

        public Builder media(SdpMediaDescription mediaDescription) {
            mediaDescriptions.add(mediaDescription);
            return this;
        }

        public SdpSession build() {
            return new SdpSession(version, origin, sessionName, connection, timing, attributes, mediaDescriptions);
        }
    }
}
