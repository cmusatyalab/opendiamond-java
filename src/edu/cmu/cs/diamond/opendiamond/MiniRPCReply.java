package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;

final class MiniRPCReply {
    final private MiniRPCMessage message;

    final private MiniRPCConnection connection;

    final private String hostname;

    public MiniRPCMessage getMessage() {
        return message;
    }

    public MiniRPCConnection getConnection() {
        return connection;
    }

    public String getHostname() {
        return hostname;
    }

    public MiniRPCReply(MiniRPCMessage message, MiniRPCConnection connection,
            String hostname) {
        this.message = message;
        this.connection = connection;
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "miniRPC reply from " + hostname + " (" + connection + "): "
                + message;
    }

    public void checkStatus() throws IOException {
        int status = getMessage().getStatus();
        if (status != MiniRPCMessage.MINIRPC_OK) {
            // TODO case on other statuses
            throw new IOException("bad status on message from " + getHostname()
                    + ": " + RPC.statusToString(status));
        }
    }
}
