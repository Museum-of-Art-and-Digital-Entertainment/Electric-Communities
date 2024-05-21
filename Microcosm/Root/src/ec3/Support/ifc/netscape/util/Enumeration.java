// Enumeration.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** The Enumeration interface specifies an API enabling iteration through a set
  * of elements. The following code demonstrates how to print all the values of
  * a Hashtable using an Enumeration:
  * <pre>
  *     Enumeration enumeration;
  *     Object key;
  *     enumeration = table.keys();
  *     while (enumeration.hasMoreElements()) {
  *         key = enumeration.nextElement();
  *         System.out.println("key = " + key + ", value = " + table.get(key);
  *     }
  * </pre>
  *
  * @see NoSuchElementException
  */
public interface Enumeration {
    /** Returns <b>true</b> if there are additional elements to enumerate.
      */
    boolean hasMoreElements();

    /** Returns the next element of the enumeration. This method throws a
      * <b>NoSuchElementException</b> if <b>hasMoreElements()</b> returns
      * <b>false</b>.
      */
    Object nextElement();
}
