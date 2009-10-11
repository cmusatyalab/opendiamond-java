package edu.cmu.cs.diamond.opendiamond;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class XDR_diamond_session_var implements XDREncodeable {

    private final String name;
    private final double value;

    public XDR_diamond_session_var(XDRGetter data) throws IOException {
        name = data.getString();
        value = data.getDouble();
    }

    public XDR_diamond_session_var(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    @Override
    public byte[] encode() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        byte encodedName[] = XDREncoders.encodeString(name);
        try {
            out.write(encodedName);
            out.writeDouble(value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
