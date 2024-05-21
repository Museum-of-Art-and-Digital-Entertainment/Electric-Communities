// FoundationCheckMenuItem.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** FoundationCheckMenuItem is a subclass of java.awt.CheckboxMenuItem
  * that overrides
  * <b>postEvent()</b> to forward a selection event to an associated IFC
  * MenuItem.
  *
  * @see Menu
  * @version     1.0
  */

class FoundationCheckMenuItem extends java.awt.CheckboxMenuItem {
    MenuItem menuItem;

    /** Constructor.
      */
    public FoundationCheckMenuItem(String label) {
        this(label, null);
    }

    /** Convenience constructor that also sets the IFC MenuItem.
      */
    public FoundationCheckMenuItem(String label, MenuItem item) {
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

    /** Sets the IFC MenuItem that maintains this FoundationCheckMenuItem.
      */
    public void setMenuItem(MenuItem aMenuItem) {
        menuItem = aMenuItem;
    }

    /** Returns the IFC MenuItem maintained by this FoundationCheckMenuItem.
      */
    public MenuItem menuItem() {
        return menuItem;
    }
}
