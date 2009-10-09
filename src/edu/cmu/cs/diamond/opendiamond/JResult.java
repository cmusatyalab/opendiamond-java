package edu.cmu.cs.diamond.opendiamond;

import java.util.Map;

class JResult extends Result {

    final private String hostname;

    final private int searchID;

    public JResult(Map<String, byte[]> attributes, String hostname, int searchID) {
        this.attributes.putAll(attributes);

        this.hostname = hostname;

        this.searchID = searchID;
    }

    String getHostname() {
        return hostname;
    }

    int getSearchID() {
        return searchID;
    }
}
