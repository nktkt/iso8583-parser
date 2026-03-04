package com.card.iso8583.codec;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class BcdCodecTest {

    @Test
    void encodeEvenDigits() {
        byte[] result = BcdCodec.encode("1234");
        assertArrayEquals(new byte[]{0x12, 0x34}, result);
    }

    @Test
    void encodeOddDigits() {
        // "123" → pad to "0123" → {0x01, 0x23}
        byte[] result = BcdCodec.encode("123");
        assertArrayEquals(new byte[]{0x01, 0x23}, result);
    }

    @Test
    void encodeMti() {
        // "0100" → {0x01, 0x00}
        byte[] result = BcdCodec.encode("0100");
        assertArrayEquals(new byte[]{0x01, 0x00}, result);
    }

    @Test
    void decodeEvenDigits() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[]{0x12, 0x34});
        assertEquals("1234", BcdCodec.decode(buf, 4));
    }

    @Test
    void decodeOddDigits() {
        // 2 bytes but only want 3 digits
        ByteBuffer buf = ByteBuffer.wrap(new byte[]{0x01, 0x23});
        assertEquals("123", BcdCodec.decode(buf, 3));
    }

    @Test
    void roundTrip() {
        String original = "0100";
        byte[] encoded = BcdCodec.encode(original);
        String decoded = BcdCodec.decode(ByteBuffer.wrap(encoded), 4);
        assertEquals(original, decoded);
    }

    @Test
    void encodeLlvar() {
        // Length 19 → "19" → {0x19}
        byte[] result = BcdCodec.encodeLl(19);
        assertArrayEquals(new byte[]{0x19}, result);
    }

    @Test
    void decodeLlvar() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[]{0x19});
        assertEquals(19, BcdCodec.decodeLl(buf));
    }

    @Test
    void encodeLllvar() {
        // Length 104 → "0104" → {0x01, 0x04}
        byte[] result = BcdCodec.encodeLll(104);
        assertArrayEquals(new byte[]{0x01, 0x04}, result);
    }

    @Test
    void decodeLllvar() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[]{0x01, 0x04});
        assertEquals(104, BcdCodec.decodeLll(buf));
    }

    @Test
    void encodeEmptyString() {
        byte[] result = BcdCodec.encode("");
        assertEquals(0, result.length);
    }

    @Test
    void encodeAmount() {
        // Amount "000000001000" (12 digits) → 6 bytes BCD
        // "00 00 00 00 10 00"
        byte[] result = BcdCodec.encode("000000001000");
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00, 0x10, 0x00}, result);
    }

    @Test
    void encodeRejectsNonDigit() {
        assertThrows(IllegalArgumentException.class, () -> BcdCodec.encode("12AB"));
        assertThrows(IllegalArgumentException.class, () -> BcdCodec.encode("123X"));
    }
}
