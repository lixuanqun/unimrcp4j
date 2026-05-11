package io.github.javamrcp.sip;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Encodes typed SIP messages as UDP datagrams.
 */
public final class SipDatagramEncoder extends MessageToMessageEncoder<SipDatagramMessage> {
    private final SipMessageGenerator generator;

    public SipDatagramEncoder() {
        this(new SipMessageGenerator());
    }

    public SipDatagramEncoder(SipMessageGenerator generator) {
        this.generator = generator;
    }

    @Override
    protected void encode(ChannelHandlerContext context, SipDatagramMessage message, List<Object> output) {
        InetSocketAddress recipient = message.recipient() != null ? message.recipient() : message.sender();
        output.add(new DatagramPacket(Unpooled.wrappedBuffer(generator.generate(message.message())), recipient));
    }
}
