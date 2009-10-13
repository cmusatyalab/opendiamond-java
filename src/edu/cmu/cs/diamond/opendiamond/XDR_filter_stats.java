/*
 *  The OpenDiamond Platform for Interimport java.io.IOException;
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

class XDR_filter_stats {
    private final String name;

    private final int objsProcessed;

    private final int objsDropped;

    public String getName() {
        return name;
    }

    public int getObjsProcessed() {
        return objsProcessed;
    }

    public int getObjsDropped() {
        return objsDropped;
    }

    public int getObjsCacheDropped() {
        return objsCacheDropped;
    }

    public int getObjsCachePassed() {
        return objsCachePassed;
    }

    public int getObjsCompute() {
        return objsCompute;
    }

    public int getHitsInterSession() {
        return hitsInterSession;
    }

    public int getHitsInterQuery() {
        return hitsInterQuery;
    }

    public int getHitsIntraQuery() {
        return hitsIntraQuery;
    }

    public long getAvgExecTime() {
        return avgExecTime;
    }

    private final int objsCacheDropped;

    private final int objsCachePassed;

    private final int objsCompute;

    private final int hitsInterSession;

    private final int hitsInterQuery;

    private final int hitsIntraQuery;

    private final long avgExecTime;

    public XDR_filter_stats(XDRGetter data) throws IOException {
        name = data.getString(RPC.MAX_FILTER_NAME);
        objsProcessed = data.getInt();
        objsDropped = data.getInt();
        objsCacheDropped = data.getInt();
        objsCachePassed = data.getInt();
        objsCompute = data.getInt();
        hitsInterSession = data.getInt();
        hitsInterQuery = data.getInt();
        hitsIntraQuery = data.getInt();
        avgExecTime = data.getLong();
    }
}
