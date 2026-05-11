package io.github.javamrcp.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First-step MRCP TCP ingress point. Content-Length aware framing will replace this stub.
 */
final class MrcpControlFrameHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MrcpControlFrameHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext context) {
        LOGGER.debug("MRCP control channel connected: {}", context.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, ByteBuf message) {
        String firstLine = firstLine(message);
        LOGGER.debug(
                "Received MRCP control bytes from {} with {} bytes: {}",
                context.channel().remoteAddress(),
                message.readableBytes(),
                firstLine);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        LOGGER.warn("Closing MRCP control channel after transport error", cause);
        context.close();
    }

    private String firstLine(ByteBuf message) {
        String payload = message.toString(StandardCharsets.US_ASCII);
        int lineEnd = payload.indexOf("\r\n");
        if (lineEnd < 0) {
            lineEnd = payload.indexOf('\n');
        }
        return lineEnd >= 0 ? payload.substring(0, lineEnd) : payload;
    }
}
