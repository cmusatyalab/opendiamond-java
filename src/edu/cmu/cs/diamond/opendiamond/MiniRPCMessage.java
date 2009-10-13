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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

final class MiniRPCMessage {
    public final static int MINIRPC_OK = 0;

    public final static int MINIRPC_PENDING = -1;

    public final static int MINIRPC_ENCODING_ERR = -2;

    public final static int MINIRPC_PROCEDURE_UNAVAIL = -3;

    public final static int MINIRPC_INVALID_ARGUMENT = -4;

    public final static int MINIRPC_INVALID_PROTOCOL = -5;

    public final static int MINIRPC_NETWORK_FAILURE = -6;

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
            return "MINIRPC_OK";
        case MINIRPC_PENDING:
            return "MINIRPC_PENDING";
        case MINIRPC_ENCODING_ERR:
            return "MINIRPC_ENCODING_ERR";
        case MINIRPC_PROCEDURE_UNAVAIL:
            return "MINIRPC_PROCEDURE_UNAVAIL";
        case MINIRPC_INVALID_ARGUMENT:
            return "MINIRPC_INVALID_ARGUMENT";
        case MINIRPC_INVALID_PROTOCOL:
            return "MINIRPC_INVALID_PROTOCOL";
        case MINIRPC_NETWORK_FAILURE:
            return "MINIRPC_NETWORK_FAILURE";
        default:
            return Integer.toString(s);

        }
    }
}
