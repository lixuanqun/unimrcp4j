package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MrcpServerConfigTest {
    @Test
    void providesServerDefaults() {
        MrcpServerConfig config = MrcpServerConfig.defaults();

        assertEquals("0.0.0.0", config.sipHost());
        assertEquals(8060, config.sipPort());
        assertEquals("0.0.0.0", config.mrcpHost());
        assertEquals(1544, config.mrcpPort());
        assertEquals(1_000, config.maxConcurrentSessions());
    }

    @Test
    void rejectsInvalidPorts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MrcpServerConfig.builder().sipPort(65_536).build());
    }
}
