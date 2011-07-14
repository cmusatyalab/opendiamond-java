/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
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
import java.util.Collection;

class XDR_blob_data implements XDREncodeable {
    private final byte[][] blobs;

    public XDR_blob_data(Collection<byte[]> blobs) {
        this.blobs = blobs.toArray(new byte[0][0]);
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeInt(blobs.length);
            for (byte[] blob : blobs) {
                out.write(XDREncoders.encodeOpaque(blob));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
