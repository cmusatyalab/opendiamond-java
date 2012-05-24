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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

class XDR_dev_stats {
    public Map<String, Long> getStats() {
        return Collections.unmodifiableMap(stats);
    }

    public List<XDR_filter_stats> getFilterStats() {
        return Collections.unmodifiableList(filterStats);
    }

    private final Map<String, Long> stats = new HashMap<String, Long>();

    private final List<XDR_filter_stats> filterStats = new ArrayList<XDR_filter_stats>();

    public XDR_dev_stats(XDRGetter data) throws IOException {
        // read server statistics
        int numStats = data.getInt();
        for (int i = 0; i < numStats; i++) {
            stats.put(data.getString(), data.getLong());
        }

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
