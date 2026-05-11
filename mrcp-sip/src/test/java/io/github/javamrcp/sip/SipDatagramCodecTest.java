package io.github.javamrcp.sip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SipDatagramCodecTest {
    @Test
    void decodesDatagramWithTransportAddresses() {
        InetSocketAddress sender = new InetSocketAddress("127.0.0.1", 5080);
        InetSocketAddress recipient = new InetSocketAddress("127.0.0.1", 8060);
        String payload = "OPTIONS sip:mrcp@127.0.0.1 SIP/2.0\r\n"
                + "Call-ID:call-1\r\n"
                + "Content-Length:0\r\n"
                + "\r\n";

        EmbeddedChannel channel = new EmbeddedChannel(new SipDatagramDecoder());
        channel.writeInbound(new DatagramPacket(
                Unpooled.copiedBuffer(payload, StandardCharsets.US_ASCII),
                recipient,
                sender));

        SipDatagramMessage datagram = channel.readInbound();
        assertNotNull(datagram);
        assertEquals(sender, datagram.sender());
        assertEquals(recipient, datagram.recipient());
        SipRequestLine requestLine = assertInstanceOf(SipRequestLine.class, datagram.message().startLine());
        assertEquals(SipMethod.OPTIONS, requestLine.method());
    }

    @Test
    void encodesDatagramToRecipient() {
        InetSocketAddress sender = new InetSocketAddress("127.0.0.1", 8060);
        InetSocketAddress recipient = new InetSocketAddress("127.0.0.1", 5080);
        SipMessage message = SipMessage.response(200, "OK")
                .header(SipMessage.CALL_ID, "call-1")
                .build();

        EmbeddedChannel channel = new EmbeddedChannel(new SipDatagramEncoder());
        channel.writeOutbound(new SipDatagramMessage(message, sender, recipient));

        DatagramPacket packet = channel.readOutbound();
        assertNotNull(packet);
        assertEquals(recipient, packet.recipient());
        assertEquals("SIP/2.0 200 OK\r\nCall-ID:call-1\r\nContent-Length:0\r\n\r\n",
                packet.content().toString(StandardCharsets.US_ASCII));
        packet.release();
    }

    @Test
    void requestBuilderCreatesExpectedStartLine() {
        SipMessage message = SipMessage.request(SipMethod.BYE, URI.create("sip:mrcp@127.0.0.1"))
                .header(SipMessage.CSEQ, "2 BYE")
                .build();

        SipRequestLine requestLine = assertInstanceOf(SipRequestLine.class, message.startLine());
        assertEquals(SipMethod.BYE, requestLine.method());
        assertEquals("2 BYE", message.firstHeaderValue(SipMessage.CSEQ).orElseThrow());
    }
}
