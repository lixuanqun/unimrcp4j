package io.github.javamrcp.spi;

/**
 * Normalized audio description passed between RTP adapters and ASR/TTS providers.
 */
public record AudioFormat(Encoding encoding, int sampleRateHz, int sampleSizeBits, int channels) {
    public AudioFormat {
        if (sampleRateHz <= 0) {
            throw new IllegalArgumentException("sampleRateHz must be positive");
        }
        if (sampleSizeBits <= 0) {
            throw new IllegalArgumentException("sampleSizeBits must be positive");
        }
        if (channels <= 0) {
            throw new IllegalArgumentException("channels must be positive");
        }
    }

    public enum Encoding {
        PCMU,
        PCMA,
        LINEAR_PCM
    }
}
