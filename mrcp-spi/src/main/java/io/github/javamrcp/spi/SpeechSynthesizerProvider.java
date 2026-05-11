package io.github.javamrcp.spi;

/**
 * TTS provider entry point.
 */
public interface SpeechSynthesizerProvider {
    SpeechSynthesisSession startSynthesis(
            SynthesisRequest request,
            SynthesisEventListener listener);
}
