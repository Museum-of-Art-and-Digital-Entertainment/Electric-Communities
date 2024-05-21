
/**
 * SLIconItem.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 */

package ec.ifc.stonelook;

import ec.ifc.app.ECIconItem;

import netscape.application.Color;
import netscape.application.Graphics;
import netscape.application.Rect;

/** Subclass of ECIconItem that draws selected item a different way. */
public class SLIconItem extends ECIconItem {

    //
    // constructors
    //
    public SLIconItem() {
        setFont(StoneLook.smallFontPlain());
        setBackgroundColor(StoneLook.dataBackgroundColor());
        setSelectedColor(StoneLook.listItemSelectedColor());
        setTextColor(StoneLook.staticTextColor());
        setSelectedTextColor(StoneLook.selectedTextColor());
    }
    
    //
    // public methods
    //
    
    /** Overridden to draw rounded, filled rect behind selected item */
    public void drawBackground(Graphics g, Rect boundsRect) {
        if (isSelected()) {
            // use copy of boundsRect to avoid modifying original
            Rect selectionRect = new Rect(boundsRect);
            Color selectedColor = selectedColor();

            // fill inset rectangle                     
            int inset = itemInset();
            selectionRect.growBy(-inset, -inset);
            g.setColor(selectedColor);
            g.fillRect(selectionRect);
            
            // draw rounded border around rectangle we just filled
            inset = StoneLook.bezelThickness();
            selectionRect.growBy(inset, inset);
            SLBezelBorder.drawBezel(g,
                                    selectionRect.x, selectionRect.y,
                                    selectionRect.width, selectionRect.height,
                                    true,
                                    selectedColor, selectedColor, null);
        }
    }   
}
