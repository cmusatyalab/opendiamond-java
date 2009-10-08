package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class XDR_sig_and_data implements XDREncodeable {
    private final XDR_sig_val sig;
    private final byte data[];

    public XDR_sig_and_data(XDR_sig_val sig, byte data[]) {
        this.sig = sig;
        this.data = Arrays.copyOf(data, data.length);
    }

    public ByteBuffer encode() {
        ByteBuffer e1 = sig.encode();
        ByteBuffer e2 = XDREncoders.encodeOpaque(data);

        System.out.println(e1);
        System.out.println(e2);

        ByteBuffer result = ByteBuffer.allocate(e1.limit() + e2.limit());

        result.put(e1);
        result.put(e2);
        result.flip();

        // byte[] a = result.array();
        // for (int i = 0; i < a.length; i++) {
        // if ((i != 0) && (i % 32) == 0) {
        // System.out.println();
        // } else if ((i != 0) && (i % 8) == 0) {
        // System.out.print(" ");
        // }
        // System.out.printf("%02x ", a[i] & 0xFF);
        // }
        // System.out.println();

        return result.asReadOnlyBuffer();
    }
}
