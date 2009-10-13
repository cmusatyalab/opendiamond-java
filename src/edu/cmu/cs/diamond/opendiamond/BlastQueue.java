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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class BlastQueue {

    private final BlockingQueue<BlastChannelObject> q;

    private final Object lock = new Object();

    private boolean shutdown;

    public BlastQueue(int size) {
        q = new ArrayBlockingQueue<BlastChannelObject>(size);
    }

    public void put(BlastChannelObject blastChannelObject)
            throws InterruptedException {
        q.put(blastChannelObject);
    }

    // return sentinel when empty
    public BlastChannelObject take() throws InterruptedException {
        // synchronize to make single consumer
        synchronized (lock) {
            if (shutdown) {
                BlastChannelObject obj = q.poll(); // returns null when empty
                if (obj == null) {
                    return BlastChannelObject.NO_MORE_RESULTS;
                } else {
                    return obj;
                }
            } else {
                return q.take(); // block
            }
        }
    }

    public void shutdown() {
        shutdown = true;

        // make sure to wake up any last taker
        q.offer(BlastChannelObject.NO_MORE_RESULTS);
    }
}
