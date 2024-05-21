package ec.ifc.app;

import netscape.application.Target;
import netscape.application.Window;

/**
 * Interface for an object that controls a window's position and visibility. 
 */
public interface WindowController {

    /** Returns the controlled window */
    public Window getWindow();
    
    /** Hides the controlled window */
    public void hide();
    
    /** shows the controlled window if necessary and brings it to the front */
    public void show();
    
    /**
     * Repositions the controlled window based on relevant aspects of current
     * context such as screen size, other window locations, etc.
     */
    public void reposition();
    
    /** Sets which window is controlled. */
    public void setWindow(Window newWindow);

    /** 
     * Returns whether the controlled window's position should be adjusted
     * before the first time it is displayed.
     */
    public boolean shouldRepositionBeforeShowing();
}
