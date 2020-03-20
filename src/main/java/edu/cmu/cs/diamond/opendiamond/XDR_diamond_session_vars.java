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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class XDR_diamond_session_vars implements XDREncodeable {

    private final List<XDR_diamond_session_var> vars = new ArrayList<XDR_diamond_session_var>();

    public XDR_diamond_session_vars(XDRGetter data) throws IOException {
        int len = data.getInt();
        for (int i = 0; i < len; i++) {
            vars.add(new XDR_diamond_session_var(data));
        }
    }

    public XDR_diamond_session_vars(List<XDR_diamond_session_var> vars) {
        this.vars.addAll(vars);
    }

    public List<XDR_diamond_session_var> getVars() {
        return Collections.unmodifiableList(vars);
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeInt(vars.size());
            for (XDR_diamond_session_var v : vars) {
                out.write(v.encode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
