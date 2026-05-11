package io.github.javamrcp.spring;

import io.github.javamrcp.server.MrcpServer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Actuator health indicator for the embedded MRCP server.
 */
public final class MrcpServerHealthIndicator implements HealthIndicator {
    private final MrcpServer server;

    public MrcpServerHealthIndicator(MrcpServer server) {
        this.server = server;
    }

    @Override
    public Health health() {
        return server.isRunning()
                ? Health.up().withDetail("mrcpServer", "running").build()
                : Health.down().withDetail("mrcpServer", "stopped").build();
    }
}
