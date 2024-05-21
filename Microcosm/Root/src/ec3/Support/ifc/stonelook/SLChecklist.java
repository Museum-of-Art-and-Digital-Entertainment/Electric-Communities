/* =====================================================================
 *    FILE: SLChecklist.java
 *    AUTHOR: John Sullivan
 *    CREATED: June 26, 1997
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 * =====================================================================
 *
 *  971203  agm     changed font to plain
 *  970813  agm     added selectedTextColor stuff
 */

package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECChecklist;

/**
 * A subclass of ECListView that applies the stone-texture look. 
 */
public class SLChecklist extends ECChecklist {

    /** Applies the stone-texture look. */   
    public SLChecklist () {
        super();

        // make list items look right
        setPrototypeItem(new SLListItem());
        SLListItem prototype = (SLListItem)prototypeItem();
        prototype.setFont(StoneLook.standardFontPlain());
        prototype.setSelectedTextColor(prototype.textColor());

        // set background color in case transparency is turned off
        setBackgroundColor(StoneLook.dataBackgroundColor());
        
        // use appropriate checked and unchecked images
        setCheckedImage(StoneLook.checkboxOn(true));
        setUncheckedImage(StoneLook.checkboxOff(true));
    }
}

