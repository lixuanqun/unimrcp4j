package io.github.javamrcp.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MrcpChannelIdentifierTest {
    @Test
    void parsesSessionAndResource() {
        MrcpChannelIdentifier identifier = MrcpChannelIdentifier.parse("32AECB23433801@speechrecog");

        assertEquals("32AECB23433801", identifier.sessionId());
        assertEquals("speechrecog", identifier.resourceName());
        assertEquals("32AECB23433801@speechrecog", identifier.wireValue());
        assertEquals(MrcpResourceType.SPEECH_RECOGNIZER, identifier.resourceType().orElseThrow());
    }

    @Test
    void rejectsMalformedIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> MrcpChannelIdentifier.parse("missing-resource@"));
        assertThrows(IllegalArgumentException.class, () -> MrcpChannelIdentifier.parse("@missing-session"));
        assertThrows(IllegalArgumentException.class, () -> MrcpChannelIdentifier.parse("missing-separator"));
    }
}
