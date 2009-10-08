package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;

public class XDR_stop implements XDREncodeable {

    final private int hostObjsReceived;
    final private int hostObjsQueued;
    final private int hostObjsRead;
    final private int appObjsQueued;
    final private int appObjsPresented;

    public XDR_stop(int hostObjsReceived, int hostObjsQueued, int hostObjsRead,
            int appObjsQueued, int appObjsPresented) {
        this.hostObjsReceived = hostObjsReceived;
        this.hostObjsQueued = hostObjsQueued;
        this.hostObjsRead = hostObjsRead;
        this.appObjsQueued = appObjsQueued;
        this.appObjsPresented = appObjsPresented;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer result = ByteBuffer.allocate(20);
        result.putInt(hostObjsReceived);
        result.putInt(hostObjsQueued);
        result.putInt(hostObjsRead);
        result.putInt(appObjsQueued);
        result.putInt(appObjsPresented);

        result.flip();

        return result.asReadOnlyBuffer();
    }

}
