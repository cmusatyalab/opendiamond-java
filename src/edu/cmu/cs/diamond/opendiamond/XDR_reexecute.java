/*
 *  The OpenDiamond Platform for Interactive Search
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

import java.util.Set;

class XDR_reexecute implements XDREncodeable {
    private final String objectID;

    private final XDR_attr_name_list attributes;

    public XDR_reexecute(String objectID, Set<String> attributes) {
        this.objectID = objectID;
        this.attributes = new XDR_attr_name_list(attributes);
    }

    public byte[] encode() {
        byte b1[] = XDREncoders.encodeString(objectID);
        byte b2[] = attributes.encode();

        byte result[] = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, result, 0, b1.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);

        return result;
    }
}
