package io.github.javamrcp.sip;

import java.util.Objects;
import java.util.Optional;

/**
 * SIP dialog identifier derived from Call-ID and From/To tags.
 */
public record SipDialogId(String callId, String localTag, String remoteTag) {
    public SipDialogId {
        Objects.requireNonNull(callId, "callId");
        Objects.requireNonNull(localTag, "localTag");
        Objects.requireNonNull(remoteTag, "remoteTag");
        if (callId.isBlank() || localTag.isBlank() || remoteTag.isBlank()) {
            throw new IllegalArgumentException("dialog id fields must not be blank");
        }
    }

    public static Optional<SipDialogId> fromIncomingRequest(SipMessage request, String localTag) {
        Optional<String> callId = request.firstHeaderValue(SipMessage.CALL_ID);
        Optional<String> from = request.firstHeaderValue(SipMessage.FROM);
        if (callId.isEmpty() || from.isEmpty() || localTag == null || localTag.isBlank()) {
            return Optional.empty();
        }
        return SipHeaderValues.parameter(from.get(), "tag")
                .map(remoteTag -> new SipDialogId(callId.get(), localTag, remoteTag));
    }
}
