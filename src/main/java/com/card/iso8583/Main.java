package com.card.iso8583;

import com.card.iso8583.codec.IsoMessageCodec;
import com.card.iso8583.model.IsoMessage;
import com.card.iso8583.spec.CardnetSpec;

public class Main {

    public static void main(String[] args) {
        var codec = new IsoMessageCodec(CardnetSpec.getInstance());

        // Build an Authorization Request (0100)
        IsoMessage request = IsoMessage.builder("0100")
                .setField(2, "4761340000000019")    // PAN
                .setField(3, "000000")               // Processing Code
                .setField(4, "000000001000")          // Amount: 10.00
                .setField(7, "0304120000")            // Transmission Date
                .setField(11, "000001")               // STAN
                .setField(12, "120000")               // Local Time
                .setField(13, "0304")                 // Local Date
                .setField(14, "2512")                 // Expiration
                .setField(18, "5999")                 // MCC
                .setField(22, "051")                  // POS Entry Mode
                .setField(25, "00")                   // POS Condition Code
                .setField(35, "4761340000000019D25122011234567890")  // Track 2
                .setField(41, "TERM0001")             // Terminal ID
                .setField(42, "MERCHANT000001 ")      // Merchant ID
                .setField(43, "MERCHANT NAME                   TOKYO JP") // Merchant Name
                .setField(49, "392")                  // Currency Code (JPY)
                .build();

        System.out.println("=== ISO 8583 Parser Demo ===");
        System.out.println();
        System.out.println("Original message:");
        System.out.println(request);
        System.out.println();

        // Pack
        byte[] packed = codec.pack(request);
        System.out.println("Packed (" + packed.length + " bytes):");
        System.out.println(bytesToHex(packed));
        System.out.println();

        // Unpack
        IsoMessage unpacked = codec.unpack(packed);
        System.out.println("Unpacked message:");
        System.out.println(unpacked);
        System.out.println();

        // Verify round-trip
        boolean match = request.mti().equals(unpacked.mti())
                && request.fields().size() == unpacked.fields().size();
        System.out.println("Round-trip match: " + match);
    }

    private static String bytesToHex(byte[] bytes) {
        var sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0 && i % 16 == 0) sb.append('\n');
            else if (i > 0) sb.append(' ');
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }
        return sb.toString();
    }
}
