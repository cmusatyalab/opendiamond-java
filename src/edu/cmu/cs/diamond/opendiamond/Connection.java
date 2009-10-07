package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class Connection {

    private static final int DIAMOND_PORT = 5872;

    private static final int NONCE_SIZE = 16;

    final private MiniRPCConnection controlSocket;

    final private MiniRPCConnection dataSocket;

    final private String hostname;

    public String getHostname() {
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

        size = sc.read(buf);
        if (size != NONCE_SIZE) {
            throw new IOException("Could not read nonce, size: " + size);
        }
        // System.out.println("read " + Arrays.toString(nonce));

        return sc;
    }

    public Connection(String host) throws IOException {
        byte nonce[] = new byte[NONCE_SIZE];

        this.hostname = host;

        // open control
        controlSocket = new MiniRPCConnection(createOneConnection(
                new InetSocketAddress(host, DIAMOND_PORT), nonce));

        // open data
        dataSocket = new MiniRPCConnection(createOneConnection(
                new InetSocketAddress(host, DIAMOND_PORT), nonce));
    }

    public void close() {
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

    public static void main(String[] args) {
        try {
            ConnectionAggregate ca = new ConnectionAggregate();

            for (String s : args) {
                ca.add(new Connection(s));
            }

            // send some messages
            requestCharactistics(ca);
            requestCharactistics(ca);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void requestCharactistics(ConnectionAggregate ca)
            throws InterruptedException, ExecutionException {
        CompletionService<MiniRPCReply> cs = ca.sendToAllControlChannels(
                MiniRPCMessage.MINIRPC_PENDING, 14, new byte[0]);
        for (int i = 0; i < ca.size(); i++) {
            Future<MiniRPCReply> f = cs.take();
            MiniRPCReply mrr = f.get();
            System.out.println(mrr.getHostname() + ": "
                    + new XDR_dev_char(mrr.getMessage().getData()));
        }
    }

    MiniRPCConnection getControlConnection() {
        return controlSocket;
    }

    MiniRPCConnection getDataConnection() {
        return dataSocket;
    }
}
