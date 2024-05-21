
/**
 * SLListItem.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 *
 * 970900   dima    introduced ECListItem and subclassed from it, moved drawInRect there
 * 970905   dima    overrode drawInRect to draw two-lined titles
 * 970813   agm     added selectedTextColor stuff
 *
 */

package ec.ifc.stonelook;

import netscape.application.Color;
import netscape.application.Font;
import netscape.application.Graphics;
import netscape.application.Rect;

import ec.ifc.app.ECListItem;

/**
 * Subclass of ECListItem that applies the stone-texture look.
 * SLListViews use these by default, so you usually won't need to
 * explicitly create them.
 */
public class SLListItem extends ECListItem {

    private Color mySelectedTextColor = StoneLook.listItemSelectedTitleColor();
    
    //
    // constructors
    //
    
    public SLListItem() {
        setSelectedColor(StoneLook.listItemSelectedColor());
    }
    
    //
    // public methods
    //
    
     /** Returns the color the SLListItem uses to draw its text when
      * selected.
      */
    public Color selectedTextColor() {
        return mySelectedTextColor; 
    }
    
    /** Sets the color the SLListItem uses to draw its text when
      * selected.
      */
    public void setSelectedTextColor(Color color) {
        mySelectedTextColor = color;
    }
    
    /**
     * Overridden to draw selected item over textured background with colored text.
     */
    protected void drawStringInRect(Graphics g, String title,
                                 Font titleFont, Rect textBounds,
                                 int justification) {

        if (!isSelected()) {
            super.drawStringInRect(g, title, titleFont, textBounds, justification);
            return;
        }

        g.setColor(mySelectedTextColor);
        g.setFont(titleFont);       
        g.drawStringInRect(title, textBounds, justification);
    }
}
