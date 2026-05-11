package io.github.javamrcp.server;

import io.github.javamrcp.sdp.SdpConnection;
import io.github.javamrcp.sdp.SdpGenerator;
import io.github.javamrcp.sdp.SdpMediaDescription;
import io.github.javamrcp.sdp.SdpMediaType;
import io.github.javamrcp.sdp.SdpOrigin;
import io.github.javamrcp.sdp.SdpParser;
import io.github.javamrcp.sdp.SdpSession;
import io.github.javamrcp.sip.SipMessage;
import io.github.javamrcp.sip.SipResponseFactory;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Handles the SIP INVITE offer/answer path for MRCPv2 sessions.
 */
public final class MrcpInviteHandler {
    private static final String LOCAL_TAG = "java-mrcp";

    private final MrcpServerConfig config;
    private final MrcpSessionRegistry sessionRegistry;
    private final MrcpSessionFactory sessionFactory;
    private final SipResponseFactory responseFactory;
    private final SdpParser sdpParser;
    private final SdpGenerator sdpGenerator;

    public MrcpInviteHandler(MrcpServerConfig config, MrcpSessionRegistry sessionRegistry) {
        this(
                config,
                sessionRegistry,
                new MrcpSessionFactory(),
                new SipResponseFactory(),
                new SdpParser(),
                new SdpGenerator());
    }

    MrcpInviteHandler(
            MrcpServerConfig config,
            MrcpSessionRegistry sessionRegistry,
            MrcpSessionFactory sessionFactory,
            SipResponseFactory responseFactory,
            SdpParser sdpParser,
            SdpGenerator sdpGenerator) {
        this.config = Objects.requireNonNull(config, "config");
        this.sessionRegistry = Objects.requireNonNull(sessionRegistry, "sessionRegistry");
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "sessionFactory");
        this.responseFactory = Objects.requireNonNull(responseFactory, "responseFactory");
        this.sdpParser = Objects.requireNonNull(sdpParser, "sdpParser");
        this.sdpGenerator = Objects.requireNonNull(sdpGenerator, "sdpGenerator");
    }

    public SipMessage handleInvite(SipMessage invite) {
        String callId = invite.firstHeaderValue(SipMessage.CALL_ID)
                .orElseThrow(() -> new IllegalArgumentException("INVITE is missing Call-ID"));
        SdpSession offer = sdpParser.parse(invite.bodyAsString());
        MrcpServerSession session = sessionFactory.createFromOffer(callId, offer);
        SdpSession answer = createAnswer(session);
        session.answerSent(answer);
        sessionRegistry.register(session);
        return responseFactory.createResponse(
                invite,
                200,
                "OK",
                LOCAL_TAG,
                "application/sdp",
                sdpGenerator.generate(answer));
    }

    public SipMessage rejectInvite(SipMessage invite, int statusCode, String reasonPhrase) {
        return responseFactory.createResponse(invite, statusCode, reasonPhrase, LOCAL_TAG);
    }

    private SdpSession createAnswer(MrcpServerSession session) {
        long sessionVersion = Instant.now().toEpochMilli();
        SdpSession.Builder builder = SdpSession.builder(SdpOrigin.ip4(
                        "java-mrcp",
                        sessionVersion,
                        sessionVersion,
                        config.advertisedHost()))
                .connection(SdpConnection.ip4(config.advertisedHost()));

        for (MrcpResourceChannel channel : session.resourceChannels()) {
            builder.media(controlAnswer(channel.controlMedia()));
        }
        for (SdpMediaDescription audioOffer : session.remoteOffer().audioMedia()) {
            builder.media(audioAnswer(audioOffer));
        }
        return builder.build();
    }

    private SdpMediaDescription controlAnswer(SdpMediaDescription offer) {
        SdpMediaDescription.Builder builder = SdpMediaDescription.builder(
                        SdpMediaType.APPLICATION,
                        config.mrcpPort(),
                        "TCP/MRCPv2")
                .formats(offer.formats())
                .attribute("setup", "passive")
                .attribute("connection", "new");
        copyAttributeValues(offer, builder, "resource");
        copyAttributeValues(offer, builder, "channel");
        copyAttributeValues(offer, builder, "cmid");
        return builder.build();
    }

    private SdpMediaDescription audioAnswer(SdpMediaDescription offer) {
        SdpMediaDescription.Builder builder = SdpMediaDescription.builder(
                        SdpMediaType.AUDIO,
                        config.rtpPort(),
                        offer.protocol())
                .formats(offer.formats())
                .attribute("sendrecv");
        copyAttributeValues(offer, builder, "rtpmap");
        copyAttributeValues(offer, builder, "fmtp");
        copyAttributeValues(offer, builder, "ptime");
        return builder.build();
    }

    private void copyAttributeValues(
            SdpMediaDescription source,
            SdpMediaDescription.Builder target,
            String attributeName) {
        List<String> values = source.attributeValues(attributeName);
        for (String value : values) {
            target.attribute(attributeName, value);
        }
    }
}
