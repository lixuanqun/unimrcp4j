package io.github.javamrcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

/**
 * Converts complete MRCP frames into typed {@code MrcpMessage} instances.
 */
public final class MrcpMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final MrcpMessageParser parser;

    public MrcpMessageDecoder() {
        this(new MrcpMessageParser());
    }

    public MrcpMessageDecoder(MrcpMessageParser parser) {
        this.parser = parser;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf frame, List<Object> output) {
        byte[] bytes = new byte[frame.readableBytes()];
        frame.getBytes(frame.readerIndex(), bytes);
        output.add(parser.parse(bytes));
    }
}
