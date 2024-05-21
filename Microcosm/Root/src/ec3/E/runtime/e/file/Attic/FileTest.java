// RtDirectory and RtFile test code
// Charles Kendrick, Electric Communities, Oct 1996

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.net.InetAddress;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.InternalError;
import java.io.RandomAccessFile;
import java.io.File;

import ec.e.file.*;
import ec.e.run.*;
import ec.e.db.*;
import ec.e.stream.*;
import ec.eload.*;
import ec.e.cap.ECapability;
import ec.e.cap.ERestrictionException;
import ec.e.cap.ERestrictedException;

public class FileTest {

	public static void main(String args[]) {
		UseFile test = new UseFile();
		test.go();
	}

}

public class UseFile {

	EWritableDirectory dirWriter, dirWriter2;
	EReadableDirectory dirReader, dirReader2;
	EWritableFile fileWriter;
	EReadableFile fileReader;
	File jfile;
	File jfile2;

	public void go() {
		
		try {
			dirWriter = EWritableDirectory.createRootCapability();
			dirWriter2 = (EWritableDirectory)dirWriter.getSubdirectory("/home/charles/examples");
			dirWriter2.deleteFile("tester");	
		} catch (Exception e) {
			e.printStackTrace();
		};
/*
		// Demonstrates moving a file.  Note you must have the directory structure and file
		// listed below, as well as write permission at the OS level. 
		try {
			dirWriter = EWritableDirectory.createRootCapability();
			dirWriter2 = (EWritableDirectory)dirWriter.getSubdirectory("/home/charles/examples");
			dirWriter2.moveFile("tester", "tester", dirWriter);	
		} catch (Exception e) {
			e.printStackTrace();
		};
*/
/*
		// demonstrates that getting a file for reading from an EWritableDirectory returns
		// an EReadableFile (not Writable).  This code throws a class cast exception.
		try {
			dirWriter = EWritableDirectory.createRootCapability();
			dirWriter2 = (EWritableDirectory)dirWriter.getSubdirectory("/home/charles/examples");
			fileReader = (EWritableFile)dirWriter.openFile("tester", "r"); 	
		} catch (Exception e) {
			e.printStackTrace();
		};
*/
/*
		// demonstrates directory traversal
		try {
			dirWriter = EWritableDirectory.createRootCapability();
			dirWriter2 = (EWritableDirectory)dirWriter.getSubdirectory("/home/charles");
			dirWriter2 = (EWritableDirectory)dirWriter2.getSubdirectory("/examples");
			// basically returns a clone
			dirWriter2 = (EWritableDirectory)dirWriter2.getSubdirectory("");
		} catch (Exception e) {
			e.printStackTrace();
		};
*/
/*
		// demonstrates that a restriction on a capability that is common between
		// the RtDirectory and RtDirectoryWriter classes (the ability to list
		// the directory) is preserved when an RtDirectoryWriter is restricted
		// to read-only and returns an RtDirectory.
		dirWriter = (EWritableDirectory)dirWriter.restrictListDirectory();
		dirReader = dirWriter.restrictToReadOnly();	
		try {
			dirReader.listDirectory();
		} catch (Exception e) {
			e.printStackTrace();
		};
*/
/*
		// demonstrates that the class returned from restrictToReadOnly is in
		// fact an EReadableDirectory and will cause a class cast exception
		// if cast to an EWritableDirectory.
		dirWriter = (EWritableDirectory)dirWriter.restrictListDirectory();
		dirWriter = (EWritableDirectory)dirWriter.restrictToReadOnly();	
*/

	}

}
