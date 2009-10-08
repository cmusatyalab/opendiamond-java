package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Connection {

    private static final int DIAMOND_PORT = 5872;

    private static final int NONCE_SIZE = 16;

    final private MiniRPCConnection control;

    final private MiniRPCConnection blast;

    final private String hostname;

    String getHostname() {
        return hostname;
    }

    private static SocketChannel createOneChannel(InetSocketAddress address,
            byte nonce[]) throws IOException {
        if (nonce.length != NONCE_SIZE) {
            throw new IllegalArgumentException("nonce[] must be NONCE_SIZE ("
                    + NONCE_SIZE + "), actual size " + nonce.length);
        }

        SocketChannel sc = SocketChannel.open(address);

        ByteBuffer buf = ByteBuffer.wrap(nonce);

        int size;

        // write nonce
        // System.out.println("writing " + Arrays.toString(nonce));
        size = sc.write(buf);
        if (size != NONCE_SIZE) {
            throw new IOException("Could not write nonce, size: " + size);
        }
        // read nonce
        buf.clear();

        size = 0;
        do {
            size += sc.read(buf);
        } while (size != NONCE_SIZE);

        // System.out.println("read " + Arrays.toString(nonce));

        return sc;
    }

    Connection(MiniRPCConnection control, MiniRPCConnection blast,
            String hostname) {
        this.control = control;
        this.blast = blast;
        this.hostname = hostname;
    }

    static Connection createConnection(String host) throws IOException {
        System.out.println("connecting to " + host);

        byte nonce[] = new byte[NONCE_SIZE];

        // open control
        MiniRPCConnection control = new MiniRPCConnection(createOneChannel(
                new InetSocketAddress(host, DIAMOND_PORT), nonce));

        // open data
        MiniRPCConnection blast = new MiniRPCConnection(createOneChannel(
                new InetSocketAddress(host, DIAMOND_PORT), nonce));

        return new Connection(control, blast, host);
    }

    public void sendCookie(Cookie c) throws IOException {
        // clear scope
        new RPC(getControlConnection(), hostname, 4, ByteBuffer.allocate(0))
                .doRPC().checkStatus();

        // define scope
        ByteBuffer data = XDREncoders.encodeString(c.getCookie());
        new RPC(getControlConnection(), hostname, 24, data).doRPC()
                .checkStatus();
    }

    void close() {
        try {
            control.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            blast.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    MiniRPCConnection getControlConnection() {
        return control;
    }

    MiniRPCConnection getDataConnection() {
        return blast;
    }
}
