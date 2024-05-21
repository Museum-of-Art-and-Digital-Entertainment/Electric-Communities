// Sort.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** Object subclass containing static sorting methods.
  * @note 1.0 upperCaseStrings checks for null element in [] objects
  */
public class Sort {
    private Sort() {
        throw new Error("All methods on Sort are static, do not call new Sort().");
    }

    private static Object[] upperCaseStrings(Object array[]) {
        int i, count;
        Object upper[];
        String str;

        count = array.length;
        upper = new Object[count];

        for (i = 0; i < count; i++) {
            str = (String)array[i];
            if (str != null)
                upper[i] = ((String)array[i]).toUpperCase();
        }

        return upper;
    }

    /** Sorts a homogenous collection of objects. The objects must all
      * implement the Comparable interface or be Strings. It also operates
      * on a parallel array of Objects--as elements of the Comparable
      * array are swapped, corresponding elements in the Object array are
      * swapped as well.
      * @see Comparable
      */
    public static void sort(Object array[], Object other[], int begin,
        int count, boolean ascending) {

        if (count <= 1)
            return;

        if (array[0] instanceof String)
            quickSortStrings(array, other, begin, begin + count -1, ascending);
        else
            quickSort(array, other, begin, begin + count - 1, ascending);
    }

    /** Sorts an array of strings. If <b>ignoreCase</b> is <b>true</b>, the
      * sort ignores case (i.e. converts the strings to upper case and then
      * compares them).  If <b>ascending</b> is <b>true</b>, this method sorts
      * the strings in ascending order, descending otherwise.
      */
    public static void sortStrings(Object strings[], int begin,
        int count, boolean ascending, boolean ignoreCase) {

        if (ignoreCase) {
            sort(upperCaseStrings(strings), strings, begin, count, ascending);
        } else {
            sort(strings, null, begin, count, ascending);
        }
    }

    /** Since java.lang.Strings are not Comparable, we need to have a
      * separate routine to handle them. Also we have to declare the array
      * as Object[] so that a new array doesn't need to be allocated.
      */
    private static void quickSortStrings(Object array[], Object other[],
                                  int left, int right, boolean ascending) {
        int i, j;
        String pivot;
        Object tmp;

        if (array.length <= 1)
            return;

        i = left;
        j = right;

        pivot = (String)array[(left + right) / 2];

        do {
            if (ascending) {
                while (i < right && pivot.compareTo((String)array[i]) > 0)
                    i++;

                while (j > left && pivot.compareTo((String)array[j]) < 0)
                    j--;
            } else {
                while (i < right && pivot.compareTo((String)array[i]) < 0)
                    i++;

                while (j > left && pivot.compareTo((String)array[j]) > 0)
                    j--;
            }

            if (i < j) {
                tmp = array[i];
                array[i] = array[j];
                array[j] = tmp;

                if (other != null) {
                    tmp = other[i];
                    other[i] = other[j];
                    other[j] = tmp;
                }
            }

            if (i <= j) {
                i++;
                j--;
            }
        } while (i <= j);

        if (left < j)
            quickSortStrings(array, other, left, j, ascending);

        if (i < right)
            quickSortStrings(array, other, i, right, ascending);
    }

    /** This is exactly the same as the String sorter, except declared to
      * deal with Comparable objects.
      */
    private static void quickSort(Object array[], Object other[],
                                  int left, int right, boolean ascending) {
        int i, j;
        Comparable pivot;
        Object tmp;

        if (array.length <= 1)
            return;

        i = left;
        j = right;

        pivot = (Comparable)array[(left + right) / 2];

        do {
            if (ascending) {
                while (i < right && pivot.compareTo((Comparable)array[i]) > 0)
                    i++;

                while (j > left && pivot.compareTo((Comparable)array[j]) < 0)
                    j--;
            } else {
                while (i < right && pivot.compareTo((Comparable)array[i]) < 0)
                    i++;

                while (j > left && pivot.compareTo((Comparable)array[j]) > 0)
                    j--;
            }

            if (i < j) {
                tmp = array[i];
                array[i] = array[j];
                array[j] = tmp;

                if (other != null) {
                    tmp = other[i];
                    other[i] = other[j];
                    other[j] = tmp;
                }
            }

            if (i <= j) {
                i++;
                j--;
            }
        } while (i <= j);

        if (left < j)
            quickSort(array, other, left, j, ascending);

        if (i < right)
            quickSort(array, other, i, right, ascending);
    }
}
