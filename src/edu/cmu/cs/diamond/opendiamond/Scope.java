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

import edu.cmu.cs.diamond.opendiamond.glue.groupidArray;

public abstract class Scope {

    protected final groupidArray gids;
    protected final int gidsSize;

    protected Scope(groupidArray gids, int gidsSize) {
        this.gids = gids;
        this.gidsSize = gidsSize;
    }
    
    groupidArray getGids() {
        return gids;
    }

    int getGidsSize() {
        return gidsSize;
    }

    abstract public String getName();
}
