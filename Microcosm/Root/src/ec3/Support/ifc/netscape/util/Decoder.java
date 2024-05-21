// Decoder.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** The Decoder interface describes the API through which objects decode
  * their essential state as a set of key-value pairs. Unarchiver implements
  * this API to decode the state of a graph of objects from an Archive.
  * @see Codable
  * @see Encoder
  * @see Unarchiver
  * @see Archive
  */
public interface Decoder {
    /** Returns the version information for the class named <b>className</b>.
      * Objects can use this information to bring forward old encodings at
      * runtime.
      */
    public int versionForClassName(String className) throws CodingException;

    /** Decodes the boolean value associated with the string <b>key</b>.
      */
    public boolean decodeBoolean(String key) throws CodingException;

    /** Decodes the boolean array associated with the string <b>key</b>.
      */
    public boolean[] decodeBooleanArray(String key) throws CodingException;

    /** Decodes the character value associated with the string <b>key</b>.
      */
    public char decodeChar(String key) throws CodingException;

    /** Decodes the character array associated with the string <b>key</b>.
      */
    public char[] decodeCharArray(String key) throws CodingException;

    /** Decodes the byte value associated with the string <b>key</b>.
      */
    public byte decodeByte(String key) throws CodingException;

    /** Decodes the byte array associated with the string <b>key</b>.
      */
    public byte[] decodeByteArray(String key) throws CodingException;

    /** Decodes the short value associated with the string <b>key</b>.
      */
    public short decodeShort(String key) throws CodingException;

    /** Decodes the short array associated with the string <b>key</b>.
      */
    public short[] decodeShortArray(String key) throws CodingException;

    /** Decodes the integer value associated with the string <b>key</b>.
      */
    public int decodeInt(String key) throws CodingException;

    /** Decodes the integer array associated with the string <b>key</b>.
      */
    public int[] decodeIntArray(String key) throws CodingException;

    /** Decodes the long value associated with the string <b>key</b>.
      */
    public long decodeLong(String key) throws CodingException;

    /** Decodes the long array value associated with the string <b>key</b>.
      */
    public long[] decodeLongArray(String key) throws CodingException;

    /** Decodes the float value associated with the string <b>key</b>.
      */
    public float decodeFloat(String key) throws CodingException;

    /** Decodes the float array associated with the string <b>key</b>.
      */
    public float[] decodeFloatArray(String key) throws CodingException;

    /** Decodes the double value associated with the string <b>key</b>.
      */
    public double decodeDouble(String key) throws CodingException;

    /** Decodes the double array associated with the string <b>key</b>.
      */
    public double[] decodeDoubleArray(String key) throws CodingException;

    /** Decodes the string value associated with the string <b>key</b>.
      */
    public String decodeString(String key) throws CodingException;

    /** Decodes the string array associated with the string <b>key</b>.
      */
    public String[] decodeStringArray(String key) throws CodingException;

    /** Decodes a reference to another Codable object.
      */
    public Object decodeObject(String key) throws CodingException;

    /** Decodes an array of Codable objects. The references to the Codable
      * objects are shared, but the reference to the array is not.
      */
    public Object[] decodeObjectArray(String key) throws CodingException;

    /** Replaces references to the object currently being decoded with
      * <b>replacement</b>. This method throws a CodingException when an
      * attempt is made to replace an object which has already been seen
      * by other objects. For maximum safety, this method should only be
      * called from leaves of the object graph.
      */
    public void replaceObject(Object replacement) throws CodingException;
}
