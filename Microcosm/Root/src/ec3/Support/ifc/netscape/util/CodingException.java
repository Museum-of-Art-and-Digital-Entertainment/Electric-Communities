// CodingException.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** Exception signaling an exceptional condition during an object's encoding or
  * decoding.
  * @see Encoder
  */
public class CodingException extends Exception {
    /** Constructs a CodingException.
      */
    public CodingException() {
        super();
    }

    /** Constructs a CodingException with descriptive message <b>string</b>.
      */
    public CodingException(String string) {
        super(string);
    }
}
