/*
 *  The OpenDiamond Platform for Interactive Search
 *
 *  Copyright (c) 2007, 2009-2020 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

public final class LabeledExample {
    private final ObjectIdentifier objectId;
    private final int label;

    public LabeledExample(ObjectIdentifier objectId, int label) {
        this.objectId = objectId;
        this.label = label;
    }

    public ObjectIdentifier getObjectId() {
        return objectId;
    }

    public int getLabel() {
        return label;
    }
}
