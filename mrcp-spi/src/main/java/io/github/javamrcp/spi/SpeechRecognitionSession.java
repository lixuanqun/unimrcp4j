package io.github.javamrcp.spi;

import java.util.concurrent.CompletionStage;

/**
 * Streaming ASR session owned by a single MRCP recognition request.
 */
public interface SpeechRecognitionSession extends AutoCloseable {
    CompletionStage<Void> accept(AudioChunk audioChunk);

    CompletionStage<Void> stop();

    @Override
    void close();
}
