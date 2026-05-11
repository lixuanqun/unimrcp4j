package io.github.javamrcp.rtp;

import java.util.Optional;

/**
 * Audio payload types targeted by the initial FreeSWITCH interop path.
 */
public enum RtpAudioCodec {
    PCMU(0, "PCMU", 8000, 1),
    PCMA(8, "PCMA", 8000, 1),
    L16_DYNAMIC(96, "L16", 16000, 1);

    private final int payloadType;
    private final String encodingName;
    private final int clockRate;
    private final int channels;

    RtpAudioCodec(int payloadType, String encodingName, int clockRate, int channels) {
        this.payloadType = payloadType;
        this.encodingName = encodingName;
        this.clockRate = clockRate;
        this.channels = channels;
    }

    public int payloadType() {
        return payloadType;
    }

    public String encodingName() {
        return encodingName;
    }

    public int clockRate() {
        return clockRate;
    }

    public int channels() {
        return channels;
    }

    public String rtpMapValue() {
        return payloadType + " " + encodingName + "/" + clockRate + (channels == 1 ? "" : "/" + channels);
    }

    public static Optional<RtpAudioCodec> fromPayloadType(int payloadType) {
        for (RtpAudioCodec codec : values()) {
            if (codec.payloadType == payloadType) {
                return Optional.of(codec);
            }
        }
        return Optional.empty();
    }
}
