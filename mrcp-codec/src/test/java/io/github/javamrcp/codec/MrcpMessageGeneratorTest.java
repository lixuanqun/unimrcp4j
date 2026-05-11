package io.github.javamrcp.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpEventLine;
import io.github.javamrcp.core.MrcpMessage;
import io.github.javamrcp.core.MrcpRequestState;
import io.github.javamrcp.core.MrcpResponseLine;
import io.github.javamrcp.core.MrcpRequestLine;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MrcpMessageGeneratorTest {
    private final MrcpMessageGenerator generator = new MrcpMessageGenerator();
    private final MrcpMessageParser parser = new MrcpMessageParser();

    @Test
    void normalizesMessageLengthAndContentLength() {
        MrcpMessage message = MrcpMessage.request("SPEAK", 543257)
                .channelIdentifier(new MrcpChannelIdentifier("32AECB23433802", "speechsynth"))
                .body("application/ssml+xml", "<speak>你好</speak>")
                .build();

        byte[] encoded = generator.generate(message);
        MrcpMessage parsed = parser.parse(encoded);

        MrcpRequestLine requestLine = assertInstanceOf(MrcpRequestLine.class, parsed.startLine());
        assertEquals(encoded.length, requestLine.messageLength());
        assertEquals(Integer.toString("<speak>你好</speak>".getBytes(StandardCharsets.UTF_8).length),
                parsed.firstHeaderValue(MrcpMessage.CONTENT_LENGTH).orElseThrow());
        assertEquals("<speak>你好</speak>", parsed.bodyAsString());
    }

    @Test
    void doesNotAddContentLengthForEmptyBody() {
        MrcpMessage message = MrcpMessage.request("STOP", 543258)
                .channelIdentifier(new MrcpChannelIdentifier("32AECB23433802", "speechsynth"))
                .build();

        String encoded = generator.generateString(message);

        assertTrue(encoded.startsWith("MRCP/2.0 "));
        assertTrue(encoded.contains(" STOP 543258\r\n"));
        assertTrue(encoded.endsWith("\r\n\r\n"));
        assertTrue(parser.parse(encoded).firstHeaderValue(MrcpMessage.CONTENT_LENGTH).isEmpty());
    }

    @Test
    void generatesResponseStartLine() {
        MrcpMessage message = MrcpMessage.response(543257, 200, MrcpRequestState.IN_PROGRESS)
                .channelIdentifier(new MrcpChannelIdentifier("32AECB23433801", "speechrecog"))
                .build();

        MrcpMessage parsed = parser.parse(generator.generate(message));
        MrcpResponseLine responseLine = assertInstanceOf(MrcpResponseLine.class, parsed.startLine());

        assertEquals(543257L, responseLine.requestId());
        assertEquals(200, responseLine.statusCode());
        assertEquals(MrcpRequestState.IN_PROGRESS, responseLine.requestState());
    }

    @Test
    void generatesEventStartLine() {
        MrcpMessage message = MrcpMessage.event("SPEAK-COMPLETE", 543257, MrcpRequestState.COMPLETE)
                .channelIdentifier(new MrcpChannelIdentifier("32AECB23433802", "speechsynth"))
                .header("Completion-Cause", "000 normal")
                .build();

        MrcpMessage parsed = parser.parse(generator.generate(message));
        MrcpEventLine eventLine = assertInstanceOf(MrcpEventLine.class, parsed.startLine());

        assertEquals("SPEAK-COMPLETE", eventLine.eventName());
        assertEquals(MrcpRequestState.COMPLETE, eventLine.requestState());
        assertEquals("000 normal", parsed.firstHeaderValue("completion-cause").orElseThrow());
    }

    @Test
    void replacesExistingContentLength() {
        MrcpMessage message = MrcpMessage.request("RECOGNIZE", 543257)
                .header(MrcpMessage.CONTENT_LENGTH, "999")
                .body("hello".getBytes(StandardCharsets.UTF_8))
                .build();

        MrcpMessage parsed = parser.parse(generator.generate(message));

        assertEquals("5", parsed.firstHeaderValue(MrcpMessage.CONTENT_LENGTH).orElseThrow());
        assertEquals("hello", parsed.bodyAsString());
    }
}
