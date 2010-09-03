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
import java.util.concurrent.Callable;

class BlastGetter implements Callable<Object> {

    private final BlastQueue q;

    private final Connection connection;

    private final String hostname;

    public BlastGetter(Connection connection, String hostname,
            BlastQueue blastQueue) {
        this.connection = connection;
        this.hostname = hostname;
        this.q = blastQueue;
    }

    private final byte[] emptyBuf = new byte[0];

    private XDR_object getAndAcknowldgeBlastChannelObject() throws IOException {
        // System.out.println(hostname + ": waiting for blast object");
        MiniRPCReply reply = new RPC(connection, hostname, 1, emptyBuf)
                .doBlastRPC();
        reply.checkStatus();

        XDR_object obj = new XDR_object(reply.getMessage().getData());
        // System.out.println(hostname + ":   blast object done");

        return obj;
    }

    public Object call() throws Exception {
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