/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
 *
 *  Copyright (c) 2009-2011 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class XDR_sig_list {
    private final List<XDR_sig_val> sigs;

    public XDR_sig_list(XDRGetter buf) throws IOException {
        sigs = new ArrayList<XDR_sig_val>();
        int count = buf.getInt();
        for (int i = 0; i < count; i++) {
            sigs.add(new XDR_sig_val(buf));
        }
    }

    public List<XDR_sig_val> getSigs() {
        return Collections.unmodifiableList(sigs);
    }
}
