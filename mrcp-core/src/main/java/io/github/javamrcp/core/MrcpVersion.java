package io.github.javamrcp.core;

/**
 * Supported MRCP protocol versions.
 */
public enum MrcpVersion {
    MRCP_2_0("MRCP/2.0");

    private final String wireValue;

    MrcpVersion(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
