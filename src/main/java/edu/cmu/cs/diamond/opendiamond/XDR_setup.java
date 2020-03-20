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

class XDR_setup implements XDREncodeable {
    private final String[] cookies;

    private final XDR_filter_config[] filters;

    public XDR_setup(List<String> cookies, List<XDR_filter_config> filters) {
        this.cookies = cookies.toArray(new String[0]);
        this.filters = filters.toArray(new XDR_filter_config[0]);
    }

    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeInt(cookies.length);
            for (String cookie : cookies) {
                out.write(XDREncoders.encodeString(cookie));
            }
            out.writeInt(filters.length);
            for (XDR_filter_config filter : filters) {
                out.write(filter.encode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
