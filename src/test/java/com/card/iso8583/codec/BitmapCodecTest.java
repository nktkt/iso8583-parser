package com.card.iso8583.codec;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class BitmapCodecTest {

    @Test
    void buildPrimaryOnly() {
        Set<Integer> fields = Set.of(2, 3, 4, 11);
        byte[] bitmap = BitmapCodec.build(fields);
        assertEquals(8, bitmap.length, "Primary bitmap should be 8 bytes");
    }

    @Test
    void buildWithSecondary() {
        Set<Integer> fields = Set.of(2, 3, 70);
        byte[] bitmap = BitmapCodec.build(fields);
        assertEquals(16, bitmap.length, "With secondary bitmap should be 16 bytes");

        // Bit 1 should be set in primary
        assertTrue((bitmap[0] & 0x80) != 0, "Bit 1 should be set for secondary bitmap");
    }

    @Test
    void roundTrip() {
        Set<Integer> original = new TreeSet<>(Set.of(2, 3, 4, 7, 11, 12, 13, 14, 18, 22, 25, 35, 41, 42, 43, 49));
        byte[] bitmap = BitmapCodec.build(original);
        Set<Integer> parsed = BitmapCodec.parse(ByteBuffer.wrap(bitmap));
        assertEquals(original, parsed);
    }

    @Test
    void roundTripWithSecondary() {
        Set<Integer> original = new TreeSet<>(Set.of(2, 3, 4, 11, 70, 90, 100, 120));
        byte[] bitmap = BitmapCodec.build(original);
        Set<Integer> parsed = BitmapCodec.parse(ByteBuffer.wrap(bitmap));
        assertEquals(original, parsed);
    }

    @Test
    void singleField() {
        Set<Integer> fields = Set.of(3);
        byte[] bitmap = BitmapCodec.build(fields);
        Set<Integer> parsed = BitmapCodec.parse(ByteBuffer.wrap(bitmap));
        assertEquals(fields, parsed);
    }

    @Test
    void field64Edge() {
        Set<Integer> fields = Set.of(2, 64);
        byte[] bitmap = BitmapCodec.build(fields);
        assertEquals(8, bitmap.length);
        Set<Integer> parsed = BitmapCodec.parse(ByteBuffer.wrap(bitmap));
        assertEquals(fields, parsed);
    }

    @Test
    void field65Edge() {
        Set<Integer> fields = Set.of(2, 65);
        byte[] bitmap = BitmapCodec.build(fields);
        assertEquals(16, bitmap.length);
        Set<Integer> parsed = BitmapCodec.parse(ByteBuffer.wrap(bitmap));
        assertEquals(fields, parsed);
    }

    @Test
    void invalidFieldNumber() {
        assertThrows(IllegalArgumentException.class, () -> BitmapCodec.build(Set.of(0)));
        assertThrows(IllegalArgumentException.class, () -> BitmapCodec.build(Set.of(1)));
        assertThrows(IllegalArgumentException.class, () -> BitmapCodec.build(Set.of(129)));
    }
}
