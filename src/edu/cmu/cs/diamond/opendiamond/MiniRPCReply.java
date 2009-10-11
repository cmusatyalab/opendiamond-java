package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;

final class MiniRPCReply {
    final private MiniRPCMessage message;

    final private String hostname;

    public MiniRPCMessage getMessage() {
        return message;
    }

    public String getHostname() {
        return hostname;
    }

    public MiniRPCReply(MiniRPCMessage message, String hostname) {
        this.message = message;
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "miniRPC reply from " + hostname + ": " + message;
    }

    // TODO throw different exceptions based on status
    public void checkStatus() throws IOException {
        int status = getMessage().getStatus();
        if (status != MiniRPCMessage.MINIRPC_OK) {
            // TODO case on other statuses
            throw new IOException("bad status on message from " + getHostname()
                    + ": " + RPC.statusToString(status));
        }
    }
}
