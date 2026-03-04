package com.card.iso8583.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * 2-byte big-endian length prefix codec for framing ISO messages over TCP.
 */
public final class LengthPrefixCodec {

    /** Maximum allowed message size (8KB). Adjust per deployment requirements. */
    public static final int MAX_MESSAGE_SIZE = 8192;

    private LengthPrefixCodec() {}

    public static void write(OutputStream out, byte[] message) throws IOException {
        int len = message.length;
        if (len > 0xFFFF) {
            throw new IOException("Message too large for 2-byte length prefix: " + len);
        }
        out.write((len >> 8) & 0xFF);
        out.write(len & 0xFF);
        out.write(message);
        out.flush();
    }

    public static byte[] read(InputStream in) throws IOException {
        int high = in.read();
        int low = in.read();
        if (high == -1 || low == -1) {
            throw new IOException("Unexpected end of stream reading length prefix");
        }
        int len = (high << 8) | low;
        if (len <= 0 || len > MAX_MESSAGE_SIZE) {
            throw new IOException("Invalid message length: " + len + " (max: " + MAX_MESSAGE_SIZE + ")");
        }
        byte[] data = new byte[len];
        int offset = 0;
        while (offset < len) {
            int read = in.read(data, offset, len - offset);
            if (read == -1) {
                throw new IOException("Unexpected end of stream reading message body");
            }
            offset += read;
        }
        return data;
    }
}
