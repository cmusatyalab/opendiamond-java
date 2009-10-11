package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XDR_attr_list {

    private final List<XDR_attribute> attributes = new ArrayList<XDR_attribute>();

    public XDR_attr_list(XDRGetter data) throws IOException {
        int len = data.getInt();

        for (int i = 0; i < len; i++) {
            attributes.add(new XDR_attribute(data));
        }
    }

    public List<XDR_attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public Map<String, byte[]> createMap() {
        HashMap<String, byte[]> result = new HashMap<String, byte[]>();

        for (XDR_attribute a : attributes) {
            result.put(a.getName(), a.getData());
        }

        return result;
    }
}
