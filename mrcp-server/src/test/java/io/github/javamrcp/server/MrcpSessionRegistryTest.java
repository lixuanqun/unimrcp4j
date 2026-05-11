package io.github.javamrcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpSessionId;
import io.github.javamrcp.sdp.SdpOrigin;
import io.github.javamrcp.sdp.SdpSession;
import java.util.List;
import org.junit.jupiter.api.Test;

class MrcpSessionRegistryTest {
    @Test
    void registersFindsAndRemovesSessions() {
        MrcpSessionRegistry registry = new MrcpSessionRegistry(2);
        MrcpServerSession session = session("s1", "call-1");

        assertSame(session, registry.register(session));
        assertEquals(1, registry.size());
        assertSame(session, registry.find(new MrcpSessionId("s1")).orElseThrow());
        assertSame(session, registry.findByCallId("call-1").orElseThrow());

        assertSame(session, registry.remove(new MrcpSessionId("s1")).orElseThrow());
        assertEquals(MrcpSessionState.TERMINATED, session.state());
        assertTrue(registry.findByCallId("call-1").isEmpty());
    }

    @Test
    void enforcesMaxSessions() {
        MrcpSessionRegistry registry = new MrcpSessionRegistry(1);
        registry.register(session("s1", "call-1"));

        assertThrows(MrcpSessionLimitExceededException.class, () -> registry.register(session("s2", "call-2")));
    }

    @Test
    void duplicateRegistrationReturnsExistingSession() {
        MrcpSessionRegistry registry = new MrcpSessionRegistry(1);
        MrcpServerSession first = session("s1", "call-1");
        MrcpServerSession duplicate = session("s1", "call-duplicate");

        assertSame(first, registry.register(first));
        assertSame(first, registry.register(duplicate));
        assertEquals(1, registry.size());
    }

    private MrcpServerSession session(String id, String callId) {
        SdpSession offer = SdpSession.builder(SdpOrigin.ip4("-", 1, 1, "127.0.0.1")).build();
        return new MrcpServerSession(new MrcpSessionId(id), callId, offer, List.of());
    }
}
