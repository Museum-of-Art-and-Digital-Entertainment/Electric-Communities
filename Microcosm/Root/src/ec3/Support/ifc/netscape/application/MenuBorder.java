// MenuBorder.java
// By Ned Etcode
// Copyright 1997  Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** The MenuBorder class draws a border around Menus.  Checks to see if
  * its Menu is top-level and VERTICAL to draw a simple border.
  * @private
  */
public class MenuBorder extends Border {
    Menu                   menu;

    final static String    MENU_KEY = "menu";


    /** Constructor.
      */
    public MenuBorder() {
        this(null);
    }

    public MenuBorder(Menu aMenu) {
        super();
        menu = aMenu;
    }

    public void setMenu(Menu aMenu) {
        menu = aMenu;
    }

    public int leftMargin() {
        if (menu.isTopLevel() && menu.menuView.type() != MenuView.VERTICAL) {
            return 0;
        }
        return 1;
    }

    public int rightMargin() {
        if (menu.isTopLevel() && menu.menuView.type() != MenuView.VERTICAL) {
            return 0;
        }
        return 4;
    }

    public int topMargin() {
        if (menu.isTopLevel() && menu.menuView.type() != MenuView.VERTICAL) {
            return 0;
        }
        return 1;
    }

    public int bottomMargin() {
        if (menu.isTopLevel() && menu.menuView.type() != MenuView.VERTICAL) {
            return 1;
        }
        return 4;
    }

    /** Primitive method for drawing the border in the given Rect. */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        if (menu.isTopLevel() && menu.menuView.type() != MenuView.VERTICAL) {
            g.setColor(Color.gray102);
            g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
            return;
        }
        // main border
        g.setColor(Color.gray231);
        g.drawLine(x, y, x, y + height - 4);
        g.drawLine(x + 1, y, x + width - 4, y);

        g.setColor(Color.gray153);
        g.drawLine(x + 1, y + height - 4, x + width - 4, y + height - 4);
        g.drawLine(x + width - 4, y + 1, x + width - 4, y + height - 5);

        // drop shadow
        g.setColor(Color.darkGray);
        g.drawLine(x + 4, y + height - 3, x + width - 3, y + height - 3);
        g.drawLine(x + width - 3, y + 4, x + width - 3, y + height - 4);

        g.setColor(Color.gray102);
        g.drawLine(x + 5, y + height - 2, x + width - 2, y + height - 2);
        g.drawLine(x + width - 2, y + 5, x + width - 2, y + height - 3);

        g.setColor(Color.gray153);
        g.drawLine(x + 6, y + height - 1, x + width - 1, y + height - 1);
        g.drawLine(x + width - 1, y + 6, x + width - 1, y + height - 2);
    }

    /** Describes the MenuBorder class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);
        info.addClass("netscape.application.MenuBorder", 1);
        info.addField(MENU_KEY, OBJECT_TYPE);
    }

    /** Encodes the MenuBorder instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);
        encoder.encodeObject(MENU_KEY, menu);
    }

    /** Decodes the MenuBorder instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);
        menu = (Menu)decoder.decodeObject(MENU_KEY);
    }
}
