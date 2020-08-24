/*
 *  The OpenDiamond Platform for Interactive Search
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

/**
 * Encapsulation of opaque filter code for use in constructing a {@link Filter}.
 * 
 */
public class FilterCode {
    final private byte[] code;
    final private Signature sig;

    /**
     * Constructs a new FilterCode from the given byte array.
     * 
     * @param code
     *            the byte array representing filter code
     */
    public FilterCode(byte code[]) {
        this.code = new byte[code.length];
        System.arraycopy(code, 0, this.code, 0, code.length);
        sig = new Signature(code);
    }

    /**
     * Constructs a new FilterCode from the given InputStream. The stream will
     * be read completely.
     * 
     * @param in
     *            the InputStream to read filter code from
     * @throws IOException
     *             if the InputStream cannot be read
     */
    public FilterCode(InputStream in) throws IOException {
        code = Util.readFully(in);
        sig = new Signature(code);
    }

    public byte[] getBytes() {
        return code;
    }

    Signature getSignature() {
        return sig;
    }
}
