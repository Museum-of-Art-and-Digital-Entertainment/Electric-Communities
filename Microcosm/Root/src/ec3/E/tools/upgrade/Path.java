// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.Stack;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import ec.transform.classparser.ClassFile;

/**
 * This class abstracts the concept of Path.  It is created with a string in
 * the syntax of CLASSPATH which defines a collection of directories: file
 * system directories and zip/jar file directories.  It then provides methods  
 * which allow access to those directories.
 */
public class Path {

    private Vector myPath;

    /**
     * Create a Path with the system classpath.
     *
     */

    public Path() {
        this(System.getProperty("java.class.path"));
    }

    /**
     * Create a path.
     *
     * @param path is a list of directories and zip/jar files.  
     * It uses the same syntax as the platform's class path.
     */

    public Path(String path) {
        myPath = parsePath(path);
    }

    /**
     * Parse a path into a Vector.
     *
     * @param name is the path string
     *
     * @return a Vector which contains the (String) file/directory names of the path.
     */

    public Vector parsePath(String pathName) {
        String sep = System.getProperty("path.separator");

        Vector pathVector = new Vector();
        while (0 != pathName.length()) {
            int i = pathName.indexOf(sep);
            String dir;
            if (i <= 0) {
                if (0 == i) continue;   // Skip empty elements
                dir = pathName;
                pathName = "";
            } else {
                dir = pathName.substring(0,i);
                pathName = pathName.substring(i+1);
            }
            pathVector.addElement(dir);
        }
        return pathVector;
    }

    /**
     * Enumerate all the elements of a path.
     *
     * @return an Enumeration which returns the PathElement for each path
     * element in order from first searched to last searched.  The caller 
     * should call data.close(); for each element returned to ensure against
     * running out of file handles in certain broken JVMs.
     */

    public Enumeration fileElements() {
        return new PathEnum(myPath, true);
    }

    /**
     * Enumerate all the elements of a path.
     *
     * @return an Enumeration which returns the PathElement for each path
     * element in order from first searched to last searched.  The data 
     * field in each PathElement returned will be null.
     */

    public Enumeration fileElementsNoStream() {
        return new PathEnum(myPath, false);
    }

    /**
     * Open a file on the first matching name in the path.
     *
     * @param name the file name to be opened.
     *
     * @return an InputStream opened on the file/.zip/.jar.
     */

    public InputStream /*nullOK*/ open(String name) throws IOException {
        Enumeration path = myPath.elements();
        while (path.hasMoreElements()) {
            String directoryName = (String)path.nextElement();
            File dir = new File(directoryName);
            if (!dir.exists()) continue;
            if (directoryName.endsWith(".zip") || directoryName.endsWith(".jar")) {
                ZipFile zf = new ZipFile(dir);
                ZipEntry zip = zf.getEntry(name);
                if (null == zip) {
                    zf.close();
                    continue;
                }
                return new PathInputStream(zf.getInputStream(zip), zf);
            } else {
                File file = new File(dir, name);
                if (!file.canRead()) continue;
                return new FileInputStream(file);
            }
        }
        return null;
    }
}


/*package*/ class PathEnum implements Enumeration {
    private boolean myReturnStreams;
    private Enumeration myPath;
    private Object myNextElement = null;
    private String myDirectoryName;
    private Enumeration myCurrentZip;
    private ZipFile myCurrentZipFile;
    private Stack myCurrentDir = new Stack();
    private String myCurrentPath = "";
    
    PathEnum(Vector path, boolean returnStreams) {
        myPath = path.elements();
        myReturnStreams = returnStreams;
    }

    public boolean hasMoreElements() {
        try {
            myNextElement = nextElement();
            return true;
        } catch(NoSuchElementException e) {
            if (myPath.hasMoreElements() || null != myCurrentZip || !myCurrentDir.empty()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public Object nextElement() throws NoSuchElementException {
        while (true) {
            if (null != myNextElement) {
                Object ret = myNextElement;
                myNextElement = null;
                return ret;
            } else if (null != myCurrentZip) {
                if (myCurrentZip.hasMoreElements()) {
                    return nextZipElement();
                } else {
                    myCurrentZip = null;
                    myCurrentZipFile = null;
                }
            } else if (!myCurrentDir.empty()) {
                Enumeration dir = (Enumeration)myCurrentDir.peek();
                if (dir.hasMoreElements() ) {
                    Object ret = nextDirElement(dir);
                    if (null != ret) return ret;
                } else {
                    myCurrentDir.pop(); // Pop the stack
                    popMyCurrentPath();
                }
            } else if (myPath.hasMoreElements()) {
                myDirectoryName = (String)myPath.nextElement();
                File file = new File(myDirectoryName);
                if (!file.exists()) continue;
                if (myDirectoryName.endsWith(".zip") || myDirectoryName.endsWith(".jar")) {
                    try {
                        myCurrentZipFile = new ZipFile(file);
                        myCurrentZip = myCurrentZipFile.entries();
                    } catch(IOException e) {
                        throwInternalException(e);
                    }
                } else {
                    if (file.isDirectory()) {
                        Object[] dirList = file.list();
                        Vector v = new Vector();
                        for (int i=0; i<dirList.length; i++) {
                            v.addElement(dirList[i]);
                        }
                        myCurrentDir.push(v.elements());
                        //myCurrentPath should remain empty
                    } else {
                        try {
                            return new PathElement(myDirectoryName, "",
                                                new FileInputStream(file));
                        } catch(IOException e) {
                            throwInternalException(e);
                        }
                    }
                }
            } else {
                throw new NoSuchElementException("Out of elements");
            }
        }
    }

    private PathElement nextZipElement() throws NoSuchElementException {
        ZipEntry ze = (ZipEntry) myCurrentZip.nextElement();
        String zipName = ze.getName();
        try {
            if (myReturnStreams) {
                return new PathElement(myDirectoryName, zipName, 
                                   myCurrentZipFile.getInputStream(ze));
            } else {
                return new PathElement(myDirectoryName, zipName, null);
            }
        } catch(IOException e) {
            throwInternalException(e);
            return null;    // Never executed
        }
    }

    private PathElement nextDirElement(Enumeration dir) throws NoSuchElementException {
        String name = (String)dir.nextElement();
        String fullName;
        if (myCurrentPath.equals("")) {
            fullName = myDirectoryName+File.separator+name;
        } else {
            fullName = myDirectoryName+File.separator+myCurrentPath+File.separator+name;
        }
        File file = new File(fullName);
        if (file.isDirectory()) {
            Object[] dirList = file.list();
            Vector v = new Vector();
            for (int i=0; i<dirList.length; i++) {
                v.addElement(dirList[i]);
            }
            myCurrentDir.push(v.elements());
            if (myCurrentPath.equals("")) {
                myCurrentPath = name;
            } else {
                myCurrentPath += File.separator+name;
            }
            return null;
        } else {
            String fullname = myCurrentPath.equals("") ? name
                        : myCurrentPath+File.separator+name;
            try {
                if (myReturnStreams) {
                return new PathElement(myDirectoryName, 
                            fullname,
                            new FileInputStream(file));
            } else {
                return new PathElement(myDirectoryName, 
                            fullname,
                            null);
            }
            } catch(IOException e) {
                throwInternalException(e);
                return null;    // Never executed
            }
        }
    }

    private void throwInternalException(Throwable e) throws NoSuchElementException {
        throw new NoSuchElementException(e.toString());
    }

    private void popMyCurrentPath() {
        int ndx = myCurrentPath.lastIndexOf(File.separatorChar);
        if (-1 == ndx) {
            myCurrentPath = "";
        } else {
            myCurrentPath = myCurrentPath.substring(0,ndx);
        }
    }
}

/**
 * PathInputStream exists to close ZipFile as well as the input stream when
 * the caller is done reading the stream.
 *
 * <p>What a dumb thing to have to write.
 */

/*package*/ class PathInputStream extends InputStream {

    private InputStream myStr;
    private ZipFile myFile;

    /*package*/ PathInputStream (InputStream str, ZipFile file) {
        myStr = str;
        myFile = file;
    }
    
    public int available() throws IOException {
        return myStr.available();
    }

    public void close() throws IOException {
        myStr.close();
        myFile.close();
    }

    public void mark(int readlimit) {
        myStr.mark(readlimit);
    }

    public boolean markSupported() {
        return myStr.markSupported();
    }

    public int read() throws IOException {
        return myStr.read();
    }

    public int read(byte b[]) throws IOException {
        return myStr.read(b);
    }

    public int read(byte b[], int off, int len) throws IOException {
        return myStr.read(b, off, len);
    }

    public void reset() throws IOException {
        myStr.reset();
    }

    public long skip(long n) throws IOException {
        return myStr.skip(n);
    }
}