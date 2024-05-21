package ec.e.openers;

import java.io.IOException;
import java.util.Hashtable;

/** 
 * An Opener for non-scalar non-arrays.  An ObjectOpener violates the
 * encapsulation of the objects it opens, and hence grants mucho
 * authority.  Further, ObjectOpeners beget ObjectOpeners, so an
 * ObjectOpener grants authority to violate the encapsulation of a set
 * of objects.  Outside the TCB, one only gets ObjectOpeners from
 * other ObjectOpeners, and these new Openers have the same authority
 * and "point of view" as the Opener they derive from.  This is known
 * as Opener contagion. <p>
 *
 * ObjectOpener is abstract.  By overridding isOk, subclasses define
 * which instance variable declarations "exist" in the world as
 * presented by this Opener.  On encoding, variables that aren't ok are
 * ignored.  <p>
 *
 * XXX not true: On decode, they are set according to their class's
 * default (no argument) constructor.  <p>
 *
 * XXX unfortunately true instead: On decode, reference veriables are
 * set to null, and scalar variables are set to the zero value for
 * their type. 
 */
public abstract class ObjectOpener extends RefOpener {

    private ObjectOpener mySuperN = null;
    private Field[] myInstVars;
    Hashtable myOpeners;

    ObjectOpener(Class clazz, Hashtable openers) {
        super(clazz);
        if (clazz == Void.TYPE) {
            throw new Error("class Void must use Opener.VOID");
        }
        if (openers.put(clazz, this) != null) {
            throw new Error("Already in table " + clazz);
        }
        Opener oldN = (Opener)openers.get(Void.TYPE);
        if (oldN == null) {
            openers.put(Void.TYPE, Opener.VOID);
        } else if (! Opener.VOID.equals(oldN)) {
            throw new Error("class Void must use Opener.VOID");
        }

        myOpeners = openers;
        Class supN;
        if (clazz.isInterface()) {
            supN = OBJECT_CLASS;
        } else {
            supN = clazz.getSuperclass();
        }
        if (supN != null) {
            mySuperN = (ObjectOpener)openerForClass(supN);
        }

        int n = getNumDeclaredFields(clazz);
        Field[] instVars = new Field[n];
        int i = 0;
        for (int j = 0; j < n; j++) {
            Field fN = getFieldN(clazz, j);
            if (fN != null && isOk(fN)) {
                instVars[i++] = fN;
            }
        }
        //i is the number of ok locally declared instance variables
        if (mySuperN == null) {
            myInstVars = new Field[i];
            System.arraycopy(instVars, 0, myInstVars, 0, i);
        } else {
            int s = mySuperN.myInstVars.length;
            //s is num of ok inherited instance variables
            myInstVars = new Field[s + i];
            System.arraycopy(mySuperN.myInstVars, 0, myInstVars, 0, s);
            System.arraycopy(instVars, 0, myInstVars, s, i);
        }            
    }

    public String toString() {
        return myClass.getName();
    }

    public String signature() {
        return "L" + myClass.getName().replace('.', '/') + ";";
    }

    /**
     * Encode the ok instance variables of 'obj' onto 'encoder'
     * according to their declared types. 
     */
    public void encodeBodyTo(Object obj, RtEncoder encoder) 
         throws IOException {
        for (int i = 0; i < myInstVars.length; i++) {
            myInstVars[i].encodeVarTo(obj, encoder);
        }
    }

    public Object makeRawInstanceN() throws IOException {
        return makeInstanceOf(type());
    }

    /**
     * Decode into the ok instance variables of 'obj' according to
     * their declared types.
     */
    public void decodeBodyFrom(Object obj, RtDecoder decoder)
         throws IOException {
        for (int i = 0; i < myInstVars.length; i++) {
            myInstVars[i].decodeVarFrom(obj, decoder);
        }
    }

    /***************** specific to ObjectOpener *****************/

    /**
     * The Opener for the superclass of this class, or null if this
     * class is Object.  Unlike Class.getSuperclass(), getSuperN() on
     * an Opener for an interface will return the Opener for class
     * Object. 
     */ 
    public ObjectOpener getSuperN() {
        return mySuperN;
    }

    /**
     * Returns an Opener capable of opening 'objN' (and therefore, of
     * opening objects like 'objN').
     */
    public RefOpener openerForObject(Object objN) {
        return (RefOpener)openerForClass(getClassOf(objN));
    }

    /**
     * Returns an Opener with the same "point-of-view" and authority
     * as this Opener, but for opening objects of type 'clazz'.
     * This is the hook for "Opener contagion".
     */
    public Opener openerForClass(Class clazz) {
        Opener result = (Opener)myOpeners.get(clazz);
        if (result != null) {
            return result;
        }
        if (clazz.getName().charAt(0) == '[') {
            return openerForSignature(clazz.getName().replace('.', '/'));
        } else {
            return openerForNonArrayClass(clazz);
        }
    }

    /**
     * Returns an Opener corresponding to the supplied signature
     */
    public Opener openerForSignature(String sig) {
        switch (sig.charAt(0)) {
        case 'Z': return Opener.BOOLEAN;
        case 'B': return Opener.BYTE;
        case 'C': return Opener.CHAR;
        case 'S': return Opener.SHORT;
        case 'I': return Opener.INT;
        case 'J': return Opener.LONG;
        case 'F': return Opener.FLOAT;
        case 'D': return Opener.DOUBLE;

        case 'L': {
            if (sig.charAt(sig.length() -1) != ';') {
                throw new IllegalArgumentException
                    ("Object signature must end in ';'");
            }
            String name = sig.substring(1, sig.length()-1).replace('/', '.');

            Class newClass;
            try {
                //XXX even using this new improved version of
                //Class.forName(), this is a potential security hole
                newClass = Opener.forNameFromClass(name, true, type());
            } catch (ClassNotFoundException ex) {
                throw new Error("FindClassFromClass failed " + ex);
            }
            //calls openerForClass rather than openerForNonArrayClass
            //because we need the table uniqueness check.
            return openerForClass(newClass);
        }
        case '[': {
            return new ArrayOpener(openerForSignature(sig.substring(1)));
        }
        default: {
            throw new IllegalArgumentException("unrecognized signature");
        }
        }
    }


    /**
     * Returns a new uninitialized instance
     */
    static public native Object makeInstanceOf(Class clazz);
        

    /**
     * Returns an Opener with the same "point-of-view" and authority
     * as this Opener, but for opening objects of non-array type
     * 'clazz', when it is known that the class isn't already in the
     * myOpeners table.  Subclasses should override this to call their
     * own constructor. <p> 
     *
     * XXX At this point, we really should be using composition rather
     * than inheritance.
     */
    abstract ObjectOpener openerForNonArrayClass(Class clazz);

    /**
     * Subclasses must override to express their what kind of instance
     * variables they consider to exist.
     *
     * XXX At this point, we really should be using composition rather
     * than inheritance .
     */
    abstract boolean isOk(Field f);

    /**
     * How many ok declared instance variables are there?
     */
    public int numInstVars() {
        return myInstVars.length;
    }

    /**
     * Returns an opener for the i'th ok instance variable declaration
     */
    public Field instVarOpener(int i) {
        return myInstVars[i];
    }

    /**
     * If the i'th locally declared field of clazz is an instance
     * variable (non-static) field, return a Field object describing
     * it.  Otherwise, return null.
     */
    private Field getFieldN(Class clazz, int i) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz must not be null");
        }
        Modifier mod = new Modifier(getFieldModifiers(clazz, i));
        if (mod.isStatic()) {
            return null;
        }
        return new Field(this,
                         openerForSignature(getFieldSignature(clazz,i)),
                         mod,
                         getFieldName(clazz, i),
                         getFieldOffset(clazz, i));
    }

    /**
     * Returns number of instance variables locally declared in class.
     */
    static private native int getNumDeclaredFields(Class clazz);
    
    /** 
     * Returns modifiers for instance variable.
     */    
    static private native int getFieldModifiers(Class clazz, int i);
    
    /**
     * Returns instance variable signature string.
     */    
    static private native String getFieldSignature(Class clazz, int i);
    
    /**
     * Returns the instance variable name.
     */    
    static private native String getFieldName(Class clazz, int i);

    /**
     * Returns the byte offset of instance variable i within an
     * instance of Class clazz
     */
    static private native int getFieldOffset(Class clazz, int i);
}    


/**
 * The simplest point of view for an ObjectOpener: all declared
 * instance variables are ok.
 */
class FullObjectOpener extends ObjectOpener {

    FullObjectOpener(Class clazz, Hashtable openers) {
        super(clazz, openers);
    }

    ObjectOpener openerForNonArrayClass(Class clazz) {
        return new FullObjectOpener(clazz, myOpeners);
    }

    boolean isOk(Field f) {
        return true;
    }
}


/**
 * An ObjectOpener whose point of view is that 'transient' instance
 * variable declarations are not ok (and so seem not to exist).
 */
class NonTransientOpener extends ObjectOpener {

    NonTransientOpener(Class clazz, Hashtable openers) {
        super(clazz, openers);
    }

    ObjectOpener openerForNonArrayClass(Class clazz) {
        return new NonTransientOpener(clazz, myOpeners);
    }

    boolean isOk(Field f) {
        return ! f.modifiers().isTransient();
    }
}


/**
 * An ObjectOpener that grants no authority, because its point of view
 * is that only public instance variable declarations are ok, so it
 * doesn't enable any access beyond that which a holder of the object
 * already has.  Since it grants no authority, its constructor is
 * public. 
 */
public class PublicOpener extends ObjectOpener {

    public PublicOpener(Class clazz, Hashtable openers) {
        super(clazz, openers);
    }

    ObjectOpener openerForNonArrayClass(Class clazz) {
        return new PublicOpener(clazz, myOpeners);
    }

    boolean isOk(Field f) {
        return f.modifiers().isPublic();
    }

    /**
     * XXX HACK.  SECURITY VIOLATION!  Should not be public.  Returns
     * a new NonTransientOpener.
     */
    static public ObjectOpener ntOpener() {
        return new NonTransientOpener(OBJECT_CLASS,
                                      new Hashtable());
    }
}


/** 
 * 'Field' is loosely based on the 'java.lang.reflect.Field' from the
 * Java 1.1 CRAPI (Core Reflection API).  Unlike the 1.1 CRAPI, class
 * Field is abstract.  There is a concrete subclass for each scalar
 * type, and one for all reference types. <p>
 *
 * A Field opens the instance variables corresponding to a particular
 * instance variable declaration in some class.  Fields are gotten
 * from their base ObjectOpener, and their point of view derives
 * from it. 
 */
public class Field {
    private ObjectOpener myBaseOpener;
    private Opener myOpener;
    private Modifier myModifiers;
    private String myName;      
    private transient int myByteOffset;

    Field(ObjectOpener base, Opener opener, Modifier modifiers,
          String name, int byteOffset) {
        myBaseOpener = base;
        myOpener = opener;
        myModifiers = modifiers;
        myName = name;
        myByteOffset = byteOffset;
    }
    
    /** 
     * Describes the class of the object we are declared in
     */
    public ObjectOpener baseOpener() { 
        return myBaseOpener; 
    }

    /**
     * Returns an Opener describing the declared type of this
     * instance variable. 
     */
    public Opener opener() {
        return myOpener;
    }

    /**
     * Modifiers for this instance variable declaration 
     */
    public Modifier modifiers() { 
        return myModifiers; 
    }

    /**
     * The name of this instance variable 
     */
    public String name() { return myName; }

    /**
     * Maybe this should be more human-readable? 
     */
    public String toString() { return myName; } 
    
    /**
     * Since a Field is created using information in the Class only
     * (not an instance), you must supply an instance of the object to
     * get the values from. In other words, the Field object describes
     * the slot in the instance, and if you want the value of a slot
     * of an object, then Field.get(Object o) will return the value of
     * the slot described by Field in Object o.
     */
    public Object getN(Object base) {
        if (base == null) {
            throw new IllegalArgumentException("base must not be null");
        } 
        return myOpener.peekFieldN(base, myByteOffset);
    }
    
    /** 
     * Set the value for a slot (described by Field) in an
     * object. The new value may be a wrapped primitive data type 
     */
    public void set(Object base, Object newValueN) {
        if (base == null) {
            throw new IllegalArgumentException("base must not be null");
        } 
        if (! myOpener.isInstance(newValueN)) { 
            throw new IllegalArgumentException
                (newValueN + " must be a " + myOpener);
        }
        myOpener.pokeField(base, myByteOffset, newValueN);
    }

    /**
     * Encodes the value of this instance variable value of 'base' onto
     * 'encoder'
     */
    public void encodeVarTo(Object base, RtEncoder encoder) 
         throws IOException {
        myOpener.encodeField(base, myByteOffset, encoder);
    }

    /**
     * Decodes from 'decoder' into this instance variable of 'base' 
     */
    public void decodeVarFrom(Object base, RtDecoder decoder)
         throws IOException {
        myOpener.decodeIntoField(base, myByteOffset, decoder);
    }
}


/**
 * Class Modifier manages the modifiers for variables and methods -
 * public/private/protected, final, synchronized, native, abstract,
 * etc.  There is an integer value in the class object that packs bits
 * designating all the modifiers. Class Modifier provides a set of
 * boolean methods to interrogate those bits. 
 */
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
    
    private int myModifiers = 0;
    
    public Modifier() {}
    public Modifier(int n) { myModifiers = n; } // Pretty brutal, eh?
    
    public final boolean isAbstract() {return (myModifiers & ABSTRACT) != 0; }
    public final boolean isFinal() {return (myModifiers & FINAL) != 0; }
    public final boolean isInterface() {
        return (myModifiers & INTERFACE) != 0; 
    }
    public final boolean isNative() {return (myModifiers & NATIVE) != 0; }
    public final boolean isPrivate() {return (myModifiers & PRIVATE) != 0; }
    public final boolean isProtected() {
        return (myModifiers & PROTECTED) != 0; 
    }
    public final boolean isPublic() {return (myModifiers & PUBLIC) != 0; }
    public final boolean isStatic() {return (myModifiers & STATIC) != 0; }
    public final boolean isSynchronized() {
        return (myModifiers & SYNCHRONIZED) != 0; 
    }
    public final boolean isTransient() {
        return (myModifiers & TRANSIENT) != 0; 
    }
    // public final boolean isVolatile() {
    //     return (myModifiers & VOLATILE) != 0; 
    // }
    // public final boolean isThreadSafe() {
    //     return (myModifiers & THREADSAFE) != 0; 
    // }
    
    public String modifiersString() {   // Ordered as in [Steele, p. 144]
        return ((isPrivate() ? "private " : (isPublic() ? "public " : "")) + 
                (isFinal() ? "final " : "") +
                (isStatic() ? "static " : "") + 
                (isTransient() ? "transient " : "")
                //+ (isVolatile() ? "volatile " : "")
                );
    }
}


