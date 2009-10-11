package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

class Cookie {
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
}
