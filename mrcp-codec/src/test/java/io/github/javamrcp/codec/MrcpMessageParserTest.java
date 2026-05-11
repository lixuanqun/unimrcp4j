package io.github.javamrcp.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpEventLine;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestLine;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpResourceType;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MrcpMessageParserTest {
    private final MrcpMessageParser parser = new MrcpMessageParser();

    @Test
    void parsesRequestWithBody() {
        String body = "hello";
        MrcpMessage message = parser.parse("""
                MRCP/2.0 94 RECOGNIZE 543257\r
                Channel-Identifier:32AECB23433801@speechrecog\r
                Content-Length:5\r
                \r
                hello""");

        assertTrue(message.startLine() instanceof MrcpRequestLine);
        MrcpRequestLine requestLine = (MrcpRequestLine) message.startLine();
        assertEquals("RECOGNIZE", requestLine.methodName());
        assertEquals(543257L, requestLine.requestId());
        assertEquals(body, message.bodyAsString());

        MrcpChannelIdentifier channelIdentifier = message.channelIdentifier().orElseThrow();
        assertEquals("32AECB23433801", channelIdentifier.sessionId());
        assertEquals(MrcpResourceType.SPEECH_RECOGNIZER, channelIdentifier.resourceType().orElseThrow());
    }

    @Test
    void parsesEventWithoutBody() {
        MrcpMessage message = parser.parse("""
                MRCP/2.0 161 SPEAK-COMPLETE 543257 COMPLETE\r
                Channel-Identifier:32AECB23433802@speechsynth\r
                Completion-Cause:000 normal\r
                \r
                """);

        assertTrue(message.startLine() instanceof MrcpEventLine);
        MrcpEventLine eventLine = (MrcpEventLine) message.startLine();
        assertEquals("SPEAK-COMPLETE", eventLine.eventName());
        assertEquals(MrcpRequestState.COMPLETE, eventLine.requestState());
        assertArrayEquals(new byte[0], message.body());
    }

    @Test
    void rejectsContentLengthMismatch() {
        byte[] frame = """
                MRCP/2.0 94 RECOGNIZE 543257\r
                Content-Length:6\r
                \r
                hello""".getBytes(StandardCharsets.UTF_8);

        assertThrows(MrcpParseException.class, () -> parser.parse(frame));
    }
}
