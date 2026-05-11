package io.github.javamrcp.core;

/**
 * MRCP events produced by recognizer and synthesizer resources.
 */
public enum MrcpResourceEvent {
    START_OF_INPUT("START-OF-INPUT"),
    RECOGNITION_COMPLETE("RECOGNITION-COMPLETE"),
    SPEAK_COMPLETE("SPEAK-COMPLETE");

    private final String wireValue;

    MrcpResourceEvent(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
