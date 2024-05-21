/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 970616, but quicksort based on other EC code
 */

package ec.util;
import java.util.Vector;

/**

 * Some sort utilities collected into one place.

 */
  

public class SortUtilities {

    static void swap(String a[], int x, int y) {
        String tmp = a[x];
        a[x] = a[y];
        a[y] = tmp;
    }

    static int divide(String a[], int low, int high) {
        if (low >= high) {
            return low;
        }
        int split = (low + high ) / 2; // Simple and deterministic

        swap(a, low, split);
        split = low;
        low++;
        while (true) {
            while ((low < high) && (a[split].compareTo(a[low]) >= 0)) {
                low++;
            }
            while ((high > low) && (a[high].compareTo(a[split]) >= 0)) {
                high--;
            }
            if (low < high) {
                swap(a, low, high);
            }
            else {
                break;
            }
        }
        if (a[split].compareTo(a[low]) < 0) {
            low--;
        }
        swap(a, low, split);
        return low;
    }

    static int divideInvert(String a[], int low, int high) {
        if (low >= high) {
            return low;
        }
        int split = (low + high ) / 2; // Simple and deterministic

        swap(a, low, split);
        split = low;
        low++;
        while (true) {
            while ((low < high) && (a[split].compareTo(a[low]) < 0)) {
                low++;
            }
            while ((high > low) && (a[high].compareTo(a[split]) < 0)) {
                high--;
            }
            if (low < high) {
                swap(a, low, high);
            }
            else {
                break;
            }
        }
        if (a[split].compareTo(a[low]) >= 0) {
            low--;
        }
        swap(a, low, split);
        return low;
    }

    public static void quickSortStringArray(String a[], int low, int high) {
        int split  = divide(a, low, high);
        if (split > low)  quickSortStringArray(a, low, split-1);
        if (split < high) quickSortStringArray(a, split+1, high);
    }

    public static void quickSortStringArrayInvert(String a[], int low, int high) {
        int split  = divideInvert(a, low, high);
        if (split > low)  quickSortStringArrayInvert(a, low, split-1);
        if (split < high) quickSortStringArrayInvert(a, split+1, high);
    }

    public static void quickSortStringArray(String a[], int low, int high, boolean toInvert) {
        if (toInvert) {
            quickSortStringArrayInvert(a, low, high);
        }
        else {
            quickSortStringArray(a, low, high);
        }
    }

    public static Vector quickSortStringVector(Vector strings) {
        int nrElements = strings.size();
        String[] a = new String[nrElements];

        strings.copyInto((Object[])a);
        quickSortStringArray(a,0,nrElements - 1);
        Vector result = new Vector(nrElements);
        for (int i = 0; i < nrElements; i++) {
            result.addElement(a[i]);
        }
        return result;
    }

    /**

     * Merge two String Vectors into a new Vector. Input vectors must
     * already be sorted in ascending order. Output vector will be
     * sorted in ascending order. Warning: The resulting Vector will be
     * eq to one of the inputs if the other one is null or empty.

     */

    public static Vector mergeSortedStringVectors(Vector v1, Vector v2) {
        if (v1 == null) return v2;
        if (v2 == null) return v1;
        int size1 = v1.size();
        int size2 = v2.size();
        if (size1 == 0) return v2;
        if (size2 == 0) return v1;

        Vector result = new Vector(size1 + size2); // Must be enough

        int i1 = 0;
        int i2 = 0;

        String s1 = (String)v1.elementAt(0);
        String s2 = (String)v2.elementAt(0);

        while (true) {
            int diff = s1.compareTo(s2); // Returns s1 - s2
            if (diff <= 0) {    // s1 is smaller or equal, so s1 is next
                i1++;
                result.addElement(s1);
                if (diff == 0) { // Strings were equal
                    i2++;       // skip s2
                    if (i2 >= size2) break;
                    s2 = (String)v2.elementAt(i2);
                }

                // Don't update s1 until done checking for equality above

                if (i1 >= size1) break; // Done with s1?
                s1 = (String)v1.elementAt(i1);
            } else {            // s2 was strictly smaller so s2 is next
                result.addElement(s2);
                i2++;
                if (i2 >= size2) break;
                s2 = (String)v2.elementAt(i2);
            }
        }

        // At this point, at least one vector is done.  Add the tail
        // of the other one if there is one.  Note that you cannot
        // trust neither s1 or s2 to be correct here.
                
        while (i1 < size1) result.addElement(v1.elementAt(i1++));
        while (i2 < size2) result.addElement(v2.elementAt(i2++));
        return result;
    }


    /**

     * Determine whether two string Vectors have *any* elements in
     * common. Input vectors must already be sorted in ascending
     * order.  */

    public static boolean sortedStringVectorsIntersect(Vector v1, Vector v2) {

        // Intersecting any set with the null set returns the null set

        if (v1 == null) return false;
        if (v2 == null) return false;
        int size1 = v1.size();
        int size2 = v2.size();

        if (size1 == 0) return false;
        if (size2 == 0) return false;

        int i1 = 0;
        int i2 = 0;

        String s1 = (String)v1.elementAt(0);
        String s2 = (String)v2.elementAt(0);

        while (true) {
            int diff = s1.compareTo(s2); // Returns s1 - s2
            if (diff <= 0) {    // s1 is smaller or equal
                if (diff == 0) return true; // Strings were equal
                i1++;
                if (i1 >= size1) break; // Done with s1?
                s1 = (String)v1.elementAt(i1);
            } else {            // s2 was strictly smaller
                i2++;
                if (i2 >= size2) break;
                s2 = (String)v2.elementAt(i2);
            }
        }
        return false;
    }
}
