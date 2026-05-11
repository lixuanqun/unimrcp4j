package io.github.javamrcp.server;

/**
 * Resource channel state used by the initial MRCP state machines.
 */
public enum MrcpResourceChannelState {
    IDLE,
    RECOGNIZING,
    SPEAKING,
    PAUSED,
    COMPLETED,
    FAILED
}
