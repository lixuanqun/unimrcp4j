package io.github.javamrcp.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MrcpResourceTypeTest {
    @Test
    void parsesKnownWireValuesCaseInsensitively() {
        assertEquals(
                MrcpResourceType.SPEECH_RECOGNIZER,
                MrcpResourceType.fromWireValue("SpeechRecog").orElseThrow());
        assertEquals(
                MrcpResourceType.SPEECH_SYNTHESIZER,
                MrcpResourceType.fromWireValue("speechsynth").orElseThrow());
    }

    @Test
    void ignoresUnknownResourceTypes() {
        assertTrue(MrcpResourceType.fromWireValue("recorder").isEmpty());
    }
}
