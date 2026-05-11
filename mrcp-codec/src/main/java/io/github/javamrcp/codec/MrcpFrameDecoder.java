package io.github.javamrcp.codec;

import io.github.javamrcp.core.MrcpMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Extracts complete MRCP frames from a TCP byte stream using header terminator and Content-Length.
 */
public final class MrcpFrameDecoder extends ByteToMessageDecoder {
    private static final byte CARRIAGE_RETURN = '\r';
    private static final byte LINE_FEED = '\n';

    private final int maxHeaderSize;
    private final int maxBodySize;

    public MrcpFrameDecoder() {
        this(8 * 1024, 4 * 1024 * 1024);
    }

    public MrcpFrameDecoder(int maxHeaderSize, int maxBodySize) {
        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException("maxHeaderSize must be positive");
        }
        if (maxBodySize < 0) {
            throw new IllegalArgumentException("maxBodySize must not be negative");
        }
        this.maxHeaderSize = maxHeaderSize;
        this.maxBodySize = maxBodySize;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf input, List<Object> output) {
        HeaderBoundary boundary = findHeaderBoundary(input);
        if (boundary == null) {
            if (input.readableBytes() > maxHeaderSize) {
                throw new CorruptedFrameException("MRCP header exceeds " + maxHeaderSize + " bytes");
            }
            return;
        }

        int headerLength = boundary.separatorStart() - input.readerIndex();
        if (headerLength > maxHeaderSize) {
            throw new CorruptedFrameException("MRCP header exceeds " + maxHeaderSize + " bytes");
        }

        int contentLength = contentLength(input, headerLength);
        if (contentLength > maxBodySize) {
            throw new CorruptedFrameException("MRCP body exceeds " + maxBodySize + " bytes");
        }

        int frameLength = headerLength + boundary.separatorLength() + contentLength;
        if (input.readableBytes() < frameLength) {
            return;
        }

        output.add(input.readRetainedSlice(frameLength));
    }

    private HeaderBoundary findHeaderBoundary(ByteBuf input) {
        int start = input.readerIndex();
        int end = input.writerIndex();
        for (int index = start; index < end - 1; index++) {
            if (index < end - 3
                    && input.getByte(index) == CARRIAGE_RETURN
                    && input.getByte(index + 1) == LINE_FEED
                    && input.getByte(index + 2) == CARRIAGE_RETURN
                    && input.getByte(index + 3) == LINE_FEED) {
                return new HeaderBoundary(index, 4);
            }
            if (input.getByte(index) == LINE_FEED && input.getByte(index + 1) == LINE_FEED) {
                return new HeaderBoundary(index, 2);
            }
        }
        return null;
    }

    private int contentLength(ByteBuf input, int headerLength) {
        String headerSection = input.toString(input.readerIndex(), headerLength, StandardCharsets.US_ASCII);
        String[] lines = headerSection.split("\\r?\\n");
        int contentLength = 0;
        boolean seen = false;
        for (int index = 1; index < lines.length; index++) {
            String line = lines[index];
            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String name = line.substring(0, separator).trim();
            if (MrcpMessage.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                if (seen) {
                    throw new CorruptedFrameException("Duplicate Content-Length header");
                }
                String value = line.substring(separator + 1).trim();
                try {
                    contentLength = Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    throw new CorruptedFrameException("Invalid Content-Length: " + value, ex);
                }
                if (contentLength < 0) {
                    throw new CorruptedFrameException("Content-Length must not be negative");
                }
                seen = true;
            }
        }
        if (!lines[0].toUpperCase(Locale.ROOT).startsWith("MRCP/2.0 ")) {
            throw new CorruptedFrameException("Unsupported MRCP start line: " + lines[0]);
        }
        return contentLength;
    }

    private record HeaderBoundary(int separatorStart, int separatorLength) {
    }
}
