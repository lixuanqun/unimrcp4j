package io.github.javamrcp.sip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SipMessageGeneratorTest {
    private final SipMessageGenerator generator = new SipMessageGenerator();
    private final SipMessageParser parser = new SipMessageParser();

    @Test
    void generatesRequestAndNormalizesContentLength() {
        String sdp = "v=0\r\ns=-\r\n";
        SipMessage message = SipMessage.request(SipMethod.INVITE, URI.create("sip:mrcp@127.0.0.1:8060"))
                .header(SipMessage.CALL_ID, "call-1")
                .header(SipMessage.CONTENT_LENGTH, "999")
                .body("application/sdp", sdp)
                .build();

        SipMessage parsed = parser.parse(generator.generate(message));
        SipRequestLine requestLine = assertInstanceOf(SipRequestLine.class, parsed.startLine());

        assertEquals(SipMethod.INVITE, requestLine.method());
        assertEquals("call-1", parsed.firstHeaderValue(SipMessage.CALL_ID).orElseThrow());
        assertEquals(Integer.toString(sdp.getBytes(StandardCharsets.UTF_8).length),
                parsed.firstHeaderValue(SipMessage.CONTENT_LENGTH).orElseThrow());
        assertEquals(sdp, parsed.bodyAsString());
    }

    @Test
    void generatesResponseWithZeroContentLength() {
        SipMessage message = SipMessage.response(200, "OK")
                .header(SipMessage.CALL_ID, "call-1")
                .build();

        String encoded = generator.generateString(message);

        assertTrue(encoded.startsWith("SIP/2.0 200 OK\r\n"));
        assertTrue(encoded.contains("Call-ID:call-1\r\n"));
        assertTrue(encoded.contains("Content-Length:0\r\n"));
        assertTrue(encoded.endsWith("\r\n\r\n"));
        assertInstanceOf(SipResponseLine.class, parser.parse(encoded).startLine());
    }
}
