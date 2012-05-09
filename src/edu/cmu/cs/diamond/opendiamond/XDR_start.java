/*
 *  The OpenDiamond Platform for Interactive Search
 *
 *  Copyright (c) 2009-2011 Carnegie Mellon University
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
import java.util.Set;

class XDR_start implements XDREncodeable {
    private static final int SEARCH_ID_LENGTH = 36;

    private final byte[] searchID;

    private final XDR_attr_name_list attributes;

    public XDR_start(byte[] searchID, Set<String> attributes) {
        if (searchID.length != SEARCH_ID_LENGTH) {
            throw new IllegalArgumentException("Search ID MUST be 36 bytes");
        }

        this.searchID = searchID;
        if (attributes != null) {
            this.attributes = new XDR_attr_name_list(attributes);
        } else {
            this.attributes = null;
        }
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.write(searchID, 0, SEARCH_ID_LENGTH);
            if (attributes != null) {
                out.writeInt(1);
                out.write(attributes.encode());
            } else {
                out.writeInt(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
