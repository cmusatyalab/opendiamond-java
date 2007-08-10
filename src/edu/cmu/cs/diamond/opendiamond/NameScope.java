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

public class NameScope extends Scope {
    private final String name;

    NameScope(String name, groupidArray gids, int size) {
        super(gids, size);
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("scope: " + name + " (gids: "
                + getGidsSize() + ")");
        return sb.toString();
    }

    @Override
    public String getName() {
        return name;
    }
}
