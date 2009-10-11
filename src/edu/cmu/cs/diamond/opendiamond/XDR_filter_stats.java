package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;

public class XDR_filter_stats {
    private final String name;
    private final int objsProcessed;
    private final int objsDropped;
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
