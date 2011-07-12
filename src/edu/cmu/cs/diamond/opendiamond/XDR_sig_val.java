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

import java.io.IOException;

class XDR_sig_val implements XDREncodeable {

    private final byte[] digest;

    public XDR_sig_val(XDRGetter buf) throws IOException {
        digest = buf.getOpaque(RPC.SIG_SIZE);
    }

    public XDR_sig_val(Signature sig) {
        digest = sig.asBytes();
    }

    public byte[] encode() {
        return XDREncoders.encodeOpaque(digest);
    }

    public Signature getSignature() {
        return Signature.fromDigest(digest);
    }
}
