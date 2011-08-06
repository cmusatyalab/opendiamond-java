/*
 *  The OpenDiamond Platform for Interactive Search
 *
 *  Copyright (c) 2007, 2009-2011 Carnegie Mellon University
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

/**
 * A Diamond filter. Use with {@link SearchFactory} to perform Diamond searches.
 * 
 */
public class Filter {
    final private FilterCode code;

    final private List<String> dependencies;

    final private List<String> arguments;

    final private String name;

    final private double minScore;

    final private double maxScore;

    final private byte blob[];

    final private Signature blobSig;

    /**
     * Constructs a new filter with the given parameters (including blob
     * and maxScore).
     * 
     * @param name
     *            the name of this filter
     * @param code
     *            the binary code that implements the Filter
     * @param minScore
     *            the filter score below which an object will be dropped
     * @param maxScore
     *            the filter score above which an object will be dropped
     * @param dependencies
     *            a list of other filter names that this filter depends on
     * @param arguments
     *            a list of arguments to the filter
     * @param blob
     *            a binary argument to this filter
     */
    public Filter(String name, FilterCode code, double minScore,
            double maxScore, Collection<String> dependencies,
            List<String> arguments, byte blob[]) {

        this.name = name.trim();
        this.code = code;
        this.minScore = minScore;
        this.maxScore = maxScore;

        this.dependencies = new ArrayList<String>(dependencies);
        this.arguments = new ArrayList<String>(arguments);

        this.blob = blob;

        blobSig = new Signature(blob);
    }

    /**
     * Constructs a new Filter with the given parameters (including
     * maxScore).
     * 
     * @param name
     *            the name of the new filter
     * @param code
     *            the binary code that implements the filter
     * @param minScore
     *            the filter score below which an object will be dropped
     * @param maxScore
     *            the filter score above which an object will be dropped
     * @param dependencies
     *            a list of other filter names that this filter depends on
     * @param arguments
     *            a list of arguments to the filter
     */
    public Filter(String name, FilterCode code, double minScore,
            double maxScore, Collection<String> dependencies,
            List<String> arguments) {
        this(name, code, minScore, maxScore, dependencies, arguments,
                new byte[0]);
    }

    /**
     * Constructs a new filter with the given parameters (including blob).
     * 
     * @param name
     *            the name of this filter
     * @param code
     *            the binary code that implements the Filter
     * @param minScore
     *            the filter score below which an object will be dropped
     * @param dependencies
     *            a list of other filter names that this filter depends on
     * @param arguments
     *            a list of arguments to the filter
     * @param blob
     *            a binary argument to this filter
     */
    public Filter(String name, FilterCode code, double minScore,
            Collection<String> dependencies, List<String> arguments,
            byte blob[]) {
        this(name, code, minScore, Double.POSITIVE_INFINITY, dependencies,
                arguments, blob);
    }

    /**
     * Constructs a new Filter with the given parameters.
     * 
     * @param name
     *            the name of the new filter
     * @param code
     *            the binary code that implements the filter
     * @param minScore
     *            the filter score below which an object will be dropped
     * @param dependencies
     *            a list of other filter names that this filter depends on
     * @param arguments
     *            a list of arguments to the filter
     */
    public Filter(String name, FilterCode code, double minScore,
            Collection<String> dependencies, List<String> arguments) {
        this(name, code, minScore, Double.POSITIVE_INFINITY, dependencies,
                arguments, new byte[0]);
    }

    @Override
    public String toString() {
        return getName() + ", bloblen: " + blob.length;
    }

    /**
     * Gets the name of this filter.
     * 
     * @return the name of this filter
     */
    public String getName() {
        return name;
    }

    FilterCode getFilterCode() {
        return code;
    }

    Signature getBlobSig() {
        return blobSig;
    }

    List<String> getDependencies() {
        return dependencies;
    }

    List<String> getArguments() {
        return arguments;
    }

    double getMinScore() {
        return minScore;
    }

    double getMaxScore() {
        return maxScore;
    }

    byte[] getBlob() {
        return blob;
    }
}
