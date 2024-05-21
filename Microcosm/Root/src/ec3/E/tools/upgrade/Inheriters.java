// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;

import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import ec.transform.classparser.ClassFile;

/**
 * Inheriters supports inquiries about which classes in a path inherit from a 
 * given class.  It also reports which classes were referenced, but not found 
 * during the search.
 * <p>N.B. All names used by this class are canonicalized to use "." as a separator.
 * This means that fully qualified class names have the same syntax here as they
 * have in Java source code.
 */
public class Inheriters {

    private Path myPath;
    private Hashtable myClasses;    // lookup ClassFile on string className
    private boolean myIsAllUnknown;
    private Hashtable myUnfoundClasses = null;

    /**
     * Create a database of classes from a path.
     *
     * @param path is a list of directories and zip/jar files to use to build
     * the database.  It uses the same syntax as the platform's class path.
     */

    public Inheriters(String path) throws IOException {
        myPath = new Path(path);
        myClasses = new Hashtable();
        parseClasses();
    }

    /**
     * Return a list of classes which inherit from a given class.
     *
     * @param className the given class.
     *
     * @return a vector of strings.  Each string is the fully qualified name
     * of one class which inherits (directly or indirectly) from className.
     */

    public Vector inheriters(String className) {
        if (!myIsAllUnknown) {
            setUnknown();
        }
        myUnfoundClasses = null;
        Vector ret = new Vector();
        String name = cannonizeClassName(className);

        Enumeration keys = myClasses.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (inheritsFrom(key, name)) {
                ret.addElement(key);
            }
        }
        return ret;
    }

    /**
     * Return a list of classes which which could not be found in the path used
     * to construct this object during the last call to "inheriters".
     *
     * @return a vector of strings or null.  Each string is the fully qualified 
     * name of one class which could not be found during the last call to 
     * "inheriters".  Returns null if called before "inheriters", or no classes
     * were missing.
     */

    public /*NilOK*/Vector undefinedClasses() {
        if (null != myUnfoundClasses) {
            Vector ret = new Vector(myUnfoundClasses.size());
            Enumeration keys = myUnfoundClasses.keys();
            while (keys.hasMoreElements()) {
                ret.addElement(keys.nextElement());
            }
            return ret;
        }
        return null;
    }

    private void parseClasses() throws IOException {
        Enumeration files = myPath.fileElements();
        while (files.hasMoreElements()) {
            PathElement pe = (PathElement)files.nextElement();
            if (pe.file.endsWith(".class")) {
                processClassFile(pe.file.substring(0,pe.file.length()-6), pe.data);
            }
        }
        myIsAllUnknown = true;
    }

    private void processClassFile(String className, InputStream is) throws IOException {
        int available = is.available();
        if (available < 4096) available = 4096;
        BufferedInputStream bs = new BufferedInputStream(is, available);
        DataInputStream ds = new DataInputStream(bs);
        ClassFile cf = new ClassFile(ds);
        ds.close();
        int[] interfaces = cf.getInterfaces();
        String[] superClasses = new String[interfaces.length+1];
        superClasses[0] = getClassName(cf, cf.getSuperClass());
        for (int i=0; i<interfaces.length; i++) {
            superClasses[i+1] = getClassName(cf, interfaces[i]);
        }
        InheritersStruct inheriters = new InheritersStruct(superClasses);
        myClasses.put(cannonizeClassName(cannonizeClassName(className)), inheriters);
    }

    private String getClassName(ClassFile cf, int classIndex) {
        if (0 == classIndex) return ""; //No class here, should be java.lang.Object
        return cannonizeClassName(cf.getClassName(classIndex));
    }

    private String cannonizeClassName(String className) {
        String ret = className.replace(File.separatorChar,'.');
        ret = ret.replace('/','.');
        return ret.replace('\\','.');
    }
    
    private void setUnknown() {
        Enumeration structs = myClasses.elements();
        while (structs.hasMoreElements()) {
            InheritersStruct is = (InheritersStruct)structs.nextElement();
            is.inheritState = InheritersStruct.UNKNOWN;
        }
        myIsAllUnknown = true;
    }

    private boolean inheritsFrom(String subClass, String superClass) {
        InheritersStruct is = (InheritersStruct)myClasses.get(subClass);
        if (null == is) {
            if (null == myUnfoundClasses) myUnfoundClasses = new Hashtable();
            myUnfoundClasses.put(subClass, subClass);
            return false;
        }
        switch (is.inheritState) {
         case InheritersStruct.UNKNOWN:
            myIsAllUnknown = false;
            String [] directSuper = is.superClasses;
            for (int i=0; i<directSuper.length; i++) {
                if (inheritsFrom(directSuper[i], superClass)
                        || superClass.equals(directSuper[i])) {
                    is.inheritState = InheritersStruct.INHERITS;
                    return true;
                }
            }
            is.inheritState = InheritersStruct.DOESNT_INHERIT;
            return false;
         case InheritersStruct.INHERITS:
            return true;
         case InheritersStruct.DOESNT_INHERIT:
            return false;
         default:
            throw new RuntimeException("Invalid state in InheritersStruct: "+is);
        }
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        Enumeration keys = myClasses.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String [] from = ((InheritersStruct)myClasses.get(key)).superClasses;
            ret.append("\n").append(key).append("==>");
            for (int i=0; i<from.length; i++) {
                ret.append(from[i]).append(", ");
            }
            ret.setLength(ret.length()-2);
        }
        return ret.toString();
    }
}



/*package*/ class InheritersStruct {
    static final int UNKNOWN = 0;
    static final int INHERITS = 1;
    static final int DOESNT_INHERIT = 2;

    /*package*/ int inheritState;
    /*package*/ String[] superClasses;

    /*package*/ InheritersStruct(String[] sc) {
        inheritState = UNKNOWN;
        superClasses = sc;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();

        ret.append(super.toString()).append(" inheritState=");
        ret.append(inheritState).append(" superClasses=");
        for (int i=0; i<superClasses.length; i++) {
            ret.append(superClasses[i]).append(", ");
        }
        ret.setLength(ret.length()-2);
        return ret.toString();
    }
}