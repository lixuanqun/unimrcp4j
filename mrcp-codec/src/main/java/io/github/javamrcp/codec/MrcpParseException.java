package io.github.javamrcp.codec;

/**
 * Raised when an MRCP/2.0 message cannot be parsed or violates basic framing rules.
 */
public class MrcpParseException extends RuntimeException {
    public MrcpParseException(String message) {
        super(message);
    }

    public MrcpParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
