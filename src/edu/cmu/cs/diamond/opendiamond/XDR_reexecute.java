package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;
import java.util.Set;

public class XDR_reexecute implements XDREncodeable {
    private final String objectID;

    private final XDR_attr_name_list attributes;

    public XDR_reexecute(String objectID, Set<String> attributes) {
        this.objectID = objectID;
        this.attributes = new XDR_attr_name_list(attributes);
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer b1 = XDREncoders.encodeString(objectID);
        ByteBuffer b2 = attributes.encode();

        ByteBuffer result = ByteBuffer.allocate(b1.limit() + b2.limit());
        result.put(b1).put(b2);
        result.flip();

        return result.asReadOnlyBuffer();
    }
}
