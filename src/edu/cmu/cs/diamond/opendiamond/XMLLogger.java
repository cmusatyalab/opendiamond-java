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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

class XMLLogger {

    private static final String APP_SESSION_DIR;

    private static AtomicInteger searchCounter = new AtomicInteger(0);

    // Initialize the global APP_SESSION_DIR -- only executes once.
    static {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String date = sdf.format(currentDate);
        String diamondLoggingDir = System.getProperty(
                "edu.cmu.cs.diamond.opendiamond.loggingframework.directory",
                Util.joinPaths(System.getProperty("user.home"),
                        "/opendiamond-logs/"));
        String temp = Util.joinPaths(diamondLoggingDir, date + "_"
                + UUID.randomUUID().toString() + "/");
        while (!(new File(temp).mkdirs())) {
            temp = Util.joinPaths(diamondLoggingDir, date + "_"
                    + UUID.randomUUID().toString() + "/");
        }
        APP_SESSION_DIR = temp;
    }

    private final Logger searchLogger;

    private final String searchDir;

    private int cookieMapCounter;

    private int filterCounter;

    private int attributeCounter;

    private int applicationDependenciesCounter;

    private int sessionCounter;

    private int totalObjects;

    private int processedObjects;

    private int droppedObjects;

    XMLLogger() throws IOException {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        searchLogger = Logger.getLogger(LoggingFramework.class.getPackage()
                .getName());
        String date = sdf.format(currentDate);

        /* create searchDir */
        String temp = Util.joinPaths(APP_SESSION_DIR, date + "_"
                + searchCounter.getAndIncrement());

        if (!(new File(temp).mkdirs())) {
            throw new IOException();
        }

        searchDir = temp;

        String logFileName = Util.joinPaths(searchDir, "raw_log.log");
        try {
            FileHandler fh = new FileHandler(logFileName);
            searchLogger.addHandler(fh);
            XMLFormatter formatter = new XMLFormatter();
            fh.setFormatter(formatter);
            searchLogger.setUseParentHandlers(false);
            searchLogger.setLevel(Level.FINEST);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    Logger getSearchLogger() {
        return searchLogger;
    }

    String[] saveFilter(Filter filter) {
        FileOutputStream fileOut1, fileOut2, fileOut3, fileOut4, fileOut5;
        String fileName1, fileName2, fileName3, fileName4, fileName5;
        fileName1 = Util.joinPaths(searchDir, "filter_" + filterCounter);
        fileName2 = Util.joinPaths(searchDir, "filtercode_" + filterCounter);
        fileName3 = Util.joinPaths(searchDir, "dependencies_" + filterCounter);
        fileName4 = Util.joinPaths(searchDir, "arguments_" + filterCounter);
        fileName5 = Util.joinPaths(searchDir, "blob_" + filterCounter);
        try {
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
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    fileOut1.close();
                    fileOut2.close();
                    fileOut3.close();
                    fileOut4.close();
                    fileOut5.close();
                } catch (IOException ignore) {
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        filterCounter++;
        return new String[] { fileName1, fileName2, fileName3, fileName4,
                fileName5 };
    }

    String saveAttributes(Set<String> desiredAttributes) {
        FileOutputStream fileOut = null;
        String fileName;
        fileName = Util.joinPaths(searchDir, "attributes_" + attributeCounter);

        try {
            File f = new File(fileName);
            fileOut = new FileOutputStream(f);
            for (String s : desiredAttributes) {
                try {
                    fileOut.write((s + "\n").getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                fileOut.close();
            } catch (IOException ignore) {
            }
        }

        attributeCounter++;
        return fileName;
    }

    String saveSessionVariables(Map<String, Double> map) {
        FileOutputStream fileOut = null;
        String fileName;
        fileName = Util.joinPaths(searchDir, "sessionVariables_"
                + attributeCounter);

        try {
            File f = new File(fileName);
            fileOut = new FileOutputStream(f);
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                try {
                    fileOut.write((Double.toString(entry.getValue()) + "\n")
                            .getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                fileOut.close();
            } catch (IOException ignore) {
            }
        }

        sessionCounter++;
        return fileName;
    }

    String[] stopSearch() {
        return new String[] { Integer.toString(totalObjects),
                Integer.toString(processedObjects),
                Integer.toString(droppedObjects) };
    }

    String startSearch() {
        return APP_SESSION_DIR;
    }

    ServerStatistics getCurrentTotalStatistics() {
        return new ServerStatistics(totalObjects, processedObjects,
                droppedObjects);
    }

    String[] updateStatistics(Map<String, ServerStatistics> result) {
        totalObjects = 0;
        processedObjects = 0;
        droppedObjects = 0;
        for (ServerStatistics ss : result.values()) {
            totalObjects += ss.getTotalObjects();
            processedObjects += ss.getProcessedObjects();
            droppedObjects += ss.getDroppedObjects();
        }
        return new String[] { Integer.toString(totalObjects),
                Integer.toString(processedObjects),
                Integer.toString(droppedObjects) };
    }

    void shutdown(Throwable cause) {
        if (cause != null)
            searchLogger.log(Level.FINEST,
                    "Logging throwable cause of failure.", Util
                            .getStackTrace(cause));
        for (Handler h : searchLogger.getHandlers()) {
            searchLogger.removeHandler(h);
            h.close();
        }
    }

    String[] saveGetNewResult(Result result) {
        if (Boolean
                .parseBoolean(System
                        .getProperty("edu.cmu.cs.diamond.opendiamond.loggingframework.detailedresults"))) {
            String[] returnArray = new String[result.getKeys().size() * 2 + 1];
            int i = 1;
            for (String s : result.getKeys()) {
                returnArray[i] = s;
                returnArray[i + 1] = Base64.encodeBytes(result.getValue(s));
                i += 2;
            }
            returnArray[0] = result.getObjectIdentifier().getHostname();
            return returnArray;
        }
        return new String[] { result.getObjectIdentifier().getHostname(),
                result.toString() };
    }

    String saveCookieMap(CookieMap cookieMap) {
        FileOutputStream fileOut = null;
        String fileName = Util.joinPaths(searchDir, "cookieMap_"
                + cookieMapCounter);
        try {
            File f = new File(fileName);
            fileOut = new FileOutputStream(f);
            try {
                fileOut.write(cookieMap.getMegaCookie().getBytes());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    fileOut.close();
                } catch (IOException ignore) {
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        cookieMapCounter++;
        return fileName;
    }

    String saveApplicationDependencies(List<String> applicationDependencies) {
        FileOutputStream fileOut = null;
        String fileName = Util.joinPaths(searchDir, "applicationDependencies_"
                + applicationDependenciesCounter);
        try {
            File f = new File(fileName);
            fileOut = new FileOutputStream(f);
            try {
                // Base64 encode string, add new line, write out bytes
                for (String s : applicationDependencies) {
                    fileOut.write((Base64.encodeBytes(s.getBytes()) + "\n")
                            .getBytes());
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    fileOut.close();
                } catch (IOException ignore) {
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        applicationDependenciesCounter++;
        return fileName;
    }
}
