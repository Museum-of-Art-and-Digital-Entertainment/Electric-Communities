package ec.e.openers;

import java.io.IOException;
import java.lang.reflect.Array;


/**
 * An Opener for objects and variables of reference types, ie,
 * non-scalars.
 */
/*package*/ class RefOpener extends VarOpener {

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
     * Returns the value of the instance variable of the reference
     * type at byteOffset within 'base'.
     */
    Object peekField(Object base, int byteOffset) {
        return peekFieldRef(base, byteOffset);
    }

    /**
     * Returns the value of the reference variable at byteOffset
     * within 'base'.
     */
    static private native Object peekFieldRef(Object base, int byteOffset);

    /**
     * If the type this opener represents is assignable from newValue,
     * set the instance variable at byteOffset within 'base' to this
     * value.
     *
     * @param newValue nullOk, suspect;
     */
    void pokeField(Object base, int byteOffset, Object newValue)
         throws ClassCastException {

        if (newValue == null || myClass.isInstance(newValue)) {
            pokeFieldRef(base, byteOffset, newValue);
        } else {
            throw new ClassCastException("a " + newValue.getClass()
                                         + " isn't a " + myClass);
        }
    }

    /**
     * Sets the instance variable at byteOffset within 'base' to
     * newValue.
     */
    static private native void pokeFieldRef(Object base, int byteOffset,
                                            Object newValue);
}

