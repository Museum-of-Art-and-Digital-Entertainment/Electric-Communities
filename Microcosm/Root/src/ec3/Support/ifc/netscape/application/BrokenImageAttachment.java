// BrokenImageAttachment.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;
import java.net.*;


/** Private class to display a broken image
  */
class BrokenImageAttachment extends TextAttachment {
    private static int WIDTH  = 32;
    private static int HEIGHT = 32;

    public int width() {
        return WIDTH;
    }

    public int height() {
        return HEIGHT;
    }
    public void drawInRect(Graphics g, Rect boundsRect) {
        Rect r = new Rect();
        if (g == null || boundsRect == null) {
            return;
        }
        g.setColor(Color.lightGray);
        g.fillRect( boundsRect);

        g.setColor(Color.black);
        g.fillRect(boundsRect.x,boundsRect.y, boundsRect.width , 1);
        g.fillRect(boundsRect.x + boundsRect.width - 1, boundsRect.y, 1, boundsRect.height);
        g.fillRect(boundsRect.x,boundsRect.y, 1,boundsRect.height);
        g.fillRect(boundsRect.x,boundsRect.y + boundsRect.height-1,boundsRect.width,1);
    }
}
