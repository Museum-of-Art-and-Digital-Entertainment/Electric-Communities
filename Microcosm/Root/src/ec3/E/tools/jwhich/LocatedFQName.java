package ec.jwhich;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.Vector;
import java.util.Hashtable;

/**
 * Represents a particular place where there might be storage corresponding 
 * to a fully qualified name.
 */
public abstract class LocatedFQName {
    
    static public final int DOESNT_EXIST = 0;
    static public final int IS_PACKAGE   = 1;
    static public final int IS_CLASSFILE = 2;
    
    private String myLocation;
    private String myFQName;

    /*package*/ LocatedFQName(String location, String fQName) {
        myLocation = location;
        myFQName = fQName;
    }

    /**
     * Where is the storage physically located?  This string is in a
     * format meaningful to a classloader, such as a filename in a
     * CLASSPATH.  When the location does represent a filename, the
     * separator character is normalized to "/", with no terminal "/" even if 
     * it represents a directory.
     */
    public String location() {
        return myLocation;
    }

    /**
     * The fully qualified name of this package, class, or whatever; with "." 
     * as the separator.  The fully qualified name of the root package is the 
     * empty string.
     */
    public String fullyQualifiedName() {
        return myFQName;
    }

    /**
     * The part of the name after the last dot
     */
    public String name() {
        int i = myFQName.lastIndexOf(".");
        /*
         * Since lastIndexOf returns -1 on failure, we don't need a test.  
         * Rather, we take the -1 to be the index of a virtual dot before the 
         * first character.
         */
        return myFQName.substring(i+1);
    }
    
    static /*package*/ String join(String prefix, String sep, String suffix) {
        if (prefix.length() == 0) {
            return suffix;
        } else if (suffix.length() == 0) {
            return prefix;
        } else {
            return prefix + sep + suffix;
        }
    }    

    static /*package*/ boolean isJavaIdentifier(String str) {
        if (! Character.isJavaIdentifierStart(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < str.length(); i++) {
            if (! Character.isJavaIdentifierPart(str.charAt(0))) {
                return false;
            }
        }
        return true;
    }    

    static private final String[] status = {
        "no",
        "package",
        "class"
    };
    
    /**
     * 
     */
    public String toString() {
        String prefix;
        try {
            prefix = status[existence()];
        } catch (IOException iox) {
            prefix = "exception (" + iox + ") with";
        }    
        return prefix + " " + join(myFQName, " in ", myLocation);
    }

    /**
     * Returns a Vector of LocatedFQNames representing the current
     * CLASSPATH.  Since these are root packages, their
     * fullyQualifiedName()s will be empty.
     */
    static public Vector classPathVector() {
        return pathVector(System.getProperty("java.class.path"));
    }

    /**
     * Given a path (like the CLASSPATH) of strings separated by the
     * local system's path separator (usually ";"), return a Vector of
     * LocatedFQNames in order, skipping the empty ones.  Since these
     * are root packages, their fullyQualifiedName()s will be empty.
     */
    static public Vector pathVector(String path) {
        int len = path.length();
        char sep = File.pathSeparatorChar;
        Vector result = new Vector();
        int j;
        for (int i = 0; i < len; i = j + 1) {
            j = path.indexOf(sep, i);
            if (j <= i + 1) {
                if (j == -1) {
                    //virtual terminator after the last string
                    j = len;
                } else if (j == i + 1) {
                    //skip zero-length strings
                    continue;
                } else if (j == i) {
                    //Probably a ;;
                    continue;
                } else {
                    throw new Error("LocatedFQName: I'm very confused");
                }    
            }
            String dir = path.substring(i, j);
            result.addElement(make(dir, ""));
        }
        return result;
    }

    /**
     * Returns an object that represents storage for the given fully 
     * qualified name rooted at the location.  With one minor exception, the 
     * result depends only on the strings, not the file system.  The 
     * exception is that the location is first canonicalized with 
     * File.getCanonicalPath(), if it works.
     */
    static public LocatedFQName make(String location, String fQName) {
        if (location.equals("__repository__")) {
            //return new RepositoryPackage(fQName);
            throw new Error
                ("not yet implemented: repository as package storage");
        }
        
        //normalize the location
        try {
            location = new File(location).getCanonicalPath();
        } catch (IOException iox) {
            location = new File(location).getAbsolutePath();
        }
        location = location.replace(File.separatorChar, '/');
        location = location.replace('\\', '/');
        if (location.endsWith("/") && location.length() > 1) {
            location = location.substring(0, location.length() -1);
        }
        String lower = location.toLowerCase(); //only for comparison
        if (lower.endsWith(".zip") || lower.endsWith(".jar")) {
            return new ZipFQName(location, fQName);
        } else {
            return new DirFQName(location, fQName);
        }
        //perhaps more cases eventually
    }    
    
    /**
     * Returns a LocatedFQName within the current one, whether or not either 
     * exists().  Does not depend on the state of the file system.
     */
    public LocatedFQName get(String relativeName) {
        return make(myLocation, join(myFQName, ".", relativeName));
    }    

    /**
     * What kind of object exists at the storage rooted at location() that 
     * corresponds to the fully qualified name?  Returns one of DOESNT_EXIST, 
     * IS_PACKAGE, or IS_CLASSFILE as appropriate.  If the object seems to 
     * exist but there seems to be a problem, throws IOException.
     */
    public abstract int existence() throws IOException;
    
    /**
     * If it's a package, returns a mapping from the next name to the next 
     * LocatedFQName object.  The mapping derives from a snapshot from the 
     * file system at the time of the query.
     */
    public abstract Hashtable members() throws IOException;
    
    /**
     * If it's something that consists of a sequence of bytes (currently only 
     * a classfile), returns an InputStream for reading those bytes.  The 
     * bytes are only expected to be correct if the relevant parts of the 
     * file system haven't changed since contents() were requested.
     */
    public abstract InputStream contents() throws IOException;
}

