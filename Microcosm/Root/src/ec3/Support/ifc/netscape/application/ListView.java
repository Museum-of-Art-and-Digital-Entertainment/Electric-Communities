// ListView.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

// ALERT!
// There needs to be a way from the UI to deselect and extend the selection.
// Need an allDirty flag after things like addItem, etc.

/** View subclass that maintains a vertical column of ListItems. ListItems can
  * be selected and deselected programmatically or via the mouse. The
  * ListView can also send a command to a Target when the user single- or
  * double-clicks on a ListItem. ListViews are often placed in ScrollViews to
  * accommodate larger amounts of data.
  *
  * <P>Note that the ListView does not resize itself automatically to fit it's
  * ListItems. If you create a ListView of a particular size and add too many
  * ListItems, the ListItems that do not fit within the bounds of the ListView
  * will not appear displayed.  After adding or removing ListItems, you should
  * call <b>sizeToMinSize()</b> to force the ListView to adjust to its new
  * contents.
  * @see ListItem
  * @see ScrollView
  * @see Target
  * @note 1.0 Keyboard UI support
  * @note 1.0 fix to multipleItemsSelected (returns true when > 1)
  * @note 1.0 archiving transparent flag
  * @note 1.0 fixed transparent drawing bug
  * @note 1.0 Added FormElement interface for browser needs
  */

public class ListView extends View implements Target, FormElement {
    ListItem protoItem;
    ListItem anchorItem;
    ListItem origSelectedItem;

    Vector items = new Vector();
    Vector selectedItems = new Vector();
    Vector dirtyItems = new Vector();

    String command;
    String doubleCommand;
    Target target;

    Color backgroundColor = Color.lightGray;

    int rowHeight = 17; // ALERT!

    boolean allowsMultipleSelection;
    boolean allowsEmptySelection;
    boolean tracksMouseOutsideBounds = true;
    boolean tracking;
    boolean enabled = true, transparent = false;

    final static String         PROTOITEM_KEY = "protoItem",
                                ANCHORITEM_KEY = "anchorItem",
                                ORIGSELECTED_KEY = "origSelectedItem",
                                ITEMS_KEY = "items",
                                SELECTEDITEMS_KEY = "selectedItems",
                                DIRTYITEMS_KEY = "dirtyItems",
                                COMMAND_KEY = "command",
                                DOUBLECOMMAND_KEY = "doubleCommand",
                                TARGET_KEY = "target",
                                BACKGROUNDCOLOR_KEY = "backgroundColor",
                                ROWHEIGHT_KEY = "rowHeight",
                                MULTSELECTION_KEY = "allowsMultipleSelection",
                                EMPTYSELECTION_KEY = "allowsEmptySelection",
                                TRACKSMOUSE_KEY = "tracksMouseOutsideBounds",
                                ENABLED_KEY = "enabled",
                                TRANSPARENT_KEY = "isTransparent";


    /** Cause the list view to select the next item in the list
      *
      */
    public static String SELECT_NEXT_ITEM = "selectNext";

    /** Cause the list view to select the previous item in the list
      *
      */
    public static String SELECT_PREVIOUS_ITEM = "selectPrevious";

    /** Constructs an empty ListView.
      */
    public ListView() {
        this(0, 0, 0, 0);
    }

    /** Constructs a ListView with bounds <B>rect</B>.
      */
    public ListView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs an empty ListView with the given bounds.
      */
    public ListView(int x, int y, int width, int height) {
        super(x, y, width, height);
        _setupKeyboard();
    }

    /** Sets the ListView's "prototype" ListItem. ListView adds additional
      * rows by cloning its prototype ListItem. Typical usage consists of
      * configuring the prototype ListItem and then modifying only the
      * unique attributes on a per-row basis. By default, the prototype
      * ListItem is just a generic ListItem instance.
      * @see #addItem
      * @see #insertItemAt
      */
    public void setPrototypeItem(ListItem item) {
        if (item == null)
            protoItem = new ListItem();
        else
            protoItem = item;

        if (protoItem.font() == null)
            protoItem.setFont(Font.fontNamed("Default"));

        protoItem.setListView(this);
    }

    /** Returns the prototype ListItem.
      * @see #setPrototypeItem
      */
    public ListItem prototypeItem() {
        if (protoItem == null)
            setPrototypeItem(null);

        return protoItem;
    }

    /** Sets the color drawn behind transparent ListItems, and any area in
      * the ListView not covered by ListItems.
      */
    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    /** Returns the background color.
      * @see #setBackgroundColor
      */
    public Color backgroundColor() {
        return backgroundColor;
    }

    /** Sets the ListView to be transparent or opaque.
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
    }

    /** Overridden to return <b>true</b> if the ListView is transparent.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Overridden to return <b>true</b> (ListViews can be autoscrolled).
      */
    public boolean wantsAutoscrollEvents() {
        return true;
    }

    /** Sets whether multiple ListItems can be selected simultaneously.
      */
    public void setAllowsMultipleSelection(boolean flag) {
        allowsMultipleSelection = flag;
    }

    /** Returns <b>true</b> if multiple ListItems can be selected
      * simultaneously.
      * @see #setAllowsMultipleSelection
      */
    public boolean allowsMultipleSelection() {
        return allowsMultipleSelection;
    }

    /** Sets whether the ListView allows a selection of zero ListItems.
      */
    public void setAllowsEmptySelection(boolean flag) {
        allowsEmptySelection = flag;
    }

    /** Returns <b>true</b> if the List allows a selection of zero ListItems.
      * @see #setAllowsEmptySelection
      */
    public boolean allowsEmptySelection() {
        return allowsEmptySelection;
    }

    /** Sets whether the ListView tracks the mouse outside its bounds. By
      * default, ListViews track the mouse outside of their bounds, which
      * means that if the user drags the mouse outside of the ListView's
      * bounds, the ListView will continue to modify the current selection.
      * Certain ListViews, like the ones in Popups, do not track the mouse
      * outside of their bounds (when the user moves the mouse outside of the
      * ListView's bounds, the current selection will not change).
      */
    public void setTracksMouseOutsideBounds(boolean flag) {
        tracksMouseOutsideBounds = flag;
    }

    /** Returns <b>true</b> if the ListView tracks the mouse outside its
      * bounds.
      * @see #setTracksMouseOutsideBounds
      */
    public boolean tracksMouseOutsideBounds() {
        return tracksMouseOutsideBounds;
    }

    /** Enables or disables the ListView and marks it dirty.
      */
    public void setEnabled(boolean flag) {
        if (enabled != flag) {
            enabled = flag;
            setDirty(true);
        }
    }

    /** Returns <b>true</b> if the ListView is enabled.
      * @see #setEnabled
      */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets the height of each row in the ListView. Each ListView rows has the
      * same height. If <b>height</b> is 0, sets the height to
      * <b>minItemHeight()</b>.
      * @see #minItemHeight
      */
    public void setRowHeight(int height) {
        if (height > 0)
            rowHeight = height;
        else
            rowHeight = minItemHeight();
    }

    /** Returns the ListView's row height.
      * @see #setRowHeight
      */
    public int rowHeight() {
        if (rowHeight > 0)
            return rowHeight;

        setRowHeight(minItemHeight());
        return rowHeight;
    }

    /** Returns the largest <b>minHeight()</b> of all of the ListView's
      * ListItems.
      */
    public int minItemHeight() {
        int i, count, minHeight, height;
        ListItem item;

        minHeight = 0;
        count = items.size();

        for (i = 0; i < count; i++) {
            item = (ListItem)items.elementAt(i);
            height = item.minHeight();

            if (height > minHeight)
                minHeight = height;
        }

        return minHeight;
    }

    /** Returns the largest <b>minWidth()</b> of all of the ListView's
      * ListItems.
      */
    public int minItemWidth() {
        int i, count, minWidth, width;
        ListItem item;

        minWidth = 0;
        count = items.size();

        for (i = 0; i < count; i++) {
            item = (ListItem)items.elementAt(i);
            width = item.minWidth();

            if (width > minWidth)
                minWidth = width;
        }

        return minWidth;
    }

    /** Returns a Size consisting of the current width and the minimum
      * height necessary to accommodate all the rows.
      */
    public Size minSize() {
        return new Size(bounds.width, count() * rowHeight());
    }

    /** Returns the ListItem at the coordinate (<b>x</b>, <b>y</b>).
      */
    public ListItem itemForPoint(int x, int y) {
        int index, count, offsetInItem;

        count = items.size();

        if (rowHeight == 0)
            return null;

        if (count == 0)
            return null;

        if (!tracksMouseOutsideBounds &&
            !Rect.contains(0, 0, width(), height(), x, y)) {
            return null;
        }

        index = y / rowHeight;

        if (index < 0) {
            index = 0;
        } else if (index >= count) {
            index = count - 1;
        }

        return itemAt(index);
    }

    /** Returns the ListItem at the given row index.
      */
    public ListItem itemAt(int index) {
        return (ListItem)items.elementAt(index);
    }

    /** Returns the row index for <b>item</b>.
      */
    public int indexOfItem(ListItem item) {
        return items.indexOf(item);
    }

    /** Returns the Rect occupied by <b>item</b> within the ListView.
      */
    public Rect rectForItem(ListItem item) {
        if (item == null) {
            return null;
        }

        return rectForItemAt(items.indexOf(item));
    }

    /** Returns the Rect for the ListItem at the given row index.
      */
    public Rect rectForItemAt(int index) {
        if (index < 0 || index >= items.size())
            return null;

        return new Rect(0, rowHeight * index, bounds.width, rowHeight);
    }

    // This method name should change.  ALERT!

    /** Returns <b>true</b> if multiple ListItems are selected.
      */
    public boolean multipleItemsSelected() {
        return (selectedItems.size() > 1);
    }

    /** Returns the row of the selected ListItem. If multiple ListItems are
      * selected, returns the row of the first selected ListItem.
      */
    public int selectedIndex() {
        ListItem item;

        item = selectedItem();

        if (item == null)
            return -1;

        return items.indexOf(item);
    }

    /** Returns the selected ListItem. If multiple ListItems are selected,
      * returns the first one.
      */
    public ListItem selectedItem() {
        if (selectedItems.size() > 0)
            return (ListItem)selectedItems.elementAt(0);

        return null;
    }

    /** Returns a Vector of all the selected ListItems. Do not alter this
      * Vector.
      */
    public Vector selectedItems() {
        return selectedItems;
    }

    /** Returns the number of ListItems in the ListView.
      */
    public int count() {
        return items.size();
    }

    /** Adds an additional row to the end of the ListView by cloning the
      * prototype ListItem. Returns the newly-create ListItem.
      */
    public ListItem addItem() {
        return insertItemAt(items.size());
    }

    /** Adds the ListItem <b>item</b> to the end of the ListView.
      */
    public ListItem addItem(ListItem item) {
        if (item.font() == null)
            item.setFont(Font.defaultFont());

        item.setListView(this);

        return insertItemAt(item, items.size());
    }

    /** Adds the ListItem <b>item</b> to the ListView at the given index.
      */
    public ListItem insertItemAt(ListItem item, int index) {
        if (item.font() == null)
            item.setFont(Font.defaultFont());

        item.setListView(this);
        items.insertElementAt(item, index);

        if (!allowsEmptySelection && selectedItems.isEmpty())
            selectItem(item);

        return item;
    }

    /** Adds a row to the ListView at the given index by cloning the
      * prototype ListItem.  Returns the newly-created ListItem.
      */
    public ListItem insertItemAt(int index) {
        ListItem newItem;

        newItem = (ListItem)prototypeItem().clone();
        items.insertElementAt(newItem, index);

        if (!allowsEmptySelection && selectedItems.isEmpty())
            selectItem(newItem);

        return newItem;
    }

    /** Removes the ListItem at the given row index.
      */
    public void removeItemAt(int index) {
        ListItem item;

        item = (ListItem)items.elementAt(index);
        items.removeElementAt(index);
        selectedItems.removeElement(item);

        if (!allowsEmptySelection && selectedItems.size() == 0) {
            if (items.size() > 0) {
                index--;
                if (index < 0) {
                    index = 0;
                }
                selectItem(itemAt(index));
            }
        }
    }

    /** Removes <b>item</b> from the ListView.
      */
    public void removeItem(ListItem item) {
        removeItemAt(items.indexOf(item));
    }

    /** Empties the ListView.
      */
    public void removeAllItems() {
        items.removeAllElements();
        selectItem(null);
    }

    /** Selects <b>item</b> within the ListView. If multiple selection is not
      * enabled, any currently selected ListItem is deselected. If <b>item</b>
      * is <b>null</b>, all of the ListView's ListItems are deselected.
      */
    public void selectItem(ListItem item) {
        int i, count;
        ListItem nextItem, selectedItem;

        if (item != null && !item.isEnabled()) {
            item = null;
        }

        if (item == null) {
            count = selectedItems.size();

            if (count > 0 && !allowsEmptySelection) {
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

            if (!allowsMultipleSelection) {
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
        }

        drawDirtyItems();
    }

    /** Calls <b>selectItem()</b> using the item at given row index
      * <b>index</b>.
      */
    public void selectItemAt(int index) {
        selectItem((ListItem)items.elementAt(index));
    }

    /** Selects <b>item</b> and deselects all others.
      */
    public void selectOnly(ListItem item) {
        int i, count;
        ListItem nextItem;
        boolean itemAlreadySelected = false;

        if (!item.isEnabled())
            return;

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

    /** Deselects <b>item</b>, unless empty selection is not allowed and
      * it is the only selected ListItem.
      */
    public void deselectItem(ListItem item) {
        if (item == null ||
                selectedItems.size() == 1 && !allowsEmptySelection) {
            return;
        }

        if (!items.contains(item) || !selectedItems.contains(item)) {
            return;
        }

        // remove from selected vector and deselect

        selectedItems.removeElement(item);
        item.setSelected(false);
        markDirty(item);

        drawDirtyItems();
    }

    /** Calls <b>scrollRectToVisible()</b> with the Rect for the item at
      * <b>index</b>.
      * @see View#scrollRectToVisible
      */
    public void scrollItemAtToVisible(int index) {
        scrollRectToVisible(rectForItemAt(index));
    }

    /** Calls <b>scrollRectToVisible()</b> with the Rect for <b>item</b>.
      * @see View#scrollRectToVisible
      */
    public void scrollItemToVisible(ListItem item) {
        scrollRectToVisible(rectForItem(item));
    }

    /** Handles mouse down events within the ListView. This method selects and
      * deselects ListItems as the user clicks on them, and sends the
      * ListView's doubleCommand on a double-click.  The normal command is
      * sent in <b>mouseUp()</b>.
      * @see #setDoubleCommand
      * @see #mouseUp
      */
    public boolean mouseDown(MouseEvent event) {
        ListItem    clickedItem;

        if (!enabled) {
            return false;
        }

        tracking = true;

        origSelectedItem = selectedItem();
        clickedItem = itemForPoint(event.x, event.y);

        if (clickedItem == null)
            return true;

        // adjust the click count

        if (anchorItem != clickedItem && event.clickCount > 1) {
            event.setClickCount(1);
        }

        // deselect all, then select

        selectOnly(clickedItem);
        anchorItem = clickedItem;

        if (event.clickCount == 2) {
            sendDoubleCommand();
            return false;
        }

        return true;
    }

    /** Tracks the mouse as the user drags it, selecting and deselecting
      * ListItems as appropriate.
      */
    public void mouseDragged(MouseEvent event) {
        ListItem   newItem, nextItem;
        int        i, startIndex, endIndex, anchorIndex, newIndex, nextIndex;

        // If we haven't started we shouldn't continue

        if (!tracking)
            return;

        // Don't draw until the end.

        disableDrawing();

        // If we only track the mouse within our bounds, then do nothing if
        // the mouse is outside.

        newItem = itemForPoint(event.x, event.y);

        if (!tracksMouseOutsideBounds) {
            if (!Rect.contains(0, 0, width(), height(), event.x, event.y))
                newItem = null;
        }

        if (!allowsMultipleSelection) {
            if (newItem != selectedItem()) {
                if (allowsEmptySelection || newItem != null) {
                    anchorItem = newItem;
                    selectItem(newItem);
                }
            }
        } else {
            anchorIndex = items.indexOf(anchorItem);
            if (newItem != null) {
                newIndex = items.indexOf(newItem);
            } else if (pointCompare(0, height(), event.y) < 0) {
                newIndex = 0;
            } else {
                newIndex = items.size() - 1;
            }
            if (anchorIndex < newIndex) {
                startIndex = anchorIndex;
                endIndex = newIndex;
            } else {
                startIndex = newIndex;
                endIndex = anchorIndex;
            }

            // deselect anyone who shouldn't be selected

            i = selectedItems.size();
            while (i-- > 0) {
                nextItem = (ListItem)selectedItems.elementAt(i);
                nextIndex = items.indexOf(nextItem);

                if (nextItem.isSelected() &&
                    (nextIndex < startIndex || nextIndex > endIndex)) {
                    deselectItem(nextItem);
                } else if (!nextItem.isSelected() &&
                        (nextIndex >= startIndex && nextIndex <= endIndex)) {
                    selectItem(nextItem);
                }
            }

            // select anyone who should be but isn't
            if(startIndex != -1 && endIndex != -1)  {
                for (i = startIndex; i <= endIndex; i++) {
                    nextItem = (ListItem)items.elementAt(i);
                    if (!nextItem.isSelected())
                        selectItem(nextItem);
                }
            }
        }

        reenableDrawing();

        // This will draw the dirty items even if no scrolling is necessary.

        autoscroll(event);
    }

    /** Calls <b>sendCommand()</b> on a single click mouse up.
      * @see #sendCommand
      */
    public void mouseUp(MouseEvent event) {
        if (event.clickCount == 1)
            sendCommand();

        tracking = false;
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

    // This is only called from one place and draws the dirty items even if
    // it doesn't need to scroll.

    private void autoscroll(MouseEvent event) {
        Rect visibleRect;

        visibleRect = Rect.newRect();
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
                Rect tmpRect = Rect.newRect(visibleRect.x, event.y,
                                            visibleRect.width, rowHeight);
                scrollRectToVisible(tmpRect);
                Rect.returnRect(tmpRect);
            } else if (event.y > visibleRect.maxY()) {
                Rect tmpRect = Rect.newRect(visibleRect.x, event.y - rowHeight,
                                            visibleRect.width, rowHeight);
                scrollRectToVisible(tmpRect);
                Rect.returnRect(tmpRect);
            }
        }
        Rect.returnRect(visibleRect);

    }

    private void markDirty(ListItem item) {
        if (dirtyItems.contains(item))
            return;

        dirtyItems.addElement(item);
    }

    private void drawDirtyItems() {
        int i, count, itemIndex, totalCount;
        Vector tmpVector;
        Rect clipRect;

        if (!canDraw())
            return;

        count = dirtyItems.size();
        if (count == 0)
            return;

        // While we are drawing the dirty items, make sure that drawView()
        // doesn't empty our list out from underneath us.

        tmpVector = dirtyItems;
        dirtyItems = null;

        clipRect = Rect.newRect(0, 0, bounds.width, rowHeight);
        totalCount = count();

        for (i = 0; i < count; i++) {
            itemIndex = items.indexOf(tmpVector.elementAt(i));

            if (itemIndex < 0 || itemIndex >= totalCount)
                continue;

            clipRect.y = itemIndex * rowHeight;
            clipRect.height = rowHeight;

            // Merge adjoining rectangles while drawing.  This currently only
            // merges two rectangles at a time, but handles the most common
            // cases.  ALERT!

            if (i < count - 1) {
                int nextIndex = items.indexOf(tmpVector.elementAt(i + 1));
                if (nextIndex == itemIndex + 1) {
                    clipRect.height += rowHeight;
                    i++;
                } else if (nextIndex == itemIndex - 1) {
                    clipRect.height += rowHeight;
                    clipRect.y -= rowHeight;
                    i++;
                }
            }

            draw(clipRect);
        }
        Rect.returnRect(clipRect);

        // Restore the dirtyItems vector and empty it out.

        dirtyItems = tmpVector;
        dirtyItems.removeAllElements();
    }

    /** Calls <b>draw()</b> with the Rect for the ListItem at the given row
      * index.
      */
    public void drawItemAt(int index) {
        Rect r = rectForItemAt(index);
        draw(r); // see the big ALERT! below...
    }

    /** Called by <b>drawView()</b> to draw the background for the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>).  You never
      * call this method directly, but should override it to produce custom
      * background drawing.
      */
    public void drawViewBackground(Graphics g, int x, int y, int width,
                                       int height) {
        if(!isTransparent() && backgroundColor != null) {
            g.setColor(backgroundColor);
            g.fillRect(x, y, width, height);
        }
    }

    /** Draws the ListView, calling <b>drawViewBackground()</b> to draw each
      * ListItem's background.
      * @see #drawViewBackground
      */
    public void drawView(Graphics g) {
        ListItem            nextItem;
        Rect            itemRect, clipRect;
        int             count, i, index;
        boolean         shouldDrawItem;
        boolean         itemIsVisible;

        // Once we are told to draw, forget about any dirty elements.  If we
        // are in the middle of drawing the dirty elements, the field will be
        // set to null so that we won't remove them.  This needs to only
        // remove Items that it actually draws or are not visible.  If you
        // draw something with a small clipping rect, then this will forget
        // about dirty Items which are visible but outside the clipping rect.
        // ALERT!

        if (dirtyItems != null)
            dirtyItems.removeAllElements();

        if (rowHeight <= 0) {
            drawViewBackground(g, 0, 0, bounds.width, bounds.height);
            return;
        }

        // Find first item which intersects the clipping rect.

        clipRect = Rect.newRect(g.clipRect());

        count = items.size();
        i = clipRect.y / rowHeight;
        if (i < 0 || i >= count) {
            drawViewBackground(g, 0, 0, bounds.width, bounds.height);
            Rect.returnRect(clipRect);
            return;
        }

        // Draw all the visible items.

        itemRect = rectForItemAt(i);
        itemIsVisible = (rectCompare(clipRect, itemRect) == 0);

        while (i < count && itemIsVisible) {
            g.pushState();
            g.setClipRect(itemRect);

            nextItem = (ListItem)items.elementAt(i);
            if (!isTransparent() && nextItem.isTransparent()) {
                drawViewBackground(g, itemRect.x, itemRect.y,
                                        bounds.width, rowHeight);
            }

            nextItem.drawInRect(g, itemRect);
            g.popState();

            i++;
            itemRect.moveBy(0, rowHeight);
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

        Rect.returnRect(clipRect);
    }

    /** Sets the ListView's Target.
      */
    public void setTarget(Target newTarget) {
        target = newTarget;
    }

    /** Returns the ListView's Target.
      * @see #setTarget
      */
    public Target target() {
        return target;
    }

    /** Sets the ListView's single-click command. ListView sends this command
      * when the user clicks a ListItem and the ListItem does not have a
      * command of its own.
      * @see ListItem#setCommand
      */
    public void setCommand(String newCommand) {
        command = newCommand;
    }

    /** Returns the ListView's single-click command.
      * @see #setCommand
      */
    public String command() {
        return command;
    }

    /** Sets the ListView's double-click command.  ListView sends this command
      * when the user double-clicks a ListItem.
      */
    public void setDoubleCommand(String newCommand) {
        doubleCommand = newCommand;
    }

    /** Returns the ListView's double-click command.
      * @see #setDoubleCommand
      */
    public String doubleCommand() {
        return doubleCommand;
    }

    /** Sends the single-click command of the selected ListItem to the
      * ListView's target. If the selected ListItem does not have a command,
      * sends the ListView's single-click command.
      * @see #setCommand
      */
    public void sendCommand() {
        if (target != null) {
            String realCommand = null;
            ListItem selectedItem = selectedItem();

            if (selectedItem != null)
                realCommand = selectedItem.command();

            if (realCommand == null)
                realCommand = command;

            target.performCommand(realCommand, this);
        }
    }

    /** Sends the ListView's double-click command to its Target.
      * @see #setDoubleCommand
      */
    public void sendDoubleCommand() {
        if (target != null && doubleCommand != null) {
            target.performCommand(doubleCommand, this);
        }
    }

    public void performCommand(String aCommand,Object anObject) {
        if(SELECT_NEXT_ITEM.equals(aCommand)) {
            selectNextItem(true);
        } else if(SELECT_PREVIOUS_ITEM.equals(aCommand))
            selectNextItem(false);
    }

    void selectNextItem(boolean forward) {
        int si = selectedIndex();
        int count = count();

        if(forward) {
            if(si < (count-1)) {
                selectItemAt(si+1);
                sendCommand();
            }
        } else {
            if(si > 0) {
                selectItemAt(si-1);
                sendCommand();
            }
        }
    }

    /** Describes the ListView class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.ListView", 2);
        info.addField(PROTOITEM_KEY, OBJECT_TYPE);
        info.addField(ANCHORITEM_KEY, OBJECT_TYPE);
        info.addField(ORIGSELECTED_KEY, OBJECT_TYPE);
        info.addField(ITEMS_KEY, OBJECT_TYPE);
        info.addField(SELECTEDITEMS_KEY, OBJECT_TYPE);
        info.addField(DIRTYITEMS_KEY, OBJECT_TYPE);

        info.addField(COMMAND_KEY, STRING_TYPE);
        info.addField(DOUBLECOMMAND_KEY, STRING_TYPE);

        info.addField(TARGET_KEY, OBJECT_TYPE);
        info.addField(BACKGROUNDCOLOR_KEY, OBJECT_TYPE);

        info.addField(ROWHEIGHT_KEY, INT_TYPE);

        info.addField(MULTSELECTION_KEY, BOOLEAN_TYPE);
        info.addField(EMPTYSELECTION_KEY, BOOLEAN_TYPE);
        info.addField(TRACKSMOUSE_KEY, BOOLEAN_TYPE);
        info.addField(ENABLED_KEY, BOOLEAN_TYPE);
        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the ListView instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(PROTOITEM_KEY, protoItem);
        encoder.encodeObject(ANCHORITEM_KEY, anchorItem);
        encoder.encodeObject(ORIGSELECTED_KEY, origSelectedItem);
        encoder.encodeObject(ITEMS_KEY, items);
        encoder.encodeObject(SELECTEDITEMS_KEY, selectedItems);
        encoder.encodeObject(DIRTYITEMS_KEY, dirtyItems);

        encoder.encodeString(COMMAND_KEY, command);
        encoder.encodeString(DOUBLECOMMAND_KEY, doubleCommand);

        encoder.encodeObject(TARGET_KEY, target);
        encoder.encodeObject(BACKGROUNDCOLOR_KEY, backgroundColor);

        encoder.encodeInt(ROWHEIGHT_KEY, rowHeight);

        encoder.encodeBoolean(MULTSELECTION_KEY, allowsMultipleSelection);
        encoder.encodeBoolean(EMPTYSELECTION_KEY, allowsEmptySelection);
        encoder.encodeBoolean(TRACKSMOUSE_KEY, tracksMouseOutsideBounds);
        encoder.encodeBoolean(ENABLED_KEY, enabled);
        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);
    }

    /** Decodes the ListView instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        protoItem = (ListItem)decoder.decodeObject(PROTOITEM_KEY);
        anchorItem = (ListItem)decoder.decodeObject(ANCHORITEM_KEY);
        origSelectedItem = (ListItem)decoder.decodeObject(ORIGSELECTED_KEY);

        items = (Vector)decoder.decodeObject(ITEMS_KEY);
        selectedItems = (Vector)decoder.decodeObject(SELECTEDITEMS_KEY);
        dirtyItems = (Vector)decoder.decodeObject(DIRTYITEMS_KEY);

        command = (String)decoder.decodeString(COMMAND_KEY);
        doubleCommand = (String)decoder.decodeString(DOUBLECOMMAND_KEY);

        target = (Target)decoder.decodeObject(TARGET_KEY);
        backgroundColor = (Color)decoder.decodeObject(BACKGROUNDCOLOR_KEY);

        rowHeight = decoder.decodeInt(ROWHEIGHT_KEY);

        allowsMultipleSelection = decoder.decodeBoolean(MULTSELECTION_KEY);
        allowsEmptySelection = decoder.decodeBoolean(EMPTYSELECTION_KEY);
        tracksMouseOutsideBounds = decoder.decodeBoolean(TRACKSMOUSE_KEY);
        enabled = decoder.decodeBoolean(ENABLED_KEY);

        if (decoder.versionForClassName("netscape.application.ListView") > 1) {
            transparent = decoder.decodeBoolean(TRANSPARENT_KEY);
        } else  {
            transparent = false;
        }
    }

    void _setupKeyboard() {
        removeAllCommandsForKeys();
        setCommandForKey(SELECT_NEXT_ITEM,KeyEvent.DOWN_ARROW_KEY,View.WHEN_SELECTED);
        setCommandForKey(SELECT_PREVIOUS_ITEM,KeyEvent.UP_ARROW_KEY,View.WHEN_SELECTED);
    }

    /** Return whether this view can become the selected view
      * when the user is moving from view to views with the keyboard
      * The default implementation returns false.
      *
      */
    public boolean canBecomeSelectedView() {
        if(isEnabled())
            return true;
        else
            return false;
    }

    /** Implementation of the FormElement interface
      *
      */
    public String formElementText() {
        if (selectedItem() != null) {
            return selectedItem().title();
        } else {
            return "";
        }
    }
}


