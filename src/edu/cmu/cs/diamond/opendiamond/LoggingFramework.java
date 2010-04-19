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

public class LoggingFramework {

	XMLLogger searchLogger;
	Logger javaLogger;

	public LoggingFramework(String logMessage) {
		try {
			this.searchLogger = new XMLLogger();
			this.javaLogger = this.searchLogger.getSearchLogger();
			this.javaLogger.log(Level.FINEST, "Initializing new LoggingFramework for a new search.", logMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveFspec(String fSpec) {
		javaLogger.log(Level.FINEST, "Saving fspec file.", searchLogger.saveFspec(fSpec));
	}

	public void shutdown() {
		javaLogger.log(Level.FINEST, "Shutting down logging framework.");
		searchLogger.shutdown(null);
	}

	public void saveFilters(List<Filter> filters) {
		javaLogger.log(Level.FINEST, "Saving filters.", searchLogger.saveFilters(filters));
	}

	public void saveAttributes(Set<String> desiredAttributes) {
		javaLogger.log(Level.FINEST, "Saving attributes.", searchLogger.saveAttributes(desiredAttributes));
	}

	public void saveSessionVariables(Map<String, Double> sessionVariables) {
		javaLogger.log(Level.FINEST, "Saving Session Variables.", searchLogger.saveSessionVariables(sessionVariables));
	}

	public void stoppedSearch(Throwable cause) {
		javaLogger.log(Level.FINEST, "Search has stopped.", searchLogger.stopSearch());
		searchLogger.shutdown(cause);
	}

	public void startedSearch() {
		javaLogger.log(Level.FINEST, "Search has started.", searchLogger.startSearch());
	}

	public ServerStatistics getCurrentTotalStatistics() {
		ServerStatistics returnValue = searchLogger.getCurrentTotalStatistics();
		javaLogger.log(Level.FINEST, "Returning current statistics.", returnValue);
		return returnValue;
	}

	public void updateStatistics(Map<String, ServerStatistics> result) {
		javaLogger.log(Level.FINEST, "Updating server statistics.", result);
		searchLogger.updateStatistics(result);
	}

	public void saveGetNewResult(Result result) {
		javaLogger.log(Level.FINEST, "Got new result.", searchLogger.saveGetNewResult(result));
	}

	public void logNoMoreResults() {
		javaLogger.log(Level.FINEST, "NO_MORE_RESULTS");
	}
}
