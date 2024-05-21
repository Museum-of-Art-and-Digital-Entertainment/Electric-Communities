// This class encapsulates "stat" information,
// answering most of the boolean queries that
// the java file class does

package ec.e.file;

import java.io.*;
import java.lang.*;

public class RtDirectoryEntry { 
  
	File Entry;
   
//
// Constructors
//

    RtDirectoryEntry(File entry) { // note constructor private to package
   		Entry = entry; 
	}
  
//
// Public
//

	// Always returns true, you can't get an RtDirectoryEntry otherwise
    public boolean exists() {
   		return true; 
	}

	public boolean isDirectory() {
		return Entry.isDirectory();
	}

	public boolean isFile() {
		return Entry.isFile();
	}

	public long lastModified() {
		return Entry.lastModified();
	}

	public long length() {
		return Entry.length();
	}

	public String getName() {
		return Entry.getName();
	}

	// capability answers
	// canWrite, canRead

	// what did these mean in the java context?
	// getAbsolutePath, getPath 
	
	// probably don't want to do: getParent

}
