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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class LoggingFramework {

    private final XMLLogger searchLogger;

    private final Logger javaLogger;

    private final Object lock = new Object();

    LoggingFramework(String logMessage) throws IOException {
        synchronized (lock) {
            this.searchLogger = new XMLLogger();
            this.javaLogger = this.searchLogger.getSearchLogger();
            this.javaLogger.log(Level.FINEST,
                    "Initializing new LoggingFramework for a new search.",
                    logMessage);
        }
    }

    void shutdown() {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Shutting down logging framework.");
            searchLogger.shutdown(null);
        }
    }

    private void saveFilters(List<Filter> filters) throws IOException {
        for (Filter f : filters) {
            javaLogger.log(Level.FINEST, "Saving filter.", searchLogger
                    .saveFilter(f));
        }
    }

    private void saveAttributes(Set<String> desiredAttributes)
            throws IOException {
        javaLogger.log(Level.FINEST, "Saving attributes.", searchLogger
                .saveAttributes(desiredAttributes));
    }

    void saveSessionVariables(Map<String, Double> sessionVariables)
            throws IOException {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Saving Session Variables.",
                    searchLogger.saveSessionVariables(sessionVariables));
        }
    }

    void stoppedSearch(Throwable cause) {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Search has stopped.", searchLogger
                    .stopSearch());
            searchLogger.shutdown(cause);
        }
    }

    void startedSearch() {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Search has started.", searchLogger
                    .startSearch());
        }
    }

    ServerStatistics getCurrentTotalStatistics() {
        synchronized (lock) {
            ServerStatistics returnValue = searchLogger
                    .getCurrentTotalStatistics();
            javaLogger.log(Level.FINEST, "Returning current statistics.",
                    returnValue);
            return returnValue;
        }
    }

    void updateStatistics(Map<String, ServerStatistics> result) {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Updating server statistics.",
                    searchLogger.updateStatistics(result));
        }
    }

    void saveGetNewResult(Result result) {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Got new result.", searchLogger
                    .saveGetNewResult(result));
        }
    }

    void logNoMoreResults() {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "NO_MORE_RESULTS");
        }
    }

    private void saveCookieMap(CookieMap cookieMap) throws IOException {
        if (cookieMap.getMegaCookie() != null) {
            javaLogger.log(Level.FINEST, "Saving the cookiemap/megacookie.",
                    searchLogger.saveCookieMap(cookieMap));
        } else {
            javaLogger.log(Level.FINEST, "Null megacookie.");
        }
    }

    private void saveApplicationDependencies(
            List<String> applicationDependencies) throws IOException {
        javaLogger.log(Level.FINEST, "Saving application dependencies.",
                searchLogger
                        .saveApplicationDependencies(applicationDependencies));
    }

    void saveSearchFactory(SearchFactory searchFactory,
            Set<String> desiredAttributes) throws IOException {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Saving search factory.");
            saveFilters(searchFactory.getFilters());
            saveCookieMap(searchFactory.getCookieMap());
            saveApplicationDependencies(searchFactory
                    .getApplicationDependencies());
            saveAttributes(desiredAttributes);
        }
    }
}
