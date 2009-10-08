package edu.cmu.cs.diamond.opendiamond;

public class BlastChannelObject {

    final private XDR_object obj;

    final private String hostname;

    public BlastChannelObject(XDR_object obj, String hostname) {
        this.obj = obj;
        this.hostname = hostname;
    }

    public XDR_object getObj() {
        return obj;
    }

    public String getHostname() {
        return hostname;
    }
}
