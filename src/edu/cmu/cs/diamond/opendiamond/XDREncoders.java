package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;

public class XDREncoders {
    private XDREncoders() {
    }

    static public ByteBuffer encodeString(String s) {
        byte bytes[] = s.getBytes();
        int len = bytes.length;
        int roundup = XDRGetter.roundup(len);

        ByteBuffer buf = ByteBuffer.allocate(roundup + 4);
        buf.putInt(len);
        buf.put(bytes);

        buf.limit(buf.capacity()).position(0);

        return buf.asReadOnlyBuffer();
    }

    static public ByteBuffer encodeOpaqueFixed(byte[] data) {
        int len = data.length;
        int roundup = XDRGetter.roundup(len);

        ByteBuffer buf = ByteBuffer.allocate(roundup);
        buf.put(data);

        buf.limit(buf.capacity()).position(0);

        return buf.asReadOnlyBuffer();
    }

    public static ByteBuffer encodeOpaque(byte[] data) {
        int len = data.length;
        int roundup = XDRGetter.roundup(len);

        ByteBuffer buf = ByteBuffer.allocate(roundup + 4);
        buf.putInt(len);
        buf.put(data);

        buf.limit(buf.capacity()).position(0);

        return buf.asReadOnlyBuffer();
    }
}
