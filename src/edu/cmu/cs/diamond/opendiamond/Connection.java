package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Connection {

    private static final int DIAMOND_PORT = 5872;

    private static final int NONCE_SIZE = 16;

    final private MiniRPCConnection controlSocket;

    final private MiniRPCConnection dataSocket;

    final private String hostname;

    String getHostname() {
        return hostname;
    }

    private static SocketChannel createOneConnection(InetSocketAddress address,
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

    Connection(String host) throws IOException {
        System.out.println("connecting to " + host);

        byte nonce[] = new byte[NONCE_SIZE];

        this.hostname = host;

        // open control
        controlSocket = new MiniRPCConnection(createOneConnection(
                new InetSocketAddress(host, DIAMOND_PORT), nonce));

        // open data
        dataSocket = new MiniRPCConnection(createOneConnection(
                new InetSocketAddress(host, DIAMOND_PORT), nonce));
    }

    public void sendCookie(Cookie c) throws IOException {
        // clear scope
        ConnectionSet.checkStatus(new RPC(getControlConnection(), hostname, 4,
                ByteBuffer.allocate(0)).doRPC());

        // define scope
        ByteBuffer data = XDREncoders.encodeString(c.getCookie());
        ConnectionSet.checkStatus(new RPC(getControlConnection(), hostname, 24,
                data).doRPC());
    }

    void close() {
        try {
            controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    MiniRPCConnection getControlConnection() {
        return controlSocket;
    }

    MiniRPCConnection getDataConnection() {
        return dataSocket;
    }
}
