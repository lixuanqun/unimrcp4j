package io.github.javamrcp.sip;

/**
 * Raised when a SIP datagram cannot be parsed.
 */
public class SipParseException extends RuntimeException {
    public SipParseException(String message) {
        super(message);
    }

    public SipParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
