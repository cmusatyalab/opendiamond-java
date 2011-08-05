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

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundleFactory {
    private List<File> bundleDirs;

    private List<File> memberDirs;

    public BundleFactory(List<File> bundleDirs, List<File> memberDirs) {
        this.bundleDirs = new ArrayList<File>(bundleDirs);
        this.memberDirs = Collections.unmodifiableList(new
                ArrayList<File>(memberDirs));
    }

    public List<Bundle> getBundles() {
        List<Bundle> bundles = new ArrayList<Bundle>();
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for (BundleType type : BundleType.values()) {
                    if (name.endsWith("." + type.getExtension())) {
                        return true;
                    }
                }
                return false;
            }
        };
        for (File dir : bundleDirs) {
            for (File file : dir.listFiles(filter)) {
                try {
                    bundles.add(getBundle(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bundles;
    }

    // Does not cache the contents of the file.  The file is reloaded when
    // getFilters() is called.
    public Bundle getBundle(File file) throws IOException {
        Bundle bundle = Bundle.getBundle(file, memberDirs);
        String ext = "." + bundle.getType().getExtension();
        if (!file.getName().endsWith(ext)) {
            throw new BundleFormatException(
                    "Bundle does not have the correct extension " + ext);
        }
        return bundle;
    }

    // Caches the contents of the file in memory.  For use when we may not
    // be able to load the file again later (e.g., it is an URL).
    public Bundle getBundle(InputStream in) throws IOException {
        return Bundle.getBundle(in, memberDirs);
    }
}
