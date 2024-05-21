/*-----------------------------------------------------------------------

  TypeTable - a list of classes refered to by a database or connection.

  -----------------------------------------------------------------------*/

/** A typetable is a table, specific to an encoded and decoded stream,
  or to a repository file, that maintains a correspondence between a set
  of small integers (used as shorthand type identifiers), and class
  names. Each object that is encoded and decode over the stream or in
  the file is represented in the stream by the type table index to its
  class, and its contents (e.g. instance variables), also encoded in the
  same manner.  */

package ec.e.db;

import ec.vcache.ClassCache;
import ec.tables.ObjKeyTable;
import java.util.Vector;

public final class TypeTable implements RtCodeable {

    static public final Trace tr = new Trace("ec.e.db.TypeTable");
    static public final Trace itr = new Trace("ec.e.db.TypeTable.Profile");

    int                 highestCodeUsed;
    ObjKeyTable classNameToIndex;
    ObjKeyTable classObjectToIndex;
    Vector              indexToClassName;
    Vector              indexToClassObject;

    private void initializeTypeTable (boolean useClasses, int size) {
        highestCodeUsed = 0;
        classNameToIndex = new ObjKeyTable(0, size, true);
        indexToClassName = new Vector(size);
        if (useClasses == true) {
            if (tr.tracing)
                tr.$("Creating Class tables for size " + size);
            classObjectToIndex = new ObjKeyTable(0, size, false);
            indexToClassObject = new Vector(size);
            expandToInclude(size);
        } else {
            if (tr.tracing)
                tr.$("Not creating Class tables");
            classObjectToIndex = null;
            indexToClassObject = null;
            expandToInclude(size);
        }
    }

    /** Constructor */

    public TypeTable () {
        this(true);
    }

    public TypeTable (boolean useClasses) {
        initializeTypeTable(useClasses, 10);
    }

    public TypeTable (boolean useClasses, boolean useAnything) {
        if (useAnything != false)
            initializeTypeTable(useClasses, 10);
    }

    /**
     * Register the given class under the given identity code.
     */
    public boolean registerClassForName (Class theClass, String theClassName,
                                         int theKindCode) {
        if (indexToClassObject == null) {
            tr.$("attempt to register class in a TypeTable without class tables");
            return(false);
        }

        classNameToIndex.put(theClassName, theKindCode);
        classObjectToIndex.put(theClass, theKindCode);
        expandToInclude(theKindCode);
        indexToClassName.setElementAt(theClassName, theKindCode);
        indexToClassObject.setElementAt(theClass, theKindCode);

        if (itr.debug && Trace.ON) System.out.println("Typetable incoming: " + theClassName);

        return(true);
    }

    public boolean registerClassByName (String theClassName, int theKindCode) {
        Class theClass = null;

        try {
            theClass = ClassCache.forName(theClassName);
        } catch (Exception e) {
            tr.$("attempt to register an unknown class: " + theClassName);
            return(false);
        }
        return(registerClassForName(theClass, theClassName, theKindCode));
    }

    /**/
    /* Grow the indexes in the TypeTable to accomodate */
    /* the class identity code given. */
    /**/
    private void expandToInclude (int theKindCode) {
        boolean expandClasses = (classObjectToIndex != null);
        if (theKindCode >= indexToClassName.size()) {
            indexToClassName.setSize(theKindCode * 2);
            if (expandClasses && (indexToClassObject != null))
                indexToClassObject.setSize(theKindCode * 2);
        }
    }

    public int indexForClass (Class theClass) {
        if (classObjectToIndex == null)
            return(classNameToIndex.get(theClass.getName()));
        else
            return(classObjectToIndex.get(theClass));
    }

    public int indexForClassName (String theClassName) {
        return(classNameToIndex.get(theClassName));
    }

    public int registerClass (String classStr, Class theClass) {
        if (classNameToIndex.containsKey(classStr))
            return(classNameToIndex.get(classStr));

        highestCodeUsed++;

        if (itr.debug && Trace.ON) System.out.println("Typetable outgoing: " + classStr);

        classNameToIndex.put(classStr, highestCodeUsed);
        expandToInclude(highestCodeUsed);
        indexToClassName.setElementAt(classStr, highestCodeUsed);

        if (classObjectToIndex != null) {
            if (theClass == null) {
                try {
                    theClass = ClassCache.forName(classStr);
                } catch (Exception e) {
                    tr.errorReportException(e, "Couldn't get class " + classStr +
                                       " to register in typetable");
                } catch (IllegalAccessError e) {
                    tr.errorReportException(e, "Couldn't get class " +
                        classStr + " to register in typetable");
                }
            }
            if (theClass == null)  {
                return(0);
            }
            indexToClassObject.setElementAt(theClass, highestCodeUsed);
            classObjectToIndex.put(theClass, highestCodeUsed);
        }

        return(highestCodeUsed);
    }

    /**/
    /* Return requested class object for the given index number. */
    /**/
    public Class classForIndex (int index) {
        if (tr.tracing)
            tr.$("Getting class for index " + index);
        if ((indexToClassObject == null) ||
                (index >= indexToClassObject.size())) {
            if (tr.tracing) {
                if (indexToClassObject == null) {
                    tr.$("No indexToClassObject vector");
                } else {
                    tr.$("Index " + index + " is beyond size " +
                         indexToClassObject.size());
                }
            }
            return(null);
        }
        return((Class) indexToClassObject.elementAt(index));
    }

    /**/
    /* Return the name for the index given. */
    /**/
    public String classNameForIndex (int index) {
        if (index >= indexToClassName.size())
            return(null);
        return((String) indexToClassName.elementAt(index));
    }

    /**/
    /* The name of the class we want to encode */
    /**/
    static String ClassName = null;
    public String classNameToEncode (RtEncoder encoder) {
        if (ClassName == null) {
            ClassName = this.getClass().getName();
        }
        return(ClassName);
    }

    /**/
    /* Store TypeTable contents to the encoder given. */
    /**/
    public void encode (RtEncoder toStream) {
        try {
            toStream.writeInt(highestCodeUsed); /* Store number of classes. */
            int i;
            for (i = 1; i <= highestCodeUsed; i++) {
                String thisName = (String) indexToClassName.elementAt(i);
                toStream.writeUTF(thisName);
            }
        } catch (Exception e) {
            tr.errorReportException(e, "Exception encoding TypeTable");
        }
    }

    public void registerClassesInTable () {
        int size = indexToClassName.size();
        if (indexToClassObject != null) {
            if (tr.tracing)
                tr.$("Already have indexToClassObject");
        } else {
            if (tr.tracing) {
                tr.$("Making indexToClassObject for size " + size);
                tr.$("Making classObjectToIndex for size " +
                     (highestCodeUsed + 1));
            }
            classObjectToIndex = new ObjKeyTable(0, highestCodeUsed + 1, true);
            indexToClassObject = new Vector(size);
        }

        indexToClassObject.setSize(size);

        /* Using a while loop here to try and get all the classes, even if */
        /* one can't get found - this is to avoid constantly setting up an */
        /* exception context for every iteration through the for loop. */
        int i = 1;
        boolean keepGoing = true;
        while (keepGoing) {
            String className = null;
            try {
                for (; i <= highestCodeUsed; i++) {
                    if (tr.tracing)
                        tr.$("Registering class for index " + i);
                    className = (String)indexToClassName.elementAt(i);
                    if (tr.tracing)
                        tr.$("Class name for index " + i + " is " + className);
                    Class theClass = ClassCache.forName(className);
                    if (tr.tracing)
                        tr.$("Class for index " + i + " is " + theClass);
                    classObjectToIndex.put(theClass, i);
                    indexToClassObject.setElementAt(theClass, i);
                }
                keepGoing = false;
            } catch (Exception e) {
                tr.$("Can't find class " + className + " for element " + i);
                i++;
            }
        }
    }

    /**/
    /* Load TypeTable contents from the decoder provided. */
    /**/
    public Object decode (RtDecoder fromStream) {
        try {
            int i;
            int size = fromStream.readInt();
            initializeTypeTable(true, size + 1);
            highestCodeUsed = size;
            for (i = 1; i <= highestCodeUsed; i++) {
                String className = fromStream.readUTF();
                Class theClass = ClassCache.forName(className);
                classNameToIndex.put(className, i);
                classObjectToIndex.put(theClass, i);
                indexToClassName.setElementAt(className, i);
                indexToClassObject.setElementAt(theClass, i);
            }
        } catch (Exception e) {
            tr.errorReportException(e, "Trouble reading Typetable");
        }
        return(this);
    }
}
