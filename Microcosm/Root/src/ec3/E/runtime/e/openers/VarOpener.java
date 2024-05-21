package ec.e.openers;

import java.io.IOException;
import java.lang.reflect.Array;
import ec.vcache.VCache;
import ec.tables.Table;
import ec.util.NestedError;

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

    static final int MIN_OPENER_LIBRARY_VERSION = 300;
    static final int RELEASED_OPENER_LIBRARY_VERSION = 300;
    static final int MAX_OPENER_LIBRARY_VERSION = 300;

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

        if (foundVersion < MIN_OPENER_LIBRARY_VERSION)
            throw new Error("Opener native library is version " +
                            foundVersion +
                            ", please upgrade to " +
                            RELEASED_OPENER_LIBRARY_VERSION);

        if (foundVersion > MAX_OPENER_LIBRARY_VERSION)
            throw new Error("Opener native library is version " +
                            foundVersion +
                            ", but system cannot use later than " +
                            MAX_OPENER_LIBRARY_VERSION);
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
     * opener at byteOffset within base, this returns it, wrapping as
     * appropriate.  Our caller must ensure that these assumptions are
     * correct.
     *
     * @return nullOk, suspect;
     */
    abstract Object peekField(Object base, int byteOffset);

    /**
     * DANGEROUS, PACKAGE SCOPE ONLY. For use by Field. ASSUMING
     * there's an instance variable of the type described by this
     * opener at byteOffset within base, this sets it, unwrapping as
     * appropriate.  Our caller must ensure that these assumptions are
     * correct.
     *
     * @param newValue nullOk, suspect;
     * @exception NullPointerException when assigning into a scalar
     * and newValue is null, or when 'base' is null.
     * @exception ClassCastException when this opener's type is not
     * assignable from newValue;
     */
    abstract void pokeField(Object base, int byteOffset, Object newValue)
         throws NullPointerException, ClassCastException;

    /**
     * DANGEROUS, PACKAGE SCOPE ONLY.  Provided to factor out a common
     * case for use by subclasses.  Returns the 32 bits at byteOffset
     * within base as an int.
     */
    static native int peekField32(Object base, int byteOffset);

    /**
     * DANGEROUS, PACKAGE SCOPE ONLY.  Provided to factor out a common
     * case for use by subclasses.  Sets the 32 bits at byteOffset
     * within base to newValue.
     */
    static native void pokeField32(Object base, int byteOffset, int newValue);

    /**
     * DANGEROUS, PACKAGE SCOPE ONLY. For use by Field. ASSUMING
     * there's an instance variable of the type described by this
     * opener at byteOffset within base, this encodes it according to
     * this type.  Our caller must ensure that these assumptions are
     * correct. <p>
     *
     * Subclasses may wish to override for efficiency, but the default
     * implementation is correct (and serves as a precise
     * specification).
     *
     * @param encoder suspect;
     */
    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {
        encodeItTo(peekField(base, byteOffset), encoder);
    }

    /**
     * DANGEROUS, PACKAGE SCOPE ONLY. For use by Field. ASSUMING
     * there's an instance variable of the type described by this
     * opener at byteOffset within base, this decodes a value
     * according to this type, and set the variable to this value.  Our
     * caller must ensure that these assumptions are correct. <p>
     *
     * Subclasses may wish to override for efficiency, but the default
     * implementation is correct (and serves as a precise
     * specification).
     *
     * @param decoder suspect;
     */
    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {
        pokeField(base, byteOffset, decodeFrom(decoder));
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

    Object peekField(Object base, int byteOffset) {
        if (peekField32(base, byteOffset) != 0) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    void pokeField(Object base, int byteOffset, Object newValue) {
        if (((Boolean)newValue).booleanValue()) {
            pokeField32(base, byteOffset, 1);
        } else {
            pokeField32(base, byteOffset, 0);
        }
    }

    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {

        if (peekField32(base, byteOffset) != 0) {
            encoder.writeBoolean(true);
        } else {
            encoder.writeBoolean(false);
        }
    }

    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {

        if (decoder.readBoolean()) {
            pokeField32(base, byteOffset, 1);
        } else {
            pokeField32(base, byteOffset, 0);
        }
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

    Object peekField(Object base, int byteOffset) {
        return VCache.toObject((byte)peekField32(base, byteOffset));
    }

    void pokeField(Object base, int byteOffset, Object newValue) {
        pokeField32(base, byteOffset, ((Byte)newValue).byteValue());
    }

    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {
        encoder.writeByte(peekField32(base, byteOffset));
    }

    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {
        pokeField32(base, byteOffset, decoder.readByte());
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

    Object peekField(Object base, int byteOffset) {
        return VCache.toObject((char)peekField32(base, byteOffset));
    }

    void pokeField(Object base, int byteOffset, Object newValue) {
        pokeField32(base, byteOffset, ((Character)newValue).charValue());
    }

    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {
        encoder.writeChar(peekField32(base, byteOffset));
    }

    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {
        pokeField32(base, byteOffset, decoder.readChar());
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

    Object peekField(Object base, int byteOffset) {
        return VCache.toObject((short)peekField32(base, byteOffset));
    }

    void pokeField(Object base, int byteOffset, Object newValue) {
        pokeField32(base, byteOffset, ((Short)newValue).shortValue());
    }

    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {
        encoder.writeShort(peekField32(base, byteOffset));
    }

    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {
        pokeField32(base, byteOffset, decoder.readShort());
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

    Object peekField(Object base, int byteOffset) {
        return VCache.toObject(peekField32(base, byteOffset));
    }

    void pokeField(Object base, int byteOffset, Object newValue) {
        pokeField32(base, byteOffset, ((Integer)newValue).intValue());
    }

    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {
        encoder.writeInt(peekField32(base, byteOffset));
    }

    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {
        pokeField32(base, byteOffset, decoder.readInt());
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

    //hack to avoid conflicting structure passing conventions.  Low
    //and High are by significance, not necessarily memory order.
    static private long peekFieldLong(Object base, int byteOffset) {
        return (((long)peekFieldLow(base, byteOffset) & 0xffffffffL)
                | ((long)peekFieldHigh(base, byteOffset) << 32));
    }

    static private native int peekFieldLow(Object base, int byteOffset);

    static private native int peekFieldHigh(Object base, int byteOffset);

    static private void pokeFieldLong(Object base, int byteOffset,
                                      long newValue) {
        pokeFieldLow(base, byteOffset, (int)newValue);
        pokeFieldHigh(base, byteOffset, (int)(newValue >> 32));
    }

    static private native void pokeFieldLow(Object base, int byteOffset,
                                            int newValue);

    static private native void pokeFieldHigh(Object base, int byteOffset,
                                             int newValue);

    Object peekField(Object base, int byteOffset) {
        return VCache.toObject(peekFieldLong(base, byteOffset));
    }

    void pokeField(Object base, int byteOffset, Object newValue) {
        pokeFieldLong(base, byteOffset, ((Long)newValue).longValue());
    }

    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {
        encoder.writeLong(peekFieldLong(base, byteOffset));
    }

    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {
        pokeFieldLong(base, byteOffset, decoder.readLong());
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

    static private native float peekFieldFloat(Object base, int byteOffset);

    static private native void pokeFieldFloat(Object base, int byteOffset,
                                              float newValue);

    Object peekField(Object base, int byteOffset) {
        return VCache.toObject(peekFieldFloat(base, byteOffset));
    }

    void pokeField(Object base, int byteOffset, Object newValue) {
        pokeFieldFloat(base, byteOffset, ((Float)newValue).floatValue());
    }

    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {
        encoder.writeFloat(peekFieldFloat(base, byteOffset));
    }

    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {
        pokeFieldFloat(base, byteOffset, decoder.readFloat());
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

    /**
     * XXX!!! BUG!!! returning a float instead of a double
     */
    static private native float peekFieldDouble(Object base, int byteOffset);

    static private native void pokeFieldDouble(Object base, int byteOffset,
                                               double newValue);

    Object peekField(Object base, int byteOffset) {
        return VCache.toObject(peekFieldDouble(base, byteOffset));
    }

    void pokeField(Object base, int byteOffset, Object newValue) {
        pokeFieldDouble(base, byteOffset, ((Double)newValue).doubleValue());
    }

    void encodeField(Object base, int byteOffset, RtEncoder encoder)
         throws IOException {
        encoder.writeDouble(peekFieldDouble(base, byteOffset));
    }

    void decodeField(Object base, int byteOffset, RtDecoder decoder)
         throws IOException {
        pokeFieldDouble(base, byteOffset, decoder.readDouble());
    }
}
