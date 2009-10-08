package edu.cmu.cs.diamond.opendiamond;

import java.util.Map;

public class JResult extends Result {

    final private Connection connection;

    public JResult(Map<String, byte[]> attributes, Connection connection) {
        this.attributes.putAll(attributes);

        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
