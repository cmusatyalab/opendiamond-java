package edu.cmu.cs.diamond.opendiamond;

import java.nio.ByteBuffer;

public interface XDREncodeable {
    ByteBuffer encode();
}
