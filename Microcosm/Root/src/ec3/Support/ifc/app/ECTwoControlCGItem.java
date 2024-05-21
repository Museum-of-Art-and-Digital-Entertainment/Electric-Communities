/* =====================================================================
 *    FILE: ECTwoControlCGItem.java
 *    AUTHOR: John Sullivan
 *    CREATED: June 26, 1997
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 * =====================================================================
 */
package ec.ifc.app;

import netscape.application.TextField;
import netscape.application.View;

/**
 * This extension of ECSimpleCGItem adds a second arbitrary View on the right
 * of each item. This second View's size is not adjusted automatically. A
 * typical use of this class is to create a list of labelled controls, each
 * of which also has a check box on the right.
 */
public class ECTwoControlCGItem extends ECSimpleCGItem {
    
    //
    // constants
    //
    protected static final int DEFAULT_SECOND_CONTROL_SPACE = 40;
    
    
    //
    // instance variables
    //
    
    /** the view that's displayed over on the right */
    protected View mySecondControl;
    /** size of the space in which the second control is horiz. centered */
    protected int mySecondControlSpace = DEFAULT_SECOND_CONTROL_SPACE;
    
    //
    // constructors
    //
    /**
     * Returns a new ECTwoControlCGItem with no controls, label field,
     * or label, and a height of 0. You can change the height of an
     * ECControlGroup item before adding it to an ECControlGroup,
     * but not afterwards.
     */
    public ECTwoControlCGItem() {
        this(null, null, null, null, 0);
    }
    
    /**
     * Returns a new ECTwoControlCGItem with the specified controls,
     * label field, label, and height. <b>control</b> is positioned after the
     * label, and resizes automatically. <b>secondControl</b> is positioned
     * on the right edge, and cannot be made to resize automatically.
     * @see ECSimpleCGItem#setControlResizesAutomatically
     */
    public ECTwoControlCGItem(View control,
                              View secondControl,
                              TextField labelField,
                              String label,
                              int height) {
        super(control, labelField, label, height);
        setSecondControl(secondControl);
    }
    
    //
    // public methods
    //
    
    /**
     * Returns the subview that's drawn on the right edge, typically a 
     * checkbox or some other small marker.
     */
    public View getSecondControl() {
        return mySecondControl;
    }
    
    /**
     * Returns the amount of space in which the second control is
     * horizontally centered.
     */
    public int getSecondControlSpace() {
        return mySecondControlSpace;
    }

    /** Overridden to call positionSecondControl() after calling super() */
    public void layoutContents(int labelWidth) {
        super.layoutContents(labelWidth);
        positionSecondControl(labelWidth);
    }
    
    /**
     * Sets the second control subview.
     * @see #getSecondControl
     */
    public void setSecondControl(View newControl) {
        if (mySecondControl != null) {
            mySecondControl.removeFromSuperview();
        }
        
        mySecondControl = newControl;
        
        if (newControl != null) {
            addSubview(mySecondControl);
            // note control is not yet positioned, awaiting next
            // layoutContents call
        }
    }
    
    /**
     * Sets the amount of space in which the second control is
     * horizontally centered.
     * @see #getSecondControlSpace
     */
    public void setSecondControlSpace(int newAmount) {
        if (newAmount == mySecondControlSpace) {
            return;
        }
        
        mySecondControlSpace = newAmount;
        relayoutContents();
    }
    
    //
    // protected methods
    //

    /**
     * Overridden to make automatically-sized control leave room for
     * second control.
     */
    protected void positionControl(int labelWidth) {
        if (myControl != null) {
            int x = labelWidth + myControlHorizMargin;
            int width = myControlResizesAutomatically
                ? width() - mySecondControlSpace - x
                : myControlFixedWidth;
            myControl.setBounds(
                x, myControlVertMargin, width,
                height() - 2*myControlVertMargin);
        }       
    }
    
    /**
     * Positions second control centered in secondControlSpace() at
     * right edge of item.
     */
    protected void positionSecondControl(int labelWidth) {
        if (mySecondControl != null) {
            int controlWidth = mySecondControl.width();
            int x = width() - mySecondControlSpace +
                    (mySecondControlSpace - controlWidth)/2;
            mySecondControl.setBounds(x, myControlVertMargin, controlWidth,
                                      height() - 2*myControlVertMargin);
        }
    }
    
    
}

