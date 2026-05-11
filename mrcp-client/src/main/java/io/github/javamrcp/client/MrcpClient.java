package io.github.javamrcp.client;

import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.sip.SipMessage;

/**
 * MRCPv2 client SDK entry point.
 */
public interface MrcpClient {
    ClientInvite createInvite(MrcpResourceType resourceType);

    record ClientInvite(MrcpClientSession session, SipMessage sipInvite) {
    }
}
