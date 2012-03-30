/*
 *  The OpenDiamond Platform for Interactive Search
 *
 *  Copyright (c) 2009-2010 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

final class MiniRPCMessage {
    public final static int MINIRPC_OK = 0;

    public final static int MINIRPC_PENDING = -1;

    public final static int MINIRPC_ENCODING_ERR = -2;

    public final static int MINIRPC_PROCEDURE_UNAVAIL = -3;

    public final static int MINIRPC_INVALID_ARGUMENT = -4;

    private final long sequence;

    private final int status;

    private final int cmd;

    private final XDRGetter data;

    MiniRPCMessage(long sequence, int status, int cmd, byte[] data) {
        if ((sequence < 0) || (sequence > 0xFFFFFFFFL)) {
            throw new IllegalArgumentException(
                    "sequence must be between 0 and " + 0xFFFFFFFFL
                            + ", given: " + sequence);
        }

        this.sequence = sequence;
        this.status = status;
        this.cmd = cmd;
        this.data = new XDRGetter(new DataInputStream(new ByteArrayInputStream(
                data)));
    }

    long getSequence() {
        return sequence;
    }

    public int getStatus() {
        return status;
    }

    public int getCmd() {
        return cmd;
    }

    public XDRGetter getData() {
        return data;
    }

    @Override
    public String toString() {
        return "sequence: " + sequence + ", status: " + statusToString(status)
                + ", cmd: " + cmd + ", data: " + data;
    }

    static String statusToString(int s) {
        switch (s) {
        case MINIRPC_OK:
            return "OK";
        case MINIRPC_PENDING:
            return "pending";
        case MINIRPC_ENCODING_ERR:
            return "encoding error";
        case MINIRPC_PROCEDURE_UNAVAIL:
            return "procedure unavailable";
        case MINIRPC_INVALID_ARGUMENT:
            return "invalid argument";
        default:
            return Integer.toString(s);

        }
    }
}
