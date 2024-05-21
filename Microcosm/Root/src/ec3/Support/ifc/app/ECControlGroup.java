/* =====================================================================
 *    FILE: ECControlGroup.java
 *    AUTHOR: John Sullivan
 *    CREATED: June 25, 1997
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 * =====================================================================
 */

package ec.ifc.app;

import java.util.Vector;
import netscape.application.Color;
import netscape.application.ContainerView;
import netscape.application.LayoutManager;
import netscape.application.Rect;
import netscape.application.ScrollGroup;
import netscape.application.View;

import ec.e.run.Trace;

/**
 * ECControlGroup manages a set of views of arbitrary complexity
 * that need to be grouped together and possibly scrolled vertically.
 * The items in the group are themselves ECControlGroupItems.
 * Each ECControlGroupItem can have a text label, and the ECControlGroup
 * helps manage aligning the labels across the set of items.<P>
 *
 * A typical use is for a scrolling list of items where each item has
 * a text label and a text field, popup menu, or other such control.
 * @see ECControlGroupItem
 */
public class ECControlGroup implements LayoutManager {

    //
    // constants
    //
    protected final static int DEFAULT_LABEL_WIDTH = 80;

    //
    // state
    //  
    /** The view that displays the scrolling set of items */
    protected ScrollGroup myScrollGroup;
    /** The container view upon which control items are placed */
    protected ContainerView myContainer;
    /** The set of ECControlGroupItems to be displayed */
    protected Vector /* ECControlGroupItem */ myItems;
    /** Should the items' text labels be shown? */
    protected boolean myShowLabels;
    /** width of label area */
    protected int myLabelWidth = DEFAULT_LABEL_WIDTH;
    
    //
    // constructors
    //
    /** Returns a new empty ECControlGroup */
    public ECControlGroup(ScrollGroup scrollGroup) {
        myScrollGroup = scrollGroup;
        myScrollGroup.setHasVertScrollBar(true);
        myItems = new Vector();
        myShowLabels = true;
        
        myContainer = createContainer();
        myContainer.sizeTo(myScrollGroup.scrollView().width(), 0);
        myScrollGroup.setContentView(myContainer);
        myContainer.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    }
    
    //
    // public methods
    //

    /** Adds the given ECControlGroupItem to the end of the displayed set. */
    public void addItem(ECControlGroupItem item) {
        
        int y = myItems.isEmpty()
            ? 0
            : ((ECControlGroupItem)myItems.lastElement()).bounds().maxY();
        item.moveTo(0, y);
        
        // remember item in our list
        myItems.addElement(item);
        
        // set up item to be displayed properly
        int itemHeight = item.height();
        item.sizeTo(myContainer.width(), itemHeight);
        item.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        item.setLayoutManager(this);
        myContainer.sizeBy(0, itemHeight);
        myContainer.addSubview(item);
        layoutItemContents(item);
    }
    
    /** Responsibility from LayoutManager; does nothing here */
    public void addSubview(View v) {}
    
    /** Returns the background color of the container */
    public Color backgroundColor() {
        return myContainer.backgroundColor();
    }
    
    /** 
     * Returns the ContainerView that the control items are placed on.
     * @see #createContainer
     */
    public ContainerView container() {
        return myContainer;
    }
    
    /** Returns the number of items in this group */
    public int count() {
        if (myItems == null) {
            return 0;
        }
        return myItems.size();
    }
    
    /** Returns the width of each item's text label */
    public int getLabelWidth() {
        return myLabelWidth;
    }
    
    /** Returns the view that displays the scrolling set of items */
    public ScrollGroup getScrollGroup() {
        return myScrollGroup;
    }
    
    /** Returns the ECControlGroupItem at the given index */
    public ECControlGroupItem itemAt(int index) {
        return (ECControlGroupItem)myItems.elementAt(index);
    }
    
    /**
     * Responsibility from layoutManager. If <b>view</b> is one of the
     * set of ECControlGroupItems displayed by this ECControlGroup, this
     * calls layoutContents() on it. If not, it is ignored.
     */
    public void layoutView(View view, int deltaWidth, int deltaHeight) {
        // we will only lay out our items
        if (myItems.indexOf(view) < 0) {
            return;
        }
    
        layoutItemContents((ECControlGroupItem)view);
    }

    /** Empties the set of ECControlGroupItems */
    public void removeAllItems() {
        int count = count();
        if (count == 0) {
            return;
        }
        
        myContainer.disableDrawing();

        // remove each item from list and container
        // walk through list backwards to avoid messing with index
        for (int i = count - 1; i >= 0; i -= 1) {
            ECControlGroupItem item 
                = (ECControlGroupItem)myItems.elementAt(i);
            item.removeFromSuperview();
            myItems.removeElementAt(i);
        }
        
        // shrink container back down to empty size
        myContainer.sizeTo(myContainer.width(), 0);

        myContainer.reenableDrawing();              
        myContainer.setDirty(true);
    }    
    
    
    /**
     * Removes an item from the set of ECControlGroupItems, and
     * closes up to fill in the gap.
     */
    public void removeItem(ECControlGroupItem item) {
        int itemIndex = myItems.indexOf(item);
        if (itemIndex < 0) {
            return;
        }
        
        myItems.removeElementAt(itemIndex);
        item.removeFromSuperview();
        
        // adjust all later items
        int itemHeight = item.height();
        int count = count();
        for (int i = itemIndex; i < count; i += 1) {
            ECControlGroupItem laterItem
                = (ECControlGroupItem)myItems.elementAt(i);
            laterItem.moveBy(0, -itemHeight);
        }
        // shrink container so scroll bars adjust
        myContainer.sizeBy(0, -itemHeight);
        
        // XXX ?? need to kick scroll bars in head here?
    }
    
    /** Responsibility from LayoutManager; does nothing here */
    public void removeSubview(View v) {}

    /** Scrolls the specified item into view */
    public void scrollItemToVisible(ECControlGroupItem item) {
        if (item == null) {
            return;
        }
        
        if (myItems.indexOf(item) < 0) {
            // this should only be called for items in this control group
            if (Trace.gui.warning && Trace.ON) {
                Trace.gui.warningm(item + " not in this control group; will do nothing");
            }
            return;
        }
        
        Rect itemRect = item.bounds();
        itemRect.moveBy(myContainer.x(), myContainer.y());
        myScrollGroup.scrollView().scrollRectToVisible(itemRect);
    }
    
    /** Sets the background color of the container and its scroll group */
    public void setBackgroundColor(Color newColor) {
        myContainer.setBackgroundColor(newColor);
        myScrollGroup.setBackgroundColor(newColor);
    }
    
    /** Sets the label width and lays out each item in response */
    public void setLabelWidth(int newWidth) {
        if (newWidth == myLabelWidth) {
            return;
        }
        
        myLabelWidth = newWidth;
        
        if (myShowLabels) {
            relayoutContents();
        }
    }
    
    /**
     * Sets the view that displays the scrolling set of items. This is
     * generally called only at initialization time, but could be switched
     * later.
     */
    public void setScrollGroup(ScrollGroup newScrollGroup) {
        if (myScrollGroup != null && newScrollGroup != null) {
            View scrollContents = myScrollGroup.contentView();
            myScrollGroup.setContentView(null);
            newScrollGroup.setContentView(scrollContents);
        }
        
        myScrollGroup = newScrollGroup;
        // readjust width of contents to match new scroll group
        myContainer.sizeTo(myScrollGroup.scrollView().width(), myContainer.height());
    }
    
    /** Sets whether or not text labels are currently showing */
    public void setShowLabels(boolean newValue) {
        if (newValue == myShowLabels) {
            return;
        }
        
        myShowLabels = newValue;
        relayoutContents();
    }
    
    /** Returns whether or not text labels are currently showing */
    public boolean showLabels() {
        return myShowLabels;
    }
    
    //
    // protected methods
    //

    /**
     * Returns a new ContainerView to use as backdrop for controls.
     * You should never call this, but you might override it to alter the
     * appearance of the container. This default implementation returns
     * a borderless but otherwise generic ContainerView.
     */
    protected ContainerView createContainer() {
        ContainerView result = new ContainerView();
        result.setBorder(null);
        result.setBackgroundColor(Color.white);
        return result;
    }

    //
    // private methods
    //
    
    /** Called when one item needs to be laid out */
    private void layoutItemContents(ECControlGroupItem item) {
        item.layoutContents(myShowLabels ? myLabelWidth : 0);
    }

    /** Called when each item needs to be laid out again */
    private void relayoutContents() {
        int count = count();
        for (int i = 0; i < count; i += 1) {
            layoutItemContents((ECControlGroupItem)myItems.elementAt(i));
        }
        
        myScrollGroup.setDirty(true);
    }   
}

