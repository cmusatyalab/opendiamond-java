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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

public class XMLLogger {

	private static final String APP_SESSION_DIR;
	private static AtomicInteger searchCounter = new AtomicInteger(0);

	// Initialize the global APP_SESSION_DIR -- only executes once.
	static {
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		String date = sdf.format(currentDate);
		String diamondLoggingDir = System.getProperty("edu.cmu.cs.diamond.opendiamond.loggingframework.directory",  Util.joinPaths(System.getProperty("user.home"), "/.diamond/logs/"));
		String temp = Util.joinPaths(diamondLoggingDir, date + "_" + UUID.randomUUID().toString() + "/");
		while (!(new File(temp).mkdirs())) {
			temp = Util.joinPaths(diamondLoggingDir, date + "_" + UUID.randomUUID().toString() + "/");
		}
		APP_SESSION_DIR = temp;
	}

	private final Logger searchLogger;
	private final String searchDir;
	private int fspecCounter;
	private int filterCounter;
	private int attributeCounter;
    private int sessionCounter;
    private int totalObjects;
    private int processedObjects;
    private int droppedObjects;

	public XMLLogger() throws IOException {
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		searchLogger =
			Logger.getLogger(LoggingFramework.class.getPackage().getName());
		String date = sdf.format(currentDate);

		/* create searchDir */
		String temp = Util.joinPaths(APP_SESSION_DIR, date + "_" + searchCounter.getAndIncrement());

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
	
	protected Logger getSearchLogger() {
		return searchLogger;
	}
	
	protected String saveFspec(String fSpec) {
		byte[] spec = fSpec.getBytes();
		FileOutputStream fileOut = null;
		String fileName =  Util.joinPaths(searchDir, "fspec_" + fspecCounter);
		try {
			File f = new File(fileName);
			fileOut = new FileOutputStream(f);
			try {
				fileOut.write(spec);
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

		fspecCounter++;
		return fileName;
	}

	protected String[] saveFilters(List<Filter> filters) {
		FileOutputStream fileOut1 = null, fileOut2 = null, fileOut3 = null;
		String fileName1, fileName2, fileName3;
		String[] returnList = new String[filters.size()*3];
		int counter = 0;
		for (Filter f : filters) {
			fileName1 =  Util.joinPaths(searchDir, "filter_" + filterCounter);
			fileName2 =  Util.joinPaths(searchDir, "encodedblob_" + filterCounter);
			fileName3 =  Util.joinPaths(searchDir, "encodedblobandsig_" + filterCounter);
			try {
				File f1 = new File(fileName1);
				File f2 = new File(fileName2);
				File f3 = new File(fileName3);

				fileOut1 = new FileOutputStream(f1);
				fileOut2 = new FileOutputStream(f2);
				fileOut3 = new FileOutputStream(f3);
				try {
					fileOut1.write(f.getFilterCode().getBytes());
					fileOut2.write(f.getEncodedBlob());
					fileOut3.write(f.getEncodedBlobSig());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						fileOut1.close();
						fileOut2.close();
						fileOut3.close();
					} catch (IOException ignore) {
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			returnList[counter] = fileName1;
			returnList[counter+1] = fileName2;
			returnList[counter+2] = fileName3;
			counter += 3;
			filterCounter++;
		}
		return returnList;
	}

	protected String saveAttributes(Set<String> desiredAttributes) {
		FileOutputStream fileOut = null;
		String fileName;
		fileName =  Util.joinPaths(searchDir, "attributes_" + attributeCounter);

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

	protected String saveSessionVariables(Map<String, Double> map) {
		FileOutputStream fileOut = null;
		String fileName;
		fileName =  Util.joinPaths(searchDir, "sessionVariables_" + attributeCounter);

		try {
			File f = new File(fileName);
			fileOut = new FileOutputStream(f);
			for (Map.Entry<String, Double> entry : map.entrySet()) {
				try {
					fileOut.write((Double.toString(entry.getValue()) + "\n").getBytes());
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

	protected String[] stopSearch() {
		return new String[] {Integer.toString(totalObjects), Integer.toString(processedObjects), Integer.toString(droppedObjects)};
	}

	protected String startSearch() {
		return APP_SESSION_DIR;
	}
	
	protected ServerStatistics getCurrentTotalStatistics() {
		return new ServerStatistics(totalObjects, processedObjects, droppedObjects);
	}

	protected void updateStatistics(Map<String, ServerStatistics> result) {
		totalObjects = 0;
		processedObjects = 0;
		droppedObjects = 0;
		for (ServerStatistics ss : result.values()) {
			totalObjects += ss.getTotalObjects();
			processedObjects += ss.getProcessedObjects();
			droppedObjects += ss.getDroppedObjects();
		}
	}

	protected void shutdown(Throwable cause) {
		if (cause != null) searchLogger.log(Level.FINEST, "Logging throwable cause of failure.", Util.getStackTrace(cause));
		for (Handler h :searchLogger.getHandlers()) {
			searchLogger.removeHandler(h);
			h.close();
		}
	}

	public String[] saveGetNewResult(Result result) {
		String[] returnArray = null;
		if (Boolean.parseBoolean(System.getProperty("edu.cmu.cs.diamond.opendiamond.loggingframework.detailedresults"))) {
			returnArray = new String[result.getKeys().size()*2+1];
			int i = 1;
			for (String s : result.getKeys()) {
				returnArray[i] = s;
				returnArray[i+1] = Base64.encodeBytes(result.getValue(s));
				i += 2;
			}
			returnArray[0] = result.getObjectIdentifier().getHostname();
			return returnArray;
		}
		return new String[] {result.getObjectIdentifier().getHostname(), result.toString()};
	}
}
