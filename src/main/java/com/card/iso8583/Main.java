package com.card.iso8583;

import com.card.iso8583.codec.IsoMessageCodec;
import com.card.iso8583.model.IsoMessage;
import com.card.iso8583.spec.CardnetSpec;
import com.card.iso8583.tcp.TcpClient;
import com.card.iso8583.tcp.TcpServer;

public class Main {

    public static void main(String[] args) throws Exception {
        var codec = new IsoMessageCodec(CardnetSpec.getInstance());

        System.out.println("=== ISO 8583 Parser Demo ===");

        // --- 1. Authorization Request (0100) ---
        demoPackUnpack(codec, "1. Authorization Request (0100)",
                IsoMessage.builder("0100")
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
                        .build());

        // --- 2. Authorization Response (0110) ---
        demoPackUnpack(codec, "2. Authorization Response (0110)",
                IsoMessage.builder("0110")
                        .setField(2, "4761340000000019")
                        .setField(3, "000000")
                        .setField(4, "000000001000")
                        .setField(11, "000001")
                        .setField(37, "000000000001")
                        .setField(38, "ABC123")
                        .setField(39, "00")
                        .setField(41, "TERM0001")
                        .setField(49, "392")
                        .build());

        // --- 3. Financial Request (0200) ---
        demoPackUnpack(codec, "3. Financial Request (0200)",
                IsoMessage.builder("0200")
                        .setField(2, "5425230000000000")
                        .setField(3, "000000")
                        .setField(4, "000000005000")
                        .setField(7, "0304130000")
                        .setField(11, "000002")
                        .setField(12, "130000")
                        .setField(13, "0304")
                        .setField(14, "2612")
                        .setField(18, "5411")
                        .setField(22, "051")
                        .setField(25, "00")
                        .setField(35, "5425230000000000D26122011000000000")
                        .setField(41, "TERM0002")
                        .setField(42, "MERCHANT000002 ")
                        .setField(43, "SUPERMARKET                     OSAKA JP")
                        .setField(49, "392")
                        .build());

        // --- 4. TCP Round-Trip Demo ---
        System.out.println("--- 4. TCP Round-Trip (0100 -> 0110) ---");
        try (var server = new TcpServer(0, codec, request ->
                IsoMessage.builder("0110")
                        .setField(2, request.getField(2).value())
                        .setField(3, request.getField(3).value())
                        .setField(4, request.getField(4).value())
                        .setField(11, request.getField(11).value())
                        .setField(37, "000000000001")
                        .setField(38, "XYZ789")
                        .setField(39, "00")
                        .setField(41, request.getField(41).value())
                        .setField(49, request.getField(49).value())
                        .build())) {

            server.start();
            server.awaitReady();

            var client = new TcpClient("localhost", server.getPort(), codec);
            IsoMessage tcpRequest = IsoMessage.builder("0100")
                    .setField(2, "4761340000000019")
                    .setField(3, "000000")
                    .setField(4, "000000001000")
                    .setField(11, "000001")
                    .setField(41, "TERM0001")
                    .setField(42, "MERCHANT000001 ")
                    .setField(49, "392")
                    .build();

            System.out.println("  Sent:     " + tcpRequest);
            IsoMessage tcpResponse = client.send(tcpRequest);
            System.out.println("  Received: " + tcpResponse);
            System.out.println("  Auth ID:  " + tcpResponse.getField(38).value());
            System.out.println("  Response: " + tcpResponse.getField(39).value());
        }
        System.out.println();
        System.out.println("Done.");
    }

    private static void demoPackUnpack(IsoMessageCodec codec, String title, IsoMessage message) {
        System.out.println();
        System.out.println("--- " + title + " ---");
        System.out.println("  Message: " + message);

        byte[] packed = codec.pack(message);
        System.out.println("  Packed:  " + packed.length + " bytes");
        System.out.println("  Hex:     " + bytesToHex(packed));

        IsoMessage unpacked = codec.unpack(packed);
        boolean match = message.mti().equals(unpacked.mti())
                && message.fields().size() == unpacked.fields().size();
        System.out.println("  Round-trip: " + (match ? "OK" : "FAILED"));
    }

    private static String bytesToHex(byte[] bytes) {
        var sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0 && i % 32 == 0) sb.append("\n           ");
            else if (i > 0) sb.append(' ');
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }
        return sb.toString();
    }
}
