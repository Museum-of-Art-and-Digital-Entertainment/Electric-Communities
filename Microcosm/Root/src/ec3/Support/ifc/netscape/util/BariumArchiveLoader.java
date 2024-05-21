// BariumArchiveLoader.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

import java.io.*;

/** This is just for migration purposes.
  * @private
  */
public class BariumArchiveLoader {
    static final String classVersionsKey = "classVersions";
    static final String classTablesKey = "classTables";
    static final String fieldNamesKey = "fieldNames";
    static final String fieldTypesKey = "fieldTypes";
    static final String instancesKey = "instances";
    static final String classNameKey = "class";
    static final String rootInstancesKey = "rootInstances";

    Archive archive;
    Hashtable archiveDict;
    Hashtable allVersions;

    IdHashtable idForName;
    String nameForId[];

    public BariumArchiveLoader(Archive archive) {
        super();
        this.archive = archive;
    }

    public void readASCII(InputStream in) throws CodingException,
        DeserializationException, IOException {
        Deserializer deserializer = null;

        if( in instanceof Deserializer) {
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

        // Load the pieces of the archive file.

        loadVersions();
        loadClassTables();
        loadInstanceData();
        loadRoots();
    }

    void loadVersions() {
        Hashtable versionDict;
        Enumeration classNameEnum;
        String className, versionString;
        int version;

        versionDict = (Hashtable)archiveDict.get(classVersionsKey);
        if (versionDict == null)
            return;

        allVersions = new Hashtable(versionDict.count());
        classNameEnum = versionDict.keys();

        while (classNameEnum.hasMoreElements()) {
            className = (String)classNameEnum.nextElement();
            versionString = (String)versionDict.get(className);
            version = Integer.parseInt(versionString);
            allVersions.put(className, new Integer(version));
        }
    }

    void loadClassTables() throws CodingException {
        Hashtable allTablesDict, tableDict;
        Enumeration classNameEnum;
        String className;
        Object fieldNamesArray[], fieldTypesArray[];
        String fieldNames[];
        byte fieldTypes[];
        ClassTable table;
        int i, count;
        ClassInfo info;

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

            fieldNamesArray = (Object[])tableDict.get(fieldNamesKey);
            fieldNames = new String[fieldNamesArray.length];
            System.arraycopy(fieldNamesArray, 0, fieldNames, 0, fieldNames.length);

            fieldTypesArray = (Object[])tableDict.get(fieldTypesKey);
            fieldTypes = fieldTypesForNames(fieldTypesArray);

            info = new ClassInfo(className);

            count = fieldNames.length;
            for (i = 0; i < count; i++) {
                info.addField(fieldNames[i], fieldTypes[i]);
            }

            // Take a guess at the versions.
            guessAtVersions(className, info);

            table = new ClassTable(archive, info);
            archive.addClassTable(table);
        }
    }

    void guessAtVersions(String className, ClassInfo info) {
        Class cls;
        Vector classNames;
        int i;
        Integer version;

        version = (Integer)allVersions.get(className);
        if (version != null)
            info.addClass(className, version.intValue());

        classNames = new Vector();

        try {
            cls = Class.forName(className);
            while (cls != null) {
                classNames.addElement(cls.getName());
                cls = cls.getSuperclass();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("unable to find class: " + className);
            return;
        }

        // skip element 0 since we did that up front.

        i = classNames.count();
        while (--i > 0) {
            className = (String)classNames.elementAt(i);
            version = (Integer)allVersions.get(className);
            if (version != null)
                info.addClass(className, version.intValue());
        }
    }

    static byte[] fieldTypesForNames(Object fieldTypesArray[])
        throws CodingException {

        int i, count;
        byte fieldTypes[];
        String typeName;

        count = fieldTypesArray.length;
        fieldTypes = new byte[count];

        for (i = 0; i < count; i++) {
            typeName = (String)fieldTypesArray[i];
            fieldTypes[i] = typeForName(typeName);
        }

        return fieldTypes;
    }

    static byte typeForName(String typeName) throws CodingException {
        typeName = typeName.toLowerCase();

        if (typeName.equals("boolean"))
            return Codable.BOOLEAN_TYPE;
        else if (typeName.equals("byte"))
            return Codable.BYTE_TYPE;
        else if (typeName.equals("short"))
            return Codable.SHORT_TYPE;
        else if (typeName.equals("int"))
            return Codable.INT_TYPE;
        else if (typeName.equals("long"))
            return Codable.LONG_TYPE;
        else if (typeName.equals("float"))
            return Codable.FLOAT_TYPE;
        else if (typeName.equals("double"))
            return Codable.DOUBLE_TYPE;
        else if (typeName.equals("string"))
            return Codable.STRING_TYPE;
        else if (typeName.equals("byte_array"))
            return Codable.BYTE_ARRAY_TYPE;
        else if (typeName.equals("object"))
            return Codable.OBJECT_TYPE;
        else if (typeName.equals("object_array"))
            return Codable.OBJECT_ARRAY_TYPE;
        else
            throw new CodingException("unknown type name: " + typeName);
    }

    void loadInstanceData() throws CodingException {
        Hashtable allInstancesDict, instanceDict;
        Enumeration nameEnum;
        String name, className;
        ClassTable table;
        int id, row;

        allInstancesDict = (Hashtable)archiveDict.get(instancesKey);
        if (allInstancesDict == null)
            return;

        // In the ASCII version of the archive we need to make two passes over
        // the instances.  The first pass creates a row for each instance and
        // builds the name to id mapping.

        nameEnum = allInstancesDict.keys();

        while (nameEnum.hasMoreElements()) {
            name = (String)nameEnum.nextElement();
            instanceDict = (Hashtable)allInstancesDict.get(name);

            if (idForName.get(name) != IdHashtable.NOT_FOUND)
                throw new CodingException("duplicate instance name: "+name);

            className = (String)instanceDict.get(classNameKey);
            if (className == null || className.equals(""))
                throw new CodingException(
                    "missing className for instance: " + name);

            table = archive.classTableForName(className);
            if (table == null)
                throw new CodingException("bad class name for instance: " +
                    name);

            id = table.newIdentifier();
            idForName.putKnownAbsent(name, id);
        }

        // The second pass over the instances actually loads the data for each
        // instance into the row.

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
            if (value instanceof Object[])
                setColumnFromArray(table, row, i, (Object[])value);
            else if ((value instanceof String) && !((String)value).equals(""))
                setColumnFromString(table, row, i, (String)value);
        }
    }

    void setColumnFromArray(ClassTable table, int row, int column,
        Object[] value) throws CodingException {

        int i, id;
        int array[];

        if (table.fieldTypes[column] != Codable.OBJECT_ARRAY_TYPE)
            throw new CodingException("bad data for field " +
                table.fieldNames[column]);

        array = new int[value.length];

        for (i = 0; i < array.length; i++) {
            id = idForName.get(value[i]);
            array[i] = id;
        }

        table.setIdentifierArrayAt(row, column, array);
    }

    void setColumnFromString(ClassTable table, int row, int column,
        String value) throws NumberFormatException, CodingException {

        switch (table.fieldTypes[column]) {
            case Codable.BOOLEAN_TYPE:
                if (value.equalsIgnoreCase("true"))
                    table.setBooleanAt(row, column, true);
                else if (value.equalsIgnoreCase("false"))
                    table.setBooleanAt(row, column, false);
                else
                    throw new CodingException("Invalid boolean value");
                break;
            case Codable.BYTE_TYPE:
                table.setByteAt(row, column, (byte)Integer.parseInt(value));
                break;
            case Codable.SHORT_TYPE:
                table.setShortAt(row, column, (short)Integer.parseInt(value));
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
            case Codable.BYTE_ARRAY_TYPE:
                table.setByteArrayAt(row, column, bytesFromString(value));
                break;
            case Codable.OBJECT_TYPE:
                table.setIdentifierAt(row, column, idForName.get(value));
                break;
            default:
                throw new CodingException("unknown type " +
                    table.fieldTypes[column]);
        }
    }

    byte[] bytesFromString(String value) {
        int i, count, nibble, outCount;
        char ch;
        byte buf[], tmp[];

        if (value == null || value.equals(""))
            return null;

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
            throw new CodingException("no root instances");

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
}
