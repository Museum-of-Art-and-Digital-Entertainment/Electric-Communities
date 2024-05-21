/** View subclass that maintains a snaking column of ListItems. Items can
  * be selected and deselected programmatically or via the mouse. The
  * ListView can also send a command to a Target when the user single- or
  * double-clicks on a ListItem. ListViews are often placed in ScrollViews to
  * accommodate larger amounts of data.
  *
  * <P>Note that the GridView does not resize itself automatically to fit it's
  * ListItems. If you create a GridView of a particular size and add too many
  * ListItems, the ListItems that do not fit within the bounds of the GridView
  * will not appear displayed.  After adding or removing ListItems, you should
  * call <b>sizeToMinSize()</b> to force the GridView to adjust to its new
  * contents.
  * @see ListItem
  * @see ScrollView                
  * 
  * Author:         Alex McKale
  *
  * 970702  agm Fixed crash if shift and mousedown introduced with IFCListViewAccess.
  * 970625  agm Modified mouseDragged to make sure that there is a current item.
  * 970619  agm Moved editting of titles to window in question.
  * 970618  agm Added editting of titles.
  * 970616 dima Subclassed from ListView, adding IFCListViewAccess class
  * 970605  agm Added keyboard extensions for dragging. 
  * 970604  agm Added keyboard extensions for selection. 
  * 970604  agm Made column width fixed rather than floating. 
  * 970602  agm Fixed to allow multiple selection.
  * 970602  agm Fixed so that "closest" item is returned. 
  */


package ec.ifc.app;

import netscape.application.IFCListViewAccess;
import netscape.application.Graphics;
import netscape.application.Image;
import netscape.application.ListItem;
import netscape.application.ListView;
import netscape.application.MouseEvent;
import netscape.application.Rect;
import netscape.application.Size;
import netscape.application.View;
import netscape.application.TextField;
import netscape.application.Font;

import netscape.util.Vector;

import ec.e.run.Trace;

public class ECGridView extends ListView {

    ListItem    currentActiveItem;

    int columnWidth = 17;   // ALERT!
    int numCols = 1;

    /** Constructs an empty GridView.
      */
    public ECGridView() {
        this(0, 0, 0, 0);
    }

    /** Constructs a GridView with bounds <B>rect</B>.
      */
    public ECGridView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs an empty GridView with the given bounds.
      */
    public ECGridView(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /** Returns a Size consisting of the current number of columns
      */
    public void setNumCols(int nCols) {
        if (nCols > 1)
            numCols = nCols;
        else 
            numCols = 1;
    }
 
    /** Sets the width of each column in the GridView. Each GridView 
      * column has the same width. 
      */
    public void setColumnWidth(int width) {
        if (width > 0)
            columnWidth = width;
    }

    /** Returns the GridView's column width.
      * @see #setColumnWidth
      */
    public int columnWidth() {
        if (columnWidth > 0)
            return columnWidth;
        else
            return 17;

    }

    /** Redetermines the number of columns that can be displayed.
      */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        
        // make sure that we have at least one column!
        if (this.width() < columnWidth())
            setNumCols(1);
        else    
            setNumCols(this.width()/columnWidth());

        // and make sure that the contents get "reshaped"
        if ((count() % numCols) != 0)
            bounds.height = rowHeight()*(1+ count()/numCols);
        else
            bounds.height = rowHeight()*(count()/numCols);
    }

    /** Returns a Size consisting of the current width and the minimum
      * height necessary to accommodate all the rows. Minimum of
      * one row is returned.
      */
    public Size minSize() {

        if ((count() % numCols) != 0)
            return new Size(bounds.width, rowHeight()*(1+ count()/numCols));
        else
            return new Size(bounds.width, rowHeight()*(count()/numCols));
    }

    /** Returns the item at the coordinate (<b>x</b>, <b>y</b>).
      */
    public ListItem itemForPoint(int x, int y) {
        int index, count, offsetInItem;
        int rowHeight = rowHeight();

        count = IFCListViewAccess.items(this).size();

        if (rowHeight == 0)
            return null;

        if (count == 0)
            return null;

        if (!tracksMouseOutsideBounds() &&
            !Rect.contains(0, 0, width(), height(), x, y)) {
            return null;
        }

        // Map onto the edges so that we get the "closest" item

        if (y > bounds.height)
            y = bounds.height - 1;
            
        index = (numCols * (y / rowHeight));

        if (x > numCols * columnWidth())
            index += numCols - 1;
        else 
            index += (x / columnWidth());

        if (index < 0) {
            index = 0;
        } else if (index >= count) {
            index = count - 1;
        }

        return itemAt(index);
    }

    /** 
      * Returns the Rect for the item at the given row index.
      */
    public Rect rectForItemAt(int index) {
        if (index < 0 || index >= count())
            return null;

        int columnWidth = columnWidth();
        int rowHeight = rowHeight();

        return new Rect((index % numCols) * columnWidth, 
            rowHeight * (index / numCols), 
            columnWidth, 
            rowHeight);
//        return new Rect(0, rowHeight * index, bounds.width, rowHeight);
    }

    /** 
      * Removes the item at the given row index.
      */
    public void removeItemAt(int index) {
        ListItem item = itemAt(index);
        Vector items = IFCListViewAccess.items(this);
        Vector selectedItems = selectedItems();
        ListItem anchorItem = IFCListViewAccess.anchorItem(this);

        items.removeElementAt(index);
        selectedItems.removeElement(item);

        if (item == anchorItem) {
            IFCListViewAccess.setAnchorItem(this, null);
        }
 
        if (!allowsEmptySelection() && selectedItems.size() == 0) {
            if (count() > 0) {
                index--;
                if (index < 0) {
                    index = 0;
                }
                selectItem(itemAt(index));
            }
        }
    }

    /** Removes <b>item</b> from the GridView.
      */
    public void removeItem(ListItem item) {
        if (item == IFCListViewAccess.anchorItem(this))
            IFCListViewAccess.setAnchorItem(this, null);
        super.removeItem(item);
    }

    /** Empties the GridView.
      */
    public void removeAllItems() {
        super.removeAllItems();
        IFCListViewAccess.setAnchorItem(this, null);
    }

    /** Selects <b>item</b> within the GridView. If multiple selection is not
      * enabled, any currently selected item is deselected. If <b>item</b>
      * is <b>null</b>, all of the GridView's items are deselected.
      */
    public void selectItem(ListItem item) {
        int i, count;
        ListItem nextItem, selectedItem;
        Vector selectedItems = selectedItems();

        if (item != null && !item.isEnabled()) {
            item = null;
        }

        if (item == null) {
            count = selectedItems.size();

            if (count > 0 && !allowsEmptySelection()) {
                selectedItem = (ListItem)selectedItems.elementAt(count - 1);
                selectedItems.removeElementAt(count - 1);
                count--;
            } else
                selectedItem = null;

            for (i = 0; i < count; i++) {
                nextItem = (ListItem)selectedItems.elementAt(i);
                nextItem.setSelected(false);
                markDirty(nextItem);
            }

            selectedItems.removeAllElements();

            if (selectedItem != null)
                selectedItems.addElement(selectedItem);

        } else if (!selectedItems.contains(item)) {
            // if only one item at a time, deselect the current one

            if (!allowsMultipleSelection()) {
                selectedItem = selectedItem();
                if (selectedItem != null) {
                    selectedItem.setSelected(false);
                    selectedItems.removeElement(selectedItem);
                    markDirty(selectedItem);
                }
            }

            // select the new one

            item.setSelected(true);
            selectedItems.addElement(item);
            markDirty(item);
            if (IFCListViewAccess.anchorItem(this) == null)
                IFCListViewAccess.setAnchorItem(this, item);
        }

        drawDirtyItems();
    }

    /** Overridden so our drawDirtyItems gets called instead of ListView's */
    public void selectOnly(ListItem item) {
        int i, count;
        ListItem nextItem;
        boolean itemAlreadySelected = false;

        if (!item.isEnabled())
            return;

        Vector selectedItems = selectedItems();
        count = selectedItems.size();

        if (count == 1 && item == selectedItems.elementAt(0))
            return;

        for (i = 0; i < count; i++) {
            nextItem = (ListItem)selectedItems.elementAt(i);
            if (nextItem != item) {
                nextItem.setSelected(false);
                markDirty(nextItem);
            } else
                itemAlreadySelected = true;
        }

        selectedItems.removeAllElements();
        selectedItems.addElement(item);

        if (!itemAlreadySelected) {
            item.setSelected(true);
            markDirty(item);
        }

        drawDirtyItems();
    }

    /** Handles mouse down events within the GridView. This method selects and
      * deselects item as the user clicks on them, and sends the
      * GridView's doubleCommand on a double-click.  The normal command is
      * sent in <b>mouseUp()</b>.
      * @see #mouseUp           
      */
    public boolean mouseDown(MouseEvent event) {
        if (!isEnabled()) {
            return false;
        }

        ListItem    clickedItem;
        ListItem    anchorItem = IFCListViewAccess.anchorItem(this);
        
        IFCListViewAccess.setTracking(this, true);

        IFCListViewAccess.setOrigSelectedItem(this, selectedItem());
        clickedItem = itemForPoint(event.x, event.y);
        currentActiveItem = clickedItem;

        if (clickedItem == null)
            return true;
        
        // adjust the click count

        if (anchorItem != clickedItem && event.clickCount() > 1) {
            event.setClickCount(1);
        }

        if (event.isControlKeyDown() && allowsMultipleSelection()) {
            if (clickedItem.isSelected()) {
                deselectItem(clickedItem);
            } else {
                selectItem(clickedItem);
            }
            IFCListViewAccess.setAnchorItem(this, clickedItem);
        } else if (event.isShiftKeyDown() && allowsMultipleSelection()) {
            ListItem   nextItem;
            int        i, nextIndex;
            Rect selectionRect;
            
            if (anchorItem != null) {
                selectionRect = rectForItem(anchorItem);
                selectionRect.unionWith( rectForItem(clickedItem));
            } else {
                selectionRect = rectForItem(clickedItem);
            }

            for (i = 0; i < count(); i++) {
                boolean overlaps = rectForItemAt(i).intersects(selectionRect);
                nextItem = itemAt(i);
                if (nextItem.isSelected()) { 
                    if (!overlaps)
                        deselectItem(nextItem);
                } else {
                    if (overlaps)
                        selectItem(nextItem);
                }
            }
            if (anchorItem == null)
                IFCListViewAccess.setAnchorItem(this, clickedItem);
        } else {
            selectOnly(clickedItem);
            IFCListViewAccess.setAnchorItem(this, clickedItem);
        }
        
        if (event.clickCount() == 2) {
            sendDoubleCommand();
            return false;
        }

        return true;
    }

    /** Tracks the mouse as the user drags it, selecting and deselecting
      * items as appropriate.
      */
    public void mouseDragged(MouseEvent event) {
        int        startIndex, endIndex, anchorIndex, newIndex, nextIndex;
        ListItem   anchorItem = IFCListViewAccess.anchorItem(this);

        // If we haven't started we shouldn't continue

        if (!IFCListViewAccess.tracking(this))
            return;

        // If we only track the mouse within our bounds, then do nothing if
        // the mouse is outside.

        ListItem newItem = itemForPoint(event.x, event.y);

        if (newItem == currentActiveItem)
            return;
            
        currentActiveItem = newItem;
        
        // Don't draw until the end.

        disableDrawing();

        if (!tracksMouseOutsideBounds()) {
            if (!Rect.contains(0, 0, width(), height(), event.x, event.y))
                newItem = null;
        }

        if (!allowsMultipleSelection()) {
            if (newItem != selectedItem()) {
                if (allowsEmptySelection() || newItem != null) {
                    IFCListViewAccess.setAnchorItem(this, newItem);
                    selectItem(newItem);
                }
            }
        } else {
            if (event.isControlKeyDown()) {
                if (newItem.isSelected()) {
                    deselectItem(newItem);
                } else {
                    selectItem(newItem);
                }
            } else if (event.isShiftKeyDown()) {
                anchorIndex = indexOfItem(anchorItem);
                if (newItem != null) {
                    newIndex = indexOfItem(newItem);
                } else if (pointCompare(0, height(), event.y) < 0) {
                    newIndex = 0;
                } else {
                    newIndex = count() - 1;
                }
                if (anchorIndex < newIndex) {
                    startIndex = anchorIndex;
                    endIndex = newIndex;
                } else {
                    startIndex = newIndex;
                    endIndex = anchorIndex;
                }

                Rect selectionRect = rectForItemAt(startIndex);
                selectionRect.unionWith( rectForItemAt(endIndex));

                for (int i = 0; i < count(); i++) {
                    boolean overlaps = rectForItemAt(i).intersects(selectionRect);
                    ListItem nextItem = itemAt(i);

                    if (nextItem.isSelected()) { 
                        if (!overlaps)
                            deselectItem(nextItem);
                    } else {
                        if (overlaps)
                            selectItem(nextItem);
                    }
                }
            }
        }

        reenableDrawing();

        // This will draw the dirty items even if no scrolling is necessary.

        autoscroll(event);
    }

    /** Calls <b>sendCommand()</b> on a single click mouse up.
      */
    public void mouseUp(MouseEvent event) {
        currentActiveItem = null;

        super.mouseUp(event);
    }

    // This is only called from one place and draws the dirty items even if
    // it doesn't need to scroll.

    private void autoscroll(MouseEvent event) {
        Rect visibleRect;
        int rowHeight = rowHeight();

        visibleRect = new Rect();
        computeVisibleRect(visibleRect);

        // If this actually scrolls, then drawView() will get called and the
        // dirtyItems vector will get emptied.  If it does not, then
        // drawDirtyItems will kick into action to clean things up.  This
        // would have a bad interaction with copybits.  The emptying of the
        // dirtyItems vector should be more fine grained to what actually gets
        // drawn.  ALERT!

        drawDirtyItems();
        if (!visibleRect.contains(event.x, event.y)) {
            if (event.y < visibleRect.y) {
                Rect tmpRect = new Rect (visibleRect.x, event.y,
                                            visibleRect.width, rowHeight);
                scrollRectToVisible(tmpRect);
                //Rect.returnRect(tmpRect);
            } else if (event.y > visibleRect.maxY()) {
                Rect tmpRect = new Rect(visibleRect.x, event.y - rowHeight,
                                            visibleRect.width, rowHeight);
                scrollRectToVisible(tmpRect);
                //Rect.returnRect(tmpRect);
            }
        }
        //Rect.returnRect(visibleRect);

    }

    private void drawDirtyItems() {
        int i, count, itemIndex, totalCount;
        Vector tmpVector;
        Rect clipRect;
        int rowHeight = rowHeight();

        if (!canDraw())
            return;

        count = IFCListViewAccess.dirtyItems(this).size();
        if (count == 0)
            return;

        // While we are drawing the dirty items, make sure that drawView()
        // doesn't empty our list out from underneath us.

        tmpVector = IFCListViewAccess.dirtyItems(this);
        IFCListViewAccess.setDirtyItems(this, null);

        clipRect = new Rect(0, 0, columnWidth(), rowHeight);
        totalCount = count();

        for (i = 0; i < count; i++) {
            itemIndex = indexOfItem((ListItem)tmpVector.elementAt(i));

            if (itemIndex < 0 || itemIndex >= totalCount)
                continue;

            clipRect.x = (itemIndex % numCols) * columnWidth();
            clipRect.y =  (rowHeight * (itemIndex/numCols));
            clipRect.height = rowHeight;

            // Merge adjoining rectangles while drawing.  This currently only
            // merges two rectangles at a time, but handles the most common
            // cases.  ALERT!

            //if (i < count - 1) {
            //    int nextIndex = items.indexOf(tmpVector.elementAt(i + 1));
            //    if (nextIndex == itemIndex + 1) {
            //        clipRect.height += rowHeight;
            //        i++;
            //    } else if (nextIndex == itemIndex - 1) {
            //        clipRect.height += rowHeight;
            //        clipRect.y -= rowHeight;
            //        i++;
            //    }
            //}

            draw(clipRect);
        }
        //Rect.returnRect(clipRect);

        // Restore the dirtyItems vector and empty it out.
        IFCListViewAccess.setDirtyItems(this, tmpVector);
        IFCListViewAccess.dirtyItems(this).removeAllElements();
    }

    /** Draws the GridView, calling <b>drawViewBackground()</b> to draw each
      * item's background.
      */
    public void drawView(Graphics g) {
        ListItem            nextItem;
        Rect            itemRect, clipRect;
        int             count, i, index;
        boolean         shouldDrawItem;
        boolean         itemIsVisible;
        int             rowHeight = rowHeight();

        // Once we are told to draw, forget about any dirty elements.  If we
        // are in the middle of drawing the dirty elements, the field will be
        // set to null so that we won't remove them.  This needs to only
        // remove Items that it actually draws or are not visible.  If you
        // draw something with a small clipping rect, then this will forget
        // about dirty Items which are visible but outside the clipping rect.
        // ALERT!

        if (IFCListViewAccess.dirtyItems(this) != null) 
            IFCListViewAccess.dirtyItems(this).removeAllElements();

        if (rowHeight <= 0) {
            drawViewBackground(g, 0, 0, bounds.width, bounds.height);
            return;
        }

        // Find first item which intersects the clipping rect.

        clipRect = g.clipRect();

        count = count();
        i = (clipRect.y / rowHeight) * numCols + clipRect.x / columnWidth() ;
        if (i < 0 || i >= count) {
            drawViewBackground(g, 0, 0, bounds.width, bounds.height);
            return;
        }

        // Draw all the visible items.

        itemRect = rectForItemAt(i);
        itemIsVisible = (rectCompare(clipRect, itemRect) == 0);

        while (i < count && itemRect.y < (clipRect.y + clipRect.height)) {
            if (itemIsVisible) {
                g.pushState();
                g.setClipRect(itemRect);

                nextItem = itemAt(i);
                if (!isTransparent() && nextItem.isTransparent()) {
                    drawViewBackground(g, itemRect.x, itemRect.y,
                                            bounds.width, rowHeight);
                }

                nextItem.drawInRect(g, itemRect);
                g.popState();
            }

            i++;
            if ((i-1) % numCols == (numCols - 1)) {
                // at the end of the row, move down and back
                itemRect.y += rowHeight;
                itemRect.x = 0;
            } else
                itemRect.x += columnWidth();

            itemIsVisible = (rectCompare(clipRect, itemRect) == 0);
        }

        // If there is space at the bottom of the view, fill it with the
        // background color.

        if (itemIsVisible) {
            int remainingHeight = bounds.height - itemRect.y;

            if (remainingHeight > 0 && !isTransparent()) {
                drawViewBackground(g, itemRect.x, itemRect.y,
                                       bounds.width, remainingHeight);
            }
        }

        // Rect.returnRect(clipRect);
    }

    private void markDirty(ListItem item) {
        if (IFCListViewAccess.dirtyItems(this).contains(item))
            return;

        IFCListViewAccess.dirtyItems(this).addElement(item);
    }

    private int pointCompare(int y, int height, int pointY) {
        if (pointY < y)
            return -1;
        else if (pointY >= (y + height))
            return 1;
        else
            return 0;
    }

    private int rectCompare(Rect containingRect, Rect otherRect) {
        if (otherRect.maxY() <= containingRect.y) {
            return -1;
        } else if (otherRect.y >= containingRect.maxY()) {
            return 1;
        } else {
            return 0;
        }
    }
    
    /** Overridden just to supply tracing information */
    public void sendCommand() {
        if (Trace.gui.usage && Trace.ON) {
            Trace.gui.usagem("selected " 
                + TraceUtils.traceDescription(selectedItem()));
        }
        super.sendCommand();
    }

    /** Overridden just to supply tracing information */
    public void sendDoubleCommand() {
        if (Trace.gui.usage && Trace.ON) {
            Trace.gui.usagem("double-clicked " 
                + TraceUtils.traceDescription(selectedItem()));
        }
        super.sendDoubleCommand();
    }

}
