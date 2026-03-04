package com.card.iso8583.codec;

import java.nio.ByteBuffer;

public final class BcdCodec {

    private BcdCodec() {}

    /**
     * Encode a numeric string to BCD bytes.
     * Odd-length strings are left-padded with '0'.
     * Example: "123" → pad to "0123" → {0x01, 0x23}
     */
    public static byte[] encode(String digits) {
        if (digits.isEmpty()) return new byte[0];
        for (int i = 0; i < digits.length(); i++) {
            if (Character.digit(digits.charAt(i), 10) < 0) {
                throw new IllegalArgumentException(
                        "Non-digit character at position " + i + ": '" + digits.charAt(i) + "'");
            }
        }
        String padded = (digits.length() % 2 != 0) ? "0" + digits : digits;
        byte[] result = new byte[padded.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int high = Character.digit(padded.charAt(i * 2), 10);
            int low = Character.digit(padded.charAt(i * 2 + 1), 10);
            result[i] = (byte) ((high << 4) | low);
        }
        return result;
    }

    /**
     * Decode BCD bytes to a numeric string of the given length.
     * If the BCD contains more digits than needed, the leading digits are trimmed.
     */
    public static String decode(ByteBuffer buf, int digitCount) {
        int byteCount = (digitCount + 1) / 2;
        var sb = new StringBuilder(byteCount * 2);
        for (int i = 0; i < byteCount; i++) {
            int b = buf.get() & 0xFF;
            sb.append(Character.forDigit(b >> 4, 10));
            sb.append(Character.forDigit(b & 0x0F, 10));
        }
        // If odd digit count, strip leading zero
        String full = sb.toString();
        return full.substring(full.length() - digitCount);
    }

    /**
     * Encode a BCD length prefix for LLVAR (2 digits → 1 byte).
     */
    public static byte[] encodeLl(int length) {
        return encode(String.format("%02d", length));
    }

    /**
     * Decode a LLVAR BCD length prefix (1 byte → int).
     */
    public static int decodeLl(ByteBuffer buf) {
        return Integer.parseInt(decode(buf, 2));
    }

    /**
     * Encode a BCD length prefix for LLLVAR (3 digits → 2 bytes).
     */
    public static byte[] encodeLll(int length) {
        return encode(String.format("%04d", length));
    }

    /**
     * Decode a LLLVAR BCD length prefix (2 bytes → int).
     */
    public static int decodeLll(ByteBuffer buf) {
        return Integer.parseInt(decode(buf, 4));
    }
}
