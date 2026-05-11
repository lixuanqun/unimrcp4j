package io.github.javamrcp.server;

/**
 * Server-side MRCP session lifecycle.
 */
public enum MrcpSessionState {
    OFFER_RECEIVED,
    ANSWER_SENT,
    ESTABLISHED,
    TERMINATING,
    TERMINATED
}
