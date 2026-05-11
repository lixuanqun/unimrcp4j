package io.github.javamrcp.sip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

class SipMessageParserTest {
    private final SipMessageParser parser = new SipMessageParser();

    @Test
    void parsesInviteWithSdpBody() {
        String sdp = "v=0\r\n"
                + "o=FreeSWITCH 1 1 IN IP4 127.0.0.1\r\n"
                + "s=-\r\n"
                + "c=IN IP4 127.0.0.1\r\n"
                + "t=0 0\r\n";
        SipMessage message = parser.parse("INVITE sip:mrcp@127.0.0.1:8060 SIP/2.0\r\n"
                + "Via:SIP/2.0/UDP 127.0.0.1:5080;branch=z9hG4bK-test\r\n"
                + "From:<sip:fs@127.0.0.1>;tag=from-tag\r\n"
                + "To:<sip:mrcp@127.0.0.1>\r\n"
                + "Call-ID:call-1\r\n"
                + "CSeq:1 INVITE\r\n"
                + "Content-Type:application/sdp\r\n"
                + "Content-Length:" + sdp.getBytes().length + "\r\n"
                + "\r\n"
                + sdp);

        SipRequestLine requestLine = assertInstanceOf(SipRequestLine.class, message.startLine());
        assertEquals(SipMethod.INVITE, requestLine.method());
        assertEquals(URI.create("sip:mrcp@127.0.0.1:8060"), requestLine.requestUri());
        assertEquals("call-1", message.firstHeaderValue(SipMessage.CALL_ID).orElseThrow());
        assertEquals("application/sdp", message.firstHeaderValue(SipMessage.CONTENT_TYPE).orElseThrow());
        assertEquals(sdp, message.bodyAsString());
    }

    @Test
    void parsesResponseAndFoldedHeader() {
        SipMessage message = parser.parse("""
                SIP/2.0 200 OK\r
                Via:SIP/2.0/UDP 127.0.0.1:5080;branch=z9hG4bK-test\r
                 ;received=127.0.0.1\r
                Call-ID:call-1\r
                Content-Length:0\r
                \r
                """);

        SipResponseLine responseLine = assertInstanceOf(SipResponseLine.class, message.startLine());
        assertEquals(200, responseLine.statusCode());
        assertEquals("OK", responseLine.reasonPhrase());
        assertEquals("SIP/2.0/UDP 127.0.0.1:5080;branch=z9hG4bK-test ;received=127.0.0.1",
                message.firstHeaderValue(SipMessage.VIA).orElseThrow());
    }

    @Test
    void acceptsCompactContentLengthHeader() {
        SipMessage message = parser.parse("""
                OPTIONS sip:mrcp@127.0.0.1 SIP/2.0
                Call-ID:call-2
                l:0

                """);

        SipRequestLine requestLine = assertInstanceOf(SipRequestLine.class, message.startLine());
        assertEquals(SipMethod.OPTIONS, requestLine.method());
    }

    @Test
    void rejectsUnsupportedMethodAndContentLengthMismatch() {
        assertThrows(SipParseException.class,
                () -> parser.parse("REGISTER sip:example.com SIP/2.0\r\nContent-Length:0\r\n\r\n"));
        assertThrows(SipParseException.class,
                () -> parser.parse("INVITE sip:a SIP/2.0\r\nContent-Length:5\r\n\r\nhi"));
    }
}
