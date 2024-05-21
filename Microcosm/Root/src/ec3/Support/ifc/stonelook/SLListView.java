/**
 * SLListView.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 */
package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECListView;

/**
 * Subclass of ListView that applies the stone-texture look.
 */
public class SLListView extends ECListView {
    
    //
    // constructor
    //

    /** Returns a new SLListView not to be used in a popup menu */
    public SLListView() {
        this(false);
    }

    /**
     * Returns a new SLListView. <b>forPopup</b> determines whether
     * this list view will be used in a popup menu. This flavor of the
     * constructor is normally called only by the SLPopup constructor.
     */
    public SLListView(boolean forPopup) {
        super(forPopup);
        setPrototypeItem(new SLListItem());
        prototypeItem().setFont(StoneLook.standardFontPlain());
        setBackgroundColor(StoneLook.dataBackgroundColor());
    }
}

