package io.github.javamrcp.sip;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Decodes UDP datagrams into typed SIP messages with sender/recipient addresses.
 */
public final class SipDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private final SipMessageParser parser;

    public SipDatagramDecoder() {
        this(new SipMessageParser());
    }

    public SipDatagramDecoder(SipMessageParser parser) {
        this.parser = parser;
    }

    @Override
    protected void decode(ChannelHandlerContext context, DatagramPacket packet, List<Object> output) {
        byte[] bytes = new byte[packet.content().readableBytes()];
        packet.content().getBytes(packet.content().readerIndex(), bytes);
        output.add(new SipDatagramMessage(
                parser.parse(bytes),
                packet.sender(),
                packet.recipient() instanceof InetSocketAddress recipient ? recipient : null));
    }
}
