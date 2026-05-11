package io.github.javamrcp.core;

/**
 * Common contract for MRCP/2.0 request, response, and event start lines.
 */
public sealed interface MrcpStartLine permits MrcpRequestLine, MrcpResponseLine, MrcpEventLine {
    MrcpVersion version();

    int messageLength();

    MrcpMessageType messageType();
}
