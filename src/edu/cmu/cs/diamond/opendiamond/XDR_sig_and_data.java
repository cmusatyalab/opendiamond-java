package edu.cmu.cs.diamond.opendiamond;

import java.util.Arrays;

public class XDR_sig_and_data implements XDREncodeable {
    private final XDR_sig_val sig;
    private final byte data[];

    public XDR_sig_and_data(XDR_sig_val sig, byte data[]) {
        this.sig = sig;
        this.data = Arrays.copyOf(data, data.length);
    }

    public byte[] encode() {
        byte b1[] = sig.encode();
        byte b2[] = XDREncoders.encodeOpaque(data);

        byte result[] = Arrays.copyOf(b1, b1.length + b2.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);

        return result;
    }
}
