/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 3
 *
 *  Copyright (c) 2007 Carnegie Mellon University
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

import edu.cmu.cs.diamond.opendiamond.glue.*;

public class Result {
    final private Map<String, byte[]> attributes = new HashMap<String, byte[]>();

    final private String objectID;

    Result(SWIGTYPE_p_void obj_handle, String objectID) {
        copyValuesFromHandle(obj_handle);
        this.objectID = objectID;
        OpenDiamond.ls_release_object(null, obj_handle);
    }

    public byte[] getData() {
        return getValue("");
    }

    public byte[] getValue(String key) {
        return attributes.get(key);
    }

    private static byte[] extractData(int len, SWIGTYPE_p_p_unsigned_char data) {
        byte[] result;
        result = new byte[len];

        byteArray d = OpenDiamond.deref_data_cookie(data);
        // XXX slow
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) d.getitem(i);
        }
        return result;
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    private void copyValuesFromHandle(SWIGTYPE_p_void obj_handle) {
        // make cookies
        SWIGTYPE_p_p_void cookie = null;
        SWIGTYPE_p_p_char name = null;
        SWIGTYPE_p_p_unsigned_char data = null;

        try {
            cookie = OpenDiamond.create_void_cookie();
            name = OpenDiamond.create_char_cookie();
            data = OpenDiamond.create_data_cookie();
            long len[] = { 0 };

            // first
            int err = OpenDiamond.lf_first_attr(obj_handle, name, len, data,
                    cookie);
            while (err == 0) {
                String localName = OpenDiamond.deref_char_cookie(name);
                byte[] localData = extractData((int) len[0], data);
                attributes.put(localName, localData);

                err = OpenDiamond.lf_next_attr(obj_handle, name, len, data,
                        cookie);
            }

            // don't forget the data
            err = OpenDiamond.lf_ref_attr(obj_handle, "", len, data);
            if (err == 0) {
                attributes.put("", extractData((int) len[0], data));
            }
        } finally {
            OpenDiamond.delete_void_cookie(cookie);
            OpenDiamond.delete_char_cookie(name);
            OpenDiamond.delete_data_cookie(data);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Result [");

        for (String name : getKeys()) {
            byte value[] = getValue(name);
            sb.append(" '" + name + "'");
            if (value.length == 0) {
                sb.append(":" + "''");
            } else if (name.endsWith(".int")) {
                sb.append(":" + Util.extractInt(value));
            } else if (name.endsWith("-Name")) {
                sb.append(":'" + Util.extractString(value) + "'");
            } else if (name.endsWith(".time")) {
                sb.append(":" + Util.extractLong(value));
            }
            sb.append(" (" + value.length + ")");
        }
        sb.append(" (");
        sb.append(objectID);
        sb.append(")");
        sb.append(" ]");
        return sb.toString();
    }

    public String getServerName() {
        return Util.extractString(getValue("Device-Name"));
    }

    public String getObjectName() {
        return Util.extractString(getValue("Display-Name"));
    }

    String getObjectID() {
        return objectID;
    }
}
