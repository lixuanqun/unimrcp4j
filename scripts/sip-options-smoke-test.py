#!/usr/bin/env python3
"""Send a SIP OPTIONS datagram to a java-mrcp server and print the response."""

import argparse
import socket
import time


def build_options(host: str, port: int) -> bytes:
    branch = f"z9hG4bK-java-mrcp-smoke-{int(time.time())}"
    message = (
        f"OPTIONS sip:mrcp@{host}:{port} SIP/2.0\r\n"
        f"Via:SIP/2.0/UDP 127.0.0.1:0;branch={branch}\r\n"
        "From:<sip:smoke@127.0.0.1>;tag=smoke\r\n"
        f"To:<sip:mrcp@{host}:{port}>\r\n"
        f"Call-ID:java-mrcp-smoke-{int(time.time())}\r\n"
        "CSeq:1 OPTIONS\r\n"
        "Max-Forwards:70\r\n"
        "Content-Length:0\r\n"
        "\r\n"
    )
    return message.encode("ascii")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8060)
    parser.add_argument("--timeout", type=float, default=3.0)
    args = parser.parse_args()

    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
        sock.settimeout(args.timeout)
        sock.sendto(build_options(args.host, args.port), (args.host, args.port))
        data, address = sock.recvfrom(8192)
        print(f"Response from {address[0]}:{address[1]}")
        print(data.decode("ascii", errors="replace"))
        return 0 if data.startswith(b"SIP/2.0 200") else 1


if __name__ == "__main__":
    raise SystemExit(main())
