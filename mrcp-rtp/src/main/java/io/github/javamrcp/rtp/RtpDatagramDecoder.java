package io.github.javamrcp.rtp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Decodes UDP datagrams into RTP packets.
 */
public final class RtpDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext context, DatagramPacket packet, List<Object> output) {
        byte[] bytes = new byte[packet.content().readableBytes()];
        packet.content().getBytes(packet.content().readerIndex(), bytes);
        output.add(new RtpDatagramPacket(
                RtpPacket.parse(bytes),
                packet.sender(),
                packet.recipient() instanceof InetSocketAddress recipient ? recipient : null));
    }
}
