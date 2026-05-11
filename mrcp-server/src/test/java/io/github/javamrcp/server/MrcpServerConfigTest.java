package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class MrcpServerConfigTest {
    @Test
    void providesServerDefaults() {
        MrcpServerConfig config = MrcpServerConfig.defaults();

        assertEquals("127.0.0.1", config.advertisedHost());
        assertEquals("0.0.0.0", config.sipHost());
        assertEquals(8060, config.sipPort());
        assertEquals("0.0.0.0", config.mrcpHost());
        assertEquals(1544, config.mrcpPort());
        assertEquals(4000, config.rtpPort());
        assertEquals(1_000, config.maxConcurrentSessions());
    }

    @Test
    void rejectsInvalidPorts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MrcpServerConfig.builder().sipPort(65_536).build());
    }

    @Test
    void appliesBuilderOverrides() {
        MrcpServerConfig config = MrcpServerConfig.builder()
                .advertisedHost("203.0.113.10")
                .sipHost("127.0.0.1")
                .sipPort(0)
                .mrcpHost("192.0.2.10")
                .mrcpPort(15440)
                .rtpPort(40000)
                .maxConcurrentSessions(800)
                .shutdownQuietPeriod(Duration.ZERO)
                .shutdownTimeout(Duration.ofSeconds(1))
                .build();

        assertEquals("203.0.113.10", config.advertisedHost());
        assertEquals("127.0.0.1", config.sipHost());
        assertEquals(0, config.sipPort());
        assertEquals("192.0.2.10", config.mrcpHost());
        assertEquals(15440, config.mrcpPort());
        assertEquals(40000, config.rtpPort());
        assertEquals(800, config.maxConcurrentSessions());
        assertEquals(Duration.ZERO, config.shutdownQuietPeriod());
        assertEquals(Duration.ofSeconds(1), config.shutdownTimeout());
    }

    @Test
    void rejectsInvalidConcurrencyAndShutdownTimeout() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MrcpServerConfig.builder().maxConcurrentSessions(0).build());
        assertThrows(
                IllegalArgumentException.class,
                () -> MrcpServerConfig.builder().advertisedHost(" ").build());
        assertThrows(
                IllegalArgumentException.class,
                () -> MrcpServerConfig.builder().rtpPort(65_536).build());
        assertThrows(
                IllegalArgumentException.class,
                () -> MrcpServerConfig.builder().shutdownTimeout(Duration.ZERO).build());
        assertThrows(
                IllegalArgumentException.class,
                () -> MrcpServerConfig.builder().shutdownQuietPeriod(Duration.ofMillis(-1)).build());
    }
}
