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

class XDR_diamond_session_var implements XDREncodeable {

    private final String name;

    private final double value;

    public XDR_diamond_session_var(XDRGetter data) throws IOException {
        name = data.getString();
        value = data.getDouble();
    }

    public XDR_diamond_session_var(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        byte encodedName[] = XDREncoders.encodeString(name);
        try {
            out.write(encodedName);
            out.writeDouble(value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
