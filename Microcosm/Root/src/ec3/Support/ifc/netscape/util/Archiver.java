// Archiver.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

import java.io.*;

/** Object subclass implementing the Encoder interface to encode a graph of
  * objects to an Archive. The following example demonstrates how to use an
  * Archiver to write a graph of objects, starting from rootObject to the
  * stream System.out:
  *
  * <pre>
  *    archiver = new Archiver(new Archive());
  *    archiver.archiveRootObject(rootObject);
  *    archiver.archive().writeASCII(System.out, true);
  * </pre>
  *
  * @see Encoder
  * @see Archive
  */
public class Archiver implements Encoder {
    Archive archive;
    ArchivingStack stack = new ArchivingStack();
    IdHashtable idHash = new IdHashtable(false);

    Object currentObject;
    ClassTable currentTable;
    int currentId;
    int currentColumnCount;
    int currentRow;
    int currentColumn;

    /** Constructs an Archiver that writes to <b>archive</b>.
      */
    public Archiver(Archive archive) {
        super();
        this.archive = archive;
    }

    /** Returns the archive used by the Archiver.
      */
    public Archive archive() {
        return archive;
    }

    /** A convenience method for writing an object to a stream.  Equivilent to:
      * <pre>
      *    archive = new Archive();
      *    archiver = new Archiver(archive);
      *    archiver.archiveRootObject(root);
      *    archive.write(out);
      * </pre>
      */
    public static void writeObject(OutputStream outputStream, Object root)
        throws IOException, CodingException {
        Archive archive;
        Archiver archiver;

        archive = new Archive();
        archiver = new Archiver(archive);
        archiver.archiveRootObject(root);
        archive.write(outputStream);
    }

    /** Starts the archiving process. This method can be called multiple
      * times to encode more than one graph (which may or may not overlap)
      * into an Archive. This automatically adds the object to the Archive's
      * array of root identifiers.
      */
    public void archiveRootObject(Object root) throws CodingException {
        int rootId;

        // This will cause the object to be archived if we haven't seen it yet.

        rootId = identifierForObject(root);
        archive.addRootIdentifier(rootId);
    }

    /** Checks whether the archiver has seen this object before.
      * If not, it gets a new identifier for the object, pushes the current
      * archiving state, and archives the new object.
      */
    private int identifierForObject(Object object) throws CodingException {
        int id;
        ClassTable table;
        ClassInfo info;
        ExternalCoder coder;
        String className;

        if (object == null)
            return 0;

        id = idHash.get(object);
        if (id != IdHashtable.NOT_FOUND)
            return id;

        // If we have never seen this object before, then we need to create
        // an identifier for it by inserting a row in the appropriate class
        // table.

        className = object.getClass().getName();
        coder = archive.externalCoderForName(className);
        table = archive.classTableForName(className);

        if (table == null) {
            info = new ClassInfo(className);

            if (coder != null)
                coder.describeClassInfo(object, info);
            else
                ((Codable)object).describeClassInfo(info);

            table = new ClassTable(archive, info);
            table.setUniqueStrings(true);
            archive.addClassTable(table);
        }

        id = table.newIdentifier();
        idHash.putKnownAbsent(object, id);

        // We archive depth first, so set things up and archive this puppy.

        pushArchivingState(object, id);

        if (coder != null)
            coder.encode(object, this);
        else
            ((Codable)object).encode(this);

        popArchivingState();

        return id;
    }

    /** This method pushes any current archiving state onto the stack and sets
      * up to begin archiving the given object. The pushed state will be
      * restored in popArchivingState().
      */
    private void pushArchivingState(Object object, int id) {
        stack.pushArchiver(this);

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
    private void popArchivingState() {
        stack.popArchiver(this);
    }

    /** This method is called at the beginning of each archive... method
      * to make sure that currentColumn matches the given key. In general,
      * the keys will be in the same order as the columns so the pointer
      * equality test will succeed and we'll rip right along.
      */
    private void prepareToArchiveField(String key) throws CodingException {
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

    /** Encoder interface method that encodes the boolean <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeBoolean(String key, boolean value)
        throws CodingException {

        prepareToArchiveField(key);
        currentTable.setBooleanAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the boolean array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeBooleanArray(String key, boolean value[], int offset,
        int length) throws CodingException {
        boolean copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new boolean[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setBooleanArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes the character <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeChar(String key, char value) throws CodingException {
        prepareToArchiveField(key);
        currentTable.setCharAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the character array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeCharArray(String key, char value[], int offset,
        int length) throws CodingException {
        char copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new char[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setCharArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes the byte <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeByte(String key, byte value) throws CodingException {
        prepareToArchiveField(key);
        currentTable.setByteAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the byte array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeByteArray(String key, byte value[], int offset,
        int length) throws CodingException {
        byte copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new byte[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setByteArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes the short <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeShort(String key, short value) throws CodingException {
        prepareToArchiveField(key);
        currentTable.setShortAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the short array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeShortArray(String key, short value[], int offset,
        int length) throws CodingException {
        short copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new short[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setShortArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes the integer <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeInt(String key, int value) throws CodingException {
        prepareToArchiveField(key);
        currentTable.setIntAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the integer array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeIntArray(String key, int value[], int offset,
        int length) throws CodingException {
        int copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new int[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setIntArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes the long <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeLong(String key, long value) throws CodingException {
        prepareToArchiveField(key);
        currentTable.setLongAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the long array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeLongArray(String key, long value[], int offset,
        int length) throws CodingException {
        long copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new long[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setLongArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes the float <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeFloat(String key, float value) throws CodingException {
        prepareToArchiveField(key);
        currentTable.setFloatAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the float array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeFloatArray(String key, float value[], int offset,
        int length) throws CodingException {
        float copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new float[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setFloatArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes the double <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeDouble(String key, double value) throws CodingException {
        prepareToArchiveField(key);
        currentTable.setDoubleAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the double array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeDoubleArray(String key, double value[], int offset,
        int length) throws CodingException {
        double copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new double[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setDoubleArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes the string <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeString(String key, String value) throws CodingException {
        prepareToArchiveField(key);
        currentTable.setStringAt(currentRow, currentColumn, value);
    }

    /** Encoder interface method that encodes the string array <b>value</b>,
      * associating it with the string <b>key</b>.
      */
    public void encodeStringArray(String key, String value[], int offset,
        int length) throws CodingException {
        String copy[] = null;

        prepareToArchiveField(key);
        if (value != null) {
            copy = new String[length];
            System.arraycopy(value, offset, copy, 0, length);
        }

        currentTable.setStringArrayAt(currentRow, currentColumn, copy);
    }

    /** Encoder interface method that encodes a reference to another Codable
      * object. If multiple objects reference the same object and each passes
      * it to <b>encodeObject()</b>, only one copy of that object is actually
      * encoded.
      */
    public void encodeObject(String key, Object value)
        throws CodingException {
        prepareToArchiveField(key);
        currentTable.setIdentifierAt(currentRow, currentColumn,
            identifierForObject(value));
    }

    /** Encoder interface method that encodes an array of Codable objects. The
      * reference to the array is not shared, but references to the objects in
      * the array are.
      */
    public void encodeObjectArray(String key, Object value[], int offset,
        int length) throws CodingException {
        int i, ids[];

        prepareToArchiveField(key);

        if (value == null) {
            currentTable.setIdentifierArrayAt(currentRow, currentColumn, null);
            return;
        }

        ids = new int[length];

        for (i = offset; i < length; i++) {
            ids[i] = identifierForObject(value[i]);
        }

        currentTable.setIdentifierArrayAt(currentRow, currentColumn, ids);
    }
}
