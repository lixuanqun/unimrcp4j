package io.github.javamrcp.rtp;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Encodes RTP packets as UDP datagrams.
 */
public final class RtpDatagramEncoder extends MessageToMessageEncoder<RtpDatagramPacket> {
    @Override
    protected void encode(ChannelHandlerContext context, RtpDatagramPacket message, List<Object> output) {
        InetSocketAddress recipient = message.recipient() != null ? message.recipient() : message.sender();
        output.add(new DatagramPacket(Unpooled.wrappedBuffer(message.packet().encode()), recipient));
    }
}
