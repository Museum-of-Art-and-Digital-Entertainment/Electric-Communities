// Border.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Abstract Object subclass responsible for drawing the adornments around many
  * user interface components. By implementing the five primitive Border
  * methods, you can create additional Border styles and use them with any
  * component that accepts Borders. Use the provided convenience methods to
  * compute often used quantities of a particular Border, such as its
  * interior rectangle.
  */

// ALERT! - I think we need to define the empty constructor

public abstract class Border implements Codable {
    /** Primitive method that returns the width of the Border's left side.
      */
    public abstract int leftMargin();

    /** Primitive method that returns the width of the Border's right side.
      */
    public abstract int rightMargin();

    /** Primitive method that returns the height of the Border's top side.
      */
    public abstract int topMargin();

    /** Primitive method that returns the height of the Border's bottom side.
      */
    public abstract int bottomMargin();

    /** Primitive method for drawing the Border in the specified rectangle.
      */
    public abstract void drawInRect(Graphics g, int x, int y, int width,
        int height);

    /** Convenience method for drawing the Border in the Rect <b>rect</b>.
      * Equivalent to the code:
      * <pre>
      *      drawInRect(g, rect.x, rect.y, rect.width, rect.height);
      * </pre>
      */
    public void drawInRect(Graphics g, Rect rect) {
        drawInRect(g, rect.x, rect.y, rect.width, rect.height);
    }

    /** Returns the Border's width margin, the sum of <b>leftMargin()</b>
      * and <b>rightMargin()</b>.
      * @see #leftMargin
      * @see #rightMargin
      */
    public int widthMargin() {
        return leftMargin() + rightMargin();
    }

    /** Returns the Border's height margin, the sum of <b>topMargin()</b>
      * and <b>bottomMargin()</b>.
      * @see #topMargin
      * @see #bottomMargin
      */
    public int heightMargin() {
        return topMargin() + bottomMargin();
    }

    /** Computes the Border's interior Rect using the primitive margin
      * methods and the coordinates of the given rectangle.  Places the results
      * in <b>interiorRect</b>.
      */
    public void computeInteriorRect(int x, int y, int width, int height,
        Rect interiorRect) {
        int left, top;

        left = leftMargin();
        top = topMargin();

        interiorRect.setBounds(x + left, y + top, width - left - rightMargin(),
                               height - top - bottomMargin());
    }

    /** Computes the Border's interior Rect using the primitive margin
      * methods and the coordinates of the Rect <b>rect</b>.  Places the
      * results in <b>interiorRect</b>.
      */
    public void computeInteriorRect(Rect rect, Rect interiorRect) {
        int left, top;

        left = leftMargin();
        top = topMargin();

        interiorRect.setBounds(rect.x + left, rect.y + top,
                               rect.width - left - rightMargin(),
                               rect.height - top - bottomMargin());
    }

    /** Computes the Border's interior rectangle using the primitive margin
      * methods and the coordinates of the given rectangle.  Returns the
      * interior Rect.
      */
    public Rect interiorRect(int x, int y, int width, int height) {
        int left, top;

        left = leftMargin();
        top = topMargin();

        return new Rect(x + left, y + top,
            width - left - rightMargin(), height - top - bottomMargin());
    }

    /** Computes the Border's interior rectangle using the primitive margin
      * methods and the coordinates of the Rect <b>rect</b>.  Returns the
      * interior Rect.
      */
    public Rect interiorRect(Rect rect) {
        int left, top;

        left = leftMargin();
        top = topMargin();

        return new Rect(rect.x + left, rect.y + top,
            rect.width - left - rightMargin(),
            rect.height - top - bottomMargin());
    }

    // The archiving method are here just for convenience.  Stateless Border
    // subclasses won't need to implement these stubs.

    /** Defined so that Borders are Codable.  Stateless Borders need not
      * override.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
    }

    /** Defined so that Borders are Codable.  Stateless Borders need not
      * override.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
    }

    /** Defined so that Borders are Codable.  Stateless Borders need not
      * override.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
    }

    /** Defined so that Borders are Codable.  Stateless Borders need not
      * override.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
