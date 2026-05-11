package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpMessage;
import java.util.List;

/**
 * Resource-specific MRCP request state machine.
 */
public interface MrcpResourceStateMachine {
    MrcpResourceChannelState state();

    List<MrcpMessage> onRequest(MrcpMessage request);
}
