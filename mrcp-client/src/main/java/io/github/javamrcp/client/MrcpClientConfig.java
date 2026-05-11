package io.github.javamrcp.client;

import java.util.Objects;

/**
 * Configuration for generating MRCPv2 client SIP/SDP offers.
 */
public record MrcpClientConfig(
        String localHost,
        int localSipPort,
        int localRtpPort,
        String serverHost,
        int serverSipPort,
        String fromUser,
        String toUser) {
    public MrcpClientConfig {
        Objects.requireNonNull(localHost, "localHost");
        Objects.requireNonNull(serverHost, "serverHost");
        Objects.requireNonNull(fromUser, "fromUser");
        Objects.requireNonNull(toUser, "toUser");
        requirePort("localSipPort", localSipPort);
        requirePort("localRtpPort", localRtpPort);
        requirePort("serverSipPort", serverSipPort);
        if (localHost.isBlank() || serverHost.isBlank() || fromUser.isBlank() || toUser.isBlank()) {
            throw new IllegalArgumentException("host and user fields must not be blank");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private static void requirePort(String name, int port) {
        if (port < 0 || port > 65_535) {
            throw new IllegalArgumentException(name + " must be between 0 and 65535");
        }
    }

    public static final class Builder {
        private String localHost = "127.0.0.1";
        private int localSipPort = 0;
        private int localRtpPort = 4000;
        private String serverHost = "127.0.0.1";
        private int serverSipPort = 8060;
        private String fromUser = "java-mrcp-client";
        private String toUser = "mrcp";

        private Builder() {
        }

        public Builder localHost(String localHost) {
            this.localHost = localHost;
            return this;
        }

        public Builder localSipPort(int localSipPort) {
            this.localSipPort = localSipPort;
            return this;
        }

        public Builder localRtpPort(int localRtpPort) {
            this.localRtpPort = localRtpPort;
            return this;
        }

        public Builder serverHost(String serverHost) {
            this.serverHost = serverHost;
            return this;
        }

        public Builder serverSipPort(int serverSipPort) {
            this.serverSipPort = serverSipPort;
            return this;
        }

        public Builder fromUser(String fromUser) {
            this.fromUser = fromUser;
            return this;
        }

        public Builder toUser(String toUser) {
            this.toUser = toUser;
            return this;
        }

        public MrcpClientConfig build() {
            return new MrcpClientConfig(
                    localHost,
                    localSipPort,
                    localRtpPort,
                    serverHost,
                    serverSipPort,
                    fromUser,
                    toUser);
        }
    }
}
