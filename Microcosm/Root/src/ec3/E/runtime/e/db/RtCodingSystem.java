package ec.e.db;

import java.util.Hashtable;

public class RtCodingSystem {

    static Hashtable specialObjectTable = new Hashtable();

    /**/
    /* Codes specifying what is in stream */
    /**/
    /*static final int kcClassInfo = 1; No longer used (GJF 1/24/97) */
    static final int kcObjectInfo = 2;
    static final int kcObjectID = 3;
    static final int kcSpecialObject = 4;

    /**/
    /* Misc codes */
    /**/
    static final int kcNull = 0;             /* For null objects */
    static final int kcManagerEncoded = 1;   /* Encoded by EncodingManager */
    static final int kcStreamEncoded = 2;    /* Encoded by the stream */
    static final int kcObjectEncoded = 3;    /* Encoded by the object itself */
    static final int kcParameterObject = 4;  /* Pruning Parameter Object */

    /**/
    /* Codes specifying data type */
    /**/
    static final int kcDataBoolean = 1;
    static final int kcDataByte = 2;
    static final int kcDataBytes = 3;
    static final int kcDataChar = 4;
    static final int kcDataChars = 5;
    static final int kcDataShort = 6;
    static final int kcDataInt = 7;
    static final int kcDataLong = 8;
    static final int kcDataFloat = 9;
    static final int kcDataDouble = 10;
    static final int kcDataObject = 11;
    static final int kcDataUTF = 12;

    /**/
    /* Codes specifying standard or unique coding */
    /**/
    static final int kcStandardObjectId = 1;
    static final int kcNewUniqueObjectId = 2;
    static final int kcOldUniqueObjectId = 3;

    /**/
    /* Special Coding classes */
    /**/
    static public void registerSpecialCodingClass (Class theClass,
                                                   RtSpecialObjectCoder owner,
                                                   Object arg) {
        Object info[] = new Object[2];
        info[0] = owner;
        info[1] = arg;
        specialObjectTable.put(theClass, info);
    }

    private static Class ThrowableClass = Throwable.class;

    static Object[] specialObjectCoderForClass (Class theClass) {
        Object[] result = (Object[]) specialObjectTable.get(theClass);
        if ((result == null) && (ThrowableClass.isAssignableFrom(theClass))) {
            result = new Object[2];
            result[0] = RtSpecialCoder.theSpecialCoder;
            result[1] =
                new Integer(RtSpecialCoder.kObjectThrowableClass);
        }
        return result;
    }
}
