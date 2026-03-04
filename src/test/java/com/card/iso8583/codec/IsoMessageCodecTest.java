package com.card.iso8583.codec;

import com.card.iso8583.model.IsoMessage;
import com.card.iso8583.spec.CardnetSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IsoMessageCodecTest {

    private final IsoMessageCodec codec = new IsoMessageCodec(CardnetSpec.getInstance());

    @Test
    void packUnpackRoundTrip() {
        IsoMessage original = IsoMessage.builder("0100")
                .setField(2, "4761340000000019")
                .setField(3, "000000")
                .setField(4, "000000001000")
                .setField(11, "000001")
                .setField(41, "TERM0001")
                .setField(42, "MERCHANT000001 ")
                .setField(49, "392")
                .build();

        byte[] packed = codec.pack(original);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals(original.mti(), unpacked.mti());
        assertEquals(original.fields().size(), unpacked.fields().size());

        for (var entry : original.fields().entrySet()) {
            assertTrue(unpacked.hasField(entry.getKey()),
                    "Missing field: " + entry.getKey());
            assertEquals(entry.getValue().value(),
                    unpacked.getField(entry.getKey()).value(),
                    "Field " + entry.getKey() + " mismatch");
        }
    }

    @Test
    void packUnpackWithManyFields() {
        IsoMessage original = IsoMessage.builder("0100")
                .setField(2, "4761340000000019")
                .setField(3, "000000")
                .setField(4, "000000001000")
                .setField(7, "0304120000")
                .setField(11, "000001")
                .setField(12, "120000")
                .setField(13, "0304")
                .setField(14, "2512")
                .setField(18, "5999")
                .setField(22, "051")
                .setField(25, "00")
                .setField(35, "4761340000000019D25122011234567890")
                .setField(41, "TERM0001")
                .setField(42, "MERCHANT000001 ")
                .setField(43, "MERCHANT NAME                   TOKYO JP")
                .setField(49, "392")
                .build();

        byte[] packed = codec.pack(original);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals("0100", unpacked.mti());
        assertEquals(original.fields().size(), unpacked.fields().size());

        for (var entry : original.fields().entrySet()) {
            assertEquals(entry.getValue().value(),
                    unpacked.getField(entry.getKey()).value(),
                    "Field " + entry.getKey());
        }
    }

    @Test
    void responseMessage() {
        IsoMessage response = IsoMessage.builder("0110")
                .setField(2, "4761340000000019")
                .setField(3, "000000")
                .setField(4, "000000001000")
                .setField(11, "000001")
                .setField(38, "ABC123")
                .setField(39, "00")
                .build();

        byte[] packed = codec.pack(response);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals("0110", unpacked.mti());
        assertEquals("ABC123", unpacked.getField(38).value());
        assertEquals("00", unpacked.getField(39).value());
    }

    @Test
    void mtiValidation() {
        assertThrows(IllegalArgumentException.class, () -> IsoMessage.builder("01").build());
        assertThrows(IllegalArgumentException.class, () -> IsoMessage.builder("01001").build());
    }
}
