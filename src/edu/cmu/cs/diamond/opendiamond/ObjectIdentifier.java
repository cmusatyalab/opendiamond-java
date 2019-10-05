/*
 *  The OpenDiamond Platform for Interactive Search
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

/**
 * Opaque identifier for a server object.
 */
public class ObjectIdentifier {

    private final String hostname;

    private final String objectID;

    private final String deviceName;

    ObjectIdentifier(String objectID, String deviceName, String hostname) {
        this.objectID = objectID;
        this.deviceName = deviceName;
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getObjectID() {
        return objectID;
    }
}
