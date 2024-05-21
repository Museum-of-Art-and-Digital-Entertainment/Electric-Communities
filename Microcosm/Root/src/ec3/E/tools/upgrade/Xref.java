// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;

import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import ec.transform.classparser.ClassFile;
import ec.transform.classparser.Method;
import ec.transform.classparser.Field;
import ec.transform.classparser.Constant;
import ec.transform.classparser.ConstantRefType;

/**
 * Xref generates information about which classes in a path invoke which methods 
 * in other classes.  It also reports which classes were referenced, but not found 
 * during the search.
 * <p>N.B. All names used by this class are canonicalized to use "." as a separator.
 * This means that fully qualified class names have the same syntax here as they
 * have in Java source code.
 */
public class Xref {

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

    public Xref(String path) throws IOException {
        myPath = new Path(path);
        myClasses = new Hashtable();
        parseClasses();
        resolveReferences();
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
     * @return The only reference to a new vector of strings or null.  Each 
     * string is the fully qualified 
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

            // Sort the classes into alphabetical order
            Sort.sortVector(ret, new StringCompare());
            return ret;
        }
        return null;
    }

    /**
     * Prints the cross reference on a print stream.
     *
     * @param out The PrintStream that will receive the cross reference.
     */

    public void printXref(PrintStream out) {
        SortCompare stringComparer = new StringCompare();   // Comparer for sorts

        // Sort the classes into alphabetical order
        Vector classes = new Vector(myClasses.size()); // Make a vector of the classes
        Enumeration keys = myClasses.keys();
        while (keys.hasMoreElements()) {
            classes.addElement(keys.nextElement());
        }
        Sort.sortVector(classes, stringComparer);

        for (int i=0; i<classes.size(); i++) {
            String className = (String)classes.elementAt(i);
            out.println(className);         // Put class name in left margin
            XrefData classData = (XrefData)myClasses.get(className);

            if (null != classData.myDefinedFields) {        // Xref the fields
                // Sort the fields into alphbetical order
                Vector fields = new Vector(classData.myDefinedFields.size());
                Enumeration fks = classData.myDefinedFields.keys();
                while (fks.hasMoreElements()) {
                    fields.addElement(fks.nextElement());
                }
                Sort.sortVector(fields, stringComparer);

                // Process each method, print it's name and the referencing classes
                for (int j=0; j<fields.size(); j++) {
                    String name = (String)fields.elementAt(j);
                    out.print("    "); out.println(name);
                    
                    XrefTarget refs = (XrefTarget)classData.myDefinedFields.get(name);

                    if (null != refs.references) {
//System.err.println("RefCount=" + refs.references.size());
                        // Sort and print the references
                        Sort.sortVector(refs.references, stringComparer);
                        for (int k=0; k< refs.references.size(); k++) {
                            out.print("        ");
                            out.println((String)refs.references.elementAt(k));
                        }
                    }
                }
            }

            if (null != classData.myDefinedMethods) {       // Do the methods
                // Sort the methods into alphbetical order
                Vector methods = new Vector(classData.myDefinedMethods.size());
                Enumeration mks = classData.myDefinedMethods.keys();
                while (mks.hasMoreElements()) {
                    methods.addElement(mks.nextElement());
                }
                Sort.sortVector(methods, stringComparer);

                // Process each method, print it's name and the referencing classes
                for (int j=0; j<methods.size(); j++) {
                    String name = (String)methods.elementAt(j);
                    out.print("    "); out.println(name);
                    
                    XrefTarget refs = (XrefTarget)classData.myDefinedMethods.get(name);

                    // Sort and print the references
                    if (null != refs.references) {
//System.err.println("RefCount=" + refs.references.size());
                        Sort.sortVector(refs.references, stringComparer);
                        for (int k=0; k< refs.references.size(); k++) {
                            out.print("        ");
                            out.println((String)refs.references.elementAt(k));
                        }
                    }
                }
            }
        }
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

        String name = cannonizeClassName(cf.getClassObject().getName());
        int[] interfaces = cf.getInterfaces();
        String[] superClasses = new String[interfaces.length+1];
        superClasses[0] = getClassName(cf, cf.getSuperClass());
        for (int i=0; i<interfaces.length; i++) {
            superClasses[i+1] = getClassName(cf, interfaces[i]);
        }
        XrefData xref = new XrefData(name, superClasses);
        
        // Get all the methods defined in the class/interface
        Enumeration methods = cf.methods();
        while (methods.hasMoreElements()) {
            Method m = (Method)methods.nextElement();
            xref.defineMethod(m);
        }
        
        // Get all the methods defined in the class/interface
        Enumeration fields = cf.fields();
        while (fields.hasMoreElements()) {
            Field f = (Field)fields.nextElement();
            xref.defineField(f);
        }
        
        // Get all the references from the class/interface
        Enumeration references = cf.references();
        while (references.hasMoreElements()) {
            ConstantRefType r = (ConstantRefType)references.nextElement();
            xref.defineReference(r, myClasses);
        }

        myClasses.put(name, xref);
    }


    private String getClassName(ClassFile cf, int classIndex) {
        if (0 == classIndex) return ""; //No class here, should be java.lang.Object
        return cannonizeClassName(cf.getClassName(classIndex));
    }

    static /*package*/ String cannonizeClassName(String className) {
        String ret = className.replace(File.separatorChar,'.');
        ret = ret.replace('/','.');
        return ret.replace('\\','.');
    }
    
    private void setUnknown() {
        Enumeration structs = myClasses.elements();
        while (structs.hasMoreElements()) {
            XrefData is = (XrefData)structs.nextElement();
            is.inheritState = XrefData.UNKNOWN;
        }
        myIsAllUnknown = true;
    }

    private void resolveReferences() {
        // First resolve the unresolved references
        Enumeration classes = myClasses.keys();
        while (classes.hasMoreElements()) {
            String thisClassName = (String)classes.nextElement();
            XrefData is = (XrefData)myClasses.get(thisClassName);
            resolveToTargets(is.myMethodReferences, true, thisClassName);
            resolveToTargets(is.myFieldReferences, false, thisClassName);
        }

        /* Next propogate references as follows:
              if class A references B.m and m is not a constructer
                 and class C inherits from B and also implements m
              then class A references C.m as well as B.m.
        */
        classes = myClasses.elements();
        while (classes.hasMoreElements()) {     // For all the classes we know
            XrefData topClass = (XrefData)classes.nextElement();
            Vector subClasses = inheriters(topClass.myClassName);
            for (int i=0; i<subClasses.size(); i++) {   // For all subclasses
                String subClassName = (String)subClasses.elementAt(i);
                XrefData subClass = (XrefData)myClasses.get(subClassName);
                if (null != topClass.myDefinedMethods) {
                    Enumeration methods = topClass.myDefinedMethods.keys();
                    while (methods.hasMoreElements()) {
                        String name = (String)methods.nextElement();
                        if (name.startsWith("<init>") || name.startsWith("<cinit>")) {
                            // Don't propergate calls to constructors
                        } else {
                            XrefTarget subMethod= subClass.getMethod(name);
                            if (null != subMethod) {
                                XrefTarget superMethod 
                                        = (XrefTarget)topClass.myDefinedMethods.get(name);
                                if (null == subMethod.references) {
                                    if (null != superMethod.references) {
                                        subMethod.references = (Vector)superMethod.references.clone();
                                    }
                                } else {
                                    mergeVectors(subMethod.references, superMethod.references);
                                }
                            }
                        }
                    }
                }
            }       
        }
    }  

    /**
     * Resolve a Vector of references to the targets in it.
     *
     * @param targets The Vector of target references.
     * @param isResolvingMethods true if references are to methods, 
     *        false if they are to fields.
     */
    private void resolveToTargets(Vector targets, boolean isResolvingMethods,
                        String thisClassName) {
        if (null == targets) return;

        for (int i=0; i<targets.size(); i++) {
            XrefPendingReference x = (XrefPendingReference)targets.elementAt(i);

            // Skip a classes references to itself
            if (x.className.equals(thisClassName)) continue;

            XrefData cl = (XrefData)myClasses.get(x.className);
            if (null == cl) {
                if (null == myUnfoundClasses) myUnfoundClasses = new Hashtable();
                myUnfoundClasses.put(x.className, x.className);
            } else {
                XrefTarget target;
                if (isResolvingMethods) {
                    target = cl.getMethod(x.elementName);
                } else {
                    target = cl.getField(x.elementName);
                }
                if (null != target) {
                    target.addReference(thisClassName);
                } else {
                    String kind = (isResolvingMethods ? "Method " : "Field ");
                    System.err.println(kind + x.elementName 
                            + " not found in " + x.className 
                            + " referenced from " + thisClassName);
                }
            }
        }
    }

    /**
     * Merge the values in two Vectors, eliminating duplicates
     *
     * @param inOut One of the Vectors to be merged.  It will contain the merged
     *        output
     * @param in The other vector to be merged.  It will remain unchanged.
     */
    private void mergeVectors(Vector inOut, Vector in) {
        if (null == in) return;
        for (int i=0; i<in.size(); i++) {
            Object entry = in.elementAt(i);
            if (!inOut.contains(entry)) {
                inOut.addElement(entry);
            }
        }
    }

    private boolean inheritsFrom(String subClass, String superClass) {
        XrefData is = (XrefData)myClasses.get(subClass);
        if (null == is) {
            if (null == myUnfoundClasses) myUnfoundClasses = new Hashtable();
            myUnfoundClasses.put(subClass, subClass);
            return false;
        }
        switch (is.inheritState) {
         case XrefData.UNKNOWN:
            myIsAllUnknown = false;
            String [] directSuper = is.superClasses;
            for (int i=0; i<directSuper.length; i++) {
                if (inheritsFrom(directSuper[i], superClass)
                        || superClass.equals(directSuper[i])) {
                    is.inheritState = XrefData.INHERITS;
                    return true;
                }
            }
            is.inheritState = XrefData.DOESNT_INHERIT;
            return false;
         case XrefData.INHERITS:
            return true;
         case XrefData.DOESNT_INHERIT:
            return false;
         default:
            throw new RuntimeException("Invalid state in XrefData: "+is);
        }
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        Enumeration keys = myClasses.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String [] from = ((XrefData)myClasses.get(key)).superClasses;
            ret.append("\n").append(key).append("==>");
            for (int i=0; i<from.length; i++) {
                ret.append(from[i]).append(", ");
            }
            ret.setLength(ret.length()-2);
        }
        return ret.toString();
    }
}



/*package*/ class XrefData {
    static final int UNKNOWN = 0;
    static final int INHERITS = 1;
    static final int DOESNT_INHERIT = 2;

    /*package*/ int inheritState;
    /*package*/ String myClassName;
    /*package*/ String[] superClasses;
    /*package*/ Hashtable myDefinedMethods;  //(String methodName, XrefTarget)
    /*package*/ Hashtable myDefinedFields;   //(String fieldName, XrefTarget)

    //The following Vectors hold references from this class to classes which
    // were not known when this class was processed.
    /*package*/ Vector myMethodReferences;
    /*package*/ Vector myFieldReferences;

    /*package*/ XrefData(String name, String[] sc) {
        inheritState = UNKNOWN;
        myClassName = name;
        superClasses = sc;
    }

    /*package*/ void defineMethod(Method m) {
        int flags = m.getAccessFlags();

        if ( (flags & Method.ACC_PRIVATE) != 0) return; //Skip private methods

        if (null == myDefinedMethods) {     //Allocate data structure if needed
            myDefinedMethods = new Hashtable();
        }
        myDefinedMethods.put(nameAndSig(m.getName(), m.getDescriptor()), 
                             new XrefTarget(flags));
    }

    /*package*/ void defineField(Field f) {
        int flags = f.getAccessFlags();

        if ( (flags & Field.ACC_PRIVATE) != 0) return;  //Skip private fields

        if (null == myDefinedFields) {      //Allocate data structure if needed
            myDefinedFields = new Hashtable();
        }
        myDefinedFields.put(f.getName(), new XrefTarget(flags));
    }

    /**
     * Record a reference to a field or method.
     *
     * @param r The ConstantRefType which describes the reference.
     * @param knownClasses A hash table linking class name to the XrefData for
     *         the classes already known.
     */
    /*package*/ void defineReference(ConstantRefType r, Hashtable knownClasses) {
        String className = Xref.cannonizeClassName(r.getClassName());
        if (Constant.CONSTANT_Fieldref == r.getTag()) {
            XrefData target = (XrefData)knownClasses.get(className);
            if (null != target) {
                XrefTarget field = target.getField(r.getName());
                if (null == field) {
                    System.err.println("Field " + r.getName() 
                            + " not found in " + className 
                            + " referenced from " + myClassName);
                    return;
                }
                field.addReference(myClassName);
                return;
            } // else make an entry in the pending list of references 
            if (null == myFieldReferences) {        //Allocate data structure if needed
                myFieldReferences = new Vector();
            }
            myFieldReferences.addElement(new XrefPendingReference(className, r.getName()));
            return;
        } 
        XrefData target = (XrefData)knownClasses.get(className);
        if (null != target) {
            XrefTarget method = target.getMethod(nameAndSig(r.getName(), r.getSignature()));
            if (null == method) {
                System.err.println("Method " + nameAndSig(r.getName(), r.getSignature()) 
                        + " not found in " + className 
                        + " referenced from " + myClassName);
                return;
            }
            target.getMethod(nameAndSig(r.getName(), r.getSignature())).addReference(myClassName);
            return;
        } // else make an entry in the pending list of references 
        if (null == myMethodReferences) {       //Allocate data structure if needed
            myMethodReferences = new Vector();
        }
        myMethodReferences.addElement(
                    new XrefPendingReference(className, 
                                nameAndSig(r.getName(), r.getSignature())));
        return;
    }

    /*package*/ XrefTarget /*nilOK*/ getMethod(String name) {
        if (null == myDefinedMethods) return null;
        return (XrefTarget)myDefinedMethods.get(name);
    }

    /*package*/ XrefTarget /*nilOK*/ getField(String name) {
        if (null == myDefinedFields) return null;
        return (XrefTarget)myDefinedFields.get(name);
    }


    private String nameAndSig(String name, String signature) {
        return name + "(" + signature + ")";
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


/*package*/ class XrefTarget {
    /*package*/ int flags;  // The flags (public etc.) for the Method/Field
    Vector references;      // Names of classes refering to
                                        // this method/field.
private static boolean spammed = false;

    /*package*/ XrefTarget(int f) {
        flags = f;
    }

    /**
     * Add a reference to a target
     *
     * @param r The class name containing the reference.
     */
    /*package*/ void addReference(String r) {
        if (null == references) references = new Vector();
        if (!references.contains(r)) references.addElement(r);
    }

    public String toString() {
        return "{" + flags + " " + references + "}";
    }
}


/*package*/ class XrefPendingReference {
    String className;
    String elementName;

    XrefPendingReference(String cl, String el) {
        className = cl;
        elementName = el;
    }
}


/*package*/ class StringCompare implements SortCompare {
    // Returns 1:m1>m2, 0:m1==m2, -1:m1<m2
    public int compare(Object /*nilok*/ m1, Object /*nilok*/ m2) {
        if (null == m1) {
            if (null == m2) return 0;
            return 1;
        } else if (null == m2) return -1;

        return ((String)m1).compareTo((String)m2);
    }
}