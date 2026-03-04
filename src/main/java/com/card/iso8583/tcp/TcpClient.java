package com.card.iso8583.tcp;

import com.card.iso8583.codec.IsoMessageCodec;
import com.card.iso8583.model.IsoMessage;

import java.io.IOException;
import java.net.Socket;

public final class TcpClient implements AutoCloseable {

    private final String host;
    private final int port;
    private final IsoMessageCodec codec;

    public TcpClient(String host, int port, IsoMessageCodec codec) {
        this.host = host;
        this.port = port;
        this.codec = codec;
    }

    private static final int SOCKET_TIMEOUT_MS = 30_000;

    public IsoMessage send(IsoMessage request) throws IOException {
        try (var socket = new Socket(host, port)) {
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
            byte[] packed = codec.pack(request);
            LengthPrefixCodec.write(socket.getOutputStream(), packed);

            byte[] responseData = LengthPrefixCodec.read(socket.getInputStream());
            return codec.unpack(responseData);
        }
    }

    @Override
    public void close() {
        // No persistent connection to close in this implementation
    }
}
