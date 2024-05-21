package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECTabView;

/** 
 * ECTabView subclass that applies the stone-texture look using
 * SLLayeredTabItems. This tab view looks right when it just overlaps
 * a raised SLBezelBorder (typically on a ContainerView)
 * @see SLLayeredTabItem
 * @see SLButtonTabView
 */
public class SLLayeredTabView extends ECTabView  {

    //
    // constructors
    //
    
    /** Constructs an SLLayeredTabView object with bounds 0,0,0,0 */
    public SLLayeredTabView() {
        this(0, 0, 0, 0);
    }
    
    /** Constructs an SLLayeredTabView object with the given bounds */
    public SLLayeredTabView(Rect bounds) {
        this(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    /** Constructs an SLLayeredTabView object with the given bounds */
    public SLLayeredTabView(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
        setPrototypeItem(new SLLayeredTabItem());
    }   
}
