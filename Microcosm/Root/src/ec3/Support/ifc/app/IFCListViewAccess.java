// IFCListViewAccess.java
// By Dimitry Nasledov
// Copyright 1997 Electric Communities.  All rights reserved.

package netscape.application;

import netscape.util.*;

/**
 * This class has been created to make it possible effectively
 * subclass from ListView class.  Unfortunately there are 5
 * package instance variables in ListView that don't have getter or
 * setter methods.
 */
public class IFCListViewAccess {

    public static final ListItem anchorItem(ListView lv) {
        return lv.anchorItem;
    }
    public static final void setAnchorItem(ListView lv, ListItem newItem) {
        lv.anchorItem = newItem;
    }
    public static final boolean tracking(ListView lv) {
        return lv.tracking;
    }
    public static final void setTracking(ListView lv, boolean newTracking) {
        lv.tracking = newTracking;
    }
    public static final Vector items(ListView lv) {
        return lv.items;
    }
    public static final ListItem origSelectedItem(ListView lv) {
        return lv.origSelectedItem;
    }
    public static final void setOrigSelectedItem(ListView lv, ListItem item) {
        lv.origSelectedItem = item;
    }
    public static final Vector dirtyItems(ListView lv) {
        return lv.dirtyItems;
    }
    public static final void setDirtyItems(ListView lv, Vector items) {
        lv.dirtyItems = items;
    }
}
