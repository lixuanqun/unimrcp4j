package io.github.javamrcp.spring;

import io.github.javamrcp.server.MrcpServerConfig;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot properties for the embedded Netty MRCP server.
 */
@ConfigurationProperties(prefix = "mrcp.server")
public class MrcpServerProperties {
    private boolean enabled = true;
    private String advertisedHost = "127.0.0.1";
    private String sipHost = "0.0.0.0";
    private int sipPort = 8060;
    private String mrcpHost = "0.0.0.0";
    private int mrcpPort = 1544;
    private int rtpPort = 4000;
    private int maxConcurrentSessions = 1_000;
    private Duration shutdownQuietPeriod = Duration.ofMillis(100);
    private Duration shutdownTimeout = Duration.ofSeconds(5);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAdvertisedHost() {
        return advertisedHost;
    }

    public void setAdvertisedHost(String advertisedHost) {
        this.advertisedHost = advertisedHost;
    }

    public String getSipHost() {
        return sipHost;
    }

    public void setSipHost(String sipHost) {
        this.sipHost = sipHost;
    }

    public int getSipPort() {
        return sipPort;
    }

    public void setSipPort(int sipPort) {
        this.sipPort = sipPort;
    }

    public String getMrcpHost() {
        return mrcpHost;
    }

    public void setMrcpHost(String mrcpHost) {
        this.mrcpHost = mrcpHost;
    }

    public int getMrcpPort() {
        return mrcpPort;
    }

    public void setMrcpPort(int mrcpPort) {
        this.mrcpPort = mrcpPort;
    }

    public int getRtpPort() {
        return rtpPort;
    }

    public void setRtpPort(int rtpPort) {
        this.rtpPort = rtpPort;
    }

    public int getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(int maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    public Duration getShutdownQuietPeriod() {
        return shutdownQuietPeriod;
    }

    public void setShutdownQuietPeriod(Duration shutdownQuietPeriod) {
        this.shutdownQuietPeriod = shutdownQuietPeriod;
    }

    public Duration getShutdownTimeout() {
        return shutdownTimeout;
    }

    public void setShutdownTimeout(Duration shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    MrcpServerConfig toConfig() {
        return MrcpServerConfig.builder()
                .advertisedHost(advertisedHost)
                .sipHost(sipHost)
                .sipPort(sipPort)
                .mrcpHost(mrcpHost)
                .mrcpPort(mrcpPort)
                .rtpPort(rtpPort)
                .maxConcurrentSessions(maxConcurrentSessions)
                .shutdownQuietPeriod(shutdownQuietPeriod)
                .shutdownTimeout(shutdownTimeout)
                .build();
    }
}
