package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;
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

    public ByteBuffer encode() {
        return XDREncoders.encodeOpaque(digest);
    }
}
