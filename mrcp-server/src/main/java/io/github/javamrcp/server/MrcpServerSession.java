package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.core.MrcpSessionId;
import io.github.javamrcp.sdp.SdpSession;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Server-side MRCP session created from a SIP/SDP offer.
 */
public final class MrcpServerSession {
    private final MrcpSessionId id;
    private final String callId;
    private final SdpSession remoteOffer;
    private final List<MrcpResourceChannel> resourceChannels;
    private final Instant createdAt;
    private final AtomicReference<MrcpSessionState> state;
    private volatile SdpSession localAnswer;

    public MrcpServerSession(
            MrcpSessionId id,
            String callId,
            SdpSession remoteOffer,
            List<MrcpResourceChannel> resourceChannels) {
        this.id = Objects.requireNonNull(id, "id");
        this.callId = Objects.requireNonNull(callId, "callId");
        this.remoteOffer = Objects.requireNonNull(remoteOffer, "remoteOffer");
        this.resourceChannels = List.copyOf(Objects.requireNonNull(resourceChannels, "resourceChannels"));
        if (callId.isBlank()) {
            throw new IllegalArgumentException("callId must not be blank");
        }
        this.createdAt = Instant.now();
        this.state = new AtomicReference<>(MrcpSessionState.OFFER_RECEIVED);
    }

    public MrcpSessionId id() {
        return id;
    }

    public String callId() {
        return callId;
    }

    public SdpSession remoteOffer() {
        return remoteOffer;
    }

    public List<MrcpResourceChannel> resourceChannels() {
        return resourceChannels;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public MrcpSessionState state() {
        return state.get();
    }

    public Optional<SdpSession> localAnswer() {
        return Optional.ofNullable(localAnswer);
    }

    public Optional<MrcpResourceChannel> firstChannel(MrcpResourceType resourceType) {
        return resourceChannels.stream()
                .filter(channel -> channel.resourceType() == resourceType)
                .findFirst();
    }

    public void answerSent(SdpSession answer) {
        Objects.requireNonNull(answer, "answer");
        localAnswer = answer;
        state.set(MrcpSessionState.ANSWER_SENT);
    }

    public void established() {
        state.set(MrcpSessionState.ESTABLISHED);
    }

    public void terminating() {
        state.set(MrcpSessionState.TERMINATING);
    }

    public void terminated() {
        state.set(MrcpSessionState.TERMINATED);
    }
}
