package ec.e.file;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.net.InetAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.InternalError;

import ec.e.dgc.*;
import ec.e.run.*;
import ec.e.db.*;
import ec.e.stream.*;
import ec.eload.*;
import ec.e.cap.ECapability;
import ec.e.cap.ERestrictionException;
import ec.e.cap.ERestrictedException;

final public class RtDirectory implements ECapability, Cloneable {

	private long expiration; // milliseconds since epoch
	private String rootPath; // path before virtual root directory
	private String path; // path from virtual root directory
	private boolean canList; // can list files in current directory 
	private boolean canGetEntry; // can get stat info on file
	private boolean canChdir; // can traverse directories downward
	private boolean canCreateDir; // can create new directories in current directory
	private boolean canRenameDir; // can rename directories in current directory
	private boolean canDeleteDir; // can delete directories in current directory
	private boolean canRename; // can rename files in current directory
	private boolean canDelete; // can delete files in current directory
	private boolean canCreate; // can create new files in current directory
	private boolean canWrite; // can write files in current directory
	private boolean canAppend; // can append to files in current directory
	private boolean canRead; // can read files in current directory
	private boolean canTemp; // can have temp space in current directory
	private long sizeLimit; // maximum size of temp files
	private long maxduration; // milliseconds
	private File Directory;

	static private boolean created = false;
	static public RtDirectory createRootCapability() {
		if (created) return null; // there can be only ONE!
		created = true;
		return new RtDirectory();
	}

//
// Constructors
//

	// create unrestricted RtDirectory
	private RtDirectory() {
		expiration = Long.MAX_VALUE ;
		rootPath = "/" ; 
		path = "" ; 
		canList = true;
		canGetEntry = true;
		canChdir = true;
		canCreateDir = true;
		canRenameDir = true;
		canDeleteDir = true;
		canWrite = true; 
		canAppend = true;
		canRead = true;
		canCreate = true;
		canRename = true;
		canTemp = true;
		sizeLimit = Long.MAX_VALUE ;
		maxduration = Long.MAX_VALUE ;

		//**//
		Directory = new File(rootPath);

	}

//
// ECapability methods
//

	public ECapability copy() {
		try {
			return (ECapability)this.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	public void restrict(String kind, String restriction) throws ERestrictionException {
	
		if (kind == "expiration") {
			long newexp = RtUtil.parseExpirationDate(restriction, false);
			if (newexp < expiration) {
				expiration = newexp ;
			}
		}
		else if (kind == "chroot") {
			if (path.startsWith(restriction)) {
				path = path.substring(restriction.length());
			}
			rootPath = RtUtil.joinPath(rootPath, "/", restriction) ;
			Directory = new File(rootPath) ;
		}
		else if (kind == "path") {
			if (restriction.startsWith(path)) {
				path = restriction ;
				Directory = new File(RtUtil.joinPath(rootPath, "/", restriction)) ;
			}
		}
		else if (kind == "access") {
			if 		(restriction == "NoList")		canList = false ;
			else if (restriction == "NoGetEntry")	canGetEntry= false ; // EXPAND
			else if (restriction == "NoChdir")		canChdir = false ; // EXPAND
			else if (restriction == "NoCreateDir")	canCreateDir = false ; // EXPAND
			else if (restriction == "NoRenameDir")	canRenameDir = false ; // EXPAND
			else if (restriction == "NoDeleteDir")	canDeleteDir = false ; // EXPAND
			else if (restriction == "NoWrite")		canWrite = false ;
			else if (restriction == "NoAppend")		canAppend = false ;
			else if (restriction == "NoRead") 		canRead = false ;
			else if (restriction == "NoCreate")     canCreate = false ;
			else if (restriction == "NoRename")     canRename = false ;
			else if (restriction == "NoTemp")		canTemp = false ;
			else throw new ERestrictionException(kind + ":" + restriction);
		}
		else if (kind == "size") {
			long newsize = Long.parseLong(restriction);
			if (newsize >= 0 && newsize < sizeLimit) {
				sizeLimit = newsize ;
			}  
		}
		else if (kind == "maxduration") {
			long newduration = RtUtil.parseExpirationDate(restriction, true);
			if (newduration >= 0 && newduration < maxduration) {
				maxduration = newduration ;
			}
		}
		else {
			throw new ERestrictionException(kind + ":" + restriction);
		}
	}

//
// Public Methods
//

	// traverse directory structure, accomplished by cloning and restricting
	public RtDirectory getSubdirectory(String newpath) throws ERestrictedException {
		if (canChdir) {
			// check newpath
			if (newpath.startsWith(path) && validPath(newpath)) {
				// create absolute path
				String abspath = RtUtil.joinPath(rootPath, "/", newpath);
				// check existance
				File targetdir = new File(abspath);
				if (targetdir.exists()) {
					RtDirectory newdir = (RtDirectory)copy();
					try {
						newdir.restrict("path", newpath); // NOT abspath, which is absolute
					} catch (ERestrictionException e) {
						throw new InternalError("restricting directory: " + e);
					}
					return newdir;
				} else throw new ERestrictedException("RtDirectory directory does not exist: " + newpath);
			} else throw new ERestrictedException("RtDirectory invalid path: " + newpath);
		} else {
			throw new ERestrictedException("RtDirectory CanChdir") ;
		}
	}

	// list directory  
	public String[] listDirectory() throws ERestrictedException {
		if (canList) {
			return Directory.list() ;	
		} else {
			throw new ERestrictedException("RtDirectory CanList") ;
		}
	}

	// get info on a file or subdirectory
	public RtDirectoryEntry getDirectoryEntry(String filename) throws ERestrictedException {
		if (canGetEntry) {
			File entry = new File(filename);
			if (entry.exists()) { // RtDirectoryEntry's must be for files that exist
				return new RtDirectoryEntry(entry);		
			} else throw new ERestrictedException("RtDirectory file does not exist: " + filename);
		} else {
			throw new ERestrictedException("RtDirectory CanGetEntry") ;
		}
	} 

	// get info on all files and subdirectories
//	public Vector getAllDirectoryEntries() {
//		String[] Files = list();
//	}

	// get an enumerator for info for all files?

	// create a new subdirectory
	public boolean makeSubdirectory(String dirname) throws ERestrictedException {
		if (canCreateDir) {
			if (validLocalPath(dirname)) {
				// create a File out of the absolute directory path and the
				// name of the directory to be created
				File newdir = new File(absPathPlus(dirname));
				if (!newdir.exists()) { // prevents clobbering
					return newdir.mkdir();
				} else return false;
			} else return false; // path outside of directory
		} else {
			throw new ERestrictedException("RtDirectory CanCreateDir") ;
		}
	}

	// delete a subdirectory (throws exception if directory not empty)
	public boolean removeSubdirectory(String dirname) throws ERestrictedException {
		if (canDeleteDir) {
			if (validLocalPath(dirname)) {
				File dir = new File(absPathPlus(dirname));
				if (dir.isDirectory()) {
					return dir.delete();
				} else return false;
			} else return false;
		} else {
			throw new ERestrictedException("RtDirectory CanDeleteDir") ;
		}
	}

	// rename a subdirectory
	public boolean renameSubdirectory(String dirname, String newname) throws ERestrictedException {
		if (canRenameDir) {
			if (validLocalPath(dirname)) {
				File dir = new File(absPathPlus(dirname));
				File newdir = new File(absPathPlus(newname));
				if (dir.isDirectory() && (!newdir.exists())) { // prevents clobbering
					return dir.renameTo(new File(newname));
				} else return false;
			} else return false;
		} else {
			throw new ERestrictedException("RtDirectory CanRenameDir") ;
		}
	}
	
	// rename a file
	public boolean renameFile(String filename, String newname) throws ERestrictedException {
		if (canRename) {
			if (validLocalPath(filename)) {
				File file = new File(absPathPlus(filename));
				File newfile = new File(absPathPlus(newname));
				if (file.isDirectory() && (!newfile.exists())) { // prevents clobbering
					return file.renameTo(new File(newname));
				} else return false;
			} else return false;
		} else {
			throw new ERestrictedException("RtDirectory CanRename") ;
		}
	}
	
	// delete a file
	public boolean deleteFile(String filename) throws ERestrictedException {
		if (canDelete) {
			if (validLocalPath(filename)) {
				File file = new File(absPathPlus(filename));
				if (!file.isDirectory()) {
					return file.delete();
				} else return false;
			} else return false;
		} else {
			throw new ERestrictedException("RtDirectory CanDelete") ;
		}
	}

	// open a file in this directory, with some allowed privileges
	public RtFile openFile(String filename, String mode) throws ERestrictedException, IOException { // only in this directory

		// check mode vs capabilities
		boolean write = (mode.indexOf('w') != -1);
		boolean read = (mode.indexOf('r') != -1); 
		boolean append = (mode.indexOf('a') != -1); 
		if (write && !canWrite) {
			throw new ERestrictedException("RtDirectory CanWrite") ;
		} else if (read && !canRead) {
			throw new ERestrictedException("RtDirectory CanRead") ;
		} else if (append && !canAppend) {
			throw new ERestrictedException("RtDirectory CanAppend") ;
		}
		if (!write && !read && !append) {
			// tried to open a file with no capabilities
			throw new ERestrictedException("RtDirectory OpenOnly"); // XXX text for exception?
		}
		if (validLocalPath(filename)) {
			String fullPath = absPathPlus(filename);
			File file = new File(fullPath);
			if (!file.exists() && !canCreate) { // if the file doesn't exist, is creation allowed?
				throw new ERestrictedException("RtDirectory CanCreate") ;
			} else {
				return new RtFile(fullPath, read, write, append, sizeLimit);
			}
		} else throw new ERestrictedException("RtDirectory file not in this directory: " + filename);
	}

	public RtFile openTemp() throws ERestrictedException {
		if (canTemp) {
			return (RtFile)null ;
		} else {
			throw new ERestrictedException("RtDirectory CanTemp") ;
		}
	}

//
// Private Utils
//

	private String absPath() {
		return RtUtil.joinPath(rootPath, "/", path);
	}

	private String absPathPlus(String morePath) {
		return RtUtil.joinPath(absPath(), "/", morePath);
	}

// These utes may be part of environment

	// check if path is valid in this directory or downstream
	private boolean validPath(String aPath) {
	
		int suspectThere = aPath.indexOf("..");
		if (suspectThere == -1)
			return true;
		else if (suspectThere == 0)
			return false;
		else {
			String s = File.pathSeparator;
			if (aPath.indexOf(".." + s) != -1)
				return false;
			else if (aPath.indexOf(s + "..") != -1)
				return false;
			}
		return false;
	}
	
	// check if path is valid in the current directory
	private boolean validLocalPath(String aPath) {
		
		String s = File.pathSeparator;
		if (aPath.indexOf(s) != -1)
			return false;
		else if (aPath == "..")
			return false;
		return true;
	
	}
	 
}

//
// Exceptions
//
	
public class RtDirectoryException extends Exception {
	public RtDirectoryException(String message) {
		super(message);
	}
}
