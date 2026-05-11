package io.github.javamrcp.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.javamrcp.server.MrcpServerConfig;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class MrcpServerPropertiesTest {
    @Test
    void mapsPropertiesToServerConfig() {
        MrcpServerProperties properties = new MrcpServerProperties();
        properties.setSipHost("127.0.0.1");
        properties.setSipPort(8061);
        properties.setMrcpHost("192.0.2.20");
        properties.setMrcpPort(15440);
        properties.setMaxConcurrentSessions(800);
        properties.setShutdownQuietPeriod(Duration.ZERO);
        properties.setShutdownTimeout(Duration.ofSeconds(1));

        MrcpServerConfig config = properties.toConfig();

        assertEquals("127.0.0.1", config.sipHost());
        assertEquals(8061, config.sipPort());
        assertEquals("192.0.2.20", config.mrcpHost());
        assertEquals(15440, config.mrcpPort());
        assertEquals(800, config.maxConcurrentSessions());
        assertEquals(Duration.ZERO, config.shutdownQuietPeriod());
        assertEquals(Duration.ofSeconds(1), config.shutdownTimeout());
    }
}
