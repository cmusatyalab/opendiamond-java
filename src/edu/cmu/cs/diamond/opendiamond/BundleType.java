/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 6
 *
 *  Copyright (c) 2011 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

public enum BundleType {
    CODEC("codec", "codec"),
    PREDICATE("predicate", "pred");

    // top-level XML manifest tag
    private final String tag;

    // bundle file extension without the dot
    private final String extension;

    private BundleType(String tag, String extension) {
        this.tag = tag;
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    static BundleType fromTag(String tag) {
        for (BundleType type : BundleType.values()) {
            if (type.tag.equals(tag)) {
                return type;
            }
        }
        return null;
    }
}
