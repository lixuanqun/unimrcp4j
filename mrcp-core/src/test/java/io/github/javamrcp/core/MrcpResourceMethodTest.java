package io.github.javamrcp.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MrcpResourceMethodTest {
    @Test
    void parsesRecognizerMethods() {
        assertEquals(MrcpRecognizerMethod.RECOGNIZE,
                MrcpRecognizerMethod.fromWireValue("recognize").orElseThrow());
        assertEquals(MrcpRecognizerMethod.START_INPUT_TIMERS,
                MrcpRecognizerMethod.fromWireValue("START-INPUT-TIMERS").orElseThrow());
        assertTrue(MrcpRecognizerMethod.fromWireValue("SPEAK").isEmpty());
    }

    @Test
    void parsesSynthesizerMethods() {
        assertEquals(MrcpSynthesizerMethod.SPEAK,
                MrcpSynthesizerMethod.fromWireValue("speak").orElseThrow());
        assertEquals(MrcpSynthesizerMethod.BARGE_IN_OCCURRED,
                MrcpSynthesizerMethod.fromWireValue("BARGE-IN-OCCURRED").orElseThrow());
        assertTrue(MrcpSynthesizerMethod.fromWireValue("RECOGNIZE").isEmpty());
    }
}
