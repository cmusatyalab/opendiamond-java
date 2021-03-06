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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class BlastQueue {

    private final BlockingQueue<BlastChannelObject> q;

    private final Object lock = new Object();

    private volatile boolean shutdown;

    private volatile boolean pause;

    public BlastQueue(int size) {
        q = new ArrayBlockingQueue<BlastChannelObject>(size);
    }

    public void put(BlastChannelObject blastChannelObject)
            throws InterruptedException {
        if (shutdown) {
            throw new IllegalStateException("queue is shut down");
        }

        if (blastChannelObject == BlastChannelObject.NO_MORE_RESULTS) {
            throw new IllegalArgumentException(
                    "cannot put the NO_MORE_RESULTS object");
        }

        if (pause) {
            return;
        }


        q.put(blastChannelObject);
    }

    //clear the queue
    public void pause() {
        pause = true;
        q.clear();
    }

    public void resume() {
        pause = false;
    }

    // return sentinel when empty
    public BlastChannelObject take() throws InterruptedException {
        // synchronize to make single consumer
        synchronized (lock) {
            if (shutdown || pause) {
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
