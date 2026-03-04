package com.card.iso8583.tcp;

import com.card.iso8583.codec.IsoMessageCodec;
import com.card.iso8583.model.IsoMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Simple TCP server for testing. Accepts connections and responds using a handler function.
 */
public final class TcpServer implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final IsoMessageCodec codec;
    private final Function<IsoMessage, IsoMessage> handler;
    private final ExecutorService executor;
    private final CountDownLatch readyLatch = new CountDownLatch(1);
    private volatile boolean running = true;

    public TcpServer(int port, IsoMessageCodec codec, Function<IsoMessage, IsoMessage> handler)
            throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.codec = codec;
        this.handler = handler;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void start() {
        executor.submit(() -> {
            readyLatch.countDown();
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    executor.submit(() -> handleClient(client));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Server accept error: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void awaitReady() throws InterruptedException {
        readyLatch.await();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    private static final int CLIENT_TIMEOUT_MS = 30_000;

    private void handleClient(Socket client) {
        try (client) {
            client.setSoTimeout(CLIENT_TIMEOUT_MS);
            byte[] requestData = LengthPrefixCodec.read(client.getInputStream());
            IsoMessage request = codec.unpack(requestData);
            IsoMessage response = handler.apply(request);
            byte[] responseData = codec.pack(response);
            LengthPrefixCodec.write(client.getOutputStream(), responseData);
        } catch (Exception e) {
            System.err.println("Client handler error: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        serverSocket.close();
        executor.shutdown();
    }
}
