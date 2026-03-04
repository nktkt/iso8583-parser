package com.card.iso8583.codec;

import com.card.iso8583.model.FieldDefinition;
import com.card.iso8583.model.FieldDefinition.*;
import com.card.iso8583.model.FieldType;
import com.card.iso8583.model.IsoField;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class FieldCodec {

    private FieldCodec() {}

    public static byte[] encode(FieldDefinition def, IsoField field) {
        return switch (def) {
            case Fixed f -> encodeValue(f.type(), field.value(), f.maxLength());
            case LlVar f -> {
                byte[] valueBytes = encodeValue(f.type(), field.value(), field.value().length());
                int len = dataLength(f.type(), field.value(), field.value().length());
                byte[] prefix = BcdCodec.encodeLl(len);
                yield concat(prefix, valueBytes);
            }
            case LllVar f -> {
                byte[] valueBytes = encodeValue(f.type(), field.value(), field.value().length());
                int len = dataLength(f.type(), field.value(), field.value().length());
                byte[] prefix = BcdCodec.encodeLll(len);
                yield concat(prefix, valueBytes);
            }
        };
    }

    public static IsoField decode(FieldDefinition def, ByteBuffer buf) {
        return switch (def) {
            case Fixed f -> {
                checkRemaining(buf, bytesNeeded(f.type(), f.maxLength()), f.fieldNumber());
                String value = decodeValue(f.type(), buf, f.maxLength());
                yield new IsoField(f.fieldNumber(), value);
            }
            case LlVar f -> {
                checkRemaining(buf, 1, f.fieldNumber()); // LL prefix = 1 byte
                int len = BcdCodec.decodeLl(buf);
                validateLength(len, f.maxLength(), f.fieldNumber());
                checkRemaining(buf, bytesNeeded(f.type(), len), f.fieldNumber());
                String value = decodeValue(f.type(), buf, len);
                yield new IsoField(f.fieldNumber(), value);
            }
            case LllVar f -> {
                checkRemaining(buf, 2, f.fieldNumber()); // LLL prefix = 2 bytes
                int len = BcdCodec.decodeLll(buf);
                validateLength(len, f.maxLength(), f.fieldNumber());
                checkRemaining(buf, bytesNeeded(f.type(), len), f.fieldNumber());
                String value = decodeValue(f.type(), buf, len);
                yield new IsoField(f.fieldNumber(), value);
            }
        };
    }

    private static void validateLength(int len, int maxLength, int fieldNumber) {
        if (len < 0 || len > maxLength) {
            throw new IllegalArgumentException(
                    "Field %d: length %d exceeds max %d".formatted(fieldNumber, len, maxLength));
        }
    }

    private static void checkRemaining(ByteBuffer buf, int needed, int fieldNumber) {
        if (buf.remaining() < needed) {
            throw new BufferUnderflowException();
        }
    }

    private static int bytesNeeded(FieldType type, int length) {
        return switch (type) {
            case BCD_NUMERIC, BCD_AMOUNT -> (length + 1) / 2;
            case ALPHA -> length;
            case BINARY -> length;
        };
    }

    // NOTE: For BCD_NUMERIC/BCD_AMOUNT LLVAR/LLLVAR, the length prefix represents
    // the digit count (not byte count). This is consistent with many Japanese card
    // network implementations (e.g., CARDNET). Some ISO 8583 implementations use
    // byte count instead — adjust if interoperating with such systems.
    private static int dataLength(FieldType type, String value, int length) {
        return switch (type) {
            case BCD_NUMERIC, BCD_AMOUNT -> length;
            case ALPHA -> value.length();
            case BINARY -> length;
        };
    }

    private static byte[] encodeValue(FieldType type, String value, int length) {
        return switch (type) {
            case BCD_NUMERIC -> {
                String padded = leftPad(value, length, '0');
                yield BcdCodec.encode(padded);
            }
            case BCD_AMOUNT -> {
                String padded = leftPad(value, length, '0');
                yield BcdCodec.encode(padded);
            }
            case ALPHA -> {
                String padded = rightPad(value, length, ' ');
                yield padded.getBytes(StandardCharsets.ISO_8859_1);
            }
            case BINARY -> {
                yield hexToBytes(value);
            }
        };
    }

    private static String decodeValue(FieldType type, ByteBuffer buf, int length) {
        return switch (type) {
            case BCD_NUMERIC, BCD_AMOUNT -> BcdCodec.decode(buf, length);
            case ALPHA -> {
                byte[] bytes = new byte[length];
                buf.get(bytes);
                yield new String(bytes, StandardCharsets.ISO_8859_1);
            }
            case BINARY -> {
                int byteLen = length;
                byte[] bytes = new byte[byteLen];
                buf.get(bytes);
                yield bytesToHex(bytes);
            }
        };
    }

    private static String leftPad(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return String.valueOf(pad).repeat(length - s.length()) + s;
    }

    private static String rightPad(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return s + String.valueOf(pad).repeat(length - s.length());
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length() / 2;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private static String bytesToHex(byte[] bytes) {
        var sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
