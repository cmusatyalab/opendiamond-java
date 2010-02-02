/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
 *
 *  Copyright (c) 2009 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;

/**
 * Exception thrown when a method (except <code>close()</code>) is called on a
 * {@link Search} that has been closed.
 */
public class SearchClosedException extends IOException {
    SearchClosedException() {
        super();
    }

    SearchClosedException(Throwable cause) {
        super(cause);
    }
}
