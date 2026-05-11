package io.github.javamrcp.rtp;

/**
 * Raised when an RTP packet cannot be parsed.
 */
public class RtpParseException extends RuntimeException {
    public RtpParseException(String message) {
        super(message);
    }
}
