/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
 *
 *  Copyright (c) 2009 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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

    private static Socket createOneChannel(String address, byte nonce[])
            throws IOException {
        if (nonce.length != NONCE_SIZE) {
            throw new IllegalArgumentException("nonce[] must be NONCE_SIZE ("
                    + NONCE_SIZE + "), actual size " + nonce.length);
        }

        Socket socket = new Socket(address, DIAMOND_PORT);
        // System.out.println(address);
        // System.out.println(socket);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // write nonce
        // System.out.println("writing " + Arrays.toString(nonce));
        out.write(nonce);

        // read nonce
        in.readFully(nonce);

        // System.out.println("read " + Arrays.toString(nonce));

        return socket;
    }

    Connection(MiniRPCConnection control, MiniRPCConnection blast,
            String hostname) {
        this.control = control;
        this.blast = blast;
        this.hostname = hostname;
    }

    static Connection createConnection(String host, List<Cookie> cookieList,
            Set<String> pushAttributes, XDR_sig_and_data fspec,
            List<Filter> filters) throws IOException {
        // System.out.println("connecting to " + host);

        byte nonce[] = new byte[NONCE_SIZE];

        MiniRPCConnection control;
        MiniRPCConnection blast;

        // open control (if exception is thrown here, it's ok)
        control = new MiniRPCConnection(createOneChannel(host, nonce));

        // open data
        try {
            blast = new MiniRPCConnection(createOneChannel(host, nonce));
        } catch (IOException e) {
            try {
                // close control and propagate
                control.close();
            } catch (IOException e2) {
            }
            throw e;
        }

        Connection conn = new Connection(control, blast, host);
        conn.sendPreStart(cookieList, pushAttributes, fspec, filters);
        return conn;
    }

    // TODO pipeline
    private void sendPreStart(List<Cookie> cookieList,
            Set<String> pushAttributes, XDR_sig_and_data fspec,
            List<Filter> filters) throws IOException {
        try {
            // define scope
            for (Cookie cookie : cookieList) {
                byte[] data = XDREncoders.encodeString(cookie.getCookie());
                new RPC(this, hostname, 24, data).doRPC().checkStatus();
            }

            // set the push attributes
            if (pushAttributes != null) {
                byte[] encodedAttributes = new XDR_attr_name_list(
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

    public void sendStart() throws IOException {
        try {
            // start search
            byte encodedSearchId[] = new byte[4]; // 0

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

        final byte[] encodedBlobSig = new XDR_blob_sig(name, sig).encode();
        final byte[] encodedBlob = new XDR_blob(name, blobData).encode();

        // System.out.println("blob sig: " + encodedBlobSig);

        // device_set_blob_by_signature = 22
        MiniRPCReply reply1 = new RPC(this, hostname, 22, encodedBlobSig)
                .doRPC();
        if (reply1.getMessage().getStatus() != RPC.DIAMOND_FCACHEMISS) {
            reply1.checkStatus();
            return;
        }

        // device_set_blob = 11
        new RPC(this, hostname, 11, encodedBlob).doRPC().checkStatus();
    }

    private void setCode(Filter f) throws IOException {
        byte code[] = f.getFilterCode().getBytes();
        XDR_sig_val sig = XDR_sig_val.createSignature(code);
        XDR_sig_and_data sigAndData = new XDR_sig_and_data(sig, code);

        final byte[] encodedSig = sig.encode();
        final byte[] encodedSigAndData = sigAndData.encode();

        // device_set_obj = 16
        MiniRPCReply reply1 = new RPC(this, hostname, 16, encodedSig).doRPC();
        if (reply1.getMessage().getStatus() != RPC.DIAMOND_FCACHEMISS) {
            reply1.checkStatus();
            return;
        }

        // device_send_obj = 17
        new RPC(this, hostname, 17, encodedSigAndData).doRPC().checkStatus();
    }

    void close() {
        // System.out.println("closing " + toString());

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

    public void sendMessageBlast(int cmd, byte data[]) throws IOException {
        try {
            blast.sendMessage(cmd, data);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public void sendControlRequest(int cmd, byte[] data) throws IOException {
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
