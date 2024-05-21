// Size.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** An Object subclass representing a positive or negative width and height.
  */

public class Size implements Codable {
    /** The Size's width. */
    public int                  width;
    /** The Size's height. */
    public int                  height;

    static private Vector       _sizeCache = new Vector();
    static private boolean      _cacheSizes = true;

    static final String         WIDTH_KEY = "width";
    static final String         HEIGHT_KEY = "height";


    /* constructors */

    /** Constructs a Size with zero width and height.
      */
    public Size() {
    }

    /** Constructs a Size with dimensions (<b>width</b>, <b>height</b>).
      */
    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /** Constructs a Size with the same dimensions as <b>templateSize</b>.
      */
    public Size(Size templateSize) {
        width = templateSize.width;
        height = templateSize.height;
    }

/* attributes */

    /** Returns <b>true</b> if the Size has zero width or height.
      */
    public boolean isEmpty() {
        return (width == 0 || height == 0);
    }



/* actions */


    /** Returns the Size's String representation.
      */
    public String toString() {
        return "(" + width + ", " + height + ")";
    }

    /** Sets the Size's dimensions to (<b>width</b>, <b>height</b>).
      */
    public void sizeTo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /** Changes the Size's dimensions by <b>deltaWidth</b> and
      * <b>deltaHeight</b>.
      */
    public void sizeBy(int deltaWidth, int deltaHeight) {
        sizeTo(width + deltaWidth, height + deltaHeight);
    }

    /** Unions the Size with <b>aSize</b>.
      */
    public void union(Size aSize) {
        if (width < aSize.width) {
            width = aSize.width;
        }
        if (height < aSize.height) {
            height = aSize.height;
        }
    }

    /** Returns <b>true</b> if the Size equals <b>anObject</b>.
     */
    public boolean equals(Object anObject) {
        Size aSize;

        if (!(anObject instanceof Size))
            return false;

        aSize = (Size)anObject;
        return (aSize.width == width && aSize.height == height);
    }

    /** Returns the Size's hash code.
      */
    public int hashCode() {
        // ALERT!
        // This is an arbitrarily choosen hash implementation.
        // There should be a better one for sizes.
        return width ^ height;
    }




/* archiving */


    /** Describes the Size class' coding information.
     * @see Codable#describeClassInfo
     */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Size", 1);
        info.addField(WIDTH_KEY, INT_TYPE);
        info.addField(HEIGHT_KEY, INT_TYPE);
    }

    /** Encodes the Size.
     * @see Codable#encode
     */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeInt(WIDTH_KEY, width);
        encoder.encodeInt(HEIGHT_KEY, height);
    }

    /** Decodes the Size.
     * @see Codable#decode
     */
    public void decode(Decoder decoder) throws CodingException {
        width = decoder.decodeInt(WIDTH_KEY);
        height = decoder.decodeInt(HEIGHT_KEY);
    }

    /** Finishes the Size decoding.
     * @see Codable#finishDecoding
     */
    public void finishDecoding() throws CodingException {
    }



/* size cache */


    /** Returns a Size from the Size cache with dimensions
      * (<b>width</b>, <b>height</b>).  Creates a new Size if the cache is
      * empty.
      * @private
      */
    static Size newSize(int width, int height) {
        Size    theSize, newSize;

        synchronized(_sizeCache) {
            if (!_cacheSizes || _sizeCache.isEmpty()) {
                return new Size(width, height);
            }

            theSize = (Size)_sizeCache.removeLastElement();
        }
        theSize.sizeTo(width, height);

        return theSize;
    }

    /** Returns a Size from the Size cache whose dimensions match
      * <b>templateSize</b>.  Creates a new Size if the cache is empty.
      * @private
      */
    static Size newSize(Size templateSize) {
        if (templateSize == null) {
            return Size.newSize(0, 0);
        } else {
            return Size.newSize(templateSize.width, templateSize.height);
        }
    }

    /** Returns a Size from the Size cache with zero size.  Equivalent to
      * the code:
      * <pre>
      *     aSize = Size.newSize(0, 0);
      * </pre>
      * Creates a new Size if the cache is empty.
      * @private
      */
    static Size newSize() {
        return Size.newSize(0, 0);
    }

    /** Places <b>aSize</b> back in the Size cache (if the cache is not full).
      * @private
      */
    static void returnSize(Size aSize) {
        if (!_cacheSizes) {
            return;
        }

        synchronized(_sizeCache) {
            if (_sizeCache.count() < 30) {
                _sizeCache.addElement(aSize);
            }
        }
    }

    /** Places the Sizes contained in <b>sizes</b> back in the Size cache
      * (if the cache is not full) and empties the Vector.
      * @private
      */
    static void returnSizes(Vector sizes) {
        int     i;

        if (sizes == null || !_cacheSizes) {
            return;
        }

        i = sizes.count();
        while (i-- > 0) {
            Size.returnSize((Size)sizes.elementAt(i));
        }

        sizes.removeAllElements();
    }

    /** Enables or disables Size caching.  With setShouldCacheSizes(false),
      * Size.newSize() methods create new Size instances and Size.returnSize()
      * methods do nothing with the Sizes they're given.  Disabling Size
      * caching can help you track down problems in your code of returning
      * Sizes to the cache while accidentally continuing to maintain
      * references to them.
      * @private
      */
    static void setShouldCacheSizes(boolean flag) {
        synchronized(_sizeCache) {
            _cacheSizes = flag;

            if (!_cacheSizes) {
                _sizeCache.removeAllElements();
            }
        }
    }
}
