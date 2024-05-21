// HRTextAttachment.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** TextAttachment subclass to display an horizontal rule.
  * @see TextAttachment
  * @private
  */
public class HRTextAttachment extends TextAttachment {
    private static int WIDTH_OFFSET=10;
    private static int HEIGHT = 12;

    public HRTextAttachment() {
        super();
    }
    public int width() {
        return owner().width();
    }

    public int height() {
        return HEIGHT;
    }
    public void drawInRect(Graphics g, Rect boundsRect) {
        Rect r = new Rect();
        if (g == null || boundsRect == null) {
            return;
        }
        r.x = boundsRect.x + WIDTH_OFFSET;
        r.width = boundsRect.width - (2*WIDTH_OFFSET);
        r.height = 2;
        r.y = boundsRect.y + ((HEIGHT - 2) / 2);
        g.setColor(Color.darkGray);
        g.fillRect( r );
        r.y++;
        r.height = 1;
        g.setColor(Color.lightGray);
        g.fillRect( r );
    }
}



