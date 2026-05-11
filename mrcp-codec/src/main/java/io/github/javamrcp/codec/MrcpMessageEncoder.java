package io.github.javamrcp.codec;

import io.github.javamrcp.core.MrcpMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Serializes typed MRCP messages for Netty TCP channels.
 */
public final class MrcpMessageEncoder extends MessageToByteEncoder<MrcpMessage> {
    private final MrcpMessageGenerator generator;

    public MrcpMessageEncoder() {
        this(new MrcpMessageGenerator());
    }

    public MrcpMessageEncoder(MrcpMessageGenerator generator) {
        this.generator = generator;
    }

    @Override
    protected void encode(ChannelHandlerContext context, MrcpMessage message, ByteBuf output) {
        output.writeBytes(generator.generate(message));
    }
}
