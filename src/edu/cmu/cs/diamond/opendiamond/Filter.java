/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
 *
 *  Copyright (c) 2007, 2009 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Filter {
    final private FilterCode code;

    final private List<String> dependencies;

    final private List<String> arguments;

    final private String evalFunction;

    final private String finiFunction;

    final private String initFunction;

    final private int merit;

    final private String name;

    final private int threshold;

    final private byte blob[];

    public Filter(String name, FilterCode code, String evalFunction,
            String initFunction, String finiFunction, int threshold,
            Collection<String> dependencies, List<String> arguments, int merit,
            byte blob[]) {

        // TODO check for valid characters as in filter_spec.l
        this.name = name.trim();
        this.code = code;
        this.evalFunction = evalFunction.trim();
        this.initFunction = initFunction.trim();
        this.finiFunction = finiFunction.trim();
        this.threshold = threshold;
        this.merit = merit;

        this.dependencies = new ArrayList<String>(dependencies);
        this.arguments = new ArrayList<String>(arguments);

        this.blob = new byte[blob.length];
        System.arraycopy(blob, 0, this.blob, 0, blob.length);
    }

    public Filter(String name, FilterCode code, String evalFunction,
            String initFunction, String finiFunction, int threshold,
            Collection<String> dependencies, List<String> arguments, int merit) {
        this(name, code, evalFunction, initFunction, finiFunction, threshold,
                dependencies, arguments, merit, new byte[0]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("FILTER " + name + "\n");
        sb.append("THRESHOLD " + threshold + "\n");
        sb.append("MERIT " + merit + "\n");
        sb.append("EVAL_FUNCTION " + evalFunction + "\n");
        sb.append("INIT_FUNCTION " + initFunction + "\n");
        sb.append("FINI_FUNCTION " + finiFunction + "\n");

        for (String arg : arguments) {
            sb.append("ARG " + arg + "\n");
        }
        for (String req : dependencies) {
            sb.append("REQUIRES " + req + "\n");
        }

        return sb.toString();
    }

    byte[] getBlob() {
        return blob;
    }

    String getName() {
        return name;
    }

    FilterCode getFilterCode() {
        return code;
    }
}
