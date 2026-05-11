package io.github.javamrcp.spi;

import io.github.javamrcp.core.MrcpSessionId;
import java.util.Locale;
import java.util.Objects;

/**
 * Provider-neutral ASR request derived from an MRCP RECOGNIZE command.
 */
public record RecognitionRequest(MrcpSessionId sessionId, Locale locale, AudioFormat inputFormat) {
    public RecognitionRequest {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(inputFormat, "inputFormat");
    }
}
