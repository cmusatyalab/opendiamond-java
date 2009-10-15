/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
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

/**
 * Interface for classes that define binary operations on named pairs of
 * doubles.
 * 
 * @see Search#mergeSessionVariables(java.util.Map, DoubleComposer)
 * 
 */
public interface DoubleComposer {
    /**
     * Performs a binary operation on a named pair of doubles.
     * 
     * @param key
     *            the name of the pair of doubles
     * @param a
     *            the first double
     * @param b
     *            the second double
     * @return the new double value
     */
    double compose(String key, double a, double b);
}
