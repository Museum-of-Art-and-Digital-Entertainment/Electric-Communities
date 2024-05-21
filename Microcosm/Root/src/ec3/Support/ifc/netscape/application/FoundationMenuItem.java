// FoundationMenuItem.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** FoundationMenuItem is a subclass of java.awt.MenuItem that overrides
  * <b>postEvent()</b> to forward a selection event to an associated IFC
  * MenuItem.
  *
  * @see Menu
  * @author David Karlton
  * @version     1.0
  */

class FoundationMenuItem extends java.awt.MenuItem {
    MenuItem menuItem;

    /** Constructor.
      */
    public FoundationMenuItem(String label) {
        this(label, null);
    }

    /** Convenience constructor that also sets the IFC MenuItem.
      */
    public FoundationMenuItem(String label, MenuItem item) {
        super(label);
        setMenuItem(item);
    }

    /** Adds the event to the IFC event queue.
      */
    public boolean postEvent(java.awt.Event evt) {
        Event event;
        Application app;
        Menu menu;
        MenuItem tmpMenuItem;

        menu = menuItem.supermenu();
        while (!menu.isTopLevel()) {
            tmpMenuItem = menu.superitem();
            menu = tmpMenuItem.supermenu();
        }

        app = menu.application();
        if (!app.isModalViewShowing()) {
            event = new Event();
            event.setProcessor(menuItem);
            app.eventLoop().addEvent(event);
        } else {
            //app.beep();
        }

        return true;
    }

    /** Sets the IFC MenuItem that maintains this FoundationMenuItem.
      */
    public void setMenuItem(MenuItem aMenuItem) {
        menuItem = aMenuItem;
    }

    /** Returns the IFC MenuItem maintained by this FoundationMenuItem.
      */
    public MenuItem menuItem() {
        return menuItem;
    }
}
