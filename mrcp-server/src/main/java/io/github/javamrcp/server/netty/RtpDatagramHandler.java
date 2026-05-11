package io.github.javamrcp.server.netty;

import io.github.javamrcp.rtp.RtpDatagramPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First RTP UDP ingress point. Media routing to sessions is layered here next.
 */
final class RtpDatagramHandler extends SimpleChannelInboundHandler<RtpDatagramPacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtpDatagramHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext context, RtpDatagramPacket datagram) {
        LOGGER.debug(
                "Received RTP packet from {}: pt={}, seq={}, ts={}, ssrc={}, payload={} bytes",
                datagram.sender(),
                datagram.packet().payloadType(),
                datagram.packet().sequenceNumber(),
                datagram.packet().timestamp(),
                datagram.packet().ssrc(),
                datagram.packet().payload().length);
    }
}
