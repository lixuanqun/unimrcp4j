package io.github.javamrcp.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.javamrcp.core.MrcpChannelIdentifier;
import io.github.javamrcp.core.MrcpMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MrcpFrameDecoderTest {
    private final MrcpMessageGenerator generator = new MrcpMessageGenerator();

    @Test
    void waitsForCompleteContentLengthBody() {
        byte[] encoded = generator.generate(MrcpMessage.request("RECOGNIZE", 543257)
                .channelIdentifier(new MrcpChannelIdentifier("32AECB23433801", "speechrecog"))
                .body("application/srgs+xml", "hello")
                .build());

        EmbeddedChannel channel = new EmbeddedChannel(new MrcpFrameDecoder());
        int split = encoded.length - 2;
        channel.writeInbound(Unpooled.wrappedBuffer(encoded, 0, split));
        assertNull(channel.readInbound());

        channel.writeInbound(Unpooled.wrappedBuffer(encoded, split, encoded.length - split));
        ByteBuf frame = channel.readInbound();
        assertNotNull(frame);
        assertEquals(new String(encoded, StandardCharsets.UTF_8), frame.toString(StandardCharsets.UTF_8));
        frame.release();
    }

    @Test
    void emitsMultipleFramesFromOneBuffer() {
        byte[] first = generator.generate(MrcpMessage.request("STOP", 1)
                .channelIdentifier(new MrcpChannelIdentifier("s1", "speechrecog"))
                .build());
        byte[] second = generator.generate(MrcpMessage.request("STOP", 2)
                .channelIdentifier(new MrcpChannelIdentifier("s2", "speechsynth"))
                .build());

        EmbeddedChannel channel = new EmbeddedChannel(new MrcpFrameDecoder());
        channel.writeInbound(Unpooled.wrappedBuffer(first, second));

        ByteBuf firstFrame = channel.readInbound();
        ByteBuf secondFrame = channel.readInbound();
        assertNotNull(firstFrame);
        assertNotNull(secondFrame);
        assertEquals(new String(first, StandardCharsets.UTF_8), firstFrame.toString(StandardCharsets.UTF_8));
        assertEquals(new String(second, StandardCharsets.UTF_8), secondFrame.toString(StandardCharsets.UTF_8));
        firstFrame.release();
        secondFrame.release();
    }
}
