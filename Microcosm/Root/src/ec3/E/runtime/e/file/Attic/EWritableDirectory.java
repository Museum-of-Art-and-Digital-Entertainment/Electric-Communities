package ec.e.file;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.net.InetAddress;
import java.io.File;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.InternalError;

import ec.e.run.*;
import ec.e.db.*;
import ec.e.stream.*;
import ec.eload.*;
import ec.e.cap.ECapability;
import ec.e.cap.ERestrictionException;
import ec.e.cap.ERestrictedException;

public class EWritableDirectory 
extends EReadableDirectory implements ECapability, Cloneable {
    
    private boolean canCreateDir; // can create new directories in current directory
    private boolean canRenameDir; // can rename directories in current directory
    private boolean canDeleteDir; // can delete directories in current directory
    private boolean canDelete; // can delete files in current directory
    private boolean canAppend; // can append to files in current directory
    private long sizeLimit; // maximum size of temp files
    private long maxDuration; // milliseconds
    
    static private boolean created = false;
    static public EWritableDirectory createRootCapability() {
        if (created) return null; // there can be only ONE!
        created = true;
        return new EWritableDirectory();
    }
    
    //
    // Constructors
    //
    
    // package private constructor to create unrestricted EWritableDirectory
    EWritableDirectory() {
        super();
        canCreateDir = true;
        canRenameDir = true;
        canDeleteDir = true;
        canDelete = true;
        canReadWrite = true; 
        canAppend = true;
        sizeLimit = Long.MAX_VALUE;
        maxDuration = Long.MAX_VALUE;
    }
    
    //
    // Restriction methods
    //
    
    // these restriction functions map to the names of the functions they
    // prevent the user of the restricted directory from calling
    public EWritableDirectory restrictMakeSubdirectory()
    {
        EWritableDirectory restrictedDir = (EWritableDirectory) copy();
        restrictedDir.canCreateDir = false;
        return restrictedDir;
    }
    
    public EWritableDirectory restrictRenameSubdirectory()
    {
        EWritableDirectory restrictedDir = (EWritableDirectory) copy();
        restrictedDir.canRenameDir = false;
        return restrictedDir;
    }
    
    public EWritableDirectory restrictDeleteSubdirectory()
    {
        EWritableDirectory restrictedDir = (EWritableDirectory) copy();
        restrictedDir.canDeleteDir = false;
        return restrictedDir;
    }
    
    public EWritableDirectory restrictDeleteFile()
    {
        EWritableDirectory restrictedDir = (EWritableDirectory) copy();
        restrictedDir.canDelete = false;
        return restrictedDir;
    }
    
    // removes the capability to open files in append mode
    public EWritableDirectory restrictFileAppend()
    {
        EWritableDirectory restrictedDir = (EWritableDirectory) copy();
        restrictedDir.canAppend = false;
        return restrictedDir;
    }
    
    // returns an instance of the EReadableDirectory class, with the settings of all 
    // capabilities common to both EReadableDirectory and EWriteableDirectory retained.
    public EReadableDirectory restrictToReadOnly()
    {
        return new EReadableDirectory(rootPath, path, canList, canChdir, canReadWrite, canSetRootPath);
    }
    
    //
    // Public Methods
    //
    
    // create a new subdirectory
    public void makeSubdirectory(EDirectoryEntry dirEntry) throws ERestrictedException {
        makeSubdirectory(dirEntry.getName());
    }
    
    public void makeSubdirectory(String dirname) throws ERestrictedException {
        if (canCreateDir) {
            if (validLocalPath(dirname)) {
                // create a File out of the absolute directory path and the
                // name of the directory to be created
                File newdir = new File(absPathPlus(dirname));
                if (!newdir.exists()) { // prevents clobbering
                    if (!newdir.mkdir()) {
                        throw new ERestrictedException("EWritableDirectory: makeSubdirectory: filesystem failure");
                    }
                } else throw new ERestrictedException("EWritableDirectory: makeSubdirectory: directory already exists"); 
            } else throw new ERestrictedException("EWritableDirectory: makeSubdirectory: invalid directory name");
        } else {
            throw new ERestrictedException("EWritableDirectory canCreateDir") ;
        }
    }
    
    // delete a subdirectory (throws exception if directory not empty)
    public void deleteSubdirectory(EDirectoryEntry dirEntry) throws ERestrictedException {
        deleteSubdirectory(dirEntry.getName());
    }
    
    public void deleteSubdirectory(String dirname) throws ERestrictedException {
        if (canDeleteDir) {
            if (validLocalPath(dirname)) {
                File dir = new File(absPathPlus(dirname));
                if (dir.isDirectory()) {
                    if (!dir.delete()) {
                        throw new ERestrictedException("EWritableDirectory: deleteSubdirectory: filesystem failure");
                    }
                } else throw new ERestrictedException(
                                                      "EWritableDirectory: deleteSubdirectory: " + dirname + " is not a directory");
            } else throw new ERestrictedException(
                                                  "EWritableDirectory: deleteSubdirectory: invalid directory name: " + dirname);
        } else {
            throw new ERestrictedException("EWritableDirectory canDeleteDir") ;
        }
    }
    
    // rename a subdirectory
    public void renameSubdirectory(EDirectoryEntry dirEntry, String newName) throws ERestrictedException {
        renameSubdirectory(dirEntry.getName(), newName);
    }
    
    public void renameSubdirectory(String dirname, String newName) throws ERestrictedException {
        if (canRenameDir) {
            if (validLocalPath(dirname)) {
                File dir = new File(absPathPlus(dirname));
                File newdir = new File(absPathPlus(newName));
                if (dir.isDirectory()) {
                    if (!newdir.exists()) { // prevents clobbering
                        if (!dir.renameTo(new File(newName))) {
                            throw new ERestrictedException("EWritableDirectory: renameSubdirectory: filesystem failure");
                        }
                    } else throw new ERestrictedException(
                                                          "EWritableDirectory: renameSubdirectory: " + newName + " already exists");
                } else throw new ERestrictedException(
                                                      "EWritableDirectory: renameSubdirectory: " + dirname + " is not a directory");
            } else throw new ERestrictedException(
                                                  "EWritableDirectory: renameSubdirectory: invalid directory name: " + dirname);
        } else {
            throw new ERestrictedException("EWritableDirectory canRenameDir") ;
        }
    }
    
    // delete a file
    public void deleteFile(EDirectoryEntry fileEntry) throws ERestrictedException {
        deleteFile(fileEntry.getName());
    }
    
    public void deleteFile(String fileName) throws ERestrictedException {
        if (canDelete) {
            if (validLocalPath(fileName)) {
                File file = new File(absPathPlus(fileName));
                if (!file.isDirectory()) {
                    if (!file.delete()) {
                        throw new ERestrictedException("EWritableDirectory: deleteFile: filesystem failure");
                    }
                } else throw new ERestrictedException(
                                                      "EWritableDirectory: deleteFile: " + fileName + " is not a file");
            } else throw new ERestrictedException(
                                                  "EWritableDirectory: deleteFile: invalid filename: " + fileName);
        } else {
            throw new ERestrictedException("EWritableDirectory canDelete") ;
        }
    }
    
    // rename a file
    public void renameFile(EDirectoryEntry fileEntry, String newName) throws ERestrictedException, IOException {
        renameFile(fileEntry.getName(), newName);
    }
    
    public void renameFile(String fileName, String newName) throws ERestrictedException, IOException {
        moveFile(fileName, newName, this);  
    }
    
    // move a file, given a directory in which to move it to.  Directly checks the
    // values of the target EWritableDirectory's instance variables to determine
    // legality of the move, then uses the java File class's renameTo function
    // to accomplish the move (the renameTo function can span directories)
    public void moveFile(EDirectoryEntry localFileEntry, String targetFileName,
                         EWritableDirectory targetDir) throws ERestrictedException, IOException {
                             moveFile(localFileEntry.getName(), targetFileName, targetDir);
    }
    
    public void moveFile(String localFileName, String targetFileName,
                         EWritableDirectory targetDir) throws ERestrictedException, IOException {
                             if (targetDir == null) targetDir = this; // null directory means this directory    
                             if (validLocalPath(localFileName) && validLocalPath(targetFileName)) {
                                 if (canDelete && targetDir.canReadWrite) {
                                     File localFile = new File(absPathPlus(localFileName));
                                     File targetFile = new File(targetDir.absPathPlus(targetFileName));
                                     if (targetFile.exists() && !targetDir.canDelete) {
                                         throw new ERestrictedException("EWritableDirectory target of move is a file, canDelete");
                                     } else {
                                         if (!localFile.renameTo(targetFile)) {
                                             throw new ERestrictedException("EWritableDirectory: moveFile: filesystem failure");
                                         }
                                     } 
                                 } else throw new ERestrictedException("EWritableDirectory can't move file");
                             } else throw new ERestrictedException(
                                                                   "EWritableDirectory: moveFile: invalid filename: " + localFileName);
    }
    
    // open a file in this directory, in some access mode
    public EReadableFile openFile(String filename, String mode) throws ERestrictedException, IOException { 
        
        // mode are "r", "rw" and "a"
        boolean read = mode.equals("r"); 
        boolean write = mode.equals("rw"); 
        boolean append = mode.equals("a"); 
        if (!read && !write && !append) {
            throw new IOException("EWritableDirectory bad mode string");
        }
        if (validLocalPath(filename)) {
            String fullPath = absPathPlus(filename);
            File file = new File(fullPath);
            boolean exists = file.exists();
            if (file.isDirectory()) {
                throw new ERestrictedException("EWritableDirectory tried to open a directory as file"); 
            } else if (!exists && (read || append)) {
                throw new ERestrictedException("EWritableDirectory can't create file for read or append");
            } 
            if (read) {
                return new EReadableFile(fullPath);
            } else if ((append && canAppend) || (write && canReadWrite)) {
                return new EWritableFile(fullPath, write, append);
            } else {
                throw new ERestrictedException("EWritableDirectory: openFile: don't have capability to open in mode: " + mode);
            }
        } else throw new ERestrictedException("EWritableDirectory: openFile: file not in this directory: " + filename);
        
    }
    
}

//
// Exceptions
//

public class EWritableDirectoryException extends Exception {
    public EWritableDirectoryException(String message) {
        super(message);
    }
}

