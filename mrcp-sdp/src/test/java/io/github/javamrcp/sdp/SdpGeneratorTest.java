package io.github.javamrcp.sdp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SdpGeneratorTest {
    private final SdpGenerator generator = new SdpGenerator();
    private final SdpParser parser = new SdpParser();

    @Test
    void generatesFreeSwitchCompatibleMrcpOfferShape() {
        SdpSession session = SdpSession.builder(SdpOrigin.ip4("java-mrcp", 1, 1, "127.0.0.1"))
                .connection(SdpConnection.ip4("127.0.0.1"))
                .media(SdpMediaDescription.builder(SdpMediaType.APPLICATION, 9, "TCP/MRCPv2")
                        .format("1")
                        .attribute("setup", "passive")
                        .attribute("connection", "new")
                        .attribute("resource", "speechrecog")
                        .attribute("channel", "32AECB23433801@speechrecog")
                        .build())
                .media(SdpMediaDescription.builder(SdpMediaType.AUDIO, 4000, "RTP/AVP")
                        .format("0")
                        .format("8")
                        .format("96")
                        .attribute("rtpmap", "0 PCMU/8000")
                        .attribute("rtpmap", "8 PCMA/8000")
                        .attribute("rtpmap", "96 L16/16000/1")
                        .attribute("sendrecv")
                        .build())
                .build();

        String sdp = generator.generate(session);
        SdpSession parsed = parser.parse(sdp);

        assertEquals("v=0\r\n"
                + "o=java-mrcp 1 1 IN IP4 127.0.0.1\r\n"
                + "s=-\r\n"
                + "c=IN IP4 127.0.0.1\r\n"
                + "t=0 0\r\n"
                + "m=application 9 TCP/MRCPv2 1\r\n"
                + "a=setup:passive\r\n"
                + "a=connection:new\r\n"
                + "a=resource:speechrecog\r\n"
                + "a=channel:32AECB23433801@speechrecog\r\n"
                + "m=audio 4000 RTP/AVP 0 8 96\r\n"
                + "a=rtpmap:0 PCMU/8000\r\n"
                + "a=rtpmap:8 PCMA/8000\r\n"
                + "a=rtpmap:96 L16/16000/1\r\n"
                + "a=sendrecv\r\n", sdp);
        assertEquals("speechrecog", parsed.mrcpControlMedia().getFirst().resources().getFirst());
        assertEquals(4000, parsed.audioMedia().getFirst().port());
    }

    @Test
    void roundTripsMediaLevelConnection() {
        SdpSession original = SdpSession.builder(SdpOrigin.ip4("-", 2, 2, "192.0.2.1"))
                .media(SdpMediaDescription.builder(SdpMediaType.AUDIO, 5000, "RTP/AVP")
                        .format("0")
                        .connection(SdpConnection.ip4("192.0.2.2"))
                        .attribute("recvonly")
                        .build())
                .build();

        SdpSession parsed = parser.parse(generator.generate(original));

        assertEquals("192.0.2.2", parsed.audioMedia().getFirst().connection().orElseThrow().address());
        assertEquals("recvonly", parsed.audioMedia().getFirst().attributes().getFirst().name());
    }
}
