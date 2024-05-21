// Polygon.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

// ALERT! Using a java.awt.Polygon here is not necessarily what you want.

/** Object subclass representing an ordered list of points.
  */

public class Polygon implements Codable {
    /* The total number of points in the Polygon. */
    public int numPoints;

    /* The X-coordinates of the Polygon's points. */
    public int xPoints[];

    /* The Y-coordinates of the Polygon's points. */
    public int yPoints[];

    java.awt.Polygon awtPolygon;

    /** Constructs a Polygon with no vertices.
      */
    public Polygon() {
        awtPolygon = new java.awt.Polygon();
        update();
    }

    /** Constructs a polygon with the given points.
      */
    public Polygon(int xPoints[], int yPoints[], int numPoints) {
        awtPolygon = new java.awt.Polygon(xPoints, yPoints, numPoints);
        update();
    }

    /** Adds the point (<b>x</b>, <b>y</b>) to the end of the Polygon's list
      * of points.
      */
    public void addPoint(int x, int y) {
        awtPolygon.addPoint(x, y);
        update();
    }

    /** Returns the Polygon's bounding Rect.
      */
    public Rect boundingRect() {
        java.awt.Rectangle box;
        Rect rect;

        box = awtPolygon.getBoundingBox();
        rect = new Rect(box.x, box.y, box.width, box.height);
        update();

        return rect;
    }

    /** Returns <b>true</b> if the Polygon contains the point
      * (<b>x</b>, <b>y</b>).
      */
    public boolean containsPoint(int x, int y) {
        boolean contains;

        contains = awtPolygon.inside(x, y);
        update();

        return contains;
    }

    /** Returns <b>true</b> if the Polygon contains the point <b>aPoint</b>.
      */
    public boolean containsPoint(Point aPoint) {
        return containsPoint(aPoint.x, aPoint.y);
    }

    /** Moves each of the Polygon's points by <b>deltaX</b> and <b>deltaY</b>.
      */
    public void moveBy(int deltaX, int deltaY) {
        int     i;

        i = awtPolygon.npoints;
        while (i-- > 0) {
            awtPolygon.xpoints[i] += deltaX;
            awtPolygon.ypoints[i] += deltaY;
        }
    }

    private void update() {
        numPoints = awtPolygon.npoints;
        xPoints = awtPolygon.xpoints;
        yPoints = awtPolygon.ypoints;
    }

    static final String XPOINTS = "xPoints";
    static final String YPOINTS = "yPoints";

    /** Describes the Polygon class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Polygon", 1);
        info.addField(XPOINTS, INT_ARRAY_TYPE);
        info.addField(YPOINTS, INT_ARRAY_TYPE);
    }

    /** Encodes the Polygon.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        if (numPoints == 0)
            return;

        encoder.encodeIntArray(XPOINTS, xPoints, 0, numPoints);
        encoder.encodeIntArray(YPOINTS, yPoints, 0, numPoints);
    }

    /** Decodes the Polygon.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        int i, xs[], ys[];

        xs = decoder.decodeIntArray(XPOINTS);
        ys = decoder.decodeIntArray(YPOINTS);

        if (xs == null || xs.length == 0)
            return;

        numPoints = xs.length;

        for (i = 0; i < xs.length; i++)
            addPoint(xs[i], ys[i]);
    }

    /** Finishes the Polygon instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
