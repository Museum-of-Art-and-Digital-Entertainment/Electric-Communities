/*
  CompoundEnumeration.java -- A utility class for handling collection
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
  A utility class that provides an enumeration of a set of enumerations.
  Arguably this should have been part of the standard Java class library all
  along.
*/
public class CompoundEnumeration implements Enumeration {
    private Enumeration enums[];
    private int currentSet;

    /**
    * Construct an Enumeration from a pair of them.
    *
    * @param enum1 The first Enumeration
    * @param enum2 The second Enumeration
    */
    public CompoundEnumeration(Enumeration enum1, Enumeration enum2) {
        enums = new Enumeration[2];
        enums[0] = enum1;
        enums[1] = enum2;
        currentSet = 0;
    }

    /**
    * Construct an Enumeration from three of them.
    *
    * @param enum1 The first Enumeration
    * @param enum2 The second Enumeration
    * @param enum3 The third Enumeration
    */
    public CompoundEnumeration(Enumeration enum1, Enumeration enum2,
                               Enumeration enum3) {
        enums = new Enumeration[3];
        enums[0] = enum1;
        enums[1] = enum2;
        enums[2] = enum3;
        currentSet = 0;
    }

    /**
    * Test if this Enumeration has more elements.
    *
    * @return true iff we're not done yet.
    */
    public boolean hasMoreElements() {
        while (currentSet < enums.length) {
            if (enums[currentSet].hasMoreElements()) {
                return true;
            }
            currentSet++;
        }
        return false;
    }

    /**
    * Get the next element in this Enumeration.
    *
    * @return The next element in the Enumeration.
    */
    public Object nextElement() {
        while (currentSet < enums.length) {
            if (enums[currentSet].hasMoreElements()) {
                return enums[currentSet].nextElement();
            }
            currentSet++;
        }
        throw(new NoSuchElementException("CompoundEnumeration is done"));
    }
}
