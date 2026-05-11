package io.github.javamrcp.server.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.javamrcp.server.MrcpInviteHandler;
import io.github.javamrcp.server.MrcpServerConfig;
import io.github.javamrcp.server.MrcpSessionRegistry;
import io.github.javamrcp.sip.SipDatagramDecoder;
import io.github.javamrcp.sip.SipDatagramEncoder;
import io.github.javamrcp.sip.SipMessage;
import io.github.javamrcp.sip.SipMessageParser;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SipDatagramHandlerTest {
    @Test
    void respondsToOptionsWithOk() {
        InetSocketAddress sender = new InetSocketAddress("127.0.0.1", 5080);
        InetSocketAddress recipient = new InetSocketAddress("127.0.0.1", 8060);
        String options = "OPTIONS sip:mrcp@127.0.0.1 SIP/2.0\r\n"
                + "Via:SIP/2.0/UDP 127.0.0.1:5080;branch=z9hG4bK-options\r\n"
                + "From:<sip:fs@127.0.0.1>;tag=from-tag\r\n"
                + "To:<sip:mrcp@127.0.0.1>\r\n"
                + "Call-ID:call-options\r\n"
                + "CSeq:1 OPTIONS\r\n"
                + "Content-Length:0\r\n"
                + "\r\n";
        EmbeddedChannel channel = new EmbeddedChannel(
                new SipDatagramDecoder(),
                new SipDatagramEncoder(),
                new SipDatagramHandler(new MrcpSessionRegistry(10),
                        new MrcpInviteHandler(MrcpServerConfig.defaults(), new MrcpSessionRegistry(10))));

        channel.writeInbound(new DatagramPacket(
                Unpooled.copiedBuffer(options, StandardCharsets.US_ASCII),
                recipient,
                sender));

        DatagramPacket responsePacket = channel.readOutbound();
        assertNotNull(responsePacket);
        assertEquals(sender, responsePacket.recipient());
        SipMessage response = new SipMessageParser().parse(responsePacket.content().toString(StandardCharsets.US_ASCII));
        assertEquals(200, response.startLine() instanceof io.github.javamrcp.sip.SipResponseLine line
                ? line.statusCode()
                : -1);
        assertEquals("call-options", response.firstHeaderValue(SipMessage.CALL_ID).orElseThrow());
        assertEquals("<sip:mrcp@127.0.0.1>;tag=java-mrcp",
                response.firstHeaderValue(SipMessage.TO).orElseThrow());
        responsePacket.release();
    }
}
