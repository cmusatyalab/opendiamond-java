package edu.cmu.cs.diamond.opendiamond;

import java.util.Map;

public class JResult extends Result {

    final private String hostname;

    public JResult(Map<String, byte[]> attributes, String hostname) {
        this.attributes.putAll(attributes);

        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }
}
