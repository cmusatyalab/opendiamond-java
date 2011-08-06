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

import java.io.IOException;

class XDR_attribute {

    private final static int MAX_ATTRIBUTE_NAME = 256;

    private final String name;

    private final byte[] data;

    public XDR_attribute(XDRGetter buf) throws IOException {
        name = buf.getString(MAX_ATTRIBUTE_NAME);
        data = buf.getOpaque();
    }

    public byte[] getData() {
        return data;
    }

    public String getName() {
        return name;
    }

}
