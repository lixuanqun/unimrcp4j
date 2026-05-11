package io.github.javamrcp.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpMessage;
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
}
