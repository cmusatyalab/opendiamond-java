package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

abstract class LoggingFramework {

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

    public static LoggingFramework createLoggingFramework(String message) throws IOException {
        return new XMLLogger(message);
    }
}