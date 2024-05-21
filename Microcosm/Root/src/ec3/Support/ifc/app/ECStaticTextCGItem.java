/* =====================================================================
 *    FILE: ECStaticTextCGItem.java
 *    AUTHOR: John Sullivan
 *    CREATED: June 26, 1997
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 * =====================================================================
 */
package ec.ifc.app;

import netscape.application.ContainerView;
import netscape.application.Font;
import netscape.application.Graphics;
import netscape.application.TextField;
import netscape.application.View;

/**
 * This is a specialized ECControlGroupItem subclass that just displays a
 * static text string. The justification and margin of the text field
 * containing the string can be specified.
 */
public class ECStaticTextCGItem extends ECControlGroupItem {
    
    //
    // constants
    //
    
    protected static final int DEFAULT_MARGIN = 4;
    
    //
    // instance variables
    //
    
    /** the text field displaying the label */
    protected TextField myTextField;
    /** the margin at left and right of the text field that we won't overrun */
    protected int myTextMargin = DEFAULT_MARGIN;
    
    
    //
    // constructors
    //
    /**
     * Returns a new ECStaticTextCGItem with the given string,
     * left justification, a default font, and a height of 0.
     * You can change the height of an ECControlGroupItem before
     * adding it to an ECControlGroup, but not afterwards.
     */
    public ECStaticTextCGItem(String text) {
        this(text, Graphics.LEFT_JUSTIFIED, null, 0);
    }
    
    /**
     * Returns a new ECStaticTextCGItem with the given string,
     * justification, font, and height. The justification must be one of
     * Graphics.LEFT_JUSTIFIED, Graphics.RIGHT_JUSTIFIED, or
     * Graphics.CENTERED
     */
    public ECStaticTextCGItem(String text,
                              int justification,
                              Font font,
                              int height) {
        setBorder(null);
        setTransparent(true);
        myTextField = TextField.createLabel(text);
        if (font != null) {
            myTextField.setFont(font);
        }
        myTextField.setJustification(justification);
        addSubview(myTextField);
        sizeTo(0, height);
    }
    
    //
    // public methods
    //
    
    /** Returns the font used to draw the displayed text */
    public Font getFont() {
        return myTextField.font();
    }
    
    /**
     * Returns the justification used for the displayed text. This is one
     * of Graphics.LEFT_JUSTIFIED, Graphics.RIGHT_JUSTIFIED, or
     * Graphics.CENTERED
     */
    public int getJustification() {
        return myTextField.justification();
    }

    /** 
     * Overridden to return null since this item doesn't have a separate label
     */
    public String getLabel() {
        return null;
    }
    
    /** The space to the left and right of the displayed text */
    public int getTextMargin() {
        return myTextMargin;
    }

    /** Overridden to reposition the text by calling positionText. */
    public void layoutContents(int labelWidth) {
        resetTextBounds();
    }
    
    /** Sets the font used to draw the displayed text */
    public void setFont(Font newFont) {
        myTextField.setFont(newFont);
    }
    
    /**
     * Sets the justification used for the displayed text.
     * @see #getJustification
     */
    public void setJustification(int newJustification) {
        myTextField.setJustification(newJustification);
    }
    
    /**
     * Sets the text label that is optionally shown as part of this item.
     * Does nothing here, because ECStaticTextCGItems don't have separate
     * labels.
     */
    public void setLabel(String newLabel) {
    }
    
    /**
     * Sets the text margin.
     * @see #getTextMargin
     */
    public void setTextMargin(int newMargin) {
        if (newMargin == myTextMargin) {
            return;
        }
        
        myTextMargin = newMargin;
        resetTextBounds();
    }
    
    //
    // private methods
    //
    /** sets the bounds of the text field based on margins */
    private void resetTextBounds() {
        myTextField.setBounds(myTextMargin, 0,
                              width() - 2*myTextMargin, height());
        
    }
}

