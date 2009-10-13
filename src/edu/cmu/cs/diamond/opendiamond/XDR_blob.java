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

import java.util.Arrays;

public class XDR_blob implements XDREncodeable {

    final private String name;
    final private byte[] blobData;

    public XDR_blob(String name, byte[] blobData) {
        if (name.length() > RPC.MAX_FILTER_NAME) {
            throw new IllegalArgumentException("name length greater than "
                    + RPC.MAX_FILTER_NAME);
        }

        this.name = name;
        this.blobData = blobData;
    }

    @Override
    public byte[] encode() {
        byte b1[] = XDREncoders.encodeString(name);
        byte b2[] = XDREncoders.encodeOpaque(blobData);

        byte result[] = Arrays.copyOf(b1, b1.length + b2.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);

        return result;
    }
}
