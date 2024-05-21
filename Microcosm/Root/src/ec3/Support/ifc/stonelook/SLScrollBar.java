package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Subclass of ScrollBar that applies the stone-texture look.
 * SLScrollGroup uses these by default, so if you're using an SLScrollGroup
 * you don't need to worry about modifying the appearance further.
 * @see SLScrollGroup
 */
public class SLScrollBar extends ScrollBar {

    /** thickness of button, which determines thickness of scroll bar */
    private static final int BUTTON_THICKNESS = 19;
    /** space between buttons and scroll tray */
    private static final int BUTTON_MARGIN = 2;
    
    //
    // Constructors
    //

    /** Creates a new SLScrollBar with the stone-texture look. */
    public SLScrollBar(int x, int y, int width, int height, int orientation) {
        super(x, y, width, height, orientation);

        // make new SLButtons to be sized later
        Button increaseButton = SLButton.createPushButton(0, 0, 0);
        increaseButton.setType(Button.CONTINUOUS_TYPE);
        Button decreaseButton = SLButton.createPushButton(0, 0, 0);
        decreaseButton.setType(Button.CONTINUOUS_TYPE);
        
        // replace buttons with our new ones
        setIncreaseButton(increaseButton);
        setDecreaseButton(decreaseButton);
        
        increaseButton.sizeTo(BUTTON_THICKNESS, BUTTON_THICKNESS);
        decreaseButton.sizeTo(BUTTON_THICKNESS, BUTTON_THICKNESS);

        // size scroll bar based on button sizes
        adjustToFit();

        // always add the parts in (superclass wouldn't call this
        // if the scroll bar was initially disabled). This must be
        // called after adjustToFit() or things look weird.
        addParts();
    }
    
    //
    // static methods
    //

    /**
     * Draws a scroll bar knob. It's static so it can be accessed
     * by other classes such as SLSlider.
     */
    public static void drawKnobInRect(Graphics g, Rect rect, boolean horizontal) {
        // shrink rect so it appears inside track
        rect.growBy(-1, -1);

        // draw button border around edges
        SLButtonBorder.drawBorder(g, rect.x, rect.y, rect.width, rect.height);
        int borderWidth = SLButtonBorder.borderWidth();
        
        // fill inside of border with color
        g.setColor(StoneLook.scrollKnobColor());
        g.fillRect(rect.x + borderWidth, rect.y + borderWidth,
                   rect.width - 2*borderWidth, rect.height - 2*borderWidth);
        
        // draw image in center of rect, unless it doesn't fit
        Bitmap knobImage = StoneLook.scrollKnobImage(horizontal);
        
        int innerWidth = rect.width - 2*borderWidth;
        int innerHeight = rect.height - 2*borderWidth;
        if (!imageFits(knobImage, innerWidth, innerHeight)) {
            knobImage = StoneLook.smallScrollKnobImage(horizontal);
            if (!imageFits(knobImage, innerWidth, innerHeight)) {
                knobImage = null;
            }
        }
        
        if (knobImage != null) {
            g.drawBitmapAt(knobImage,
                           rect.x + (rect.width - knobImage.width())/2,
                           rect.y + (rect.height - knobImage.height())/2);          
        }
    }   

    //
    // public methods
    //

    /**
     * Overridden to position buttons properly (ScrollBar insets them by
     * one pixel from each edge).
     */
    public void addParts() {
        // unlike our superclass, we leave the buttons in when disabled,
        // but we draw them differently.
        updateButtonsState();
    
        Button decreaseButton = decreaseButton();
        if (decreaseButton != null && !decreaseButton.isInViewHierarchy()) {
            decreaseButton().moveTo(0, 0);
            addSubview(decreaseButton);         
        }

        Button increaseButton = increaseButton();
        if (increaseButton != null && !increaseButton.isInViewHierarchy()) {    
            if (axis() == Scrollable.HORIZONTAL) {
                increaseButton.moveTo(width() - increaseButton.width(), 0);
            } else {
                increaseButton.moveTo(0, height() - increaseButton.height());
            }
            
            addSubview(increaseButton);
        }
    }

    /** Overridden to draw the guts of the scroll bar differently. */
    public void drawView(Graphics g) {
        // Draw the disabled look if necessary
        if (!isEnabled() || !isActive()) {
            drawDisabled(g);
            return;
        }

        Rect areaRect = scrollTrayRect();

        // frame scroll track
        SLBezelBorder.drawBezel(g, areaRect.x, areaRect.y,
                                areaRect.width, areaRect.height,
                                false,
                                StoneLook.scrollBarBorderLineColor(),
                                StoneLook.scrollBarBorderInsideColor(),
                                StoneLook.scrollBarBorderHighlightColor());
        
        // fill scroll track
        Image trackBackground = StoneLook.scrollTrackTexture(
            axis() == Scrollable.HORIZONTAL);
        int borderThickness = StoneLook.bezelThickness();
        areaRect.growBy(-borderThickness, -borderThickness);
        trackBackground.drawTiled(g, areaRect);

        // draw the knob
        drawViewKnobInRect(g, knobRect());
    }
    
    /** Overridden to draw the knob differently. */
    public void drawViewKnobInRect(Graphics g, Rect rect) {
        drawKnobInRect(g, rect, axis() == Scrollable.HORIZONTAL);
    }
    
    /**
     * overridden to return true, since there are transparent areas
     * between the arrows and main body.
     */
    public boolean isTransparent() {
        return true;
    }

    /** Overridden to not assume a border around the buttons. */
    public Size minSize() {
        int     width, height;

        if (increaseButton() != null) {
            width = increaseButton().bounds.width;
            height = increaseButton().bounds.height;
        } else {
            width = height = 0;
        }

        if (decreaseButton() != null) {
            if (axis() == Scrollable.HORIZONTAL) {
                width += decreaseButton().bounds.width;
                if (height < decreaseButton().bounds.height) {
                    height = decreaseButton().bounds.height;
                }
            } else {
                if (width < decreaseButton().bounds.width) {
                    width = decreaseButton().bounds.width;
                }
                height += decreaseButton().bounds.height;
            }
        }

        return new Size(width, height);
    }

    /**
     * Overridden to disable buttons instead of removing them.
     */
    public void removeParts() {
        // unlike our superclass, we leave the buttons in when disabled,
        // but we draw them differently.
        updateButtonsState();
    }

    /** Overridden to leave space between buttons and track */
    public Rect scrollTrayRect() {
        Rect result = super.scrollTrayRect();
        // first eliminate the one-pixel border added by ScrollBar
        result.growBy(1,1);
        
        if (axis() == Scrollable.HORIZONTAL) {
            result.growBy(-BUTTON_MARGIN, 0);
        } else {
            result.growBy(0, -BUTTON_MARGIN);
        }
        
        
        return result;
    }
    
    //
    // private methods
    //

    /** Returns whether or not given image fits in given space */   
    private static boolean imageFits(Image image,
                                     int maxWidth, int maxHeight) {
        return image.width() <= maxWidth && image.height() <= maxHeight;
        
    }

    /** Makes sure a vertical ScrollBar is wide enough and a horizontal
     * ScrollBar is tall enough for its buttons.
     */
    private void adjustToFit() {
        if (axis() == Scrollable.HORIZONTAL) {
            sizeTo(bounds.width, minSize().height);
        } else {
            sizeTo(minSize().width, bounds.height);
        }
    }
    
    /** Draws a disabled scroll bar */
    private void drawDisabled(Graphics g) {
        // fill body with disabled texture
        Rect trayRect = scrollTrayRect();
        Color color = StoneLook.disabledScrollBarColor();
        SLBezelBorder.drawBezel(g, trayRect.x, trayRect.y, trayRect.width,
                                trayRect.height, true, color, color, null);   
        int bezelThickness = StoneLook.bezelThickness();
        trayRect.growBy(-bezelThickness, -bezelThickness);
        g.fillRect(trayRect);
    }    

    /** updates enabled-ness and image of buttons */    
    private void updateButtonsState() {
        boolean looksDisabled = !isEnabled() || !isActive();
        boolean isHorizontal = axis() == Scrollable.HORIZONTAL;
        
        Button decreaseButton = decreaseButton();
        if (decreaseButton != null) {
            decreaseButton.setEnabled(!looksDisabled);              
            Image decreaseImage;
            Image altDecreaseImage;
            if (isHorizontal) {
                if (looksDisabled) {
                    decreaseImage = StoneLook.scrollLeftDisabledImage();
                    altDecreaseImage = decreaseImage;
                } else {
                    decreaseImage = StoneLook.scrollLeftImage();
                    altDecreaseImage = StoneLook.scrollLeftPressedImage();
                }
            } else {
                if (looksDisabled) {
                    decreaseImage = StoneLook.scrollUpDisabledImage();
                    altDecreaseImage = decreaseImage;
                } else {
                    decreaseImage = StoneLook.scrollUpImage();
                    altDecreaseImage = StoneLook.scrollUpPressedImage();
                }
            }
            decreaseButton.setImage(decreaseImage);
            decreaseButton.setAltImage(altDecreaseImage);
        }
        
        Button increaseButton = increaseButton();
        if (increaseButton != null)  {
            increaseButton.setEnabled(!looksDisabled);
            Image increaseImage;
            Image altIncreaseImage;
            if (isHorizontal) {
                if (looksDisabled) {
                    increaseImage = StoneLook.scrollRightDisabledImage();
                    altIncreaseImage = increaseImage;
                } else {
                    increaseImage = StoneLook.scrollRightImage();
                    altIncreaseImage = StoneLook.scrollRightPressedImage();
                }
            } else {
                if (looksDisabled) {
                    increaseImage = StoneLook.scrollDownDisabledImage();
                    altIncreaseImage = increaseImage;
                } else {
                    increaseImage = StoneLook.scrollDownImage();
                    altIncreaseImage = StoneLook.scrollDownPressedImage();
                }
            }
            increaseButton.setImage(increaseImage);
            increaseButton.setAltImage(altIncreaseImage);
        }
        
    }
}

