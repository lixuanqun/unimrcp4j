package io.github.javamrcp.spring;

import io.github.javamrcp.server.MrcpServer;
import org.springframework.context.SmartLifecycle;

/**
 * Synchronously bridges the asynchronous MRCP server lifecycle into Spring Boot.
 */
public final class MrcpServerLifecycle implements SmartLifecycle {
    private final MrcpServer server;
    private volatile boolean running;

    public MrcpServerLifecycle(MrcpServer server) {
        this.server = server;
    }

    @Override
    public void start() {
        server.start().toCompletableFuture().join();
        running = true;
    }

    @Override
    public void stop() {
        server.stop().toCompletableFuture().join();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running && server.isRunning();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }
}
