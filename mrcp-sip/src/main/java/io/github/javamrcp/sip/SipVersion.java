package io.github.javamrcp.sip;

/**
 * Supported SIP protocol versions.
 */
public enum SipVersion {
    SIP_2_0("SIP/2.0");

    private final String wireValue;

    SipVersion(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
