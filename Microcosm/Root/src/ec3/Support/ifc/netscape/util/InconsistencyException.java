// InconsistencyException.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** Error signaling that an inconsistent internal state has been
  * discovered. Classes throw this error when something that should be
  * impossible has happened. This error is usually due to incorrect program
  * logic.
  */
public class InconsistencyException extends Error {
    /** Constructs an InconsistencyException with no message.
      */
    public InconsistencyException() {
        super();
    }

    /** Constructs an InconsistencyException with a descriptive message.
      */
    public InconsistencyException(String s) {
        super(s);
    }
}
