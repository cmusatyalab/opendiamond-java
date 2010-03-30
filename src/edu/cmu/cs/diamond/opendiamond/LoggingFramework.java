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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
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

	synchronized static public void setup() {
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("yyy.MM.dd-HH.mm.ss-a");
		Logger theLogger =
			Logger.getLogger(LoggingFramework.class.getPackage().getName());
		LoggingFramework.date = sdf.format(currentDate);
		String logFileName = "raw_log-" + date + ".log";
		files = new ArrayList<String>();
		files.add(logFileName);
		try {
			FileHandler fh = new FileHandler(logFileName);
			theLogger.addHandler(fh);
			fh.setFormatter(new XMLFormatter());
			theLogger.setUseParentHandlers(false);
			theLogger.setLevel(Level.FINEST);
			theLogger.finest("Logging Enabled.");
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

	synchronized static public void shutdown() {
		if (!enabled) return;
		enabled = false;
		ZipStream zipOut = new ZipStream(date + ".zip");
		byte[] buf;
		for (String fileName : files) {
			zipOut.storeFile(fileName);
		}
		zipOut.cleanUp();
	}

	synchronized public static void saveFilters(List<Filter> filters) {
		if (!enabled) return;
		FileOutputStream fileOut = null;
		String fileName;
		Logger theLogger =
			Logger.getLogger(LoggingFramework.class.getPackage().getName());
		for (Filter f : filters) {
			fileName = "filter_" + filter_counter + "_" + date;
			theLogger.finest("Saving Filter: " + fileName);
			try {
				fileOut = new FileOutputStream(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				fileOut.write(f.getFilterCode().getBytes());
				fileOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			files.add(fileName);
			filter_counter++;
		}
	}

	synchronized public static void saveAttributes(Set<String> desiredAttributes) {
		if (!enabled) return;
		FileOutputStream fileOut = null;
		String fileName;
		Logger theLogger =
			Logger.getLogger(LoggingFramework.class.getPackage().getName());
		fileName = "attributes_" + attribute_counter + "_" + date;
		theLogger.finest("Saving Attributes: " + fileName);
		
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
	}
}
