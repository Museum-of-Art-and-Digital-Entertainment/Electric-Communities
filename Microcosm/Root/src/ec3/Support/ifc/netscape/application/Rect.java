// Rect.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass representing a rectangle (its origin and positive size).
  */

public class Rect implements Codable {
    /** The Rect's X-coordinate. */
    public int          x;
    /** The Rect's Y-coordinate. */
    public int          y;
    /** The Rect's width. */
    public int          width;
    /** The Rect's height. */
    public int          height;

    static private Vector       _rectCache = new Vector();
    static private boolean      _cacheRects = true;

    static final String         X_KEY = "x";
    static final String         Y_KEY = "y";
    static final String         WIDTH_KEY = "width";
    static final String         HEIGHT_KEY = "height";



/* static methods */

    /** Returns <b>true</b> if the rect defined by
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) contains the
      * point (<b>pointX</b>, <b>pointY</b>).
      */
    public static boolean contains(int x, int y, int width, int height,
                                        int pointX, int pointY) {
        return (pointX >= x && pointX < x + width &&
                pointY >= y && pointY < y + height);
    }

    /** Returns a Rect representing the intersection of <b>rect1</b> and
      * <b>rect2</b>.
      */
    public static Rect rectFromIntersection(Rect rect1, Rect rect2) {
        Rect    newRect;

        newRect = new Rect(rect1);
        newRect.intersectWith(rect2);

        return newRect;
    }

    /** Returns a Rect representing the union of <b>rect1</b> and
      * <b>rect2</b>.
      */
    public static Rect rectFromUnion(Rect rect1, Rect rect2) {
        Rect    newRect;

        newRect = new Rect(rect1);
        newRect.unionWith(rect2);

        return newRect;
    }


/* constructors */


    /** Constructs a Rect with origin (0, 0) and zero width and height.
     */
    public Rect() {
    }

    /** Constructs a Rect with origin
      * (<b>x</b>, <b>y</b>) and size (<b>width</b>, <b>height</b>).
      */
    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Constructs a Rect with the same origin and size as <b>templateRect</b>.
      */
    public Rect(Rect templateRect) {
        x = templateRect.x;
        y = templateRect.y;
        width = templateRect.width;
        height = templateRect.height;
    }

    /** Returns the Rect's String representation.
      */
    public String toString() {
        return "(" + x + ", " + y + ", " + width + ", " + height + ")";
    }

/* attributes */


    /** Sets the Rect's origin to (<b>x</b>, <b>y</b>) and size to
      * (<b>width</b>, <b>height</b>).  If <b>width</b> or <b>height</b> are
      * negative, sets them to zero.
      */
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width < 0 ? 0 : width;
        this.height = height < 0 ? 0 : height;
    }

    /** Sets the Rect to have the same origin and size as <b>rect</b>.  If
      * <b>rect</b> is <b>null</b>, sets the Rect's origin to (0, 0) and zero
      * width and height.
      * @see #setBounds(int, int, int, int)
      */
    public void setBounds(Rect rect) {
        if (rect == null) {
            setBounds(0, 0, 0, 0);
        } else {
            setBounds(rect.x, rect.y, rect.width, rect.height);
        }
    }

    /** Sets the Rect's origin to (<b>x1</b>, <b>y1</b>) and size to
      * (<b>x2 - x1</b>, <b>y2 - y1</b>).
      */
    public void setCoordinates(int x1, int y1, int x2, int y2) {
        setBounds(x1, y1, x2 - x1, y2 - y1);
    }

    /** Sets the Rect's origin to (<b>x</b>, <b>y</b>).
      */
    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Moves the Rect's origin by (<b>deltaX</b>, <b>deltaY</b>).
      */
    public void moveBy(int deltaX, int deltaY) {
        x += deltaX;
        y += deltaY;
    }

    /** Sets the Rect's size to (<b>width</b>, <b>height</b>).
      */
    public void sizeTo(int width, int height) {
        this.width = width < 0 ? 0 : width;
        this.height = height < 0 ? 0 : height;
    }

    /** Changes the Rect's size by (<b>deltaWidth</b>, <b>deltaHeight</b>).
      */
    public void sizeBy(int deltaWidth, int deltaHeight) {
        width += deltaWidth;
        if (width < 0) {
            width = 0;
        }
        height += deltaHeight;
        if (height < 0) {
            height = 0;
        }
    }

    /** Moves the Rect's left and right sides by <b>deltaX</b> pixels and its
      * top and bottom sides by <b>deltaY</b>.  For example, starting with a
      * Rect with dimensions (5, 5, 10, 10), <b>growBy(1, -1)</b> leaves the
      * Rect with the dimensions (4, 6, 12, 8).
      */
    public void growBy(int deltaX, int deltaY) {
        x -= deltaX;
        y -= deltaY;
        width += 2 * deltaX;
        height += 2 * deltaY;

        if (width < 0) {
            width = 0;
        }
        if (height < 0) {
            height = 0;
        }
    }



/* rect coordinate convenience methods */


    /** Convenience method for computing x + width.
      */
    public int maxX() {
        return x + width;
    }

    /** Convenience method for computing y + height.
      */
    public int maxY() {
        return y + height;
    }

    /** Convenience method for computing x + width / 2.
      */
    public int midX() {
        return x + width / 2;
    }

    /** Convenience method for computing y + height / 2.
      */
    public int midY() {
        return y + height / 2;
    }

    /** Returns <b>true</b> if the Rect equals <b>anObject</b>.
      */
    public boolean equals(Object anObject) {
        Rect    rect;

        if (!(anObject instanceof Rect))
            return false;

        rect = (Rect)anObject;
        return (rect.x == x && rect.y == y && rect.width == width &&
                rect.height == height);
    }

    /** Returns the Rect's hash code.
      */
    public int hashCode() {
        // I don't know a good hash function for rects.  ALERT!
        return x ^ y ^ width ^ height;
    }

    /** Returns <b>true</b> if the Rect has zero width or height.
      */
    public boolean isEmpty() {
        return (width == 0 || height == 0);
    }

    /** Returns <b>true</b> if the Rect contains the point
      * (<b>x</b>, <b>y</b>).
      */
    public boolean contains(int x, int y) {
        return (x >= this.x && x < this.x + width &&
                y >= this.y && y < this.y + height);
    }

    /** Returns <b>true</b> if the Rect contains <b>aPoint</b>.
      * @see #contains(int, int)
      */
    public boolean contains(Point aPoint) {
        return contains(aPoint.x, aPoint.y);
    }

    /** Returns <b>true</b> if the Rect completely contains <b>aRect</b>.
      */
    public boolean contains(Rect aRect) {
        if (aRect == null) {
            return false;
//      } else if (aRect.width == 0 || aRect.height == 0 ||
//            width == 0 || height == 0) {
//            return false;
        }

        if (aRect.x >= x && (aRect.x + aRect.width <= x + width) &&
            aRect.y >= y && (aRect.y + aRect.height <= y + height)) {
            return true;
        }

        return false;
    }

    /** Returns <b>true</b> if the Rect and the rectangle (<b>x</b>, <b>y</b>,
      * <b>width</b>, <b>height</b>) overlap.
      */
    public boolean intersects(int x, int y, int width, int height) {
        if (this.x >= x + width || this.x + this.width <= x ||
            this.y >= y + height || this.y + this.height <= y) {
            return false;
        }

        return !(this.width == 0 || this.height == 0 || width == 0 ||
                 height == 0);
    }

    /** Returns <b>true</b> if the Rect and <b>aRect</b> overlap.
      */
    public boolean intersects(Rect aRect) {
        if (aRect == null) {
            return false;
        }

        return intersects(aRect.x, aRect.y, aRect.width, aRect.height);
    }


/* combining with another rect */

    /** Sets the Rect's origin and size to correspond to the intersection of
      * the Rect's current dimensions and the rectangle (<b>x</b>, <b>y</b>,
      * <b>width</b>, <b>height</b>).
      */
    public void intersectWith(int x, int y, int width, int height) {
        int     x1, y1, x2, y2, myx2, myy2;

        x1 = x;
        y1 = y;
        x2 = x1 + width;
        y2 = y1 + height;
        myx2 = this.x + this.width;
        myy2 = this.y + this.height;

        if (this.x >= x2 || myx2 <= x1) {
            x1 = x2 = 0;
        } else if (this.x > x1 && this.x < x2) {
            x1 = this.x;

            if (myx2 < x2) {
                x2 = myx2;
            }
        } else if (myx2 > x1 && myx2 < x2) {
            x2 = myx2;
        }

        if (this.y >= y2 || myy2 <= y1) {
            y1 = y2 = 0;
        } else if (this.y > y1 && this.y < y2) {
            y1 = this.y;

            if (myy2 < y2) {
                y2 = myy2;
            }
        } else if (myy2 > y1 && myy2 < y2) {
            y2 = myy2;
        }

        setCoordinates(x1, y1, x2, y2);
    }

    /** Sets the Rect's origin and size to correspond to the intersection of
      * the Rect's current dimensions and <b>aRect</b>.
      */
    public void intersectWith(Rect aRect) {
        intersectWith(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /** Returns a new Rect corresponding to the intersection of the Rect and
      * <b>aRect</b>
      */
    public Rect intersectionRect(Rect aRect) {
        int     x1, y1, x2, y2, myx2, myy2;

        x1 = aRect.x;
        y1 = aRect.y;
        x2 = aRect.x + aRect.width;
        y2 = aRect.y + aRect.height;
        myx2 = x + width;
        myy2 = y + height;

        if (x >= x2 || myx2 <= x1 || y >= y2 || myy2 <= y1) {
            return new Rect();
        }

        if (x > x1 && x < x2) {
            x1 = x;

            if (myx2 < x2) {
                x2 = myx2;
            }
        } else if (myx2 > x1 && myx2 < x2) {
            x2 = myx2;
        }

        if (y > y1 && y < y2) {
            y1 = y;

            if (myy2 < y2) {
                y2 = myy2;
            }
        } else if (myy2 > y1 && myy2 < y2) {
            y2 = myy2;
        }

        return new Rect(x1, y1, x2 - x1, y2 - y1);
    }

    /** Sets the Rect's origin and size to correspond to the union of the
      * the Rect and the rectangle (<b>x</b>, <b>y</b>, <b>width</b>,
      * <b>height</b>).
      */
    public void unionWith(int x, int y, int width, int height) {
        int     x1, y1, x2, y2;

        x1 = this.x < x ? this.x : x;
        x2 = x + width;
        if (this.x + this.width > x2) {
            x2 = this.x + this.width;
        }

        y1 = this.y < y ? this.y : y;
        y2 = y + height;
        if (this.y + this.height > y2) {
            y2 = this.y + this.height;
        }

        setCoordinates(x1, y1, x2, y2);
    }

    /** Sets the Rect's origin and size to correspond to the union of the
      * the Rect and <b>aRect</b>.
      */
    public void unionWith(Rect aRect) {
        if (aRect == null) {
            return;
        }

        unionWith(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /** Returns a new Rect corresponding to the union of the Rect and
      * <b>aRect</b>.
      */
    public Rect unionRect(Rect aRect) {
        int     myx2, myy2, x1, y1, x2, y2;

        if (aRect == null) {
            return new Rect(this);
        }

        x1 = x < aRect.x ? x : aRect.x;
        x2 = aRect.x + aRect.width;
        myx2 = x + width;
        if (myx2 > x2) {
            x2 = myx2;
        }

        y1 = y < aRect.y ? y : aRect.y;
        y2 = aRect.y + aRect.height;
        myy2 = y + height;
        if (myy2 > y2) {
            y2 = myy2;
        }

        return new Rect(x1, y1, x2 - x1, y2 - y1);
    }

    void filterEmptyRects(Vector aVector) {
        Rect    nextRect;
        int     i;

        i = aVector.count();
        while (i-- > 0) {
            nextRect = (Rect)aVector.elementAt(i);
            if (nextRect.width == 0 || nextRect.height == 0) {
                aVector.removeElementAt(i);
            }
        }
    }

    /** Fills <b>rects</b> with Rects representing the regions within the
      * Rect that do not overlap with <b>aRect</b>.  If the two Rects do not
      * overlap, it leaves <b>rects</b> empty.
      */
    public void computeDisunionRects(Rect aRect, Vector rects) {
        if (aRect == null || !intersects(aRect) || rects == null) {
            return;
        }

        if (aRect.contains(this)) {
            return;
        }

        /* -1 */
        if (contains(aRect)) {
            rects.addElement(Rect.newRect(x, y, aRect.x - x, height));
            rects.addElement(Rect.newRect(aRect.x, y, aRect.width,
                                          aRect.y - y));
            rects.addElement(Rect.newRect(aRect.x, aRect.maxY(), aRect.width,
                                          maxY() - aRect.maxY()));
            rects.addElement(Rect.newRect(aRect.maxX(), y,
                                          maxX() - aRect.maxX(), height));

            filterEmptyRects(rects);
            return;
        }

        /* 1 */
        if (aRect.x <= x && aRect.y <= y) {
            if (aRect.maxX() > maxX()) {
                rects.addElement(Rect.newRect(x, aRect.maxY(), width,
                                              maxY() - aRect.maxY()));
            } else if (aRect.maxY() > maxY()) {
                rects.addElement(Rect.newRect(aRect.maxX(), y,
                                              maxX() - aRect.maxX(), height));
            } else {
                rects.addElement(Rect.newRect(aRect.maxX(), y,
                                              maxX() - aRect.maxX(),
                                              aRect.maxY() - y));
                rects.addElement(Rect.newRect(x, aRect.maxY(), width,
                                              maxY() - aRect.maxY()));
            }

            filterEmptyRects(rects);
            return;
        }

        /* 2 */
        if (aRect.x <= x && aRect.maxY() >= maxY()) {
            if (aRect.maxX() > maxX()) {
                rects.addElement(Rect.newRect(x, y, width, aRect.y - y));
            } else {
                rects.addElement(Rect.newRect(x, y, width, aRect.y - y));
                rects.addElement(Rect.newRect(aRect.maxX(), aRect.y,
                                              maxX() - aRect.maxX(),
                                              maxY() - aRect.y));
            }

            filterEmptyRects(rects);
            return;
        }

        /* 3 */
        if (aRect.x <= x) {
            if (aRect.maxX() >= maxX()) {
                rects.addElement(Rect.newRect(x, y, width, aRect.y - y));
                rects.addElement(Rect.newRect(x, aRect.maxY(), width,
                                              maxY() - aRect.maxY()));
            } else {
                rects.addElement(Rect.newRect(x, y, width, aRect.y - y));
                rects.addElement(Rect.newRect(aRect.maxX(), aRect.y,
                                              maxX() - aRect.maxX(),
                                              aRect.height));
                rects.addElement(Rect.newRect(x, aRect.maxY(), width,
                                              maxY() - aRect.maxY()));
            }

            filterEmptyRects(rects);
            return;
        }

        /* 4 */
        if (aRect.x <= maxX() && aRect.maxX() > maxX()) {
            if (aRect.y <= y && aRect.maxY() > maxY()) {
                rects.addElement(Rect.newRect(x, y, aRect.x - x, height));
            } else if (aRect.y <= y) {
                rects.addElement(Rect.newRect(x, y, aRect.x - x,
                                              aRect.maxY() - y));
                rects.addElement(Rect.newRect(x, aRect.maxY(), width,
                                              maxY() - aRect.maxY()));
            } else if (aRect.maxY() > maxY()) {
                rects.addElement(Rect.newRect(x, y, width, aRect.y - y));
                rects.addElement(Rect.newRect(x, aRect.y, aRect.x - x,
                                              maxY() - aRect.y));
            } else {
                rects.addElement(Rect.newRect(x, y, width, aRect.y - y));
                rects.addElement(Rect.newRect(x, aRect.y, aRect.x - x,
                                              aRect.height));
                rects.addElement(Rect.newRect(x, aRect.maxY(), width,
                                              maxY() - aRect.maxY()));
            }

            filterEmptyRects(rects);
            return;
        }

        /* 5 */
        if (aRect.x >= x && aRect.maxX() <= maxX()) {
            if (aRect.y <= y && aRect.maxY() > maxY()) {
                rects.addElement(Rect.newRect(x, y, aRect.x - x, height));
                rects.addElement(Rect.newRect(aRect.maxX(), y,
                                              maxX() - aRect.maxX(), height));
            } else if (aRect.y <= y) {
                rects.addElement(Rect.newRect(x, y, aRect.x - x, height));
                rects.addElement(Rect.newRect(aRect.x, aRect.maxY(),
                                              aRect.width,
                                              maxY() - aRect.maxY()));
                rects.addElement(Rect.newRect(aRect.maxX(), y,
                                              maxX() - aRect.maxX(), height));
            } else {
                rects.addElement(Rect.newRect(x, y, aRect.x - x, height));
                rects.addElement(Rect.newRect(aRect.x, y, aRect.width,
                                              aRect.y - y));
                rects.addElement(Rect.newRect(aRect.maxX(), y,
                                              maxX() - aRect.maxX(), height));
            }

            filterEmptyRects(rects);
            return;
        }

    }



/* archiving */


    /** Describes the Rect class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Rect", 1);
        info.addField(X_KEY, INT_TYPE);
        info.addField(Y_KEY, INT_TYPE);
        info.addField(WIDTH_KEY, INT_TYPE);
        info.addField(HEIGHT_KEY, INT_TYPE);
    }

    /** Encodes the Rect.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeInt(X_KEY, x);
        encoder.encodeInt(Y_KEY, y);
        encoder.encodeInt(WIDTH_KEY, width);
        encoder.encodeInt(HEIGHT_KEY, height);
    }

    /** Decodes the Rect.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        x = decoder.decodeInt(X_KEY);
        y = decoder.decodeInt(Y_KEY);
        width = decoder.decodeInt(WIDTH_KEY);
        height = decoder.decodeInt(HEIGHT_KEY);
    }

    /** Finishes the Rect decoding.  This method does nothing.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }




/* rect cache */


    /** Returns a Rect from the Rect cache with origin (<b>x</b>, <b>y</b>)
      * and size (<b>width</b>, <b>height</b>).  Creates a new Rect if the
      * cache is empty.
      * @private
      */
    static Rect newRect(int x, int y, int width, int height) {
        Rect    theRect;

        synchronized(_rectCache) {
            if (!_cacheRects || _rectCache.isEmpty()) {
                return new Rect(x, y, width, height);
            }

            theRect = (Rect)_rectCache.removeLastElement();
        }
        theRect.setBounds(x, y, width, height);

        return theRect;
    }

    /** Returns a Rect from the Rect cache whose origin and size match
      * <b>templateRect</b>.  Creates a new Rect if the cache is empty.
      * @private
      */
    static Rect newRect(Rect templateRect) {
        if (templateRect == null) {
            return Rect.newRect(0, 0, 0, 0);
        } else {
            return Rect.newRect(templateRect.x, templateRect.y,
                                templateRect.width, templateRect.height);
        }
    }

    /** Returns a Rect from the Rect cache with origin (0, 0) and zero size.
      * Equivalent to the code:
      * <pre>
      *     Rect.newRect(0, 0, 0, 0);
      * </pre>
      * Creates a new Rect if the cache is empty.
      * @private
      */
    static Rect newRect() {
        return Rect.newRect(0, 0, 0, 0);
    }


    /** Places aRect back in the Rect cache (if the cache is not full).
      * @private
      */
    static void returnRect(Rect aRect) {
        if (!_cacheRects) {
            return;
        }

        synchronized(_rectCache) {
            if (_rectCache.count() < 50) {
                _rectCache.addElement(aRect);
            }
        }
    }

    /** Places the Rects contained in <b>rects</b> back in the Rect cache
      * (if the cache is not full) and empties the Vector.
      * @private
      */
    static void returnRects(Vector rects) {
        int     i;

        if (rects == null || !_cacheRects) {
            return;
        }

        i = rects.count();
        while (i-- > 0) {
            Rect.returnRect((Rect)rects.elementAt(i));
        }

        rects.removeAllElements();
    }

    /** Enables and disables Rect caching.  With setShouldCacheRects(false),
      * Rect.newRect() methods create new Rects and Rect.returnRect() methods
      * do nothing with the Rects they're given.  Disabling Rect caching can
      * help you track down problems in your code of returning Rects to the
      * cache while accidentally continuing to maintain references to them.
      * @private
      */
    static void setShouldCacheRects(boolean flag) {
        synchronized(_rectCache) {
            _cacheRects = flag;
            if (!_cacheRects) {
                _rectCache.removeAllElements();
            }
        }
    }
}
