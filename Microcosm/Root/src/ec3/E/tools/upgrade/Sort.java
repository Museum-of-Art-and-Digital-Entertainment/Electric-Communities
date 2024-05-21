// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;

import java.util.Vector;


/**
 * This class provides methods to perform various kinds of sorts.
 */
public class Sort {

    /**
     * Sort a vector in place based on a compare class.  This method uses a
     * Shell sort
     *
     * @param vec The Vector to be sorted in place
     * @param comp An object implementing SortCompare which performs the 
     * comparison operation.
     */

    public static void sortVector(Vector vec, SortCompare comp) {
    //Sorts on name and types of parameters and return type with a Shell sort
        int length = vec.size();
        int h = length >> 1;
        while (h>0) {
            for (int j=h; j<length; j++) {
                int i = j-h;
                Object r = vec.elementAt(j);
                while (true) {
                    if (comp.compare(r, vec.elementAt(i)) < 0) {
                        vec.setElementAt(vec.elementAt(i), i+h);
                        i -= h;
                        if (i >= 0) continue;
                    } 
                    vec.setElementAt(r, i+h);
                    break;
                }
            }
            h>>=1;
        }
    }
}