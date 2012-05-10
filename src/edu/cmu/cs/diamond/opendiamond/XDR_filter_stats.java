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
import java.util.HashMap;
import java.util.Map;

class XDR_filter_stats {
    public String getName() {
        return name;
    }

    public Map<String, Long> getStats() {
        return stats;
    }

    private final String name;

    private final Map<String, Long> stats = new HashMap<String, Long>();

    public XDR_filter_stats(XDRGetter data) throws IOException {
        name = data.getString(RPC.MAX_FILTER_NAME);

        // read statistics
        int numStats = data.getInt();
        for (int i = 0; i < numStats; i++) {
            stats.put(data.getString(), data.getLong());
        }
    }
}
