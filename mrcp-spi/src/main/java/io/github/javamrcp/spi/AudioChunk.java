package io.github.javamrcp.spi;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Immutable audio frame sent to or returned by provider implementations.
 */
public record AudioChunk(ByteBuffer payload, AudioFormat format, long timestampMillis, boolean endOfStream) {
    public AudioChunk {
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(format, "format");
        payload = payload.asReadOnlyBuffer();
    }

    public static AudioChunk endOfStream(AudioFormat format) {
        return new AudioChunk(ByteBuffer.allocate(0), format, System.currentTimeMillis(), true);
    }
}
