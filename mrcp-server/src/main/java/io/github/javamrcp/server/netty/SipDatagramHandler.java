package io.github.javamrcp.server.netty;

import io.github.javamrcp.sip.SipDatagramMessage;
import io.github.javamrcp.sip.SipMessage;
import io.github.javamrcp.sip.SipMethod;
import io.github.javamrcp.sip.SipRequestLine;
import io.github.javamrcp.sip.SipResponseFactory;
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
    private static final String SERVER_TAG = "java-mrcp";

    private final SipResponseFactory responseFactory = new SipResponseFactory();

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
        if (message.startLine() instanceof SipRequestLine requestLine
                && requestLine.method() == SipMethod.OPTIONS) {
            SipMessage response = responseFactory.createResponse(message, 200, "OK", SERVER_TAG);
            context.writeAndFlush(new SipDatagramMessage(response, datagram.sender(), datagram.sender()));
        }
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
