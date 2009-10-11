package edu.cmu.cs.diamond.opendiamond;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class XDREncoders {
    private XDREncoders() {
    }

    static public byte[] encodeString(String s) {
        return encodeOpaque(s.getBytes());
    }

    public static byte[] encodeOpaque(byte[] data) {
        int len = data.length;
        int roundup = XDRGetter.roundup(len);
        int slack = roundup - len;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeInt(len);
            out.write(data);

            for (int i = 0; i < slack; i++) {
                out.write(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
