/*
  IndirectElement.java -- A utility class for handling collection enumeration
                          better than Java.

  Chip Morningstar
  Electric Communities
  7-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.

*/

package ec.e.util;

/**
  This interface allows an object that is an element of an Enumeration to
  provide an object of its choice in place of itself when the Enumeration is
  iterated over.

  Arguably this should have been part of the standard Java class library all
  along.
*/
public interface IndirectElement {
    /**
    * Provide the object that this object wishes to have represent it in an
    * Enumeration.
    */
    Object getElement();
}
