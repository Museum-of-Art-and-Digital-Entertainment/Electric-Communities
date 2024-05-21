/**
 * SLWindow.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 *
 *
 * Modification history:
 *
 * 970522   agm     made class public.
 */


package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.*;

/**
 * Subclass of ContainerView that's used to make an SLWindow's background
 * and border have that stone-texture look.  This class needs to be accessed
 * outside of this package because one may want to customize the background
 * color or image.
 */
public class SLWindowContainerView extends ContainerView {
    
    /** Returns a new SLWindowContainerView */
    public SLWindowContainerView(SLWindow window) {
        setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        setBorder(new SLWindowBorder(window));
        setImage(StoneLook.backgroundTexture());
        setImageDisplayStyle(Image.TILED);
        // the texture covers the background color, but we'll set it anyway
        // so if a caller sets the image to null it will have the right color
        setBackgroundColor(StoneLook.darkBackgroundColor());
    }

}
