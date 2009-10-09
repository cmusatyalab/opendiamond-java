package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Set;

class Connection {

    private static final int DIAMOND_PORT = 5872;

    private static final int NONCE_SIZE = 16;

    final private MiniRPCConnection control;

    final private MiniRPCConnection blast;

    final private String hostname;

    String getHostname() {
        return hostname;
    }

    // all public methods must close() on IOException!

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

    static Connection createConnection(String host, Cookie cookie)
            throws IOException {
        System.out.println("connecting to " + host);

        byte nonce[] = new byte[NONCE_SIZE];

        MiniRPCConnection control;
        MiniRPCConnection blast;

        // open control (if exception is thrown here, it's ok)
        control = new MiniRPCConnection(createOneChannel(new InetSocketAddress(
                host, DIAMOND_PORT), nonce));

        // open data
        try {
            blast = new MiniRPCConnection(createOneChannel(
                    new InetSocketAddress(host, DIAMOND_PORT), nonce));
        } catch (IOException e) {
            try {
                // close control and propagate
                control.close();
            } catch (IOException e2) {
            }
            throw e;
        }

        Connection conn = new Connection(control, blast, host);
        conn.sendCookie(cookie);
        return conn;
    }

    private void sendCookie(Cookie c) throws IOException {
        // clear scope
        new RPC(this, hostname, 4, ByteBuffer.allocate(0)).doRPC()
                .checkStatus();

        // define scope
        ByteBuffer data = XDREncoders.encodeString(c.getCookie());
        new RPC(this, hostname, 24, data).doRPC().checkStatus();
    }

    public void sendPreStart(Set<String> pushAttributes,
            XDR_sig_and_data fspec, List<Filter> filters) throws IOException {
        try {
            // set the push attributes
            if (pushAttributes != null) {
                ByteBuffer encodedAttributes = new XDR_attr_name_list(
                        pushAttributes).encode();
                new RPC(this, hostname, 20, encodedAttributes).doRPC()
                        .checkStatus();
            }

            // set the fspec
            // device_set_spec = 6
            new RPC(this, hostname, 6, fspec.encode()).doRPC().checkStatus();

            // set the codes and blobs
            for (Filter f : filters) {
                setCode(f);
                setBlob(f);
            }
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public void sendStart(int searchID) throws IOException {
        try {
            // start search
            ByteBuffer encodedSearchId = ByteBuffer.allocate(4);
            encodedSearchId.putInt(searchID).flip();

            // device_start_search = 1
            new RPC(this, hostname, 1, encodedSearchId).doRPC().checkStatus();
        } catch (IOException e) {
            close();
            throw e;
        }

    }

    private void setBlob(Filter f) throws IOException {
        byte blobData[] = f.getBlob();
        String name = f.getName();

        XDR_sig_val sig = XDR_sig_val.createSignature(blobData);

        final ByteBuffer encodedBlobSig = new XDR_blob_sig(name, sig).encode();
        final ByteBuffer encodedBlob = new XDR_blob(name, blobData).encode();

        System.out.println("blob sig: " + encodedBlobSig);

        // device_set_blob_by_signature = 22
        MiniRPCReply reply1 = new RPC(this, hostname, 22, encodedBlobSig
                .duplicate()).doRPC();
        if (reply1.getMessage().getStatus() != RPC.DIAMOND_FCACHEMISS) {
            reply1.checkStatus();
            return;
        }

        // device_set_blob = 11
        new RPC(this, hostname, 11, encodedBlob.duplicate()).doRPC()
                .checkStatus();
    }

    private void setCode(Filter f) throws IOException {
        byte code[] = f.getFilterCode().getBytes();
        XDR_sig_val sig = XDR_sig_val.createSignature(code);
        XDR_sig_and_data sigAndData = new XDR_sig_and_data(sig, code);

        final ByteBuffer encodedSig = sig.encode();
        final ByteBuffer encodedSigAndData = sigAndData.encode();

        // device_set_obj = 16
        MiniRPCReply reply1 = new RPC(this, hostname, 16, encodedSig
                .duplicate()).doRPC();
        if (reply1.getMessage().getStatus() != RPC.DIAMOND_FCACHEMISS) {
            reply1.checkStatus();
            return;
        }

        // device_send_obj = 17
        new RPC(this, hostname, 17, encodedSigAndData.duplicate()).doRPC()
                .checkStatus();
    }

    void close() {
        System.out.println("closing " + toString());

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

    private MiniRPCMessage receiveFrom(MiniRPCConnection c) throws IOException {
        try {
            return c.receive();
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public MiniRPCMessage receiveBlast() throws IOException {
        try {
            return receiveFrom(blast);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public void sendMessageBlast(int cmd, ByteBuffer data) throws IOException {
        try {
            blast.sendMessage(cmd, data);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public void sendControlRequest(int cmd, ByteBuffer data) throws IOException {
        try {
            control.sendRequest(cmd, data);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public MiniRPCMessage receiveControl() throws IOException {
        try {
            return control.receive();
        } catch (IOException e) {
            close();
            throw e;
        }
    }
}
