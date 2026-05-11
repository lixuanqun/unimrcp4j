package io.github.javamrcp.provider.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpSessionId;
import io.github.javamrcp.spi.AudioChunk;
import io.github.javamrcp.spi.AudioFormat;
import io.github.javamrcp.spi.SynthesisEventListener;
import io.github.javamrcp.spi.SynthesisRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class MockSpeechSynthesizerProviderTest {
    @Test
    void emitsAudioAndComplete() {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.LINEAR_PCM, 16000, 16, 1);
        List<AudioChunk> chunks = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean();

        new MockSpeechSynthesizerProvider(160).startSynthesis(
                new SynthesisRequest(new MrcpSessionId("s1"), Locale.CHINA, "你好", format),
                new SynthesisEventListener() {
                    @Override
                    public void onAudio(AudioChunk audioChunk) {
                        chunks.add(audioChunk);
                    }

                    @Override
                    public void onComplete() {
                        completed.set(true);
                    }

                    @Override
                    public void onError(Throwable error) {
                        throw new AssertionError(error);
                    }
                });

        assertEquals(160, chunks.getFirst().payload().remaining());
        assertTrue(completed.get());
    }
}
