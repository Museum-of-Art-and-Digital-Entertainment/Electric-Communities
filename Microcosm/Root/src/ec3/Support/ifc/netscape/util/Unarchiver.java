// Unarchiver.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

import java.io.*;

/** Object subclass implementing the Decoder interface to decode a graph of
  * objects from an Archive. The following example demonstrates how to use
  * an Unarchiver to retrieve the first root object from an Archive read from
  * System.in:
  *
  * <pre>
  *    archive = new Archive();
  *    archive.readASCII(System.in)
  *    rootIdentifiers = archive.rootIdentifiers();
  *    unarchiver = new Unarchiver(archive);
  *    rootObject = unarchiver.unarchiveIdentifier(rootIdentifiers[0]);
  * </pre>
  *
  * @see Decoder
  * @see Archive
  * @see Archiver
  */
public class Unarchiver implements Decoder {
    Archive archive;
    ArchivingStack stack = new ArchivingStack();

    Object objectForId[];
    boolean referenceGivenOut[];

    int unarchivedCount;
    Object unarchivedObjects[];
    ExternalCoder unarchivedCoders[];

    Object currentObject;
    ClassTable currentTable;
    int currentId;
    int currentColumnCount;
    int currentRow;
    int currentColumn;

    // ALERT!  These are here to handle the case where the Unarchiver has a
    // null ClassLoader and is trying to unarchive classes from a non-null
    // ClassLoader.

    netscape.application.FoundationApplet applet;
    boolean appletInitialized;

    /** Primitive constructor creating an Unarchiver that retrieves objects
      * from <b>archive</b>. Do not mutate the Archive while using the
      * Unarchiver.
      */
    public Unarchiver(Archive archive) {
        super();

        this.archive = archive;
    }

    /** Returns the archive from which the Unarchiver decodes objects.
      */
    public Archive archive() {
        return archive;
    }

    /** A convenience method for reading an object from a stream.
      * Equivilent to the code:
      * <pre>
      *     archive = new Archive();
      *     archive.read(in);
      *     unarchiver = new Unarchiver(archive);
      *
      *     rootIds = archive.rootIdentifiers();
      *     if (rootIds == null || rootIds.length == 0)
      *         return null;
      *
      *     return unarchiver.unarchiveIdentifier(rootIds[0]);
      * </pre>
      */
    public static Object readObject(InputStream inputStream) throws
                                                            IOException,
        CodingException {
        Archive archive;
        Unarchiver unarchiver;
        int rootIds[];

        archive = new Archive();
        archive.read(inputStream);
        unarchiver = new Unarchiver(archive);

        rootIds = archive.rootIdentifiers();
        if (rootIds == null || rootIds.length == 0)
            return null;

        return unarchiver.unarchiveIdentifier(rootIds[0]);
    }

    /** Unarchives a graph of objects starting from the object identified by
      * <b>identifier</b> in an archive. That object, and all objects it
      * references, will be reconstructed by calling their empty constructor
      * followed by <b>decode()</b>.
      * @see Archive#rootIdentifiers
      * @see Codable
      * @see Decoder
      */
    public Object unarchiveIdentifier(int identifier) throws CodingException {
        Object rootObject;

        if (identifier == 0)
            return null;

        // We make these arrays big enough to map all the ids in the archive.

        if (objectForId == null) {
            referenceGivenOut = new boolean[archive.identifierCount()];
            objectForId = new Object[referenceGivenOut.length];
            unarchivedObjects = new Object[referenceGivenOut.length];
            unarchivedCoders = new ExternalCoder[referenceGivenOut.length];
        }

        // Make sure that this is empty.  It will be filled up by
        // objectForId() as it unarchives objects.

        clearFinishList();

        try {
            // Pull out the root object.  This will cause all the objects it
            // references, and the ones they reference, etc.  to be
            // unarchived.

            rootObject = objectForIdentifier(identifier);

            // Make a pass over all the newly unarchived objects and give them
            // a chance to do some post unarchiving clean up now that their
            // all their brethren have been created.

            processFinishList();
        } finally {
            // No matter what happens, be sure we clean up after ourselves.

            clearFinishList();
        }

        return rootObject;
    }

    // ALERT!  This is the only dependency on application in util.  There
    // should be a general solution to the Class.forName() problem with null
    // ClassLoaders.

    /** Called to look up a class from the Archive by name.
      */
    protected Class classForName(String className) throws CodingException {
        Class cls = null;

        if (!appletInitialized) {
            applet = (netscape.application.FoundationApplet)
                netscape.application.AWTCompatibility.awtApplet();
            appletInitialized = true;
        }

        try {
            if (applet != null)
                cls = applet.classForName(className);
            else
                cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            creationException(e.toString(), className);
        } catch (NoSuchMethodError e) {
            creationException(e.toString(), className);
        }

        return cls;
    }

    private Object newInstance(String className) throws CodingException {
        Class archivedClass;
        Object object = null;

        archivedClass = classForName(className);

        try {
            object = archivedClass.newInstance();
        } catch (InstantiationException e) {
            creationException(e.toString(), className);
        } catch (IllegalAccessException e) {
            creationException(e.toString(), className);
        } catch (NoSuchMethodError e) {
            creationException(e.toString(), className);
        }

        return object;
    }

    /** Whenever we have a problem making an object from the Archive, we
      * try to throw a helpful message.
      */
    private void creationException(String baseException, String className)
        throws CodingException {
        throw new CodingException(baseException + ".  Class " + className +
            " must be public and define a constructor taking no arguments.");
    }

    // All ids > 0 map to non-null objects.  id = 0 maps to null.

    private Object objectForIdentifier(int id) throws CodingException {
        ExternalCoder coder;
        Object object;
        ClassTable table;
        String className;
        ClassInfo info;

        // This works and won't go off the end of the array because we made
        // the objectForId array big enough to hold all the objects in the
        // archive.

        object = objectForId[id];
        if (object != null) {
            // If the object is not null, then we have given a reference out
            // to someone.  We keep this state to detect replacement cycles.

            referenceGivenOut[id] = true;
            return object;
        } else if (id == 0)
            return null;

        // We haven't seen this id before, so we are going to have to make a
        // new instance.

        table = archive.classTableForIdentifier(id);
        className = table.className();

        // We always check to see if there is an external coder for a given
        // class name.

        coder = archive.externalCoderForName(className);
        if (coder != null) {
            object = coder.newInstance(className);
        } else {
            object = newInstance(className);
        }

        // To make unarchiving go fast, we stuff the ClassTable's fieldNames
        // with the exact Strings the class is using so that pointer
        // comparisons can be used to early-out equality tests.

        if (!table.hasUniqueStrings()) {
            info = new ClassInfo(className);

            if (coder != null)
                coder.describeClassInfo(object, info);
            else
                ((Codable)object).describeClassInfo(info);

            table.uniqueStrings(info);
        }

        // Put the original object in the list to get finishDecoding() later.
        // If the object was replaced, the new object should not get
        // finishDecoding() since it was not unarchived.  The original object
        // can always forward finishDecoding() if appropriate.

        addToFinishList(coder, object);

        // This is where we decode the newly created object.  Be careful to
        // read the object back out of the table in case it was replaced
        // during the call to decode().

        objectForId[id] = object;
        pushUnarchivingState(object, id);

        if (coder != null)
            coder.decode(object, this);
        else
            ((Codable)object).decode(this);

        popUnarchivingState();
        return objectForId[id];
    }

    /** This method pushes any current unarchiving state onto the stack and
      * sets up to begin unarchiving the given object. The pushed state
      * will be restored in popUnarchivingState().
      */
    private void pushUnarchivingState(Object object, int id) {
        stack.pushUnarchiver(this);

        currentObject = object;
        currentTable = archive.classTableForIdentifier(id);
        currentId = id;
        currentRow = archive.rowForIdentifier(id);
        currentColumn = -1;
        currentColumnCount = currentTable.fieldCount;
    }

    /** This method pops the stack of archiving states. The current object is
      * picked up where we left off.
      */
    private void popUnarchivingState() {
        stack.popUnarchiver(this);
    }

    /** This maintains the list of objects which have been unarchived in
      * this session.
      */
    private void addToFinishList(ExternalCoder coder, Object object) {
        unarchivedCoders[unarchivedCount] = coder;
        unarchivedObjects[unarchivedCount] = object;
        unarchivedCount++;
    }

    private void processFinishList() throws CodingException {
        int i, count;
        ExternalCoder coder;
        Object object;

        count = unarchivedCount;
        for (i = 0; i < count; i++) {
            coder = unarchivedCoders[i];
            object = unarchivedObjects[i];

            if (coder != null)
                coder.finishDecoding(object);
            else
                ((Codable)object).finishDecoding();

            // Clear things out while we're here.  If an exception gets
            // thrown, clearFinishList() will be called anyway.

            unarchivedCoders[i] = null;
            unarchivedObjects[i] = null;
        }

        unarchivedCount = 0;
    }

    private void clearFinishList() {
        int i, count;

        count = unarchivedCount;
        for (i = 0; i < count; i++) {
            unarchivedCoders[i] = null;
            unarchivedObjects[i] = null;
        }

        unarchivedCount = 0;
    }

    /** This method is called at the beginning of each unarchive... method
      * to make sure that currentColumn matches the given key. In
      * general, the keys will be in the same order as the columns so the
      * pointer equality test will succeed and we'll rip right along.
      */
    private void prepareToUnarchiveField(String key) throws CodingException {
        int i, count;
        String fieldNames[];

        count = currentColumnCount;
        fieldNames = currentTable.fieldNames;

        // Scan forward looking for a matching column.  It is kind of common
        // to omit fields when archiving/unarchiving, so skipping forward a
        // few after a miss will usually get us back on track.

        for (i = currentColumn + 1; i < count; i++) {
            if (key == fieldNames[i]) {
                currentColumn = i;
                return;
            }
        }

        // Our optimism has not paid off.  Go ask the ClassTable for
        // the column.

        currentColumn = currentTable.columnForField(key);
        if (currentColumn < 0) {
            throw new CodingException("Unknown field name: " + key);
        }
    }

    /** Decoder interface method that returns the version information for the
      * class named <b>className</b>.  Objects can use this information to
      * bring forward old encodings at runtime.
      */
    public int versionForClassName(String className) throws CodingException {
        return currentTable.versionForClassName(className);
    }

    /** Decoder interface method that decodes the boolean value associated
      * with the string <b>key</b>.
      */
    public boolean decodeBoolean(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.booleanAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the boolean array associated
      * with the string <b>key</b>.
      */
    public boolean[] decodeBooleanArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.booleanArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the character value associated
      * with the string <b>key</b>.
      */
    public char decodeChar(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.charAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the character array associated
      * with the string <b>key</b>.
      */
    public char[] decodeCharArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.charArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the byte value associated with
      * the string <b>key</b>.
      */
    public byte decodeByte(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.byteAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the byte array associated with
      * the string <b>key</b>.
      */
    public byte[] decodeByteArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.byteArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the short value associated with
      * the string <b>key</b>.
      */
    public short decodeShort(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.shortAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the short array associated with
      * the string <b>key</b>.
      */
    public short[] decodeShortArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.shortArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the integer value associated
      * with the string <b>key</b>.
      */
    public int decodeInt(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.intAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the integer array associated
      * with the string <b>key</b>.
      */
    public int[] decodeIntArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.intArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the long value associated with
      * the string <b>key</b>.
      */
    public long decodeLong(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.longAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the long array value associated
      * with the string <b>key</b>.
      */
    public long[] decodeLongArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.longArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the float value associated with
      * the string <b>key</b>.
      */
    public float decodeFloat(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.floatAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the float array associated with
      * the string <b>key</b>.
      */
    public float[] decodeFloatArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.floatArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the double value associated with
      * the string <b>key</b>.
      */
    public double decodeDouble(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.doubleAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the double array associated with
      * the string <b>key</b>.
      */
    public double[] decodeDoubleArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.doubleArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the string value associated with
      * the string <b>key</b>.
      */
    public String decodeString(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.stringAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes the string array associated with
      * the string <b>key</b>.
      */
    public String[] decodeStringArray(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return currentTable.stringArrayAt(currentRow, currentColumn);
    }

    /** Decoder interface method that decodes a reference to another Codable
      * object.
      */
    public Object decodeObject(String key) throws CodingException {
        prepareToUnarchiveField(key);
        return objectForIdentifier(currentTable.identifierAt(currentRow,
            currentColumn));
    }

    /** Decoder interface method that decodes an array of Codable objects. The
      * references to the Codable objects are shared, but the reference to the
      * array is not.
      */
    public Object[] decodeObjectArray(String key) throws CodingException {
        int i;
        int ids[];
        Object objects[];

        prepareToUnarchiveField(key);
        ids = currentTable.identifierArrayAt(currentRow, currentColumn);
        if (ids == null) {
            return null;
        }

        objects = new Object[ids.length];

        for (i = 0; i < ids.length; i++)
            objects[i] = objectForIdentifier(ids[i]);

        return objects;
    }

    /** Decoder interface method that replaces references to the object
      * currently being decoded with <b>replacement</b>. This method throws a
      * CodingException when an attempt is made to replace an object which has
      * already been seen by other objects. For maximum safety, this method
      * should only be called from leaves of the object graph.
      */
    public void replaceObject(Object replacement) throws CodingException {
        // Need to print out interesting debugging information here.  ALERT!
        if (referenceGivenOut[currentId]) {
            throw new CodingException("Circular replacement exception");
        }

        objectForId[currentId] = replacement;
    }
}
