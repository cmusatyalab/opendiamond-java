/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
 *
 *  Copyright (c) 2009-2011 Carnegie Mellon University
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
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
            List<Filter> filters) throws ServerException {
        // System.out.println("connecting to " + host);

        byte nonce[] = new byte[NONCE_SIZE];

        MiniRPCConnection control;
        MiniRPCConnection blast;

        try {
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
            conn.sendPreStart(cookieList, filters);
            return conn;
        } catch (IOException e) {
            throw new ServerException(host, e);
        }
    }

    private void sendPreStart(List<Cookie> cookieList, List<Filter> filters)
            throws IOException {
        try {
            List<XDR_filter_config> configs = new ArrayList<XDR_filter_config>();
            HashMap<Signature, byte[]> sigToBlob = new HashMap<Signature, byte[]>();

            // gather filter configs and blob signatures for each filter
            for (Filter f : filters) {
                FilterCode code = f.getFilterCode();
                Signature codeSig = code.getSignature();
                Signature blobSig = f.getBlobSig();

                configs.add(new XDR_filter_config(f.getName(),
                        new XDR_sig_val(codeSig), f.getMinScore(),
                        f.getMaxScore(), f.getDependencies(), f.getArguments(),
                        new XDR_sig_val(blobSig)));
                sigToBlob.put(codeSig, code.getBytes());
                sigToBlob.put(blobSig, f.getBlob());
            }

            // collect cookie data
            List<String> cookieData = new ArrayList<String>();
            for (Cookie cookie : cookieList) {
                cookieData.add(cookie.getCookie());
            }

            // configure the search
            byte[] encodedSetup = new XDR_setup(cookieData, configs).encode();

            // setup = 25
            MiniRPCReply reply = new RPC(this, hostname, 25,
                    encodedSetup).doRPC();
            reply.checkStatus();

            // see if any blobs missed in the server's cache
            List<XDR_sig_val> missing = new XDR_sig_list(reply.getMessage()
                    .getData()).getSigs();
            if (missing.size() > 0) {
                // collect blob data for those blobs
                List<byte[]> blobData = new ArrayList<byte[]>();
                for (XDR_sig_val sigVal : missing) {
                    blobData.add(sigToBlob.get(sigVal.getSignature()));
                }
                byte[] encodedBlobs = new XDR_blob_data(blobData).encode();

                // send_blobs = 26
                new RPC(this, hostname, 26, encodedBlobs).doRPC().
                        checkStatus();
            }
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public void sendStart(Set<String> pushAttributes) throws IOException {
        try {
            // Generate a search ID that should be unique over the lifetime
            // of this scope cookie.  OpenDiamond-Java doesn't use this for
            // anything, but the servers may choose to use it (in concert
            // with the scope cookie) to correlate a particular search across
            // multiple servers.  The protocol only gives us 32 bits, so
            // divide them as follows:
            // - 24 bits: millisecond-granularity timestamp right-shifted
            //   by 11 bits.  This gives us a 397-day rollover and a 2-second
            //   resolution.  Most scope cookies expire much sooner than
            //   397 days.
            // - 8 bits: random uniquifier.  This can't be a serial number
            //   because there may be multiple uncoordinated clients.
            //   This allows on average 16 new searches per 2-second tick
            //   before we hit a collision due to the birthday paradox.
            int searchId = (int) (((System.currentTimeMillis() >>> 11)
                    & 0xffffff) << 8);
            byte rand[] = new byte[1];
            new SecureRandom().nextBytes(rand);
            searchId |= ((int) rand[0]) & 0xff;
            byte[] encodedStart = new XDR_start(searchId,
                    pushAttributes).encode();

            // start = 27
            new RPC(this, hostname, 27, encodedStart).doRPC().checkStatus();
        } catch (IOException e) {
            close();
            throw e;
        }
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

    public MiniRPCMessage receiveBlast() throws ServerException {
        try {
            return blast.receive();
        } catch (IOException e) {
            close();
            throw new ServerException(hostname, e);
        }
    }

    public void sendBlastRequest(int cmd, byte data[]) throws ServerException {
        try {
            blast.sendRequest(cmd, data);
        } catch (IOException e) {
            close();
            throw new ServerException(hostname, e);
        }
    }

    public void sendControlRequest(int cmd, byte[] data) throws ServerException {
        try {
            control.sendRequest(cmd, data);
        } catch (IOException e) {
            close();
            throw new ServerException(hostname, e);
        }
    }

    public MiniRPCMessage receiveControl() throws ServerException {
        try {
            return control.receive();
        } catch (IOException e) {
            close();
            throw new ServerException(hostname, e);
        }
    }
}
