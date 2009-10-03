package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;

public final class MiniRPCMessage {
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

    private final ByteBuffer data;

    public MiniRPCMessage(long sequence, int status, int cmd, ByteBuffer data) {
        if ((sequence < 0) || (sequence > 0xFFFFFFFFL)) {
            throw new IllegalArgumentException(
                    "sequence must be between 0 and " + 0xFFFFFFFFL
                            + ", given: " + sequence);
        }

        this.sequence = sequence;
        this.status = status;
        this.cmd = cmd;
        this.data = data.asReadOnlyBuffer();
    }

    public long getSequence() {
        return sequence;
    }

    public int getStatus() {
        return status;
    }

    public int getCmd() {
        return cmd;
    }

    public ByteBuffer getData() {
        return data;
    }
}
