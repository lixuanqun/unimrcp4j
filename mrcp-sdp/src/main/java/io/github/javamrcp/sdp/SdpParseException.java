package io.github.javamrcp.sdp;

/**
 * Raised when an SDP document cannot be parsed or violates the supported subset.
 */
public class SdpParseException extends RuntimeException {
    public SdpParseException(String message) {
        super(message);
    }

    public SdpParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
