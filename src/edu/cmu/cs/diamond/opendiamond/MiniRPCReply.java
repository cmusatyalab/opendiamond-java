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
            throw new IOException(RPC.statusToString(status));
        }
    }
}
