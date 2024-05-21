package ec.e.openers;

import java.io.IOException;
import java.lang.reflect.Array;
import ec.util.NestedException;

/**
 * An Opener for objects and variables of reference types, ie,
 * non-scalars.
 */
/*package*/ class RefOpener extends VarOpener {
    static private final Trace tr = new Trace("ec.e.openers.RefOpener");

    private Class myClass;

    RefOpener() {
        myClass = JavaUtil.OBJECT_TYPE;
    }

    RefOpener(Class clazz) {
        myClass = clazz;
    }

    public Class type() {
        return myClass;
    }

    /**
     * Delegates to the serializer
     */
    void encodeItTo(Object ref, RtEncoder serializer)
         throws IOException
    {
        serializer.encodeObject(ref);
    }

    /**
     * Delegates to the unserializer
     */
    Object decodeFrom(RtDecoder unserializer) throws IOException {
        return unserializer.decodeObject();
    }

    /**
     * Returns the value of the reference variable using fieldID
     * within 'base'.
     */
    static private native Object peekFieldRef(Object base, String fieldID, String sig);

    static public native Object testNullRet();

    /**
     * Sets the instance variable at byteOffset within 'base' to
     * newValue.
     */
    static private native void pokeFieldRef(Object base, String fieldID, String sig,
                                            Object newValue);
    /**
     * Returns the value of the instance variable of the reference
     * type using fieldID within 'base'.
     */
    Object peekField(Object base, String fieldID, String sig) {
        Object result = null;
        try {
            result =  peekFieldRef(base, fieldID, sig);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in refOpener.peekField(" + base + ", " + fieldID + ", " + sig + ")", ex);
        }
        if ((result != null) && (System.identityHashCode(result) == 65568)) {
            tr.errorm("PeekFieldRef returned a bogus NULL for (" + base + ", " + fieldID + ", " + sig + ")");
            //throw new NullPointerException("in refOpener.peekField(" + base + ", " + fieldID + ", " + sig + ")");
            result = null;
        }
        return result;
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder serializer)
         throws IOException {
        Object result = null;
        try {
            result =  peekField(base, fieldID, sig);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in refOpener.encodeField(" + base + ", " + fieldID + ", " + sig + ")", ex);
        }
        serializer.encodeObject(result);
    }

    static public void testNull() {
        Object testnull;
        testnull = testNullRet();
        if (testnull != null) {
            NullPointerException ex = new NullPointerException();
            ex.printStackTrace();
            tr.warningm("Impossible non- NULL");
            throw ex;
        }
    }

    /**
     * If the type this opener represents is assignable from newValue,
     * set the instance variable using fieldID within 'base' to this
     * value.
     *
     * @param newValue nullOk, suspect;
     */
    void pokeField(Object base, String fieldID, String sig, Object newValue)
        throws ClassCastException {
            Object testnull;
            testnull = testNullRet();
            if (testnull != null) {
                NullPointerException ex = new NullPointerException();
                ex.printStackTrace();
                tr.warningm("Impossible non- NULL(1) in refOpener.pokeField(" + base.getClass() + ", " + fieldID + ", " + sig + ", " + newValue + ")", ex);
                throw ex;
            }
            if (newValue == null || myClass.isInstance(newValue)) {
                try {
                    pokeFieldRef(base, fieldID, sig, newValue);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new NestedException("in refOpener.pokeField(" + base.getClass() + ", " + fieldID + ", " + sig + ", "  + newValue + ")", ex);
                }
            } else {
                throw new ClassCastException("a " + newValue.getClass()
                    + " isn't a " + myClass);
            }
            testnull = testNullRet();
            if (testnull != null) {
                NullPointerException ex = new NullPointerException();
                ex.printStackTrace();
                tr.warningm("Impossible non- NULL(2) in refOpener.pokeField(" + base.getClass() + ", " + fieldID + ", " + sig + ", " + newValue + ")", ex);
                throw ex;
            }
    }

}
