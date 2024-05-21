// PrimitiveCoder.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** This class knows how to encode/decode all the primitive data types. We
  * can't properly recreate "arrays of arrays of ... TYPE" in the current
  * java language and APIs so we have to punt. The two Codable types which
  * are not considered "primitive" are OBJECT_TYPE and OBJECT_ARRAY_TYPE.
  * Take a look at this again when we get the reflection APIs. ALERT!
  */
class PrimitiveCoder implements ExternalCoder {
    byte type;

    /** Constructs a PrimitiveCoder for a given type.
      * @see Codable
      */
    PrimitiveCoder(byte type) {
        super();
        this.type = type;
    }

    /** This just returns the null. We will call replaceObject() during
      * decode with the real value.
      */
    public Object newInstance(String className) throws CodingException {
        return null;
    }

    static String classNameForType(byte type) {
        // These are the class names for all the primitive types.  Some of
        // these classes won't exist until JDK 1.1.

        switch (type) {
            case Codable.BOOLEAN_TYPE:       return "java.lang.Boolean";
            case Codable.BOOLEAN_ARRAY_TYPE: return "[Z";
            case Codable.CHAR_TYPE:          return "java.lang.Character";
            case Codable.CHAR_ARRAY_TYPE:    return "[C";
            case Codable.BYTE_TYPE:          return "java.lang.Byte";
            case Codable.BYTE_ARRAY_TYPE:    return "[B";
            case Codable.SHORT_TYPE:         return "java.lang.Short";
            case Codable.SHORT_ARRAY_TYPE:   return "[S";
            case Codable.INT_TYPE:           return "java.lang.Integer";
            case Codable.INT_ARRAY_TYPE:     return "[I";
            case Codable.LONG_TYPE:          return "java.lang.Long";
            case Codable.LONG_ARRAY_TYPE:    return "[J";
            case Codable.FLOAT_TYPE:         return "java.lang.Float";
            case Codable.FLOAT_ARRAY_TYPE:   return "[F";
            case Codable.DOUBLE_TYPE:        return "java.lang.Double";
            case Codable.DOUBLE_ARRAY_TYPE:  return "[D";
            case Codable.STRING_TYPE:        return "java.lang.String";
            case Codable.STRING_ARRAY_TYPE:  return "[Ljava.lang.String;";
            case Codable.OBJECT_TYPE:
            case Codable.OBJECT_ARRAY_TYPE:
            default:
                throw new InconsistencyException("Non-primitive type!");
        }
    }

    String className() {
        return classNameForType(type);
    }

    public void describeClassInfo(Object object, ClassInfo info) {
        info.addClass(classNameForType(type), 1);
        info.addField("value", type);
    }

    public void encode(Object object, Encoder encoder) throws CodingException {
        switch (type) {
            case Codable.BOOLEAN_TYPE:
                encoder.encodeBoolean("value",
                    ((Boolean)object).booleanValue());
                break;
            case Codable.BOOLEAN_ARRAY_TYPE:
                encoder.encodeBooleanArray("value", (boolean[])object,
                    0, ((boolean[])object).length);
                break;
            case Codable.CHAR_TYPE:
                encoder.encodeChar("value",
                    ((Character)object).charValue());
                break;
            case Codable.CHAR_ARRAY_TYPE:
                encoder.encodeCharArray("value", (char[])object,
                    0, ((char[])object).length);
                break;
            case Codable.BYTE_TYPE:
                throw new CodingException("java.lang.Byte not supported");
            case Codable.BYTE_ARRAY_TYPE:
                encoder.encodeByteArray("value", (byte[])object,
                    0, ((byte[])object).length);
                break;
            case Codable.SHORT_TYPE:
                throw new CodingException("java.lang.Short not supported");
            case Codable.SHORT_ARRAY_TYPE:
                encoder.encodeShortArray("value", (short[])object,
                    0, ((short[])object).length);
                break;
            case Codable.INT_TYPE:
                encoder.encodeInt("value", ((Integer)object).intValue());
                break;
            case Codable.INT_ARRAY_TYPE:
                encoder.encodeIntArray("value", (int[])object,
                    0, ((int[])object).length);
                break;
            case Codable.LONG_TYPE:
                encoder.encodeLong("value", ((Long)object).longValue());
                break;
            case Codable.LONG_ARRAY_TYPE:
                encoder.encodeLongArray("value", (long[])object,
                    0, ((long[])object).length);
                break;
            case Codable.FLOAT_TYPE:
                encoder.encodeFloat("value", ((Float)object).floatValue());
                break;
            case Codable.FLOAT_ARRAY_TYPE:
                encoder.encodeFloatArray("value", (float[])object,
                    0, ((float[])object).length);
                break;
            case Codable.DOUBLE_TYPE:
                encoder.encodeDouble("value", ((Double)object).doubleValue());
                break;
            case Codable.DOUBLE_ARRAY_TYPE:
                encoder.encodeDoubleArray("value", (double[])object,
                    0, ((double[])object).length);
                break;
            case Codable.STRING_TYPE:
                encoder.encodeString("value", (String)object);
                break;
            case Codable.STRING_ARRAY_TYPE:
                encoder.encodeStringArray("value", (String[])object,
                    0, ((String[])object).length);
                break;
            case Codable.OBJECT_TYPE:
            case Codable.OBJECT_ARRAY_TYPE:
            default:
                throw new CodingException("Non-primitive type!");
        }
    }

    public void decode(Object object, Decoder decoder) throws CodingException {
        Object value;

        switch (type) {
            case Codable.BOOLEAN_TYPE:
                if (decoder.decodeBoolean("value"))
                    value = Boolean.TRUE;
                else
                    value = Boolean.FALSE;
                break;
            case Codable.BOOLEAN_ARRAY_TYPE:
                value = decoder.decodeBooleanArray("value");
                break;
            case Codable.CHAR_TYPE:
                value = new Character(decoder.decodeChar("value"));
                break;
            case Codable.CHAR_ARRAY_TYPE:
                value = decoder.decodeCharArray("value");
                break;
            case Codable.BYTE_TYPE:
                throw new CodingException("java.lang.Byte not supported");
            case Codable.BYTE_ARRAY_TYPE:
                value = decoder.decodeByteArray("value");
                break;
            case Codable.SHORT_TYPE:
                throw new CodingException("java.lang.Short not supported");
            case Codable.SHORT_ARRAY_TYPE:
                value = decoder.decodeShortArray("value");
                break;
            case Codable.INT_TYPE:
                value = new Integer(decoder.decodeInt("value"));
                break;
            case Codable.INT_ARRAY_TYPE:
                value = decoder.decodeIntArray("value");
                break;
            case Codable.LONG_TYPE:
                value = new Long(decoder.decodeLong("value"));
                break;
            case Codable.LONG_ARRAY_TYPE:
                value = decoder.decodeLongArray("value");
                break;
            case Codable.FLOAT_TYPE:
                value = new Float(decoder.decodeFloat("value"));
                break;
            case Codable.FLOAT_ARRAY_TYPE:
                value = decoder.decodeFloatArray("value");
                break;
            case Codable.DOUBLE_TYPE:
                value = new Double(decoder.decodeDouble("value"));
                break;
            case Codable.DOUBLE_ARRAY_TYPE:
                value = decoder.decodeDoubleArray("value");
                break;
            case Codable.STRING_TYPE:
                value = decoder.decodeString("value");
                break;
            case Codable.STRING_ARRAY_TYPE:
                value = decoder.decodeStringArray("value");
                break;
            case Codable.OBJECT_TYPE:
            case Codable.OBJECT_ARRAY_TYPE:
            default:
                throw new CodingException("Non-primitive type!");
        }

        decoder.replaceObject(value);
    }

    public void finishDecoding(Object object) throws CodingException {
    }
}
