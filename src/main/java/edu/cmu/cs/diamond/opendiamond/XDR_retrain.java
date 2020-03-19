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
import java.util.List;

class XDR_retrain implements XDREncodeable {
    private final String[] names;
    private final Integer[] labels;
    private final byte[][] features;

    public XDR_retrain(List<String> names, List<Integer> labels, List<byte[]> features) {
        this.features = features.toArray(new byte[0][0]);
        this.names = names.toArray(new String[0]);
        this.labels = labels.toArray(new Integer[0]);
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeInt(names.length);
            for (String name : names) {
                out.write(XDREncoders.encodeString(name));
            }
            out.writeInt(labels.length);
            for (int label : labels) {
                out.writeInt(label);
            }
            out.writeInt(features.length);
            for (byte[] feature : features) {
                out.write(XDREncoders.encodeOpaque(feature));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}

