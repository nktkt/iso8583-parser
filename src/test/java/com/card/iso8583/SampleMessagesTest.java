package com.card.iso8583;

import com.card.iso8583.codec.IsoMessageCodec;
import com.card.iso8583.model.IsoMessage;
import com.card.iso8583.spec.CardnetSpec;
import org.junit.jupiter.api.Test;

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
                .setField(38, "ABC123")
                .setField(39, "00")
                .build();

        byte[] packed = codec.pack(msg);
        IsoMessage unpacked = codec.unpack(packed);

        assertEquals("0110", unpacked.mti());
        assertEquals("ABC123", unpacked.getField(38).value());
        assertEquals("00", unpacked.getField(39).value());
    }

    @Test
    void financialRequest0200() {
        IsoMessage msg = IsoMessage.builder("0200")
                .setField(2, "4761340000000019")
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
                .setField(35, "4761340000000019D25122011234567890")
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
    void packedBytesAreCorrectForMti() {
        IsoMessage msg = IsoMessage.builder("0100")
                .setField(3, "000000")
                .build();

        byte[] packed = codec.pack(msg);
        // First 2 bytes should be BCD-encoded MTI "0100" → 0x01, 0x00
        assertEquals(0x01, packed[0] & 0xFF);
        assertEquals(0x00, packed[1] & 0xFF);
    }
}
