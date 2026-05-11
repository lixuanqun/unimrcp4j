package io.github.javamrcp.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MrcpClientConfigTest {
    @Test
    void providesDefaultsAndOverrides() {
        MrcpClientConfig defaults = MrcpClientConfig.builder().build();
        assertEquals("127.0.0.1", defaults.localHost());
        assertEquals(8060, defaults.serverSipPort());

        MrcpClientConfig config = MrcpClientConfig.builder()
                .localHost("192.0.2.1")
                .localSipPort(5090)
                .localRtpPort(4100)
                .serverHost("192.0.2.2")
                .serverSipPort(8061)
                .fromUser("client")
                .toUser("server")
                .build();

        assertEquals("192.0.2.1", config.localHost());
        assertEquals(5090, config.localSipPort());
        assertEquals(4100, config.localRtpPort());
        assertEquals("192.0.2.2", config.serverHost());
        assertEquals(8061, config.serverSipPort());
        assertEquals("client", config.fromUser());
        assertEquals("server", config.toUser());
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> MrcpClientConfig.builder().localHost(" ").build());
        assertThrows(IllegalArgumentException.class, () -> MrcpClientConfig.builder().serverSipPort(70000).build());
    }
}
