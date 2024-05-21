// ASCIIArchiveLoader.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

import java.io.*;

class ASCIIArchiveLoader {
    static final String classTablesKey = "classTables";
    static final String fieldNamesKey = "fieldNames";
    static final String fieldTypesKey = "fieldTypes";
    static final String classNamesKey = "classNames";
    static final String classVersionsKey = "classVersions";
    static final String instancesKey = "instances";
    static final String rootInstancesKey = "rootInstances";
    static final String archiveVersionKey = "archiveVersion";

    Archive archive;
    Hashtable archiveDict;

    IdHashtable idForName;
    String nameForId[];
    Hashtable baseNameForTable;

    ASCIIArchiveLoader(Archive archive) {
        super();
        this.archive = archive;
    }

    void readASCII(InputStream in) throws CodingException,
        DeserializationException, IOException {
        Deserializer deserializer = null;
        String versionString;

        if (in instanceof Deserializer) {
            deserializer = (Deserializer) in;
        }

        // While reading an ASCII archive we need to maintain a mapping
        // between instance "names" in the ASCII file, and the archive ids for
        // those names.

        idForName = new IdHashtable(true);

        // Read in the ASCII archive file.  If there are any syntax errors in
        // the file this will throw a DeserializationException.  We can't
        // really add more useful error information to that at this point.

        if( deserializer == null )
            deserializer = new Deserializer(in);
        archiveDict = (Hashtable)deserializer.readObject();

        // Check the version information.

        versionString = (String)archiveDict.get(archiveVersionKey);
        if (versionString == null)
            throw new IOException("Missing archiveVersion");

        archive.version = Integer.parseInt(versionString);
        if (archive.version != Archive.ARCHIVE_VERSION)
            throw new IOException("Unknown archiveVersion " + archive.version);

        // Load the pieces of the archive file.

        loadClassInfo();
        loadInstanceData();
        loadRoots();
    }

    void loadClassInfo() throws CodingException {
        Hashtable allTablesDict, tableDict;
        Enumeration classNameEnum;
        String className;
        Object fieldNames[], fieldTypes[], classNames[], classVersions[];
        ClassTable table;
        ClassInfo info;
        int i, version;

        // Get the information for each of the class tables.  It is not
        // immediately an error to have no class tables, but if there are
        // instances in the archive they will have no place to go and an
        // exception will be thrown during loadInstanceData().

        allTablesDict = (Hashtable)archiveDict.get(classTablesKey);
        if (allTablesDict == null)
            return;

        classNameEnum = allTablesDict.keys();

        while (classNameEnum.hasMoreElements()) {

            className = (String)classNameEnum.nextElement();
            tableDict = (Hashtable)allTablesDict.get(className);
            info = new ClassInfo(className);

            classNames = (Object[])tableDict.get(classNamesKey);
            classVersions = (Object[])tableDict.get(classVersionsKey);

            for (i = 0; i < classNames.length; i++) {
                version = Integer.parseInt((String)classVersions[i]);
                info.addClass((String)classNames[i], version);
            }

            fieldNames = (Object[])tableDict.get(fieldNamesKey);
            fieldTypes = (Object[])tableDict.get(fieldTypesKey);

            for (i = 0; i < fieldNames.length; i++) {
                info.addField((String)fieldNames[i],
                    typeForName((String)fieldTypes[i]));
            }

            table = new ClassTable(archive, info);
            archive.addClassTable(table);
        }
    }

    static byte typeForName(String typeName) throws CodingException {
        int length;

        length = typeName.length();

        if (length <= 0)
            throw new CodingException("unknown type name: " + typeName);

        switch(typeName.charAt(0)) {
            case 'b':
                if (typeName.equals("boolean"))
                    return Codable.BOOLEAN_TYPE;
                if (typeName.equals("boolean[]"))
                    return Codable.BOOLEAN_ARRAY_TYPE;
                if (typeName.equals("byte"))
                    return Codable.BYTE_TYPE;
                if (typeName.equals("byte[]"))
                    return Codable.BYTE_ARRAY_TYPE;
                break;
            case 'c':
                if (typeName.equals("char"))
                    return Codable.CHAR_TYPE;
                if (typeName.equals("char[]"))
                    return Codable.CHAR_ARRAY_TYPE;
                break;
            case 's':
                if (typeName.equals("short"))
                    return Codable.SHORT_TYPE;
                if (typeName.equals("short[]"))
                    return Codable.SHORT_ARRAY_TYPE;
                break;
            case 'i':
                if (typeName.equals("int"))
                    return Codable.INT_TYPE;
                if (typeName.equals("int[]"))
                    return Codable.INT_ARRAY_TYPE;
                break;
            case 'l':
                if (typeName.equals("long"))
                    return Codable.LONG_TYPE;
                if (typeName.equals("long[]"))
                    return Codable.LONG_ARRAY_TYPE;
                break;
            case 'f':
                if (typeName.equals("float"))
                    return Codable.FLOAT_TYPE;
                if (typeName.equals("float[]"))
                    return Codable.FLOAT_ARRAY_TYPE;
                break;
            case 'd':
                if (typeName.equals("double"))
                    return Codable.DOUBLE_TYPE;
                if (typeName.equals("double[]"))
                    return Codable.DOUBLE_ARRAY_TYPE;
                break;
            case 'j':
                if (length == 16) {
                    if (typeName.equals("java.lang.String"))
                        return Codable.STRING_TYPE;
                    if (typeName.equals("java.lang.Object"))
                        return Codable.OBJECT_TYPE;
                } else if (length == 18) {
                    if (typeName.equals("java.lang.String[]"))
                        return Codable.STRING_ARRAY_TYPE;
                    if (typeName.equals("java.lang.Object[]"))
                        return Codable.OBJECT_ARRAY_TYPE;
                }
                break;
        }

        throw new CodingException("unknown type name: " + typeName);
    }

    void loadInstanceData() throws CodingException {
        String className;
        Enumeration classNameEnum;
        Hashtable allTablesDict, tableDict;

        allTablesDict = (Hashtable)archiveDict.get(classTablesKey);
        if (allTablesDict == null)
            return;

        // In the ASCII version of the archive we need to make two passes over
        // the instances.  The first pass creates a row for each instance and
        // builds the name to id mapping.

        classNameEnum = allTablesDict.keys();

        while (classNameEnum.hasMoreElements()) {
            className = (String)classNameEnum.nextElement();
            tableDict = (Hashtable)allTablesDict.get(className);

            loadRowMapping(className, tableDict);
        }

        // The second pass over the instances actually loads the data for each
        // instance into the row.

        classNameEnum = allTablesDict.keys();

        while (classNameEnum.hasMoreElements()) {
            className = (String)classNameEnum.nextElement();
            tableDict = (Hashtable)allTablesDict.get(className);

            loadRowData(className, tableDict);
        }

    }

    void loadRowMapping(String className, Hashtable tableDict)
        throws CodingException {
        int id;
        Hashtable allInstancesDict, instanceDict;
        ClassTable table;
        Enumeration nameEnum;
        String name;

        allInstancesDict = (Hashtable)tableDict.get(instancesKey);
        if (allInstancesDict == null)
            return;

        nameEnum = allInstancesDict.keys();
        while (nameEnum.hasMoreElements()) {
            name = (String)nameEnum.nextElement();
            instanceDict = (Hashtable)allInstancesDict.get(name);

            if (idForName.get(name) != IdHashtable.NOT_FOUND)
                throw new CodingException("duplicate instance name: "+name);

            table = archive.classTableForName(className);
            if (table == null)
                throw new CodingException("bad class name for instance: " +
                    name);

            id = table.newIdentifier();
            idForName.putKnownAbsent(name, id);
        }
    }

    void loadRowData(String className, Hashtable tableDict)
        throws CodingException {
        int id, row;
        Enumeration nameEnum;
        String name;
        ClassTable table;
        Hashtable allInstancesDict, instanceDict;

        allInstancesDict = (Hashtable)tableDict.get(instancesKey);
        if (allInstancesDict == null)
            return;

        nameEnum = allInstancesDict.keys();
        while (nameEnum.hasMoreElements()) {
            name = (String)nameEnum.nextElement();
            id = idForName.get(name);
            table = archive.tableForId[id];
            row = archive.rowForId[id];
            instanceDict = (Hashtable)allInstancesDict.get(name);
            loadRow(table, row, instanceDict);
        }
    }

    void loadRow(ClassTable table, int row, Hashtable instanceDict)
        throws CodingException {
        int i;
        Object value;

        for (i = 0; i < table.fieldCount; i++) {
            value = instanceDict.get(table.fieldNames[i]);
            if (value != null)
                setColumnFromValue(table, row, i, value);
        }
    }

    void setColumnFromValue(ClassTable table, int row, int column, Object v)
        throws CodingException {
        int i, count;
        boolean booleanArray[];
        char charArray[];
        byte byteArray[];
        short shortArray[];
        int intArray[];
        long longArray[];
        float floatArray[];
        double doubleArray[];
        String stringArray[];
        String value = null;
        Object array[] = null;

        if (v instanceof String) {
            value = (String)v;

            switch (table.fieldTypes[column]) {
                case Codable.BOOLEAN_TYPE:
                    if (value.equalsIgnoreCase("true"))
                        table.setBooleanAt(row, column, true);
                    else if (value.equalsIgnoreCase("false"))
                        table.setBooleanAt(row, column, false);
                    else
                        throw new CodingException("Invalid boolean value");
                    break;
                case Codable.CHAR_TYPE:
                    table.setCharAt(row, column, (char)value.charAt(0));
                    break;
                case Codable.BYTE_TYPE:
                    table.setByteAt(row, column,
                        (byte)Integer.parseInt(value));
                    break;
                case Codable.BYTE_ARRAY_TYPE:
                    table.setByteArrayAt(row, column, bytesFromString(value));
                    break;
                case Codable.SHORT_TYPE:
                    table.setShortAt(row, column,
                        (short)Integer.parseInt(value));
                    break;
                case Codable.INT_TYPE:
                    table.setIntAt(row, column, Integer.parseInt(value));
                    break;
                case Codable.LONG_TYPE:
                    table.setLongAt(row, column, Long.parseLong(value));
                    break;
                case Codable.FLOAT_TYPE:
                    table.setFloatAt(row, column,
                        Float.valueOf(value).floatValue());
                    break;
                case Codable.DOUBLE_TYPE:
                    table.setDoubleAt(row, column,
                        Double.valueOf(value).doubleValue());
                    break;
                case Codable.STRING_TYPE:
                    table.setStringAt(row, column, value);
                    break;
                case Codable.OBJECT_TYPE:
                    table.setIdentifierAt(row, column, idForName.get(value));
                    break;
                default:
                    throw new CodingException("Invalid value " + value);
            }
        } else if (v instanceof Object[]) {
            array = (Object[])v;

            switch (table.fieldTypes[column]) {
                case Codable.BOOLEAN_ARRAY_TYPE:
                    count = array.length;
                    booleanArray = new boolean[count];
                    for (i = 0; i < count; i++) {
                        value = (String)array[i];
                        if (value.equalsIgnoreCase("true"))
                            booleanArray[i] = true;
                        else if (value.equalsIgnoreCase("false"))
                            booleanArray[i] = false;
                        else
                            throw new CodingException("Invalid boolean value");
                    }
                    table.setBooleanArrayAt(row, column, booleanArray);
                    break;
                case Codable.CHAR_ARRAY_TYPE:
                    count = array.length;
                    charArray = new char[count];
                    for (i = 0; i < count; i++) {
                        value = (String)array[i];
                        charArray[i] = value.charAt(0);
                    }
                    table.setCharArrayAt(row, column, charArray);
                    break;
                case Codable.SHORT_ARRAY_TYPE:
                    count = array.length;
                    shortArray = new short[count];
                    for (i = 0; i < count; i++) {
                        value = (String)array[i];
                        shortArray[i] = (short)Integer.parseInt(value);
                    }
                    table.setShortArrayAt(row, column, shortArray);
                    break;
                case Codable.INT_ARRAY_TYPE:
                    count = array.length;
                    intArray = new int[count];
                    for (i = 0; i < count; i++) {
                        value = (String)array[i];
                        intArray[i] = Integer.parseInt(value);
                    }
                    table.setIntArrayAt(row, column, intArray);
                    break;
                case Codable.LONG_ARRAY_TYPE:
                    count = array.length;
                    longArray = new long[count];
                    for (i = 0; i < count; i++) {
                        value = (String)array[i];
                        longArray[i] = Long.parseLong(value);
                    }
                    table.setLongArrayAt(row, column, longArray);
                    break;
                case Codable.FLOAT_ARRAY_TYPE:
                    count = array.length;
                    floatArray = new float[count];
                    for (i = 0; i < count; i++) {
                        value = (String)array[i];
                        floatArray[i] = Float.valueOf(value).floatValue();
                    }
                    table.setFloatArrayAt(row, column, floatArray);
                    break;
                case Codable.DOUBLE_ARRAY_TYPE:
                    count = array.length;
                    doubleArray = new double[count];
                    for (i = 0; i < count; i++) {
                        value = (String)array[i];
                        doubleArray[i] = Double.valueOf(value).doubleValue();
                    }
                    table.setDoubleArrayAt(row, column, doubleArray);
                    break;
                case Codable.STRING_ARRAY_TYPE:
                    count = array.length;
                    stringArray = new String[count];
                    for (i = 0; i < count; i++) {
                        stringArray[i] = (String)array[i];
                    }
                    table.setStringArrayAt(row, column, stringArray);
                    break;
                case Codable.OBJECT_ARRAY_TYPE:
                    count = array.length;
                    intArray = new int[count];
                    for (i = 0; i < count; i++) {
                        value = (String)array[i];
                        if (value != null)
                            intArray[i] = idForName.get(value);
                    }
                    table.setIdentifierArrayAt(row, column, intArray);
                    break;
                default:
                    throw new CodingException("Invalid value" + value);
            }
        }
    }

    byte[] bytesFromString(String value) {
        int i, count, nibble, outCount;
        char ch;
        byte buf[], tmp[];

        if (value == null)
            return null;

        if (value.equals(""))
            return new byte[0];

        count = value.length();
        buf = new byte[count / 2 + 1];
        i = 0;
        outCount = 0;

        while (i < count) {
            while (i < count) {
                ch = value.charAt(i++);
                if (Character.isSpace(ch))
                    continue;

                if (i >= count)
                    throw new NumberFormatException("bad byte string");

                nibble = nibbleForHexChar(ch);
                ch = value.charAt(i++);
                buf[outCount++] = (byte)((nibble << 4) + nibbleForHexChar(ch));
            }
        }

        tmp = new byte[outCount];
        System.arraycopy(buf, 0, tmp, 0, outCount);

        return tmp;
    }

    int nibbleForHexChar(char ch) {
        if (ch >= '0' && ch <= '9')
            return ch - '0';
        else if (ch >= 'a' && ch <= 'f')
            return ch - 'a' + 10;
        else if (ch >= 'A' && ch <= 'F')
            return ch - 'A' + 10;

        throw new NumberFormatException("bad byte string");
    }

    void loadRoots() throws CodingException {
        int i, id;
        Object rootsArray[];
        String name;

        rootsArray = (Object[])archiveDict.get(rootInstancesKey);

        if (rootsArray == null || rootsArray.length == 0)
            return;

        for (i = 0; i < rootsArray.length; i++) {
            name = (String)rootsArray[i];
            if (name == null || name.equals(""))
                id = 0;
            else
                id = idForName.get(name);

            if (id == 0)
                throw new CodingException("unknown root instance " + name);

            archive.addRootIdentifier(id);
        }
    }

    void writeASCII(OutputStream out, boolean formatted) throws IOException {
        Serializer serializer;
        String versionString;

        // We need to maintain a mapping from archive id to a human readable
        // name for the ASCII file.  This is always accessed through the
        // method nameForId().

        nameForId = new String[archive.idCount];
        baseNameForTable = new Hashtable();

        // Save the pieces of the archive file.

        archiveDict = new Hashtable();

        versionString = String.valueOf(Archive.ARCHIVE_VERSION);
        archiveDict.put(archiveVersionKey, versionString);

        saveClassInfo();
        saveInstanceData();
        saveRoots();

        // Write out the archive.

        if( formatted )
            serializer = (Serializer) new FormattingSerializer(out);
        else
            serializer = new Serializer(out);

        serializer.writeObject(archiveDict);

        // Necessary. serializer has internal output buffer.
        serializer.flush();
    }

    String nameForId(int id) {
        String name, base, seqString;
        ClassTable table;
        int lastDotIndex, digits;

        name = nameForId[id];

        if (name != null)
            return name;

        table = archive.tableForId[id];
        if (table == null) {
            return null;
        }

        // Create base name for all the instances from this table.

        base = (String)baseNameForTable.get(table);
        if (base == null) {
            base = table.className();
            lastDotIndex = base.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < (base.length() - 1)) {
                if (base.charAt(0) != '[')
                    base = base.substring(lastDotIndex + 1, base.length());
            }

            // Append a character which can't appear in a valid class name
            // to ensure uniqueness.

            base = base + "-";

            // Test to make sure this base is unique.  If the short name is
            // not unique, then fall back to the long name.

            if (baseNameForTable.get(base) != null) {
                base = table.className() + "-";

                // If the long name isn't unique, then just keep trying.
                digits = 0;
                while (baseNameForTable.get(base) != null) {
                    base = table.className() + "-" + digits + "-";
                    digits++;
                }
            }

            baseNameForTable.put(table, base);
        }

        // For now just make a name that is the class name followed by a
        // unique number (the row number in that table).

        digits = decimalDigitCount(table.rowCount());
        seqString = decimalString(archive.rowForId[id], digits);

        name = base + seqString;
        nameForId[id] = name;

        return name;
    }

    int decimalDigitCount(int number) {
        int i = 0;

        while (number > 0) {
            i++;
            number = number / 10;
        }

        return i;
    }

    String decimalString(int number, int digits) {
        int i;
        char buf[] = new char[digits];

        for (i = 0; i < digits; i++) {
            buf[i] = '0';
        }

        i = digits;
        while (number > 0 && i > 0) {
            i--;
            buf[i] = (char)(number % 10 + '0');
            number = number / 10;
        }

        return new String(buf);
    }

    void saveClassInfo() {
        Hashtable allTablesDict, tableDict;
        Enumeration tableEnum;
        ClassTable table;

        allTablesDict = new Hashtable();
        tableEnum = archive.classTables.elements();

        while (tableEnum.hasMoreElements()) {
            table = (ClassTable)tableEnum.nextElement();
            tableDict = dictionaryForTable(table);
            allTablesDict.put(table.className(), tableDict);
        }

        archiveDict.put(classTablesKey, allTablesDict);
    }

    Hashtable dictionaryForTable(ClassTable table) {
        int i;
        Hashtable tableDict;
        String typeArray[], classVersions[];

        tableDict = new Hashtable(5);
        typeArray = new String[table.fieldCount];

        for (i = 0; i < table.fieldCount; i++) {
            typeArray[i] = nameForType(table.fieldTypes[i]);
        }

        tableDict.put(fieldNamesKey, table.fieldNames);
        tableDict.put(fieldTypesKey, typeArray);

        classVersions = new String[table.classCount];

        for (i = 0; i < table.classCount; i++) {
            classVersions[i] = String.valueOf(table.classVersions[i]);
        }

        tableDict.put(classNamesKey, table.classNames);
        tableDict.put(classVersionsKey, classVersions);

        return tableDict;
    }

    String nameForType(int fieldType) {
        switch (fieldType) {
            case Codable.BOOLEAN_TYPE:       return "boolean";
            case Codable.BOOLEAN_ARRAY_TYPE: return "boolean[]";
            case Codable.CHAR_TYPE:          return "char";
            case Codable.CHAR_ARRAY_TYPE:    return "char[]";
            case Codable.BYTE_TYPE:          return "byte";
            case Codable.BYTE_ARRAY_TYPE:    return "byte[]";
            case Codable.SHORT_TYPE:         return "short";
            case Codable.SHORT_ARRAY_TYPE:   return "short[]";
            case Codable.INT_TYPE:           return "int";
            case Codable.INT_ARRAY_TYPE:     return "int[]";
            case Codable.LONG_TYPE:          return "long";
            case Codable.LONG_ARRAY_TYPE:    return "long[]";
            case Codable.FLOAT_TYPE:         return "float";
            case Codable.FLOAT_ARRAY_TYPE:   return "float[]";
            case Codable.DOUBLE_TYPE:        return "double";
            case Codable.DOUBLE_ARRAY_TYPE:  return "double[]";
            case Codable.STRING_TYPE:        return "java.lang.String";
            case Codable.STRING_ARRAY_TYPE:  return "java.lang.String[]";
            case Codable.OBJECT_TYPE:        return "java.lang.Object";
            case Codable.OBJECT_ARRAY_TYPE:  return "java.lang.Object[]";
            default:
                throw new IllegalArgumentException("Unknown field type: " +
                    fieldType);
        }
    }

    void saveInstanceData() {
        int i, row;
        Hashtable allInstancesDict, instanceDict, allTablesDict, tableDict;
        ClassTable table;
        String name;

        allTablesDict = (Hashtable)archiveDict.get(classTablesKey);

        // id = 0 means null so we can just skip it.

        for (i = 1; i < archive.idCount; i++) {
            table = archive.tableForId[i];
            row = archive.rowForId[i];
            instanceDict = dictionaryForInstance(table, row);

            tableDict = (Hashtable)allTablesDict.get(table.className);
            allInstancesDict = (Hashtable)tableDict.get(instancesKey);
            if (allInstancesDict == null) {
                allInstancesDict = new Hashtable();
                tableDict.put(instancesKey, allInstancesDict);
            }

            allInstancesDict.put(nameForId(i), instanceDict);
        }
    }

    Hashtable dictionaryForInstance(ClassTable table, int row) {
        int i;
        Hashtable dict;
        Object fieldValue;

        dict = new Hashtable(2 * table.fieldCount);

        for (i = 0; i < table.fieldCount; i++) {
            fieldValue = valueForField(table, row, i);
            if (fieldValue != null)
                dict.put(table.fieldNames[i], fieldValue);
        }

        return dict;
    }

    Object valueForField(ClassTable table, int row, int column) {
        int i, count;
        boolean booleanArray[];
        char charArray[];
        byte byteArray[];
        short shortArray[];
        int intArray[];
        long longArray[];
        float floatArray[];
        double doubleArray[];
        String stringArray[];

        switch (table.fieldTypes[column]) {
            case Codable.BOOLEAN_TYPE:
                return String.valueOf(table.booleanAt(row, column));
            case Codable.BOOLEAN_ARRAY_TYPE:
                booleanArray = table.booleanArrayAt(row, column);
                if (booleanArray == null)
                    return null;
                count = booleanArray.length;
                stringArray = new String[count];
                for (i = 0; i < count; i++)
                    stringArray[i] = String.valueOf(booleanArray[i]);
                return stringArray;
            case Codable.CHAR_TYPE:
                return String.valueOf(table.charAt(row, column));
            case Codable.CHAR_ARRAY_TYPE:
                charArray = table.charArrayAt(row, column);
                if (charArray == null)
                    return null;
                count = charArray.length;
                stringArray = new String[count];
                for (i = 0; i < count; i++)
                    stringArray[i] = String.valueOf(charArray[i]);
                return stringArray;
            case Codable.BYTE_TYPE:
                return String.valueOf(table.byteAt(row, column) & 0xff);
            case Codable.BYTE_ARRAY_TYPE:
                return bytesString(table.byteArrayAt(row, column));
            case Codable.SHORT_TYPE:
                return String.valueOf(table.shortAt(row, column));
            case Codable.SHORT_ARRAY_TYPE:
                shortArray = table.shortArrayAt(row, column);
                if (shortArray == null)
                    return null;
                count = shortArray.length;
                stringArray = new String[count];
                for (i = 0; i < count; i++)
                    stringArray[i] = String.valueOf(shortArray[i]);
                return stringArray;
            case Codable.INT_TYPE:
                return String.valueOf(table.intAt(row, column));
            case Codable.INT_ARRAY_TYPE:
                intArray = table.intArrayAt(row, column);
                if (intArray == null)
                    return null;
                count = intArray.length;
                stringArray = new String[count];
                for (i = 0; i < count; i++)
                    stringArray[i] = String.valueOf(intArray[i]);
                return stringArray;
            case Codable.LONG_TYPE:
                return String.valueOf(table.longAt(row, column));
            case Codable.LONG_ARRAY_TYPE:
                longArray = table.longArrayAt(row, column);
                if (longArray == null)
                    return null;
                count = longArray.length;
                stringArray = new String[count];
                for (i = 0; i < count; i++)
                    stringArray[i] = String.valueOf(longArray[i]);
                return stringArray;
            case Codable.FLOAT_TYPE:
                return String.valueOf(table.floatAt(row, column));
            case Codable.FLOAT_ARRAY_TYPE:
                floatArray = table.floatArrayAt(row, column);
                if (floatArray == null)
                    return null;
                count = floatArray.length;
                stringArray = new String[count];
                for (i = 0; i < count; i++)
                    stringArray[i] = String.valueOf(floatArray[i]);
                return stringArray;
            case Codable.DOUBLE_TYPE:
                return String.valueOf(table.doubleAt(row, column));
            case Codable.DOUBLE_ARRAY_TYPE:
                doubleArray = table.doubleArrayAt(row, column);
                if (doubleArray == null)
                    return null;
                count = doubleArray.length;
                stringArray = new String[count];
                for (i = 0; i < count; i++)
                    stringArray[i] = String.valueOf(doubleArray[i]);
                return stringArray;
            case Codable.STRING_TYPE:
                return table.stringAt(row, column);
            case Codable.STRING_ARRAY_TYPE:
                return table.stringArrayAt(row, column);
            case Codable.OBJECT_TYPE:
                return nameForId(table.identifierAt(row, column));
            case Codable.OBJECT_ARRAY_TYPE:
                intArray = table.identifierArrayAt(row, column);
                if (intArray == null)
                    return null;
                count = intArray.length;
                stringArray = new String[count];
                for (i = 0; i < count; i++)
                    stringArray[i] = nameForId(intArray[i]);
                return stringArray;
            default:
                throw new InconsistencyException("Unknown field type: " +
                    table.fieldTypes[column]);
        }
    }

    static byte hexChar[] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    static String bytesString(byte bytes[]) {
        int i, count, outCount;
        byte b;
        byte buf[];

        if (bytes == null)
            return null;

        if (bytes.length == 0)
            return "";

        count = bytes.length;
        buf = new byte[2 * count + (count / 4) + 1];
        outCount = 0;

        for (i = 0; i < count; i++) {
            b = bytes[i];
            buf[outCount++] = hexChar[(b >>> 4) & 0xf];
            buf[outCount++] = hexChar[b & 0xf];
            if (((i + 1) % 4) == 0)
                buf[outCount++] = ' ';
        }

        if (buf[outCount - 1] == ' ')
            outCount--;

        return new String(buf, 0, 0, outCount);
    }

    void saveRoots() {
        int i;
        String rootsArray[];

        rootsArray = new String[archive.rootCount];

        for (i = 0; i < archive.rootCount; i++)
            rootsArray[i] = nameForId(archive.roots[i]);

        archiveDict.put(rootInstancesKey, rootsArray);
    }
}
