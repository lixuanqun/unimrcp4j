package io.github.javamrcp.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First-step SIP UDP ingress point. Full SIP transaction parsing will be layered here next.
 */
final class SipDatagramHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SipDatagramHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext context, DatagramPacket packet) {
        String firstLine = firstLine(packet);
        LOGGER.debug(
                "Received SIP datagram from {} with {} bytes: {}",
                packet.sender(),
                packet.content().readableBytes(),
                firstLine);
    }

    private String firstLine(DatagramPacket packet) {
        String payload = packet.content().toString(StandardCharsets.US_ASCII);
        int lineEnd = payload.indexOf("\r\n");
        if (lineEnd < 0) {
            lineEnd = payload.indexOf('\n');
        }
        return lineEnd >= 0 ? payload.substring(0, lineEnd) : payload;
    }
}
