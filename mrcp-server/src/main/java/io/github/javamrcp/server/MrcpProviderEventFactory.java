package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpHeader;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpResourceEvent;
import io.github.javamrcp.spi.RecognitionResult;

/**
 * Maps provider callbacks to MRCP events.
 */
public final class MrcpProviderEventFactory {
    public MrcpMessage recognitionComplete(MrcpMessage request, RecognitionResult result) {
        MrcpRequestLine requestLine = requestLine(request);
        return eventBuilder(request, MrcpResourceEvent.RECOGNITION_COMPLETE, requestLine.requestId())
                .header("Completion-Cause", "000 success")
                .body("application/nlsml+xml", nlsml(result.text(), result.confidence()))
                .build();
    }

    public MrcpMessage speakComplete(MrcpMessage request) {
        MrcpRequestLine requestLine = requestLine(request);
        return eventBuilder(request, MrcpResourceEvent.SPEAK_COMPLETE, requestLine.requestId())
                .header("Completion-Cause", "000 normal")
                .build();
    }

    private MrcpMessage.Builder eventBuilder(MrcpMessage request, MrcpResourceEvent event, long requestId) {
        MrcpMessage.Builder builder = MrcpMessage.event(event.wireValue(), requestId, MrcpRequestState.COMPLETE);
        request.firstHeaderValue(MrcpMessage.CHANNEL_IDENTIFIER)
                .map(MrcpChannelIdentifier::parse)
                .ifPresent(builder::channelIdentifier);
        request.headers().stream()
                .filter(header -> header.hasName("Request-State"))
                .findFirst()
                .map(MrcpHeader::value)
                .ifPresent(value -> builder.header("Request-State", value));
        return builder;
    }

    private MrcpRequestLine requestLine(MrcpMessage request) {
        if (request.startLine() instanceof MrcpRequestLine requestLine) {
            return requestLine;
        }
        throw new IllegalArgumentException("MRCP request message is required");
    }

    private String nlsml(String text, double confidence) {
        return "<?xml version=\"1.0\"?>\n"
                + "<result xmlns=\"http://www.ietf.org/xml/ns/mrcpv2\">\n"
                + "  <interpretation confidence=\"" + confidence + "\">\n"
                + "    <input>" + escapeXml(text) + "</input>\n"
                + "  </interpretation>\n"
                + "</result>";
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
