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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

public class LoggingFramework {
	static private boolean setup;

	static public void setup() {
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("yyy.MM.dd-HH.mm.ss-a");
		Logger theLogger =
			Logger.getLogger(LoggingFramework.class.getPackage().getName());
		try {
			FileHandler fh = new FileHandler("raw_log-" + sdf.format(currentDate) + "-.log");
			theLogger.addHandler(fh);
			fh.setFormatter(new XMLFormatter());
			theLogger.setUseParentHandlers(false);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setup = true;
	}

	synchronized static public Logger getLogger() {
		if (!setup) {
			LoggingFramework.setup();
		}
		return Logger.getLogger(LoggingFramework.class.getPackage().getName());
	}
}
