package com.card.iso8583.codec;

import com.card.iso8583.model.FieldDefinition;
import com.card.iso8583.model.IsoField;
import com.card.iso8583.model.IsoMessage;
import com.card.iso8583.spec.MessageSpec;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.TreeMap;

public final class IsoMessageCodec {

    private final MessageSpec spec;

    public IsoMessageCodec(MessageSpec spec) {
        this.spec = spec;
    }

    public byte[] pack(IsoMessage message) {
        // MTI: 4 digits → 2 bytes BCD
        byte[] mtiBytes = BcdCodec.encode(message.mti());

        // Bitmap
        Set<Integer> fieldNumbers = message.fields().keySet();
        byte[] bitmapBytes = BitmapCodec.build(fieldNumbers);

        // Fields — first pass to collect encoded bytes, then concatenate
        byte[][] encodedFields = new byte[message.fields().size()][];
        int totalFieldBytes = 0;
        int idx = 0;
        for (var entry : message.fields().entrySet()) {
            int fieldNum = entry.getKey();
            IsoField field = entry.getValue();
            FieldDefinition def = spec.getField(fieldNum);
            if (def == null) {
                throw new IllegalArgumentException("Unknown field: " + fieldNum);
            }
            byte[] encoded = FieldCodec.encode(def, field);
            encodedFields[idx++] = encoded;
            totalFieldBytes += encoded.length;
        }
        byte[] fieldBytes = new byte[totalFieldBytes];
        int offset = 0;
        for (byte[] encoded : encodedFields) {
            System.arraycopy(encoded, 0, fieldBytes, offset, encoded.length);
            offset += encoded.length;
        }

        // Combine all
        byte[] result = new byte[mtiBytes.length + bitmapBytes.length + fieldBytes.length];
        System.arraycopy(mtiBytes, 0, result, 0, mtiBytes.length);
        System.arraycopy(bitmapBytes, 0, result, mtiBytes.length, bitmapBytes.length);
        System.arraycopy(fieldBytes, 0, result, mtiBytes.length + bitmapBytes.length, fieldBytes.length);
        return result;
    }

    public IsoMessage unpack(byte[] data) {
        if (data.length < 10) { // minimum: 2 (MTI) + 8 (bitmap)
            throw new IllegalArgumentException("Message too short: " + data.length + " bytes");
        }
        ByteBuffer buf = ByteBuffer.wrap(data);

        // MTI: 2 bytes BCD → 4 digits
        String mti = BcdCodec.decode(buf, 4);

        // Bitmap
        Set<Integer> fieldNumbers = BitmapCodec.parse(buf);

        // Fields
        TreeMap<Integer, IsoField> fields = new TreeMap<>();
        for (int fieldNum : fieldNumbers) {
            FieldDefinition def = spec.getField(fieldNum);
            if (def == null) {
                throw new IllegalArgumentException("Unknown field in bitmap: " + fieldNum);
            }
            IsoField field = FieldCodec.decode(def, buf);
            fields.put(fieldNum, field);
        }

        return new IsoMessage(mti, fields);
    }
}
