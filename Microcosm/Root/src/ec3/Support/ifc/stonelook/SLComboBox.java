/**
 * SLComboBox.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 */
package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECComboBox;

/** Subclass of ECComboBox that applies the stone-texture look */
public class SLComboBox extends ECComboBox {

    /** thickness of button */
    private static final int BUTTON_THICKNESS = 19;

    //
    // constructors
    //
    
    /** returns an SLComboBox with bounds 0,0,0,0 */
    public SLComboBox()   {
        this(0,0,0,0);
    }
    
    /** returns an SLComboBox with the specified bounds */  
    public SLComboBox(int x, int y, int width, int height)    {
        super(x,y,width,height);
        updateColors();
    }
    
    //
    // appearance-alerting overrides
    //  
    
    /** Overridden to create an SLBezelBorder */
    protected Border createBorder() {
        return new SLBezelBorder(BezelBorder.LOWERED);
    }

    /**
     * Overridden to create a stone-look-style marble button.
     */ 
    protected Button createDropButton() {
        SLButton button = (SLButton)SLButton.createPushButton(
            0, 0, BUTTON_THICKNESS, BUTTON_THICKNESS);
        button.setImage(StoneLook.scrollDownImage());
        button.setAltImage(StoneLook.scrollDownPressedImage());
        return button;
    }   
    
    /**
     * Overridden to create an SLListView.
     */ 
    protected ListView createListView() {
        SLListView result = new SLListView();
        result.setBackgroundColor(StoneLook.lightBackgroundColor());
        return result;
    }   

    /**
     * Overridden to create an SLScrollGroup.
     */ 
    protected ScrollGroup createScrollGroup() {
        SLScrollGroup result = new SLScrollGroup();
        result.setBackgroundColor(StoneLook.lightBackgroundColor());
        return result;     
    }   

    /**
     * Overridden to create a borderless SLTextField.
     */ 
    protected TextField createTextField() {
        TextField textField = new SLTextField();        
        textField.setBorder(null);
        return textField;
    }
    
    /** Overridden to fill background with editable text color */
    public void drawView(Graphics g) {
        Border border = border();
        Rect bounds = bounds();
        Rect innerBounds;
        if (border != null) {
            innerBounds = new Rect();
            border.computeInteriorRect(
                0, 0, bounds.width, bounds.height, innerBounds);
        } else {
            innerBounds = bounds;
        }
        g.setColor(myTextField.backgroundColor());
        g.fillRect(innerBounds);
        super.drawView(g);
    }
    
    /** Overridden to adjust button appearance */
    public void listChanged() {
        updateButtonAppearance();
    }
    
    /** Overridden to adjust colors */
    public void setEnabled(boolean newValue) {
        super.setEnabled(newValue);
        updateColors();
    }
    
    //
    // private methods
    //
    
    private void updateButtonAppearance() {
        boolean buttonEnabled = isEnabled() && (count() > 0);
    
        myDropButton.setEnabled(buttonEnabled);
        if (buttonEnabled) {
            myDropButton.setImage(StoneLook.scrollDownImage());
            myDropButton.setAltImage(StoneLook.scrollDownPressedImage());
        } else {
            myDropButton.setImage(StoneLook.scrollDownDisabledImage());
            myDropButton.setAltImage(StoneLook.scrollDownDisabledImage());
        }
    
        myDropButton.setDirty(true);
    }
    
    
    private void updateColors() {
        Color fillColor;
        Color borderColor;
        Color textColor;
        
        // text labels (transparent) use editable text colors
        if (isEnabled()) {
            fillColor = StoneLook.editableTextFillColor();
            borderColor = StoneLook.editableBorderColor();
            textColor = StoneLook.editableTextColor();
        } else {
            fillColor = StoneLook.disabledEditableTextFillColor();
            borderColor = StoneLook.disabledBorderColor();
            textColor = StoneLook.disabledEditableTextColor();
        }
        myTextField.setBackgroundColor(fillColor);
        myTextField.setTextColor(textColor);
        Border border = border();
        if (border instanceof SLBezelBorder) {
            ((SLBezelBorder)border).setLineColor(borderColor);
            ((SLBezelBorder)border).setInsideColor(fillColor);
        }
        
        updateButtonAppearance();
    
        setDirty(true);
    }
}
