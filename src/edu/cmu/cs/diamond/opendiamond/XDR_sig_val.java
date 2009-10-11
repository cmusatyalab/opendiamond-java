package edu.cmu.cs.diamond.opendiamond;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class XDR_sig_val implements XDREncodeable {

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
