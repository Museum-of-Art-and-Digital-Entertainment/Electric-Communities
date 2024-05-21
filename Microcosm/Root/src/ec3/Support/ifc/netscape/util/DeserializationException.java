// DeserializationException.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** Exception signaling an exceptional condition during deserialization.  This
  * exception also provides a means for a Deserializer client to determine
  * which line generated the exception.
  */
public class DeserializationException extends Exception {
    int lineNumber = -1;

    /** Constructs an empty DeserializationException.
      */
    private DeserializationException() {
        super();
    }

    /** Constructs a DeserializationException with the descriptive message
      * <b>string</b> and <b>lineNumber</b>, the line number on which
      * the error occurred.
      */
    public DeserializationException(String string, int lineNumber) {
        super(string);
        this.lineNumber = lineNumber;
    }

    /** Returns the line number at which the DeserializationException
      * occurred.
      */
    public int lineNumber() {
        return lineNumber;
    }
}
