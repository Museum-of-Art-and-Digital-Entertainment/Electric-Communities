package ec.e.openers;

import java.io.IOException;
import java.util.Hashtable;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import ec.e.start.Vat;


/**
 * An ObjOpener for non-scalar non-arrays.  A Surgeon violates the
 * encapsulation of the objects it opens, and hence grants mucho
 * authority.  Further, Surgeons beget Surgeons, so a Surgeon grants
 * authority to violate the encapsulation of a set of objects.
 * Outside the TCB, one can only make encapsulation violating Surgeons
 * by using a ClassRecipe whose authority must derive from the
 * all-powerful RootClassRecipe. <p>
 *
 * Surgeon delegates approval of FieldKnives to its OpenerRecipe's
 * ClassRecipe.  By deciding which are ok, a ClassRecipe defines which
 * instance variable declarations "exist" in the world as presented by
 * this ObjOpener.  On encoding, variables that aren't ok are ignored.
 * <p>
 *
 * XXX not true: On decode, they are set according to their class's
 * default (no argument) constructor.  <p>
 *
 * XXX unfortunately true instead: On decode, reference veriables are
 * set to null, and scalar variables are set to the zero value for
 * their type.
 */
public class Surgeon extends ObjOpener {

    /**
     * If myBase is non-null, it is a Chef for one of the superclasses
     * of the class opened by this Surgeon.  This Surgeon uses the
     * Chef for the part of the object starting at that superclass,
     * but operates directly on the remainder of the object.
     *
     * myBase nullOk.
     */
    private Chef myBase;

    /** mySuper nullOk. */
    private ObjOpener mySuper;
    private FieldKnife[] myInstVars;

    Surgeon(OpenerRecipe maker, Class clazz)
         throws ClassNotFoundException,
           InvocationTargetException,
           NoSuchMethodException,
           IllegalAccessException
    {
        super(maker, clazz, null);
        init(OpenerID.make(initFields(maker, clazz)));

        for (int i = 0; i < myInstVars.length; i++) {
            myInstVars[i].activate();
        }
    }


    public String toString() {
        return myClass.getName();
    }

    /**
     * Currently returns an array of the ok instance variable names
     * (preceded with commentary from a base Chef, if any).
     */
    public Object commentary() {
        String[] baseNames = {};
        if (myBase != null) {
            Object baseCommentary = myBase.commentary();
            if (! (baseCommentary instanceof String[])) {
                return baseCommentary;
            }
            baseNames = (String[])baseCommentary;
        }
        String[] result = new String[baseNames.length + myInstVars.length];
        System.arraycopy(baseNames, 0, result, 0, baseNames.length);
        for (int i = 0; i < myInstVars.length; i++) {
            result[baseNames.length + i] = myInstVars[i].name();
        }
        return result;
    }

    /**
     * Just encodes according to a base Chef, if any.
     */
    public void encodePreface(Object obj, RtEncoder encoder)
         throws IOException {
        if (myBase != null) {
            myBase.encodePreface(obj, encoder);
        }
    }

    /**
     * Encode the ok instance variables of 'obj' onto 'encoder'
     * according to their declared types.  (Encodes first according to
     * the base Chef, if any).
     */
    public void encodeBody(Object obj, RtEncoder encoder)
         throws IOException {
        if (myBase != null) {
            myBase.encodeBody(obj, encoder);
        }
        for (int i = 0; i < myInstVars.length; i++) {
            myInstVars[i].encodeVarTo(obj, encoder);
        }
    }

    /**
     * If there's a base Chef, have it instantiate my type and decode
     * the preface.  Otherwise, return a raw instantiation of my type.
     */
    public Object decodePreface(RtDecoder decoder) throws IOException {
        if (myBase == null) {
            return rawInstanceOf(type());
        } else {
            return myBase.decodePrefaceOf(type(), decoder);
        }
    }

    /**
     * Decode into the ok instance variables of 'obj' according to
     * their declared types.  If there's a base Chef, have it decode
     * its part first.
     */
    public void decodeBody(Object obj, RtDecoder decoder)
         throws IOException {
        if (myBase != null) {
            myBase.decodeBody(obj, decoder);
        }
        for (int i = 0; i < myInstVars.length; i++) {
            myInstVars[i].decodeVarFrom(obj, decoder);
        }
    }

    /***************** specific to Surgeon *****************/

    /**
     * Dangerous, Private scope only!
     * Uses native code to create and return a new uninitialized
     * instance.
     */
    static private native Object rawInstanceOf(Class clazz);

    /**
     * The ObjOpener for the superclass of this class, or null if this
     * class is Object.  Unlike Class.getSuperclass(), super() on
     * an ObjOpener for an interface will return the ObjOpener for class
     * Object.
     *
     * @return nullOk;
     */
    public ObjOpener getSuper() {
        return mySuper;
    }

    /**
     * How many ok declared instance variables are there?  (Doesn't
     * count those handled by my base Chef, if any.)
     */
    public int numInstVars() {
        return myInstVars.length;
    }

    /**
     * Returns an opener for the i'th ok instance variable
     * declaration.  (Doesn't count those handled by my base Chef, if
     * any.)
     */
    public FieldKnife instVarOpener(int i) {
        return myInstVars[i];
    }

    /**
     * Calculate the openerID of the Surgeon from the signatures of
     * clazz, the allowed fields of clazz, and a Chef for a base
     * superclass of clazz, if any. <p>
     */
    private String initFields(OpenerRecipe maker, Class clazz)
         throws ClassNotFoundException,
           InvocationTargetException,
           NoSuchMethodException,
           IllegalAccessException
    {
        ClassRecipe classRecipe = maker.classRecipe();
        int n = getNumDeclaredFields(clazz);
        FieldKnife[] instVars = new FieldKnife[n];
        int numOk = 0;
        for (int j = 0; j < n; j++) {
            /* f nullOk; */
            FieldKnife f = getField(clazz, j);
            if (f != null) {
                classRecipe.approve(f);
                if (f.isEnabled()) {
                    instVars[numOk++] = f;
                }
            }
        }
        //numOk is the number of ok locally declared instance variables

        /* sup nullOk; */
        Class sup;
        if (clazz.isInterface()) {
            sup = JavaUtil.OBJECT_TYPE;
        } else {
            sup = clazz.getSuperclass();
        }
        if (sup != null) {
            //XXX if the Surgeon's being make for decoding, we should
            //look up a super for decoding
            mySuper = (ObjOpener)(maker.forEncodingAn(sup));
        }

        myInstVars = new FieldKnife[numOk];
        System.arraycopy(instVars, 0, myInstVars, 0, numOk);

        if (mySuper == null || mySuper instanceof Chef) {
            myBase = (Chef)mySuper;

        } else if (mySuper instanceof Surgeon) {
            Surgeon superSurgeon = (Surgeon)mySuper;
            myBase = superSurgeon.myBase;
            myInstVars = (FieldKnife[])JavaUtil.append(superSurgeon.myInstVars,
                                                       myInstVars);
        } else {
            throw new Error("Can't be super " + mySuper);
        }

        /*
         * Calculate the openerID of the Surgeon from the
         * base Chef if any, the signatures of the fields, and the
         * clazz
         */
        String result;
        if (myBase == null) {
            result = "(()";
        } else {
            result = "((";
            for (int i = 0; i < myBase.myPreface.length; i++) {
                String fqn = myBase.myPreface[i].type().getName();
                result += JavaUtil.signature(fqn);
            }
            result += ")";
            for (int i = 0; i < myBase.myBody.length; i++) {
                String fqn = myBase.myBody[i].type().getName();
                result += JavaUtil.signature(fqn);
            }
        }
        for (int i = 0; i < myInstVars.length; i++) {
            result += myInstVars[i].signature();
        }
        result += ")" + JavaUtil.signature(clazz.getName());
        return result;
    }

    /**
     * If the i'th locally declared field of clazz is an instance
     * variable (non-static) field, return a FieldKnife object describing
     * it.  Otherwise, return null.
     *
     * @return nullOk;
     */
    private FieldKnife getField(Class clazz, int i) {
        int mod = getFieldModifiers(clazz, i);
        if ((mod & Modifier.STATIC) != 0) {
            return null;
        }
        return new FieldKnife(this,
                              clazz,
                              getFieldSignature(clazz, i),
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
 * 'FieldKnife' is loosely based on the 'java.lang.reflect.Field' from the
 * Java 1.1 CRAPI (Core Reflection API). <p>
 *
 * A FieldKnife opens the instance variables corresponding to a particular
 * instance variable declaration in some class.  FieldKnifes are gotten
 * from their base Surgeon, and their point of view derives from it.
 */
public final class FieldKnife implements AwakeAfterRevival {
    private Surgeon myBaseOpener;
    private Class myBaseType;
    private String mySignature;
    private VarOpener myOpener;
    private int myModifier;
    private String myName;
    private transient int myByteOffset;
    private boolean amEnabled;

    FieldKnife(Surgeon base, Class baseType, String sig, int modifier,
               String name, int byteOffset) {
        myBaseOpener = base;
        myBaseType = baseType;
        mySignature = sig;
        myOpener = null;
        myModifier = modifier;
        myName = name;
        myByteOffset = byteOffset;
        amEnabled = false;
    }

    /**
     * The only way an object outside this package can cause a
     * FieldKnife to be enable()d is to tell the RootClassRecipe to do
     * so.  Only FieldKnives that have been enabled during the
     * ClassRecipe.approve() request are then activated.
     */
    void enable() {
        amEnabled = true;
    }

    /**
     *
     */
    boolean isEnabled() {
        return amEnabled;
    }

    /**
     * Besides dealing with the security issues around 'enable()',
     * this initialization had to be delayed to break a nasty circular
     * initialization problem.  (If only Channels were more tightly
     * integrated into Java.  Sigh.)  <p>
     *
     * Big change: all fields of reference type now just use a generic
     * opener for java.lang.Object.  This more cleanly breaks the
     * circular dependency while also being cleaner and fixing other
     * problems.
     */
    void activate() {
        myOpener = VarOpener.fromSignature(mySignature);
    }

    /**
     * Describes the class of the object we are declared in
     */
    public Surgeon baseOpener() {
        return myBaseOpener;
    }

    /**
     * the class of the object we are declared in
     */
    public Class baseType() {
        return myBaseType;
    }

    /**
     * Valid even before activate().
     */
    public String signature() {
        return mySignature;
    }

    /**
     * XXX For now, a FieldKnife simply decodes without authority.
     * Instead, it should find the instance variable with its myName
     * and modify its internal state to match, especially
     * myByteOffset.  Until myByteOffset is guaranteed to be correct
     * in the restored world, a FieldKnife is too dangerous to be left
     * on.
     */
    public void awakeAfterRevival() {
        myOpener = null;
    }

    /**
     * Modifier for this instance variable declaration
     */
    public int modifier() {
        return myModifier;
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
     * Since a FieldKnife is created using information in the Class only
     * (not an instance), you must supply an instance of the object to
     * get the values from. In other words, the FieldKnife object describes
     * the slot in the instance, and if you want the value of a slot
     * of an object, then FieldKnife.get(Object o) will return the value of
     * the slot described by FieldKnife in Object o.
     *
     * @return nullOk, suspect;
     * @exception NullPointerException if called on a FieldKnife that has
     * not been activated.
     */
    public Object get(Object base) {
        if (base == null) {
            throw new IllegalArgumentException("base must not be null");
        }
        return myOpener.peekField(base, myByteOffset);
    }

    /**
     * Set the value for a slot (described by FieldKnife) in an
     * object. The new value may be a wrapped primitive data type
     *
     * @param newValue nullOk, suspect;
     * @exception NullPointerException if called on a FieldKnife that has
     * not been activated.
     */
    public void set(Object base, Object newValue) {
        if (base == null) {
            throw new IllegalArgumentException("base must not be null");
        }
        //relies on pokeField for type checking newValue
        myOpener.pokeField(base, myByteOffset, newValue);
    }

    /**
     * Encodes the value of this instance variable value of 'base' onto
     * 'encoder'
     *
     * @exception NullPointerException if called on a FieldKnife that has
     * not been activated.
     */
    public void encodeVarTo(Object base, RtEncoder encoder)
         throws IOException
    {
        myOpener.encodeField(base, myByteOffset, encoder);
    }

    /**
     * Decodes from 'decoder' into this instance variable of 'base'
     *
     * @exception NullPointerException if called on a FieldKnife that has
     * not been activated.
     */
    public void decodeVarFrom(Object base, RtDecoder decoder)
         throws IOException
    {
        myOpener.decodeField(base, myByteOffset, decoder);
    }
}

