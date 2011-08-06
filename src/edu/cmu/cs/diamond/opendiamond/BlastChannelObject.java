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

import java.io.IOException;

class BlastChannelObject {

    final private XDR_object obj;

    final private String hostname;

    final private IOException exception;

    static final BlastChannelObject NO_MORE_RESULTS = new BlastChannelObject(
            null, null, null);

    public BlastChannelObject(XDR_object obj, String hostname,
            IOException exception) {
        this.obj = obj;
        this.hostname = hostname;
        this.exception = exception;
    }

    public XDR_object getObj() {
        return obj;
    }

    public String getHostname() {
        return hostname;
    }

    public IOException getException() {
        return exception;
    }
}
