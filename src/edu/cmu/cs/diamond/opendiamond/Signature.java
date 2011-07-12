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
import java.util.Arrays;
import java.util.Formatter;

class Signature {

    private final byte[] digest;

    private Signature(byte data[], boolean asDigest) {
        if (asDigest) {
            digest = new byte[data.length];
            System.arraycopy(data, 0, digest, 0, data.length);
        } else {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            digest = md.digest(data);
        }
    }

    public Signature(byte data[]) {
        this(data, false);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Signature) {
            return Arrays.equals(digest, ((Signature) obj).digest);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(digest);
    }

    public byte[] asBytes() {
        byte[] ret = new byte[RPC.SIG_SIZE];
        System.arraycopy(digest, 0, ret, 0, RPC.SIG_SIZE);
        return ret;
    }

    public String asString() {
        Formatter f = new Formatter();
        for (byte b : digest) {
            f.format("%02x", b & 0xff);
        }
        return f.toString();
    }

    public static Signature fromDigest(byte[] digest) {
        return new Signature(digest, true);
    }
}
