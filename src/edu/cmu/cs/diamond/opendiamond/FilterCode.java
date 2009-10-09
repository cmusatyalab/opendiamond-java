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

import java.io.IOException;
import java.io.InputStream;

public class FilterCode {
    final private byte[] code;

    public FilterCode(byte code[]) {
        this.code = new byte[code.length];
        System.arraycopy(code, 0, this.code, 0, code.length);
    }

    public FilterCode(InputStream in) throws IOException {
        code = Util.readFully(in);
    }

    byte[] getBytes() {
        return code;
    }
}
