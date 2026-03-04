package com.card.iso8583;

import com.card.iso8583.codec.IsoMessageCodec;
import com.card.iso8583.model.IsoMessage;
import com.card.iso8583.spec.CardnetSpec;
import org.junit.jupiter.api.Test;

import java.nio.BufferUnderflowException;

import static org.junit.jupiter.api.Assertions.*;

class SampleMessagesTest {

    private final IsoMessageCodec codec = new IsoMessageCodec(CardnetSpec.getInstance());

    @Test
    void authorizationRequest0100() {
        IsoMessage msg = IsoMessage.builder("0100")
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

        byte[] packed = codec.pack(msg);
        assertNotNull(packed);
        assertTrue(packed.length > 10);

        IsoMessage unpacked = codec.unpack(packed);
        assertEquals("0100", unpacked.mti());
        assertEquals("4761340000000019", unpacked.getField(2).value());
        assertEquals("000000001000", unpacked.getField(4).value());
        assertEquals("TERM0001", unpacked.getField(41).value());
        assertEquals("392", unpacked.getField(49).value());
    }

    @Test
    void authorizationResponse0110() {
        IsoMessage msg = IsoMessage.builder("0110")
                .setField(2, "4761340000000019")
                .setField(3, "000000")
                .setField(4, "000000001000")
                .setField(11, "000001")
                .setField(37, "000000000001")
                .setField(38, "ABC123")
                .setField(39, "00")
                .setField(41, "TERM0001")
                .setField(49, "392")
                .build();

        byte[] packed = codec.pack(msg);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals("0110", unpacked.mti());
        assertEquals("4761340000000019", unpacked.getField(2).value());
        assertEquals("000000000001", unpacked.getField(37).value());
        assertEquals("ABC123", unpacked.getField(38).value());
        assertEquals("00", unpacked.getField(39).value());
        assertEquals("TERM0001", unpacked.getField(41).value());
    }

    @Test
    void financialRequest0200() {
        IsoMessage msg = IsoMessage.builder("0200")
                .setField(2, "5425230000000000")
                .setField(3, "000000")
                .setField(4, "000000005000")
                .setField(7, "0304130000")
                .setField(11, "000002")
                .setField(12, "130000")
                .setField(13, "0304")
                .setField(14, "2512")
                .setField(18, "5999")
                .setField(22, "051")
                .setField(25, "00")
                .setField(35, "5425230000000000D25122011000000000")
                .setField(41, "TERM0001")
                .setField(42, "MERCHANT000001 ")
                .setField(43, "MERCHANT NAME                   TOKYO JP")
                .setField(49, "392")
                .build();

        byte[] packed = codec.pack(msg);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals("0200", unpacked.mti());
        assertEquals("000000005000", unpacked.getField(4).value());
        assertEquals("000002", unpacked.getField(11).value());
    }

    @Test
    void financialResponse0210() {
        IsoMessage msg = IsoMessage.builder("0210")
                .setField(2, "5425230000000000")
                .setField(3, "000000")
                .setField(4, "000000005000")
                .setField(11, "000002")
                .setField(37, "000000000002")
                .setField(38, "DEF456")
                .setField(39, "00")
                .setField(41, "TERM0001")
                .setField(49, "392")
                .build();

        byte[] packed = codec.pack(msg);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals("0210", unpacked.mti());
        assertEquals("5425230000000000", unpacked.getField(2).value());
        assertEquals("DEF456", unpacked.getField(38).value());
        assertEquals("00", unpacked.getField(39).value());
    }

    @Test
    void reversalRequest0400() {
        IsoMessage msg = IsoMessage.builder("0400")
                .setField(2, "4761340000000019")
                .setField(3, "000000")
                .setField(4, "000000001000")
                .setField(7, "0304120000")
                .setField(11, "000003")
                .setField(12, "120000")
                .setField(13, "0304")
                .setField(25, "00")
                .setField(37, "000000000001")
                .setField(38, "ABC123")
                .setField(41, "TERM0001")
                .setField(42, "MERCHANT000001 ")
                .setField(49, "392")
                .setField(90, "010000000103041200000000000000000000000000")
                .build();

        byte[] packed = codec.pack(msg);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals("0400", unpacked.mti());
        assertTrue(unpacked.hasField(90));
        assertEquals("010000000103041200000000000000000000000000",
                unpacked.getField(90).value());
    }

    @Test
    void networkManagement0800() {
        IsoMessage msg = IsoMessage.builder("0800")
                .setField(7, "0304120000")
                .setField(11, "000099")
                .setField(70, "001")
                .build();

        byte[] packed = codec.pack(msg);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals("0800", unpacked.mti());
        assertEquals("001", unpacked.getField(70).value());
    }

    @Test
    void packedBytesAreCorrectForMti() {
        IsoMessage msg = IsoMessage.builder("0100")
                .setField(3, "000000")
                .build();

        byte[] packed = codec.pack(msg);
        assertEquals(0x01, packed[0] & 0xFF);
        assertEquals(0x00, packed[1] & 0xFF);
    }

    // --- Validation tests ---

    @Test
    void unpackTooShortMessage() {
        assertThrows(IllegalArgumentException.class,
                () -> codec.unpack(new byte[]{0x01, 0x00}));
    }

    @Test
    void unpackTruncatedFieldData() {
        IsoMessage msg = IsoMessage.builder("0100")
                .setField(2, "4761340000000019")
                .setField(3, "000000")
                .setField(4, "000000001000")
                .build();
        byte[] packed = codec.pack(msg);

        // Truncate the packed data — remove last 3 bytes
        byte[] truncated = new byte[packed.length - 3];
        System.arraycopy(packed, 0, truncated, 0, truncated.length);

        assertThrows(BufferUnderflowException.class,
                () -> codec.unpack(truncated));
    }

    @Test
    void allFieldsRoundTrip() {
        // Message with fields spanning both primary and secondary bitmap
        IsoMessage msg = IsoMessage.builder("0100")
                .setField(2, "4761340000000019")
                .setField(3, "000000")
                .setField(4, "000000001000")
                .setField(11, "000001")
                .setField(41, "TERM0001")
                .setField(42, "MERCHANT000001 ")
                .setField(49, "392")
                .setField(70, "001")
                .setField(102, "1234567890")
                .build();

        byte[] packed = codec.pack(msg);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals(msg.fields().size(), unpacked.fields().size());
        for (var entry : msg.fields().entrySet()) {
            assertEquals(entry.getValue().value(),
                    unpacked.getField(entry.getKey()).value(),
                    "Field " + entry.getKey());
        }
    }
}
