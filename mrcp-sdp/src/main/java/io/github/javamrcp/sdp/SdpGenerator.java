package io.github.javamrcp.sdp;

import java.util.Objects;

/**
 * Serializes SDP session descriptions.
 */
public final class SdpGenerator {
    private static final String CRLF = "\r\n";

    public String generate(SdpSession session) {
        Objects.requireNonNull(session, "session");
        StringBuilder builder = new StringBuilder();
        appendLine(builder, 'v', Integer.toString(session.version()));
        appendLine(builder, 'o', session.origin().wireValue());
        appendLine(builder, 's', session.sessionName());
        session.connection().ifPresent(connection -> appendLine(builder, 'c', connection.wireValue()));
        appendLine(builder, 't', session.timing());
        for (SdpAttribute attribute : session.attributes()) {
            appendLine(builder, 'a', attribute.wireValue());
        }
        for (SdpMediaDescription mediaDescription : session.mediaDescriptions()) {
            appendLine(builder, 'm', mediaDescription.mediaLineValue());
            mediaDescription.connection().ifPresent(connection -> appendLine(builder, 'c', connection.wireValue()));
            for (SdpAttribute attribute : mediaDescription.attributes()) {
                appendLine(builder, 'a', attribute.wireValue());
            }
        }
        return builder.toString();
    }

    private void appendLine(StringBuilder builder, char type, String value) {
        builder.append(type).append('=').append(value).append(CRLF);
    }
}
