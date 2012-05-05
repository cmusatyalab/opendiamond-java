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

import java.io.IOException;
import java.util.concurrent.Callable;

class BlastGetter implements Callable<Object> {

    private final BlastQueue q;

    private final Connection connection;

    private final String hostname;

    private final int maxOutstandingRequests;

    public BlastGetter(Connection connection, String hostname,
            BlastQueue blastQueue, int maxOutstandingRequests) {
        this.connection = connection;
        this.hostname = hostname;
        this.q = blastQueue;
        this.maxOutstandingRequests = maxOutstandingRequests;
    }

    private final int CMD = 2;

    private final byte[] DATA = new byte[0];

    private XDR_object getAndAcknowldgeBlastChannelObject() throws IOException {
        // System.out.println(hostname + ": waiting for blast object");
        // receive previous reply
        MiniRPCReply reply = new MiniRPCReply(connection.receiveBlast(),
                hostname);
        reply.checkStatus();

        // send another request
        connection.sendBlastRequest(CMD, DATA);

        // System.out.println(hostname + ":   blast object done");

        return new XDR_object(reply.getMessage().getData());
    }

    public Object call() throws Exception {
        // queue up requests
        for (int i = 0; i < maxOutstandingRequests; i++) {
            connection.sendBlastRequest(CMD, DATA);
        }

        // block, waiting for blast channel object, then stick into queue
        while (true) {
            XDR_object obj = getAndAcknowldgeBlastChannelObject();

            // no more objects?
            if (obj.getAttributes().isEmpty() && (obj.getData().length == 0)) {
                return null;
            }

            q.put(new BlastChannelObject(obj, hostname, null));
        }
    }
}