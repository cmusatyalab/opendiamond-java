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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

class XMLLogger extends LoggingFramework {

    private static final String APP_SESSION_DIR;

    private static final AtomicInteger searchCounter = new AtomicInteger(0);

    // Initialize the global APP_SESSION_DIR -- only executes once.
    static {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String date = sdf.format(currentDate);
        String diamondLoggingDir = System.getProperty(
                "edu.cmu.cs.diamond.opendiamond.loggingframework.directory",
                Util.joinPaths(System.getProperty("user.home"),
                        "opendiamond-logs"));
        String temp = Util.joinPaths(diamondLoggingDir, date + "_"
                + UUID.randomUUID().toString());
        while (!(new File(temp).mkdirs())) {
            temp = Util.joinPaths(diamondLoggingDir, date + "_"
                    + UUID.randomUUID().toString());
        }
        APP_SESSION_DIR = temp;
    }

    private final String searchDir;

    private int cookieMapCounter;

    private int filterCounter;

    private int attributeCounter;

    private int applicationDependenciesCounter;

    private int sessionCounter;

    private int totalObjects;

    private int processedObjects;

    private int droppedObjects;

    private final Logger javaLogger;

    private final Object lock = new Object();

    XMLLogger(String logMessage) throws IOException {
        synchronized (lock) {
            Date currentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            javaLogger = Logger.getLogger(XMLLogger.class
                    .getPackage().getName());
            String date = sdf.format(currentDate);

            /* create searchDir */
            String temp = Util.joinPaths(APP_SESSION_DIR, date + "_"
                    + searchCounter.getAndIncrement());

            if (!(new File(temp).mkdirs())) {
                throw new IOException();
            }

            searchDir = temp;

            String logFileName = Util.joinPaths(searchDir, "raw_log.log");
            FileHandler fh = new FileHandler(logFileName);
            javaLogger.addHandler(fh);
            XMLFormatter formatter = new XMLFormatter();
            fh.setFormatter(formatter);
            javaLogger.setUseParentHandlers(false);
            javaLogger.setLevel(Level.FINEST);

            this.javaLogger.log(Level.FINEST,
                    "Initializing new LoggingFramework for a new search.",
                    logMessage);
        }
    }

    @Override
    public void shutdown(Throwable cause) {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Shutting down logging framework.");
            if (cause != null)
                javaLogger.log(Level.FINEST,
                        "Logging throwable cause of failure.", Util
                                .getStackTrace(cause));
            for (Handler h : javaLogger.getHandlers()) {
                javaLogger.removeHandler(h);
                h.close();
            }
        }
    }

    private void saveFilters(List<Filter> filters) throws IOException {
        for (Filter f : filters) {
            javaLogger.log(Level.FINEST, "Saving filter.", saveFilter(f));
        }
    }

    private String[] saveFilter(Filter filter) throws IOException {
        FileOutputStream fileOut1, fileOut2, fileOut3, fileOut4, fileOut5;
        String fileName1, fileName2, fileName3, fileName4, fileName5;
        fileName1 = Util.joinPaths(searchDir, "filter_" + filterCounter);
        fileName2 = Util.joinPaths(searchDir, "filtercode_" + filterCounter);
        fileName3 = Util.joinPaths(searchDir, "dependencies_" + filterCounter);
        fileName4 = Util.joinPaths(searchDir, "arguments_" + filterCounter);
        fileName5 = Util.joinPaths(searchDir, "blob_" + filterCounter);
        File f1 = new File(fileName1);
        File f2 = new File(fileName2);
        File f3 = new File(fileName3);
        File f4 = new File(fileName4);
        File f5 = new File(fileName5);

        fileOut1 = new FileOutputStream(f1);
        fileOut2 = new FileOutputStream(f2);
        fileOut3 = new FileOutputStream(f3);
        fileOut4 = new FileOutputStream(f4);
        fileOut5 = new FileOutputStream(f5);
        try {
            fileOut1
                    .write((Base64.encodeBytes(filter.getName().getBytes()) + "\n")
                            .getBytes());
            fileOut1.write((Base64.encodeBytes(filter.getEvalFunction()
                    .getBytes()) + "\n").getBytes());
            fileOut1.write((Base64.encodeBytes(filter.getInitFunction()
                    .getBytes()) + "\n").getBytes());
            fileOut1.write((Base64.encodeBytes(filter.getFiniFunction()
                    .getBytes()) + "\n").getBytes());
            fileOut1.write((Integer.toString(filter.getThreshold()) + "\n")
                    .getBytes());
            fileOut2.write(filter.getFilterCode().getBytes());

            for (String s : filter.getDependencies()) {
                fileOut3.write((Base64.encodeBytes(s.getBytes()) + "\n")
                        .getBytes());
            }

            for (String s : filter.getArguments()) {
                fileOut4.write((Base64.encodeBytes(s.getBytes()) + "\n")
                        .getBytes());
            }

            fileOut5.write(filter.getBlob());
        } finally {
            try {
                fileOut1.close();
            } catch (IOException ignore) {
            }
            try {
                fileOut2.close();
            } catch (IOException ignore) {
            }
            try {
                fileOut3.close();
            } catch (IOException ignore) {
            }
            try {
                fileOut4.close();
            } catch (IOException ignore) {
            }
            try {
                fileOut5.close();
            } catch (IOException ignore) {
            }
        }

        filterCounter++;
        return new String[] { fileName1, fileName2, fileName3, fileName4,
                fileName5 };
    }

    private void saveAttributes(Set<String> desiredAttributes)
            throws IOException {
        FileOutputStream fileOut = null;
        String fileName;
        fileName = Util.joinPaths(searchDir, "attributes_" + attributeCounter);

        try {
            File f = new File(fileName);
            fileOut = new FileOutputStream(f);
            for (String s : desiredAttributes) {
                fileOut.write((s + "\n").getBytes());
            }
        } finally {
            try {
                fileOut.close();
            } catch (IOException ignore) {
            }
        }

        attributeCounter++;

        javaLogger.log(Level.FINEST, "Saving attributes.", fileName);
    }

    @Override
    public void saveSessionVariables(Map<String, Double> sessionVariables)
            throws IOException {
        synchronized (lock) {
            FileOutputStream fileOut = null;
            String fileName;
            fileName = Util.joinPaths(searchDir, "sessionVariables_"
                    + attributeCounter);

            try {
                File f = new File(fileName);
                fileOut = new FileOutputStream(f);
                for (Map.Entry<String, Double> entry : sessionVariables
                        .entrySet()) {
                    fileOut.write((Double.toString(entry.getValue()) + "\n")
                            .getBytes());
                }
            } finally {
                try {
                    fileOut.close();
                } catch (IOException ignore) {
                }
            }

            sessionCounter++;

            javaLogger.log(Level.FINEST, "Saving Session Variables.", fileName);
        }
    }

    @Override
    public void stoppedSearch(Throwable cause) {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "Search has stopped.", new String[] {
                    Integer.toString(totalObjects),
                    Integer.toString(processedObjects),
                    Integer.toString(droppedObjects) });
            shutdown(cause);
        }
    }

    @Override
    public void startedSearch() {
        synchronized (lock) {
            javaLogger
                    .log(Level.FINEST, "Search has started.", APP_SESSION_DIR);
        }
    }

    @Override
    public void updateStatistics(Map<String, ServerStatistics> result) {
        synchronized (lock) {
            totalObjects = 0;
            processedObjects = 0;
            droppedObjects = 0;
            for (ServerStatistics ss : result.values()) {
                totalObjects += ss.getTotalObjects();
                processedObjects += ss.getProcessedObjects();
                droppedObjects += ss.getDroppedObjects();
            }

            javaLogger.log(Level.FINEST, "Updating server statistics.",
                    new String[] { Integer.toString(totalObjects),
                            Integer.toString(processedObjects),
                            Integer.toString(droppedObjects) });
        }
    }

    @Override
    public void saveGetNewResult(Result result) {
        synchronized (lock) {
            String array[];

            if (Boolean
                    .parseBoolean(System
                            .getProperty("edu.cmu.cs.diamond.opendiamond.loggingframework.detailedresults"))) {
                array = new String[result.getKeys().size() * 2 + 1];
                int i = 1;
                for (String s : result.getKeys()) {
                    array[i] = s;
                    array[i + 1] = Base64.encodeBytes(result.getValue(s));
                    i += 2;
                }
                array[0] = result.getObjectIdentifier().getHostname();
            } else {
                array = new String[] {
                        result.getObjectIdentifier().getHostname(),
                        result.toString() };
            }

            javaLogger.log(Level.FINEST, "Got new result.", array);

        }
    }

    @Override
    public void logNoMoreResults() {
        synchronized (lock) {
            javaLogger.log(Level.FINEST, "NO_MORE_RESULTS");
        }
    }

    private void saveCookieMap(CookieMap cookieMap) throws IOException {
        if (cookieMap.getMegaCookie() != null) {
            FileOutputStream fileOut = null;
            String fileName = Util.joinPaths(searchDir, "cookieMap_"
                    + cookieMapCounter);
            try {
                File f = new File(fileName);
                fileOut = new FileOutputStream(f);
                fileOut.write(cookieMap.getMegaCookie().getBytes());
            } finally {
                try {
                    fileOut.close();
                } catch (IOException ignore) {
                }
            }

            cookieMapCounter++;
            javaLogger.log(Level.FINEST, "Saving the cookiemap/megacookie.",
                    fileName);
        } else {
            javaLogger.log(Level.FINEST, "Null megacookie.");
        }
    }

    private void saveApplicationDependencies(
            List<String> applicationDependencies) throws IOException {
        FileOutputStream fileOut = null;
        String fileName = Util.joinPaths(searchDir, "applicationDependencies_"
                + applicationDependenciesCounter);
        File f = new File(fileName);
        fileOut = new FileOutputStream(f);
        try {
            // Base64 encode string, add new line, write out bytes
            for (String s : applicationDependencies) {
                fileOut.write((Base64.encodeBytes(s.getBytes()) + "\n")
                        .getBytes());
            }
        } finally {
            try {
                fileOut.close();
            } catch (IOException ignore) {
            }
        }

        applicationDependenciesCounter++;

        javaLogger.log(Level.FINEST, "Saving application dependencies.",
                fileName);
    }

    @Override
    public void saveSearchFactory(SearchFactory searchFactory,
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
