/*
 *  The OpenDiamond Platform for Interactive Search
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
import java.util.concurrent.atomic.AtomicInteger;

final class MiniRPCConnection {
    final private AtomicInteger nextSequence = new AtomicInteger();

    final private Socket socket;

    final private DataInputStream in;

    final private DataOutputStream out;

    public MiniRPCConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    private void send(long sequence, int status, int cmd, byte data[])
            throws IOException {
        if ((sequence < 0) || (sequence > 0xFFFFFFFFL)) {
            throw new IllegalArgumentException(
                    "sequence must be between 0 and " + 0xFFFFFFFFL
                            + ", given: " + sequence);
        }

        // System.out.println("sending: " + sequence + " " + status + " " + cmd
        // + " (" + data.length + ") " + Arrays.toString(data));

        // write header
        out.writeInt((int) sequence);
        out.writeInt(status);
        out.writeInt(cmd);
        out.writeInt(data.length);

        // write data
        out.write(data);
    }

    public void sendRequest(int cmd, byte data[]) throws IOException {
        if (cmd <= 0) {
            throw new IllegalArgumentException("cmd must be positive");
        }
        send(nextSequence.getAndIncrement() & 0xFFFFFFFFL,
                MiniRPCMessage.MINIRPC_PENDING, cmd, data);
    }

    public MiniRPCMessage receive() throws IOException {
        long sequence = in.readInt() & 0xFFFFFFFFL;
        int status = in.readInt();
        int cmd = in.readInt();
        int datalen = in.readInt();

        byte data[] = readXDRData(datalen);

        return new MiniRPCMessage(sequence, status, cmd, data);
    }

    private byte[] readXDRData(int datalen) throws IOException {
        // get slack required
        int roundup = XDRGetter.roundup(datalen);
        int slack = roundup - datalen;

        byte buf[] = new byte[datalen];
        in.readFully(buf);

        // skip slack
        for (int i = 0; i < slack; i++) {
            in.read();
        }

        return buf;
    }

    public void close() throws IOException {
        socket.close();
    }
}
