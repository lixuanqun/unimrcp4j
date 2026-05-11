package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.core.MrcpSessionId;
import io.github.javamrcp.sdp.SdpMediaDescription;
import io.github.javamrcp.sdp.SdpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Creates server sessions from validated SIP/SDP offers.
 */
public final class MrcpSessionFactory {
    public MrcpServerSession createFromOffer(String callId, SdpSession offer) {
        Objects.requireNonNull(callId, "callId");
        Objects.requireNonNull(offer, "offer");
        List<SdpMediaDescription> audioMedia = offer.audioMedia();
        List<MrcpResourceChannel> channels = new ArrayList<>();
        for (SdpMediaDescription controlMedia : offer.mrcpControlMedia()) {
            MrcpChannelIdentifier channelIdentifier = channelIdentifier(controlMedia);
            MrcpResourceType resourceType = channelIdentifier.resourceType()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unsupported MRCP resource: " + channelIdentifier.resourceName()));
            channels.add(new MrcpResourceChannel(
                    channelIdentifier,
                    resourceType,
                    controlMedia,
                    audioMedia.isEmpty() ? null : audioMedia.getFirst()));
        }
        if (channels.isEmpty()) {
            throw new IllegalArgumentException("SDP offer does not contain MRCPv2 control media");
        }
        return new MrcpServerSession(sessionId(channels), callId, offer, channels);
    }

    private MrcpChannelIdentifier channelIdentifier(SdpMediaDescription controlMedia) {
        Optional<String> channel = controlMedia.channel();
        if (channel.isPresent()) {
            return MrcpChannelIdentifier.parse(channel.get());
        }
        List<String> resources = controlMedia.resources();
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("MRCP control media must contain resource or channel attribute");
        }
        return new MrcpChannelIdentifier(MrcpSessionId.random().value(), resources.getFirst());
    }

    private MrcpSessionId sessionId(List<MrcpResourceChannel> channels) {
        return new MrcpSessionId(channels.getFirst().channelIdentifier().sessionId());
    }
}
