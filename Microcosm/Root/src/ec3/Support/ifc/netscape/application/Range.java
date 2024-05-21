// Range.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import java.lang.*;
import netscape.util.*;


/** Object subclass representing a range of indexed data. A Range consists of
  * an index location and a length.
  * @note 1.0 fixed bug in contains()
  */

public class Range extends Object implements Codable {

    /** A null (undefined) range. */
    private static Range nullRange;

    /** The Range's first index. */
    public int index;

    /** The Range's length. */
    public int length;

    static final String         INDEX_KEY = "index";
    static final String         LENGTH_KEY = "length";



    /* static methods */

    public static Range nullRange() {
        if (nullRange == null) {
            nullRange = new Range(-1,0);
        }

        return nullRange;
    }

    /** Returns the intersection of <b>range1</b> and <b>range2</b>.
      */
    public static Range rangeFromIntersection(Range range1, Range range2) {
        Range   newRange;

        newRange = new Range(range1);
        newRange.intersectWith(range2);

        return newRange;
    }

    /** Returns the union of <b>range1</b> and <b>range2</b>.
      */
    public static Range rangeFromUnion(Range range1, Range range2) {
        Range   newRange;

        newRange = new Range(range1);
        newRange.unionWith(range2);

        return newRange;
    }

    /** Returns a Range containing <b>index1</b> and <b>index2</b>.
      */
    public static Range rangeFromIndices(int index1, int index2) {
        if( index1 < index2 )
            return new Range(index1,index2-index1);
        else
            return new Range(index2,index1-index2);

    }



    /* constructors */

    /** Constructs a Range with value Range.nullRange().   */
    public Range() {
        super();
        index = nullRange().index;
        length= nullRange().length;
    }

    /** Constructs a Range with index <b>index</b> and length <b>length</b>.
      */
    public Range(int index, int length) {
        super();
        this.index  = index;
        this.length = length;
    }

    /** Constructs a Range with the same index and length as
      * <b>templateRange</b>.
      */
    public Range(Range templateRange) {
        super();
        index = templateRange.index;
        length = templateRange.length;
    }

    /** Returns the Range's first index.
      */
    public int index() {
        return index;
    }

    /** Returns the Range's length.
      */
    public int length() {
        return length;
    }

    /** Returns the last index included in the Range.
      * <i>Note: If range length is zero, the return value
      * is undefined.</i>
      */
    public int lastIndex() {
        return index + length - 1;
    }

    /** Returns <b>true</b> if the Range equals <b>anObject</b>.
      */
    public boolean equals(Object anObject) {
        Range aRange;

        if(!(anObject instanceof Range))
            return false;

        aRange = (Range) anObject;
        if( aRange.index == index && aRange.length == length )
            return true;
        else
            return false;
    }

    /** Computes the union of the Range and <b>aRange</b>. Stores the result in
      * the receiver.  If the Range and <b>aRange</b> do not intersect, the
      * union consists of the range from the minimum index to the maximum
      * index.
      */
    public void unionWith(Range aRange) {
        unionWith(aRange.index, aRange.length);
    }

    /** Computes the union of the Range and the range defined by
      * <b>anIndex</b> and <b>aLength</b>. Stores the result in the receiver.
      * If the ranges do not intersect, the union consists of the range from
      * the minimum index to the maximum index.
      */
    public void unionWith(int anIndex, int aLength) {
        int low,high;

        if( index == nullRange().index ) {
            index = anIndex;
            length = aLength;
        } else if( anIndex == nullRange().index ) {
            return;
        } else {
            if( index < anIndex )
                low = index;
            else
                low = anIndex;
            if( (index+length) > (anIndex + aLength))
                high = index + length;
            else
                high = anIndex + aLength;
            index = low;
            length = high - low;
        }
    }

    /** Computes the intersection of the Range and <b>aRange</b>. Stores
      * the result in the receiver. The result is Range.nullRange() if the
      * ranges do not intersect.
      */
    public void intersectWith(Range aRange) {
        intersectWith(aRange.index, aRange.length);
    }

    /** Computes the intersection of the Range and the range defined by
      * <b>anIndex</b> and <b>aLength</b>. Stores the result in the receiver.
      * The result is Range.nullRange() if the ranges do not intersect.
      */
    public void intersectWith(int anIndex, int aLength) {
        int lowIndex,lowLength;
        int highIndex,highLength;

        if( index < anIndex ) {
            lowIndex  = index;
            lowLength = length;
            highIndex  = anIndex;
            highLength = aLength;
        } else {
            lowIndex = anIndex;
            lowLength = aLength;
            highIndex = index;
            highLength = length;
        }

        if( (lowIndex+lowLength) <= highIndex ) {
            index  = nullRange().index;
            length = nullRange().length;
        } else {
            index  = highIndex;
            if( (highIndex + highLength) > (lowIndex + lowLength) )
                length = (lowIndex + lowLength) - highIndex;
            else
                length = highLength;
        }
    }

    /** Returns the Range's String representation.
      */
    public String toString() {
        if( isNullRange() )
            return "Null range";
        else
            return "(" + index + ", " + length + ")";
    }


  /*
   * Conveniences
   */

    /** Returns <b>true</b> if the receiver intersects <b>aRange</b>.
      */
    public boolean intersects(Range aRange) {
        boolean result;
        int oldIndex = index;
        int oldLength = length;

        this.intersectWith( aRange );
        if( index == nullRange().index )
            result = false;
        else
            result = true;
        index = oldIndex;
        length = oldLength;
        return result;
    }

    /** Returns <b>true</b> if the receiver intersects the range defined by
      * <b>anIndex</b> and <b>aLength</b>.
      */
    public boolean intersects(int anIndex, int aLength) {
        boolean result;
        int oldIndex = index;
        int oldLength = length;

        this.intersectWith( anIndex, aLength );
        if( index == nullRange().index )
            result = false;
        else
            result = true;
        index = oldIndex;
        length = oldLength;
        return result;
    }

    /** Returns <b>true</b> if the receiver equals Range.nullRange().
      */
    public boolean isNullRange() {
        if( index == nullRange().index )
            return true;
        else
            return false;
    }

    /** Returns <b>true</b> the range has a length of zero.
      */
    public boolean isEmpty() {
        if( length == 0 )
            return true;
        else
            return false;
    }

    /** Returns <b>true</b> if the Range includes <b>anIndex</b>.
      */
    public boolean contains(int anIndex) {
        if( anIndex >= index && anIndex < (index+length))
            return true;
        else
            return false;
    }


/* archiving */


    /** Describes the Range class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Range", 1);
        info.addField(INDEX_KEY, INT_TYPE);
        info.addField(LENGTH_KEY, INT_TYPE);
    }

    /** Encodes the Range.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeInt(INDEX_KEY, index);
        encoder.encodeInt(LENGTH_KEY, length);
    }

    /** Decodes the Range.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        index = decoder.decodeInt(INDEX_KEY);
        length = decoder.decodeInt(LENGTH_KEY);
    }

    /** Finishes the Range decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
