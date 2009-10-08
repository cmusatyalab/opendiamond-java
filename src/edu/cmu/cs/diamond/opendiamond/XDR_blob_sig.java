package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;

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
    public ByteBuffer encode() {
        ByteBuffer b1 = XDREncoders.encodeString(name);
        ByteBuffer b2 = sig.encode();

        ByteBuffer result = ByteBuffer.allocate(b1.limit() + b2.limit())
                .put(b1).put(b2);
        result.flip();
        return result.asReadOnlyBuffer();
    }
}
