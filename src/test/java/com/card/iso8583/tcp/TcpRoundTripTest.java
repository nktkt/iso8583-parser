package com.card.iso8583.tcp;

import com.card.iso8583.codec.IsoMessageCodec;
import com.card.iso8583.model.IsoMessage;
import com.card.iso8583.spec.CardnetSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TcpRoundTripTest {

    @Test
    void sendRequestReceiveResponse() throws Exception {
        var codec = new IsoMessageCodec(CardnetSpec.getInstance());

        // Server handler: 0100 → 0110 response
        try (var server = new TcpServer(0, codec, request -> {
            assertEquals("0100", request.mti());
            return IsoMessage.builder("0110")
                    .setField(2, request.getField(2).value())
                    .setField(3, request.getField(3).value())
                    .setField(4, request.getField(4).value())
                    .setField(11, request.getField(11).value())
                    .setField(38, "ABC123")
                    .setField(39, "00")
                    .build();
        })) {
            server.start();
            server.awaitReady();

            var client = new TcpClient("localhost", server.getPort(), codec);

            IsoMessage request = IsoMessage.builder("0100")
                    .setField(2, "4761340000000019")
                    .setField(3, "000000")
                    .setField(4, "000000001000")
                    .setField(11, "000001")
                    .setField(41, "TERM0001")
                    .setField(42, "MERCHANT000001 ")
                    .setField(49, "392")
                    .build();

            IsoMessage response = client.send(request);

            assertEquals("0110", response.mti());
            assertEquals("4761340000000019", response.getField(2).value());
            assertEquals("000000001000", response.getField(4).value());
            assertEquals("ABC123", response.getField(38).value());
            assertEquals("00", response.getField(39).value());
        }
    }

    @Test
    void financialRequestResponse() throws Exception {
        var codec = new IsoMessageCodec(CardnetSpec.getInstance());

        try (var server = new TcpServer(0, codec, request -> {
            assertEquals("0200", request.mti());
            return IsoMessage.builder("0210")
                    .setField(2, request.getField(2).value())
                    .setField(3, request.getField(3).value())
                    .setField(4, request.getField(4).value())
                    .setField(11, request.getField(11).value())
                    .setField(38, "DEF456")
                    .setField(39, "00")
                    .build();
        })) {
            server.start();
            server.awaitReady();

            var client = new TcpClient("localhost", server.getPort(), codec);

            IsoMessage request = IsoMessage.builder("0200")
                    .setField(2, "5425230000000000")
                    .setField(3, "000000")
                    .setField(4, "000000002500")
                    .setField(11, "000099")
                    .setField(41, "TERM0002")
                    .setField(42, "MERCHANT000002 ")
                    .setField(49, "392")
                    .build();

            IsoMessage response = client.send(request);

            assertEquals("0210", response.mti());
            assertEquals("DEF456", response.getField(38).value());
            assertEquals("00", response.getField(39).value());
        }
    }
}
