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

/**
 * A Diamond filter. Use with {@link SearchFactory} to perform Diamond searches.
 * 
 */
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

    final private byte encodedBlob[];

    final private byte encodedBlobSig[];

    /**
     * Constructs a new filter with the given parameters (including blob).
     * 
     * @param name
     *            the name of this filter
     * @param code
     *            the binary code that implements the Filter
     * @param evalFunction
     *            the name of the eval function in the shared object referred to
     *            by <code>code</code>
     * @param initFunction
     *            the name of the init function in the shared object referred to
     *            by <code>code</code>
     * @param finiFunction
     *            the name of the fini function in the shared object referred to
     *            by <code>code</code>
     * @param threshold
     *            a value that sets the filter drop threshold
     * @param dependencies
     *            a list of other filter names that this filter depends on
     * @param arguments
     *            a list of arguments to the filter
     * @param blob
     *            a binary argument to this filter
     */
    public Filter(String name, FilterCode code, String evalFunction,
            String initFunction, String finiFunction, int threshold,
            Collection<String> dependencies, List<String> arguments,
            byte blob[]) {

        // TODO check for valid characters as in filter_spec.l
        this.name = name.trim();
        this.code = code;
        this.evalFunction = evalFunction.trim();
        this.initFunction = initFunction.trim();
        this.finiFunction = finiFunction.trim();
        this.threshold = threshold;
        this.merit = 100;

        this.dependencies = new ArrayList<String>(dependencies);
        this.arguments = new ArrayList<String>(arguments);

        XDR_sig_val sig = XDR_sig_val.createSignature(blob);
        encodedBlobSig = new XDR_blob_sig(name, sig).encode();
        encodedBlob = new XDR_blob(name, blob).encode();
    }

    /**
     * Constructs a new Filter with the given parameters.
     * 
     * @param name
     *            the name of the new filter
     * @param code
     *            the binary code that implements the filter
     * @param evalFunction
     *            the name of the eval function in the shared object referred to
     *            by <code>code</code>
     * @param initFunction
     *            the name of the init function in the shared object referred to
     *            by <code>code</code>
     * @param finiFunction
     *            the name of the fini function in the shared object referred to
     *            by <code>code</code>
     * @param threshold
     *            a value that sets the filter drop threshold
     * @param dependencies
     *            a list of other filter names that this filter depends on
     * @param arguments
     *            a list of arguments to the filter
     */
    public Filter(String name, FilterCode code, String evalFunction,
            String initFunction, String finiFunction, int threshold,
            Collection<String> dependencies, List<String> arguments) {
        this(name, code, evalFunction, initFunction, finiFunction, threshold,
                dependencies, arguments, new byte[0]);
    }

    public Filter(FilterCode code, byte encodedBlob[], byte encodedBlobSig[]) {
    	this.code = code;
    	this.encodedBlob = encodedBlob;
    	this.encodedBlobSig = encodedBlobSig;
        this.name = "";
        this.evalFunction = "";
        this.initFunction = "";
        this.finiFunction = "";
        this.threshold = 0;
        this.merit = 100;

        this.dependencies = null;
        this.arguments = null;   	
    }
    
    @Override
    public String toString() {
        return getName() + ", encoded bloblen: " + getEncodedBlob().length;
    }

    String getFspec() {
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

    byte[] getEncodedBlobSig() {
        return encodedBlobSig;
    }

    byte[] getEncodedBlob() {
        return encodedBlob;
    }
}
