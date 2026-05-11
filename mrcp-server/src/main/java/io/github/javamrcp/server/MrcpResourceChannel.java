package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpResourceType;
import io.github.javamrcp.sdp.SdpMediaDescription;
import java.util.Objects;
import java.util.Optional;

/**
 * A negotiated MRCP resource channel with its control media and associated audio media.
 */
public record MrcpResourceChannel(
        MrcpChannelIdentifier channelIdentifier,
        MrcpResourceType resourceType,
        SdpMediaDescription controlMedia,
        SdpMediaDescription audioMedia) {
    public MrcpResourceChannel {
        Objects.requireNonNull(channelIdentifier, "channelIdentifier");
        Objects.requireNonNull(resourceType, "resourceType");
        Objects.requireNonNull(controlMedia, "controlMedia");
    }

    public Optional<SdpMediaDescription> audioMediaDescription() {
        return Optional.ofNullable(audioMedia);
    }
}
