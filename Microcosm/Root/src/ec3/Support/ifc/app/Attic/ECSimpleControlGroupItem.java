/* =====================================================================
 *    FILE: ECControlGroupItem.java
 *    AUTHOR: John Sullivan
 *    CREATED: June 25, 1997
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 * =====================================================================
 */
package ec.ifc.app;

import netscape.application.ContainerView;
import netscape.application.Graphics;
import netscape.application.TextField;
import netscape.application.View;

/**
 * This is the simplest ECControlGroupItem subclass. It combines
 * a textfield displaying the label with one other subview, called the
 * 'control'. The control is typically a textfield or popup menu, but
 * can be any View. The width of the control can be automatically sized
 * or be set to a fixed size. The height of the label field and control
 * are determined from the height of the ECSimpleControlGroupItem and
 * the adjustable contentsMargin(). <P>
 * You can subclass from ECSimpleControlGroupItem to add additional subviews.
 */
public class ECSimpleControlGroupItem extends ECControlGroupItem {
    
    //
    // constants
    //
    
    protected static final int DEFAULT_CONTROL_HORIZ_MARGIN = 8;
    protected static final int DEFAULT_CONTROL_VERT_MARGIN = 4;
    protected static final int DEFAULT_CONTROL_FIXED_WIDTH = 50;
    
    //
    // instance variables
    //
    
    /** the text field displaying the label */
    protected TextField myLabelField;
    /** the subview that the label is a label for */
    protected View myControl;
    /** whether or not the control's width is determined automatically */
    protected boolean myControlResizesAutomatically;
    /** the width of the control if it's fixed */
    protected int myControlFixedWidth = DEFAULT_CONTROL_FIXED_WIDTH;
    /** the space between the right edge of the label and the control */
    protected int myControlHorizMargin = DEFAULT_CONTROL_HORIZ_MARGIN;  
    /** the space above and below the control */
    protected int myControlVertMargin = DEFAULT_CONTROL_VERT_MARGIN;
    /** the label width we were most recently told to adjust to */
    protected int lastLabelWidth = 0;
    
    //
    // constructors
    //
    /**
     * Returns a new ECSimpleControlGroupItem with no control, label field,
     * or label, and a height of 0. You can change the height of an
     * ECControlGroup item before adding it to an ECControlGroup,
     * but not afterwards.
     */
    public ECSimpleControlGroupItem() {
        this(null, null, null, 0);
    }
    
    /**
     * Returns a new ECSimpleControlGroupItem with the specified control,
     * label field, label, and height. The control resizes automatically.
     * @see #setControlResizesAutomatically
     */
    public ECSimpleControlGroupItem(View control,
                                    TextField labelField,
                                    String label,
                                    int height) {
        setBorder(null);
        setTransparent(true);
        myControlResizesAutomatically = true;

        setControl(control);
        setLabelField(labelField);
        setLabel(label);
        sizeTo(0, height);
    }
    
    //
    // public methods
    //
    
    /**
     * Returns whether positionControl() determines the width of the control
     * automatically. If this is false, positionControl() uses the result
     * of getControlFixedWidth().
     */
    public boolean controlResizesAutomatically() {
        return myControlResizesAutomatically;
    }
    
    /**
     * Returns the subview that the label is a label for, typically a text
     * field, popup menu, or other such simple control.
     */
    public View getControl() {
        return myControl;
    }

    /**
     * Returns the control's fixed with, which is used by positionControl()
     * when controlResizesAutomatically returns false.
     * @see #controlResizesAutomatically
     */
    public int getControlFixedWidth() {
        return myControlFixedWidth;
    }

    /**
     * Returns the amount of space that layoutContents() provides between
     * the right edge of the label and the left edge of the control
     */
    public int getControlHorizMargin() {
        return myControlHorizMargin;
    }
    
    /**
     * Returns the amount of space that layoutContents() provides above
     * and below the control.
     */
    public int getControlVertMargin() {
        return myControlVertMargin;
    }
    
    /**
     * Returns the text label that is optionally shown as part of this item.
     * Subclasses should override to read this string from wherever it is
     * stored (perhaps in a text field)
     */
    public String getLabel() {
        return getLabelField().stringValue();
    }
    
    /**
     * Returns the text field displaying the label. Creates a new one
     * if there is none currently.
     */
    public TextField getLabelField() {
        if (myLabelField == null) {
            myLabelField = TextField.createLabel("");
            addSubview(myLabelField);
            // note label field is not yet positioned, awaiting next
            // layoutContents call
        }
        return myLabelField;
    }

    /**
     * Called by ECControlGroup when the width or other conditions of this
     * item change in such a way that this item needs to be laid out again.
     * <b>labelWidth</b> specifies how much horizontal space should be
     * reserved for the label (if any). Subclasses should override after
     * calling super() to reposition additional subviews appropriately.
     * To change the positioning of the label field or control, subclasses
     * should override positionLabelField and/or positionControl instead.
     */
    public void layoutContents(int labelWidth) {
        // remember this for relayoutContents' sake
        lastLabelWidth = labelWidth;
        
        positionLabelField(labelWidth);
        positionControl(labelWidth);
    }
    
    /**
     * Positions the subviews appropriately, using most recent label width
     * that was passed to layoutContents. Call when the contents of this
     * item have changed (not when the control group as a whole has changed)
     */
    public void relayoutContents() {
        layoutContents(lastLabelWidth);
    }
    
    /**
     * Sets the control subview.
     * @see #getControl
     */
    public void setControl(View newControl) {
        if (myControl != null) {
            myControl.removeFromSuperview();
        }
        
        myControl = newControl;
        
        if (newControl != null) {
            addSubview(myControl);
            // note control is not yet positioned, awaiting next
            // layoutContents call
        }
    }
    
    /**
     * Sets the control's fixed width
     * @see #getControlFixedWidth
     */
    public void setControlFixedWidth(int newValue) {
        if (newValue == myControlFixedWidth) {
            return;
        }
        myControlFixedWidth = newValue;
        if (!myControlResizesAutomatically) {
            relayoutContents();
        }
    }
    
    
    /**
     * Sets the control horizontal margin, and repositions the control.
     * Values less than zero are converted to zero.
     * @see #getControlHorizMargin
     */
    public void setControlHorizMargin(int newMargin) {
        if (newMargin < 0) {
            newMargin = 0;
        }
        
        if (newMargin == myControlHorizMargin) {
            return;
        }
        
        myControlHorizMargin = newMargin;
        relayoutContents();
    }
    
    /**
     * Sets whether the control resizes automatically.
     * @see #controlResizesAutomatically
     */
    public void setControlResizesAutomatically(boolean newValue) {
        if (newValue == myControlResizesAutomatically) {
            return;
        }
        myControlResizesAutomatically = newValue;
        relayoutContents();
    }
    
    /**
     * Sets the control vertical margin, and repositions the control.
     * Values less than zero are converted to zero.
     * @see #getControlVertMargin
     */
    public void setControlVertMargin(int newMargin) {
        if (newMargin < 0) {
            newMargin = 0;
        }
        
        if (newMargin == myControlVertMargin) {
            return;
        }
        
        myControlVertMargin = newMargin;
        relayoutContents();
    }
    
    /**
     * Sets the text label that is optionally shown as part of this item.
     * Subclasses should override to store this string somwhere (perhaps in
     * a text field)
     */
    public void setLabel(String newLabel) {
        getLabelField().setStringValue(newLabel);
    }

    /**
     * Sets the text field displaying the label. Call this if you want
     * the text field to be a TextField subclass.
     */
    public void setLabelField(TextField newField) {
        if (myLabelField != null) {
            myLabelField.removeFromSuperview();
            newField.setStringValue(myLabelField.stringValue());
        }
        
        myLabelField = newField;
        
        if (myLabelField != null) {
            addSubview(myLabelField);
            // note label field is not yet positioned, awaiting next
            // layoutContents call          
        }
    }
    
    //
    // protected methods
    //

    /**
     * Sets the position of the control based on the specified label
     * width. Called from layoutContents. Override if you want to position
     * the control differently.
     */
    protected void positionControl(int labelWidth) {
        if (myControl != null) {
            int x = labelWidth + myControlHorizMargin;
            int width = myControlResizesAutomatically
                ? width() - myControlHorizMargin - x
                : myControlFixedWidth;
            myControl.setBounds(
                x, myControlVertMargin, width,
                height() - 2*myControlVertMargin);
        }       
    }
    
    /**
     * Sets the position of the label field based on the specified label
     * width, and sets the justification to Graphics.RIGHT_JUSTIFIED.
     * Called from layoutContents. Override if you want to position
     * the label field differently, or use a different justification.
     */
    protected void positionLabelField(int labelWidth) {
        if (myLabelField != null) {
            myLabelField.setJustification(Graphics.RIGHT_JUSTIFIED);
            myLabelField.setBounds(0, 0, labelWidth, height());
        }       
    }
    
    
}

