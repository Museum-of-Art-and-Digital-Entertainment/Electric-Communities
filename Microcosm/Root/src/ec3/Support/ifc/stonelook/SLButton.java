package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.*;

/**
 * Subclass of Button that applies the stone-texture look.
 */
public class SLButton extends ECButton {

    /** extra distance to push title away from image; default is too close */
    protected final static int EXTRA_TITLE_MARGIN = 3;
    
    /** margin between border and default-button ring */
    protected final static int DEFAULT_BORDER_INSET = 0;

    /**
     * Storage for extra images used in disabled state. These
     * are used for disabled checkboxes and radio buttons.
     */
    Image myDisabledImage;
    Image myDisabledAltImage;
    
    //
    // constructors
    //

    /**
     * Returns a new SLButton with the specified bounds. This is a package
     * method only. Other callers should use createCheckButton,
     * createPushButton, etc.
     */
    SLButton(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
    }   
    
    //
    // public static methods
    //

/**
 * Returns an unchecked non-exclusive check button with the stone-texture look.
 * Parameters specify the rectangle in which the check button is drawn, left-aligned
 * and vertically centered.
 */
    public static Button createCheckButton(int x, int y, int width, int height) {
        SLButton checkButton = new SLButton(x, y, width, height);
        checkButton.setType(Button.TOGGLE_TYPE);
        checkButton.setTransparent(true);

        checkButton.setImage(StoneLook.checkboxOff(true));        
        checkButton.setAltImage(StoneLook.checkboxOn(true));       
        checkButton.setDisabledImage(StoneLook.checkboxOff(false));        
        checkButton.setDisabledAltImage(StoneLook.checkboxOn(false));       
        checkButton.setImagePosition(Button.IMAGE_ON_LEFT);
        checkButton.setFont(standardFont());

        // set appropriate colors for title and disabled title
        checkButton.updateTitleColors();

        return checkButton;
    }

/**
 * Returns a push button with the stone-texture look. Parameters specify the
 * top-left corner and width of the button. The height of the button is a constant
 * determined by the height at which the special border looks good.
 */
    public static SLButton createPushButton(int x, int y, int width) {
        // most SLPushButtons are the same height, so they look right with the art
        // used in SLButtonBorder
        return (SLButton)createPushButton(x, y, width, standardPushButtonHeight());
    }

    /**
     * Returns a push button with the stone-texture look, with specified bounds.
     * Use this if you are specifying a non-standard height for the button.
     * Otherwise, use the version of this call that does not specify a height.
     */
     public static SLButton createPushButton(Rect r) {
         return (SLButton)createPushButton(r.x, r.y, r.width, r.height);
     }

    /**
     * Returns a push button with the stone-texture look, with specified bounds.
     * Use this if you are specifying a non-standard height for the button.
     * Otherwise, use the version of this call that does not specify a height.
     */
     public static Button createPushButton(int x, int y, int width, int height) {

        SLButton pushButton = new SLButton(x, y, width, height);
        pushButton.setType(Button.PUSH_TYPE);
        pushButton.setFont(standardFont());

        Border b = new SLButtonBorder(pushButton);

        pushButton.setRaisedBorder(b);
        pushButton.setLoweredBorder(b);
        
        pushButton.setRaisedColor(StoneLook.raisedButtonColor());
        pushButton.setLoweredColor(StoneLook.loweredButtonColor());

        // set appropriate colors for title and disabled title
        pushButton.updateTitleColors();

        return pushButton;
     }

/**
 * Returns an unchecked exclusive radio button with the stone-texture look.
 * Parameters specify the rectangle in which the radio button is drawn, left-aligned
 * and vertically centered.
 */
    public static Button createRadioButton(int x, int y, int width, int height) {
        SLButton radioButton = new SLButton(x, y, width, height);
        radioButton.setType(Button.RADIO_TYPE);
        radioButton.setTransparent(true);
                
        radioButton.setImage(StoneLook.radioButtonOff(true));     
        radioButton.setAltImage(StoneLook.radioButtonOn(true));        
        radioButton.setDisabledImage(StoneLook.radioButtonOff(false));     
        radioButton.setDisabledAltImage(StoneLook.radioButtonOn(false));        
        radioButton.setImagePosition(Button.IMAGE_ON_LEFT);
        radioButton.setFont(standardFont());
        
        // set appropriate colors for title and disabled title
        radioButton.updateTitleColors();
        
        return radioButton;
    }
    
    /** Returns the font used by default for new SLButtons. */
    public static Font standardFont() {
        return StoneLook.standardFontBold();
    }

    /**
     * Returns the height at which push buttons are created. This
     * height is chosen to make the standard border look right.
     * @see #createPushButton
     */
    public static int standardPushButtonHeight() {
        return SLButtonBorder.BUTTON_HEIGHT;
    }

    //
    // public instance methods
    //  
    
    /**
     * Overridden to draw the image at the upper-left corner instead of
     * centered on the left side if imagePosition() == IMAGE_ON_LEFT
     * and the button isn't bordered and the title is multi-line. This
     * makes a check box or radio button image line up with the first
     * line of the text instead of the center, as per Ellen. Also draws
     * the default button marker.
     */
    public void drawViewInterior(Graphics g, String title, Image image,
                                 Rect interiorRect) {
                                 
        if (imagePosition() != IMAGE_ON_LEFT
                || image == null
                || isBordered()
                || title == null
                || title.indexOf('\n') < 0) {
            // in all other cases, just do the normal thing
            super.drawViewInterior(g, title, image, interiorRect);
        } else {
            // draw image at upper-left instead of the centered-left
            // that the inherited method would have done   
            image.drawAt(g, interiorRect.x + 1, interiorRect.y + 1);

            // Put some distance between the image and the text
            // Note that the hardwired numbers here are straight from the superclass
            Size imageAreaSize = imageAreaSize();
            interiorRect.moveBy(imageAreaSize.width + 3, 0);
            interiorRect.sizeBy(-(imageAreaSize.width + 4), 0);

            drawViewTitleInRect(g, title, font(), interiorRect,
                                Graphics.LEFT_JUSTIFIED);
        }
        
        // draw the default button indicator
        if (isDefault() && isBordered() && isEnabled()) {
            // for some arcane reason, interiorRect is not exactly the
            // interior of the border here, so we'll compute it
            raisedBorder().computeInteriorRect(
                0, 0, width(), height(), interiorRect);
            
            interiorRect.growBy(-DEFAULT_BORDER_INSET, -DEFAULT_BORDER_INSET);
            // if button is pressed, offset border down and to the right
            if (state()) {
                interiorRect.moveBy(1, 1);
            }
            g.setColor(StoneLook.defaultButtonIndicatorColor());
            g.drawRoundedRect(interiorRect,
                              SLBezelBorder.ROUNDED_CORNER_DIAMETER,
                              SLBezelBorder.ROUNDED_CORNER_DIAMETER);
            
        }
    }

    /** Overridden to increase the margin between image and text. */
    public void drawViewTitleInRect(Graphics g, String title,
                                    Font titleFont, Rect textBounds,
                                    int justification) {

        // push the title a little more away from the image
        if (justification == Graphics.LEFT_JUSTIFIED && image() != null) {
            textBounds.x += EXTRA_TITLE_MARGIN;
            textBounds.width -= EXTRA_TITLE_MARGIN;
        }
        super.drawViewTitleInRect(g, title, titleFont, textBounds, justification);
    }

    /**
     * Returns the image used for this button when it's disabled and
     * its state is false (e.g., a check box that's off)
     */
    public Image disabledImage() {
        return myDisabledImage;
    }

    /**
     * Returns the image used for this button when it's disabled and
     * its state is true (e.g., a check box that's on)
     */
    public Image disabledAltImage() {
        return myDisabledAltImage;
    }

    /**
     * Returns whether this button should be drawn with the default-button
     * look (i.e., the button in a window that responds to Enter or Return).
     */
     public boolean isDefault() {
        RootView rootView = rootView();
        if (!(rootView instanceof ECRootView))
            return false;
            
        return ((ECRootView)rootView).defaultButton() == this;
    }
    
    /**
     * Sets the image used for this button when it's disabled and
     * its state is false (e.g., a check box that's off)
     */
    public void setDisabledImage(Image newImage) {
        myDisabledImage = newImage;
    }

    /**
     * Sets the image used for this button when it's disabled and
     * its state is true (e.g., a check box that's on)
     */
    public void setDisabledAltImage(Image newImage) {
        myDisabledAltImage = newImage;
    }   

    /** Overridden to swap images with disabled images if they aren't null */
    public void setEnabled(boolean newValue) {
        if (newValue == isEnabled()) {
            return;
        }
        
        super.setEnabled(newValue);
        
        // swap images and disabled images
        if (myDisabledImage != null) {
            Image swap = image();
            setImage(myDisabledImage);
            myDisabledImage = swap;
        }
        if (myDisabledAltImage != null) {
            Image swap = altImage();
            setAltImage(myDisabledAltImage);
            myDisabledAltImage = swap;
        }
        
        // set color for bordered buttons
        if (isBordered()) {
            if (newValue) {
                setRaisedColor(StoneLook.raisedButtonColor());
                setLoweredColor(StoneLook.loweredButtonColor());
            } else {
                setRaisedColor(StoneLook.disabledButtonColor());
                setLoweredColor(StoneLook.disabledButtonColor());
            }
        }
    }

    //
    // private methods
    //
    
    /**
     * utility to set up title color and disabled title color
     * based on whether button has a border.
     */
    private void updateTitleColors() {
        if (isBordered()) {
            setTitleColor(StoneLook.activeButtonTitleColor());
            setDisabledTitleColor(StoneLook.disabledButtonTitleColor());
        } else {
            setTitleColor(StoneLook.borderlessButtonTitleColor());
            setDisabledTitleColor(StoneLook.disabledStaticTextColor());
        }
    }
}
