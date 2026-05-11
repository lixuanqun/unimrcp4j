package io.github.javamrcp.provider.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpSessionId;
import io.github.javamrcp.spi.AudioChunk;
import io.github.javamrcp.spi.AudioFormat;
import io.github.javamrcp.spi.RecognitionEventListener;
import io.github.javamrcp.spi.RecognitionRequest;
import io.github.javamrcp.spi.RecognitionResult;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class MockSpeechRecognizerProviderTest {
    @Test
    void emitsPartialAndFinalResults() {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.LINEAR_PCM, 16000, 16, 1);
        List<RecognitionResult> partials = new ArrayList<>();
        List<RecognitionResult> finals = new ArrayList<>();
        MockSpeechRecognizerProvider provider = new MockSpeechRecognizerProvider("你好", 0.9);

        var session = provider.startRecognition(
                new RecognitionRequest(new MrcpSessionId("s1"), Locale.CHINA, format),
                new RecognitionEventListener() {
                    @Override
                    public void onPartialResult(RecognitionResult result) {
                        partials.add(result);
                    }

                    @Override
                    public void onFinalResult(RecognitionResult result) {
                        finals.add(result);
                    }

                    @Override
                    public void onError(Throwable error) {
                        throw new AssertionError(error);
                    }
                });

        session.accept(new AudioChunk(ByteBuffer.wrap(new byte[] {1, 2}), format, 0, false)).toCompletableFuture().join();
        session.accept(AudioChunk.endOfStream(format)).toCompletableFuture().join();

        assertEquals("你好", partials.getFirst().text());
        assertEquals("你好", finals.getFirst().text());
        assertTrue(finals.getFirst().finalResult());
    }
}
