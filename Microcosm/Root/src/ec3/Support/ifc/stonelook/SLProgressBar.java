package ec.ifc.stonelook;

import netscape.application.Scrollable;
import ec.ifc.app.ECProgressBar;

public class SLProgressBar extends ECProgressBar {
    
    //
    // constructor
    //
    
    /** returns a new ECProgress bar with empty bounds */
    public SLProgressBar() {
        this(0, 0, 0, 0, Scrollable.HORIZONTAL);
    }
    
    /** returns a new ECProgress bar with the given bounds */
    public SLProgressBar(int x, int y, int width, int height, int orientation) {
        super(x, y, width, height, orientation);
        setBorder(new SLBezelBorder());
        setBackgroundColor(StoneLook.lightBackgroundColor());
        setFillColor(StoneLook.darkBackgroundColor());
    }
}
