package com.card.iso8583.codec;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.TreeSet;

public final class BitmapCodec {

    private BitmapCodec() {}

    /**
     * Build bitmap bytes from a set of field numbers.
     * Returns 8 bytes (primary only) or 16 bytes (primary + secondary).
     */
    public static byte[] build(Set<Integer> fieldNumbers) {
        long primary = 0L;
        long secondary = 0L;
        boolean needSecondary = false;

        for (int f : fieldNumbers) {
            if (f < 2 || f > 128) {
                throw new IllegalArgumentException("Field number out of range (must be 2-128): " + f);
            }
            if (f <= 64) {
                primary |= (1L << (64 - f));
            } else {
                secondary |= (1L << (128 - f));
                needSecondary = true;
            }
        }

        if (needSecondary) {
            // Set bit 1 of primary to indicate secondary bitmap
            primary |= (1L << 63);
            byte[] result = new byte[16];
            putLong(result, 0, primary);
            putLong(result, 8, secondary);
            return result;
        } else {
            byte[] result = new byte[8];
            putLong(result, 0, primary);
            return result;
        }
    }

    /**
     * Parse bitmap from ByteBuffer. Reads 8 or 16 bytes depending on bit 1.
     * Returns the set of field numbers present in the bitmap.
     */
    public static Set<Integer> parse(ByteBuffer buf) {
        if (buf.remaining() < 8) {
            throw new BufferUnderflowException();
        }
        long primary = buf.getLong();
        Set<Integer> fields = new TreeSet<>();

        boolean hasSecondary = (primary & (1L << 63)) != 0;

        // Parse primary bitmap (bits 2-64, skip bit 1 which is secondary indicator)
        for (int bit = 2; bit <= 64; bit++) {
            if ((primary & (1L << (64 - bit))) != 0) {
                fields.add(bit);
            }
        }

        if (hasSecondary) {
            if (buf.remaining() < 8) {
                throw new BufferUnderflowException();
            }
            long secondary = buf.getLong();
            for (int bit = 65; bit <= 128; bit++) {
                if ((secondary & (1L << (128 - bit))) != 0) {
                    fields.add(bit);
                }
            }
        }

        return fields;
    }

    private static void putLong(byte[] bytes, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            bytes[offset + i] = (byte) (value >>> (56 - i * 8));
        }
    }
}
