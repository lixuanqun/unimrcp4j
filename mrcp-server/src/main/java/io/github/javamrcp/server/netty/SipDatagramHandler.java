package io.github.javamrcp.server.netty;

import io.github.javamrcp.server.MrcpInviteHandler;
import io.github.javamrcp.server.MrcpServerConfig;
import io.github.javamrcp.server.MrcpServerSession;
import io.github.javamrcp.server.MrcpSessionRegistry;
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

    private final MrcpSessionRegistry sessionRegistry;
    private final MrcpInviteHandler inviteHandler;
    private final SipResponseFactory responseFactory = new SipResponseFactory();

    SipDatagramHandler(MrcpServerConfig config, MrcpSessionRegistry sessionRegistry) {
        this(sessionRegistry, new MrcpInviteHandler(config, sessionRegistry));
    }

    SipDatagramHandler(MrcpSessionRegistry sessionRegistry, MrcpInviteHandler inviteHandler) {
        this.sessionRegistry = sessionRegistry;
        this.inviteHandler = inviteHandler;
    }

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
        if (message.startLine() instanceof SipRequestLine requestLine) {
            handleRequest(context, datagram, message, requestLine);
        }
    }

    private void handleRequest(
            ChannelHandlerContext context,
            SipDatagramMessage datagram,
            SipMessage message,
            SipRequestLine requestLine) {
        switch (requestLine.method()) {
            case OPTIONS -> respond(context, datagram, responseFactory.createResponse(message, 200, "OK", SERVER_TAG));
            case INVITE -> handleInvite(context, datagram, message);
            case ACK -> message.firstHeaderValue(SipMessage.CALL_ID)
                    .flatMap(sessionRegistry::findByCallId)
                    .ifPresent(MrcpServerSession::established);
            case BYE, CANCEL -> {
                message.firstHeaderValue(SipMessage.CALL_ID)
                        .flatMap(sessionRegistry::findByCallId)
                        .ifPresent(session -> sessionRegistry.remove(session.id()));
                respond(context, datagram, responseFactory.createResponse(message, 200, "OK", SERVER_TAG));
            }
        }
    }

    private void handleInvite(ChannelHandlerContext context, SipDatagramMessage datagram, SipMessage message) {
        try {
            respond(context, datagram, inviteHandler.handleInvite(message));
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Rejecting SIP INVITE: {}", ex.getMessage());
            respond(context, datagram, inviteHandler.rejectInvite(message, 488, "Not Acceptable Here"));
        } catch (RuntimeException ex) {
            LOGGER.warn("Failed to handle SIP INVITE", ex);
            respond(context, datagram, inviteHandler.rejectInvite(message, 500, "Server Internal Error"));
        }
    }

    private void respond(ChannelHandlerContext context, SipDatagramMessage request, SipMessage response) {
        context.writeAndFlush(new SipDatagramMessage(response, request.sender(), request.sender()));
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
