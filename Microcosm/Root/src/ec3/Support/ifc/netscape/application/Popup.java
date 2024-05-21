// Popup.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

// ALERT! List:
// Center item in display.
// get background color from the list.
// get rid of sizeToMinSize on list.
// call setRowHeight directly based on the size of the button.
// keep it from tracking the mouse outside the popup.
// make the list send an action on all mouse ups.
// implement Codable APIs
// jla - rules for sending item's command vs. popup's command aren't clear

/** View subclass that, when clicked, pops up a window containing a ListView
  * of PopupItems.  When the user selects a PopupItem, the Popup sends the
  * PopupItem's command to the Popup's target.  By default, the Popup creates
  * and displays PopupItems, but you can create and use your own ListItem
  * subclass.
  * @see PopupItem
  * @see ListView
  * @note 1.0 support for Keyboard UI
  * @note 1.0 added enable/disable support, archiving change
  * @note 1.0 updated minSize to do the right thing
  * @note 1.0 new method removeItemAt() taking an index
  * @note 1.0 setPopupWindow() now adds ContainerView to window
  * @note 1.0 Added FormElement interface for browser needs
  */

public class Popup extends View implements Target, FormElement {
    ListView popupList;
    Window popupWindow;
    ContainerView container;
    ListItem selectedItem,wasSelectedItem;
    Target target;
    Image image;
    boolean _showingPopupForKeyboard;
    boolean enabled = true;

    final static String         LISTVIEW_KEY = "popupList",
                                WINDOW_KEY = "popupWindow",
                                CONTAINER_KEY = "container",
                                SELECTEDITEM_KEY = "selectedItem",
                                TARGET_KEY = "target",
                                SELECTEDIMAGE_KEY = "selectedImage",
                                ENABLED_KEY = "enabled";

    /** Cause the popup to select the next available item.
      *
      */
    public final static String  SELECT_NEXT_ITEM = ListView.SELECT_NEXT_ITEM;

    /** Cause the popup to select the previous available item.
      *
      */
    public final static String  SELECT_PREVIOUS_ITEM = ListView.SELECT_PREVIOUS_ITEM;

    /** Cause the popup to popup, displaying all the available choices.
      *
      */
    public final static String  POPUP = "popup";

    /** Cause the popup to close and cancel any changes
      *
      */
    final static String CLOSE_POPUP_AND_CANCEL = "cancel";

    /** Cause the popup to close, commit and send command
      *
      */
    final static String CLOSE_POPUP_AND_COMMIT = "commit";

    /** Constructs an empty Popup.
      */
    public Popup() {
        this(0, 0, 0, 0);
    }

    /** Constructs an empty Popup with bounds <B>rect</B>.  This Rect
      * defines the bounds of the inactive (not popped-up) Popup. When active,
      * the Popup grows vertically to fully display its items.
      */
    public Popup(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs an empty Popup with the given bounds.  This rectangle
      * defines the bounds of the inactive (not popped-up) Popup. When active,
      * the Popup grows vertically to fully display its items.
      */
    public Popup(int x, int y, int width, int height) {
        super(x, y, width, height);

        ListView list;
        Window window;
        PopupItem prototype;

        if (false) {
            window = new ExternalWindow(Window.BLANK_TYPE);
        } else {
            InternalWindow internalWindow =
                           new InternalWindow(x, y, width, height);
            internalWindow.setType(InternalWindow.BLANK_TYPE);
            internalWindow.setLayer(InternalWindow.POPUP_LAYER);
            internalWindow._contentView.setTransparent(true);
            internalWindow.setScrollsToVisible(true);
            window = internalWindow;
        }

        container = new ContainerView(0, 0, width, height);
        container.setTransparent(true);
        container.setBorder(BezelBorder.raisedBezel());
        container.setVertResizeInstruction(HEIGHT_CAN_CHANGE);
        container.setHorizResizeInstruction(WIDTH_CAN_CHANGE);

        list = new ListView(0, 0, width, height);
        //window.addSubview(container);

        prototype = new PopupItem();
        prototype.setPopup(this);

        setPopupList(list);
        setPrototypeItem(prototype);
        setPopupWindow(window);
        setPopupImage(Bitmap.bitmapNamed(
                                    "netscape/application/PopupKnobH.gif"));
        _setupKeyboard();
    }

    /** Sets the prototype ListItem used by the Popup's ListView.
      */
    public void setPrototypeItem(ListItem item) {
        popupList.setPrototypeItem(item);
        if (item instanceof PopupItem) {
            ((PopupItem)item).setPopup(this);
        }
    }

    /** Returns the prototype ListItem used by the Popup's ListView.
      * @see ListView#setPrototypeItem
      */
    public ListItem prototypeItem() {
        return popupList.prototypeItem();
    }

    /** Removes all ListItems from the Popup.
      */
    public void removeAllItems() {
        popupList.removeAllItems();
    }

    /** Adds a ListItem with the given title and command to the Popup. Calls
      * <b>addItem()</b> on the Popup's ListView.
      * @see ListView#addItem
      */
    public ListItem addItem(String title, String command) {
        ListItem item;

        hidePopupIfNeeded();

        item = popupList.addItem();
        item.setTitle(title);
        item.setCommand(command);

        return item;
    }

    /** Removes the ListItem with title <b>title</b> from the Popup.
      */
    public void removeItem(String title) {
        ListItem    nextItem;
        int     i;

        if (title == null) {
            return;
        }

        i = popupList.count();
        while (i-- > 0) {
            nextItem = (ListItem)itemAt(i);
            if (title.equals(nextItem.title())) {
                hidePopupIfNeeded();
                popupList.removeItemAt(i);
                return;
            }
        }
    }

    /** Removes the ListItem at <b>index</b>.
      *
      */
    public void removeItemAt(int index) {
        if (popupList.count() > index)  {
            hidePopupIfNeeded();
            popupList.removeItemAt(index);
        }
    }

    /** Returns the index of the Popup's selected row.
      */
    public int selectedIndex() {
        int index;

        index = popupList.indexOfItem(selectedItem);
        if (index < 0 && popupList.count() > 0) {
            index = 0;
            selectItemAt(0);
        }

        return index;
    }

    /** Returns the Popup's selected ListItem.
      */
    public ListItem selectedItem() {
        int index;

        index = selectedIndex();
        if (index < 0)
            return null;

        return popupList.itemAt(index);
    }

    /** Selects a particular Popup item. When inactive, the Popup displays
      * the currently selected ListItem.
      */
    public void selectItem(ListItem item) {
        selectedItem = item;
        draw();
    }

    /** Calls <b>selectItem()</b> using the ListItem at the given row.
      */
    public void selectItemAt(int index) {
        selectItem(popupList.itemAt(index));
    }

    /** Returns the number of ListItems the Popup contains.
      */
    public int count() {
        return popupList.count();
    }

    /** Returns the ListItem at the given row index.
      */
    public ListItem itemAt(int index) {
        return popupList.itemAt(index);
    }

    /** Sets the Popup's Border. The Popup draws this Border around its
      * smaller inactive state and around its window when active.  You can
      * customize a Popup's look by setting a different Border.
      */
    public void setBorder(Border aBorder) {
        container.setBorder(aBorder);
    }

    /** Returns the Popup's border.
      * @see #setBorder
      */
    public Border border() {
        return container.border();
    }

    /** Sets the ListView the Popup should use to maintain its ListItems.
      * You can customize a Popup's look by providing a custom ListView.
      */
    public void setPopupList(ListView list) {
        popupList = list;
        popupList.setTarget(this);
        popupList.setAllowsMultipleSelection(false);
        popupList.setAllowsEmptySelection(true);
        popupList.setTracksMouseOutsideBounds(false);
        container.addSubview(popupList);
    }

    /** Returns the Popup's ListView.
      * @see #setPopupList
      */
    public ListView popupList() {
        return popupList;
    }

    /** Sets the Window used to contain the active Popup. You can change the
      * active Popup's appearance by providing a custom Window.
      */
    public void setPopupWindow(Window window) {
        InternalWindow  internalWindow;

        popupWindow = window;
        if (window instanceof InternalWindow) {
            internalWindow = (InternalWindow)window;
            internalWindow.setScrollsToVisible(true);
            window.addSubview(container);
        }
    }

    /** Returns the active Popup's Window.
      * @see #setPopupWindow
      */
    public Window popupWindow() {
        return popupWindow;
    }

    /** Sets the Image displayed by the selected ListItem.
      */
    public void setPopupImage(Image anImage) {
        image = anImage;
    }

    /** Returns the Image displayed by the selected ListItem.
      * @see #setPopupImage
      */
    public Image popupImage() {
        return image;
    }

    /** Sizes and positions the Popup's Window to accomodate its ListItems, and
      * positions the Popup's ListView within its Window.  Popup calls this
      * method before bringing its Window onscreen.
      */
    protected void layoutPopupWindow() {
        Border  border;
        Rect    windowRect;
        int     selectedIndex;

        // Need to handle 0 height!  ALERT!
        // popupList.sizeToMinSize();  ALERT!

        border = container.border();

        popupList.setRowHeight(bounds.height - border.heightMargin());
        popupList.setBounds(border.leftMargin(),
                            border.topMargin(),
                            bounds.width - border.widthMargin(),
                            popupList.rowHeight() * popupList.count());

        selectedIndex = selectedIndex();

        windowRect = Rect.newRect(0, 0, width(), height());
        convertRectToView(null, windowRect, windowRect);

        if (rootView().windowClipView() != null) {
            rootView().convertRectToView(rootView().windowClipView(),
                                         windowRect, windowRect);
        }

        popupWindow.setBounds(windowRect.x,
                              windowRect.y
                                  - selectedIndex * popupList.rowHeight(),
                              popupList.width() + border.widthMargin(),
                              popupList.height() + border.heightMargin());

        Rect.returnRect(windowRect);
    }

    /** Brings the Popup's Window onscreen. Popup calls this method after
      * calling <b>layoutPopupWindow()</b>, in response to a mouse down event
      * on the Popup's inactive state. This method actually pops up the Popup.
      */
    protected void showPopupWindow(MouseEvent event) {
        InternalWindow  internalWindow;
        ExternalWindow  externalWindow;

        if (popupWindow instanceof InternalWindow) {
            internalWindow = (InternalWindow)popupWindow;

            internalWindow.setRootView(rootView());
            Application.application().beginModalSessionForView(internalWindow);
        } else {
            externalWindow = (ExternalWindow)popupWindow;

            Application.application().beginModalSessionForView(
                                                externalWindow.rootView());
        }

        popupWindow.show();
        rootView().setMouseView(popupList);  // ALERT!

        // Should this dispatch through the queue ALERT!
        if(event != null)
            popupList.mouseDown(convertEventToView(popupList, event));
        else {
            popupList.selectItem(selectedItem());
            rootView().makeSelectedView(popupList);
        }
    }

    /** Catches mouse events on the Popup's inactive "button". Calls
      * <b>layoutPopupWindow()</b> followed by <b>showPopupWindow()</b>.
      */
    public boolean mouseDown(MouseEvent event) {
        if (!isEnabled()) {
            return false;
        }

        layoutPopupWindow();
        showPopupWindow(event);

        return true;
    }

    /** Enables or disables the Popup.
      *
      */
    public void setEnabled(boolean flag) {
        enabled = flag;
        setDirty(true);
    }

    /** Returns true if the Popup is enabled, false otherwise.
      *
      */
    public boolean isEnabled() {
        return enabled;
    }

    /** Returns <b>true</b> if the Popup is transparent.  A Popup is
      * transparent if its ListView is transparent.
      * @see #popupList
      */
    public boolean isTransparent() {
        return popupList.isTransparent();
    }

    /** Draws the inactive Popup.
      */
    public void drawView(Graphics g) {
        Border  border;
        Rect    itemRect;
        Color   color = null;

        border = container.border();

        if (selectedItem == null && popupList.selectedItem() == null) {
            selectItem(popupList.itemAt(0));
        }

        if (!popupList.isTransparent() && selectedItem != null &&
            selectedItem.isTransparent()) {
            g.setColor(popupList.backgroundColor());
            g.fillRect(0, 0, width(), height());
        }

        if (selectedItem != null) {
            itemRect = Rect.newRect(border.leftMargin(),
                                    border.topMargin(),
                                    bounds.width - border.widthMargin(),
                                    bounds.height - border.heightMargin());
            g.pushState();
            g.setClipRect(itemRect);
            if (!isEnabled()) {
                color = selectedItem.textColor();
                selectedItem.setTextColor(Color.gray);
            }
            selectedItem.drawInRect(g, itemRect);
            if (!isEnabled()) {
                selectedItem.setTextColor(color);
            }
            g.popState();

            Rect.returnRect(itemRect);
        }

        border.drawInRect(g, 0, 0, width(), height());
    }

    /** Sets the Popup's Target. The Popup sends its command to its Target when
      * the currently selected ListItem changes.
      */
    public void setTarget(Target newTarget) {
        target = newTarget;
    }

    /** Returns the Popup's Target.
      * @see #setTarget
      */
    public Target target() {
        return target;
    }

    /** Sets the Popup's command. The Popup sends this command to its Target
      * if the selected ListItem does not have a command.
      */
    public void setCommand(String newCommand) {
        popupList.setCommand(newCommand);
    }

    /** Returns the Popup's command.
      * @see #setCommand
      */
    public String command() {
        return popupList.command();
    }

    /** Sends a command to the Popup's Target.  This command is either the
      * selected ListItem's command, or the Popup's command (if the ListItem
      * has no command).
      */
    public void sendCommand() {
        if (target != null) {
            String realCommand = null;

            if (selectedItem != null)
                realCommand = selectedItem.command();

            if (realCommand == null)
                realCommand = command();

            target.performCommand(realCommand, this);
        }
    }

    /** Responds to a message from its ListView that the user has selected a
      * different ListItem.  Calls <b>sendCommand()</b> and hides the
      * Popup Window.
      */
    public void performCommand(String command, Object data) {
        InternalWindow  internalWindow;
        ExternalWindow  externalWindow;

        if(SELECT_NEXT_ITEM.equals(command)) {
            selectNextItem(true);
        } else if(SELECT_PREVIOUS_ITEM.equals(command)) {
            selectNextItem(false);
        } else if(POPUP.equals(command)) {
            layoutPopupWindow();
            wasSelectedItem = selectedItem();
            _showingPopupForKeyboard = true;
            showPopupWindow(null);
            _setupKeyboardToClosePopup(true);
        } else {
            if(showingPopupForKeyboard()) {
                boolean shouldClose = false;
                if(CLOSE_POPUP_AND_CANCEL.equals(command)) {
                    selectItem(wasSelectedItem);
                    shouldClose = true;
                } else if(CLOSE_POPUP_AND_COMMIT.equals(command)) {
                    selectItem(popupList.selectedItem());
                    if(popupList.selectedItem() != null)
                        sendCommand();
                    shouldClose = true;
                }

                if(shouldClose) {
                    popupList.disableDrawing();
                    popupList.deselectItem(popupList.selectedItem());
                    popupList.reenableDrawing();
                    popupWindow.hide();

                    if (popupWindow instanceof InternalWindow) {
                        internalWindow = (InternalWindow)popupWindow;

                        Application.application().endModalSessionForView(internalWindow);
                    } else {
                        externalWindow = (ExternalWindow)popupWindow;

                        Application.application().endModalSessionForView(
                                        externalWindow.rootView());
                    }
                    _setupKeyboardToClosePopup(false);
                    _showingPopupForKeyboard = false;
                }
            } else {
                if (popupList.selectedItem() != null) {
                    selectedItem = popupList.selectedItem();
                }

                if (selectedItem != null) {
                    sendCommand();
                }

                popupList.disableDrawing();
                popupList.deselectItem(selectedItem);
                popupList.reenableDrawing();
                popupWindow.hide();

                if (popupWindow instanceof InternalWindow) {
                    internalWindow = (InternalWindow)popupWindow;

                    Application.application().endModalSessionForView(internalWindow);
                } else {
                    externalWindow = (ExternalWindow)popupWindow;

                    Application.application().endModalSessionForView(
                                         externalWindow.rootView());
                }
                _setupKeyboardToClosePopup(false);
            }
        }

        draw();
    }

    public Size minSize()   {
        int w, h;
        int bw, bh;
        int iw, ih;

        bw = 0;
        bh = 0;
        if(container.border() != null)  {
            bw = container.border().widthMargin();
            bh = container.border().heightMargin();
        }

        w = 0;
        h = 0;
        if(popupList != null)   {
            w = popupList.minItemWidth();
            h = popupList.minItemHeight();
        }

        iw = 0;
        ih = 0;
        if(image != null)   {
            iw = image.width();
            ih = image.height();
        }

        // We want the higher of the row height or the image height
        // if imageHieght is less than height, we just want h, otherwise
        // we need to add the difference.
        if(ih < h)  {
            ih = 0;
        } else {
            ih = h - ih;
        }

        return new Size(bw + w + iw, bh + h + ih);
    }

    /** Describes the Popup class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.Popup", 2);
        info.addField(LISTVIEW_KEY, OBJECT_TYPE);
        info.addField(WINDOW_KEY, OBJECT_TYPE);
        info.addField(CONTAINER_KEY, OBJECT_TYPE);
        info.addField(SELECTEDITEM_KEY, OBJECT_TYPE);
        info.addField(TARGET_KEY, OBJECT_TYPE);
        info.addField(SELECTEDIMAGE_KEY, OBJECT_TYPE);

        info.addField(ENABLED_KEY, BOOLEAN_TYPE);
    }

    /** Archives the Popup instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);
        encoder.encodeObject(LISTVIEW_KEY, popupList);
        encoder.encodeObject(WINDOW_KEY, popupWindow);
        encoder.encodeObject(CONTAINER_KEY, container);
        encoder.encodeObject(SELECTEDITEM_KEY, selectedItem);
        encoder.encodeObject(TARGET_KEY, target);
        encoder.encodeObject(SELECTEDIMAGE_KEY, image);

        encoder.encodeBoolean(ENABLED_KEY, enabled);
    }

    /** Unarchives the Popup instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);
        popupList = (ListView)decoder.decodeObject(LISTVIEW_KEY);
        popupWindow = (Window)decoder.decodeObject(WINDOW_KEY);
        container = (ContainerView)decoder.decodeObject(CONTAINER_KEY);
        selectedItem = (ListItem)decoder.decodeObject(SELECTEDITEM_KEY);
        target = (Target)decoder.decodeObject(TARGET_KEY);
        image = (Image)decoder.decodeObject(SELECTEDIMAGE_KEY);

        if (decoder.versionForClassName("netscape.application.Popup") > 1) {
            enabled = (boolean)decoder.decodeBoolean(ENABLED_KEY);
        }
    }

    void _setupKeyboard() {
        removeAllCommandsForKeys();
        setCommandForKey(SELECT_NEXT_ITEM,KeyEvent.DOWN_ARROW_KEY,View.WHEN_SELECTED);
        setCommandForKey(SELECT_PREVIOUS_ITEM,KeyEvent.UP_ARROW_KEY,View.WHEN_SELECTED);
        setCommandForKey(POPUP,KeyEvent.RETURN_KEY,View.WHEN_SELECTED);
        setCommandForKey(POPUP,' ',View.WHEN_SELECTED);
    }

    void selectNextItem(boolean forward) {
        int index = selectedIndex();
        int count = count();

        if(forward && index < (count-1))
            selectItemAt(index+1);
        else if(!forward && index > 0)
            selectItemAt(index-1);
        if(selectedItem() != null)
            sendCommand();
    }


    /** Return whether this view can become the selected view
      * when the user is moving from view to views with the keyboard
      * Popup's implementation returns true
      *
      */
    public boolean canBecomeSelectedView() {
        return true;
    }


    void _setupKeyboardToClosePopup(boolean doIt) {
        if(doIt) {
            setCommandForKey(CLOSE_POPUP_AND_CANCEL,KeyEvent.ESCAPE_KEY,View.ALWAYS);
            setCommandForKey(CLOSE_POPUP_AND_COMMIT,KeyEvent.RETURN_KEY,View.ALWAYS);
        } else {
            removeCommandForKey(KeyEvent.ESCAPE_KEY);
            setCommandForKey(POPUP,KeyEvent.RETURN_KEY,View.WHEN_SELECTED);
        }
    }

    protected void ancestorWillRemoveFromViewHierarchy(View view)   {
        super.ancestorWillRemoveFromViewHierarchy(view);
        hidePopupIfNeeded();
    }

    /** @private */
    public void hidePopupIfNeeded() {
        if(showingPopupForKeyboard()) {
            performCommand(CLOSE_POPUP_AND_CANCEL, this);
        }
    }

    /** @private */
    public boolean showingPopupForKeyboard()    {
        return _showingPopupForKeyboard;
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

