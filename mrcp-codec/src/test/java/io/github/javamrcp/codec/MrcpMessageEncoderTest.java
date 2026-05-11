package io.github.javamrcp.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MrcpMessageEncoderTest {
    @Test
    void encodesOutboundMessages() {
        EmbeddedChannel channel = new EmbeddedChannel(new MrcpMessageEncoder());
        MrcpMessage message = MrcpMessage.request("STOP", 7)
                .channelIdentifier(new MrcpChannelIdentifier("s1", "speechrecog"))
                .build();

        channel.writeOutbound(message);
        ByteBuf encoded = channel.readOutbound();

        assertNotNull(encoded);
        String wireMessage = encoded.toString(StandardCharsets.US_ASCII);
        assertEquals("MRCP/2.0 " + encoded.readableBytes() + " STOP 7\r\n"
                + "Channel-Identifier:s1@speechrecog\r\n"
                + "\r\n", wireMessage);
        encoded.release();
    }
}
