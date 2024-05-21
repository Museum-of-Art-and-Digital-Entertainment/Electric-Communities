// FontItem.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


class FontItem extends PopupItem {
    String      fontName;
    int         tag;


/* constructors */

    /** Constructs a FontItem. */
    FontItem() {
        super();
    }



/* actions */

    /** Sets the name of the Font that this FontItem represents. */
    public void setFontName(String aName) {
        fontName = aName;
    }

    /** Returns the name of the Font that this FontItem represents.
      * @see #setFontName
      */
    public String fontName() {
        return fontName;
    }

    /** Returns <b>true</b> if the FontItem's font name matches <b>aName</b>.
      */
    public boolean hasFontName(String aName) {
        if (aName == null) {
            return false;
        }

        return aName.equals(fontName);
    }

    /** Sets the FontItem's tag. */
    public void setTag(int tag) {
        this.tag = tag;
    }

    /** Returns the FontItem's tag.
      * @see #setTag
      */
    public int tag() {
        return tag;
    }
}
