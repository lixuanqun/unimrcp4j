package io.github.javamrcp.provider.mock;

import io.github.javamrcp.spi.AudioChunk;
import io.github.javamrcp.spi.RecognitionEventListener;
import io.github.javamrcp.spi.RecognitionRequest;
import io.github.javamrcp.spi.RecognitionResult;
import io.github.javamrcp.spi.SpeechRecognitionSession;
import io.github.javamrcp.spi.SpeechRecognizerProvider;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Deterministic recognizer used for protocol and integration tests.
 */
public final class MockSpeechRecognizerProvider implements SpeechRecognizerProvider {
    private final String finalText;
    private final double confidence;

    public MockSpeechRecognizerProvider() {
        this("mock recognition result", 1.0d);
    }

    public MockSpeechRecognizerProvider(String finalText, double confidence) {
        this.finalText = Objects.requireNonNull(finalText, "finalText");
        this.confidence = confidence;
    }

    @Override
    public SpeechRecognitionSession startRecognition(
            RecognitionRequest request,
            RecognitionEventListener listener) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(listener, "listener");
        return new Session(listener);
    }

    private final class Session implements SpeechRecognitionSession {
        private final RecognitionEventListener listener;
        private final AtomicBoolean partialEmitted = new AtomicBoolean();
        private final AtomicBoolean stopped = new AtomicBoolean();

        private Session(RecognitionEventListener listener) {
            this.listener = listener;
        }

        @Override
        public CompletionStage<Void> accept(AudioChunk audioChunk) {
            if (stopped.get()) {
                return CompletableFuture.completedFuture(null);
            }
            if (!audioChunk.endOfStream() && audioChunk.payload().remaining() > 0
                    && partialEmitted.compareAndSet(false, true)) {
                listener.onPartialResult(new RecognitionResult(finalText, false, confidence));
            }
            if (audioChunk.endOfStream()) {
                listener.onFinalResult(new RecognitionResult(finalText, true, confidence));
                stopped.set(true);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<Void> stop() {
            if (stopped.compareAndSet(false, true)) {
                listener.onFinalResult(new RecognitionResult(finalText, true, confidence));
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void close() {
            stopped.set(true);
        }
    }
}
