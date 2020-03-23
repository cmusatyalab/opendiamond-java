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
import java.util.List;

class XDR_label_examples implements XDREncodeable {

    private final List<XDR_labeled_example> examples;

    XDR_label_examples(List<XDR_labeled_example> examples) {
        this.examples = examples;
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeInt(examples.size());
            for (XDR_labeled_example example : examples) {
                out.write(example.encode());
            }
        } catch(IOException e) {
            throw new RuntimeException("Failed to encode label examples call", e);
        }

        return baos.toByteArray();
    }
}
