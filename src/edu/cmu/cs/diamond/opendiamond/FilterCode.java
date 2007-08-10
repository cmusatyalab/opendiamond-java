/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 3
 *
 *  Copyright (c) 2007 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilterCode {
    private byte[] code;

    public FilterCode(byte code[]) {
        this.code = new byte[code.length];
        System.arraycopy(code, 0, this.code, 0, code.length);
    }

    public FilterCode(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte bb[] = new byte[4096];
        
        int amount;
        while((amount = in.read(bb)) != -1) {
            out.write(bb, 0, amount);
        }

        code = out.toByteArray();
    }

    byte[] getBytes() {
        return code;
    }
}
