// ScrollViewLineBorder.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** The LineBorder class draws a 2 pixel wide colored line around the given
  * Rect.
  * @private
  */
public class ScrollViewLineBorder extends Border {

    /** LineBorders have a leftMargin of 1 pixel. */
    public int leftMargin() {
        return 2;
    }

    /** LineBorders have a rightMargin of 1 pixel. */
    public int rightMargin() {
        return 2;
    }

    /** LineBorders have a topMargin of 1 pixel. */
    public int topMargin() {
        return 2;
    }

    /** LineBorders have a bottomMargin of 1 pixel. */
    public int bottomMargin() {
        return 2;
    }

    /** Primitive method for drawing the border in the given Rect. */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.gray153);
        g.drawLine(x, y, x, height - 2);
        g.drawLine(x + 1, y, x + 1, height - 2);
        g.drawLine(x, y, width - 3, y);
        g.drawLine(x, y + 1, width - 3, y + 1);

        g.setColor(Color.lightGray);
        g.drawLine(x, height - 1, width - 1, height - 1);
        g.drawLine(width - 1, y, width - 1, height - 1);

        g.setColor(Color.gray231);
        g.drawLine(x + 2, height - 2, width - 2, height - 2);
        g.drawLine(width - 2, y, width - 2, height - 2);
    }

    /** Describes the LineBorder class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);
        info.addClass("netscape.application.ScrollViewLineBorder", 1);
    }
}
