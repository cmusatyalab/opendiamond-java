/*
 *  The OpenDiamond Platform for Interactive Search
 *
 *  Copyright (c) 2009-2010 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Cookie {
    final static String BEGIN_COOKIE = "-----BEGIN OPENDIAMOND SCOPECOOKIE-----";

    final static String END_COOKIE = "-----END OPENDIAMOND SCOPECOOKIE-----";

    private final List<String> servers;

    private final String rawCookie;

    private final int version;

    private final UUID serial;

    private final String expires;

    private final String scopeData;

    private final Boolean proxyFlag;

    public Cookie(String s, final Boolean proxyFlag) throws IOException {
        if (!s.startsWith(BEGIN_COOKIE)) {
            throw new IllegalArgumentException("String does not begin with "
                    + BEGIN_COOKIE);
        }

        rawCookie = s;

        int end = s.indexOf(END_COOKIE);
        s = s.substring(BEGIN_COOKIE.length(), end);

        String cookie = new String(Base64.decode(s), "UTF-8");
        // System.out.println(cookie);

        // split into header and body
        int boundary = cookie.indexOf("\n\n");
        String header = cookie.substring(0, boundary);
        scopeData = cookie.substring(boundary + 2);

        // get properties
        boolean hasVersion = false;
        int version = 0;
        UUID serial = null;
        String expires = null;
        List<String> servers = null;

        for (String line : header.split("\n")) {
            String ss[] = line.split(":", 2);

            String key = ss[0];
            String val = null;
            if (ss.length > 1) {
                val = ss[1].trim();
            }
            if (key.equals("Version")) {
                hasVersion = true;
                version = Integer.parseInt(val);
            } else if (key.equals("Serial")) {
                serial = UUID.fromString(val);
            } else if (key.equals("Expires")) {
                expires = val;
            } else if (key.equals("Servers")) {
                servers = Arrays.asList(val.split(";|,"));
            }
        }

        // commit
        this.version = version;
        this.serial = serial;
        this.servers = Collections.unmodifiableList(servers);
        this.expires = expires;
        this.proxyFlag = proxyFlag;

        // check
        if (!hasVersion) {
            throw new IllegalArgumentException("missing Version");
        }
        if (serial == null) {
            throw new IllegalArgumentException("missing Serial");
        }
        if (servers == null) {
            throw new IllegalArgumentException("missing Servers");
        }
        if (expires == null) {
            throw new IllegalArgumentException("missing Expires");
        }
    }

    public Cookie(String s) throws IOException {
        this(s,false);
    }

    public List<String> getServers() {
        return servers;
    }

    public String getCookie() {
        return rawCookie;
    }

    public Boolean getProxyFlag() {
        return proxyFlag;
    }


    @Override
    public String toString() {
        return "servers: " + servers + ", version: " + version + ", serial: "
                + serial + ", expires: " + expires + ", proxyFlag: " + proxyFlag;
    }

    public String getScopeData() {
        return scopeData;
    }
}
