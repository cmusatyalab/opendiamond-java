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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

public class LoggingFramework {
	static private boolean enabled = false;
	static private ArrayList<String> files;
	static private String date;
	static private int fspec_counter;
	static private int filter_counter;
	static private int attribute_counter;
    static private int totalObjects;
    static private int processedObjects;
    static private int droppedObjects;

	synchronized static public void setup() {
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		Logger theLogger =
			Logger.getLogger(LoggingFramework.class.getPackage().getName());
		LoggingFramework.date = sdf.format(currentDate);
		String logFileName = "raw_log-" + date + ".log";
		files = new ArrayList<String>();
		files.add(logFileName);
		try {
			FileHandler fh = new FileHandler(logFileName);
			theLogger.addHandler(fh);
			XMLFormatter formatter = new XMLFormatter();
			fh.setFormatter(new XMLFormatter());
			theLogger.setUseParentHandlers(false);
			theLogger.setLevel(Level.FINEST);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		enabled = true;
		fspec_counter = 0;
		filter_counter = 0;
		totalObjects = 0;
		processedObjects = 0;
		droppedObjects = 0;
	}

	static public boolean startup() {
		return true;
	}
	
	synchronized static public Logger getLogger() {
		if (!enabled) {
			LoggingFramework.setup();
		}
		return Logger.getLogger(LoggingFramework.class.getPackage().getName());
	}

	synchronized static public String saveFspec(StringBuilder sbSpec) {
		if (!enabled) return null;
		byte[] spec = sbSpec.toString().getBytes();
		FileOutputStream fileOut = null;
		String fileName = "fspec_" + fspec_counter + "_" + date;
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
		
		files.add(fileName);
		
		fspec_counter++;
		
		return fileName;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	synchronized static public boolean shutdown() {
		if (!enabled) return false;
		Logger logger = getLogger();
		enabled = false;
		for (Handler h : logger.getHandlers()) {
			logger.removeHandler(h);
			h.close();
		}
		ZipStream zipOut = new ZipStream(date + ".zip",'w');
		byte[] buf;
		for (String fileName : files) {
			zipOut.storeFile(fileName);
		}
		zipOut.cleanUp();
		return true;
	}

	synchronized public static String[] saveFilters(List<Filter> filters) {
		if (!enabled) return null;
		FileOutputStream fileOut1 = null, fileOut2 = null, fileOut3 = null;
		String fileName1, fileName2, fileName3;
		String[] returnList = new String[filters.size()*3];
		int counter = 0;
		for (Filter f : filters) {
			fileName1 = "filter_" + filter_counter + "_" + date;
			fileName2 = "encodedblob_" + filter_counter + "_" + date;
			fileName3 = "encodedblobandsig_" + filter_counter + "_" + date;
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
			files.add(fileName1);
			files.add(fileName2);
			files.add(fileName3);
			returnList[counter] = fileName1;
			returnList[counter+1] = fileName2;
			returnList[counter+2] = fileName3;
			counter += 3;
			filter_counter++;
		}
		return returnList;
	}

	synchronized public static String saveAttributes(Set<String> desiredAttributes) {
		if (!enabled) return null;
		FileOutputStream fileOut = null;
		String fileName;
		fileName = "attributes_" + attribute_counter + "_" + date;
		
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
		
		files.add(fileName);
		attribute_counter++;
		return fileName;
	}

	synchronized public static boolean startSearch() {
		if (!enabled) return false;
		LoggingFramework.totalObjects = 0;
		LoggingFramework.processedObjects = 0;
		LoggingFramework.droppedObjects = 0;
		return true;
	}

	synchronized public static String[] stopSearch() {
		if (!enabled) return null;
		Logger theLogger =
			Logger.getLogger(LoggingFramework.class.getPackage().getName());
		return new String[] {Integer.toString(LoggingFramework.totalObjects), Integer.toString(LoggingFramework.processedObjects), Integer.toString(LoggingFramework.droppedObjects)};
	}

	synchronized public static ServerStatistics getCurrentTotalStatistics() {
		return new ServerStatistics(LoggingFramework.totalObjects, LoggingFramework.processedObjects, LoggingFramework.droppedObjects);
	}

	synchronized public static void updateStatistics(Map<String, ServerStatistics> result) {
		if (!enabled) return;
		LoggingFramework.totalObjects = 0;
		LoggingFramework.processedObjects = 0;
		LoggingFramework.droppedObjects = 0;
		for (ServerStatistics ss : result.values()) {
			LoggingFramework.totalObjects += ss.getTotalObjects();
			LoggingFramework.processedObjects += ss.getProcessedObjects();
			LoggingFramework.droppedObjects += ss.getDroppedObjects();
		}
	}
}
