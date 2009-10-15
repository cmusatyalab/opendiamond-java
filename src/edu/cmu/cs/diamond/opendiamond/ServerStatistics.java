/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
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

/**
 * A class representing some runtime statistics for a single server.
 */
public class ServerStatistics {
    final private int totalObjects;

    final private int processedObjects;

    final private int droppedObjects;

    ServerStatistics(int totalObjects, int processedObjects, int droppedObjects) {
        this.totalObjects = totalObjects;
        this.processedObjects = processedObjects;
        this.droppedObjects = droppedObjects;
    }

    /**
     * Gets the count of dropped objects.
     * 
     * @return dropped object count
     */
    public int getDroppedObjects() {
        return droppedObjects;
    }

    /**
     * Gets the count of processed objects.
     * 
     * @return processed object count
     */
    public int getProcessedObjects() {
        return processedObjects;
    }

    /**
     * Gets the count of total objects.
     * 
     * @return total object count
     */
    public int getTotalObjects() {
        return totalObjects;
    }

    @Override
    public String toString() {
        return totalObjects + " total, " + processedObjects + " processed, "
                + droppedObjects + " dropped";
    }
}
