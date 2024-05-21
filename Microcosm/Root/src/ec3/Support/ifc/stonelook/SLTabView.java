package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECTabView;

/** 
 * ECTabView subclass that applies the stone-texture look using
 * SLTabItems.
 * @see SLTabItem
 */
public class SLTabView extends ECTabView  {

    //
    // constructors
    //
    
    /** Constructs an SLTabView object with bounds 0,0,0,0 */
    public SLTabView() {
        this(0, 0, 0, 0, new SLTabItem(), DEFAULT_TAB_LOCATION);
    }
    
    /** Constructs an SLTabView object with the given bounds */
    public SLTabView(Rect bounds) {
        this(bounds.x, bounds.y, bounds.width, bounds.height, new SLTabItem(), 
            DEFAULT_TAB_LOCATION);
    }
    
    /** Constructs an SLTabView object with the given bounds */
    public SLTabView(int x, int y, int width, int height) {
        this(x, y, width, height, new SLTabItem(), DEFAULT_TAB_LOCATION);
    }
    
    /** Constructs an SLTabView object with the given bounds */
    public SLTabView(int x, int y, int width, int height, int location) {
        this(x, y, width, height, new SLTabItem(), location);
    }

    /** Constructs an SLTabView object with the given bounds */
    private SLTabView(int x, int y, int width, int height, SLTabItem item, int location) {
        super(x, y, width, height, item, location);
        setButtonImages();
    }

    //
    // private methods
    //
    private void setButtonImages() {
        if (isHorizontal()) {
            setTopLeftButtonImage(StoneLook.tabLeftImage());
            setTopLeftButtonAltImage(StoneLook.tabLeftPressedImage());
            setBottomRightButtonImage(StoneLook.tabRightImage());
            setBottomRightButtonAltImage(StoneLook.tabRightPressedImage());

            setOtherTopLeftButtonImage(StoneLook.tabLeftOtherImage());
            setOtherTopLeftButtonAltImage(StoneLook.tabLeftOtherPressedImage());
            setOtherBottomRightButtonImage(StoneLook.tabRightOtherImage());
            setOtherBottomRightButtonAltImage(StoneLook.tabRightOtherPressedImage());
        }
        else {
            setTopLeftButtonImage(StoneLook.tabUpImage());
            setTopLeftButtonAltImage(StoneLook.tabUpPressedImage());
            setBottomRightButtonImage(StoneLook.tabDownImage());
            setBottomRightButtonAltImage(StoneLook.tabDownPressedImage());

            setOtherTopLeftButtonImage(StoneLook.tabUpOtherImage());
            setOtherTopLeftButtonAltImage(StoneLook.tabUpOtherPressedImage());
            setOtherBottomRightButtonImage(StoneLook.tabDownOtherImage());
            setOtherBottomRightButtonAltImage(StoneLook.tabDownOtherPressedImage());
        }       

        myTopLeftButton.setTransparent(true);
        myBottomRightButton.setTransparent(true);
    }
}
