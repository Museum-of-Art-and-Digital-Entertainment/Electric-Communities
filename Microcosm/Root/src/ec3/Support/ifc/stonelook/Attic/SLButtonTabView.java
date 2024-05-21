package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECTabView;

/** 
 * ECTabView subclass that applies the stone-texture look using
 * SLButtonTabItems. This tab view looks right when it just overlaps
 * a lowered SLBezelBorder (typically on a ContainerView)
 * @see SLButtonTabItem
 * @see SLLayeredTabView
 */
public class SLButtonTabView extends ECTabView  {

    //
    // constructors
    //
    
    /** Constructs an SLButtonTabView object with bounds 0,0,0,0 */
    public SLButtonTabView() {
        this(0, 0, 0, 0);
    }
    
    /** Constructs an SLButtonTabView object with the given bounds */
    public SLButtonTabView(Rect bounds) {
        this(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    /** Constructs an SLButtonTabView object with the given bounds */
    public SLButtonTabView(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
        setPrototypeItem(new SLButtonTabItem());
    }   
}
