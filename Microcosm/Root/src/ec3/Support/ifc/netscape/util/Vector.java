// Vector.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** Object subclass that manages an array of objects). A Vector cannot contain
  * <b>null</b>.
  * @note 1.0 toString() prints as formatted text
  */

public class Vector implements Cloneable, Codable {
    Object      array[];
    int         count;

    final static String         ARRAY_KEY = "array";

    /** Constructs a Vector with an initial capacity of 8 elements.
      */
    public Vector() {
        super();
        count = 0;
    }

    /** Primitive constructor. Constructs a Vector large enough to hold
      * <b>initialCapacity</b> elements.  The Vector will grow to accomodate
      * additional objects, as needed.
      */
    public Vector(int initialCapacity) {
        super();

        array = new Object[initialCapacity];
        count = 0;
    }

    /** Clones the Vector.  Does not clone its elements.
      */
    public Object clone() {
        Vector newVect;

        try {
            newVect = (Vector)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(
                                "Error in clone(). This shouldn't happen.");
        }

        if (count == 0) {
            newVect.array = null;
            return newVect;
        }

        newVect.array = new Object[count];
        System.arraycopy(array, 0, newVect.array, 0, count);

        return newVect;
    }

    /** Returns the number of elements in the Vector.
      */
    public int count() {
        return count;
    }

    /** Returns the number of elements in the Vector.
      */
    public int size() {
        return count;
    }

    /** Returns <b>true</b> if the Vector contains no elements.
      */
    public boolean isEmpty() {
        return (count == 0);
    }

    /** Adds <b>element</b> as the last element of the Vector, if not already
      * present within the Vector.  Throws a NullPointerException if
      * <b>element</b> is <b>null</b>.
      */
    public void addElementIfAbsent(Object element) {
        if (element == null) {
            throw new NullPointerException(
                                "It is illegal to store nulls in Vectors.");
        }

        if (element != null && !contains(element)) {
            addElement(element);
        }
    }

    /** Inserts <b>element</b> before <b>existingElement</b> in the Vector.
      * If <b>existingElement</b> is <b>null</b> or cannot be found, this
      * method does nothing and returns <b>false</b>, otherwise it returns
      * <b>true</b>.  This method throws a NullPointerException if
      * <b>element</b> is <b>null</b>.
      */
    public boolean insertElementBefore(Object element,
                                       Object existingElement) {
        int     index;

        if (element == null) {
            throw new NullPointerException(
                                "It is illegal to store nulls in Vectors.");
        }

        if (existingElement == null) {
            return false;
        }

        index = indexOf(existingElement);
        if (index == -1) {
            return false;
        }

        insertElementAt(element, index);

        return true;
    }

    /** Inserts <b>element</b> after <b>existingElement</b> in the Vector.
      * If <b>existingElement</b> is <b>null</b> or cannot be found, this
      * method does nothing and returns <b>false</b>, otherwise it returns
      * <b>true</b>.  This method throws a NullPointerException if
      * <b>element</b> is <b>null</b>.
      */
    public boolean insertElementAfter(Object element, Object existingElement) {
        int     index;

        if (element == null) {
            throw new NullPointerException(
                                "It is illegal to store nulls in Vectors.");
        }

        if (existingElement == null) {
            return false;
        }

        index = indexOf(existingElement);
        if (index == -1) {
            return false;
        }

        if (index >= count - 1) {
            addElement(element);
        } else {
            insertElementAt(element, index + 1);
        }

        return true;
    }

    /** Adds the elements contained in <b>aVector</b> that are not already
      * present in the Vector to the end of the Vector.
      */
    public void addElementsIfAbsent(Vector aVector) {
        Object  nextObject;
        int     addCount, i;

        if (aVector == null) {
            return;
        }

        addCount = aVector.count();
        for (i = 0; i < addCount; i++) {
            nextObject = aVector.elementAt(i);
            if (!contains(nextObject)) {
                addElement(nextObject);
            }
        }
    }

    /** Adds the elements contained in <b>aVector</b> to the end of the Vector.
      */
    public void addElements(Vector aVector) {
        int addCount, i;

        if (aVector == null)
            return;

        addCount = aVector.count();

        if (array == null || (count + addCount) >= array.length)
            ensureCapacity(count + addCount);

        for (i = 0; i < addCount; i++)
            addElement(aVector.elementAt(i));
    }

    /** Removes all occurrences of <b>element</b> from the Vector.
      */
    public void removeAll(Object element) {
        int i = count();

        while (i-- > 0) {
            if (elementAt(i).equals(element))
                removeElementAt(i);
        }
    }

    /** Removes and returns the element at index 0, or <b>null</b> if the
      * Vector is empty.
      */
    public Object removeFirstElement() {
        if (count == 0) {
            return null;
        }

        return removeElementAt(0);
    }

    /** Removes and returns the element at index count() - 1 (the last object)
      * or <b>null</b> if the Vector is empty.
      */
    public Object removeLastElement() {
        if (count == 0) {
            return null;
        }

        return removeElementAt(count - 1);
    }

    /** Replaces the element at <b>index</b> with <b>element</b>. Returns
      * the replaced object. This method throws a NullPointerException if
      * <b>element</b> is <b>null</b>, and throws an
      * ArrayIndexOutOfBoundsException if <b>index</b> is an illegal index
      * value.
      */
    public Object replaceElementAt(int index, Object element) {
        Object          oldObject;

        if (element == null) {
            throw new NullPointerException(
                                "It is illegal to store nulls in Vectors.");
        }

        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + count);
        } else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index + " < 0");
        }

        oldObject = elementAt(index);
        array[index] = element;

        return oldObject;
    }

    /** Returns an array containing the Vector's contents.
      */
    public Object[] elementArray() {
        Object newArray[];

        newArray = new Object[count];
        if (count > 0) {
            System.arraycopy(array, 0, newArray, 0, count);
        }

        return newArray;
    }

    /** Copies the Vector's elements into <b>anArray</b>. This array must be
      * large enough to contain the elements.
      */
    public void copyInto(Object anArray[]) {
        if (count > 0) {
            System.arraycopy(array, 0, anArray, 0, count);
        }
    }

    /** Minimizes the Vector's storage area.
      */
    public void trimToSize() {
        if (count == 0)
            array = null;
        else if (count != array.length)
            array = elementArray();
    }

    /** Increases, if necessary, the the Vector's storage area so that it can
      * contain <b>minCapacity</b> elements.
      */
    public void ensureCapacity(int minCapacity) {
        int newLength;
        Object newArray[];

        if (array == null)
            array = new Object[8];

        if (minCapacity < array.length)
            return;

        if (array.length < 8)
            newLength = 8;
        else
            newLength = array.length;

        while (newLength < minCapacity)
            newLength = 2 * newLength;

        newArray = new Object[newLength];
        System.arraycopy(array, 0, newArray, 0, count);

        array = newArray;
    }

    /** Returns the number of elements that can be stored in the Vector
      * without increasing the Vector's storage area.
      */
    public int capacity() {
        if (array == null)
            return 0;

        return array.length;
    }

    /** Returns an Enumeration that can be used to iterate through all of the
      * Vector's elements.
      */
    public Enumeration elements() {
        return new VectorEnumerator(this);
    }

    /** Returns an Enumeration that can be used to iterate through the vector's
      * elements beginning at element <b>index</b>.
      */
    public Enumeration elements(int index) {
        return new VectorEnumerator(this, index);
    }

    /** Returns <b>true</b> if the Vector contains <b>element</b>. The
      * comparison is performed using <b>equals()</b> with each element.
      */
    public boolean contains(Object element) {
        if (indexOf(element, 0) != -1)
            return true;

        return false;
    }

    /** Returns <b>true</b> if the Vector contains <b>element</b>. The
      * comparison is performed using the <b>==</b> operator with each element.
      */
    public boolean containsIdentical(Object element) {
        if (indexOfIdentical(element, 0) != -1)
            return true;

        return false;
    }

    /** Returns the index of <b>element</b> in the Vector.  Returns <b>-1</b>
      * if the element is not present. The comparison is performed using
      * <b>equals()</b> with each element.
      */
    public int indexOf(Object element) {
        return indexOf(element, 0);
    }

    /** Returns the index of <b>element</b> in the Vector, starting at
      * <b>index</b>.  Returns <b>-1</b> if the element is not present.
      * The comparison is performed using <b>equals()</b> with each element.
      */
    public int indexOf(Object element, int index) {
        int i;

        for (i = index; i < count; i++)
            if (array[i].equals(element))
                return i;

        return -1;
    }

    /** Returns the index of <b>element</b> in the Vector, starting at
      * index.  Returns <b>-1</b> if the element is not present.
      * The comparison is performed using the <b>==</b> operator
      * with each element.
      */
    public int indexOfIdentical(Object element, int index) {
        int i;

        for (i = index; i < count; i++)
            if (array[i] == element)
                return i;

        return -1;
    }

    /** Returns the index of <b>element</b> in the Vector.  Returns <b>-1</b>
      * if the element is not present. The comparison is performed using the
      * <b>==</b> operator with each element.
      */
    public int indexOfIdentical(Object element) {
        return indexOfIdentical(element, 0);
    }

    /** Returns the last index of <b>element</b> in the Vector.  Returns
      * <b>-1</b> if the element is not present. The comparison is performed
      * using <b>equals()</b> with each element.
      */
    public int lastIndexOf(Object element) {
        return lastIndexOf(element, count);
    }

    /** Returns the last index of <b>element</b> in the vector, starting at
      * <b>index</b>.  Returns <b>-1</b> if the element is not present. The
      * comparison is performed using <b>equals()</b> with each element.
      */
    public int lastIndexOf(Object element, int index) {
        int i;
        if (index > count) {
            throw new ArrayIndexOutOfBoundsException(index + " > " + count);
        }

        for (i = index - 1; i >= 0; i--)
            if (array[i].equals(element))
                return i;

        return -1;
    }

    /** Returns the element at <b>index</b>.
      */
    public Object elementAt(int index) {
        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + count);
        }

        return array[index];
    }

    /** Returns the Vector's first element.
      */
    public Object firstElement() {
        if (count == 0)
            return null;

        return array[0];
    }

    /** Returns the Vector's last element.
      */
    public Object lastElement() {
        if (count == 0)
            return null;

        return array[count - 1];
    }

    /** Sets the element at <b>index</b> to <b>element</b>. This method throws
      * a NullPointerException if <b>element</b> is <b>null</b>, and throws an
      * ArrayIndexOutOfBoundsException if <b>index</b> is an illegal index
      * value.
      */
    public void setElementAt(Object element, int index) {
        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + count);
        }

        if (element == null) {
            throw new NullPointerException(
                                "It is illegal to store nulls in Vectors.");
        }

        array[index] = element;
    }

    /** Removes the element at <b>index</b>. This method throws an
      * ArrayIndexOutOfBoundsException if <b>index</b> is an illegal index
      * value.
      */
    public Object removeElementAt(int index) {
        Object  object;
        int     copyCount;

        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + count);
        }

        object = array[index];

        copyCount = count - index - 1;
        if (copyCount > 0)
            System.arraycopy(array, index + 1, array, index, copyCount);

        count--;
        array[count] = null;

        return object;
    }

    /** Inserts <b>element</b> into the Vector at <b>index</b>. This method
      * throws a NullPointerException if <b>element</b> is <b>null</b>, and
      * throws an ArrayIndexOutOfBoundsException if <b>index</b> is an illegal
      * index value.
      */
    public void insertElementAt(Object element, int index) {
        if (index >= count + 1) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + count);
        }

        if (element == null) {
            throw new NullPointerException(
                        "It is illegal to store nulls in Vectors.");
        }

        if (array == null || count >= array.length)
            ensureCapacity(count + 1);

        System.arraycopy(array, index, array, index + 1, count - index);
        array[index] = element;
        count++;
    }

    /** Adds <b>element</b> to the end of the vector. This method throws a
      * NullPointerException if <b>element</b> is <b>null</b>.
      */
    public void addElement(Object element) {
        if (element == null) {
            throw new NullPointerException(
                            "It is illegal to store nulls in Vectors.");
        }

        if (array == null || count >= array.length)
            ensureCapacity(count + 1);

        array[count] = element;
        count++;
    }

    /** Removes the first occurrence of <b>element</b> from the Vector. The
      * comparison is performed using <b>equals()</b> with each element.
      */
    public boolean removeElement(Object element) {
        int i;

        i = indexOf(element);
        if (i < 0)
            return false;

        removeElementAt(i);
        return true;
    }

    /** Removes the first occurrence of <b>element</b> from the vector. The
      * comparison is done using <b>==</b> with each element.
      */
    public boolean removeElementIdentical(Object element) {
        int i;

        i = indexOfIdentical(element, 0);
        if (i < 0)
            return false;

        removeElementAt(i);
        return true;
    }

    /** Empties the Vector, but leaves its capacity unchanged.
      */
    public void removeAllElements() {
        int i;

        for (i = 0; i < count; i++)
            array[i] = null;

        count = 0;
    }

    /** Sorts the Vector's contents. Throws a ClassCastException if the
      * Vector's contents are not all Strings, or are not Comparable. String
      * comparisons are case sensitive.
      */
    public void sort(boolean ascending) {
        Sort.sort(array, null, 0, count, ascending);
    }

    /** Sorts the Vector's contents. Throws a ClassCastException if the
      * Vector's contents are not all Strings. If <b>ignoreCase</b> is
      * <b>true</b>, this method converts the Strings to upper case and then
      * compared.
      */
    public void sortStrings(boolean ascending, boolean ignoreCase) {
        Sort.sortStrings(array, 0, count, ascending, ignoreCase);
    }

    /** Returns the Vector's string representation.
      */
    public String toString() {
        return FormattingSerializer.serializeObject(this);
    }

    /** Describes the Vector class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.util.Vector", 1);
        info.addField(ARRAY_KEY, OBJECT_ARRAY_TYPE);
    }

    /** Encodes the Vector instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        if (count == 0) {
            return;
        }

        encoder.encodeObjectArray(ARRAY_KEY, array, 0, count);
    }

    /** Decodes the Vector instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        array = decoder.decodeObjectArray(ARRAY_KEY);

        if (array == null)
            count = 0;
        else
            count = array.length;
    }

    /** Finishes the Vector's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
