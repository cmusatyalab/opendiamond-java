/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
 *
 *  Copyright (c) 2009 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class Cookie {
    final static String BEGIN_COOKIE = "-----BEGIN OPENDIAMOND SCOPECOOKIE-----";

    final static String END_COOKIE = "-----END OPENDIAMOND SCOPECOOKIE-----";

    private final List<String> servers;

    private final String rawCookie;

    private final int version;

    private final UUID serial;

    private final byte[] keyId;

    private final XMLGregorianCalendar expires;

    private final String scopeData;

    public Cookie(String s) throws IOException {
        if (!s.startsWith(BEGIN_COOKIE)) {
            throw new IllegalArgumentException("String does not begin with "
                    + BEGIN_COOKIE);
        }

        rawCookie = s;

        int end = s.indexOf(END_COOKIE);
        s = s.substring(BEGIN_COOKIE.length(), end);

        String cookie = new String(Base64.decode(s));
        // System.out.println(cookie);

        // split into header and body
        int boundary = cookie.indexOf("\n\n");
        String header = cookie.substring(0, boundary);
        scopeData = cookie.substring(boundary + 2);

        // get properties
        boolean hasVersion = false;
        int version = 0;
        UUID serial = null;
        byte keyId[] = null;
        XMLGregorianCalendar expires = null;
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
            } else if (key.equals("KeyId")) {
                keyId = hexDecode(val);
            } else if (key.equals("Expires")) {
                DatatypeFactory df;
                try {
                    df = DatatypeFactory.newInstance();
                    expires = df.newXMLGregorianCalendar(val);
                } catch (DatatypeConfigurationException e) {
                    e.printStackTrace();
                }
            } else if (key.equals("Servers")) {
                servers = Arrays.asList(val.split(";|,"));
            }
        }

        // commit
        this.version = version;
        this.serial = serial;
        this.servers = Collections.unmodifiableList(servers);
        this.keyId = keyId;
        this.expires = expires;

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
        if (keyId == null) {
            throw new IllegalArgumentException("missing KeyId");
        }
        if (expires == null) {
            throw new IllegalArgumentException("missing Expires");
        }
    }

    private byte[] hexDecode(String string) {
        byte input[] = string.getBytes();
        byte result[] = new byte[input.length / 2];

        for (int i = 0; i < input.length; i += 2) {
            String s = new String(input, i, 2);
            result[i / 2] = (byte) Integer.parseInt(s, 16);
        }

        return result;
    }

    public List<String> getServers() {
        return servers;
    }

    public String getCookie() {
        return rawCookie;
    }

    @Override
    public String toString() {
        return "servers: " + servers + ", version: " + version + ", serial: "
                + serial + ", keyId: " + Arrays.toString(keyId) + ", expires: "
                + expires;
    }

    public String getScopeData() {
        return scopeData;
    }

    public static Map<String, Cookie> createDefaultCookieMap()
            throws IOException {
        // get newscope file
        File home = new File(System.getProperty("user.home"));
        File diamondDir = new File(home, ".diamond");
        File newscope = new File(diamondDir, "NEWSCOPE");

        InputStream in = new FileInputStream(newscope);
        String megacookie = new String(Util.readFully(in));
        in.close();

        Map<String, Cookie> cookieMap = createCookieMap(megacookie);

        return cookieMap;
    }

    public static Map<String, Cookie> createCookieMap(String megacookie)
            throws IOException {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();

        // fill map from hostnames to cookies
        List<String> cookies = splitCookies(megacookie);
        for (String s : cookies) {
            Cookie c = new Cookie(s);
            // System.out.println(c);

            List<String> servers = c.getServers();
            for (String server : servers) {
                cookieMap.put(server, c);
            }
        }
        return cookieMap;
    }

    static List<String> splitCookies(String megacookie) {
        List<String> result = new ArrayList<String>();

        String lines[] = megacookie.split("\n");
        boolean inCookie = false;
        StringBuilder sb = null;
        for (String l : lines) {
            if (l.equals(BEGIN_COOKIE)) {
                inCookie = true;
                sb = new StringBuilder();
            }

            if (!inCookie) {
                continue;
            }

            sb.append(l);
            sb.append('\n');

            if (!l.equals(END_COOKIE)) {
                continue;
            }

            inCookie = false;
            result.add(sb.toString());
        }

        return result;
    }
}
