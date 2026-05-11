package io.github.javamrcp.spi;

import io.github.javamrcp.core.MrcpSessionId;
import java.util.Locale;
import java.util.Objects;

/**
 * Provider-neutral TTS request derived from an MRCP SPEAK command.
 */
public record SynthesisRequest(
        MrcpSessionId sessionId,
        Locale locale,
        String text,
        AudioFormat outputFormat) {
    public SynthesisRequest {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(outputFormat, "outputFormat");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
    }
}
