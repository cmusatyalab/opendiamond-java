package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;

public class BlastChannelObject {

    final private XDR_object obj;

    final private String hostname;

    final private IOException exception;

    static final BlastChannelObject NO_MORE_RESULTS = new BlastChannelObject(
            null, null, null);

    public BlastChannelObject(XDR_object obj, String hostname,
            IOException exception) {
        this.obj = obj;
        this.hostname = hostname;
        this.exception = exception;
    }

    public XDR_object getObj() {
        return obj;
    }

    public String getHostname() {
        return hostname;
    }

    public IOException getException() {
        return exception;
    }
}
