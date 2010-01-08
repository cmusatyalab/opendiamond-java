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
import java.util.Map.Entry;

/**
 * Representation of a set of Diamond "cookies" for authenticating to Diamond
 * servers.
 * 
 */
public class CookieMap {

    private static final CookieMap EMPTY_COOKIE_MAP = new CookieMap();

    private final Map<String, List<Cookie>> cookieMap;

    /**
     * Creates a CookieMap with settings taken from the current environment.
     * 
     * @return a new CookieMap
     * @throws IOException
     *             if the source of the default map cannot be found or used
     */
    public static CookieMap createDefaultCookieMap() throws IOException {
        // get newscope file
        File home = new File(System.getProperty("user.home"));
        File diamondDir = new File(home, ".diamond");
        File newscope = new File(diamondDir, "NEWSCOPE");

        InputStream in = new FileInputStream(newscope);
        String megacookie = new String(Util.readFully(in), "UTF-8");
        in.close();

        return new CookieMap(megacookie);
    }

    /**
     * Returns an empty CookieMap.
     * 
     * @return an empty CookieMap
     */
    public static CookieMap emptyCookieMap() {
        return EMPTY_COOKIE_MAP;
    }

    /**
     * Constructs a new CookieMap from a string representation.
     * 
     * @param megacookie
     *            the string to construct the CookieMap from
     * @throws IOException
     *             if the string representation is malformed
     */
    public CookieMap(String megacookie) throws IOException {
        Map<String, List<Cookie>> cookieMap = new HashMap<String, List<Cookie>>();

        // fill map from hostnames to cookies
        List<String> cookies = splitCookies(megacookie);
        for (String s : cookies) {
            Cookie c = new Cookie(s);
            // System.out.println(c);

            List<String> servers = c.getServers();
            for (String server : servers) {
                List<Cookie> cookieList = cookieMap.get(server);
                if (cookieList == null) {
                    cookieList = new ArrayList<Cookie>();
                    cookieMap.put(server, cookieList);
                }

                cookieList.add(c);
            }
        }

        this.cookieMap = cookieMap;
    }

    private CookieMap() {
        cookieMap = Collections.emptyMap();
    }

    private static List<String> splitCookies(String megacookie) {
        List<String> result = new ArrayList<String>();

        String lines[] = megacookie.split("\n");
        boolean inCookie = false;
        StringBuilder sb = null;
        for (String l : lines) {
            if (l.equals(Cookie.BEGIN_COOKIE)) {
                inCookie = true;
                sb = new StringBuilder();
            }

            if (!inCookie) {
                continue;
            }

            sb.append(l);
            sb.append('\n');

            if (!l.equals(Cookie.END_COOKIE)) {
                continue;
            }

            inCookie = false;
            result.add(sb.toString());
        }

        return result;
    }

    Set<Entry<String, List<Cookie>>> entrySet() {
        return cookieMap.entrySet();
    }

    List<Cookie> get(String host) {
        return cookieMap.get(host);
    }

    @Override
    public String toString() {
        return cookieMap.toString();
    }
}
