package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;

public class XDR_blob implements XDREncodeable {

    final private String name;
    final private byte[] blobData;

    public XDR_blob(String name, byte[] blobData) {
        if (name.length() > RPC.MAX_FILTER_NAME) {
            throw new IllegalArgumentException("name length greater than "
                    + RPC.MAX_FILTER_NAME);
        }

        this.name = name;
        this.blobData = blobData;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer b1 = XDREncoders.encodeString(name);
        ByteBuffer b2 = XDREncoders.encodeOpaque(blobData);

        ByteBuffer result = ByteBuffer.allocate(b1.limit() + b2.limit())
                .put(b1).put(b2);
        result.flip();

        return result.asReadOnlyBuffer();
    }

}
