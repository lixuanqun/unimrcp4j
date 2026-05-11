package io.github.javamrcp.spi;

import java.util.Objects;

/**
 * Recognition hypothesis emitted by an ASR provider.
 */
public record RecognitionResult(String text, boolean finalResult, double confidence) {
    public RecognitionResult {
        Objects.requireNonNull(text, "text");
        if (confidence < 0.0d || confidence > 1.0d) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
    }
}
