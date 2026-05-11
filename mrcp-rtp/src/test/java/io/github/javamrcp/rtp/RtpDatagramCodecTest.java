package io.github.javamrcp.rtp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;

class RtpDatagramCodecTest {
    @Test
    void decodesDatagramWithTransportAddresses() {
        RtpPacket packet = new RtpPacket(false, 0, 1, 160, 10, new byte[] {1, 2});
        InetSocketAddress sender = new InetSocketAddress("127.0.0.1", 5000);
        InetSocketAddress recipient = new InetSocketAddress("127.0.0.1", 4000);
        EmbeddedChannel channel = new EmbeddedChannel(new RtpDatagramDecoder());

        channel.writeInbound(new DatagramPacket(Unpooled.wrappedBuffer(packet.encode()), recipient, sender));

        RtpDatagramPacket decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(sender, decoded.sender());
        assertEquals(recipient, decoded.recipient());
        assertEquals(1, decoded.packet().sequenceNumber());
        assertArrayEquals(new byte[] {1, 2}, decoded.packet().payload());
    }

    @Test
    void encodesDatagram() {
        RtpPacket packet = new RtpPacket(true, 8, 2, 320, 20, new byte[] {3, 4});
        InetSocketAddress sender = new InetSocketAddress("127.0.0.1", 4000);
        InetSocketAddress recipient = new InetSocketAddress("127.0.0.1", 5000);
        EmbeddedChannel channel = new EmbeddedChannel(new RtpDatagramEncoder());

        channel.writeOutbound(new RtpDatagramPacket(packet, sender, recipient));

        DatagramPacket encoded = channel.readOutbound();
        assertNotNull(encoded);
        assertEquals(recipient, encoded.recipient());
        assertEquals(2, RtpPacket.parse(encoded.content().array()).sequenceNumber());
        encoded.release();
    }
}
