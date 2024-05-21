// NoSuchElementException.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** RuntimeException signaling that an Enumeration is empty. Enumerations
  * throw this exception when <b>nextElement()</b> is called but the
  * Enumeration is empty.
  * @see Enumeration
  */
public class NoSuchElementException extends RuntimeException {
    /** Constructs a NoSuchElementException with no message. */
    public NoSuchElementException() {
        super();
    }

    /** Constructs a NoSuchElementException with a descriptive message. */
    public NoSuchElementException(String s) {
        super(s);
    }
}

