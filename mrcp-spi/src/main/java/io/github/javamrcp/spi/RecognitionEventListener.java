package io.github.javamrcp.spi;

/**
 * Callback surface used by streaming ASR providers.
 */
public interface RecognitionEventListener {
    void onPartialResult(RecognitionResult result);

    void onFinalResult(RecognitionResult result);

    void onError(Throwable error);
}
