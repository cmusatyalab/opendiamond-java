/*
 *  The OpenDiamond Platform for Interactive Search
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
    final private long totalObjects;

    final private long processedObjects;

    final private long droppedObjects;

    ServerStatistics(long totalObjects, long processedObjects,
            long droppedObjects) {
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
        if (droppedObjects < Integer.MIN_VALUE
                || droppedObjects > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Value out of range supported by int");
        }

        return (int) droppedObjects;
    }

    /**
     * Gets the count of processed objects.
     * 
     * @return processed object count
     */
    public int getProcessedObjects() {
        if (droppedObjects < Integer.MIN_VALUE
                || droppedObjects > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Value out of range supported by int");
        }

        return (int) processedObjects;
    }

    /**
     * Gets the count of total objects.
     * 
     * @return total object count
     */
    public int getTotalObjects() {
        if (droppedObjects < Integer.MIN_VALUE
                || droppedObjects > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Value out of range supported by int");
        }

        return (int) totalObjects;
    }

    @Override
    public String toString() {
        return totalObjects + " total, " + processedObjects + " processed, "
                + droppedObjects + " dropped";
    }

    // These API methods are deprecated because applications expect the values
    // to
    // be integers.
    //
    // /**
    // * Gets the count of dropped objects.
    // *
    // * @return dropped object count
    // */
    // public long getDroppedObjects() {
    // return droppedObjects;
    // }
    //
    // /**
    // * Gets the count of processed objects.
    // *
    // * @return processed object count
    // */
    // public long getProcessedObjects() {
    // return processedObjects;
    // }
    //
    // /**
    // * Gets the count of total objects.
    // *
    // * @return total object count
    // */
    // public long getTotalObjects() {
    // return totalObjects;
    // }
}
