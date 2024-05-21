// FastStringBuffer.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/**
 * Object subclass resembling the java.lang.StringBuffer class (an object that
 * manages a mutable string).  Unlike java.lang.StringBuffer, none of
 * FastStringBuffer's methods are synchronized, which results in a significant
 * performance increase.  FastStringBuffer also has additional API that allows
 * it to be more easily modified than the standard StringBuffer.
 * @note 1.0 fixes some error by 1 problems in insert() method
 */


class FastStringBuffer extends Object {
    String      string;
    char        buffer[];
    int         length;
    boolean     doublesCapacity;

/* constructors */

    /** Creates a FastStringBuffer containing the empty string.
      */
    public FastStringBuffer() {
        this("");
    }

    /** Creates a FastStringBuffer containing the characters in <b>aString</b>.
      */
    public FastStringBuffer(String aString) {
        super();

        if (aString == null || aString.equals("")) {
            buffer = new char[8];
        } else {
            buffer = new char[aString.length() + 1];
            setStringValue(aString);
        }
        doublesCapacity=false;
    }

    /** Creates a FastStringBuffer containing the characters in <b>aString</b>
     *  from the index <b>start</b> to <b>end<\b>. <b>end<\b> is excluded.
     */
    public FastStringBuffer(String aString,int start,int end) {
        int i;
        buffer = new char[end - start];
        length = end-start;
        string = null;
        doublesCapacity = false;
        aString.getChars(start,end,buffer,0);
    }

    /** Creates a FastStringBuffer containing the character <b>aChar</b>.
      */
    public FastStringBuffer(char aChar) {
        super();

        buffer = new char[8];
        buffer[0] = aChar;
        length = 1;
        doublesCapacity=false;
    }

    void _increaseCapacityTo(int newCapacity) {
        char          oldBuffer[];

        if (buffer.length <= newCapacity) {
            oldBuffer = buffer;
            if( doublesCapacity )
              buffer = new char[newCapacity * 2];
            else
              buffer = new char[newCapacity + 20];
            System.arraycopy(oldBuffer, 0, buffer, 0, oldBuffer.length);
        }
    }

    /** Set whether the FastStringBuffer should double its size
     *  when some data is inserted and the internal buffer is
     *  too small.
     */
    public void setDoublesCapacityWhenGrowing(boolean aFlag){
        doublesCapacity = aFlag;
    }

    /** Returns whether FastStringBuffer doubles its size when
     *  some data is inserted and the internal buffer is
     *  too small.
     */
    public boolean doublesCapacityWhenGrowing() {
        return doublesCapacity;
    }

    /** Sets the FastStringBuffer's contents to the characters in
      * <b>aString</b>.
      */
    public void setStringValue(String aString) {
        if (aString == null || aString.equals("")) {
            length = 0;
        } else {
            length = aString.length();
            _increaseCapacityTo(length);
            aString.getChars(0, length, buffer, 0);
        }

        string = aString;
    }

    /** Returns the String for the FastStringBuffer's contents.
      */
    public String toString() {
        if (string == null) {
            string = new String(buffer, 0, length);
        }

        return string;
    }

    /** Returns the character at <b>index</b>.
      * @exception StringIndexOutOfBoundsException If the index is invalid.
      */
    public char charAt(int index) {
        if (index < 0 || index >= length) {
            throw new StringIndexOutOfBoundsException(index);
        }

        return buffer[index];
    }

    /** Returns the index of the first occurrance of <b>aChar</b> in the
      * FastStringBuffer, starting at character <b>offset</b>.
      * @exception StringIndexOutOfBoundsException If the offset is invalid.
      */
    public int indexOf(char aChar, int offset) {
        int     i;

        if (offset < 0 || offset >= length) {
            throw new StringIndexOutOfBoundsException(offset);
        }

        for (i = offset; i < length; i++) {
            if (buffer[i] == aChar) {
                return i;
            }
        }

        return -1;
    }

    /** Returns the index of the first occurrance of <b>aChar</b> in the
      * FastStringBuffer.  Equivalent to the code:
      * <pre>
      *     indexOf(aChar, 0);
      * </pre>
      * @see #indexOf
      */
    public int indexOf(char aChar) {
        return indexOf(aChar, 0);
    }

    /** Returns <b>true</b> if the FastStringBuffer contains a space or tab
      * character at position <b>index</b>.
      * @exception StringIndexOutOfBoundsException If the index is invalid.
      */
    public boolean tabOrSpaceAt(int index) {
        if (index < 0 || index >= length) {
            throw new StringIndexOutOfBoundsException(index);
        }

        return (buffer[index] == ' ' || buffer[index] == '\t');
    }

    /** Appends <b>aChar</b> to the FastStringBuffer.
      */
    public void append(char aChar) {
        _increaseCapacityTo(length + 1);
        buffer[length++] = aChar;
        string = null;
    }

    /** Appends <b>aString</b> to the FastStringBuffer.
      */
    public void append(String aString) {
        if (aString == null || aString.equals("")) {
            return;
        }

        _increaseCapacityTo(length + aString.length());
        aString.getChars(0, aString.length(), buffer, length);
        length += aString.length();
        string = null;
    }

    /** Inserts <b>aChar</b> at <b>index</b>.  If <b>index</b> is
      * greater than or equal to the number of characters within the buffer,
      * appends <b>aChar</b>.
      * @exception StringIndexOutOfBoundsException if the index is invalid.
      */
    public void insert(char aChar, int index) {
        char    oldBuffer[];
        int     i;

        if (index < 0) {
            throw new StringIndexOutOfBoundsException(index);
        } else if (index >= length) {
            append(aChar);
            return;
        }

        if (length < buffer.length) {
            if (index != length) {
                System.arraycopy(buffer, index, buffer, index + 1,
                                 length - index);
            }
            buffer[index] = aChar;
            length++;
            string = null;
            return;
        }

        oldBuffer = buffer;
        buffer = new char[buffer.length + 20];
        if (index > 0) {
            System.arraycopy(oldBuffer, 0, buffer, 0, index);
        }
        if (index != length ) {
            System.arraycopy(oldBuffer, index, buffer, index + 1,
                             length - index);
        }
        buffer[index] = aChar;
        length++;

        string = null;
    }

    /** Inserts <b>aString</b> at <b>index</b>.  If <b>index</b> is
      * greater than or equal to the number of characters within the buffer,
      * appends <b>aString</b>.
      * @exception StringIndexOutOfBoundsException If the index is invalid.
      */
        public void insert(String aString, int index) {
        char    oldBuffer[];
        int     stringLength, i;

        if (index < 0) {
            throw new StringIndexOutOfBoundsException(index);
        } else if (index > length) {
            append(aString);
            return;
        } else if (aString == null || aString.equals("")) {
            return;
        }

        stringLength = aString.length();
        if (length + stringLength < buffer.length) {
            System.arraycopy(buffer, index, buffer, index + stringLength,
                             length - index);
            aString.getChars(0, stringLength, buffer, index);
            length += stringLength;
            string = null;
            return;
        }

        oldBuffer = buffer;
        buffer = new char[length + stringLength + 20];
        if (index > 0) {
            System.arraycopy(oldBuffer, 0, buffer, 0, index);
        }
        System.arraycopy(oldBuffer, index, buffer, index + stringLength,
                         length - index);
        aString.getChars(0, stringLength, buffer, index);
        length += stringLength;

        string = null;
    }

    /** Removes the character at <b>index</b>.
      * @exception StringIndexOutOfBoundsException if the index is invalid.
      */
    public void removeCharAt(int index) {
        if (index < 0 || index >= length) {
            throw new StringIndexOutOfBoundsException(index);
        }

        if (index + 1 == length) {
            length--;
            string = null;
            return;
        }

        System.arraycopy(buffer, index + 1, buffer, index, length - (index+1));
        length--;

        string = null;
    }

    /** Truncates the FastStringBuffer to <b>aLength</b> characters.  If
      * <b>aLength</b> is invalid, does nothing.
      */
    public void truncateToLength(int aLength) {
        if (aLength < 0 || aLength > length) {
            return;
        }

        length = aLength;

        string = null;
    }

    /** Returns the number of characters in the FastStringBuffer.
      */
    public int length() {
        return length;
    }

    /** Returns the number of characters in the FastStringBuffer.
      */
    public void moveChars(int fromIndex, int toIndex) {
        if (fromIndex <= toIndex) {
            return;
        } else if (fromIndex < 0 || fromIndex >= length) {
            throw new StringIndexOutOfBoundsException(fromIndex);
        } else if (toIndex < 0 || toIndex >= length) {
            throw new StringIndexOutOfBoundsException(toIndex);
        }

        System.arraycopy(buffer, fromIndex, buffer, toIndex,
                         length - fromIndex);
        length -= fromIndex - toIndex;

        string = null;
    }

    /** Returns the FastStringBuffer's char array, for situation where it is
      * needed.  For example, you can draw the FastStringBuffer's contents
      * by passing the array to the Graphic's <b>drawString()</b> method that
      * takes a char array, rather than first convert the StringBuffer to a
      * String.  You should never modify this array yourself.
      */
    public char[] charArray() {
        return buffer;
    }
}
