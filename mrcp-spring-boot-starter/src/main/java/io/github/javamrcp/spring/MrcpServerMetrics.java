package io.github.javamrcp.spring;

import io.github.javamrcp.server.MrcpServer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Registers basic MRCP server gauges.
 */
public final class MrcpServerMetrics {
    public MrcpServerMetrics(MeterRegistry registry, MrcpServer server) {
        Gauge.builder("mrcp.server.running", server, value -> value.isRunning() ? 1.0d : 0.0d)
                .description("Whether the embedded MRCP server is running")
                .register(registry);
    }
}
