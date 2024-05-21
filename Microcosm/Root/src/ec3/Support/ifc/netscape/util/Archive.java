// Archive.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

import java.io.*;

/** Object subclass used by the Archiver and Unarchiver to store the
  * encoded state of Codable objects. Archives contain a collection of
  * ClassTables and a mapping of object identifiers to a row in a specific
  * ClassTable. Each ClassTable row corresponds to an encoded object.
  * References between objects are encoded as opaque integers (identifiers).
  * The archive also maintains a set of root identifiers from which all
  * objects in the archive can be reached. Most programs will not need to
  * deal directly with the Archive class, but instead will work with Archivers
  * and Unarchivers.
  *
  * @see ClassTable
  * @see Archiver
  * @see Unarchiver
  * @note 1.0 Made the binary archive magic number public
  */
public class Archive {
    /** @private */
    public static final int ARCHIVE_MAGIC = 0x6E656421;
    static final int ARCHIVE_VERSION = 1;

    // This can be static for now since it is hidden and immutable.
    static Hashtable externalCoders;

    int version;

    int rootCount;
    int roots[];

    int idCount;
    int rowForId[];
    ClassTable tableForId[];

    Hashtable classTables;

    /** When set to <b>true</b>, the Archive will print messages when fields
      * are accessed out of order.  Out of order access reduces archiving
      * performance.
      * @private
      */
    public boolean performanceDebug = false;

    /** Constructs a new, empty Archive.
      */
    public Archive() {
        super();

        rootCount = 0;
        roots = new int[4];

        // By convention id = 0 maps to the null object.  It has no table
        // or row, but is implicitly in every archive.

        idCount = 1;
        rowForId = new int[4];
        tableForId = new ClassTable[4];

        classTables = new Hashtable();
    }

    private static int[] growIntArray(int array[]) {
        int newArray[];

        newArray = new int[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, array.length);

        return newArray;
    }

    private static ClassTable[] growTableArray(ClassTable array[]) {
        ClassTable newArray[];

        newArray = new ClassTable[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, array.length);

        return newArray;
    }

    /** Adds an identifier to the array of root identifiers. The identifier
      * must be created by calling <b>newIdentifier()</b> on a ClassTable.
      * @see ClassTable#newIdentifier
      */
    public void addRootIdentifier(int id) {
        if (rootCount >= roots.length)
            roots = growIntArray(roots);

        roots[rootCount] = id;
        rootCount++;
    }

    /** Removes an identifier from the array of root identifiers. Returns
      * <b>true</b> if the identifier was in the array, <b>false</b> if it
      * was not.
      * @see ClassTable#addRootIdentifier
      */
    public boolean removeRootIdentifier(int id) {
        int i;
        boolean removed = false;

        for (i = 0; i < rootCount; i++) {
            if (roots[i] == id) {
                removed = true;
                rootCount--;
                break;
            }
        }

        for (; i < rootCount; i++)
            roots[i] = roots[i + 1];

        return removed;
    }

    /** Returns a copy of the root identifier array.
      */
    public int[] rootIdentifiers() {
        int rootsCopy[];

        rootsCopy = new int[rootCount];
        System.arraycopy(roots, 0, rootsCopy, 0, rootCount);

        return rootsCopy;
    }

    /** Returns the ClassTable for a given class name.
      */
    public ClassTable classTableForName(String className) {
        return (ClassTable)classTables.get(className);
    }

    /** Adds a new ClassTable to the archive. This should be called after
      * creating a new ClassTable.
      * @see ClassTable
      */
    public void addClassTable(ClassTable table) {
        classTables.put(table.className(), table);
    }

    /** Returns the ClassTable for a given object identifier in the archive.
      * @see ClassTable
      */
    public ClassTable classTableForIdentifier(int id) {
        if (id >= idCount)
            throw new ArrayIndexOutOfBoundsException(id);

        return tableForId[id];
    }

    /** Returns the row for a given object identifier in the archive.
      * @see ClassTable
      */
    public int rowForIdentifier(int id) {
        if (id >= idCount)
            throw new ArrayIndexOutOfBoundsException(id);

        return rowForId[id];
    }

    /** Primitive method for mapping a ClassTable and row to a new object
      * identifier. Most programs will simply call
      * <b>ClassTable.newIdentifier()</b> to get a new object identifier.
      * @see ClassTable#newIdentifier
      */
    public int mapIdentifier(ClassTable table, int row) {
        int id;

        if (idCount >= rowForId.length) {
            rowForId = growIntArray(rowForId);
            tableForId = growTableArray(tableForId);
        }

        id = idCount;
        rowForId[id] = row;
        tableForId[id] = table;
        idCount++;

        return id;
    }

    /** Returns the number of object identifiers in the archive. All
      * identifiers will be between 0 and (identifierCount() - 1), inclusive.
      */
    public int identifierCount() {
        return idCount;
    }

    /** This is a hook which allows objects to be registered that know how
      * to codify objects which don't themselves implement Codable. If this
      * works well, this should be made public in the next release.
      */
    static synchronized void setupExternalCoders() {
        PrimitiveCoder coder;

        if (externalCoders != null)
            return;

        externalCoders = new Hashtable(24);

        coder = new PrimitiveCoder(Codable.BOOLEAN_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.BOOLEAN_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.CHAR_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.CHAR_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.BYTE_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.BYTE_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.SHORT_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.SHORT_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.INT_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.INT_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.LONG_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.LONG_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.FLOAT_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.FLOAT_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.DOUBLE_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.DOUBLE_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.STRING_TYPE);
        externalCoders.put(coder.className(), coder);
        coder = new PrimitiveCoder(Codable.STRING_ARRAY_TYPE);
        externalCoders.put(coder.className(), coder);
    }

    ExternalCoder externalCoderForName(String className) {
        if (externalCoders == null) {
            setupExternalCoders();
        }

        return (ExternalCoder)externalCoders.get(className);
    }

    /** Reads a binary serialization of the Archive's contents from
      * <b>inputStream</b>.
      * @see Unarchiver#readObject
      */
    public void read(InputStream inputStream) throws IOException {
        int i, count, magic;
        ClassTable table, tables[];
        CompactInputStream in;

        if (inputStream instanceof CompactInputStream)
            in = (CompactInputStream)inputStream;
        else
            in = new CompactInputStream(inputStream);

        // Read in the magic number and archive version.

        magic = in.readInt();
        if (magic != ARCHIVE_MAGIC)
            throw new IOException("Bad magic number " + magic);

        version = in.readInt();
        if (version != ARCHIVE_VERSION)
            throw new IOException("Unknown archiveVersion " + version);

        // Read in all the ClassTables.

        count = in.readCompactInt();
        tables = new ClassTable[count];

        for (i = 0; i < count; i++) {
            table = new ClassTable(this);
            tables[i] = table;
            table.readInfo(in);
            addClassTable(table);
        }

        for (i = 0; i < count; i++) {
            table = tables[i];
            table.readData(in);
        }

        // Read in the root ids.

        count = in.readCompactInt();
        roots = new int[count];
        rootCount = count;

        for (i = 0; i < count; i++) {
            roots[i] = in.readCompactInt();
        }

        // Read in the id mappings.  This must happen after reading the
        // ClassTables or the tables array will not be set properly.

        count = in.readCompactInt();
        rowForId = new int[count];
        tableForId = new ClassTable[count];
        idCount = count;

        for (i = 1; i < count; i++) {
            rowForId[i] = in.readCompactInt();
            tableForId[i] = tables[in.readCompactInt()];
        }

        // Leave room for expansion.

        count = in.readCompactInt();
        if (count > 0)
            in.skipBytes(count);
    }

    /** Writes a binary serialization of the Archive's contents to
      * <b>outputStream</b>.
      * @see Archiver#writeObject
      */
    public void write(OutputStream outputStream) throws IOException {
        int i, count;
        Object tables[];
        CompactOutputStream out;
        ClassTable table;

        if (outputStream instanceof CompactOutputStream)
            out = (CompactOutputStream)outputStream;
        else
            out = new CompactOutputStream(outputStream);

        // Write out the magic number and archive version.  To be nice to
        // others, we won't write these out in compact form.

        out.writeInt(ARCHIVE_MAGIC);
        out.writeInt(ARCHIVE_VERSION);

        // Write out all the ClassTables.  We need to map each ClassTable to
        // an int which we cache in ClassTable.tableId.  This id is good only
        // for this write.

        tables = classTables.elementsArray();
        if (tables == null)
            count = 0;
        else
            count = tables.length;

        out.writeCompactInt(count);

        // Write out all the meta-data first.

        for (i = 0; i < count; i++) {
            table = (ClassTable)tables[i];
            table.tableId = i;
            table.writeInfo(out);
        }

        // Write out all the instance data.

        for (i = 0; i < count; i++) {
            table = (ClassTable)tables[i];
            table.writeData(out);
        }

        // Write out the root ids.

        count = rootCount;
        out.writeCompactInt(count);
        for (i = 0; i < count; i++)
            out.writeCompactInt(roots[i]);

        // Write out the id mappings.  This must happen after writing out the
        // ClassTables or the tableId will not be set properly.

        count = idCount;
        out.writeCompactInt(count);
        for (i = 1; i < count; i++) {
            out.writeCompactInt(rowForId[i]);
            table = tableForId[i];
            if (table == null)
                out.writeCompactInt(0);
            else
                out.writeCompactInt(table.tableId);
        }

        // Leave room for expansion.

        out.writeCompactInt(0);

        out.flush();
    }

    /** Reads an ASCII serialization of the Archive's contents from
      * <b>inputStream</b>.
      * @see Serializer
      * @see Deserializer
      */
    public void readASCII(InputStream inputStream) throws CodingException,
        DeserializationException, IOException {
        ASCIIArchiveLoader loader;

        loader = new ASCIIArchiveLoader(this);
        loader.readASCII(inputStream);
    }

    /** Writes an ASCII serialization of the Archive's contents to
      * <b>outputStream</b>.  If <b>formatted</b> is <b>true</b>, the Archive
      * formats the output for easy reading.  If <b>false</b>, the output will
      * be as compact as possible.
      * @see Serializer
      * @see Deserializer
      */
    public void writeASCII(OutputStream outputStream, boolean formatted)
        throws CodingException, IOException {
        ASCIIArchiveLoader loader;

        loader = new ASCIIArchiveLoader(this);
        loader.writeASCII(outputStream, formatted);
    }
}
