/**
 * SLContainerView.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 */
package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Subclass of ContainerView that sets up the stone-texture look.
 */
public class SLContainerView extends ContainerView {

    /**
     * border type to pass to constructor to get a raised
     * container thinner and lighter than what you would
     * get with BezelBorder.RAISED
     */
    public final static int THIN_RAISED = 1001;
    /** border type to pass to constructor to get no border at all */
    public final static int NO_BORDER = 1002;

    /**
     * Creates a new SLContainerView with lowered bezel border and textured
     * background.
     */
    public SLContainerView() {
        this(BezelBorder.LOWERED);
    }
    
    /**
     * Creates a new SLContainerView with an appropriate border. The border type
     * is set by <b>borderType</b> (BezelBorder.LOWERED or BezelBorder.RAISED
     * or SLContainerView.THIN_RAISED).
     */
    public SLContainerView(int borderType) {
        Color backgroundColor = StoneLook.lightBackgroundColor();
        setBackgroundColor(StoneLook.lightBackgroundColor());

        Border border;
        if (borderType == BezelBorder.LOWERED) {
            border = new SLBezelBorder(borderType);
        } else if (borderType == SLContainerView.THIN_RAISED) {
            border = new SLBezelBorder(BezelBorder.RAISED);
            ((SLBezelBorder)border).setHighlightColor(
                StoneLook.selectedTabEdgeColor(true));
            backgroundColor = StoneLook.lightestBackgroundColor();
        } else if (borderType == SLContainerView.NO_BORDER)  {
            border = null;  
        } else {
            border = new SLSlabBorder();
        }
        setBorder(border);
        // do this after setting border, so the border's inside color will change
        setBackgroundColor(backgroundColor);
    }
    
    //
    // public methods
    //
    /**
     * Overridden to fix bug in superclass where background color is filled
     * throughout entire bounds instead of just the border's inner rect.
     */
    public void drawViewBackground(Graphics g) {
        Rect    tmpRect;

        /* draw background color only if color has been set and image doesn't
         * completely fill our bounds
         */
        Image image = image();
        int imageDisplayStyle = imageDisplayStyle();
        Color backgroundColor = backgroundColor();
        
        if (!isTransparent() && (image == null ||
            imageDisplayStyle == Image.CENTERED && backgroundColor != null)) {

            if (image == null) {
                tmpRect = new Rect();
            } else {
                tmpRect = new Rect(0, 0, image.width(), image.height());
            }
            if (!tmpRect.contains(bounds)) {
                Border border = border();
                if (border != null) {
                    tmpRect.setBounds(
                        border.interiorRect(0, 0, width(), height()));
                } else {
                    tmpRect.setBounds(0, 0, width(), height());             
                }

                // removed titleField-moving code here because as a subclass
                // I can't access titleField in any way. Sucks.
                
                g.setColor(backgroundColor);
                g.fillRect(tmpRect);
            }
        }

        if (image != null) {
            image.drawWithStyle(g, 0, 0, width(), height(), imageDisplayStyle);
        }
    }
    
    /** overridden to set the border's inside color as well */
    public void setBackgroundColor(Color newColor) {
        super.setBackgroundColor(newColor);
        Border border = border();
        if (border instanceof SLBezelBorder) {
            ((SLBezelBorder)border).setInsideColor(newColor);
        }
    }
     
}

