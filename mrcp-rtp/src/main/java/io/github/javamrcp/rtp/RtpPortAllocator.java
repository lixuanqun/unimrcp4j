package io.github.javamrcp.rtp;

import java.util.BitSet;

/**
 * Allocates even RTP ports from a configured inclusive range.
 */
public final class RtpPortAllocator {
    private final int startPort;
    private final int endPort;
    private final BitSet allocated;

    public RtpPortAllocator(int startPort, int endPort) {
        if (startPort < 0 || endPort > 65_535 || startPort > endPort) {
            throw new IllegalArgumentException("invalid RTP port range");
        }
        this.startPort = startPort % 2 == 0 ? startPort : startPort + 1;
        this.endPort = endPort;
        this.allocated = new BitSet(Math.max(0, endPort - this.startPort + 1));
    }

    public synchronized int allocate() {
        for (int port = startPort; port <= endPort; port += 2) {
            int index = port - startPort;
            if (!allocated.get(index)) {
                allocated.set(index);
                return port;
            }
        }
        throw new IllegalStateException("No RTP ports available in range " + startPort + "-" + endPort);
    }

    public synchronized void release(int port) {
        if (port < startPort || port > endPort || port % 2 != 0) {
            return;
        }
        allocated.clear(port - startPort);
    }

    public synchronized int allocatedCount() {
        return allocated.cardinality();
    }
}
