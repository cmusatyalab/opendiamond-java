/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 5
 *
 *  Copyright (c) 2009-2010 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

abstract class LoggingFramework {

    private static final LoggingFramework NULL_LOGGER = new LoggingFramework() {
        @Override
        public void updateStatistics(Map<String, ServerStatistics> result) {
        }

        @Override
        public void stoppedSearch(Throwable cause) {
        }

        @Override
        public void startedSearch() {
        }

        @Override
        public void shutdown(Throwable cause) {
        }

        @Override
        public void saveSessionVariables(Map<String, Double> sessionVariables) {
        }

        @Override
        public void saveSearchFactory(SearchFactory searchFactory,
                Set<String> desiredAttributes) {
        }

        @Override
        public void saveGetNewResult(Result result) {
        }

        @Override
        public void logNoMoreResults() {
        }
    };

    public abstract void saveSearchFactory(SearchFactory searchFactory,
            Set<String> desiredAttributes) throws IOException;

    public abstract void logNoMoreResults();

    public abstract void saveGetNewResult(Result result);

    public abstract void updateStatistics(Map<String, ServerStatistics> result);

    public abstract void startedSearch();

    public abstract void stoppedSearch(Throwable cause);

    public abstract void saveSessionVariables(
            Map<String, Double> sessionVariables) throws IOException;

    public abstract void shutdown(Throwable cause);

    public static LoggingFramework createLoggingFramework(String message)
            throws IOException {
        boolean enabled = Boolean.parseBoolean(System.getProperty(
                "edu.cmu.cs.diamond.opendiamond.loggingframework.enabled",
                "true"));

        if (enabled) {
            return new XMLLogger(message);
        } else {
            return NULL_LOGGER;
        }
    }
}
