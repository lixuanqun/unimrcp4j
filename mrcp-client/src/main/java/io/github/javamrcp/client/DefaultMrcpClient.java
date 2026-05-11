package io.github.javamrcp.client;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.rtp.RtpAudioCodec;
import io.github.javamrcp.sdp.SdpConnection;
import io.github.javamrcp.sdp.SdpGenerator;
import io.github.javamrcp.sdp.SdpMediaDescription;
import io.github.javamrcp.sdp.SdpMediaType;
import io.github.javamrcp.sdp.SdpOrigin;
import io.github.javamrcp.sdp.SdpSession;
import io.github.javamrcp.sip.SipMessage;
import io.github.javamrcp.sip.SipMethod;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default client SDK implementation for generating SIP/SDP MRCPv2 offers.
 */
public final class DefaultMrcpClient implements MrcpClient {
    private final MrcpClientConfig config;
    private final SdpGenerator sdpGenerator = new SdpGenerator();
    private final AtomicLong cseq = new AtomicLong(1);

    public DefaultMrcpClient(MrcpClientConfig config) {
        this.config = config;
    }

    @Override
    public ClientInvite createInvite(MrcpResourceType resourceType) {
        String callId = UUID.randomUUID() + "@" + config.localHost();
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        MrcpChannelIdentifier channelIdentifier = new MrcpChannelIdentifier(sessionId, resourceType.wireValue());
        SdpSession offer = createOffer(channelIdentifier, resourceType);
        MrcpClientSession session = new MrcpClientSession(callId, channelIdentifier, resourceType, offer);
        SipMessage invite = createSipInvite(callId, offer);
        return new ClientInvite(session, invite);
    }

    private SdpSession createOffer(MrcpChannelIdentifier channelIdentifier, MrcpResourceType resourceType) {
        long sessionVersion = System.currentTimeMillis();
        return SdpSession.builder(SdpOrigin.ip4("java-mrcp-client", sessionVersion, sessionVersion, config.localHost()))
                .connection(SdpConnection.ip4(config.localHost()))
                .media(SdpMediaDescription.builder(SdpMediaType.APPLICATION, 9, "TCP/MRCPv2")
                        .format("1")
                        .attribute("setup", "active")
                        .attribute("connection", "new")
                        .attribute("resource", resourceType.wireValue())
                        .attribute("channel", channelIdentifier.wireValue())
                        .build())
                .media(SdpMediaDescription.builder(SdpMediaType.AUDIO, config.localRtpPort(), "RTP/AVP")
                        .format(Integer.toString(RtpAudioCodec.PCMU.payloadType()))
                        .format(Integer.toString(RtpAudioCodec.PCMA.payloadType()))
                        .format(Integer.toString(RtpAudioCodec.L16_DYNAMIC.payloadType()))
                        .attribute("rtpmap", RtpAudioCodec.PCMU.rtpMapValue())
                        .attribute("rtpmap", RtpAudioCodec.PCMA.rtpMapValue())
                        .attribute("rtpmap", RtpAudioCodec.L16_DYNAMIC.rtpMapValue())
                        .attribute("sendrecv")
                        .build())
                .build();
    }

    private SipMessage createSipInvite(String callId, SdpSession offer) {
        URI requestUri = URI.create("sip:" + config.toUser() + "@" + config.serverHost() + ":" + config.serverSipPort());
        String from = "<sip:" + config.fromUser() + "@" + config.localHost() + ">;tag=" + UUID.randomUUID();
        String to = "<sip:" + config.toUser() + "@" + config.serverHost() + ">";
        return SipMessage.request(SipMethod.INVITE, requestUri)
                .header(SipMessage.VIA, "SIP/2.0/UDP " + config.localHost() + ":" + config.localSipPort()
                        + ";branch=z9hG4bK-" + UUID.randomUUID())
                .header(SipMessage.FROM, from)
                .header(SipMessage.TO, to)
                .header(SipMessage.CALL_ID, callId)
                .header(SipMessage.CSEQ, cseq.getAndIncrement() + " INVITE")
                .header("Contact", "<sip:" + config.fromUser() + "@" + config.localHost() + ":"
                        + config.localSipPort() + ">")
                .body("application/sdp", sdpGenerator.generate(offer))
                .build();
    }
}
