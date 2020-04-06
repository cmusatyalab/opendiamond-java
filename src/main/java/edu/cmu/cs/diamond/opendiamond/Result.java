/*
 *  The OpenDiamond Platform for Interactive Search
 *
 *  Copyright (c) 2007, 2009 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A single result from a {@link Search}, consisting of key-value pairs
 * (attributes).
 *
 */
public class Result {
    final private Map<String, byte[]> attributes = new HashMap<String, byte[]>();

    private final ObjectIdentifier objectIdentifier;

    public Result(ObjectIdentifier objectIdentifier) {
        this.objectIdentifier = objectIdentifier;
    }

    Result(Map<String, byte[]> attributes, String hostname) {
        this.attributes.putAll(attributes);

        objectIdentifier = new ObjectIdentifier(Util
                .extractString(getValue("_ObjectID")), Util.extractString(getValue("Device-Name")), hostname);
    }

    /**
     * Gets the "data" attribute of this result. Equivalent to
     * <code>getValue("")</code>.
     *
     * @return a byte array with the data of this result
     */
    public byte[] getData() {
        return getValue("");
    }

    /**
     * Gets the value associated with a particular key.
     *
     * @param key
     *            the name of the attribute to get the value for
     * @return the value
     */
    public byte[] getValue(String key) {
        byte[] v = attributes.get(key);
        if (v == null) {
            return null;
        } else {
            byte result[] = new byte[v.length];
            System.arraycopy(v, 0, result, 0, v.length);
            return result;
        }
    }

    public String getStrValue(String name) {
        byte value[] = getValue(name);
        try {
        if (value.length == 0) {
            return "";
        } else if (name.endsWith(".int")) {
            return Integer.toString(Util.extractInt(value));
        } else if (name.endsWith("-Name") || name.equals("_ObjectID")) {
            return Util.extractString(value);
        } else if (name.endsWith(".time")) {
            return Long.toString(Util.extractLong(value));
        } else if (name.endsWith("score")) {
            return Util.extractString(value);
        } else if (name.endsWith(".json")) {
            try {
                System.out.println("json string: " + Util.extractString(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        } else {
            return "";
        }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Gets the keys of this result, for use in <code>getValue</code>.
     *
     * @return a set of keys
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Result [");

        for (String name : getKeys()) {
            byte value[] = getValue(name);
            sb.append(" '" + name + "'");
            sb.append(":" + getStrValue(name));
            sb.append(" (" + value.length + ")");
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * Gets the "server name" of this result. The server name is a
     * server-defined string. Equivalent to
     * <code>Util.extractString(getValue("Device-Name"))</code>.
     *
     * @return the server-defined server name of this result
     */
    public String getServerName() {
        return Util.extractString(getValue("Device-Name"));
    }

    /**
     * Gets the "name" of this result. The name is a server-defined string.
     * Equivalent to <code>Util.extractString(getValue("Display-Name"))</code>.
     *
     * @return the server-defined name of this result
     */
    public String getName() {
        return Util.extractString(getValue("Display-Name"));
    }

    /**
     * Gets the server-defined identifier for this result. Useful for passing to
     * {@link SearchFactory#generateResult(ObjectIdentifier, Set)}.
     *
     * @return the identifier for this result
     */
    public ObjectIdentifier getObjectIdentifier() {
        return objectIdentifier;
    }
}
