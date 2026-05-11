package io.github.javamrcp.spi;

/**
 * ASR provider entry point. Implementations can wrap cloud SDKs, WebSocket streams,
 * gRPC services, or local models without leaking vendor APIs into MRCP modules.
 */
public interface SpeechRecognizerProvider {
    SpeechRecognitionSession startRecognition(
            RecognitionRequest request,
            RecognitionEventListener listener);
}
