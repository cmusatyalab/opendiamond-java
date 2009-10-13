/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
 *
 *  Copyright (c) 2009 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class XDR_attr_name_list implements XDREncodeable {

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
