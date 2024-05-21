/*
  DiscreteEnumeration.java -- A utility class for handling collection
                              enumeration better than Java.

  Chip Morningstar
  Electric Communities
  7-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.

*/

package ec.e.util;

import java.util.Enumeration;

/**
  A utility class that provides enumerations over small, fixed-size groups of
  objects.
*/
public class DiscreteEnumeration implements Enumeration {
    private ArrayEnumeration enum;

    /** Make an Enumeration over one object */
    public DiscreteEnumeration(Object obj1) {
        Object elems[] = new Object[1];
        elems[0] = obj1;
        enum = new ArrayEnumeration(elems);
    }

    /** Make an Enumeration over two objects */
    public DiscreteEnumeration(Object obj1, Object obj2) {
        Object elems[] = new Object[2];
        elems[0] = obj1;
        elems[1] = obj2;
        enum = new ArrayEnumeration(elems);
    }

    /** Make an Enumeration over three objects */
    public DiscreteEnumeration(Object obj1, Object obj2, Object obj3) {
        Object elems[] = new Object[3];
        elems[0] = obj1;
        elems[1] = obj2;
        elems[2] = obj3;
        enum = new ArrayEnumeration(elems);
    }

    /** Make an Enumeration over four objects */
    public DiscreteEnumeration(Object obj1, Object obj2, Object obj3,
                               Object obj4) {
        Object elems[] = new Object[4];
        elems[0] = obj1;
        elems[1] = obj2;
        elems[2] = obj3;
        elems[3] = obj4;
        enum = new ArrayEnumeration(elems);
    }

    /** Make an Enumeration over five objects */
    public DiscreteEnumeration(Object obj1, Object obj2, Object obj3,
                               Object obj4, Object obj5) {
        Object elems[] = new Object[5];
        elems[0] = obj1;
        elems[1] = obj2;
        elems[2] = obj3;
        elems[3] = obj4;
        elems[4] = obj5;
        enum = new ArrayEnumeration(elems);
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
    * @return The next element in the Enumeration.
    */
    public Object nextElement() {
        return(enum.nextElement());
    }
}
