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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class XDR_object {
    private final long searchID;

    private final byte[] data;

    private final Map<String, byte[]> attributes;

    public XDR_object(XDRGetter buf) throws IOException {
        searchID = buf.getInt() & 0xFFFFFFFFL;
        data = buf.getOpaque();

        Map<String, byte[]> tmpMap = new HashMap<String, byte[]>();
        int attrCount = buf.getInt();
        for (int i = 0; i < attrCount; i++) {
            XDR_attribute attr = new XDR_attribute(buf);

            tmpMap.put(attr.getName(), attr.getData());
        }
        attributes = Collections.unmodifiableMap(tmpMap);
    }

    public long getSearchID() {
        return searchID;
    }

    public byte[] getData() {
        return data;
    }

    public Map<String, byte[]> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "searchID: " + searchID + ", attributes: " + attributes
                + ", data: " + Arrays.toString(data);
    }
}
