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
import java.util.List;

class XDR_filter_config implements XDREncodeable {
    private final String name;

    private final XDR_sig_val code;

    private final double minScore;

    private final double maxScore;

    private final String dependencies[];

    private final String arguments[];

    private final XDR_sig_val blob;

    public XDR_filter_config(String name, XDR_sig_val code, double minScore,
            double maxScore, List<String> dependencies, List<String> arguments,
            XDR_sig_val blob) {
        this.name = name;
        this.code = code;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.dependencies = dependencies.toArray(new String[0]);
        this.arguments = arguments.toArray(new String[0]);
        this.blob = blob;
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.write(XDREncoders.encodeString(name));
            out.writeInt(arguments.length);
            for (String s : arguments) {
                out.write(XDREncoders.encodeString(s));
            }
            out.writeInt(dependencies.length);
            for (String s : dependencies) {
                out.write(XDREncoders.encodeString(s));
            }
            out.writeDouble(minScore);
            out.writeDouble(maxScore);
            out.write(code.encode());
            out.write(blob.encode());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
