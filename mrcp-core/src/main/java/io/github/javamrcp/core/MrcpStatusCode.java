package io.github.javamrcp.core;

/**
 * MRCP status codes used by the initial server state machines.
 */
public enum MrcpStatusCode {
    SUCCESS(200),
    METHOD_NOT_ALLOWED(405),
    METHOD_NOT_VALID_IN_THIS_STATE(402),
    UNSUPPORTED_HEADER_VALUE(407),
    SERVER_INTERNAL_ERROR(500);

    private final int code;

    MrcpStatusCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
