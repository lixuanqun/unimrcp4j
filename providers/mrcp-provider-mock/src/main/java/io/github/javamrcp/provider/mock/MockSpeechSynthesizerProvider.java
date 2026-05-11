package io.github.javamrcp.provider.mock;

import io.github.javamrcp.spi.AudioChunk;
import io.github.javamrcp.spi.SpeechSynthesisSession;
import io.github.javamrcp.spi.SpeechSynthesizerProvider;
import io.github.javamrcp.spi.SynthesisEventListener;
import io.github.javamrcp.spi.SynthesisRequest;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Deterministic synthesizer that emits a fixed-size silent audio chunk.
 */
public final class MockSpeechSynthesizerProvider implements SpeechSynthesizerProvider {
    private final int audioBytes;

    public MockSpeechSynthesizerProvider() {
        this(320);
    }

    public MockSpeechSynthesizerProvider(int audioBytes) {
        if (audioBytes < 0) {
            throw new IllegalArgumentException("audioBytes must not be negative");
        }
        this.audioBytes = audioBytes;
    }

    @Override
    public SpeechSynthesisSession startSynthesis(
            SynthesisRequest request,
            SynthesisEventListener listener) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(listener, "listener");
        if (audioBytes > 0) {
            listener.onAudio(new AudioChunk(
                    ByteBuffer.wrap(new byte[audioBytes]),
                    request.outputFormat(),
                    System.currentTimeMillis(),
                    false));
        }
        listener.onComplete();
        return new Session();
    }

    private static final class Session implements SpeechSynthesisSession {
        private final AtomicBoolean stopped = new AtomicBoolean();

        @Override
        public CompletionStage<Void> stop() {
            stopped.set(true);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void close() {
            stopped.set(true);
        }
    }
}
