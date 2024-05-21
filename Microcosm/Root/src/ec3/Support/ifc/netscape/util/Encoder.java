// Encoder.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** The Encoder interface describes the API through which objects encode
  * their essential state as a set of key-value pairs. Archiver implements
  * this API to encode the state of a graph of objects into an Archive.
  *
  * @see Codable
  * @see Archiver
  * @see Archive
  */
public interface Encoder {
    /** Encodes the boolean <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeBoolean(String key, boolean value)
        throws CodingException;

    /** Encodes the boolean array <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeBooleanArray(String key, boolean value[], int offset,
        int length) throws CodingException;

    /** Encodes the character <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeChar(String key, char value) throws CodingException;

    /** Encodes the character array <b>value</b>, associating it with the
      * string <b>key</b>.
      */
    public void encodeCharArray(String key, char value[], int offset,
        int length) throws CodingException;

    /** Encodes the byte <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeByte(String key, byte value) throws CodingException;

    /** Encodes the byte array <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeByteArray(String key, byte value[], int offset,
        int length) throws CodingException;

    /** Encodes the short <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeShort(String key, short value) throws CodingException;

    /** Encodes the short array <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeShortArray(String key, short value[], int offset,
        int length) throws CodingException;

    /** Encodes the integer <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeInt(String key, int value) throws CodingException;

    /** Encodes the integer array <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeIntArray(String key, int value[], int offset,
        int length) throws CodingException;

    /** Encodes the long <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeLong(String key, long value) throws CodingException;

    /** Encodes the long array <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeLongArray(String key, long value[], int offset,
        int length) throws CodingException;

    /** Encodes the float <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeFloat(String key, float value) throws CodingException;

    /** Encodes the float array <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeFloatArray(String key, float value[], int offset,
        int length) throws CodingException;

    /** Encodes the double <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeDouble(String key, double value) throws CodingException;

    /** Encodes the double array <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeDoubleArray(String key, double value[], int offset,
        int length) throws CodingException;

    /** Encodes the string <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeString(String key, String value) throws CodingException;

    /** Encodes the string array <b>value</b>, associating it with the string
      * <b>key</b>.
      */
    public void encodeStringArray(String key, String value[], int offset,
        int length) throws CodingException;

    /** Encodes a reference to another Codable object. If multiple objects
      * reference the same object and each passes it to <b>encodeObject()</b>,
      * only one copy of that object is actually encoded.
      */
    public void encodeObject(String key, Object value)
        throws CodingException;

    /** Encodes an array of Codable objects. The reference to the array is
      * not shared, but references to the objects in the array are.
      */
    public void encodeObjectArray(String key, Object value[], int offset,
        int length) throws CodingException;
}
