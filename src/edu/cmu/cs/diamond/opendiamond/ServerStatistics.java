/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 3
 *
 *  Copyright (c) 2007 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

public class ServerStatistics {
    final private String hostname;

    final private int totalObjects;

    final private int processedObjects;

    final private int droppedObjects;

    public ServerStatistics(String hostname, int totalObjects,
            int processedObjects, int droppedObjects) {
        this.hostname = hostname;
        this.totalObjects = totalObjects;
        this.processedObjects = processedObjects;
        this.droppedObjects = droppedObjects;
    }

    public String getHostname() {
        return hostname;
    }

    public int getDroppedObjects() {
        return droppedObjects;
    }

    public int getProcessedObjects() {
        return processedObjects;
    }

    public int getTotalObjects() {
        return totalObjects;
    }

    @Override
    public String toString() {
        return hostname + ": " + totalObjects + " total, " + processedObjects
                + " processed, " + droppedObjects + " dropped";
    }
}
