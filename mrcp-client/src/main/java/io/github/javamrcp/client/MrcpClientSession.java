package io.github.javamrcp.client;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.sdp.SdpSession;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client-side session generated for a SIP INVITE offer.
 */
public final class MrcpClientSession {
    private final String callId;
    private final MrcpChannelIdentifier channelIdentifier;
    private final MrcpResourceType resourceType;
    private final SdpSession localOffer;
    private final AtomicReference<MrcpClientState> state = new AtomicReference<>(MrcpClientState.INVITE_CREATED);
    private volatile SdpSession remoteAnswer;

    public MrcpClientSession(
            String callId,
            MrcpChannelIdentifier channelIdentifier,
            MrcpResourceType resourceType,
            SdpSession localOffer) {
        this.callId = Objects.requireNonNull(callId, "callId");
        this.channelIdentifier = Objects.requireNonNull(channelIdentifier, "channelIdentifier");
        this.resourceType = Objects.requireNonNull(resourceType, "resourceType");
        this.localOffer = Objects.requireNonNull(localOffer, "localOffer");
        if (callId.isBlank()) {
            throw new IllegalArgumentException("callId must not be blank");
        }
    }

    public String callId() {
        return callId;
    }

    public MrcpChannelIdentifier channelIdentifier() {
        return channelIdentifier;
    }

    public MrcpResourceType resourceType() {
        return resourceType;
    }

    public SdpSession localOffer() {
        return localOffer;
    }

    public MrcpClientState state() {
        return state.get();
    }

    public Optional<SdpSession> remoteAnswer() {
        return Optional.ofNullable(remoteAnswer);
    }

    public void answerReceived(SdpSession answer) {
        remoteAnswer = Objects.requireNonNull(answer, "answer");
        state.set(MrcpClientState.ANSWER_RECEIVED);
    }

    public void established() {
        state.set(MrcpClientState.ESTABLISHED);
    }

    public void terminated() {
        state.set(MrcpClientState.TERMINATED);
    }
}
