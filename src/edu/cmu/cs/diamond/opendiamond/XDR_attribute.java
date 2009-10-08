package edu.cmu.cs.diamond.opendiamond;

public class XDR_attribute {

    private final static int MAX_ATTRIBUTE_NAME = 256;

    private final String name;

    private final byte[] data;

    public XDR_attribute(XDRGetter buf) {
        name = buf.getString(MAX_ATTRIBUTE_NAME);
        data = buf.getOpaque();
    }

    public byte[] getData() {
        return data;
    }

    public String getName() {
        return name;
    }

}
