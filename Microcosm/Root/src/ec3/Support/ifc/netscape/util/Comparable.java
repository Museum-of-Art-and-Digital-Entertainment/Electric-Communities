// Comparable.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** Interface implemented by objects to enable sorting algortithms to sort
  * collections of homogenous instances.
  */
public interface Comparable {
    /** Compares two objects. Returns <b>-1</b> if "this < other", returns
      * <b>1</b> if "this > other", and returns <b>0</b> if "this == other".
      * The other object must be the same type as this object, otherwise the
      * ClassCastException is thrown.
      * @exception ClassCastException The other object is not of the same type.
      */
    public int compareTo(Object other);
}
