package edu.cmu.cs.diamond.opendiamond;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XDR_attr_name_list implements XDREncodeable {

    private final String strings[];

    public XDR_attr_name_list(Collection<String> list) {
        strings = list.toArray(new String[0]);
    }

    @Override
    public byte[] encode() {
        // length + strings

        int finalSize = 4;
        List<byte[]> bufs = new ArrayList<byte[]>();
        for (String s : strings) {
            byte[] bb = XDREncoders.encodeString(s);
            bufs.add(bb);
            finalSize += bb.length;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            // length
            out.writeInt(strings.length);

            // strings
            for (byte b[] : bufs) {
                out.write(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

}
