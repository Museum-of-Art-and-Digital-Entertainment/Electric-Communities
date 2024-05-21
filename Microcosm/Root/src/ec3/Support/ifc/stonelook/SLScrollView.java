package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Subclass of ScrollView that applies the stone-texture look.
 * SLScrollGroup uses these by default, so if you're using an SLScrollGroup
 * you don't need to worry about modifying the appearance further.
 * @see SLScrollGroup
 */
class SLScrollView extends ScrollView {

    //
    // Constructors
    //
    
    /** creates a new SLScrollView with empty bounds */
    public SLScrollView() {
        this(0, 0, 0, 0);
    }

    /** creates a new SLScrollView with specified bounds */ 
    public SLScrollView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** creates a new SLScrollView with specified bounds */ 
    public SLScrollView(int x, int y, int width, int height) {
        super(x, y, width, height);
        
        setBackgroundColor(StoneLook.dataBackgroundColor());
    }
}

