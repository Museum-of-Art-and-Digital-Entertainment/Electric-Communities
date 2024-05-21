// Menu.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass managing a collection of MenuItems.
  * There are two Menu types: top-level main
  * Menus and submenus (a menu invoked by a MenuItem). Both of these types
  * of Menus can be used for AWT-based native Menus as well as IFC View-based
  * pure java Menus. Top-level menus have an
  * associated java.awt.MenuBar, while submenus have an associated
  * java.awt.Menu. These are not utilized when the Menu is used within an
  * IFC View-based structure.
  * You generally create a new Menu and add MenuItems to it, configuring them
  * with submenus as appropriate. The following code creates a Menu with a
  * single MenuItem containing a single-item submenu:
  * <pre>
  *     menu = new Menu(true);
  *     menuItem = menu.addItemWithSubmenu("Menu One");
  *     menuItem.submenu().addItem("Item One", command, target);
  * </pre>
  * Once a Menu structure is created, it can be added to a MenuView within
  * its Constructor method, or by calling <b>setMenu</b> on an existing
  * MenuView. This MenuView can then be added to an InternalWindow or
  * ExternalWindow with <b>setMenuView</b>. Additionally, you can add
  * this Menu directly to an ExternalWindow with <b>setMenu</b>, and
  * this will create an AWT-based native Menu on the top edge of the
  * Window. The following code adds the same Menu created above to an
  * existing ExternalWindow through both mechanisms:
  * <pre>
  *     externalWindow.setMenu(menu);
  *
  *     menuView = new MenuView(menu);
  *     menuView.sizeToMinSize();
  *     externalWindow.setMenuView(menuView);
  * </pre>
  *
  * <I><b>Note:</b> The Menu class does not support the insertion of a
  * submenu-less MenuItem into a top-level AWT-based Menu.</i>
  *
  * @see MenuItem, MenuView
  * @note 1.0 completely rewritten
  * @note 1.0 new method addItemAt taking a MenuItem description and index
  * @note 1.0 handleCommandKeyEvent() checks if Control modifier is down
  */

public class Menu implements Codable {
    java.awt.Menu       awtMenu;
    java.awt.MenuBar    awtMenuBar;
    Application         application;
    Vector              items;
    MenuItem            superitem, prototypeItem;
    Border              border;
    Color               backgroundColor;
    boolean             transparent = false;
    MenuView            menuView = null;

    final static String    ITEMS_KEY = "items",
                           SUPERITEM_KEY = "superitem",
                           PROTOTYPEITEM_KEY = "prototypeItem",
                           BORDER_KEY = "border",
                           BACKGROUNDCOLOR_KEY = "backgroundColor",
                           TRANSPARENT_KEY = "transparent";


    /** Constructs a Menu.
      * This Menu will be top-level by default.
      */
    public Menu() {
        this(true);
    }

    /** Constructs a Menu.
      * If <b>isTopLevel</b> is <b>true</b>, this Menu will be a top-level
      * Menu.
      * This denotation is critical for AWT-based native Menus, and is
      * not necessary with IFC View-based Menus.
      */
    public Menu(boolean isTopLevel) {
        super();
        MenuItem        protoItem;

        items = new Vector();

        backgroundColor = Color.lightGray;
        setBorder(new MenuBorder(this));

        if (isTopLevel) {
            awtMenuBar = new java.awt.MenuBar();
        } else {
            awtMenu = new java.awt.Menu("");
        }

        protoItem = new MenuItem();
        setPrototypeItem(protoItem);
    }

    /** Returns <b>true</b> if the Menu is a top-level menu.
      */
    boolean isTopLevel() {
        if (superitem != null && superitem.supermenu() != null) {
            return false;
        }
        return true;
    }

    /** Sets the MenuItem that contains this Menu. If this Menu is a top-level
      * Menu, this variable is null. You should never call this method
      * directly.
      */
    void setSuperitem(MenuItem item) {
        superitem = item;
    }

    /** Returns the MenuItem that contains this Menu. If this Menu is a
      * top-level menu, this varaible is null.
      * @see #setSuperitem
      */
    MenuItem superitem() {
        return superitem;
    }

    /** Sets the prototype MenuItem for this Menu.  Whenever the Menu needs a
      * new MenuItem, it clones the prototype MenuItem.
      */
    public void setPrototypeItem(MenuItem prototype) {
        prototypeItem = prototype;
    }

    /** Returns the Menu's prototype MenuItem.
      * @see #setPrototypeItem
      */
    public MenuItem prototypeItem() {
        return prototypeItem;
    }

    /** Sets the Color drawn behind transparent MenuItems, and any area in
      * the Menu not covered by MenuItems. This has no effect on AWT-based
      * native Menus.
      *
      */
    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    /** Returns the background color.
      * @see #setBackgroundColor
      *
      */
    public Color backgroundColor() {
        return backgroundColor;
    }

    /** Sets the Menu's Border. The Menu draws this Border around its smaller
      * inactive state and around its window when a MenuItem's submenu is
      * active. You can customize a Menu's look by setting a different Border.
      * This has no effect on AWT-based native Menus.
      *
      */
    public void setBorder(Border aBorder) {
        if (aBorder == null) {
            border = EmptyBorder.emptyBorder();
        } else {
            border = aBorder;
        }
    }

    /** Returns the Menu's Border.
      * @see #setBorder
      *
      */
    public Border border() {
        return border;
    }

    /** Sets the Menu to be transparent or opaque. This has no effect on
      * AWT-based native Menus.
      *
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
    }

    /** Overridden to return <b>true</b> if the Menu is transparent.
      * @see #setTransparent
      *
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Creates a new MenuItem with characteristics derived from
      * the prototype. If <b>isCheckbox</b> is true, this will be a checkbox
      * MenuItem. This MenuItem is not added to the Menu.
      */
    MenuItem createItem(boolean isCheckbox) {
        MenuItem menuItem;

        menuItem = (MenuItem)prototypeItem().clone();

        if (!isCheckbox) {
            menuItem.foundationMenuItem = new FoundationMenuItem("",
                                                                 menuItem);
        } else {
            menuItem.foundationMenuItem =
                new FoundationCheckMenuItem("", menuItem);
            menuItem.setCheckedImage(Bitmap.bitmapNamed(
                               "netscape/application/RadioButtonOn.gif"));
            menuItem.setUncheckedImage(Bitmap.bitmapNamed(
                               "netscape/application/RadioButtonOff.gif"));
            menuItem.setImage(menuItem.uncheckedImage());
            menuItem.setSelectedImage(menuItem.uncheckedImage());
        }

        // ALERT.  Must set Font because the new FoundationMenuItem by
        // default has a null java.awt.Font.
        menuItem.setFont(prototypeItem().font());

        return menuItem;
    }

    /** Creates a new Menu for use as a MenuItem's submenu. Called by
      * <b>addItemWithSubmenu()</b>. Subclasses should override this method
      * to return a subclass instance.
      */
    protected Menu createMenuAsSubmenu() {
        Menu      menu;
        MenuItem  item;

        menu = new Menu(false);
        menu.setPrototypeItem(createItem(false));
        menu.setBackgroundColor(backgroundColor());
        //menu.setBorder(border);
        return menu;
    }

    /** Adds a new MenuItem, containing a submenu, to the end of this
      * Menu, with title <b>title</b>.
      */
    public MenuItem addItemWithSubmenu(String title) {
        MenuItem menuItem;
        Menu menu;

        menuItem = createItem(false);
        menuItem.setTitle(title);

        menu = createMenuAsSubmenu();
        menuItem.setSubmenu(menu);
        addItemAt(menuItem, itemCount());
        return menuItem;
    }

    /** Adds a new MenuItem to the end of this Menu, with title <b>title</b>,
      * command <b>command</b> and Target <b>target</b>.
      */
    public MenuItem addItem(String title, String command, Target target) {
        return addItem(title, (char)0, command, target);
    }

    /** Adds a new MenuItem to the end of this Menu, with title <b>title</b>,
      * command <b>command</b> and Target <b>target</b>. If <b>isCheckbox</b>
      * is true, this adds a checkbox MenuItem.
      *
      */
    public MenuItem addItem(String title, String command, Target target,
                            boolean isCheckbox) {
        return addItem(title, (char)0, command, target, isCheckbox);
    }

    /** Adds a new MenuItem to the end of this Menu, with title <b>title</b>,
      * command key equivalent <b>key</b>, command <b>command</b> and Target
      * <b>target</b>.
      */
    public MenuItem addItem(String title, char key, String command,
                            Target target) {
        return addItem(title, key, command, target, false);
    }

    /** Adds a new MenuItem to the end of this Menu, with title <b>title</b>,
      * command key equivalent <b>key</b>, command <b>command</b> and Target
      * <b>target</b>. If <b>isCheckbox</b> is true, this will add a checkbox
      * MenuItem.
      *
      */
    public MenuItem addItem(String title, char key, String command,
                            Target target, boolean isCheckbox) {
        return addItemAt(title, key, command, target, isCheckbox, itemCount());
    }

    /** Adds a new MenuItem at the specified <b>index</b> in this Menu, with
      * title <b>title</b>,
      * command key equivalent <b>key</b>, command <b>command</b> and Target
      * <b>target</b>. If <b>isCheckbox</b> is true, this will add a checkbox
      * MenuItem.
      *
      */
    public MenuItem addItemAt(String title, char key,
                              String command, Target target,
                              boolean isCheckbox, int index) {
        MenuItem menuItem;

        menuItem = createItem(isCheckbox);
        menuItem.setSubmenu(null);
        menuItem.setSupermenu(this);
        menuItem.setCommandKey(key);
        menuItem.setTitle(title);
        menuItem.setTarget(target);
        menuItem.setCommand(command);
        addItemAt(menuItem, index);
        return menuItem;
    }

    /** Adds a separator line to this Menu at the current position.
      *
      */
    public MenuItem addSeparator() {
        MenuItem  menuItem = null;

        menuItem = createItem(false);
        menuItem.setSeparator(true);
        addItemAt(menuItem, itemCount());
        return menuItem;
    }

    /** Returns the index of the specified MenuItem.
      */
    public int indexOfItem(MenuItem item) {
        return items.indexOf(item);
    }

    /** Returns the number of MenuItems this Menu contains.
      */
    public int itemCount() {
        return items.count();
    }

    /** Returns the MenuItem at <b>index</b>.
      */
    public MenuItem itemAt(int index) {
        return (MenuItem)items.elementAt(index);
    }

    /** Adds the MenuItem <b>menuItem</b> at <b>index</b>.
      */
    public void addItemAt(MenuItem menuItem, int index) {
        java.awt.Menu menu;
        int i;

        menuItem.setSupermenu(this);
        if (menuItem.hasSubmenu()) {
            menu = menuItem.submenu().awtMenu();

            menu.setLabel(menuItem.title());
            menu.setFont(AWTCompatibility.awtFontForFont(menuItem.font()));
            if (isTopLevel()) {
                if (awtMenuBar != null) {
                    awtMenuBar.add(menu);
                }
            } else {
                if (awtMenu != null) {
                    awtMenu.add(menu);
                }
            }
        } else {
            if (isTopLevel()) {
                // this will be an ugly hack to allow top level MenuItems
            } else {
                if (awtMenu != null) {
                    if (menuItem.isSeparator()) {
                        awtMenu.addSeparator();
                    } else {
                        awtMenu.add(menuItem.foundationMenuItem());
                    }
                }
            }
        }
        items.insertElementAt(menuItem, index);
        for (i = 0; i < itemCount(); i++) {
            itemAt(i).setTitle(itemAt(i).title());
        }
    }

    /** Removes <b>menuItem</b> from the Menu.
      */
    public void removeItem(MenuItem menuItem) {
        items.removeElement(menuItem);
        if (isTopLevel()) {
            awtMenuBar.remove(menuItem.foundationMenuItem());
        } else {
            awtMenu.remove(menuItem.foundationMenuItem());
        }
    }

    /** Removes the MenuItem at <b>index</b>.
      */
    public void removeItemAt(int index) {
        items.removeElementAt(index);
        if (isTopLevel()) {
            awtMenuBar.remove(index);
        } else {
            awtMenu.remove(index);
        }
    }

    /** Replaces the MenuItem at <b>index</b> with <b>menuItem</b>. This has
      * no effect on AWT-based native Menus.
      *
      */
    public void replaceItemAt(int index, MenuItem menuItem) {
        items.replaceElementAt(index, menuItem);
    }

    /** Replaces the MenuItem with the new MenuItem. This has no effect
      * on AWT-based native Menus.
      *
      */
    public void replaceItem(MenuItem item, MenuItem newItem) {
        int i;

        i = indexOfItem(item);
        if (i != -1) {
            replaceItemAt(i, newItem);
        }
    }

    /** Called by <b>mouseUp()</b> in MenuView so that selected MenuItems
      * can send
      * their commands.  The selected MenuItem is passed in as <b>data</b>,
      * so if it is null, no command is sent.  You should never call this
      * method directly.
      */
    public void performCommand(String command, Object data) {
        MenuItem  item;

        if (data != null) {
            item = (MenuItem)data;
            item.setState(!item.state());
            item.sendCommand();
        }
    }

    /** Returns the largest title width of all of the Menu's MenuItems.
      */
    int minItemWidth() {
        int          i, width, maxWidth;

        maxWidth = 0;
        for (i = 0; i < itemCount(); i++) {
            width = itemAt(i).minWidth();

            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    /** Examines all MenuItems for a command key equivalent.  If the Menu
      * finds a match, this method performs the corresponding command and
      * returns <b>true</b>.
      */
    public boolean handleCommandKeyEvent(KeyEvent event) {
        MenuItem     item;
        boolean      eventHandled = false;

        if (!event.isControlKeyDown()) {
            return false;
        }

        item = itemForKeyEvent(event);
        if (item != null) {
            item.sendCommand();
            eventHandled = true;
        }
        return eventHandled;
    }

    MenuItem itemForKeyEvent(KeyEvent event) {
        int      i;
        MenuItem item, menuItem = null;

        if (!event.isControlKeyDown()) {
            return null;
        }

        for (i = 0; i < itemCount() && menuItem == null; i++) {
            item = itemAt(i);
            if (!item.isEnabled()) {
                continue;
            }
            if (item.hasSubmenu()) {
                menuItem = item.submenu().itemForKeyEvent(event);
            } else {
                // ALERT.  this should really compare the event.key to
                // the MenuItem's commandkey as a char, not an int
                if ((int)item.commandKey() == event.key + 64) {
                    menuItem = item;
                }
            }
        }
        return menuItem;
    }

    /** Returns the java.awt.Menu associated with this Menu. This variable
      * is only set in submenus.
      * @see Menu#awtMenuBar
      */
    java.awt.Menu awtMenu() {
        return awtMenu;
    }

    /** Returns the java.awt.Menu associated with this Menu. This variable
      * is only set if this is a top-level Menu.
      * @see Menu#awtMenu
      */
    java.awt.MenuBar awtMenuBar() {
        return awtMenuBar;
    }

    /** Sets the application associated with this menu. This variable is
      * only set if this is a top-level Menu. You never call this method
      * directly, but rather through ExternalWindow in its setMenu method.
      * The Application is used by FoundationMenuItem to create an Event.
      */
    void setApplication(Application app) {
        application = app;
    }

    /** Returns the application associated with this menu. This variable is
      * only set if this is a top-level Menu.
      */
    Application application() {
        return application;
    }

    /** Describes the Menu class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Menu", 1);

        info.addField(ITEMS_KEY, OBJECT_TYPE);
        info.addField(SUPERITEM_KEY, OBJECT_TYPE);
        info.addField(PROTOTYPEITEM_KEY, OBJECT_TYPE);

        info.addField(BORDER_KEY, OBJECT_TYPE);
        info.addField(BACKGROUNDCOLOR_KEY, OBJECT_TYPE);

        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the Menu instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(ITEMS_KEY, items);
        encoder.encodeObject(SUPERITEM_KEY, superitem);
        encoder.encodeObject(PROTOTYPEITEM_KEY, prototypeItem);

        encoder.encodeObject(BORDER_KEY, border);
        encoder.encodeObject(BACKGROUNDCOLOR_KEY, backgroundColor);

        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);
    }

    /** Decodes the Menu instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        items = (Vector)decoder.decodeObject(ITEMS_KEY);
        superitem = (MenuItem)decoder.decodeObject(SUPERITEM_KEY);
        prototypeItem = (MenuItem)decoder.decodeObject(PROTOTYPEITEM_KEY);

        border = (Border)decoder.decodeObject(BORDER_KEY);
        backgroundColor = (Color)decoder.decodeObject(BACKGROUNDCOLOR_KEY);

        transparent = decoder.decodeBoolean(TRANSPARENT_KEY);
    }

    /** Finishes the Menu's unarchiving.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
