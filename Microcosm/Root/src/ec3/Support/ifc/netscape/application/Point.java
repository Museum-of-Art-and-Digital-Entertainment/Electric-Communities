// Point.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass representing an (x, y) coordinate.
  */

public class Point implements Codable {
    /** The Point's X-coordinate. */
    public int                  x;
    /** The Point's Y-coordinate. */
    public int                  y;

    static private Vector       _pointCache = new Vector();
    static private boolean      _cachePoints = true;

    static final String         X_KEY = "x";
    static final String         Y_KEY = "y";



/* constructors */

    /** Constructs a Point with coordinates (0, 0).
      */
    public Point() {
    }

    /** Constructs a Point with coordinates (<b>x</b>, <b>y</b>).
      */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Constructs a Point with coordinates
      * (<b>templatePoint.x</b>, <b>templatePoint.y</b>).
      */
    public Point(Point templatePoint) {
        x = templatePoint.x;
        y = templatePoint.y;
    }


/* actions */


    /** Returns the Point's String representation.
      */
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /** Sets the Point's coordinates to (<b>x</b>, <b>y</b>).
      */
    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Moves the Point by (<b>deltaX</b>, <b>deltaY</b>).
      */
    public void moveBy(int deltaX, int deltaY) {
        x += deltaX;
        y += deltaY;
    }

    /** Returns <b>true</b> if the Point equals <b>anObject</b>.
      */
    public boolean equals(Object anObject) {
        Point aPoint;

        if (!(anObject instanceof Point))
            return false;

        aPoint = (Point)anObject;
        return (aPoint.x == x && aPoint.y == y);
    }

    /** Returns the Point's hash code.
      */
    public int hashCode() {
        // ALERT!
        // This is an arbitrarily choosen hash implementation.
        // There should be a better one for points.
        return x ^ y;
    }



/* archiving */


    /** Describes the Point class' coding information.
     * @see Codable#describeClassInfo
     */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Point", 1);
        info.addField(X_KEY, INT_TYPE);
        info.addField(Y_KEY, INT_TYPE);
    }

    /** Encodes the Point.
     * @see Codable#encode
     */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeInt(X_KEY, x);
        encoder.encodeInt(Y_KEY, y);
    }

    /** Decodes the Point.
     * @see Codable#decode
     */
    public void decode(Decoder decoder) throws CodingException {
        x = decoder.decodeInt(X_KEY);
        y = decoder.decodeInt(Y_KEY);
    }

    /** Finishes the Point decoding.
     * @see Codable#finishDecoding
     */
    public void finishDecoding() throws CodingException {
    }



/* point cache */


    /** Returns a Point from the Point cache with coordinates
      * (<b>x</b>, <b>y</b>).  Creates a new Point if the cache is empty.
      * @private
      */
    static Point newPoint(int x, int y) {
        Point   thePoint;

        synchronized(_pointCache) {
            if (!_cachePoints || _pointCache.isEmpty()) {
                return new Point(x, y);
            }

            thePoint = (Point)_pointCache.removeLastElement();
        }
        thePoint.moveTo(x, y);

        return thePoint;
    }

    /** Returns a Point from the Point cache with the same coordinates as
      * <b>templatePoint</b>.  Creates a new Point if the cache is empty.
      * @private
      */
    static Point newPoint(Point templatePoint) {
        if (templatePoint == null) {
            return Point.newPoint(0, 0);
        } else {
            return Point.newPoint(templatePoint.x, templatePoint.y);
        }
    }

    /** Returns a Point from the Point cache with coordinates (0, 0).
      * Equivalent to the code:
      * <pre>
      *     aPoint = Point.newPoint(0, 0);
      * </pre>
      * Creates a new Point if the cache is empty.
      * @private
      */
    static Point newPoint() {
        return Point.newPoint(0, 0);
    }

    /** Places <b>aPoint</b> back in the Point cache (if the cache is not full).
      * @private
      */
    static void returnPoint(Point aPoint) {
        if (!_cachePoints) {
            return;
        }

        synchronized(_pointCache) {
            if (_pointCache.count() < 30) {
                _pointCache.addElement(aPoint);
            }
        }
    }

    /** Places the Points contained in <b>points</b> back in the Point cache
      * (if the cache is not full) and empties the Vector.
      * @private
      */
    static void returnPoints(Vector points) {
        int     i;

        if (points == null || !_cachePoints) {
            return;
        }

        i = points.count();
        while (i-- > 0) {
            Point.returnPoint((Point)points.elementAt(i));
        }

        points.removeAllElements();
    }

    /** Enables or disables Point caching.  With setShouldCachePoints(false),
      * Point.newPoint() methods create new Point instances and
      * Point.returnPoint() methods do nothing with the Points they're given.
      * Disabling Point caching can help you track down problems in your code
      * where you return Points to the cache but accidentally continue to
      * maintain references to them.
      * @private
      */
    static void setShouldCachePoints(boolean flag) {
        synchronized(_pointCache) {
            _cachePoints = flag;

            if (!_cachePoints) {
                _pointCache.removeAllElements();
            }
        }
    }
}
