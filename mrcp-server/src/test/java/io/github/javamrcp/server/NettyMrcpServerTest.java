package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.server.netty.NettyMrcpServer;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class NettyMrcpServerTest {
    @Test
    void startsAndStopsOnEphemeralPorts() {
        MrcpServerConfig config = MrcpServerConfig.builder()
                .sipHost("127.0.0.1")
                .sipPort(0)
                .mrcpHost("127.0.0.1")
                .mrcpPort(0)
                .shutdownQuietPeriod(Duration.ZERO)
                .shutdownTimeout(Duration.ofSeconds(2))
                .build();
        NettyMrcpServer server = new NettyMrcpServer(config);

        server.start().toCompletableFuture().join();
        assertTrue(server.isRunning());

        server.stop().toCompletableFuture().join();
        assertFalse(server.isRunning());
    }
}
