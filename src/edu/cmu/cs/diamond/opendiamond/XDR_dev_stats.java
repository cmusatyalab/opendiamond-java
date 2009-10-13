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
import java.util.ArrayList;
import java.util.List;

class XDR_dev_stats {

    public int getObjsTotal() {
        return objsTotal;
    }

    public int getObjsProcessed() {
        return objsProcessed;
    }

    public int getObjsDropped() {
        return objsDropped;
    }

    public int getObjsNproc() {
        return objsNproc;
    }

    public int getSystemLoad() {
        return systemLoad;
    }

    public long getAvgObjTime() {
        return avgObjTime;
    }

    public List<XDR_filter_stats> getFilterStats() {
        return filterStats;
    }

    private final int objsTotal;

    private final int objsProcessed;

    private final int objsDropped;

    private final int objsNproc;

    private final int systemLoad;

    private final long avgObjTime;

    private final List<XDR_filter_stats> filterStats = new ArrayList<XDR_filter_stats>();

    public XDR_dev_stats(XDRGetter data) throws IOException {
        objsTotal = data.getInt();
        objsProcessed = data.getInt();
        objsDropped = data.getInt();
        objsNproc = data.getInt();
        systemLoad = data.getInt();
        avgObjTime = data.getLong();

        int numFilters = data.getInt();
        if (numFilters > RPC.MAX_FILTERS) {
            throw new IllegalArgumentException(
                    "length of ds_filter_stats too large: " + numFilters
                            + " > " + RPC.MAX_FILTERS);
        }

        // read filters
        for (int i = 0; i < numFilters; i++) {
            filterStats.add(new XDR_filter_stats(data));
        }
    }
}
