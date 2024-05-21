package ec.e.openers;

import java.io.IOException;
import java.util.Hashtable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import ec.e.start.Vat;
import ec.util.NestedException;


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
    static private final Trace tr = new Trace("ec.e.openers.Surgeon");

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
        RefOpener.testNull();
        if (myBase == null) {
            Object result;
            try {
                result = rawInstanceOf(type());
                for (int i = 0; i < myInstVars.length; i++) {
                    FieldKnife f = myInstVars[i];
                    char c = (f.signature()).charAt(0);
                    if (((c == '[') || (c == 'L')) && (f.get(result) != null)) {
                        tr.errorm("rawInstance returned non-null value in class " + type() +
                            " field " + f + " of type " + f.signature());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new NestedException("in decodePreface, rawinstanceOf(" + type() + ")", ex);
            }
            RefOpener.testNull();
            return result;
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
             RefOpener.testNull();
        if (myBase != null) {
            myBase.decodeBody(obj, decoder);
        }
        for (int i = 0; i < myInstVars.length; i++) {
            myInstVars[i].decodeVarFrom(obj, decoder);
        }
        RefOpener.testNull();
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
        Field[] flds = clazz.getDeclaredFields();
        int n = flds.length;
        FieldKnife[] instVars = new FieldKnife[n];
        int numOk = 0;
        for (int j = 0; j < n; j++) {
            /* f nullOk; */
            FieldKnife f = getField(clazz, flds, j);
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
    private FieldKnife getField(Class clazz, Field[] flds, int i) {
        Field field = flds[i];
        int mod = field.getModifiers();
        if ((mod & Modifier.STATIC) != 0) {
            return null;
        }
        Class fieldClass = field.getType();
        String sig = JavaUtil.signature(fieldClass.getName());
        String fieldName = field.getName();
        int fieldID = 0;
        try {
            // fieldID = getFieldID(clazz, fieldName, sig);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in Surgeon.getField, getFieldID(" +
                clazz + ", " + fieldName + ", " + sig + ")", ex);
        }
        return new FieldKnife(this,
                              clazz,
                              sig,
                              mod,
                              fieldName,
                              fieldID);
    }

    /**
     * Returns a C pointer to a FieldID structure that JNI says remains valid
     * as long as clazz is not garbage collected. We make sure of that by caching
     * it with clazz. The FieldID can only be used inside of a JNI native method.
     * XXX This will break if an int cannot hold a pointer. By the time that is not
     * true, we should be well past the point when we have to kludge this functionality
     * using native methods and we can recode this using pure Java or whatever language
     * we are using then.
     */
    static private native int getFieldID(Class clazz, String fieldName, String sig);

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
    private transient String myFieldID;
    private boolean amEnabled;

    FieldKnife(Surgeon base, Class baseType, String sig, int modifier,
               String name, int fieldID) {
        myBaseOpener = base;
        myBaseType = baseType;
        mySignature = sig;
        myOpener = null;
        myModifier = modifier;
        myName = name;
        myFieldID = name;
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
     * myFieldID.  Until myFieldID is guaranteed to be correct
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
        return myOpener.peekField(base, myFieldID, mySignature);
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
        myOpener.pokeField(base, myFieldID, mySignature, newValue);
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
        myOpener.encodeField(base, myFieldID, mySignature, encoder);
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
        myOpener.decodeField(base, myFieldID, mySignature, decoder);
    }
}

