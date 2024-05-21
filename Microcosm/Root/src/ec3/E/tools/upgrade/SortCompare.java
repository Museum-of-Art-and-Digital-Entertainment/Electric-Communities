// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;


/**
 * This interface defines a compare function for the Sort class.
 */
public interface SortCompare {

    /**
     * Describe which element is greater
     *
     * @param m1 The first element.  It may be null if the object being sorted
     * contains null elements.
     *
     * @param m2 The second element  It may be null if the object being sorted
     * contains null elements.
     *
     * @return An integer as follows: if m1<m2 then the integer is negative.
     * If m1==m2 the the integer is zero.  If m1>m2 then the integer is greater
     * than zero.
     */

    public int compare(Object /*nilok*/ m1, Object /*nilok*/ m2);
}