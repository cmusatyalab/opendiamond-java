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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class XDR_labeled_example implements XDREncodeable {

    private final String objectID;
    private final int label;

    XDR_labeled_example(String objectID, int label) {
        this.objectID = objectID;
        this.label = label;
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.write(XDREncoders.encodeString(objectID));
            out.writeInt(label);
        } catch(IOException e) {
            throw new RuntimeException("Failed to encode labeled example", e);
        }

        return baos.toByteArray();
    }
}
