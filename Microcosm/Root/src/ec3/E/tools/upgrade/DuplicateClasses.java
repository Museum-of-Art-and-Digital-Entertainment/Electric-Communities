// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;

import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * Inheriters supports inquiries about which classes in a path inherit from a 
 * given class.  It also reports which classes were referenced, but not found 
 * during the search.
 * <p>N.B. All names used by this class are canonicalized to use "." as a separator.
 * This means that fully qualified class names have the same syntax here as they
 * have in Java source code.
 */
public class DuplicateClasses {

    private Path myPath;
    private Hashtable myClasses;    // lookup ClassFile on string className

    /**
     * Create a database of which files contain which classes from a path.
     *
     * @param path is a list of directories and zip/jar files to use to build
     * the database.  It uses the same syntax as the platform's class path.
     */

    public DuplicateClasses(String path) throws IOException {
        myPath = new Path(path);
        myClasses = new Hashtable();
        parseClasses();
    }

    /**
     * Return an Enumeration of classes and which file(s) they are in.
     *
     * @return a Enumeration.  Each nextElement call on the Enumeration 
     * returns a two element array.  The first element is a String and is the
     * classname.  The second element is an array of strings each of which is
     * the file name of a file which contains the class.
     */

    public Enumeration elements() {
        return new DuplicateEnumeration(myClasses);
    }


    private void parseClasses() throws IOException {
        Enumeration files = myPath.fileElementsNoStream();
        while (files.hasMoreElements()) {
            PathElement pe = (PathElement)files.nextElement();
            if (pe.file.endsWith(".class")) {
                processClassFile(pe.file.substring(0,pe.file.length()-6), pe.path);
            }
        }
    }

    private void processClassFile(String className, String path) throws IOException {
        DuplicateStruct data 
                = (DuplicateStruct)myClasses.get(cannonizeClassName(className));
        if (null == data) {
            myClasses.put(cannonizeClassName(className), new DuplicateStruct(path));
        } else {
            String[] files = data.myFiles;
            String[] newfiles = new String[files.length+1];
            System.arraycopy(files, 0, newfiles, 0, files.length);
            newfiles[files.length] = path;
            data.myFiles = newfiles;
        }
    }

    private String cannonizeClassName(String className) {
        String ret = className.replace(File.separatorChar,'.');
        ret = ret.replace('/','.');
        return ret.replace('\\','.');
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        Enumeration keys = myClasses.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String [] from = ((DuplicateStruct)myClasses.get(key)).myFiles;
            ret.append("\n").append(key).append("==>");
            for (int i=0; i<from.length; i++) {
                ret.append(from[i]).append(", ");
            }
            ret.setLength(ret.length()-2);
        }
        return ret.toString();
    }
}



/*package*/ class DuplicateStruct {
    /*package*/ String[] myFiles = new String[1];

    /*package*/ DuplicateStruct(String filename) {
        myFiles[0] = filename;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();

        ret.append(super.toString()).append(" Files=");
        for (int i=0; i<myFiles.length; i++) {
            ret.append(myFiles[i]).append(", ");
        }
        ret.setLength(ret.length()-2);
        return ret.toString();
    }
}



/*package*/ class DuplicateEnumeration implements Enumeration {
    Hashtable myClasses;
    Enumeration myList;

    /*package*/ DuplicateEnumeration(Hashtable classes) {
        myClasses = classes;
        myList = classes.keys();
    }

    public Object nextElement() {
        Object[] ret = new Object[2];
        String className = (String) myList.nextElement();
        ret[0] = className;

        ret[1] = (String[])( (DuplicateStruct)myClasses.get(className)).myFiles;
        return ret;
    }
    
    public boolean hasMoreElements() {
        return myList.hasMoreElements();
    }
}