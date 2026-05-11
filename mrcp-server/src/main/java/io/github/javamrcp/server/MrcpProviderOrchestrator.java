package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpRecognizerMethod;
import io.github.javamrcp.core.MrcpSessionId;
import io.github.javamrcp.core.MrcpSynthesizerMethod;
import io.github.javamrcp.spi.AudioFormat;
import io.github.javamrcp.spi.RecognitionEventListener;
import io.github.javamrcp.spi.RecognitionRequest;
import io.github.javamrcp.spi.RecognitionResult;
import io.github.javamrcp.spi.SpeechRecognizerProvider;
import io.github.javamrcp.spi.SpeechSynthesizerProvider;
import io.github.javamrcp.spi.SynthesisEventListener;
import io.github.javamrcp.spi.SynthesisRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bridges provider callbacks into MRCP completion events.
 */
public final class MrcpProviderOrchestrator {
    private final SpeechRecognizerProvider recognizerProvider;
    private final SpeechSynthesizerProvider synthesizerProvider;
    private final MrcpProviderEventFactory eventFactory;

    public MrcpProviderOrchestrator(
            SpeechRecognizerProvider recognizerProvider,
            SpeechSynthesizerProvider synthesizerProvider) {
        this(recognizerProvider, synthesizerProvider, new MrcpProviderEventFactory());
    }

    MrcpProviderOrchestrator(
            SpeechRecognizerProvider recognizerProvider,
            SpeechSynthesizerProvider synthesizerProvider,
            MrcpProviderEventFactory eventFactory) {
        this.recognizerProvider = recognizerProvider;
        this.synthesizerProvider = synthesizerProvider;
        this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory");
    }

    public List<MrcpMessage> maybeComplete(MrcpMessage request, MrcpResourceChannel channel) {
        if (!(request.startLine() instanceof MrcpRequestLine requestLine)) {
            return List.of();
        }
        if (channel.resourceType().wireValue().equals("speechrecog")
                && MrcpRecognizerMethod.fromWireValue(requestLine.methodName()).orElse(null)
                == MrcpRecognizerMethod.RECOGNIZE) {
            return recognize(request, channel);
        }
        if (channel.resourceType().wireValue().equals("speechsynth")
                && MrcpSynthesizerMethod.fromWireValue(requestLine.methodName()).orElse(null)
                == MrcpSynthesizerMethod.SPEAK) {
            return synthesize(request, channel);
        }
        return List.of();
    }

    private List<MrcpMessage> recognize(MrcpMessage request, MrcpResourceChannel channel) {
        if (recognizerProvider == null) {
            return List.of();
        }
        AtomicReference<RecognitionResult> finalResult = new AtomicReference<>();
        RecognitionRequest recognitionRequest = new RecognitionRequest(
                new MrcpSessionId(channel.channelIdentifier().sessionId()),
                Locale.CHINA,
                defaultAudioFormat());
        var recognitionSession = recognizerProvider.startRecognition(recognitionRequest, new RecognitionEventListener() {
            @Override
            public void onPartialResult(RecognitionResult result) {
                // Partial results will be surfaced in a later MRCP event streaming milestone.
            }

            @Override
            public void onFinalResult(RecognitionResult result) {
                finalResult.set(result);
            }

            @Override
            public void onError(Throwable error) {
                throw new IllegalStateException("Recognition provider failed", error);
            }
        });
        recognitionSession.stop().toCompletableFuture().join();
        return Optional.ofNullable(finalResult.get())
                .map(result -> List.of(eventFactory.recognitionComplete(request, result)))
                .orElseGet(List::of);
    }

    private List<MrcpMessage> synthesize(MrcpMessage request, MrcpResourceChannel channel) {
        if (synthesizerProvider == null) {
            return List.of();
        }
        List<MrcpMessage> events = new ArrayList<>();
        SynthesisRequest synthesisRequest = new SynthesisRequest(
                new MrcpSessionId(channel.channelIdentifier().sessionId()),
                Locale.CHINA,
                request.bodyAsString().isBlank() ? "mock synthesis" : request.bodyAsString(),
                defaultAudioFormat());
        synthesizerProvider.startSynthesis(synthesisRequest, new SynthesisEventListener() {
            @Override
            public void onAudio(io.github.javamrcp.spi.AudioChunk audioChunk) {
                // RTP transmission is handled by the media pipeline; this milestone only maps completion.
            }

            @Override
            public void onComplete() {
                events.add(eventFactory.speakComplete(request));
            }

            @Override
            public void onError(Throwable error) {
                throw new IllegalStateException("Synthesis provider failed", error);
            }
        });
        return events;
    }

    private AudioFormat defaultAudioFormat() {
        return new AudioFormat(AudioFormat.Encoding.LINEAR_PCM, 16000, 16, 1);
    }
}
