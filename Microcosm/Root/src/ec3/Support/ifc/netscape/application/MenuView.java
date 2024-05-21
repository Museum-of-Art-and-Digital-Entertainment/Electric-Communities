// MenuView.java
// By Ned Etcode
// Copyright 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** View subclass managing a Menu. There are two types of MenuViews,
  * <b>HORIZONTAL</b> and <b>VERTICAL</b>. You set this type with the
  * <b>setType</b> method to tell the MenuView how it should arrange
  * its Menu's MenuItems for displaying. MenuViews arrange themselves
  * hierarchically to mirror the structure of their respective Menus, using
  * their <b>owner</b> and <b>child</b> instance variables for traversing.
  * Thus, a top-level
  * MenuView will have a null owner, and the lowest showing MenuView will
  * have a null child. When
  * a MenuView receives a MouseEvent that should show a submenu, a new
  * MenuView is created with its Menu data set appropriately, and then that
  * MenuView is
  * instructed to show. Additionally, the behavior of
  * MouseEvents on the MenuView differs if it manages a top-level Menu. Note
  * that MenuViews manage an InternalWindow that they use to draw into,
  * and this
  * Window is never shown in MenuViews that are designated as static
  * "menu bar" style MenuViews. You usually create a new MenuView with a
  * given Menu, and then set it on a Window with <b>setMenuView</b>. These
  * MenuViews must own a top-level Menu, and are usually of type
  * <b>HORIZONTAL</b>.
  *
  * @see Menu, MenuItem
  *
  * @note 1.0 added support for command key equivalents
  * @note 1.0 added protected method createMenuWindow
  */

public class MenuView extends View {
    Menu                menu;
    MenuItem            selectedItem;
    public MenuView     owner, child;
    InternalWindow      menuWindow;
    int                 type, itemHeight = 17;  // ALERT
    boolean             transparent = false;

    final static String        MENU_KEY = "menu",
                               OWNER_KEY = "owner",
                               CHILD_KEY = "child",
                               MENUWINDOW_KEY = "menuWindow",
                               MENUITEMHEIGHT_KEY = "itemHeight",
                               TYPE_KEY = "type",
                               TRANSPARENT_KEY = "transparent";


    public static final int    HORIZONTAL = 0;
    public static final int    VERTICAL = 1;

    /** Constructs a MenuView with origin (0, 0) and zero width and height.
      * This MenuView will create its own top-level Menu, will be of type
      * HORIZONTAL, and will have no owner.
      */
    public MenuView() {
        this(0, 0, 0, 0, null, null);
    }

    /** Constructs a MenuView with origin (0, 0) and zero width and height.
      * This MenuView will use <b>aMenu</b> to define its structure, and will
      * determine its type by whether or not <b>aMenu</b> is top-level. This
      * MenuView's owner will be null.
      */
    public MenuView(Menu aMenu) {
        this(0, 0, 0, 0, aMenu, null);
    }

    /** Constructs a MenuView with bounds (<b>x</b>, <b>y</b>, <b>width</b>,
      * </b>height</b>). This MenuView will create its own top-level Menu,
      * will be of type HORIZONTAL, and will have no owner.
      */
    public MenuView(int x, int y, int width, int height) {
        this(x, y, width, height, null, null);
    }

    /** Constructs a MenuView with bounds (<b>x</b>, <b>y</b>, <b>width</b>,
      * </b>height</b>). This MenuView will use <b>aMenu</b> to define
      * its structure, and will determine its type by whether or not
      * <b>aMenu</b> is top-level. This MenuView's owner will be null.
      */
    public MenuView(int x, int y, int width, int height, Menu aMenu) {
        this(x, y, width, height, aMenu, null);
    }

    /** Constructs a MenuView with bounds (<b>x</b>, <b>y</b>, <b>width</b>,
      * </b>height</b>). This MenuView will use <b>aMenu</b> to define
      * its structure, and will determine its type by whether or not
      * <b>aMenu</b> is top-level. This MenuView will have <b>anOwner</b>
      * as its owner.
      */
    public MenuView(int x, int y, int width, int height, Menu aMenu,
                    MenuView anOwner) {
        super(x, y, width, height);
        if (aMenu != null) {
            menu = aMenu;
        } else {
            menu = new Menu(true);
        }
        owner = anOwner;

        menuWindow = createMenuWindow();
        menuWindow.addSubview(this);

        if (menu.isTopLevel()) {
            type = HORIZONTAL;
            setHorizResizeInstruction(WIDTH_CAN_CHANGE);
        } else {
            type = VERTICAL;
        }

        // ALERT!  This is the little hack that allows Menus to know
        // about their MenuViews, so that MenuItems can tell the MenuViews
        // to redraw them, and so MenuBorders know how to draw.
        menu.menuView = this;
    }

    /** Creates the InternalWindow that is used by this MenuView for
      * displaying its Menu's MenuItems. Note that toplevel MenuViews never
      * use an InternalWindow for drawing, so the returned Object is
      * ignored. Subclasses can override this
      * method to return their own custom subclass of InternalWindow.
      */
    protected InternalWindow createMenuWindow() {
        InternalWindow window;

        window = new InternalWindow(0, 0, 0, 0);
        window.setType(InternalWindow.BLANK_TYPE);
        window.setLayer(InternalWindow.IGNORE_WINDOW_CLIPVIEW_LAYER+11);
        window.setCanBecomeMain(false);
        window._contentView.setTransparent(true);
        window.setScrollsToVisible(true);
        return window;
    }

    /** Sets this MenuView to be of type <b>aType</b>, either <b>HORIZONTAL</b>
      * or <b>VERTICAL</b>.
      */
    public void setType(int aType) {
        type = aType;
    }

    /** Returns this MenuView's type.
      * @see #setType
      */
    public int type() {
        return type;
    }

    /** Sets the Menu this MenuView owns.
      */
    public void setMenu(Menu theMenu) {
        menu = theMenu;
    }

    /** Returns the Menu this MenuView owns.
      */
    public Menu menu() {
        return menu;
    }

    /** Sets <b>menuView</b> as the owner of this MenuView.  A MenuView's
      * owner is the MenuView that hierarchically displays this MenuView.
      */
    public void setOwner(MenuView menuView) {
        owner = menuView;
    }

    /** Returns the owner of this MenuView.
      * @see #setOwner
      */
    public MenuView owner() {
        return owner;
    }

    /** Returns the background Color of the MenuView's Menu.
      */
    public Color backgroundColor() {
        return menu.backgroundColor();
    }

    /** Returns the Border of the MenuView's Menu.
      */
    public Border border() {
        return menu.border();
    }

    /** Sets the MenuView to be transparent or opaque. This will also set the
      * transparency for the InternalWindow this MenuView draws into.
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
        menuWindow.setTransparent(flag);
    }

    /** Overridden to return <b>true</b> if the MenuView is transparent.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Sets the height of each MenuItem in the MenuView. Each MenuItem has
      * the same height. If <b>height</b> is 0, sets the height to
      * <b>minItemHeight()</b>.
      * @see #minItemHeight
      */
    public void setItemHeight(int height) {
        if (height > 0)
            itemHeight = height;
        else
            itemHeight = minItemHeight();
    }

    /** Returns the MenuView's MenuItem height.
      * @see #setItemHeight
      */
    public int itemHeight() {
        if (itemHeight > 0)
            return itemHeight;

        setItemHeight(minItemHeight());
        return itemHeight;
    }

    /** Returns the largest <b>minHeight()</b> of all of the MenuView's
      * MenuItems.
      */
    public int minItemHeight() {
        int    i, minHeight, height, count;

        minHeight = 0;
        count = menu.itemCount();
        for (i = 0; i < count; i++) {
            height = menu.itemAt(i).minHeight();

            if (height > minHeight) {
                minHeight = height;
            }
        }
        return minHeight;
    }

    /** Returns the largest title width of all of the MenuView's MenuItems.
      */
    public int minItemWidth() {
        int          i, width, maxWidth;

        maxWidth = 0;
        for (i = 0; i < menu.itemCount(); i++) {
            width = menu.itemAt(i).minWidth();

            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    /** Overridden to return a Size consisting of the minimum width necessary
      * to accommodate all the MenuItems and the <b>itemHeight()</b>.
      * This Size will reflect whether or not this MenuView is of type
      * <b>HORIZONTAL</b> or <b>VERTICAL</b>.
      */
    public Size minSize() {
        int width, height, count, i;
        MenuItem item;

        count = menu.itemCount();

        if (type == HORIZONTAL) {
            width = 0;
            for (i = 0; i < count; i++) {
                item = menu.itemAt(i);
                width += item.minWidth();
            }
            height = itemHeight();
        } else {
            width = minItemWidth();
            height = count * itemHeight();
        }

        width += border().widthMargin();
        height += border().heightMargin();

        return new Size(width, height);
    }

    /** Returns the MenuItem at the coordinate (<b>x</b>, <b>y</b>).
      */
    public MenuItem itemForPoint(int x, int y) {
        int       i, index;
        MenuItem  item;
        Rect      itemRect;

        index = -1;
        for (i = 0; i < menu.itemCount(); i++) {
            itemRect = rectForItemAt(i);
            if (itemRect.contains(x, y)) {
                index = i;
                break;
            }
        }

        if (index >= 0) {
            item = menu.itemAt(index);
        } else {
            item = null;
        }
        return item;
    }

    /** Returns the row of the selected MenuItem. If no MenuItem is selected,
      * returns -1.
      */
    public int selectedIndex() {
        MenuItem item;

        item = selectedItem();

        if (item == null) {
            return -1;
        }

        return menu.indexOfItem(item);
    }

    /** Returns the selected MenuItem. If no MenuItem is selected, returns
      * null.
      */
    public MenuItem selectedItem() {
        return selectedItem;
    }

    /** Selects <b>item</b> and deselects any other selected MenuItem.
      */
    public void selectItem(MenuItem item) {
        if (!item.isEnabled()) {
            return;
        }

        if (selectedItem != item) {
            if (selectedItem != null) {
                selectedItem.setSelected(false);
                addDirtyRect(rectForItemAt(menu.indexOfItem(selectedItem)));
            }
            item.setSelected(true);
            selectedItem = item;
            addDirtyRect(rectForItemAt(menu.indexOfItem(item)));
        }
    }

    /** Deselects any MenuItem that might be currently selected.
      */
    public void deselectItem() {
        if (selectedItem != null) {
            selectedItem.setSelected(false);
            addDirtyRect(rectForItemAt(menu.indexOfItem(selectedItem)));
            selectedItem = null;
        }
    }

    /** Returns the Rect that describes the MenuItem at <b>index</b>.
      */
    public Rect rectForItemAt(int index) {
        Rect      itemRect;
        MenuItem  item;
        int       i, x, y, width;

        if (index < 0 || index >= menu.itemCount())
            return null;

        x = 0;
        if (type == HORIZONTAL) {
            y = 0;
            for (i = 0; i < index; i++) {
                item = menu.itemAt(i);
                x += item.minWidth();
            }
            width = menu.itemAt(index).minWidth();
        } else {
            y = itemHeight() * index;
            width = bounds.width - border().widthMargin();
        }

        x += border().leftMargin();
        y += border().topMargin();

        itemRect = Rect.newRect(x, y, width, itemHeight());
        return itemRect;
    }

    /** Returns a Rect describing the MenuView's "interior," the MenuView's
      * bounds minus its left, right, top and bottom borders.
      */
    public Rect interiorRect() {
        Rect interiorRect;

        interiorRect = new Rect(border().leftMargin(),
                                border().topMargin(),
                                bounds.width - border().widthMargin(),
                                bounds.height - border().heightMargin());
        return interiorRect;
    }

    /** Calls <b>draw()</b> with the Rect for the MenuItem at the given row
      * index.
      */
    public void drawItemAt(int index) {
        Rect rect = rectForItemAt(index);
        draw(rect);
    }

    /** Returns a MenuView with its structure defined by <b>theMenu</b>.
      * Subclasses of MenuView should override this method to return
      * their special subclass.
      */
    protected MenuView createMenuView(Menu theMenu) {
        return new MenuView(0, 0, 0, 0, theMenu, this);
    }

    /** Climbs the ownership tree to find the top-level MenuView.  Top-level
      * MenuViews have a null owner, as well as a top-level Menu.
      * @see #setOwner
      */
    MenuView mainOwner() {
        MenuView   myOwner;

        myOwner = owner;
        while (myOwner.owner() != null) {
            myOwner = myOwner.owner();
        }

        return myOwner;
    }

    /** Overridden from View to collect <i>all</i> KeyStrokes and forward
      * them to the Menu for processing.
      */
    boolean performCommandForKeyStroke(KeyStroke aKeyStroke, int condition) {
        if (Application.application().activeMenuViews.count() > 0) {
            return false;
        }
        if (window() != null) {
            if (!this.descendsFrom(rootView()._mainWindow)) {
                return false;
            }
        }
        if (condition == View.ALWAYS || condition == View.WHEN_IN_MAIN_WINDOW) {
            KeyEvent event = new KeyEvent((long)0, aKeyStroke.key,
                                          aKeyStroke.modifiers, true);
            return menu.handleCommandKeyEvent(event);
        }
        return false;
    }

    /** Called by Application on any MenuViews that are
      * registered for notification.  This should only be called by
      * Application, and is intended only for top-level MenuViews.
      */
    void mouseWillDown(MouseEvent event) {
        Rect        rect;
        MenuView    myChild;
        MouseEvent  newEvent;
        RootView    firstRootView;

        firstRootView = Application.application().firstRootView();
        if (firstRootView != rootView()) {
            hideAll();
            return;
        }

        newEvent = rootView().convertEventToView(this, event);
        if (containsPoint(newEvent.x, newEvent.y)) {
            return;
        }

        if (selectedItem() != null) {
            myChild = child;
            while (myChild != null) {
                rect = new Rect(myChild.bounds);
                myChild.superview().convertRectToView(null, rect, rect);
                if (Rect.contains(rect.x, rect.y, rect.width, rect.height,
                                  event.x, event.y)) {
                    return;
                }
                myChild = myChild.child;
            }
        }

        hideAll();
    }

    void hideAll() {
        Application.application().removeActiveMenuView(this);
        if (child != null) {
            child.hide();
        }
        if (isVisible()) {
            hide();
        }
        deselectItem();
    }

    /** Overridden to receive mouse clicks.
      */
    public boolean mouseDown(MouseEvent event) {
        MenuItem  clickedItem;

        if (owner != null) {
            return true;
        }

        clickedItem = itemForPoint(event.x, event.y);
        if (clickedItem == null || !clickedItem.isEnabled()) {
            if (child != null) {
                child.hide();
            }
            deselectItem();
            return true;
        }

        if (selectedItem != null && clickedItem == selectedItem) {
            if (isVisible()) {
                return true;
            }
            Application.application().removeActiveMenuView(this);
            if (child != null) {
                child.hide();
                child = null;
            }
            deselectItem();
            return true;
        }

        selectItem(clickedItem);

        if (clickedItem.isEnabled() && clickedItem.hasSubmenu()) {
            MenuView menuView = createMenuView(clickedItem.submenu());
            menuView.show(rootView(), event);
            child = menuView;
        }

        return true;
    }

    /** Overridden to receive mouse drag events.
      */
    public void mouseDragged(MouseEvent event) {
        MenuItem        clickedItem;
        MouseEvent      newEvent;
        MenuView        menuView;
        int             i;


        clickedItem = itemForPoint(event.x, event.y);

        if (!Rect.contains(0, 0, width(), height(), event.x, event.y)) {
            clickedItem = null;
            if (owner != null && child == null ||
                       owner == null && child == null && isVisible()) {
                deselectItem();
            }
        }

        if (clickedItem != null && !clickedItem.isEnabled()) {
            return;
        }

        menuView = child;
        while (menuView != null && menuView.isVisible()) {
            Rect rect = new Rect(menuView.bounds);
            newEvent = convertEventToView(menuView, event);
            if (Rect.contains(rect.x, rect.y, rect.width, rect.height,
                              newEvent.x, newEvent.y)) {
                clickedItem = menuView.itemForPoint(newEvent.x, newEvent.y);
                if (clickedItem != null) {
                    menuView.mouseDragged(newEvent);
                    menuView.autoscroll(newEvent);
                    return;
                }
            } else if (menuView.child == null) {
                if (menuView.selectedItem() == null) {
                    menuView.autoscroll(newEvent);
                }
                menuView.deselectItem();
            }
            menuView = menuView.child;
        }

        if (clickedItem != null && clickedItem != selectedItem()) {
            if (selectedItem != null && selectedItem.hasSubmenu() &&
                    child != null && child.isVisible()) {
                child.hide();
                child = null;
            }
            selectItem(clickedItem);
            if (clickedItem.hasSubmenu()) {
                menuView = createMenuView(clickedItem.submenu());
                menuView.show(rootView(), event);
                child = menuView;
            }
        }
    }

    /** Overridden to receive mouse up events.
      */
    public void mouseUp(MouseEvent event) {
        int       i;
        MenuItem  clickedItem = null;
        MenuView  menuView;

        clickedItem = itemForPoint(event.x, event.y);
        if (owner == null && selectedItem == clickedItem) {
            if (isVisible() && selectedItem != null) {
                Application.application().removeActiveMenuView(this);
                hide();
                menu.performCommand("", clickedItem);
            } else {
                return;
            }
        }

        if (owner == null && clickedItem == null) {
            menuView = child;
            while (menuView.child != null) {
                menuView = menuView.child;
            }
            menuView.mouseUp(convertEventToView(menuView, event));
        }

        if (owner != null && selectedItem != null) {
            clickedItem = selectedItem;
            Application.application().removeActiveMenuView(mainOwner());
            hide();
            menuView = owner;
            while (menuView.owner() != null) {
                menuView.hide();
                menuView = menuView.owner();
            }
            menuView.hide();
            menu.performCommand("", clickedItem);
        }
    }

    /** Overridden to receive mouse entered events.
      */
    public void mouseEntered(MouseEvent event) {
        if ((owner == null && selectedItem != null) || isVisible()) {
            mouseDragged(event);
        }
    }

    /** Overridden to receive mouse moved events.
      */
    public void mouseMoved(MouseEvent event) {
        if ((owner == null && selectedItem != null) || isVisible()) {
            mouseDragged(event);
        }
    }

    /** Overridden to receive mouse exited events.
      */
    public void mouseExited(MouseEvent event) {
        if ((owner == null && selectedItem != null) || isVisible()) {
            mouseDragged(event);
        }
    }

    /** Overridden to draw this MenuView.  This handles both top-level and
      * submenu type MenuViews.
      */
    public void drawView(Graphics g) {
        Rect      itemRect, clipRect;
        MenuItem  item;
        int       i, count, width;
        boolean   flag;

        border().drawInRect(g, 0, 0, width(), height());

        count = menu.itemCount();

        width = 0;
        for (i = 0; i < count; i++) {
            item = menu.itemAt(i);
            itemRect = rectForItemAt(i);
            width += itemRect.width;

            if (!g.clipRect().intersects(itemRect)) {
                continue;
            }

            g.pushState();
            g.setClipRect(itemRect);

            if (!isTransparent()) {
                g.setColor(backgroundColor());
                g.fillRect(itemRect.x, itemRect.y, itemRect.width,
                           itemRect.height);
            }
            if (type == HORIZONTAL) {
                flag = false;
            } else {
                flag = true;
            }
            item.drawInRect(g, itemRect, flag);
            g.popState();

            Rect.returnRect(itemRect);
        }
        if (width < interiorRect().width && type == HORIZONTAL &&
               !isTransparent()) {
            g.setColor(backgroundColor());
            g.fillRect(interiorRect().x + width, interiorRect().y,
                       interiorRect().width - width, interiorRect().height);
        }
    }

    /** Shows the InternalWindow that holds this MenuView.  This method
      * is automatically called by this MenuView's owner when it should
      * show. You should not call this method on
      * a MenuView that has been added to a Window with <b>setMenuView</b>.
      * You should only call this method directly on top-level
      * context-sensitive MenuViews. In this case, <b>rootView</b> is the view
      * in which to show the InternalWindow containing the context-sensitive MenuView.
      * <b>event</b> should be in the coordinate system of <b>rootView</b> and
      * the (x, y) values of the event become the origin of the InternalWindow.
      */
    public void show(RootView rootView, MouseEvent event) {
        Rect     windowRect, itemRect;
        int      width, height, i, x, y;
        Menu     theMenu;
        MenuView menuView;

        if (menu.itemCount() == 0) {
            return;
        }

        width = minItemWidth() + border().widthMargin() + 20;   // ALERT!
        height = (itemHeight() * menu.itemCount()) + border().heightMargin();

        sizeTo(width, height);

        if (menu.superitem() != null) {
            theMenu = menu.superitem().supermenu();
        } else {
            theMenu = menu;
        }

        if (owner != null) {
            menuView = owner;
        } else {
            menuView = this;
        }

        windowRect = Rect.newRect(0, 0, width(), height());

        menuView.convertRectToView(null, windowRect, windowRect);

        if (rootView.windowClipView() != null && menuWindow.layer() !=
                              InternalWindow.IGNORE_WINDOW_CLIPVIEW_LAYER+11) {
            rootView.convertRectToView(rootView.windowClipView(),
                                       windowRect, windowRect);
        }

        itemRect = menuView.rectForItemAt(menuView.selectedIndex());
        if (itemRect == null) {
            itemRect = new Rect(0, 0, 0, 0);
        }

        if (theMenu.isTopLevel() && menuView.type == HORIZONTAL) {
            x = windowRect.x + itemRect.x - menuView.border().leftMargin();
            y = windowRect.y + menuView.height();
        } else if (menu.isTopLevel() && type == VERTICAL) {
            x = event.x;
            y = event.y;
        } else {
            x = windowRect.x + itemRect.width +
                      menuView.border().widthMargin() - 3;  // ALERT
            y = windowRect.y + itemRect.maxY() - itemRect.height -
                                  menuView.border().topMargin();
        }

        // ALERT!  If we have a context-sensitive top-level Menu, it has
        // horizontal resize instructions, so we have to counteract them
        int w = width();
        menuWindow.setBounds(x, y, width(), height());
        sizeTo(w, height());

        Rect.returnRect(windowRect);
        Rect.returnRect(itemRect);

        if (owner != null) {
            menuView = mainOwner();
        } else {
            menuView = this;
        }
        Application.application().addActiveMenuView(menuView);

        menuWindow.setRootView(rootView);
        menuWindow.show();
    }

    /** Hides the InternalWindow that holds this MenuView. This method is
      * automatically called by this MenuView's owner when it should
      * hide.
      * @see #show
      */
    public void hide() {
        MenuItem   item;
        MenuView   menuView;

        if (menuWindow.isVisible()) {
            menuWindow.hide();
        }

        if (selectedItem != null && selectedItem.hasSubmenu()) {
            child.hide();
            child = null;
        }
        deselectItem();
    }

    /** Returns <b>true</b> if this MenuView is visible by virtue of its
      * InternalWindow.
      */
    public boolean isVisible() {
        return menuWindow.isVisible();
    }

    /** Overriden to return <b>true</b>.
      */
    public boolean wantsAutoscrollEvents() {
        return true;
    }

    /** Magic autoscroller. Called in mouseDragged by top-most MenuView, when
      * it knows the MenuView in question should scroll. Taken from ListView.
      */
    void autoscroll(MouseEvent event) {
        Rect visibleRect;

        visibleRect = Rect.newRect();
        computeVisibleRect(visibleRect);

        if (!visibleRect.contains(event.x, event.y)) {
            if (event.y < visibleRect.y) {
                Rect tmpRect = Rect.newRect(visibleRect.x, event.y,
                                            visibleRect.width,
                                            itemHeight());
                scrollRectToVisible(tmpRect);
                Rect.returnRect(tmpRect);
            } else if (event.y > visibleRect.maxY()) {
                Rect tmpRect = Rect.newRect(visibleRect.x, event.y -
                                            itemHeight(),
                                            visibleRect.width,
                                            itemHeight());
                scrollRectToVisible(tmpRect);
                Rect.returnRect(tmpRect);
            }
        }
        Rect.returnRect(visibleRect);
    }

    /** Describes the MenuView class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.MenuView", 1);
        info.addField(MENU_KEY, OBJECT_TYPE);
        info.addField(OWNER_KEY, OBJECT_TYPE);
        info.addField(CHILD_KEY, OBJECT_TYPE);
        info.addField(MENUWINDOW_KEY, OBJECT_TYPE);

        info.addField(MENUITEMHEIGHT_KEY, INT_TYPE);
        info.addField(TYPE_KEY, INT_TYPE);

        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the MenuView instance.
      * @see Codable#decode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(MENU_KEY, menu);
        encoder.encodeObject(OWNER_KEY, owner);
        encoder.encodeObject(CHILD_KEY, child);
        encoder.encodeObject(MENUWINDOW_KEY, menuWindow);

        encoder.encodeInt(MENUITEMHEIGHT_KEY, itemHeight);
        encoder.encodeInt(TYPE_KEY, type);

        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);
    }

    /** Decodes the MenuView instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        menu = (Menu)decoder.decodeObject(MENU_KEY);
        owner = (MenuView)decoder.decodeObject(OWNER_KEY);
        child = (MenuView)decoder.decodeObject(CHILD_KEY);
        menuWindow = (InternalWindow)decoder.decodeObject(MENUWINDOW_KEY);

        itemHeight = (int)decoder.decodeInt(MENUITEMHEIGHT_KEY);
        type = (int)decoder.decodeInt(TYPE_KEY);

        transparent = (boolean)decoder.decodeBoolean(TRANSPARENT_KEY);
    }

    /** Finishes the MenuView instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        super.finishDecoding();
    }
}
