package io.github.javamrcp.sip;

import java.util.List;
import java.util.Objects;

/**
 * Builds SIP responses from incoming requests while preserving transaction headers.
 */
public final class SipResponseFactory {
    public SipMessage createResponse(SipMessage request, int statusCode, String reasonPhrase, String localTag) {
        return createResponse(request, statusCode, reasonPhrase, localTag, null, null);
    }

    public SipMessage createResponse(
            SipMessage request,
            int statusCode,
            String reasonPhrase,
            String localTag,
            String contentType,
            String body) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(reasonPhrase, "reasonPhrase");
        if (!(request.startLine() instanceof SipRequestLine)) {
            throw new IllegalArgumentException("request message is required");
        }

        SipMessage.Builder builder = SipMessage.response(statusCode, reasonPhrase);
        copyHeaders(request, builder, SipMessage.VIA);
        copyHeaders(request, builder, SipMessage.FROM);
        request.firstHeaderValue(SipMessage.TO)
                .map(to -> localTag == null || localTag.isBlank()
                        ? to
                        : SipHeaderValues.appendParameterIfMissing(to, "tag", localTag))
                .ifPresent(to -> builder.header(SipMessage.TO, to));
        copyHeaders(request, builder, SipMessage.CALL_ID);
        copyHeaders(request, builder, SipMessage.CSEQ);
        if (body != null) {
            builder.body(contentType, body);
        }
        return builder.build();
    }

    private void copyHeaders(SipMessage request, SipMessage.Builder builder, String name) {
        List<String> values = request.headers().stream()
                .filter(header -> header.hasName(name))
                .map(SipHeader::value)
                .toList();
        for (String value : values) {
            builder.header(name, value);
        }
    }
}
