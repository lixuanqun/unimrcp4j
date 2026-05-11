package io.github.javamrcp.client;

/**
 * Client-side MRCP session state.
 */
public enum MrcpClientState {
    NEW,
    INVITE_CREATED,
    ANSWER_RECEIVED,
    ESTABLISHED,
    TERMINATED
}
