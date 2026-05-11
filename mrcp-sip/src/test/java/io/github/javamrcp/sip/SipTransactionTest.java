package io.github.javamrcp.sip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.junit.jupiter.api.Test;

class SipTransactionTest {
    @Test
    void parsesCSeqAndTransactionId() {
        SipMessage request = SipMessage.request(SipMethod.INVITE, URI.create("sip:mrcp@127.0.0.1"))
                .header(SipMessage.VIA, "SIP/2.0/UDP 127.0.0.1:5080;branch=z9hG4bK-123")
                .header(SipMessage.CSEQ, "42 INVITE")
                .build();

        SipCSeq cSeq = SipCSeq.parse(request.firstHeaderValue(SipMessage.CSEQ).orElseThrow());
        SipServerTransactionId transactionId = SipServerTransactionId.fromRequest(request).orElseThrow();

        assertEquals(42, cSeq.sequence());
        assertEquals(SipMethod.INVITE, cSeq.method());
        assertEquals("z9hG4bK-123", transactionId.branch());
        assertEquals(SipMethod.INVITE, transactionId.method());
    }

    @Test
    void extractsTagsAndBuildsDialogId() {
        SipMessage request = SipMessage.request(SipMethod.INVITE, URI.create("sip:mrcp@127.0.0.1"))
                .header(SipMessage.FROM, "<sip:fs@127.0.0.1>;tag=remote-tag")
                .header(SipMessage.TO, "<sip:mrcp@127.0.0.1>")
                .header(SipMessage.CALL_ID, "call-1")
                .build();

        SipDialogId dialogId = SipDialogId.fromIncomingRequest(request, "local-tag").orElseThrow();

        assertEquals("call-1", dialogId.callId());
        assertEquals("local-tag", dialogId.localTag());
        assertEquals("remote-tag", dialogId.remoteTag());
        assertTrue(SipHeaderValues.hasParameter("<sip:a>;tag=abc", "tag"));
        assertEquals("<sip:a>;tag=abc", SipHeaderValues.appendParameterIfMissing("<sip:a>", "tag", "abc"));
    }

    @Test
    void responseFactoryPreservesTransactionHeadersAndAddsLocalTag() {
        SipMessage request = SipMessage.request(SipMethod.OPTIONS, URI.create("sip:mrcp@127.0.0.1"))
                .header(SipMessage.VIA, "SIP/2.0/UDP 127.0.0.1:5080;branch=z9hG4bK-123")
                .header(SipMessage.FROM, "<sip:fs@127.0.0.1>;tag=remote-tag")
                .header(SipMessage.TO, "<sip:mrcp@127.0.0.1>")
                .header(SipMessage.CALL_ID, "call-1")
                .header(SipMessage.CSEQ, "1 OPTIONS")
                .build();

        SipMessage response = new SipResponseFactory().createResponse(request, 200, "OK", "local-tag");

        SipResponseLine responseLine = (SipResponseLine) response.startLine();
        assertEquals(200, responseLine.statusCode());
        assertEquals("SIP/2.0/UDP 127.0.0.1:5080;branch=z9hG4bK-123",
                response.firstHeaderValue(SipMessage.VIA).orElseThrow());
        assertEquals("<sip:mrcp@127.0.0.1>;tag=local-tag",
                response.firstHeaderValue(SipMessage.TO).orElseThrow());
        assertEquals("call-1", response.firstHeaderValue(SipMessage.CALL_ID).orElseThrow());
        assertEquals("1 OPTIONS", response.firstHeaderValue(SipMessage.CSEQ).orElseThrow());
    }
}
