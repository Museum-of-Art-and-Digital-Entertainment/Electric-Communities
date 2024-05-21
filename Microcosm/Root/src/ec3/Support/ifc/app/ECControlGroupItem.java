/* =====================================================================
 *    FILE: ECControlGroupItem.java
 *    AUTHOR: John Sullivan
 *    CREATED: June 25, 1997
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 * =====================================================================
 */
package ec.ifc.app;

import netscape.application.ContainerView;

/**
 * One item in a set managed by an ECControlGroup. This is the abstract
 * superclass that adds some callbacks used by ECControlGroup to 
 * ContainerView. You will probably most often instantiate the subclass
 * ECSimpleControlGroupItem, but you might also create your own subclasses.
 * @see ECControlGroup
 * @see ECSimpleControlGroupItem
 */
public abstract class ECControlGroupItem extends ContainerView {
    
    //
    // public methods
    //

    /**
     * Returns the text label that is optionally shown as part of this item.
     * Subclasses should override to read this string from wherever it is
     * stored (perhaps in a text field)
     */
    public abstract String getLabel();
    
    /**
     * Called by ECControlGroup when the width or other conditions of this
     * item change in such a way that this item needs to be laid out again.
     * <b>labelWidth</b> specifies how much horizontal space should be
     * reserved for the label (if any). Subclasses should override to
     * reposition subviews appropriately.
     */
    public abstract void layoutContents(int labelWidth);
    
    /**
     * Sets the text label that is optionally shown as part of this item.
     * Subclasses should override to store this string somwhere (perhaps in
     * a text field)
     */
    public abstract void setLabel(String newLabel);
}

