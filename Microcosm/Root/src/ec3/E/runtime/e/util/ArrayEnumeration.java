/*
  ArrayEnumeration.java -- A utility class for handling collection enumeration
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
  A utility class that provides an enumeration of an array.
  Arguably this should be in some more generally useful package than ec.e.hab
  (arguably this should have been part of the standard Java class library all
  along).
*/
public class ArrayEnumeration implements Enumeration {
    private Object array[];
    private int nextIndex;

    /**
    * Construct an Enumeration for a given array.
    *
    * @param array The array to enumerate.
    */
    public ArrayEnumeration(Object array[]) {
        this.array = new Object[array.length];
        System.arraycopy(array, 0, this.array, 0, array.length);
        this.nextIndex = 0;
    }

    /**
    * Test if this Enumeration has more elements.
    *
    * @return true iff we're not done yet.
    */
    public boolean hasMoreElements() {
        return(nextIndex < array.length);
    }

    /**
    * Get the next element in this Enumeration.
    *
    * @return The next element in the Enumeration.
    */
    public Object nextElement() {
        if (nextIndex < array.length)
            return(array[nextIndex++]);
        else
            throw(new NoSuchElementException("ArrayEnumeration is done"));
    }
}
