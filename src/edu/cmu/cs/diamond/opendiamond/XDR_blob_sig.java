package edu.cmu.cs.diamond.opendiamond;

import java.util.Arrays;

public class XDR_blob_sig implements XDREncodeable {

    final private String name;
    final private XDR_sig_val sig;

    public XDR_blob_sig(String name, XDR_sig_val sig) {
        if (name.length() > RPC.MAX_FILTER_NAME) {
            throw new IllegalArgumentException("name length greater than "
                    + RPC.MAX_FILTER_NAME);
        }

        this.name = name;
        this.sig = sig;
    }

    @Override
    public byte[] encode() {
        byte b1[] = XDREncoders.encodeString(name);
        byte b2[] = sig.encode();

        byte result[] = Arrays.copyOf(b1, b1.length + b2.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);

        return result;
    }
}
