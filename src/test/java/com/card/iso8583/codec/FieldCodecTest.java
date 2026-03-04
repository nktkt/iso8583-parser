package com.card.iso8583.codec;

import com.card.iso8583.model.FieldDefinition;
import com.card.iso8583.model.FieldDefinition.*;
import com.card.iso8583.model.FieldType;
import com.card.iso8583.model.IsoField;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class FieldCodecTest {

    @Test
    void fixedBcdNumeric() {
        var def = new Fixed(3, "Processing Code", FieldType.BCD_NUMERIC, 6);
        var field = new IsoField(3, "000000");

        byte[] encoded = FieldCodec.encode(def, field);
        assertEquals(3, encoded.length); // 6 digits → 3 BCD bytes

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("000000", decoded.value());
    }

    @Test
    void fixedBcdAmount() {
        var def = new Fixed(4, "Amount", FieldType.BCD_AMOUNT, 12);
        var field = new IsoField(4, "000000001000");

        byte[] encoded = FieldCodec.encode(def, field);
        assertEquals(6, encoded.length); // 12 digits → 6 BCD bytes

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("000000001000", decoded.value());
    }

    @Test
    void fixedAlpha() {
        var def = new Fixed(41, "Terminal ID", FieldType.ALPHA, 8);
        var field = new IsoField(41, "TERM0001");

        byte[] encoded = FieldCodec.encode(def, field);
        assertEquals(8, encoded.length);

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("TERM0001", decoded.value());
    }

    @Test
    void fixedAlphaWithPadding() {
        var def = new Fixed(41, "Terminal ID", FieldType.ALPHA, 8);
        var field = new IsoField(41, "TERM");

        byte[] encoded = FieldCodec.encode(def, field);
        assertEquals(8, encoded.length); // Should be right-padded with spaces

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("TERM    ", decoded.value()); // Spaces preserved on decode
    }

    @Test
    void llvarBcdNumeric() {
        var def = new LlVar(2, "PAN", FieldType.BCD_NUMERIC, 19);
        var field = new IsoField(2, "4761340000000019");

        byte[] encoded = FieldCodec.encode(def, field);
        // 1 byte LL (BCD "16") + 8 bytes BCD data
        assertEquals(9, encoded.length);
        assertEquals(0x16, encoded[0] & 0xFF); // Length 16 in BCD

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("4761340000000019", decoded.value());
    }

    @Test
    void llvarAlpha() {
        var def = new LlVar(44, "Additional Response Data", FieldType.ALPHA, 25);
        var field = new IsoField(44, "APPROVED");

        byte[] encoded = FieldCodec.encode(def, field);
        // 1 byte LL (BCD "08") + 8 bytes ALPHA data
        assertEquals(9, encoded.length);
        assertEquals(0x08, encoded[0] & 0xFF);

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("APPROVED", decoded.value());
    }

    @Test
    void lllvarAlpha() {
        var def = new LllVar(48, "Additional Data Private", FieldType.ALPHA, 999);
        var field = new IsoField(48, "TEST DATA");

        byte[] encoded = FieldCodec.encode(def, field);
        // 2 bytes LLL (BCD "0009") + 9 bytes ALPHA data
        assertEquals(11, encoded.length);

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("TEST DATA", decoded.value());
    }

    @Test
    void fixedBinary() {
        var def = new Fixed(52, "PIN Data", FieldType.BINARY, 8);
        var field = new IsoField(52, "0123456789ABCDEF");

        byte[] encoded = FieldCodec.encode(def, field);
        assertEquals(8, encoded.length);

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("0123456789ABCDEF", decoded.value());
    }

    @Test
    void llvarOddLengthPan() {
        var def = new LlVar(2, "PAN", FieldType.BCD_NUMERIC, 19);
        var field = new IsoField(2, "4761340000000019123");

        byte[] encoded = FieldCodec.encode(def, field);
        // Length 19 → BCD 0x19, data 19 digits → 10 BCD bytes
        assertEquals(0x19, encoded[0] & 0xFF);

        IsoField decoded = FieldCodec.decode(def, ByteBuffer.wrap(encoded));
        assertEquals("4761340000000019123", decoded.value());
    }
}
