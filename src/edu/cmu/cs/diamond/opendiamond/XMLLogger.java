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

	private final Logger SEARCH_LOGGER;
	private final String SEARCH_DIR;
	private int fspec_counter;
	private int filter_counter;
	private int attribute_counter;
    private int session_counter;
    private int totalObjects;
    private int processedObjects;
    private int droppedObjects;

	public XMLLogger() throws IOException {
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		SEARCH_LOGGER =
			Logger.getLogger(LoggingFramework.class.getPackage().getName());
		String date = sdf.format(currentDate);

		/* create searchDir */
		String temp = Util.joinPaths(APP_SESSION_DIR, date + "_" + searchCounter.getAndIncrement());

		if (!(new File(temp).mkdirs())) {
			throw new IOException();
		}

		SEARCH_DIR = temp;

		String logFileName = SEARCH_DIR + "/raw_log.log";
		try {
			FileHandler fh = new FileHandler(logFileName);
			SEARCH_LOGGER.addHandler(fh);
			XMLFormatter formatter = new XMLFormatter();
			fh.setFormatter(formatter);
			SEARCH_LOGGER.setUseParentHandlers(false);
			SEARCH_LOGGER.setLevel(Level.FINEST);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fspec_counter = 0;
		filter_counter = 0;
		session_counter = 0;
		totalObjects = 0;
		processedObjects = 0;
		droppedObjects = 0;
	}
	
	protected Logger getSearchLogger() {
		return SEARCH_LOGGER;
	}
	
	protected String saveFspec(String fSpec) {
		byte[] spec = fSpec.getBytes();
		FileOutputStream fileOut = null;
		String fileName = SEARCH_DIR + "/fspec_" + fspec_counter;
		try {
			fileOut = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fileOut.write(spec);
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fspec_counter++;
		return fileName;
	}

	protected String[] saveFilters(List<Filter> filters) {
		FileOutputStream fileOut1 = null, fileOut2 = null, fileOut3 = null;
		String fileName1, fileName2, fileName3;
		String[] returnList = new String[filters.size()*3];
		int counter = 0;
		for (Filter f : filters) {
			fileName1 = SEARCH_DIR + "/filter_" + filter_counter;
			fileName2 = SEARCH_DIR + "/encodedblob_" + filter_counter;
			fileName3 = SEARCH_DIR + "/encodedblobandsig_" + filter_counter;
			try {
				fileOut1 = new FileOutputStream(fileName1);
				fileOut2 = new FileOutputStream(fileName2);
				fileOut3 = new FileOutputStream(fileName3);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				fileOut1.write(f.getFilterCode().getBytes());
				fileOut1.close();
				fileOut2.write(f.getEncodedBlob());
				fileOut2.close();
				fileOut3.write(f.getEncodedBlobSig());
				fileOut3.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			returnList[counter] = fileName1;
			returnList[counter+1] = fileName2;
			returnList[counter+2] = fileName3;
			counter += 3;
			filter_counter++;
		}
		return returnList;
	}

	protected String saveAttributes(Set<String> desiredAttributes) {
		FileOutputStream fileOut = null;
		String fileName;
		fileName = SEARCH_DIR + "/attributes_" + attribute_counter;

		try {
			fileOut = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String s : desiredAttributes) {
			try {
				fileOut.write((s + "\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		attribute_counter++;
		return fileName;
	}

	protected String saveSessionVariables(Map<String, Double> map) {
		FileOutputStream fileOut = null;
		String fileName;
		fileName = SEARCH_DIR + "/sessionVariables_" + attribute_counter;

		try {
			fileOut = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String s : map.keySet()) {
			try {
				fileOut.write((Double.toString(map.get(s)) + "\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		session_counter++;
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

	protected void shutdown() {
		for (Handler h :SEARCH_LOGGER.getHandlers()) {
			SEARCH_LOGGER.removeHandler(h);
			h.close();
		}
	}

	public String[] saveGetNewResult(Map<String, byte[]> attrs) {
		String[] returnArray = null;
		if (Boolean.parseBoolean(System.getProperty("edu.cmu.cs.diamond.opendiamond.loggingframework.detailedresults"))) {
			returnArray = new String[attrs.size()*2];
			int i = 0;
			for (String s : attrs.keySet()) {
				returnArray[i] = s;
				returnArray[i+1] = Base64.encodeBytes(attrs.get(s));
				i += 2;
			}
		}
		return returnArray;
	}
}
