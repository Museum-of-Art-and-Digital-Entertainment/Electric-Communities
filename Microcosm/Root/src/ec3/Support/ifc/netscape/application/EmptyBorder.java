// EmptyBorder.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Border subclass with a zero margin border that draws nothing. EmptyBorders
  * act as placeholders for classes that take Borders but do not
  * want to check for <b>null</b> everywhere. As such, only implementors of
  * classes with Borders usually deal with this class. A typical application
  * removes a Border from an interface component by calling
  * <b>setBorder(null)</b>, which the component may note by setting its Border
  * to an EmptyBorder instance.
  */
public class EmptyBorder extends Border {
    private static Border emptyBorder;

    /** Convenience method for retrieving the shared EmptyBorder. */
    public static Border emptyBorder() {
        if (emptyBorder == null) {
            emptyBorder = new EmptyBorder();
        }

        return emptyBorder;
    }

    /** Constructs an EmptyBorder. */
    public EmptyBorder() {
        super();
    }

    /** Returns the EmptyBorder's left margin of 0 pixels. */
    public int leftMargin() {
        return 0;
    }

    /** Returns the EmptyBorder's right margin of 0 pixels. */
    public int rightMargin() {
        return 0;
    }

    /** Returns the EmptyBorder's top margin of 0 pixels. */
    public int topMargin() {
        return 0;
    }

    /** Returns the EmptyBorder's bottom margin of 0 pixels. */
    public int bottomMargin() {
        return 0;
    }

    /** Draws the EmptyBorder.  This method does nothing. */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
    }

    private static Class emptyBorderClass;

    private static Class emptyBorderClass() {
        if (emptyBorderClass == null) {
            emptyBorderClass = emptyBorder().getClass();
        }

        return emptyBorderClass;
    }

    /** Empty Borders store no additional state, but implement <b>decode()</b>
      * to replace themselves with the shared EmptyBorder instance, provided
      * they are not a subclass of EmptyBorder.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        if (getClass() == emptyBorderClass())
            decoder.replaceObject(emptyBorder());
    }
}
