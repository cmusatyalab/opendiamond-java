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
import java.util.*;

class XDR_attr_list {

    private final List<XDR_attribute> attributes = new ArrayList<XDR_attribute>();

    public XDR_attr_list(XDRGetter data) throws IOException {
        int len = data.getInt();

        for (int i = 0; i < len; i++) {
            attributes.add(new XDR_attribute(data));
        }
    }

    public List<XDR_attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public Map<String, byte[]> createMap() {
        HashMap<String, byte[]> result = new HashMap<String, byte[]>();

        for (XDR_attribute a : attributes) {
            result.put(a.getName(), a.getData());
        }

        return result;
    }
}
