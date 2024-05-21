/*
  IndirectEnumeration.java -- A utility class for handling collection
                              enumeration better than Java.

  Chip Morningstar
  Electric Communities
  7-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.

*/

package ec.e.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
  A utility class that provides an Enumeration of things referenced by the
  elements of another Enumeration.
*/
public class IndirectEnumeration implements Enumeration {
    private Enumeration enum;

    /**
    * Construct an IndirectEnumeration from an Enumeration.
    *
    * @param enum The Enumeration to enumerate with indirection applied. Its
    *   elements should all implement the IndirectElement interface.
    */
    public IndirectEnumeration(Enumeration enum) {
        this.enum = enum;
    }

    /**
    * Test if this Enumeration has more elements.
    *
    * @return true iff we're not done yet.
    */
    public boolean hasMoreElements() {
        return(enum.hasMoreElements());
    }

    /**
    * Get the next element in this Enumeration.
    *
    * @return The next (indirect) element in the Enumeration.
    */
    public Object nextElement() {
        Object indirect = enum.nextElement();
        if (indirect instanceof IndirectElement())
            return(((IndirectElement)indirect).getElement());
        else
            throw(new NoSuchElementException("non-IndirectElement in IndirectEnumeration"));
    }
}
