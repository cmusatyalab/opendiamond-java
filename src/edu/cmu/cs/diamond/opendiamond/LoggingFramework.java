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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class LoggingFramework {

	XMLLogger searchLogger;
	Logger javaLogger;

	LoggingFramework(String logMessage) {
		try {
			this.searchLogger = new XMLLogger();
			this.javaLogger = this.searchLogger.getSearchLogger();
			this.javaLogger.log(Level.FINEST, "Initializing new LoggingFramework for a new search.", logMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void shutdown() {
		javaLogger.log(Level.FINEST, "Shutting down logging framework.");
		searchLogger.shutdown(null);
	}

	void saveFilters(List<Filter> filters) {
		javaLogger.log(Level.FINEST, "Saving filters.", searchLogger.saveFilters(filters));
	}

	void saveAttributes(Set<String> desiredAttributes) {
		javaLogger.log(Level.FINEST, "Saving attributes.", searchLogger.saveAttributes(desiredAttributes));
	}

	void saveSessionVariables(Map<String, Double> sessionVariables) {
		javaLogger.log(Level.FINEST, "Saving Session Variables.", searchLogger.saveSessionVariables(sessionVariables));
	}

	void stoppedSearch(Throwable cause) {
		javaLogger.log(Level.FINEST, "Search has stopped.", searchLogger.stopSearch());
		searchLogger.shutdown(cause);
	}

	void startedSearch() {
		javaLogger.log(Level.FINEST, "Search has started.", searchLogger.startSearch());
	}

	ServerStatistics getCurrentTotalStatistics() {
		ServerStatistics returnValue = searchLogger.getCurrentTotalStatistics();
		javaLogger.log(Level.FINEST, "Returning current statistics.", returnValue);
		return returnValue;
	}

	void updateStatistics(Map<String, ServerStatistics> result) {
		javaLogger.log(Level.FINEST, "Updating server statistics.", result);
		searchLogger.updateStatistics(result);
	}

	void saveGetNewResult(Result result) {
		javaLogger.log(Level.FINEST, "Got new result.", searchLogger.saveGetNewResult(result));
	}

	void logNoMoreResults() {
		javaLogger.log(Level.FINEST, "NO_MORE_RESULTS");
	}

	void saveCookieMap(CookieMap cookieMap) {
		javaLogger.log(Level.FINEST, "Saving the cookiemap/megacookie.", searchLogger.saveCookieMap(cookieMap));
	}

	public void saveApplicationDependencies(List<String> applicationDependencies) {
		javaLogger.log(Level.FINEST, "Saving application dependencies.", searchLogger.saveApplicationDependencies(applicationDependencies));
	}
}
