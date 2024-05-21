/**
 * ECPopup.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by Dima Nasledov
 *
 */
package ec.ifc.app;

import netscape.application.Application;
import netscape.application.Border;
import netscape.application.Font;
import netscape.application.FontMetrics;
import netscape.application.Image;
import netscape.application.ListItem;
import netscape.application.MouseEvent;
import netscape.application.Popup;
import netscape.application.Rect;

import netscape.util.Vector;

/**
 * This is a subclass of netscape.application.Popup class
 * The parent class doesn't trim it's title, so that when
 * it is too long, it will just be clipped, quite ugly sometimes.
 * We want to maintain it's pretty look
 */
public class ECPopup extends Popup  {
    private Vector myTitles = null;

    public ECPopup() {
        this(0, 0, 0, 0);
    }

    public ECPopup(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    public ECPopup(int x, int y, int width, int height) {
        super(x, y, width, height);
        myTitles = new Vector();
        // make the popup list be one of our special ones that knows how
        // to stay popped up properly
        setPopupList(new ECListView(true));
    }

    /** Adds a ListItem with the given title and command to the Popup. Calls
      * <b>addItem()</b> on the Popup's ListView.
      * @see ListView#addItem
      */
    public ListItem addItem(String title, String command) {
        myTitles.addElement(title);
        return super.addItem(trimString(title), command);
    }

    /** Removes all ListItems from the Popup.
      */
    public void removeAllItems() {
        myTitles.removeAllElements();
        super.removeAllItems();
    }

    /** Removes the ListItem with title <b>title</b> from the Popup.
      */
    public void removeItem(String title) {
        myTitles.removeElement(title);
        super.removeItem(trimString(title));
    }

    /** Removes the ListItem at <b>index</b>.
      *
      */
    public void removeItemAt(int index) {
        myTitles.removeElementAt(index);
        super.removeItemAt(index);
    }

    /** Primitive method for changing a View's bounds Rect. Sets the
      * View's bounding rectangle and then calls <b>didMoveBy()</b>
      * with the old origin, and <b>didSizeBy()</b> with the old size. It also
      * adjusts the size of its drawingBuffer, if any, and notifies its
      * superview if this View's size has changed.
      * @see #bounds()
      * @see #localBounds()
      */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        int count = count();

        for (int i = 0; i < count; i++) {
            itemAt(i).setTitle(trimString((String)myTitles.elementAt(i)));
        }
    }
    
    /** overridden to register with application as currentPopup */
    protected void showPopupWindow(MouseEvent event) {
        Application app = Application.application();
        if (app instanceof ECApplication) {
            ((ECApplication)app).setCurrentPopup(this);
        }
        super.showPopupWindow(event);
    }
    
    //
    // private methods
    //

    /**
     * trim the string to fit into the popup label
     */
    private String trimString(String srcString) {
        Border      border = border();
        Image       image = popupImage();
        int         width = width();

        if (image != null) {
            width -= image.width();
        }

        if (border != null) {
            width -= border.leftMargin() + border.rightMargin();
        }
        
        return ECStringUtilities.ellipsizedString(
            srcString, width, prototypeItem().font());
    }
}

