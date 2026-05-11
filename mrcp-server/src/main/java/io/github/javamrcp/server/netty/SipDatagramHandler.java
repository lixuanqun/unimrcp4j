package io.github.javamrcp.server.netty;

import io.github.javamrcp.sip.SipDatagramMessage;
import io.github.javamrcp.sip.SipMessage;
import io.github.javamrcp.sip.SipRequestLine;
import io.github.javamrcp.sip.SipResponseLine;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First-step typed SIP UDP ingress point. SIP transactions will be layered here next.
 */
final class SipDatagramHandler extends SimpleChannelInboundHandler<SipDatagramMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SipDatagramHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext context, SipDatagramMessage datagram) {
        SipMessage message = datagram.message();
        LOGGER.debug(
                "Received SIP {} from {}: startLine={}, headers={}, bodyLength={}",
                message.messageType(),
                datagram.sender(),
                startLineSummary(message),
                message.headers().size(),
                message.body().length);
    }

    private String startLineSummary(SipMessage message) {
        if (message.startLine() instanceof SipRequestLine requestLine) {
            return requestLine.method() + " " + requestLine.requestUri();
        }
        if (message.startLine() instanceof SipResponseLine responseLine) {
            return responseLine.statusCode() + " " + responseLine.reasonPhrase();
        }
        return message.startLine().toString();
    }
}
