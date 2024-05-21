package ec.e.openers;

import java.io.IOException;
import java.lang.reflect.Array;
import ec.vcache.VCache;
import ec.tables.Table;
import ec.util.NestedError;
import ec.util.NestedException;

/**
 * VarOpeners & ObjOpeners are a capability-based set of abstractions
 * for inspecting, encoding, & decoding sets of objects.  <p>
 *
 * The Opener classes are built to trust other Opener classes--full
 * mutual trust.  Therefore this set is defined only in this package,
 * and is not extensible by subclassing.  This is enforced by having
 * no Opener constructor have greater than package scope.  Openers are
 * extensible instead by parameterization--mostly by defining new
 * kinds of Recipes. This is the <a href =
 * "http://www-int.communities.com/archives/ec_tech/0148.html">
 * Semi-final Classes</a> technique. <p>
 *
 * This package is the first test of some <a href =
 * "http://www-int.communities.com/archives/ec_tech/0137.html">
 * experimental coding conventions</a>.
 *
 * @see ec.e.openers.ObjOpener
 */
public abstract class VarOpener {

    static private final Trace tr = new Trace("ec.e.openers.VarOpener");

    static public final Class VAR_OPENER_TYPE
        = JavaUtil.classForName("ec.e.openers.VarOpener");

    VarOpener() {}

    static {
        System.loadLibrary("openers");
        staticSelfTest();
    }

    /**
     * Opener native library version match verification.
     * libraryVersion() returns a version number as an integer.
     * We compare it to a range of library versions that we can handle.
     * Version numbers use two last decimal digits as a "minor version" field.
     */

    static final int MIN_OPENER_LIBRARY_VERSION = 400;
    static final int RELEASED_OPENER_LIBRARY_VERSION = 400;
    static final int MAX_OPENER_LIBRARY_VERSION = 400;

    static private native int libraryVersion();

    /**
     * staticSelfTest - checks this class and its static environment
     * for certain expected inconsistencies. For now, we just check
     * that we have loaded our native code library and that it is
     * the version we expect it to be.
     * If we find a problem, we throw an Error.
     */

    static public void staticSelfTest() {
        int foundVersion = libraryVersion();

        if (tr.debug) tr.debugm("Opener native library version " +
            foundVersion + ", java library version " + RELEASED_OPENER_LIBRARY_VERSION);

        if (foundVersion < MIN_OPENER_LIBRARY_VERSION) {
            throw new Error("Opener native library is version " +
                foundVersion +
                ", please upgrade to " +
                RELEASED_OPENER_LIBRARY_VERSION);
        }

        if (foundVersion > MAX_OPENER_LIBRARY_VERSION) {
            throw new Error("Opener native library is version " +
                foundVersion +
                ", but system cannot use later than " +
                MAX_OPENER_LIBRARY_VERSION);
        }
    }

    static final VarOpener BOOLEAN  = new BooleanOpener();
    static final VarOpener BYTE     = new ByteOpener();
    static final VarOpener CHAR     = new CharOpener();
    static final VarOpener SHORT    = new ShortOpener();
    static final VarOpener INT      = new IntOpener();
    static final VarOpener LONG     = new LongOpener();
    static final VarOpener FLOAT    = new FloatOpener();
    static final VarOpener DOUBLE   = new DoubleOpener();
    //no void opener
    static final VarOpener REF      = new RefOpener();


    /**
     *
     */
    static public VarOpener fromClass(Class clazz) {
        if (clazz.isPrimitive()) {
            return fromSigChar(JavaUtil.signature(clazz.getName()).charAt(0));
        } else {
            return new RefOpener(clazz);
        }
    }


    /**
     *
     */
    static public VarOpener fromSigChar(char sig) {
        switch (sig) {
            case 'Z':   return BOOLEAN;
            case 'B':   return BYTE;
            case 'C':   return CHAR;
            case 'S':   return SHORT;
            case 'I':   return INT;
            case 'J':   return LONG;
            case 'F':   return FLOAT;
            case 'D':   return DOUBLE;
            case 'V':   throw new IllegalArgumentException("no void opener");
            case 'L':
            case '[':   return REF;
            default: throw new IllegalArgumentException(sig + " unrecognized");
        }
    }


    /**
     *
     */
    static public VarOpener fromSignature(String sig) {
        return fromClass(JavaUtil.classForSignature(sig));
    }


    /**
     * Defaults to the type()'s toString().
     */
    public String toString() {
        return type().toString();
    }

    /**
     * Returns a Class for the type that this VarOpener opens.  If this
     * is an VarOpener for a scalar type, this will return the
     * corresponding primitive class (a non-instantiable class
     * representing the scalar type).
     *
     * @see java.lang.Class#isPrimitive
     */
    public abstract Class type();

    /**
     * Encode 'obj' onto 'encoder' according to the type this VarOpener
     * describes.  Unwraps 'obj' as necessary.
     *
     * @param obj nullOk, suspect;
     * @param encoder suspect;
     */
    abstract void encodeItTo(Object obj, RtEncoder encoder)
         throws IOException;

    /**
     * Decode an object from 'decoder' according to the type this VarOpener
     * describes.  Wraps result as necessary.
     *
     * @param decoder suspect;
     * @return nullOk, suspect;
     */
    abstract Object decodeFrom(RtDecoder decoder)
         throws IOException;


    /*********************** Array Support ********************
     * All the array support methods are only package scope, and all &
     * only they have the word "Element" in their name
     *******************************************************/

    /** For use by ArrayOpener: 'array' must be an array of the type
     * represented by this opener.
     * Encodes elements of array onto encoder according to the type
     * of this opener.  Subclasses may wish to override for
     * efficiency, but the default implementation is correct (and
     * serves as a precise specification).
     *
     * @param encoder suspect;
     */
    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        int n = Array.getLength(array);
        for (int i = 0; i < n; i++) {
            encodeItTo(Array.get(array, i), encoder);
        }
    }

    /** For use by ArrayOpener: 'array' must be an array of the type
     * represented by this opener.
     * Decodes array-length elements from decoder according to the
     * type of this opener, and stores them into the array.
     * Subclasses may wish to override for efficiency, but the default
     * implementation is correct (and serves as a precise
     * specification).
     *
     * @param decoder suspect;
     */
    void decodeElements(Object array, RtDecoder decoder)
         throws IOException {
        int n = Array.getLength(array);
        for (int i = 0; i < n; i++) {
            Array.set(array, i, decodeFrom(decoder));
        }
    }


    /****************** Instance Variable Support ******************
     * All the instance variable support methods are only package
     * scope, and all & only they have the word "Field" in their name
     *******************************************************/


    /**
     * DANGEROUS, PACKAGE SCOPE ONLY. For use by Field. ASSUMING
     * there's an instance variable of the type described by this
     * opener using fieldID  within base, this returns it, wrapping as
     * appropriate.  Our caller must ensure that these assumptions are
     * correct. fieldID is an opaque int descriptor meaningful only to the
     * native code that creates and manipulates it.
     *
     * @return nullOk, suspect;
     */
    abstract Object peekField(Object base, String fieldID, String sig);

    /**
     * DANGEROUS, PACKAGE SCOPE ONLY. For use by Field. ASSUMING
     * there's an instance variable of the type described by this
     * opener using fieldID within base, this sets it, unwrapping as
     * appropriate.  Our caller must ensure that these assumptions are
     * correct.
     *
     * @param newValue nullOk, suspect;
     * @exception NullPointerException when assigning into a scalar
     * and newValue is null, or when 'base' is null.
     * @exception ClassCastException when this opener's type is not
     * assignable from newValue;
     */
    abstract void pokeField(Object base, String fieldID, String sig, Object newValue)
         throws NullPointerException, ClassCastException;

    /**
     * DANGEROUS, PACKAGE SCOPE ONLY. For use by Field. ASSUMING
     * there's an instance variable of the type described by this
     * opener using fieldID within base, this encodes it according to
     * this type.  Our caller must ensure that these assumptions are
     * correct. <p>
     *
     * Subclasses may wish to override for efficiency, but the default
     * implementation is correct (and serves as a precise
     * specification).
     *
     * @param encoder suspect;
     */
    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
        encodeItTo(peekField(base, fieldID, sig), encoder);
    }

    /**
     * DANGEROUS, PACKAGE SCOPE ONLY. For use by Field. ASSUMING
     * there's an instance variable of the type described by this
     * opener using fieldID within base, this decodes a value
     * according to this type, and set the variable to this value.  Our
     * caller must ensure that these assumptions are correct. <p>
     *
     * Subclasses may wish to override for efficiency, but the default
     * implementation is correct (and serves as a precise
     * specification).
     *
     * @param decoder suspect;
     */
    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
         throws IOException {
        pokeField(base, fieldID, sig, decodeFrom(decoder));
    }
}


/**
 * Represents the scalar 'boolean' type
 */
final class BooleanOpener extends VarOpener {

    BooleanOpener() {}

    public Class type() {
        return Boolean.TYPE;
    }

    void encodeItTo(Object obj, RtEncoder encoder) throws IOException {
        encoder.writeBoolean(((Boolean)obj).booleanValue());
    }

    Object decodeFrom(RtDecoder decoder) throws IOException {
        if (decoder.readBoolean()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        boolean[] a = (boolean[])array;
        for (int i = 0; i < a.length; i++) {
            encoder.writeBoolean(a[i]);
        }
    }

    void decodeElements(Object array, RtDecoder decoder) throws IOException {
        boolean[] a = (boolean[])array;
        for (int i = 0; i < a.length; i++) {
            a[i] = decoder.readBoolean();
        }
    }

    static private native boolean peekFieldBoolean(Object base, String fieldID);

    static private native void pokeFieldBoolean(Object base, String fieldID,
                                              boolean newValue);

    Object peekField(Object base, String fieldID, String sig) {
        boolean result;
        try {
            result = peekFieldBoolean(base, fieldID);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in peekFieldBoolean(" + base + ", " + fieldID + ")", ex);
        }
        if (result) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    void pokeField(Object base, String fieldID, String sig, Object newValue) {
        RefOpener.testNull();
        try {
            pokeFieldBoolean(base, fieldID, ((Boolean)newValue).booleanValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in pokeFieldBoolean(" +
                base + ", " + fieldID + ", " + newValue + ")", ex);
        }
        RefOpener.testNull();
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
             boolean result;
             try {
                 result = peekFieldBoolean(base, fieldID);
             } catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in encodeField, peekFieldBoolean(" + base + ", " + fieldID + ")", ex);
             }
             encoder.writeBoolean(result);
    }

    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
         throws IOException {
             RefOpener.testNull();
             boolean result = decoder.readBoolean();
             try {
                 pokeFieldBoolean(base, fieldID, result);
             } catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in decodeField, pokeFieldBoolean(" +
                     base + ", " + fieldID + ", " + result + ")", ex);
             }
             RefOpener.testNull();
    }
}


/**
 * Represents the scalar 'byte' type
 */
final class ByteOpener extends VarOpener {

    ByteOpener() {}

    public Class type() {
        return Byte.TYPE;
    }

    void encodeItTo(Object obj, RtEncoder encoder) throws IOException {
        encoder.writeByte(((Byte)obj).byteValue());
    }

    Object decodeFrom(RtDecoder decoder) throws IOException {
        return VCache.toObject(decoder.readByte());
    }

    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        byte[] a = (byte[])array;
        encoder.write(a);
    }

    void decodeElements(Object array, RtDecoder decoder) throws IOException {
        byte[] a = (byte[])array;
        decoder.readFully(a);
    }

    static private native byte peekFieldByte(Object base, String fieldID);

    static private native void pokeFieldByte(Object base, String fieldID,
                                              byte newValue);

    Object peekField(Object base, String fieldID, String sig) {
        byte result;
        try {
            result = peekFieldByte(base, fieldID);
        }  catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in peekFieldByte(" + base + ", " + fieldID + ")", ex);
        }
        return VCache.toObject(result);
    }

    void pokeField(Object base, String fieldID, String sig, Object newValue) {
        RefOpener.testNull();
        try {
            pokeFieldByte(base, fieldID, ((Byte)newValue).byteValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in pokeFieldByte(" +
                base + ", " + fieldID + ", " + newValue + ")", ex);
        }
        RefOpener.testNull();
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
             byte result;
             try {
                 result = peekFieldByte(base, fieldID);
             }  catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in encodeField, peekFieldByte(" + base + ", " + fieldID + ")", ex);
             }
             encoder.writeByte(result);
    }

    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
        throws IOException {
            RefOpener.testNull();
            byte result = decoder.readByte();
            try {
                pokeFieldByte(base, fieldID, result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new NestedException("in decodeField, pokeFieldByte(" +
                    base + ", " + fieldID + ", " + result + ")", ex);
            }
            RefOpener.testNull();
    }
}


/**
 * Represents the scalar 'char' type
 */
final class CharOpener extends VarOpener {

    CharOpener() {}

    public Class type() {
        return Character.TYPE;
    }

    void encodeItTo(Object obj, RtEncoder encoder) throws IOException {
        encoder.writeChar(((Character)obj).charValue());
    }

    Object decodeFrom(RtDecoder decoder) throws IOException {
        return VCache.toObject(decoder.readChar());
    }

    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        char[] a = (char[])array;
        for (int i = 0; i < a.length; i++) {
            encoder.writeChar(a[i]);
        }
    }

    void decodeElements(Object array, RtDecoder decoder) throws IOException {
        char[] a = (char[])array;
        for (int i = 0; i < a.length; i++) {
            a[i] = decoder.readChar();
        }
    }

    static private native char peekFieldChar(Object base, String fieldID);

    static private native void pokeFieldChar(Object base, String fieldID,
                                              char newValue);

    Object peekField(Object base, String fieldID, String sig) {
        char result;
        try {
            result = peekFieldChar(base, fieldID);
        }  catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in peekFieldChar(" + base + ", " + fieldID + ")", ex);
        }
        return VCache.toObject(result);
    }

    void pokeField(Object base, String fieldID, String sig, Object newValue) {
        RefOpener.testNull();
        try {
            pokeFieldChar(base, fieldID, ((Character)newValue).charValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in pokeFieldChar(" +
                base + ", " + fieldID + ", " + newValue + ")", ex);
        }
        RefOpener.testNull();
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
             char result;
             try {
                 result = peekFieldChar(base, fieldID);
             }  catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in encodeField, peekFieldChar(" + base + ", " + fieldID + ")", ex);
             }
             encoder.writeChar(result);
    }

    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
        throws IOException {
            RefOpener.testNull();
            char result = decoder.readChar();
            try {
                pokeFieldChar(base, fieldID, result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new NestedException("in decodeField, pokeFieldChar(" +
                    base + ", " + fieldID + ", " + result + ")", ex);
            }
            RefOpener.testNull();
    }
}


/**
 * Represents the scalar 'short' type
 */
final class ShortOpener extends VarOpener {

    ShortOpener() {}

    public Class type() {
        return Short.TYPE;
    }

    void encodeItTo(Object obj, RtEncoder encoder) throws IOException {
        encoder.writeShort(((Short)obj).shortValue());
    }

    Object decodeFrom(RtDecoder decoder) throws IOException {
        return VCache.toObject(decoder.readShort());
    }

    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        short[] a = (short[])array;
        for (int i = 0; i < a.length; i++) {
            encoder.writeShort(a[i]);
        }
    }

    void decodeElements(Object array, RtDecoder decoder) throws IOException {
        short[] a = (short[])array;
        for (int i = 0; i < a.length; i++) {
            a[i] = decoder.readShort();
        }
    }

    static private native short peekFieldShort(Object base, String fieldID);

    static private native void pokeFieldShort(Object base, String fieldID,
                                              short newValue);

    Object peekField(Object base, String fieldID, String sig) {
        short result;
        try {
            result = peekFieldShort(base, fieldID);
        }  catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in peekFieldShort(" + base + ", " + fieldID + ")", ex);
        }
        return VCache.toObject(result);
    }

    void pokeField(Object base, String fieldID, String sig, Object newValue) {
        RefOpener.testNull();
        try {
            pokeFieldShort(base, fieldID, ((Short)newValue).shortValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in pokeFieldShort(" +
                base + ", " + fieldID + ", " + newValue + ")", ex);
        }
        RefOpener.testNull();
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
             short result;
             try {
                 result = peekFieldShort(base, fieldID);
             }  catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in encodeField, peekFieldShort(" + base + ", " + fieldID + ")", ex);
             }
             encoder.writeShort(result);
    }

    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
         throws IOException {
             RefOpener.testNull();
            short result = decoder.readShort();
            try {
                pokeFieldShort(base, fieldID, result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new NestedException("in decodeField, pokeFieldShort(" +
                    base + ", " + fieldID + ", " + result + ")", ex);
            }
            RefOpener.testNull();
    }
}


/**
 * Represents the scalar 'int' type
 */
final class IntOpener extends VarOpener {

    IntOpener() {}

    public Class type() {
        return Integer.TYPE;
    }

    void encodeItTo(Object obj, RtEncoder encoder) throws IOException {
        encoder.writeInt(((Integer)obj).intValue());
    }

    Object decodeFrom(RtDecoder decoder) throws IOException {
        return VCache.toObject(decoder.readInt());
    }

    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        int[] a = (int[])array;
        for (int i = 0; i < a.length; i++) {
            encoder.writeInt(a[i]);
        }
    }

    void decodeElements(Object array, RtDecoder decoder) throws IOException {
        int[] a = (int[])array;
        for (int i = 0; i < a.length; i++) {
            a[i] = decoder.readInt();
        }
    }

    static private native int peekFieldInt(Object base, String fieldID);

    static private native void pokeFieldInt(Object base, String fieldID,
                                              int newValue);

    Object peekField(Object base, String fieldID, String sig) {
        int result;
        try {
            result = peekFieldInt(base, fieldID);
        }  catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in peekFieldInt(" + base + ", " + fieldID + ")", ex);
        }
        return VCache.toObject(result);
    }

    void pokeField(Object base, String fieldID, String sig, Object newValue) {
        RefOpener.testNull();
        try {
            pokeFieldInt(base, fieldID, ((Integer)newValue).intValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in pokeFieldInt(" +
                base + ", " + fieldID + ", " + newValue + ")", ex);
        }
        RefOpener.testNull();
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
             int result;
             try {
                 result = peekFieldInt(base, fieldID);
             }  catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in encodeField, peekFieldInt(" + base + ", " + fieldID + ")", ex);
             }
             encoder.writeInt(result);
    }

    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
         throws IOException {
             RefOpener.testNull();
            int result = decoder.readInt();
            try {
                pokeFieldInt(base, fieldID, result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new NestedException("in decodeField, pokeFieldInt(" +
                    base + ", " + fieldID + ", " + result + ")", ex);
            }
            RefOpener.testNull();
    }
}


/**
 * Represents the scalar 'long' type
 */
final class LongOpener extends VarOpener {

    LongOpener() {}

    public Class type() {
        return Long.TYPE;
    }

    void encodeItTo(Object obj, RtEncoder encoder) throws IOException {
        encoder.writeLong(((Long)obj).longValue());
    }

    Object decodeFrom(RtDecoder decoder) throws IOException {
        return VCache.toObject(decoder.readLong());
    }

    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        long[] a = (long[])array;
        for (int i = 0; i < a.length; i++) {
            encoder.writeLong(a[i]);
        }
    }

    void decodeElements(Object array, RtDecoder decoder) throws IOException {
        long[] a = (long[])array;
        for (int i = 0; i < a.length; i++) {
            a[i] = decoder.readLong();
        }
    }

    static private native long peekFieldLong(Object base, String fieldID);

    static private native void pokeFieldLong(Object base, String fieldID,
                                              long newValue);

    Object peekField(Object base, String fieldID, String sig) {
        long result;
        try {
            result = peekFieldLong(base, fieldID);
        }  catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in peekFieldLong(" + base + ", " + fieldID + ")", ex);
        }
        return VCache.toObject(result);
    }

    void pokeField(Object base, String fieldID, String sig, Object newValue) {
        RefOpener.testNull();
        try {
            pokeFieldLong(base, fieldID, ((Long)newValue).longValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in pokeFieldLong(" +
                base + ", " + fieldID + ", " + newValue + ")", ex);
        }
        RefOpener.testNull();
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
             long result;
             try {
                 result = peekFieldLong(base, fieldID);
             }  catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in encodeField, peekFieldLong(" + base + ", " + fieldID + ")", ex);
             }
             encoder.writeLong(result);
    }

    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
         throws IOException {
             RefOpener.testNull();
            long result = decoder.readLong();
            try {
                pokeFieldLong(base, fieldID, result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new NestedException("in decodeField, pokeFieldLong(" +
                    base + ", " + fieldID + ", " + result + ")", ex);
            }
            RefOpener.testNull();
    }
}


/**
 * Represents the scalar 'float' type
 */
final class FloatOpener extends VarOpener {

    FloatOpener() {}

    public Class type() {
        return Float.TYPE;
    }

    void encodeItTo(Object obj, RtEncoder encoder) throws IOException {
        encoder.writeFloat(((Float)obj).floatValue());
    }

    Object decodeFrom(RtDecoder decoder) throws IOException {
        return VCache.toObject(decoder.readFloat());
    }

    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        float[] a = (float[])array;
        for (int i = 0; i < a.length; i++) {
            encoder.writeFloat(a[i]);
        }
    }

    void decodeElements(Object array, RtDecoder decoder) throws IOException {
        float[] a = (float[])array;
        for (int i = 0; i < a.length; i++) {
            a[i] = decoder.readFloat();
        }
    }

    static private native float peekFieldFloat(Object base, String fieldID);

    static private native void pokeFieldFloat(Object base, String fieldID,
                                              float newValue);

    Object peekField(Object base, String fieldID, String sig) {
        float result;
        try {
            result = peekFieldFloat(base, fieldID);
        }  catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in peekFieldFloat(" + base + ", " + fieldID + ")", ex);
        }
        return VCache.toObject(result);
    }

    void pokeField(Object base, String fieldID, String sig, Object newValue) {
        RefOpener.testNull();
        try {
            pokeFieldFloat(base, fieldID, ((Float)newValue).floatValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in pokeFieldFloat(" +
                base + ", " + fieldID + ", " + newValue + ")", ex);
        }
        RefOpener.testNull();
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
             float result;
             try {
                 result = peekFieldFloat(base, fieldID);
             }  catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in encodeField, peekFieldFloat(" + base + ", " + fieldID + ")", ex);
             }
             encoder.writeFloat(result);
    }

    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
         throws IOException {
             RefOpener.testNull();
            float result = decoder.readFloat();
            try {
                pokeFieldFloat(base, fieldID, result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new NestedException("in decodeField, pokeFieldFloat(" +
                    base + ", " + fieldID + ", " + result + ")", ex);
            }
            RefOpener.testNull();
    }
}


/**
 * Represents the scalar 'double' type
 */
final class DoubleOpener extends VarOpener {

    DoubleOpener() {}

    public Class type() {
        return Double.TYPE;
    }

    void encodeItTo(Object obj, RtEncoder encoder) throws IOException {
        encoder.writeDouble(((Double)obj).doubleValue());
    }

    Object decodeFrom(RtDecoder decoder) throws IOException {
        return VCache.toObject(decoder.readDouble());
    }

    void encodeElements(Object array, RtEncoder encoder) throws IOException {
        double[] a = (double[])array;
        for (int i = 0; i < a.length; i++) {
            encoder.writeDouble(a[i]);
        }
    }

    void decodeElements(Object array, RtDecoder decoder) throws IOException {
        double[] a = (double[])array;
        for (int i = 0; i < a.length; i++) {
            a[i] = decoder.readDouble();
        }
    }

    static private native double peekFieldDouble(Object base, String fieldID);

    static private native void pokeFieldDouble(Object base, String fieldID,
                                               double newValue);

    Object peekField(Object base, String fieldID, String sig) {
        double result;
        try {
            result = peekFieldDouble(base, fieldID);
        }  catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in peekFieldDouble(" + base + ", " + fieldID + ")", ex);
        }
        return VCache.toObject(result);
    }

    void pokeField(Object base, String fieldID, String sig, Object newValue) {
        RefOpener.testNull();
        try {
            pokeFieldDouble(base, fieldID, ((Double)newValue).doubleValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NestedException("in pokeFieldDouble(" +
                base + ", " + fieldID + ", " + newValue + ")", ex);
        }
        RefOpener.testNull();
    }

    void encodeField(Object base, String fieldID, String sig, RtEncoder encoder)
         throws IOException {
             double result;
             try {
                 result = peekFieldDouble(base, fieldID);
             }  catch (Exception ex) {
                 ex.printStackTrace();
                 throw new NestedException("in encodeField, peekFieldDouble(" + base + ", " + fieldID + ")", ex);
             }
             encoder.writeDouble(result);
    }

    void decodeField(Object base, String fieldID, String sig, RtDecoder decoder)
        throws IOException {
            RefOpener.testNull();
            double result = decoder.readDouble();
            try {
                pokeFieldDouble(base, fieldID, result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new NestedException("in decodeField, pokeFieldDouble(" +
                    base + ", " + fieldID + ", " + result + ")", ex);
            }
            RefOpener.testNull();
    }
}
