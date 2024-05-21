// JDKMenuShortcut.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application.jdk11compatibility;

import netscape.application.*;
import java.awt.MenuShortcut;

/** @private */
public class JDKMenuShortcut implements netscape.application.MenuShortcut {
    public void setMenuShortcut(java.awt.MenuItem item, char key) {
        java.awt.MenuShortcut  s;

        s = new java.awt.MenuShortcut((int)key);
        item.setShortcut(s);
    }
}
