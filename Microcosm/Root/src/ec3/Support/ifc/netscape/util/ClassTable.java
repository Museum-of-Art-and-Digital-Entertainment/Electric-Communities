// ClassTable.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

import java.io.*;

/** Object subclass used by an Archive to store the raw data for its encoded
  * objects. Each class in an Archive has a ClassTable that holds the data for
  * instances of that class. The data in a ClassTable is accessed by row and
  * column index, with a row corresponding to an instance of the class and a
  * column corresponding to a field of that class. Field names can be mapped to
  * column indexes through <b>ClassTable.columnForField()</b>; object
  * identifiers can be mapped to row indexes through
  * <b>ClassTable.rowForIdentifier()</b>. New rows are created through
  * <b>ClassTable.newIdentifier()</b>. Only programs that directly manipulate
  * archive data must deal with ClassTables.
  *
  * @see Archive
  * @see ClassInfo
  */
public class ClassTable {
    Archive archive;
    String className;

    int classCount;
    String classNames[];
    int classVersions[];

    int fieldCount;
    byte fieldTypes[];
    String fieldNames[];
    IdHashtable columnForField;

    int rowCapacity;
    int rowCount;
    Object fieldColumns[];

    // This is some scratch space used by Archive.read() and Archive.write().
    int tableId;

    // This is some scratch space used by the Archiver/Unarchiver.  It is set
    // to true when the Strings in the fieldNames array came directly from
    // describeClassInfo() so that the pointers will be the same.  This is
    // used to speed up Unarchiving.
    boolean uniqueStrings;

    /** Constructs a new ClassTable for a given Archive. The ClassTable
      * must be added to the archive by calling <b>Archive.addClassTable()</b>
      * once the ClassTable's class name has been set. This constructor is only
      * useful when reading a ClassTable from a stream.
      * @see #readInfo
      * @see #readData
      * @see Archive#addClassTable
      */
    public ClassTable(Archive archive) {
        super();
        this.archive = archive;
    }

    /** Constructs a new ClassTable for the Archive <b>archive</b>. The
      * ClassTable must be added to the Archive by calling
      * <b>Archive.addClassTable()</b>. <b>classInfo</b> helps configure the
      * ClassTable.
      * @see Archive#addClassTable
      */
    public ClassTable(Archive archive, ClassInfo classInfo) {
        this(archive);

        className = classInfo.className();

        classCount = classInfo.classCount();
        classNames = classInfo.classNames();
        classVersions = classInfo.classVersions();

        fieldCount = classInfo.fieldCount();
        fieldTypes = classInfo.fieldTypes();
        fieldNames = classInfo.fieldNames();

        fieldColumns = new Object[fieldCount];
    }

    /** Returns the archive to which this ClassTable belongs.
      */
    public Archive archive() {
        return archive;
    }

    /** Returns the name of the class this ClassTable represents.
      */
    public String className() {
        return className;
    }

    /** Returns the chain of superclasses recorded in this ClassTable.
      */
    public String[] classNames() {
        return classNames;
    }

    /** Returns the version for a given class name.
      */
    public int versionForClassName(String className) {
        int i, count;

        count = classCount;
        for (i = 0; i < count; i++) {
            if (classNames[i].equals(className))
                return classVersions[i];
        }

        return 0;
    }

    /** Returns the number of rows in the ClassTable.
      */
    public int rowCount() {
        return rowCount;
    }

    private Object newColumn(int fieldType, int length) {
        switch (fieldType) {
            case Codable.BOOLEAN_TYPE:       return new boolean[length];
            case Codable.BOOLEAN_ARRAY_TYPE: return new boolean[length][];
            case Codable.CHAR_TYPE:          return new char[length];
            case Codable.CHAR_ARRAY_TYPE:    return new char[length][];
            case Codable.BYTE_TYPE:          return new byte[length];
            case Codable.BYTE_ARRAY_TYPE:    return new byte[length][];
            case Codable.SHORT_TYPE:         return new short[length];
            case Codable.SHORT_ARRAY_TYPE:   return new short[length][];
            case Codable.INT_TYPE:           return new int[length];
            case Codable.INT_ARRAY_TYPE:     return new int[length][];
            case Codable.LONG_TYPE:          return new long[length];
            case Codable.LONG_ARRAY_TYPE:    return new long[length][];
            case Codable.FLOAT_TYPE:         return new float[length];
            case Codable.FLOAT_ARRAY_TYPE:   return new float[length][];
            case Codable.DOUBLE_TYPE:        return new double[length];
            case Codable.DOUBLE_ARRAY_TYPE:  return new double[length][];
            case Codable.STRING_TYPE:        return new String[length];
            case Codable.STRING_ARRAY_TYPE:  return new String[length][];
            case Codable.OBJECT_TYPE:        return new int[length];
            case Codable.OBJECT_ARRAY_TYPE:  return new int[length][];
            default:
                throw new InconsistencyException("Unknown field type: " +
                    fieldType);
        }
    }

    private void ensureCapacity(int newCapacity) {
        int i;
        Object newColumn;

        if (newCapacity <= rowCapacity)
            return;

        for (i = 0; i < fieldCount; i++) {
            newColumn = newColumn(fieldTypes[i], newCapacity);

            if (rowCapacity > 0)
                System.arraycopy(fieldColumns[i], 0, newColumn, 0,
                                 rowCapacity);

            fieldColumns[i] = newColumn;
        }

        rowCapacity = newCapacity;
    }

    private void growCapacity() {
        int newCapacity;

        if (rowCapacity == 0)
            newCapacity = 8;
        else
            newCapacity = 2 * rowCapacity;

        ensureCapacity(newCapacity);
    }

    /** Creates a new row in the ClassTable and a new identifier for the
      * archive, returning the new identifier. The new row can be discovered
      * by calling <b>rowForIdentifier()</b>.
      */
    public int newIdentifier() {
        int id;

        if (rowCount >= rowCapacity)
            growCapacity();

        id = archive.mapIdentifier(this, rowCount);
        rowCount++;

        return id;
    }

    /** Returns the row index for a given object identifier.
      */
    public int rowForIdentifier(int id) {
        return archive.rowForIdentifier(id);
    }

    /** Returns the column index for the given field name.
      */
    public int columnForField(String key) {
        int i;

        if (archive.performanceDebug) {
            System.err.println("*** Field " + key + " of class " +
                className + " was accessed out of order.");
        }

        if (columnForField == null) {
            columnForField = new IdHashtable(true);

            // We have to add one to the column because the NOT_FOUND
            // constant is 0.  ALERT!

            for (i = 0; i < fieldCount; i++) {
                columnForField.putKnownAbsent(fieldNames[i], i + 1);
            }
        }

        i = columnForField.get(key);

        return i - 1;
    }

    /** This calls describeClassInfo() on the object and replaces equals
      * Strings in the fieldNames array. This method is very opportunistic
      * and bails out at the first sign of trouble.
      */
    void uniqueStrings(ClassInfo info) {
        int i, count;
        String src[], dst[];

        // This is set to true even if we fail.  We don't want to keep
        // trying.

        setUniqueStrings(true);

        if (info.fieldCount != fieldCount) {
            if (archive.performanceDebug) {
                System.err.println("Failed to unique Strings in class " +
                    info.className());
            }
            return;
        }

        // Blow away the columnForField cache so it will benefit too.

        columnForField = null;

        src = info.fieldNames;
        dst = fieldNames;
        count = fieldCount;

        for (i = 0; i < count; i++) {
            if (dst[i].equals(src[i]))
                dst[i] = src[i];
            else {
                if (archive.performanceDebug) {
                    System.err.println("Failed to unique Strings in class " +
                        info.className());
                }
                return;
            }
        }

        // Do the same thing for the classNames.

        if (info.classCount != classCount)
            return;

        src = info.classNames;
        dst = classNames;
        count = classCount;

        for (i = 0; i < count; i++) {
            if (dst[i].equals(src[i]))
                dst[i] = src[i];
            else
                return;
        }
    }

    void setUniqueStrings(boolean flag) {
        uniqueStrings = flag;
    }

    boolean hasUniqueStrings() {
        return uniqueStrings;
    }

    /** Returns the boolean value located at <b>row</b>, <b>column</b>.
      * @see #setBooleanAt
      */
    public boolean booleanAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((boolean[])fieldColumns[column])[row];
    }

    /** Sets the boolean value at <b>row</b>, <b>column</b>.
      */
    public void setBooleanAt(int row, int column, boolean value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((boolean[])fieldColumns[column])[row] = value;
    }

    /** Returns the boolean array located at <b>row</b>, <b>column</b>.
      * @see #setBooleanArrayAt
      */
    public boolean[] booleanArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((boolean[][])fieldColumns[column])[row];
    }

    /** Sets the boolean array at <b>row</b>, <b>column</b>.
      */
    public void setBooleanArrayAt(int row, int column, boolean value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((boolean[][])fieldColumns[column])[row] = value;
    }

    /** Returns the character value located at <b>row</b>, <b>column</b>.
      * @see #setCharAt
      */
    public char charAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((char[])fieldColumns[column])[row];
    }

    /** Sets the character value at <b>row</b>, <b>column</b>.
      */
    public void setCharAt(int row, int column, char value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((char[])fieldColumns[column])[row] = value;
    }

    /** Returns the character array located at <b>row</b>, <b>column</b>.
      * @see #setCharArrayAt
      */
    public char[] charArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((char[][])fieldColumns[column])[row];
    }

    /** Sets the character array at <b>row</b>, <b>column</b>.
      */
    public void setCharArrayAt(int row, int column, char value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((char[][])fieldColumns[column])[row] = value;
    }

    /** Returns the byte value located at <b>row</b>, <b>column</b>.
      * @see #@setByteAt
      */
    public byte byteAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((byte[])fieldColumns[column])[row];
    }

    /** Sets the byte value at <b>row</b>, <b>column</b>.
      */
    public void setByteAt(int row, int column, byte value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((byte[])fieldColumns[column])[row] = value;
    }

    /** Returns the byte array located at <b>row</b>, <b>column</b>.
      * @see #setByteArrayAt
      */
    public byte[] byteArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((byte[][])fieldColumns[column])[row];
    }

    /** Sets the byte array located at <b>row</b>, <b>column</b>.
      */
    public void setByteArrayAt(int row, int column, byte value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((byte[][])fieldColumns[column])[row] = value;
    }

    /** Returns the short value located at <b>row</b>, <b>column</b>.
      * @see #setShortAt
      */
    public short shortAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((short[])fieldColumns[column])[row];
    }

    /** Sets the short value at <b>row</b>, <b>column</b>.
      */
    public void setShortAt(int row, int column, short value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((short[])fieldColumns[column])[row] = value;
    }

    /** Returns the short array located at <b>row</b>, <b>column</b>.
      * @see #setShortArrayAt
      */
    public short[] shortArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((short[][])fieldColumns[column])[row];
    }

    /** Sets the short array at <b>row</b>, <b>column</b>.
      */
    public void setShortArrayAt(int row, int column, short value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((short[][])fieldColumns[column])[row] = value;
    }

    /** Returns the integer value located at <b>row</b>, <b>column</b>.
      * @see #setIntegerAt
      */
    public int intAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((int[])fieldColumns[column])[row];
    }

    /** Sets the integer value located at <b>row</b>, <b>column</b>.
      */
    public void setIntAt(int row, int column, int value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((int[])fieldColumns[column])[row] = value;
    }

    /** Returns the integer array located at <b>row</b>, <b>column</b>.
      * @see #setIntegerArrayAt
      */
    public int[] intArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((int[][])fieldColumns[column])[row];
    }

    /** Sets the integer array located at <b>row</b>, <b>column</b>.
      */
    public void setIntArrayAt(int row, int column, int value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((int[][])fieldColumns[column])[row] = value;
    }

    /** Returns the long value located at <b>row</b>, <b>column</b>.
      * @see #setLongAt
      */
    public long longAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((long[])fieldColumns[column])[row];
    }

    /** Sets the long value at <b>row</b>, <b>column</b>.
      */
    public void setLongAt(int row, int column, long value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((long[])fieldColumns[column])[row] = value;
    }

    /** Returns the long array located at <b>row</b>, <b>column</b>.
      * @see #setLongArrayAt
      */
    public long[] longArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((long[][])fieldColumns[column])[row];
    }

    /** Sets the long array at <b>row</b>, <b>column</b>.
      */
    public void setLongArrayAt(int row, int column, long value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((long[][])fieldColumns[column])[row] = value;
    }

    /** Returns the float value located at <b>row</b>, <b>column</b>.
      * @see #setFloatAt
      */
    public float floatAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((float[])fieldColumns[column])[row];
    }

    /** Sets the float value at <b>row</b>, <b>column</b>.
      */
    public void setFloatAt(int row, int column, float value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((float[])fieldColumns[column])[row] = value;
    }

    /** Returns the float array located at <b>row</b>, <b>column</b>.
      * @see #setFloatArrayAt
      */
    public float[] floatArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((float[][])fieldColumns[column])[row];
    }

    /** Sets the float array at <b>row</b>, <b>column</b>.
      */
    public void setFloatArrayAt(int row, int column, float value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((float[][])fieldColumns[column])[row] = value;
    }

    /** Returns the double value located at <b>row</b>, <b>column</b>.
      * @see #setDoubleAt
      */
    public double doubleAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((double[])fieldColumns[column])[row];
    }

    /** Sets the double value at <b>row</b>, <b>column</b>.
      */
    public void setDoubleAt(int row, int column, double value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((double[])fieldColumns[column])[row] = value;
    }

    /** Returns the double array located at <b>row</b>, <b>column</b>.
      * @see #setDoubleArrayAt
      */
    public double[] doubleArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((double[][])fieldColumns[column])[row];
    }

    /** Sets the double array at <b>row</b>, <b>column</b>.
      */
    public void setDoubleArrayAt(int row, int column, double value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((double[][])fieldColumns[column])[row] = value;
    }

    /** Returns the string value located at <b>row</b>, <b>column</b>.
      * @see #setStringAt
      */
    public String stringAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((String[])fieldColumns[column])[row];
    }

    /** Sets the string value at <b>row</b>, <b>column</b>.
      */
    public void setStringAt(int row, int column, String value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((String[])fieldColumns[column])[row] = value;
    }

    /** Returns the string array located at <b>row</b>, <b>column</b>.
      * @see #setStringArrayAt
      */
    public String[] stringArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((String[][])fieldColumns[column])[row];
    }

    /** Sets the string array at <b>row</b>, <b>column</b>.
      */
    public void setStringArrayAt(int row, int column, String value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((String[][])fieldColumns[column])[row] = value;
    }

    /** Returns the identifier at <b>row</b>, <b>column</b>.
      * @see #setIdentifierAt
      */
    public int identifierAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((int[])fieldColumns[column])[row];
    }

    /** Sets the identifier at <b>row</b>, <b>column</b>.
      */
    public void setIdentifierAt(int row, int column, int value) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((int[])fieldColumns[column])[row] = value;
    }

    /** Returns the identifier array at <b>row</b>, <b>column</b>.
      * @see #setIdentifierArrayAt
      */
    public int[] identifierArrayAt(int row, int column) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        return ((int[][])fieldColumns[column])[row];
    }

    /** Sets the identifier array at <b>row</b>, <b>column</b>.
      */
    public void setIdentifierArrayAt(int row, int column, int value[]) {
        if (row >= rowCount)
            throw new ArrayIndexOutOfBoundsException(row);

        ((int[][])fieldColumns[column])[row] = value;
    }

    /** Writes the ClassTable's class information to <b>outputStream</b>.
      */
    public void writeInfo(OutputStream outputStream) throws IOException {
        int i, count;
        CompactOutputStream out;

        if (outputStream instanceof CompactOutputStream)
            out = (CompactOutputStream)outputStream;
        else
            out = new CompactOutputStream(outputStream);

        out.writeCompactUTF(className);

        count = fieldCount;
        out.writeCompactInt(count);

        for (i = 0; i < count; i++) {
            out.writeCompactUTF(fieldNames[i]);
            out.writeByte(fieldTypes[i]);
        }

        count = classCount;
        out.writeCompactInt(count);

        for (i = 0; i < count; i++) {
            out.writeCompactUTF(classNames[i]);
            out.writeCompactInt(classVersions[i]);
        }

        // Leave room for expansion.

        out.writeCompactInt(0);
    }

    /** Writes the ClassTable's instance data to <b>outputStream</b>.
      */
    public void writeData(OutputStream outputStream) throws IOException {
        int i, count;
        Object column;
        CompactOutputStream out;

        if (outputStream instanceof CompactOutputStream)
            out = (CompactOutputStream)outputStream;
        else
            out = new CompactOutputStream(outputStream);

        out.writeCompactInt(rowCount);

        count = fieldCount;

        // Write out all the boolean columns first so they get compacted
        // better.  The boolean arrays don't matter since they do a
        // writeCompactInt() which flushes the boolean buffer.

        for (i = 0; i < count; i++) {
            if (fieldTypes[i] == Codable.BOOLEAN_TYPE)
                writeBooleanColumn(out, (boolean[])fieldColumns[i], rowCount);
        }

        // Write out the rest of the columns.

        for (i = 0; i < count; i++) {
            column = fieldColumns[i];

            switch (fieldTypes[i]) {
                case Codable.BOOLEAN_TYPE:
                    break;
                case Codable.BOOLEAN_ARRAY_TYPE:
                    writeBooleanArrayColumn(out,(boolean[][])column, rowCount);
                    break;
                case Codable.CHAR_TYPE:
                    writeCharColumn(out, (char[])column, rowCount);
                    break;
                case Codable.CHAR_ARRAY_TYPE:
                    writeCharArrayColumn(out, (char[][])column, rowCount);
                    break;
                case Codable.BYTE_TYPE:
                    writeByteColumn(out, (byte[])column, rowCount);
                    break;
                case Codable.BYTE_ARRAY_TYPE:
                    writeByteArrayColumn(out, (byte[][])column, rowCount);
                    break;
                case Codable.SHORT_TYPE:
                    writeShortColumn(out, (short[])column, rowCount);
                    break;
                case Codable.SHORT_ARRAY_TYPE:
                    writeShortArrayColumn(out, (short[][])column, rowCount);
                    break;
                case Codable.INT_TYPE:
                    writeIntColumn(out, (int[])column, rowCount);
                    break;
                case Codable.INT_ARRAY_TYPE:
                    writeIntArrayColumn(out, (int[][])column, rowCount);
                    break;
                case Codable.LONG_TYPE:
                    writeLongColumn(out, (long[])column, rowCount);
                    break;
                case Codable.LONG_ARRAY_TYPE:
                    writeLongArrayColumn(out, (long[][])column, rowCount);
                    break;
                case Codable.FLOAT_TYPE:
                    writeFloatColumn(out, (float[])column, rowCount);
                    break;
                case Codable.FLOAT_ARRAY_TYPE:
                    writeFloatArrayColumn(out, (float[][])column, rowCount);
                    break;
                case Codable.DOUBLE_TYPE:
                    writeDoubleColumn(out, (double[])column, rowCount);
                    break;
                case Codable.DOUBLE_ARRAY_TYPE:
                    writeDoubleArrayColumn(out, (double[][])column, rowCount);
                    break;
                case Codable.STRING_TYPE:
                    writeStringColumn(out, (String[])column, rowCount);
                    break;
                case Codable.STRING_ARRAY_TYPE:
                    writeStringArrayColumn(out, (String[][])column, rowCount);
                    break;
                case Codable.OBJECT_TYPE:
                    writeIntColumn(out, (int[])column, rowCount);
                    break;
                case Codable.OBJECT_ARRAY_TYPE:
                    writeIntArrayColumn(out, (int[][])column, rowCount);
                    break;
                default:
                    throw new InconsistencyException("Unknown field type: " +
                        fieldTypes[i]);
            }
        }

        // Leave room for expansion.

        out.writeCompactInt(0);
    }

    private void writeBooleanColumn(CompactOutputStream out,
        boolean column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            out.writeCompactBoolean(column[i]);
        }
    }

    private void writeBooleanArrayColumn(CompactOutputStream out,
        boolean column[][], int count) throws IOException {
        int i;
        boolean array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                out.writeCompactInt(array.length);
                writeBooleanColumn(out, array, array.length);
            }
        }
    }

    private void writeCharColumn(CompactOutputStream out,
        char column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            out.writeChar(column[i]);
        }
    }

    private void writeCharArrayColumn(CompactOutputStream out,
        char column[][], int count) throws IOException {
        int i;
        char array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                out.writeCompactInt(array.length);
                writeCharColumn(out, array, array.length);
            }
        }
    }

    private void writeByteColumn(CompactOutputStream out,
        byte column[], int count) throws IOException {
        out.write(column, 0, count);
    }

    private void writeByteArrayColumn(CompactOutputStream out,
        byte column[][], int count) throws IOException {
        int i;
        byte array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                out.writeCompactInt(array.length);
                writeByteColumn(out, array, array.length);
            }
        }
    }

    private void writeShortColumn(CompactOutputStream out,
        short column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            out.writeShort(column[i]);
        }
    }

    private void writeShortArrayColumn(CompactOutputStream out,
        short column[][], int count) throws IOException {
        int i;
        short array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                out.writeCompactInt(array.length);
                writeShortColumn(out, array, array.length);
            }
        }
    }

    private void writeIntColumn(CompactOutputStream out,
        int column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            out.writeCompactInt(column[i]);
        }
    }

    private void writeIntArrayColumn(CompactOutputStream out,
        int column[][], int count) throws IOException {
        int i, j, length;
        int array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                length = array.length;
                out.writeCompactInt(length);

                // Arrays of ints often contain big values (like pixel values)
                // so we don't write them out in compact form since they would
                // get slightly bigger.  ALERT!

                for (j = 0; j < length; j++) {
                    out.writeInt(array[j]);
                }
            }
        }
    }

    private void writeLongColumn(CompactOutputStream out,
        long column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            out.writeLong(column[i]);
        }
    }

    private void writeLongArrayColumn(CompactOutputStream out,
        long column[][], int count) throws IOException {
        int i;
        long array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                out.writeCompactInt(array.length);
                writeLongColumn(out, array, array.length);
            }
        }
    }

    private void writeFloatColumn(CompactOutputStream out,
        float column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            out.writeFloat(column[i]);
        }
    }

    private void writeFloatArrayColumn(CompactOutputStream out,
        float column[][], int count) throws IOException {
        int i;
        float array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                out.writeCompactInt(array.length);
                writeFloatColumn(out, array, array.length);
            }
        }
    }

    private void writeDoubleColumn(CompactOutputStream out,
        double column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            out.writeDouble(column[i]);
        }
    }

    private void writeDoubleArrayColumn(CompactOutputStream out,
        double column[][], int count) throws IOException {
        int i;
        double array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                out.writeCompactInt(array.length);
                writeDoubleColumn(out, array, array.length);
            }
        }
    }

    private void writeStringColumn(CompactOutputStream out,
        String column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            out.writeCompactUTF(column[i]);
        }
    }

    private void writeStringArrayColumn(CompactOutputStream out,
        String column[][], int count) throws IOException {
        int i;
        String array[];

        for (i = 0; i < count; i++) {
            array = column[i];
            if (array == null)
                out.writeCompactInt(-1);
            else {
                out.writeCompactInt(array.length);
                writeStringColumn(out, array, array.length);
            }
        }
    }

    /** Reads class information from <b>inputStream</b>.
      */
    public void readInfo(InputStream inputStream) throws IOException {
        int i, count;
        CompactInputStream in;

        if (inputStream instanceof CompactInputStream)
            in = (CompactInputStream)inputStream;
        else
            in = new CompactInputStream(inputStream);

        className = in.readCompactUTF();

        count = in.readCompactInt();
        fieldCount = count;

        fieldNames = new String[count];
        fieldTypes = new byte[count];
        fieldColumns = new Object[count];

        for (i = 0; i < count; i++) {
            fieldNames[i] = in.readCompactUTF();
            fieldTypes[i] = in.readByte();
        }

        count = in.readCompactInt();
        classCount = count;

        classNames = new String[count];
        classVersions = new int[count];

        for (i = 0; i < count; i++) {
            classNames[i] = in.readCompactUTF();
            classVersions[i] = in.readCompactInt();
        }

        // Leave room for expansion.

        count = in.readCompactInt();
        if (count > 0)
            in.skipBytes(count);
    }

    /** Reads instance data from <b>input</b>.
      */
    public void readData(InputStream inputStream) throws IOException {
        int i, count;
        Object column;
        CompactInputStream in;

        if (inputStream instanceof CompactInputStream)
            in = (CompactInputStream)inputStream;
        else
            in = new CompactInputStream(inputStream);

        rowCount = in.readCompactInt();
        ensureCapacity(rowCount);

        count = fieldCount;

        // Read in all the boolean columns first.

        for (i = 0; i < count; i++) {
            if (fieldTypes[i] == Codable.BOOLEAN_TYPE)
                readBooleanColumn(in, (boolean[])fieldColumns[i], rowCount);
        }

        // Write out the rest of the columns.

        for (i = 0; i < count; i++) {
            column = fieldColumns[i];

            switch (fieldTypes[i]) {
                case Codable.BOOLEAN_TYPE:
                    break;
                case Codable.BOOLEAN_ARRAY_TYPE:
                    readBooleanArrayColumn(in,(boolean[][])column, rowCount);
                    break;
                case Codable.CHAR_TYPE:
                    readCharColumn(in, (char[])column, rowCount);
                    break;
                case Codable.CHAR_ARRAY_TYPE:
                    readCharArrayColumn(in, (char[][])column, rowCount);
                    break;
                case Codable.BYTE_TYPE:
                    readByteColumn(in, (byte[])column, rowCount);
                    break;
                case Codable.BYTE_ARRAY_TYPE:
                    readByteArrayColumn(in, (byte[][])column, rowCount);
                    break;
                case Codable.SHORT_TYPE:
                    readShortColumn(in, (short[])column, rowCount);
                    break;
                case Codable.SHORT_ARRAY_TYPE:
                    readShortArrayColumn(in, (short[][])column, rowCount);
                    break;
                case Codable.INT_TYPE:
                    readIntColumn(in, (int[])column, rowCount);
                    break;
                case Codable.INT_ARRAY_TYPE:
                    readIntArrayColumn(in, (int[][])column, rowCount);
                    break;
                case Codable.LONG_TYPE:
                    readLongColumn(in, (long[])column, rowCount);
                    break;
                case Codable.LONG_ARRAY_TYPE:
                    readLongArrayColumn(in, (long[][])column, rowCount);
                    break;
                case Codable.FLOAT_TYPE:
                    readFloatColumn(in, (float[])column, rowCount);
                    break;
                case Codable.FLOAT_ARRAY_TYPE:
                    readFloatArrayColumn(in, (float[][])column, rowCount);
                    break;
                case Codable.DOUBLE_TYPE:
                    readDoubleColumn(in, (double[])column, rowCount);
                    break;
                case Codable.DOUBLE_ARRAY_TYPE:
                    readDoubleArrayColumn(in, (double[][])column, rowCount);
                    break;
                case Codable.STRING_TYPE:
                    readStringColumn(in, (String[])column, rowCount);
                    break;
                case Codable.STRING_ARRAY_TYPE:
                    readStringArrayColumn(in, (String[][])column, rowCount);
                    break;
                case Codable.OBJECT_TYPE:
                    readIntColumn(in, (int[])column, rowCount);
                    break;
                case Codable.OBJECT_ARRAY_TYPE:
                    readIntArrayColumn(in, (int[][])column, rowCount);
                    break;
                default:
                    throw new InconsistencyException("Unknown field type: " +
                        fieldTypes[i]);
            }
        }

        // Leave room for expansion.

        count = in.readCompactInt();
        if (count > 0)
            in.skipBytes(count);
    }

    private void readBooleanColumn(CompactInputStream in,
        boolean column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            column[i] = in.readCompactBoolean();
        }
    }

    private void readBooleanArrayColumn(CompactInputStream in,
        boolean column[][], int count) throws IOException {
        int i, length;
        boolean array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {
                array = new boolean[length];
                column[i] = array;
                readBooleanColumn(in, array, length);
            }
        }
    }

    private void readCharColumn(CompactInputStream in,
        char column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            column[i] = in.readChar();
        }
    }

    private void readCharArrayColumn(CompactInputStream in,
        char column[][], int count) throws IOException {
        int i, length;
        char array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {
                array = new char[length];
                column[i] = array;
                readCharColumn(in, array, length);
            }
        }
    }

    private void readByteColumn(CompactInputStream in,
        byte column[], int count) throws IOException {
        in.readFully(column, 0, count);
    }

    private void readByteArrayColumn(CompactInputStream in,
        byte column[][], int count) throws IOException {
        int i, length;
        byte array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {
                array = new byte[length];
                column[i] = array;
                readByteColumn(in, array, length);
            }
        }
    }

    private void readShortColumn(CompactInputStream in,
        short column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            column[i] = in.readShort();
        }
    }

    private void readShortArrayColumn(CompactInputStream in,
        short column[][], int count) throws IOException {
        int i, length;
        short array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {
                array = new short[length];
                column[i] = array;
                readShortColumn(in, array, length);
            }
        }
    }

    private void readIntColumn(CompactInputStream in,
        int column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            column[i] = in.readCompactInt();
        }
    }

    private void readIntArrayColumn(CompactInputStream in,
        int column[][], int count) throws IOException {
        int i, j, length;
        int array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {

                // Arrays of ints often contain big values (like pixel values)
                // so we don't write them out in compact form since they would
                // get slightly bigger.  ALERT!

                array = new int[length];
                column[i] = array;
                for (j = 0; j < length; j++) {
                    array[j] = in.readInt();
                }
            }
        }
    }

    private void readLongColumn(CompactInputStream in,
        long column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            column[i] = in.readLong();
        }
    }

    private void readLongArrayColumn(CompactInputStream in,
        long column[][], int count) throws IOException {
        int i, length;
        long array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {
                array = new long[length];
                column[i] = array;
                readLongColumn(in, array, length);
            }
        }
    }

    private void readFloatColumn(CompactInputStream in,
        float column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            column[i] = in.readFloat();
        }
    }

    private void readFloatArrayColumn(CompactInputStream in,
        float column[][], int count) throws IOException {
        int i, length;
        float array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {
                array = new float[length];
                column[i] = array;
                readFloatColumn(in, array, length);
            }
        }
    }

    private void readDoubleColumn(CompactInputStream in,
        double column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            column[i] = in.readDouble();
        }
    }

    private void readDoubleArrayColumn(CompactInputStream in,
        double column[][], int count) throws IOException {
        int i, length;
        double array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {
                array = new double[length];
                column[i] = array;
                readDoubleColumn(in, array, length);
            }
        }
    }

    private void readStringColumn(CompactInputStream in,
        String column[], int count) throws IOException {
        int i;

        for (i = 0; i < count; i++) {
            column[i] = in.readCompactUTF();
        }
    }

    private void readStringArrayColumn(CompactInputStream in,
        String column[][], int count) throws IOException {
        int i, length;
        String array[];

        for (i = 0; i < count; i++) {
            length = in.readCompactInt();
            if (length >= 0) {
                array = new String[length];
                column[i] = array;
                readStringColumn(in, array, length);
            }
        }
    }
}
