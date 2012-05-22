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

import java.util.Map;

/**
 * A class representing some runtime statistics for a single server.
 */
public class ServerStatistics {
    final private static String objsTotal = "objs_total";

    final private static String objsProcessed = "objs_processed";

    final private static String objsDropped = "objs_dropped";

    final private Map<String, Long> serverStatistics;

    ServerStatistics(Map<String, Long> serverStatistics) {
        this.serverStatistics = serverStatistics;
    }

    public long getStat(String name) {
        if (serverStatistics.get(name) == null) {
            throw new IllegalArgumentException("No such statistics name");
        }

        return serverStatistics.get(name);
    }

    /**
     * Gets the count of dropped objects.
     * 
     * @return dropped object count
     */
    @Deprecated
    public int getDroppedObjects() {
        long droppedObjects = getStat(objsDropped);

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
    @Deprecated
    public int getProcessedObjects() {
        long processedObjects = getStat(objsProcessed);

        if (processedObjects < Integer.MIN_VALUE
                || processedObjects > Integer.MAX_VALUE) {
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
    @Deprecated
    public int getTotalObjects() {
        long totalObjects = getStat(objsTotal);

        if (totalObjects < Integer.MIN_VALUE
                || totalObjects > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Value out of range supported by int");
        }

        return (int) totalObjects;
    }

    @Override
    public String toString() {
        return getStat(objsTotal) + " total, " + getStat(objsProcessed)
                + " processed, " + getStat(objsDropped) + " dropped";
    }
}
