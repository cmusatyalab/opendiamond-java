/**
 * 
 */
package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

class RPC implements Callable<MiniRPCReply> {

    final private MiniRPCConnection connection;

    final private int cmd;

    final private byte[] data;

    final private String hostname;

    public RPC(MiniRPCConnection connection, String hostname, int cmd,
            byte[] data) {
        this.connection = connection;
        this.hostname = hostname;
        this.cmd = cmd;
        this.data = data;
    }

    @Override
    public MiniRPCReply call() throws Exception {
        return doRPC();
    }

    public MiniRPCReply doRPC() throws IOException {
        connection.sendRequest(cmd, ByteBuffer.wrap(data));
        return new MiniRPCReply(connection.receive(), connection, hostname);
    }
}