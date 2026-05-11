package io.github.javamrcp.server;

import java.util.concurrent.CompletionStage;

/**
 * Lifecycle contract for MRCP server implementations.
 */
public interface MrcpServer {
    CompletionStage<Void> start();

    CompletionStage<Void> stop();

    boolean isRunning();
}
