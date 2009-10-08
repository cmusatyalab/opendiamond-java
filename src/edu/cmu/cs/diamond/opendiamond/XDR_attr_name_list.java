package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XDR_attr_name_list implements XDREncodeable {

    private final String strings[];

    public XDR_attr_name_list(Collection<String> list) {
        strings = list.toArray(new String[0]);
    }

    @Override
    public ByteBuffer encode() {
        // length + strings

        int finalSize = 4;
        List<ByteBuffer> bufs = new ArrayList<ByteBuffer>();
        for (String s : strings) {
            ByteBuffer bb = XDREncoders.encodeString(s);
            bufs.add(bb);
            finalSize += bb.limit();
        }

        ByteBuffer finalBuf = ByteBuffer.allocate(finalSize);

        // length
        finalBuf.putInt(strings.length);

        // strings
        for (ByteBuffer b : bufs) {
            finalBuf.put(b);
        }

        finalBuf.flip();

        return finalBuf.asReadOnlyBuffer();
    }

}
