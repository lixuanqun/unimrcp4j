package io.github.javamrcp.server.netty;

import io.github.javamrcp.core.MrcpMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First-step typed MRCP control ingress point. Session routing will be layered here next.
 */
final class MrcpControlFrameHandler extends SimpleChannelInboundHandler<MrcpMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MrcpControlFrameHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext context) {
        LOGGER.debug("MRCP control channel connected: {}", context.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, MrcpMessage message) {
        LOGGER.debug(
                "Received MRCP {} from {}: startLine={}, headers={}, bodyLength={}",
                message.messageType(),
                context.channel().remoteAddress(),
                message.startLine(),
                message.headers().size(),
                message.body().length);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        LOGGER.warn("Closing MRCP control channel after transport error", cause);
        context.close();
    }
}
