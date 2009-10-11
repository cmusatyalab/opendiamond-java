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

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Searchlet {
    final private List<Filter> filters;

    final private List<String> dependencies;

    public Searchlet(List<Filter> filters, List<String> applicationDependencies) {
        this.filters = Collections.unmodifiableList(new ArrayList<Filter>(
                filters));
        this.dependencies = new ArrayList<String>(applicationDependencies);
    }

    File createFilterSpecFile() throws IOException {
        File out = File.createTempFile("filterspec", ".txt");
        out.deleteOnExit();

        Writer w = new FileWriter(out);
        w.write(toString());
        w.close();
        return out;
    }

    File[] createFilterFiles() throws IOException {
        File result[] = new File[filters.size()];

        int i = 0;
        for (Filter f : filters) {
            File file = File.createTempFile("filter", ".bin");
            file.deleteOnExit();

            DataOutputStream out = new DataOutputStream(new FileOutputStream(
                    file));
            out.write(f.getFilterCode().getBytes());
            out.close();

            result[i++] = file;
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Filter f : filters) {
            sb.append(f.toString());
        }

        if (dependencies != null) {
            sb.append("FILTER APPLICATION\n");
            for (String d : dependencies) {
                sb.append("REQUIRES " + d + "\n");
            }
        }

        return sb.toString();
    }

    List<Filter> getFilters() {
        return filters;
    }
}
