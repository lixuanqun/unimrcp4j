package io.github.javamrcp.sip;

import java.util.Objects;
import java.util.Optional;

/**
 * Server transaction key for SIP requests received over UDP.
 */
public record SipServerTransactionId(String branch, SipMethod method) {
    public SipServerTransactionId {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(method, "method");
        if (branch.isBlank()) {
            throw new IllegalArgumentException("branch must not be blank");
        }
    }

    public static Optional<SipServerTransactionId> fromRequest(SipMessage request) {
        if (!(request.startLine() instanceof SipRequestLine requestLine)) {
            return Optional.empty();
        }
        Optional<String> branch = request.firstHeaderValue(SipMessage.VIA)
                .flatMap(value -> SipHeaderValues.parameter(value, "branch"));
        return branch.map(value -> new SipServerTransactionId(value, requestLine.method()));
    }
}
