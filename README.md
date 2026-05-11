# java-mrcp

Pure Java MRCPv2 toolkit for building FreeSWITCH-compatible speech servers.

## Initial decisions

- Java 21
- Maven multi-module build
- MRCPv2 only; MRCPv1 is intentionally out of scope
- Netty for SIP, MRCP control, and RTP transport layers
- Spring Boot 3.x starter for embedded server integration
- Provider SPI so ASR/TTS vendors such as Volcengine, Aliyun, Tencent, Azure, or local engines can be added independently
- Apache-2.0 license

## Modules

```text
mrcp-core                 MRCP domain model shared by server and client
mrcp-codec                MRCPv2 parser, generator, and Netty Content-Length framing
mrcp-sip                  SIP/2.0 parser, generator, and Netty UDP datagram codecs
mrcp-sdp                  SDP parser/generator for MRCPv2 SIP offer/answer
mrcp-spi                  Provider-neutral ASR/TTS streaming interfaces
mrcp-server               Netty-based MRCPv2 server runtime
mrcp-client               Placeholder for the future MRCPv2 client SDK
mrcp-spring-boot-starter  Spring Boot auto-configuration for embedded server usage
```

## Netty transport scope

The current skeleton binds:

- SIP signaling over UDP, default `0.0.0.0:8060`, with typed SIP datagram decoding
- MRCPv2 control over TCP, default `0.0.0.0:1544`, with Content-Length aware framing and typed message decoding

The next protocol milestones are:

1. SIP INVITE/ACK/BYE/CANCEL transaction handling.
2. FreeSWITCH `mod_unimrcp` offer/answer session orchestration.
3. Resource state machines for `speechrecog` and `speechsynth`.
4. RTP over UDP with PCMU, PCMA, and L16 audio formats.
5. Provider orchestration for mock ASR/TTS and real vendor adapters.

## Spring Boot usage

Add `mrcp-spring-boot-starter` to an application and configure:

```yaml
mrcp:
  server:
    enabled: true
    sip-host: 0.0.0.0
    sip-port: 8060
    mrcp-host: 0.0.0.0
    mrcp-port: 1544
    max-concurrent-sessions: 1000
```

## Build

```bash
mvn test
```
