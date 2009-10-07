package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;

public class XDRBuffer {

    private static int roundup(int n) {
        int roundup = n;
        if ((roundup & 0x3) != 0) {
            // round up
            roundup = (roundup + 4) & (~3);
        }
        return roundup;
    }

    final private ByteBuffer data;

    public XDRBuffer(ByteBuffer data) {
        this.data = data;
    }

    public int getInt() {
        return data.getInt();
    }

    public byte[] getOpaque() {
        return getOpaque(Integer.MAX_VALUE);
    }

    public byte[] getOpaque(int maxLength) {
        int len = data.getInt();
        if (len > maxLength) {
            throw new IllegalStateException("length greater than max (" + len
                    + " > " + maxLength + ")");
        }
        return getOpaqueFixed(len);
    }

    public byte[] getOpaqueFixed(int len) {
        int roundup = roundup(len);
        int slack = roundup - len;

        byte result[] = new byte[len];
        data.get(result);
        data.position(data.position() + slack);

        return result;
    }

    public long getLong() {
        return data.getLong();
    }

    public String getString(int maxLength) {
        return new String(getOpaque(maxLength));
    }
}
