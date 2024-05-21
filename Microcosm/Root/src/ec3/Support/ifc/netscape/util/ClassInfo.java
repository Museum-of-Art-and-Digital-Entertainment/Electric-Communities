// ClassInfo.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** Object subclass describing the schema for a class' encoding. When a Codable
  * object's <b>describeClassInfo()</b> method is invoked, the Codable object
  * must populate the ClassInfo instance with its class name, version, field
  * names, and field types:
  *
  * <pre>
  *     public void describeClassInfo(ClassInfo info) {
  *         super.describeClassInfo(info);
  *
  *         info.addClass("MyView", 1);
  *         info.addField("someObject", OBJECT_TYPE);
  *         info.addField("someString", STRING_TYPE);
  *         info.addField("someInt", INT_TYPE);
  *     }
  * </pre>
  *
  * Each class in the class hierarchy supplies information only about itself.
  * All subclasses should call <b>super.describeClassInfo()</b> to allow each
  * superclass to add additional class name/class version pairs and field
  * name/field type pairs.
  * @note 1.0 Removed builder specific properties, thereby removing dependency
  *           upon the BuilderInfo object
  */
public class ClassInfo {
    String className;

    int classCount;
    String classNames[];
    int classVersions[];

    int fieldCount;
    String fieldNames[];
    byte fieldTypes[];

    // We need to store the most derived class because it needs to be
    // preserved, and may not be in the inheritance path.

    /** Constructs a ClassInfo instance for the class named <b>className</b>.
      * The class' name is the most derived class of the object for which the
      * ClassInfo is being built.
      */
    public ClassInfo(String className) {
        super();
        this.className = className;
        classNames = new String[8];
        classVersions = new int[8];
        fieldNames = new String[24];
        fieldTypes = new byte[24];
    }

    /** Adds an additional class name/class version pair. This information
      * can be used during decoding to bring forward old encodings at run
      * time.
      * @see Decoder#getVersion
      */
    public void addClass(String className, int version) {
        ensureClassCapacity(classCount);
        classNames[classCount] = className;
        classVersions[classCount] = version;
        classCount++;
    }

    /** Adds a field name/field type pair.
      */
    public void addField(String fieldName, byte fieldType) {
        ensureFieldCapacity(fieldCount);
        fieldNames[fieldCount] = fieldName;
        fieldTypes[fieldCount] = fieldType;
        fieldCount++;
    }

    /** Returns the name of the most derived class for this ClassInfo.
      */
    public String className() {
        return className;
    }

    /** Returns the number of classes which have been added with
      * <b>addClass()</b>.
      * @see #addClass
      */
    public int classCount() {
        return classCount;
    }

    /** Returns an array of the class names added with <b>addClass()</b>.
      * @see #addClass
      */
    public String[] classNames() {
        String tmp[];

        tmp = new String[classCount];
        System.arraycopy(classNames, 0, tmp, 0, classCount);

        return tmp;
    }

    /** Returns an array parallel to <b>classNames()</b> with corresponding
      * versions added with <b>addClass()</b>.
      * @see #classNames
      * @see #addClass
      */
    public int[] classVersions() {
        int tmp[];

        tmp = new int[classCount];
        System.arraycopy(classVersions, 0, tmp, 0, classCount);

        return tmp;
    }

    /** Returns the number of fields added with <b>addField()</b>.
      * @see #addField
      */
    public int fieldCount() {
        return fieldCount;
    }

    /** Returns an array of all the field names added with <b>addField()</b>.
      * @see #addField
      */
    public String[] fieldNames() {
        String tmp[];

        tmp = new String[fieldCount];
        System.arraycopy(fieldNames, 0, tmp, 0, fieldCount);

        return tmp;
    }

    /** Returns an array parallel to <b>fieldNames()</b> of all the field types
      * added with <b>addField()</b>.
      * @see #fieldNames
      * @see #addField
      */
    public byte[] fieldTypes() {
        byte tmp[];

        tmp = new byte[fieldCount];
        System.arraycopy(fieldTypes, 0, tmp, 0, fieldCount);

        return tmp;
    }

    protected void ensureClassCapacity(int cap) {
        int newCap, oldLen;
        String newInheritancePath[];
        int newClassVersions[];

        if (cap < classNames.length)
            return;

        if (classNames.length == 0)
            newCap = 8;
        else
            newCap = 2 * classNames.length;

        while (newCap < cap)
            newCap = 2 * newCap;

        oldLen = classNames.length;
        newInheritancePath = new String[newCap];
        newClassVersions = new int[newCap];

        System.arraycopy(classNames, 0, newInheritancePath, 0, oldLen);
        System.arraycopy(classVersions, 0, newClassVersions, 0, oldLen);

        classNames = newInheritancePath;
        classVersions = newClassVersions;
    }

    protected void ensureFieldCapacity(int cap) {
        String  newFieldNames[];
        int     newCap, oldLen;
        byte    newFieldTypes[];

        newCap = fieldCapacityFor(cap);
        if(newCap < 0)
            return;

        oldLen = fieldNames.length;
        newFieldNames = new String[newCap];
        newFieldTypes = new byte[newCap];

        System.arraycopy(fieldNames, 0, newFieldNames, 0, oldLen);
        System.arraycopy(fieldTypes, 0, newFieldTypes, 0, oldLen);

        fieldNames = newFieldNames;
        fieldTypes = newFieldTypes;
    }

    public int fieldCapacityFor(int cap)    {
        int newCap = 0;

        if (cap < fieldNames.length)
            return -1;

        if (fieldNames.length == 0)
            newCap = 24;
        else
            newCap = 2 * fieldNames.length;

        while (newCap < cap)
            newCap = 2 * newCap;

        return newCap;
    }
}
