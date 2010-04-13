package edu.cmu.cs.diamond.opendiamond;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

public class ZipStream {

	private ZipOutputStream zout;
	private ZipFile zin;
	private static final int COMPRESSION_LEVEL = 9;

	public ZipStream(String name, char readWrite) {
		if (readWrite == 'w') {
			FileOutputStream fout = null;
			try {
				fout = new FileOutputStream(name);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			zout = new ZipOutputStream(fout);
			zout.setLevel(ZipStream.COMPRESSION_LEVEL);
		} else {
			zin = null;
			try {
				zin = new ZipFile(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void storeFile(String name) {
		byte[] data = null;
		try {
			data = Util.readFully(new FileInputStream(name));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			zout.putNextEntry(new ZipEntry(name));
			zout.write(data);
			zout.closeEntry();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File file = new File(name);
		file.delete();
	}

	public byte[] readFile(String name) {
		try {
			return Util.readFully(zin.getInputStream(zin.getEntry(name)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void cleanUp() {
		try {
			if (zout != null) zout.close();
			if (zin != null) zin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
