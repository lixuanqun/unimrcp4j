package io.github.javamrcp.spi;

import java.util.concurrent.CompletionStage;

/**
 * Streaming TTS session owned by a single MRCP speak request.
 */
public interface SpeechSynthesisSession extends AutoCloseable {
    CompletionStage<Void> stop();

    @Override
    void close();
}
