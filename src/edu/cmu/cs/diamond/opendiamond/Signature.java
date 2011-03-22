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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

class Signature {

    public static final int SIG_SIZE = 16;

    private final byte[] digest;

    public Signature(byte data[]) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        digest = md.digest(data);
    }

    public byte[] asBytes() {
        byte[] ret = new byte[SIG_SIZE];
        System.arraycopy(digest, 0, ret, 0, SIG_SIZE);
        return ret;
    }

    public String asString() {
        Formatter f = new Formatter();
        for (byte b : digest) {
            f.format("%02x", b & 0xff);
        }
        return f.toString();
    }
}
