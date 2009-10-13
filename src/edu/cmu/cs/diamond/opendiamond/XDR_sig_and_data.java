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

import java.util.Arrays;

class XDR_sig_and_data implements XDREncodeable {
    private final XDR_sig_val sig;

    private final byte data[];

    public XDR_sig_and_data(XDR_sig_val sig, byte data[]) {
        this.sig = sig;
        this.data = Arrays.copyOf(data, data.length);
    }

    public byte[] encode() {
        byte b1[] = sig.encode();
        byte b2[] = XDREncoders.encodeOpaque(data);

        byte result[] = Arrays.copyOf(b1, b1.length + b2.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);

        return result;
    }
}
