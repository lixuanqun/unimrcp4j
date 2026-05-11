# FreeSWITCH integration smoke tests

This directory documents the current manual interop path for FreeSWITCH
`mod_unimrcp`.

## Current supported smoke path

The Java server currently supports the signaling setup path:

```text
SIP OPTIONS -> 200 OK
SIP INVITE with SDP offer -> 200 OK with SDP answer
SIP ACK -> session established
SIP BYE/CANCEL -> 200 OK and session cleanup
```

RTP media and provider-backed MRCP completion are implemented as library
primitives and mock providers, but a full FreeSWITCH audio call flow is still a
later integration milestone.

## FreeSWITCH profile

Use the example profile at:

```text
examples/freeswitch/unimrcp-java-mrcpserver.xml
```

Adjust:

- `server-ip`
- `server-port`
- `client-ip`
- RTP range

to match the machine running the Java MRCP server.

## SIP OPTIONS smoke test

Start an application that embeds `mrcp-spring-boot-starter`, then run:

```bash
python3 scripts/sip-options-smoke-test.py --host 127.0.0.1 --port 8060
```

Expected response:

```text
SIP/2.0 200 OK
```

## Next integration checks

1. Add a Spring Boot example application that starts the embedded server.
2. Add an INVITE/SDP smoke script.
3. Add RTP loopback checks with mock ASR/TTS providers.
4. Add a FreeSWITCH dialplan example for `detect_speech` and `speak`.
