// InternalAlertBorder.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** InternalAlertBorder draws a yellow and black stripe.
  *
  * @author    AW
  * @version   1.0
  */
class InternalAlertBorder extends InternalWindowBorder {
    private Bitmap stripe;

    public InternalAlertBorder(InternalWindow aWindow) {
        super(aWindow);
        stripe = SystemImages.alertStripe();
    }

    public int topMargin() {
        return stripe.height();
    }

    public void drawTitleBar(Graphics g, int x, int y, int width,
                                 int height) {
        int maxX,maxY;



        maxX = width - 1;
        maxY = topMargin() - 1;

        stripe.drawTiled(g,x+2,y,maxX - x - 3,topMargin());

        g.setColor(new Color(101, 101, 101));
        g.drawPoint(x, y);
        g.drawPoint(maxX, y);

        g.setColor(new Color(89, 89, 89));
        g.drawLine(x + 1, y, maxX - 1, y);
        g.drawLine(x, y + 1, x, maxY);

        g.setColor(new Color(218, 218, 218));
        g.drawPoint(x + 1, y + 1);
        g.drawPoint(x + 2, y + 2);

        g.setColor(Color.white);
        g.drawLine(x + 1, y + 2, x + 1, maxY - 2);
        g.drawLine(x + 2, y + 1, maxX - 2, y + 1);

        g.setColor(new Color(143, 143, 143));
        g.drawPoint(maxX - 1, y + 1);
        g.drawLine(maxX - 1, y + 2, maxX - 1, maxY - 1);

        g.setColor(Color.gray);
        g.drawLine(x + 2, maxY - 1, maxX - 2, maxY - 1);

        g.setColor(Color.darkGray);
        g.drawLine(maxX, y + 1, maxX, maxY);
        g.drawLine(x + 1, maxY, maxX - 1, maxY);

        g.setColor(new Color(165, 165, 165));
        g.drawPoint(x + 1, maxY - 1);
        g.drawPoint(x + 2, maxY - 2);
    }
}

