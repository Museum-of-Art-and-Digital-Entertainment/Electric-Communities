package ec.e.file;

import java.io.File;
import java.io.IOException;
import ec.e.start.Vat;
import ec.util.ReadOnlyHashtable;

/**
* Abstract base class for all sturdy directory objects.
*/
abstract public class EDirectoryBase extends EDirectoryEntry
implements EFileInfo {
    static final char UNIVERSAL_SEPARATOR = '/';

    /**
    * Base constructor for directory objects.
    *
    * @param vat The vat we are running in.
    * @param file The file we represent.
    */
    EDirectoryBase(Vat vat, File file) {
        super(vat, file);
    }

    /**
    * Produce a hashtable describing the contents of this directory.
    *
    * @return A ReadOnlyHashtable mapping names to objects representing the
    *   things in this directory. The exact nature of these objects depends on
    *   which subclass of EDirectoryBase is implementing contents().
    */
    abstract public ReadOnlyHashtable contents();

    /**
    * Produce an EReadableDirectory representation of this same directory.
    */
    abstract public EReadableDirectory asReadableDirectory();

    /**
    * Length in bytes of this directory file.
    */
    public long length() {
        return(myFile.length());
    }

    /**
    * Determines the time that the directory represented by this object was
    * last modified. As per the Java class library spec, the return value is
    * system-dependent and should only be used to compare with other values
    * returned by lastModified(). It should not be interpreted as an absolute
    * time.
    *
    * @return the "time" the file specified by this object was last modified
    */
    public long lastModified() {
        return(myFile.lastModified());
    }

    /**
    * Test if this entry describes a directory (it does).
    *
    * @return true
    */
    public boolean isDirectory() {
        return(true);
    }

    /**
    * Lookup a specific directory pathname relative to this directory.
    *
    * @param path A pathname (in local format) for the file desired.
    * @return A Java File object corresponding to the directory at the end of
    *   the path in question.
    */
    File lookupDirectoryPath(String path) throws IOException {
        File newFile = lookupPath(path);
        if (newFile.isDirectory())
            return(newFile);
        else
            throw new IOException(path + " is not a directory");
    }

    /**
    * Lookup a specific file pathname relative to this directory.
    *
    * @param path A pathname (in local format) for the file desired.
    * @return A Java File object corresponding to the file at the end of the
    *   path in question.
    */
    File lookupFilePath(String path) throws IOException {
        File newFile = lookupPath(path);
        if (newFile.isFile())
            return(newFile);
        else
            throw new IOException(path + " is not a file");
    }

    /**
    * Count the elements in a path string. This is internal method for parsing
    * path strings.
    *
    * @param pathString An file path string.
    * @return The number of elements in the path string.
    */
    private int countPathElems(String pathString) {
        int count = 1;
        for (int i=0; i<pathString.length(); ++i) {
            if (pathString.charAt(i) == myFile.separatorChar ||
                   pathString.charAt(i) == UNIVERSAL_SEPARATOR)
                ++count;
        }
        return(count);
    }

    /**
    * Lookup a specific directory entry relative to this directory.
    *
    * @param pathString A pathname (in local format) for the object desired.
    * @return A Java File object corresponding to the file or directory at
    *   the end of the path in question.
    * @throws IOException if no such entry could be looked up.
    */
    private File lookupPath(String pathString) throws IOException {

        /* First, break up the path into it's constituent components */
        String givenPathString = pathString;
        int pathLength = countPathElems(pathString);
        String path[] = new String[pathLength];
        for (int i=0; i<pathLength; ++i) {
            path[i] = pathStringHead(pathString);
            pathString = pathStringTail(pathString);
        }

        /* Then, walk the directory file tree from here. By doing this we
           guarantee that the path points to something real and that the user
           isn't trying to sneak upward using ".." as a path element. */
        File result = myFile;
        for (int i=0; i<pathLength; ++i) {
            String contents[] = result.list(); 
            if (contents == null) {
                throw new IOException(path[i] + " in " + givenPathString +
                                      " is not a directory");
            }
            boolean found = false;
            for (int j=0; j<contents.length; ++j) {
                if (path[i].equalsIgnoreCase(contents[j])) {
                    result = new File(result, path[i]);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IOException(givenPathString + " could not be found");
            }
        }
        return(result);
    }

    /**
    * Extract the first element from a path string. This is an internal method
    * for parsing path strings.
    *
    * @param pathString An FQN path string.
    * @return The first element in the path.
    */
    private String pathStringHead(String pathString) {
        int delim = pathString.indexOf(myFile.separatorChar);
        if (delim == -1)
            delim = pathString.indexOf(UNIVERSAL_SEPARATOR);
        if (delim == -1)
            return(pathString);
        else
            return(pathString.substring(0, delim));
    }

    /**
    * Extract the tail elements from a path string. This is an internal method
    * for parsing path strings.
    *
    * @param pathString An FQN path string.
    * @return The path string stripped of its first element.
    */
    private String pathStringTail(String pathString) {
        int delim = pathString.indexOf(myFile.separatorChar);
        if (delim == -1)
            delim = pathString.indexOf(UNIVERSAL_SEPARATOR);
        if (delim == -1)
            return("");
        else
            return(pathString.substring(delim + 1));
    }
}
