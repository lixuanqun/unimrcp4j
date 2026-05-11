package io.github.javamrcp.server;

import java.time.Duration;
import java.util.Objects;

/**
 * Transport-level configuration for the first Netty MRCPv2 server milestone.
 */
public record MrcpServerConfig(
        String advertisedHost,
        String sipHost,
        int sipPort,
        String mrcpHost,
        int mrcpPort,
        int rtpPort,
        int maxConcurrentSessions,
        Duration shutdownQuietPeriod,
        Duration shutdownTimeout) {
    private static final int MAX_PORT = 65_535;

    public MrcpServerConfig {
        Objects.requireNonNull(advertisedHost, "advertisedHost");
        Objects.requireNonNull(sipHost, "sipHost");
        Objects.requireNonNull(mrcpHost, "mrcpHost");
        Objects.requireNonNull(shutdownQuietPeriod, "shutdownQuietPeriod");
        Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
        if (advertisedHost.isBlank()) {
            throw new IllegalArgumentException("advertisedHost must not be blank");
        }
        requirePort("sipPort", sipPort);
        requirePort("mrcpPort", mrcpPort);
        requirePort("rtpPort", rtpPort);
        if (maxConcurrentSessions <= 0) {
            throw new IllegalArgumentException("maxConcurrentSessions must be positive");
        }
        if (shutdownQuietPeriod.isNegative()) {
            throw new IllegalArgumentException("shutdownQuietPeriod must not be negative");
        }
        if (shutdownTimeout.isNegative() || shutdownTimeout.isZero()) {
            throw new IllegalArgumentException("shutdownTimeout must be positive");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MrcpServerConfig defaults() {
        return builder().build();
    }

    private static void requirePort(String name, int port) {
        if (port < 0 || port > MAX_PORT) {
            throw new IllegalArgumentException(name + " must be between 0 and " + MAX_PORT);
        }
    }

    public static final class Builder {
        private String advertisedHost = "127.0.0.1";
        private String sipHost = "0.0.0.0";
        private int sipPort = 8060;
        private String mrcpHost = "0.0.0.0";
        private int mrcpPort = 1544;
        private int rtpPort = 4000;
        private int maxConcurrentSessions = 1_000;
        private Duration shutdownQuietPeriod = Duration.ofMillis(100);
        private Duration shutdownTimeout = Duration.ofSeconds(5);

        private Builder() {
        }

        public Builder advertisedHost(String advertisedHost) {
            this.advertisedHost = advertisedHost;
            return this;
        }

        public Builder sipHost(String sipHost) {
            this.sipHost = sipHost;
            return this;
        }

        public Builder sipPort(int sipPort) {
            this.sipPort = sipPort;
            return this;
        }

        public Builder mrcpHost(String mrcpHost) {
            this.mrcpHost = mrcpHost;
            return this;
        }

        public Builder mrcpPort(int mrcpPort) {
            this.mrcpPort = mrcpPort;
            return this;
        }

        public Builder rtpPort(int rtpPort) {
            this.rtpPort = rtpPort;
            return this;
        }

        public Builder maxConcurrentSessions(int maxConcurrentSessions) {
            this.maxConcurrentSessions = maxConcurrentSessions;
            return this;
        }

        public Builder shutdownQuietPeriod(Duration shutdownQuietPeriod) {
            this.shutdownQuietPeriod = shutdownQuietPeriod;
            return this;
        }

        public Builder shutdownTimeout(Duration shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
            return this;
        }

        public MrcpServerConfig build() {
            return new MrcpServerConfig(
                    advertisedHost,
                    sipHost,
                    sipPort,
                    mrcpHost,
                    mrcpPort,
                    rtpPort,
                    maxConcurrentSessions,
                    shutdownQuietPeriod,
                    shutdownTimeout);
        }
    }
}
