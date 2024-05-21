package ec.e.inspect;

/** The file ECReflect.java attempt to mimic the functionality in the
    Jave 1.1. Reflection API.  Functionality and names match wherever
    possible.  The main discrepancies are some extensions to Class
    Class (that have been put into InspectableClass as static methods
    explicitly called using the class in question as first argument)
    and the classes ec.e.inspect.Null, ec.e.inspect.Short, and
    ec.e.inspect.Byte that mimic functionality of similarly named
    classes in the java.lang package.
    */

class Null {
    public String toString() { return "null"; }
    public Object nullValue() { return null; }
}

/** There is no class java.lang.Short - This class ec.e.inspect.Short is a poor substitute
  */

public class Short extends Number {
    int value;
    public Short(int n) { value = n; }
    public Short(String s) { value = Integer.valueOf(s).intValue(); }
    public short shortValue() { return (short) value; }
    public int intValue() { return (int) value; }
    public long longValue() { return (long) value; }
    public float floatValue() { return (float) value; }
    public double doubleValue() { return (double) value; }
    public String toString() { return Integer.toString(value); }
}

/** There is no class java.lang.Byte - This class ec.e.inspect.Byte is a poor substitute
  */

public class Byte extends Number {
    int value;
    public Byte(int n) { value = n; }
    public Byte(String s) { value = Integer.valueOf(s).intValue(); }
    public byte byteValue() { return (byte) value; }
    public int intValue() { return (int) value; }
    public long longValue() { return (long) value; }
    public float floatValue() { return (float) value; }
    public double doubleValue() { return (double) value; }
    public String toString() { return Integer.toString(value); }
}

/** In 1.1, classes are used as type identifiers. The primitive data
  types (boolean, byte, char etc) are often "wrapped" in special
  classes (Boolean, Byte, Character etc) whenever we need to pass
  their values around freely as bona fide Objects. We keep a number of
  final variables around with the appropriate associations of wrapper
  class, signature, and human-readable string for all primitive data
  types.

  For bona fide Objects we can trivially find the signature etc with
  existing methods on class Class.

  For Arrays this however breaks down completely. In 1.1, the JVM
  hooks allows you to determine the data types and signatures for
  elements of the array (which gets especially interesting for
  multidimensional arrays). We can only fake it here by using the same
  simple int array for *all* arrays. We special case this in the
  places where we need the information since that's all we can do.
  */

public final class InspectorType {
    static final public InspectorType BOOLEAN = new InspectorType (new Boolean(false).getClass(),"Z", "boolean");
    static final public InspectorType BYTE    = new InspectorType (new Byte(0).getClass(),"B", "byte");
    static final public InspectorType CHAR    = new InspectorType (new Character(' ').getClass(),"C", "char");
    static final public InspectorType DOUBLE  = new InspectorType (new Double(0.0).getClass(),"D", "double");
    static final public InspectorType FLOAT   = new InspectorType (new Float(0.0).getClass(),"F", "float");
    static final public InspectorType INT     = new InspectorType (new Integer(0).getClass(),"I", "int");
    static final public InspectorType LONG    = new InspectorType (new Long(0).getClass(),"J", "long");
    static final public InspectorType SHORT   = new InspectorType (new Short(0).getClass(),"S", "short");
    static final public InspectorType STRING  = new InspectorType (new String().getClass(),"Ljava.lang.String;", "string");
    static final public InspectorType OBJECT  = new InspectorType (new Object().getClass(),"Ljava.lang.Object;", "object");
    static final public InspectorType CLASS   = new InspectorType (new Object().getClass().getClass(),"Ljava.lang.Class;","class");

    // These are too simpleminded. We use only the strings.

    static final public InspectorType ARRAY   = new InspectorType (new int[1].getClass(),"[I","array");
    static final public InspectorType VOID    = new InspectorType (new Object().getClass(),"V","void");
//  static final public InspectorType ENUM    = new InspectorType (new Enum().getClass(),"E", "enum");
    static final public InspectorType FUNCTION= new InspectorType (new Object().getClass(),"(","function");
    static final public InspectorType ANY     = new InspectorType (new Object().getClass(),"A","any");

    private Class ourClass;
    private String signature;
    private String label;

    private InspectorType(Class ourClass, String signature, String label) {
        this.ourClass = ourClass; 
        this.signature = signature;
        this.label = label;
    }

    public Class getInspectorTypeClass() { return ourClass; }
    public String toString() { return label; }
    public String getSignature() { return signature; }
}

/** Class Modifier manages the modifiers for variables and methods -
public/private/protected, final, synchronized, native, abstract, etc.
There is an integer value in the class object that packs bits
designating all the modifiers. Class Modifier provides a set of
boolean methods to interrogate those bits. */

class Modifier {

    // Constant values modeled after C constants found in java/include/tree.h

    public final static int PUBLIC = 1;
    public final static int PRIVATE = 2;
    public final static int PROTECTED = 4;
    public final static int STATIC = 8; 

    public final static int FINAL = 0x0010;
    public final static int SYNCHRONIZED = 0x0020;
    public final static int THREADSAFE = 0x0040;
    public final static int TRANSIENT = 0x0080;

    public final static int NATIVE = 0x0100;
    public final static int INTERFACE = 0x0200;
    public final static int ABSTRACT = 0x0400; 

    //  public final static int VOLATILE = 0;

    public final static boolean isAbstract(int modifiers) {return ((modifiers & ABSTRACT) != 0); }
    public final static boolean isFinal(int modifiers) {return ((modifiers & FINAL) != 0); }
    public final static boolean isInterface(int modifiers) {return ((modifiers & INTERFACE) != 0); }
    public final static boolean isNative(int modifiers) {return ((modifiers & NATIVE) != 0); }
    public final static boolean isPrivate(int modifiers) {return ((modifiers & PRIVATE) != 0); }
    public final static boolean isProtected(int modifiers) {return ((modifiers & PROTECTED) != 0); }
    public final static boolean isPublic(int modifiers) {return ((modifiers & PUBLIC) != 0); }
    public final static boolean isStatic(int modifiers) {return ((modifiers & STATIC) != 0); }
    public final static boolean isSynchronized(int modifiers) {return ((modifiers & SYNCHRONIZED) != 0); }
    public final static boolean isTransient(int modifiers) {return ((modifiers & TRANSIENT) != 0); }
    // public final static boolean isVolatile(int modifiers) {return ((modifiers & VOLATILE) != 0); }
    // public final static boolean isThreadSafe(int modifiers) {return ((modifiers & THREADSAFE) != 0); }

    public String modifiersString(int modifiers) {  // Ordered as in [Steele, p. 144]
        return (isPrivate(modifiers)?"private ":(isPublic(modifiers)?"public ":"")) + (isFinal(modifiers)?"final ":"") +
          (isStatic(modifiers)?"static ":"") + (isTransient(modifiers)?"transient ":"")
          //+ (isVolatile(modifiers)?"volatile ":"")
          ;
    }
}

/** A Field object carries type information about an instance variable
  in an object.  It is derived from the class object (hence no actual
  instance is needed). In Java 1.1. Field objects can be retrieved
  from hooks in the JVM. */

public class Field {
    private Class declaringClass;   // The class of the object we are defined in
    private int index;      // Index to variable info in that class object
    private int modifiers;  // Modifiers for the inspected object we are defined in
    private Class type;     // The class object identifying the datatype of this variable (slot)
    private String name;        // The name of this variable (slot)

    Field(Class declaringClass, int index, int modifiers, Class type, String name) {
        this.type = type;
        this.declaringClass = declaringClass;
        this.index = index;
        this.modifiers = modifiers;
        this.name = name;
    }

    public Class getDeclaringClass() { return declaringClass; }
    public int getModifiers() { return modifiers; }
    public Class getType() { return type; }
    public String getName() { return name; }
    public String getDeclaredSignature() { // Not in 1.1 reflection API
        return InspectableClass.getFieldSignature(declaringClass,index);
    }

    /** The Declared Signature of a field is the signature according to the declarations in the class.
      This can differ from the Assigned Signature, which is the signature of the actual value
      in cases where the assigned value is actually a subclass of the declared object type.
      Note that you need an instance to get the assigned signature but don't need it to get the
      declared signature.
      */

    /** Returns the actual signature of the variable assigned to the given field in the given object.
      Not part of Java 1.1 Reflection API
     */

    public String getAssignedSignature(Object object) { // Note - this object is the *current* object
        if (object == null) return "N"; // Null signature
        String declared = getDeclaredSignature();

        // For reference types we attempt to return the actually assigned signature

        if ((declared.charAt(0) != 'L') && (object != null)) return InspectableClass.getAssignedSignature(get(object));
        if ((declared.charAt(0) != '[') && (object != null)) return InspectableClass.getAssignedSignature(get(object));
        return declared;        // All primitive data types
    }    

    public String toString() { return name; } // Maybe this should be more human-readable?

    /** The getYadaYadaYada() methods return values of the appropriate data
type. You must already know what data type something is before asking
for it using the appropriate method (since a method call in Java is
typesafe). Since a Field is created using information in the Class
only (not an instance), you must supply an instance of the object to
get the values from. In other words, the Field object describes the
slot in the instance, and if you want the value of a slot of an
object, then Field.get(Object o) will return the value of the slot
described by Field in Object o.  */

    /** The same holds for the Set() methods. */

    public Object get(Object iObject) {
        if (iObject == null) return null;
        return InspectableClass.get(declaringClass,iObject,index);
    }

    /** Set the value for a slot (described by Field) in an
        object. The new value may be a wrapped primitive data type */

    public void set(Object iObject, Object newValue) {
        //  System.out.println("Set(" + declaringClass + "," + iObject + "," + index + "," +
        //                     newValue + "(a " + newValue.getClass() + "))");
        if (newValue instanceof Boolean)
            InspectableClass.setBooleanFieldValue(declaringClass,  iObject, index, ((Boolean)newValue).booleanValue());
        else if (newValue instanceof Byte)
            InspectableClass.setByteFieldValue(declaringClass,  iObject, index, ((Byte)newValue).byteValue());
        else if (newValue instanceof Character)
            InspectableClass.setCharFieldValue(declaringClass,  iObject, index, ((Character)newValue).charValue());
        else if (newValue instanceof Short)
            InspectableClass.setShortFieldValue(declaringClass, iObject, index, ((Short)newValue).shortValue());
        else if (newValue instanceof Integer)
            InspectableClass.setIntFieldValue(declaringClass,   iObject, index, ((Integer)newValue).intValue());
        else if (newValue instanceof Long)
            InspectableClass.setLongFieldValue(declaringClass,  iObject, index, ((Long)newValue).longValue());
        else if (newValue instanceof Float)
            InspectableClass.setFloatFieldValue(declaringClass, iObject, index, ((Float)newValue).floatValue());
        else if (newValue instanceof Double)
            InspectableClass.setDoubleFieldValue(declaringClass,iObject,index, ((Double)newValue).doubleValue());
        else {                      // instanceof Object. No point in checking for that!
            InspectableClass.setFieldValue(declaringClass, iObject, index, newValue);
        }
    }
}

/** InspectableClass contains stuff that will be in in class Class in
  Java 1.1.  Most methods here call on native code to access in-core
  information in the class definitions. In 1.1 this will be done with
  hooks to the JVM. 

  Some methods here have no counterpart in Java 1.1 and should
  possibly be moved elsewhere but realistically we won't change this
  until we actually move to true 1.1. Reflection.  */

public class InspectableClass {

    private static final int requirednativeLibraryVersion = 4; // Minimum required version of native library

    /** Internal-use methods, not part of Java 1.1. Core Reflection API. */

    private static native int inspectorNativeLibraryVersion();

    /** These methods will be in class Class in Java 1.1 - We use
        static methods on InspectableClass until then. */

    /** Returns the number of fields in a class, ignoring fields inherited from superclasses.
      Note, that for now getNumberFields only returns the number of non-static instance variable fields */

    public native static int getNumberFields(Class declaringClass); // Returns number of fields for class

    /** Returns the number of fields in a class, including fields inherited from superclasses (except Object).
      Note, that for now getTotalNumberFields only returns the number of non-static instance variable fields */

    public native static int getTotalNumberFields(Class declaringClass); // Returns number of fields for class and superclasses

    /** Note, that for now getFieldModifiers only returns the
        modifiers for non-static instance variable fields */

    public native static int getFieldModifiers(Class declaringClass, int i); // returns modifiers for field

    /** Note, that for now getFieldSignature only returns the
        signature for non-static instance variable fields */

    public native static String getFieldSignature(Class declaringClass, int i); // returns field signature string

    /** Note, that for now getFieldSignature only returns the
        names of non-static instance variable fields */

    public native static String getFieldName(Class declaringClass, int i); // returns one field name

    private static boolean inspectLibLoaded = false;

    /** Low-level getter. Note, that for now getFieldValue only
        returns the values for non-static instance variable fields */

    public native static Object getFieldValue(Class declaringClass, Object iObject, int i); // returns one (possibly wrapped) value

    /** Low-level getters for arrays. Note, that for now
        getFieldXXXValue only returns the values for non-static
        instance variable fields */

    public native static boolean[] getFieldBooleanArrayValue(Class declaringClass,Object iObject,int i); // returns value as array
    public native static char[] getFieldCharArrayValue(Class declaringClass, Object iObject, int i); // returns value as array
    public native static byte[] getFieldByteArrayValue(Class declaringClass, Object iObject, int i); // returns value as array
    public native static short[] getFieldShortArrayValue(Class declaringClass, Object iObject, int i); // returns value as array
    public native static int[] getFieldIntArrayValue(Class declaringClass, Object iObject, int i); // returns value as array
    public native static long[] getFieldLongArrayValue(Class declaringClass, Object iObject, int i); // returns value as array
    public native static float[] getFieldFloatArrayValue(Class declaringClass, Object iObject, int i); // returns value as array
    public native static double[] getFieldDoubleArrayValue(Class declaringClass, Object iObject, int i); // returns value as array
    public native static Object[] getFieldArrayValue(Class declaringClass, Object iObject, int i); // returns value as array

    /** Low-level setters. Note, that for now set-XXX-FieldValue only
        sets the values for non-static instance variable fields */

    public native static void setBooleanFieldValue(Class declaringClass, Object iObject, int i, boolean val);
    public native static void setByteFieldValue(Class declaringClass, Object iObject, int i, byte val);
    public native static void setCharFieldValue(Class declaringClass, Object iObject, int i, char val);
    public native static void setShortFieldValue(Class declaringClass, Object iObject, int i, short val);
    public native static void setIntFieldValue(Class declaringClass, Object iObject, int i, int val);
    public native static void setLongFieldValue(Class declaringClass, Object iObject, int i, long val);
    public native static void setFloatFieldValue(Class declaringClass, Object iObject, int i, float val);
    public native static void setDoubleFieldValue(Class declaringClass, Object iObject, int i, double val);
    public native static void setFieldValue(Class declaringClass, Object iObject, int i, Object val); // All reference types

    public static Object get(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        Object result = getFieldValue(declaringClass,iObject,i);
        //      System.out.println("GetFieldValue returned " + ((result == null)?"null":result));
        return result;
    }

    public static Object[] getArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldArrayValue(declaringClass,iObject,i);
    }

    public static boolean[] getBooleanArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldBooleanArrayValue(declaringClass,iObject,i);
    }

    public static byte[] getByteArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldByteArrayValue(declaringClass,iObject,i);
    }

    public static char[] getCharArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldCharArrayValue(declaringClass,iObject,i);
    }

    public static short[] getShortArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldShortArrayValue(declaringClass,iObject,i);
    }

    public static int[] getIntArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldIntArrayValue(declaringClass,iObject,i);
    }

    public static long[] getLongArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldLongArrayValue(declaringClass,iObject,i);
    }

    public static float[] getFloatArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldFloatArrayValue(declaringClass,iObject,i);
    }

    public static double[] getDoubleArray(Class declaringClass, Object iObject, int i) {
        if (declaringClass == null) return null;
        return getFieldDoubleArrayValue(declaringClass,iObject,i);
    }

    public static void setIntValue(Class declaringClass, Object iObject, int i, int newValue) {
        if (declaringClass == null) return;
        setIntFieldValue(declaringClass,iObject,i,newValue);
    }

    /** getInspectableObject - A method set to wrap primitive objects to
      real Objects of the appropriate class.  These are called from
      GetFieldValue native method.  Some of these (Null, Short, and
      Byte) are currently in ec.e.inspect rather than java.lang package.
      They should probably be made private. */

    public static Object getInspectableObject(boolean x)    { 
        //      System.out.println("Attempting to create a boolean for " + x);
        return new Boolean(x); }

    public static Object getInspectableObject(byte x)       { return new Byte(x); }
    public static Object getInspectableObject(char x)       { return new Character(x); }
//  public static Object getInspectableObject(enum x)       { return new Enum(x); }
    public static Object getInspectableObject(short x)      { return new Short(x); }
    public static Object getInspectableObject(int x)        { return new Integer(x); }
    public static Object getInspectableObject(long x)       { return new Long(x); }
    public static Object getInspectableObject(float x)      { return new Float(x); }
    public static Object getInspectableObject(double x)     { return new Double(x); }
    public static Object getInspectableObject(Object x)     { 
        //      System.out.println("Creating an object of type " + x);
        return (x == null)?new Null():x; }

    /** Get the actual signature of some data. */

    public static String getAssignedSignature(boolean x)    { return "Z"; }
    public static String getAssignedSignature(byte x)       { return "B"; }
    public static String getAssignedSignature(char x)       { return "C"; }
//  public static String getAssignedSignature(enum x)       { return "E"; }
    public static String getAssignedSignature(short x)      { return "S"; }
    public static String getAssignedSignature(int x)        { return "I"; }
    public static String getAssignedSignature(long x)       { return "J"; }
    public static String getAssignedSignature(float x)      { return "F"; }
    public static String getAssignedSignature(double x)     { return "D"; }
    public static String getAssignedSignature(Object iObject) {
        return Inspector.getObjectSignature(iObject);
    }

    /** Method added to make startup more robust. Not part of Sun Reflection API */

    public static boolean inspectorExists() { return inspectLibLoaded; }

    /** Given a signature, return a class object. We use a set of
      constants for all wrapped data types. */

    public static Class classForSignature(String signature) {
        if (signature == null) return null;

        try {
            switch (signature.charAt(0)) {
            case 'V': return InspectorType.VOID.getInspectorTypeClass();
            case 'Z': return InspectorType.BOOLEAN.getInspectorTypeClass();
            case 'B': return InspectorType.BYTE.getInspectorTypeClass();
            case 'C': return InspectorType.CHAR.getInspectorTypeClass();
                // case 'E': return InspectorType.ENUM.getInspectorTypeClass();
            case 'S': return InspectorType.SHORT.getInspectorTypeClass();
            case 'I': return InspectorType.INT.getInspectorTypeClass();
            case 'J': return InspectorType.LONG.getInspectorTypeClass();
            case 'F': return InspectorType.FLOAT.getInspectorTypeClass();
            case 'D': return InspectorType.DOUBLE.getInspectorTypeClass();
            case 'L':   return Class.forName(signature.substring(1,signature.length() - 1));

                // The next one is a bad case - We always return the class of an array of integer.
                // We don't fix it here (maybe we should) - We attempt to special case it wherever it matters
                // We need to synthesize a class on the fly, depending on the array content type . Not that easy to do.

            case '[': return InspectorType.ARRAY.getInspectorTypeClass();

                // Don't yet know what to do about these

                //      case '(': return InspectorType.FUNCTION.getInspectorTypeClass();
                //      case 'A': return InspectorType.ANY.getInspectorTypeClass(); 
            }
        } catch (ClassNotFoundException e) {};
        return null;
    }

    public static Class getFieldType(Class declaringClass, int i) { // returns field type as a class
        String signature = getFieldSignature(declaringClass,i);
        Class result = classForSignature(signature);
        if (result != null) return result;
        return InspectorType.ANY.getInspectorTypeClass(); // Random default case, shouldn't happen.
    }

    public static boolean fieldIsArray(Class declaringClass, int i) {
        String signature = getFieldSignature(declaringClass,i);
        if (signature.charAt(0) == '[') return true;
        else return false;
    }

    public static boolean fieldIsPrimitive(Class declaringClass, int i) {
        String signature = getFieldSignature(declaringClass,i);
        if ((signature.charAt(0) == '[') ||
            (signature.charAt(0) == 'A') ||
            (signature.charAt(0) == 'L')) return false;
        else return true;
    }

    /** getFields() uses the native methods defined above to get at
      the low-level inspectable data and returns those as an array of
      Field objects. This is the main method to analyze an Object. It
      simply calls Object.getClass() on the object and analyzes that
      class using Class.getFields() (actually, for now, InspectableClass.getFields())*/

    public static Field[] getFields(Object iObject) {
        Class declaringClass = iObject.getClass();
        String signature = ((Object)iObject).toString();
        if (signature.charAt(0) == '[') return null; // An array.
        return getFields(declaringClass);
    }
    
    /** getFields() uses the native methods defined above to get at
      the low-level inspectable data and returns those as an array of
      Field objects. This is the main method to analyze a Class and
      will live in Class Class in Java 1.1 */

    public static Field[] getFields(Class declaringClass) {
        if (declaringClass == null) return null;
        if (!inspectLibLoaded) return null;
        int n = getTotalNumberFields(declaringClass);
        if (n < 0) return null;
        Field[] fields = new Field[n];
        String name;
        int modifiers;
        Class type;

        for (int i=0;i<n;i++) {
            String signature = getFieldSignature(declaringClass,i);
            modifiers = getFieldModifiers(declaringClass, i);
            type = getFieldType(declaringClass, i);
            name = getFieldName(declaringClass, i);
            fields[i] = new Field(declaringClass, i, modifiers, type, name);
        }
        return fields;
    }
    
    static { 
        try {
            System.loadLibrary("inspect");   // Load in our native methods at load time.
            int libVersion = inspectorNativeLibraryVersion();
            System.out.println("Inspector library is version " + libVersion);
            if (libVersion < requirednativeLibraryVersion)
                throw new Error("Inspector native library is an old version - please update it to version " +
                                requirednativeLibraryVersion);
            inspectLibLoaded = true;
        } catch (Throwable e) {
            System.out.println("Could not load inspector library - inspector disabled:" + e);
            Inspector.enableGathering(false); // Don't even bother gathering objects now
        }
    }
}
    
