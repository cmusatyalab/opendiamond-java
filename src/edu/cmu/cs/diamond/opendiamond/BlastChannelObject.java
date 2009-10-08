package edu.cmu.cs.diamond.opendiamond;

public class BlastChannelObject {

    final private XDR_object obj;

    final private Connection connection;

    public BlastChannelObject(XDR_object obj, Connection connection) {
        this.obj = obj;
        this.connection = connection;
    }

    public XDR_object getObj() {
        return obj;
    }

    public Connection getConnection() {
        return connection;
    }
}
