//
// ECProgressBar - 
//
// Copyright 1997 Electric Communities.  All rights reserved.
//
//  971203  agm override mouse so that the slider cannot be used.
//              cannot just disable as the color would be wrong.

package ec.ifc.app;

import netscape.application.Border;
import netscape.application.Color;
import netscape.application.Graphics;
import netscape.application.MouseEvent;
import netscape.application.Rect;
import netscape.application.Scrollable;
import netscape.application.View;

public class ECProgressBar extends ECSlider {
    
    /** color drawn in filled area */
    protected Color myFillColor = null;
    
    //
    // constructor
    //
    
    /** returns a new ECProgress bar with empty bounds */
    public ECProgressBar() {
        this(0, 0, 0, 0, Scrollable.HORIZONTAL);
    }
    
    /** returns a new ECProgress bar with the given bounds */
    public ECProgressBar(int x, int y, int width, int height, int orientation) {
        super(x, y, width, height, orientation);
        myFillColor = Color.black;
    }
    
    //
    // public methods
    //
    
    /** overridden because progress bars should not be settable by clicking :> */
    public boolean mouseDown(MouseEvent event) {
        if (!isEnabled()) {
            return false;
        } 
        return true;
    }
    
    public void mouseDragged(MouseEvent event) {
    }
 
    /** overridden to draw border, background color, and fill color */
    public void drawView(Graphics g) {
        Border border = border();

        Rect interior = new Rect(0, 0, bounds.width, bounds.height);
        border.drawInRect(g, interior);
        
        border.computeInteriorRect(interior, interior);
        
        g.setColor(backgroundColor());
        g.fillRect(interior);
        
        if (myIsVertical) {
            interior.height = mySliderPosition;
        } else {
            interior.width = mySliderPosition;
        }
        g.setColor(fillColor());
        g.fillRect(interior);
    }

    /** returns color drawn in filled area */
    public Color fillColor() {
        return myFillColor;
    }
    
    /** Sets color drawn in filled area */
    public void setFillColor(Color newColor) {
        myFillColor = newColor;
    }
}
