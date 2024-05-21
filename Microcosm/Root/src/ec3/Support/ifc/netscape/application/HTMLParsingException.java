// HTMLParsingException.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/** Exception signaling that an HTML parsing exception has occurred. This
  * exception also provides a means for an HTML parser client to determine
  * which line generated the exception.
  */
public class HTMLParsingException extends Exception {
    int lineNumber = -1;

    /** Constructs an empty HTMLParsingException.
      */
    private HTMLParsingException() {
        super();
    }

    /** Constructs an HTMLParsingException with the descriptive message
      * <b>string</b>, and the line number on which the error occurred.
      */
    public HTMLParsingException(String string, int lineNumber) {
        super(string);
        this.lineNumber = lineNumber;
    }

    /** Returns the line number at which the HTMLParsingException
      * occurred.
      */
    public int lineNumber() {
        return lineNumber;
    }
}
