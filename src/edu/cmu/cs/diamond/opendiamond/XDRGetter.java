package edu.cmu.cs.diamond.opendiamond;

import java.io.DataInputStream;
import java.io.IOException;

public class XDRGetter {

    static int roundup(int n) {
        int roundup = n;
        if ((roundup & 0x3) != 0) {
            // round up
            roundup = (roundup + 4) & (~3);
        }
        return roundup;
    }

    final private DataInputStream data;

    public XDRGetter(DataInputStream data) {
        this.data = data;
    }

    public int getInt() throws IOException {
        return data.readInt();
    }

    public byte[] getOpaque() throws IOException {
        return getOpaque(Integer.MAX_VALUE);
    }

    public byte[] getOpaque(int maxLength) throws IOException {
        int len = data.readInt();
        if (len > maxLength) {
            throw new IllegalStateException("length greater than max (" + len
                    + " > " + maxLength + ")");
        }
        return getOpaqueFixed(len);
    }

    public byte[] getOpaqueFixed(int len) throws IOException {
        int roundup = roundup(len);
        int slack = roundup - len;

        byte result[] = new byte[len];
        data.read(result);

        // skip slack
        for (int i = 0; i < slack; i++) {
            data.read();
        }

        return result;
    }

    public long getLong() throws IOException {
        return data.readLong();
    }

    public String getString(int maxLength) throws IOException {
        return new String(getOpaque(maxLength));
    }
}
