/*
  EmptyEnumeration.java -- A utility class for handling collection enumeration
                           better than Java.

  Chip Morningstar
  Electric Communities
  7-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.

*/

package ec.e.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
  A utility class that provides an enumeration with no elements in it.
  Arguably this should have been part of the standard Java class library all
  along.
*/
public class EmptyEnumeration implements Enumeration {

    static public final Enumeration THE_ONE = new EmptyEnumeration();

    /**
    * All purpose constructor
    */
    public EmptyEnumeration() { }

    /**
    * Test if this Enumeration has more elements (hint: it doesn't).
    *
    * @return false, always
    */
    public boolean hasMoreElements() {
        return(false);
    }

    /**
    * Get the (non-existent) next element in this Enumeration.
    *
    * @return Never returns, always throws a NoSuchElementException
    */
    public Object nextElement() {
        throw(new NoSuchElementException("EmptyEnumeration never has elements"));
    }
}
