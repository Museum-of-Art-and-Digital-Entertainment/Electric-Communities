// ExternalCoder.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** This may one day be public, but for now it is just the mechanism by
  * which we encode/decode primitive types.
  * @private
  */
interface ExternalCoder {
    public Object newInstance(String className) throws CodingException;
    public void describeClassInfo(Object object, ClassInfo info);
    public void encode(Object object, Encoder encoder) throws CodingException;
    public void decode(Object object, Decoder decoder) throws CodingException;
    public void finishDecoding(Object object) throws CodingException;
}
