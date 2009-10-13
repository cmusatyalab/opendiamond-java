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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class XDR_sig_val implements XDREncodeable {

    public static final int SIG_SIZE = 16;

    private final byte[] digest;

    public XDR_sig_val(byte[] digest) {
        if (digest.length > SIG_SIZE) {
            throw new IllegalArgumentException(
                    "digest must be no larger than SIG_SIZE");
        }

        this.digest = Arrays.copyOf(digest, digest.length);
    }

    public byte[] encode() {
        return XDREncoders.encodeOpaque(digest);
    }

    public static XDR_sig_val createSignature(byte data[]) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        XDR_sig_val sig = new XDR_sig_val(md.digest(data));

        return sig;
    }
}
