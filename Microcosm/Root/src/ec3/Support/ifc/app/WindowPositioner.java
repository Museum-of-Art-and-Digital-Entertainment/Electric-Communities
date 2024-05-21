/**
 * WindowPositioner interface for managing window positions
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * John Sullivan
 */
package ec.ifc.app;
 
import netscape.application.Rect; 
import ec.ifc.app.ECExternalWindow;
 
public interface WindowPositioner  {
    
    /**
     * Returns a good position (bounding rectangle) for the given window.
     * Takes saved bounds (if any) into account.
     */ 
    public Rect recommendBounds(ECExternalWindow window);
    
    /** Stores the current bounds for the given window. */  
    public void saveBounds(ECExternalWindow window);

    /**
     * Returns the saved bounds for this window, possibly adjusted to fit
     * on the current screen or otherwise sanity-checked.
     */ 
    public Rect savedBounds(ECExternalWindow window);
    
    /**
     * Returns the saved bounds associated with this positioning key,
     * possibly adjusted to fit on the current screen or otherwise
     * sanity-checked. Sometimes useful to determine the size of a window
     * before the window has been re-created.
     */ 
    public Rect savedBoundsForKey(String positioningKey);
}
