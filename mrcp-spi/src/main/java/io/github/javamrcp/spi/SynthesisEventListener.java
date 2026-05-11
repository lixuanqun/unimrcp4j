package io.github.javamrcp.spi;

/**
 * Callback surface used by streaming TTS providers.
 */
public interface SynthesisEventListener {
    void onAudio(AudioChunk audioChunk);

    void onComplete();

    void onError(Throwable error);
}
