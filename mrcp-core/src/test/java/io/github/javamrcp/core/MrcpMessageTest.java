package io.github.javamrcp.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class MrcpMessageTest {
    @Test
    void defensivelyCopiesBodyAndHeaders() {
        byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
        MrcpMessage message = new MrcpMessage(
                new MrcpRequestLine(MrcpVersion.MRCP_2_0, 0, "RECOGNIZE", 1),
                List.of(new MrcpHeader("Channel-Identifier", "session@speechrecog")),
                body);

        body[0] = 'H';
        byte[] returned = message.body();
        returned[1] = 'A';

        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), message.body());
        assertEquals("session@speechrecog", message.headers().getFirst().value());
        assertThrows(UnsupportedOperationException.class, () -> message.headers().add(new MrcpHeader("X-Test", "1")));
    }

    @Test
    void builderCreatesUtf8BodyAndCaseInsensitiveHeaderLookup() {
        MrcpMessage message = MrcpMessage.request("SPEAK", 9)
                .header("X-Test", "first")
                .body("application/ssml+xml", "<speak>你好</speak>")
                .build();

        assertEquals(MrcpMessageType.REQUEST, message.messageType());
        assertEquals("first", message.firstHeaderValue("x-test").orElseThrow());
        assertEquals("application/ssml+xml", message.firstHeaderValue(MrcpMessage.CONTENT_TYPE).orElseThrow());
        assertEquals("<speak>你好</speak>", message.bodyAsString());
    }

    @Test
    void withStartLinePreservesHeadersAndBodyInNewMessage() {
        MrcpMessage original = MrcpMessage.request("STOP", 1)
                .header("Channel-Identifier", "session@speechsynth")
                .build();
        MrcpMessage changed = original.withStartLine(
                new MrcpRequestLine(MrcpVersion.MRCP_2_0, 42, "STOP", 1));

        assertNotSame(original, changed);
        assertEquals(42, changed.startLine().messageLength());
        assertEquals(original.headers(), changed.headers());
        assertArrayEquals(original.body(), changed.body());
    }

    @Test
    void rejectsInvalidStartLineAndHeaderValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new MrcpRequestLine(MrcpVersion.MRCP_2_0, -1, "STOP", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new MrcpRequestLine(MrcpVersion.MRCP_2_0, 0, " ", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new MrcpResponseLine(MrcpVersion.MRCP_2_0, 0, 1, 99, MrcpRequestState.COMPLETE));
        assertThrows(IllegalArgumentException.class,
                () -> new MrcpHeader("Bad:Name", "value"));
    }
}
