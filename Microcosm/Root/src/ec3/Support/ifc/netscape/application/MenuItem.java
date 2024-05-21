// MenuItem.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass representing a single item in a Menu. MenuItems are used
  * as both wrappers for AWT-based native MenuItems as well as for
  * IFC View-based pure
  * java Menus. Their behavior is determined by whether or not a given Menu
  * that holds the MenuItem
  * is set directly on an ExternalWindow with <b>setMenu</b>, or if a MenuView
  * holding a Menu (with the MenuItem)
  * is added to a Window with <b>setMenuView</b>. A MenuItem stores a
  * reference to a java.awt.MenuItem, as well as implementing
  * <b>drawInRect</b> for use in MenuViews.
  * A MenuItem may or may not have a submenu. In general, you don't
  * create MenuItems yourself, but instead use Menu's <b>addItem()</b> and
  * <b>addItemWithSubmenu()</b> methods.
  * @see Menu
  * @note 1.0 completely rewritten
  * @note 1.0 added support for MenuShortcuts in JDK 1.1.1
  */

public class MenuItem implements Codable, Cloneable, EventProcessor {
    Menu                  submenu, supermenu;
    java.awt.MenuItem     foundationMenuItem;
    String                command;
    String                title;
    Target                target;
    char                  commandKey;
    Font                  font;
    Image                 checkedImage, uncheckedImage;
    Image                 image, selectedImage;
    Color                 selectedColor, selectedTextColor;
    Color                 textColor, disabledColor;
    boolean               selected, separator, state, enabled;
    Object                data;

    final static String   SUBMENU_KEY = "submenu",
                          SUPERMENU_KEY = "supermenu",
                          COMMAND_KEY = "command",
                          TITLE_KEY = "title",
                          TARGET_KEY = "target",
                          COMMANDKEY_KEY = "commandKey",
                          FONT_KEY = "font",
                          CHECKEDIMAGE_KEY = "checkedImage",
                          UNCHECKEDIMAGE_KEY = "uncheckedImage",
                          IMAGE_KEY = "image",
                          SELECTEDIMAGE_KEY = "selectedImage",
                          SELECTEDCOLOR_KEY = "selectedColor",
                          SELECTEDTEXTCOLOR_KEY = "selectedTextColor",
                          TEXTCOLOR_KEY = "textColor",
                          DISABLEDCOLOR_KEY = "disabledColor",
                          SEPARATOR_KEY = "separator",
                          STATE_KEY = "state",
                          ENABLED_KEY = "enabled",
                          DATA_KEY = "data";

    /** Constructs an empty MenuItem.
      */
    public MenuItem() {
        this("", (char)0, null, null, false);
    }

    /** Constructs a MenuItem with the specified title, command, and
      * Target.
      */
    public MenuItem(String title, String command, Target target) {
        this(title, (char)0, command, target, false);
    }

    /** Constructs a MenuItem with the specified title, command key
      * equivalent, command, and Target.
      */
    public MenuItem(String title, char key, String command, Target target) {
        this(title, key, command, target, false);
    }

    /** Constructs a MenuItem with the specified title, command, and Target.
      * If <b>isCheckbox</b> is true, this will be a checkbox MenuItem.
      *
      */
    public MenuItem(String title, String command, Target target,
                    boolean isCheckbox) {
        this(title, (char)0, command, target, isCheckbox);
    }

    /** Constructs a MenuItem with the specified title, command key
      * equivalent, command, and Target. If <b>isCheckbox</b> is true,
      * this will be a checkbox MenuItem.
      *
      */
    public MenuItem(String title, char key, String command, Target target,
                    boolean isCheckbox) {
        super();

        commandKey = Character.toUpperCase(key);
        if (!isCheckbox) {
            foundationMenuItem = new FoundationMenuItem(title, this);
        } else {
            foundationMenuItem = new FoundationCheckMenuItem(title, this);
            setUncheckedImage(Bitmap.bitmapNamed(
                                 "netscape/application/RadioButtonOff.gif"));
            setCheckedImage(Bitmap.bitmapNamed(
                                 "netscape/application/RadioButtonOn.gif"));
            setImage(uncheckedImage);
            setSelectedImage(uncheckedImage);
        }
        setEnabled(true);
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setTitle(title);
        setTarget(target);
        setCommand(command);

        selectedColor = Color.white;
        textColor = Color.black;
        selectedTextColor = Color.black;
        disabledColor = Color.gray;
    }

    /** Clones the MenuItem. Menu adds addtional items by cloning its
      * prototype MenuItem.
      */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InconsistencyException(
                                this + ": clone() not supported :" + e);
        }
    }

    /** Returns the FoundationMenuItem associated with this MenuItem. You
      * should only be concerned with this FoundationMenuItem if you are
      * using AWT-based native Menus.
      */
    java.awt.MenuItem foundationMenuItem() {
        return foundationMenuItem;
    }

    /** Sets this MenuItem to be a separator if <b>isSeparator</b> is true.
      *
      */
    public void setSeparator(boolean isSeparator) {
        separator = isSeparator;
        if (isSeparator) {
            setEnabled(true);
        }
    }

    /** Returns <b>true</b> if this MenuItem is a separator, <b>false</b>
      * otherwise.
      *
      */
    public boolean isSeparator() {
        return separator;
    }

    /** Sets the MenuItem's data, a storage place for arbitrary data associated
      * with the MenuItem.
      */
    public void setData(Object data)    {
        this.data = data;
    }

    /** Returns the MenuItem's data.
      * @see #setData
      */
    public Object data()    {
        return this.data;
    }

    /** Sets this MenuItem to have the specified submenu. Note that in
      * order for a Menu structure to work properly, MenuItems should be
      * added directly to Menus with <b>addItem</b> and
      * <b>addItemWithSubmenur</b>. It is not sufficient to create a
      * MenuItem and call <b>setSubmenu</b> with a given Menu. You should
      * not call this method directly.
      */
    public void setSubmenu(Menu aMenu) {
        submenu = aMenu;
        if (submenu != null) {
            submenu.setSuperitem(this);
        }
    }

    /** Returns the MenuItem's submenu, <b>null</b> if it doesn't have one.
      */
    public Menu submenu() {
        return submenu;
    }

    /** Returns <b>true</b> if this MenuItem has a submenu.
      * @see #setSubmenu
      */
    public boolean hasSubmenu() {
        if (submenu != null) {
            return true;
        } else {
            return false;
        }
    }

    /** Sets the MenuItem's supermenu. Note that in
      * order for a Menu structure to work properly, MenuItems should be
      * added directly to Menus with <b>addItem</b> and
      * <b>addItemWithSubmenur</b>. It is not sufficient to create a
      * MenuItem and call <b>setSupermenu</b> with a given Menu. You should
      * not call this method directly.
      */
    public void setSupermenu(Menu aMenu) {
        supermenu = aMenu;
    }

    /** Returns the MenuItem's supermenu.
      * @see #setSupermenu
      */
    public Menu supermenu() {
        return supermenu;
    }

    /** Sets the MenuItem's command key equivalent.
      */
    public void setCommandKey(char key) {
        commandKey = Character.toUpperCase(key);
        setTitle(title);
    }

    /** Returns the MenuItem's command key equivalent, '\0' if it doesn't have
      * one.
      * @see #setCommandKey
      */
    public char commandKey() {
        return commandKey;
    }

    /** Sets the MenuItem's state if this is a checkbox MenuItem. This does
      * nothing if this is a regular MenuItem.
      *
      */
    public void setState(boolean aState) {
        state = aState;
        if (foundationMenuItem instanceof FoundationCheckMenuItem) {
            ((FoundationCheckMenuItem)foundationMenuItem).setState(state);
            if (state) {
                setImage(checkedImage());
                setSelectedImage(checkedImage());
            } else {
                setImage(uncheckedImage());
                setSelectedImage(uncheckedImage());
            }
        }
    }

    /** Returns the current state of the MenuItem. If this is not a checkbox
      * MenuItem, returns false.
      *
      */
    public boolean state() {
        if (foundationMenuItem instanceof FoundationCheckMenuItem) {
            //return ((FoundationCheckMenuItem)foundationMenuItem).getState();
            return state;
        }
        return false;
    }

    /** Sets the Image the MenuItem displays next to its title.
      * @see #setSelectedImage
      */
    public void setImage(Image theImage) {
        image = theImage;
    }

    /** Returns the Image the MenuItem displays next to its title.
      * @see #setImage
      *
      */
    public Image image() {
        return image;
    }

    /** Sets the Image the MenuItem displays next to its title when
      * selected.
      * @see #setImage
      *
      */
    public void setSelectedImage(Image theImage) {
        selectedImage = theImage;
    }

    /** Returns the Image the MenuItem displays next to its title when
      * selected.
      * @see #setSelectedImage
      */
    public Image selectedImage() {
        return selectedImage;
    }

    /** Sets the Image displayed on the MenuItem if it is a checkbox
      * MenuItem and its state is true.
      *
      */
    public void setCheckedImage(Image theImage) {
        checkedImage = theImage;
        if (foundationMenuItem instanceof FoundationCheckMenuItem) {
            if (state() == true) {
                setImage(checkedImage);
                setSelectedImage(checkedImage);
            }
        }
    }

    /** Returns the Image displayed on the MenuItem if it is a checkbox
      * MenuItem and its state is true.
      *
      */
    public Image checkedImage() {
        return checkedImage;
    }

    /** Sets the Image displayed on the MenuItem if it is a checkbox
      * MenuItem and its state is false.
      *
      */
    public void setUncheckedImage(Image theImage) {
        uncheckedImage = theImage;
        if (foundationMenuItem instanceof FoundationCheckMenuItem) {
            if (state() == false) {
                setImage(uncheckedImage);
                setSelectedImage(uncheckedImage);
            }
        }
    }

    /** Returns the Image displayed on the MenuItem if it is a checkbox
      * MenuItem and its state is false.
      *
      */
    public Image uncheckedImage() {
        return uncheckedImage;
    }

    /** Sets the color the MenuItem uses to draw its background when
      * selected.
      *
      */
    public void setSelectedColor(Color color) {
        selectedColor = color;
    }

    /** Returns the color the MenuItem uses to draw its background when
      * selected.
      *
      */
    public Color selectedColor() {
        return selectedColor;
    }

    /** Sets the color the MenuItem uses to draw its foreground text when
      * selected.
      *
      */
    public void setSelectedTextColor(Color color) {
        selectedTextColor = color;
    }

    /** Returns the color the MenuItem uses to draw its foreground text when
      * selected.
      *
      */
    public Color selectedTextColor() {
        return selectedTextColor;
    }

    /** Sets the color the MenuItem uses to draw its foreground text.
      *
      */
    public void setTextColor(Color color) {
        textColor = color;
    }

    /** Returns the color the MenuItem uses to draw its foreground text.
      *
      */
    public Color textColor() {
        return textColor;
    }

    /** Sets the color the MenuItem uses to draw its text when disabled.
      *
      */
    public void setDisabledColor(Color color) {
        disabledColor = color;
    }

    /** Returns the color the MenuItem uses to draw its text when disabled.
      *
      */
    public Color disabledColor() {
        return disabledColor;
    }

    /** Sets this MenuItem to be selected if <b>isSelected</b> is
      * <b>true</b>, or unselected if <b>false</b>.
      *
      */
    public void setSelected(boolean isSelected) {
        if (!isEnabled())
            isSelected = false;

        selected = isSelected;
    }

    /** Returns <b>true</b> if the MenuItem is selected.
      * @see #setSelected
      *
      */
    public boolean isSelected() {
        return selected;
    }

    /** Sets the MenuItem's command.
      */
    public void setCommand(String newCommand) {
        command = newCommand;
    }

    /** Returns the MenuItem's command.
      * @see #setCommand
      */
    public String command() {
        return command;
    }

    /** Sets the MenuItem's Target.
      */
    public void setTarget(Target aTarget) {
        target = aTarget;
    }

    /** Returns the MenuItem's Target.
      * @see #setTarget
      */
    public Target target() {
        return target;
    }

    /** Called by the EventLoop to process a selection event. Calls
      * <b>sendCommand()</b>.
      */
    public void processEvent(Event event) {
        sendCommand();
    }

    /** Tells the MenuItem to send its command to its Target.
      * @see #setTarget
      */
    public void sendCommand() {
        if (target != null) {
            target.performCommand(command, this);
        }
    }

    // ALERT.  This is doing OS checks.  Be afraid.
    private boolean canUseTabFormatter() {
        // ALERT.  it's not guaranteed that all windows platforms
        // can handle the tab key, so we can't depend on it
        return false;
        /*
        if ("x86".equals(System.getProperty("os.arch")) ||
            "Pentium".equals(System.getProperty("os.arch"))) {
            return true;
        } else {
            return false;
        }
        */
    }

    /** Sets the MenuItem's title.
      */
    public void setTitle(String aString) {
        FontMetrics   metrics;
        String        tmpString;
        StringBuffer  buf;
        int           numSpaces, i, maxWidth, width;

        tmpString = aString;
        title = aString;
        if (commandKey() != 0) {
            if (JDK11AirLock.setMenuShortcut(this, commandKey())) {
                tmpString = aString;
            } else if (canUseTabFormatter()) {
                tmpString = aString + "\tCtrl+" + commandKey();
            } else {
                buf = new StringBuffer();
                metrics = font().fontMetrics();
                if (supermenu() != null) {
                    maxWidth = supermenu().minItemWidth();
                } else {
                    maxWidth = minWidth();
                }
                width = metrics.stringWidth(aString);

                buf.append(aString);
                numSpaces = (maxWidth - width) / metrics.stringWidth(" ");
                for (i = 0; i < numSpaces; i++) {
                    buf.append(' ');
                }
                tmpString = buf.toString() + "  Ctrl+" + commandKey();
            }
        }
        foundationMenuItem.setLabel(tmpString);
    }

    /** Returns the MenuItem's title.
      */
    public String title() {
        return title;
    }

    /** Returns the minimum height required to display the MenuItem's title.
      *
      */
    public int minHeight() {
        int    height = 0;

        if (font() != null) {
            height = font().fontMetrics().stringHeight();
        }

        return height;
    }

    /** Returns the minimum width required to display the MenuItem's title
      * and Image, if any.
      *
      */
    public int minWidth() {
        int        width = 0;

        if (image != null)
            width = image.width();

        if (selectedImage != null)
            if (selectedImage.width() > width)
                width = selectedImage.width();

        if (font() != null)
            width += font().fontMetrics().stringWidth(title);

        // Beware of magic constant!  ALERT!
        if (width > 0) {
            width += 11;
        }

        width += commandKeyWidth();

        return width;
    }

    /** Returns the width required to display the command key expression
      * on the right of the MenuItem.
      */
    int commandKeyWidth() {
        int   width = 0;

        // More scary magic constants!  ALERT!
        if (font() != null && commandKey() != (char)0) {
            width = font().fontMetrics().stringWidth("Ctrl+W");
            width += 10;
        }

        return width;
    }

    /** Enables or disables the MenuItem. Disabled MenuItems cannot be
      * selected and render their title using <b>disabledColor</b>.
      */
    public void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
        if (hasSubmenu()) {
            submenu.awtMenu().enable(isEnabled);
        } else {
            foundationMenuItem.enable(isEnabled);
        }
        requestDraw();
    }

    /** Returns <b>true</b> if the MenuItem is enabled, <b>false</b> otherwise.
      * @see #setEnabled
      */
    public boolean isEnabled() {
        return enabled;
        /*
        if (hasSubmenu()) {
            return submenu.awtMenu().isEnabled();
        } else {
            return foundationMenuItem.isEnabled();
        }
        */
    }

    /** Sets the Font used to display the MenuItem's title.
      */
    public void setFont(Font aFont) {
        java.awt.Font awtFont;

        font = aFont;
        awtFont = AWTCompatibility.awtFontForFont(font);
        foundationMenuItem.setFont(awtFont);
    }

    /** Returns the Font used to display the MenuItem's title.
      * @see #setFont
      */
    public Font font() {
        //Font font;

        //font = AWTCompatibility.fontForAWTFont(foundationMenuItem.getFont());
        return font;
    }

    /** Convenience method for this MenuItem to redraw.
      *
      */
    public void requestDraw() {
        if (supermenu() != null) {
            if (supermenu().menuView != null) {   // ALERT! menuView backptr
                supermenu().menuView.drawItemAt(supermenu().indexOfItem(this));
            }
        }
    }

    /** Called from <b>drawInRect()</b> to draw the MenuItem if it is
      * a separator. Subclasses can override this method to do custom
      * drawing. This method has no meaning for MenuItems that are being used
      * in AWT-based native Menus.
      *
      */
    protected void drawSeparator(Graphics g, Rect boundsRect) {
        int  midHeight, width;

        midHeight = boundsRect.y + (boundsRect.height / 2);
        width = boundsRect.x + boundsRect.width;

        g.setColor(Color.gray153);
        g.drawLine(boundsRect.x, midHeight - 1, width, midHeight - 1);
        g.setColor(Color.gray231);
        g.drawLine(boundsRect.x, midHeight, width, midHeight);
    }

    /** Called from <b>drawInRect()</b> to draw the MenuItem's background.
      * Subclasses can override this method to draw custom backgrounds.
      * This method has no meaning for MenuItems that are being used
      * in AWT-based native Menus.
      *
      */
    protected void drawBackground(Graphics g, Rect boundsRect) {
        if (isSelected()) {
            g.setColor(selectedColor);
            g.fillRect(boundsRect);
        }
    }

    /** Called from <b>drawInRect()</b> to draw the MenuItem's title.
      * Subclasses can override this method to draw the title string in a
      * special way. This method has no meaning for MenuItems that are
      * being used in AWT-based native Menus.
      *
      */
    protected void drawStringInRect(Graphics g, String title,
                                    Font titleFont, Rect textBounds,
                                    int justification) {
        String   string;
        Rect     textRect;
        int      width = 0;
        Font     font;

        if (isEnabled() && !isSelected()) {
            g.setColor(textColor);
        } else if (isEnabled() && isSelected()) {
            g.setColor(selectedTextColor);
        } else {
            g.setColor(disabledColor);
        }
        g.setFont(titleFont);
        g.drawStringInRect(title, textBounds, justification);

        if (commandKey() != (char)0) {
            font = font();
            if (font != null) {
                width = font.fontMetrics().stringWidth("Ctrl+W");
                width += 10;   // ALERT
            }

            textRect = new Rect(textBounds.x + textBounds.width - width,
                                textBounds.y, width, textBounds.height);
            string = "Ctrl+" + commandKey();
            g.drawStringInRect(string, textRect, Graphics.LEFT_JUSTIFIED);
        }
    }

    /** Called by Menu to draw the MenuItem. If the MenuItem is
      * transparent, its Menu will have already drawn its background.  If
      * not, the MenuItem should entirely fill <b>boundsRect</b>. If
      * <b>showsArrow</b> is true and this MenuItem has a submenu, a
      * submenu arrow will be drawn on the right edge.
      * This method has no meaning for MenuItems that are being used
      * in AWT-based native Menus.
      *
      */
    public void drawInRect(Graphics g, Rect boundsRect, boolean
                           showsArrow) {
        Image   theImage;
        Rect    tmpRect;
        int     width, height;

        if (isSeparator()) {
            drawSeparator(g, boundsRect);
            return;
        }

        drawBackground(g, boundsRect);

        if (isSelected()) {
            theImage = selectedImage;
        } else {
            theImage = image;
        }

        width = height = 0;
        if (image != null) {
            width = image.width();
            height = image.height();
        }
        if (selectedImage != null) {
            if (selectedImage.width() > width) {
                width = selectedImage.width();
            }
            if (selectedImage.height() > height) {
                height = selectedImage.height();
            }
        }

        if (theImage != null) {
            theImage.drawAt(g, boundsRect.x,
                            boundsRect.y + (boundsRect.height - height)/2);
        }

        if (title != null && title.length() > 0) {
            tmpRect = Rect.newRect(boundsRect.x + 2 + width, boundsRect.y,
                                   boundsRect.width - 2 - width,
                                   boundsRect.height);
            drawStringInRect(g, title, font(), tmpRect,
                             Graphics.LEFT_JUSTIFIED);

            Rect.returnRect(tmpRect);
        }

        if (showsArrow && hasSubmenu()) {
            theImage = Bitmap.bitmapNamed(
                              "netscape/application/ScrollRightArrow.gif");
            width = theImage.width();
            height = theImage.height();

            theImage.drawAt(g, boundsRect.x + boundsRect.width - width,
                            boundsRect.y + (boundsRect.height - height)/2);
        }
    }

    /** Describes the MenuItem class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.MenuItem", 1);
        info.addField(SUBMENU_KEY, OBJECT_TYPE);
        info.addField(SUPERMENU_KEY, OBJECT_TYPE);

        info.addField(COMMAND_KEY, STRING_TYPE);
        info.addField(TITLE_KEY, STRING_TYPE);
        info.addField(TARGET_KEY, OBJECT_TYPE);
        info.addField(COMMANDKEY_KEY, CHAR_TYPE);

        info.addField(FONT_KEY, OBJECT_TYPE);
        info.addField(CHECKEDIMAGE_KEY, OBJECT_TYPE);
        info.addField(UNCHECKEDIMAGE_KEY, OBJECT_TYPE);
        info.addField(IMAGE_KEY, OBJECT_TYPE);
        info.addField(SELECTEDIMAGE_KEY, OBJECT_TYPE);
        info.addField(SELECTEDCOLOR_KEY, OBJECT_TYPE);
        info.addField(SELECTEDTEXTCOLOR_KEY, OBJECT_TYPE);
        info.addField(TEXTCOLOR_KEY, OBJECT_TYPE);
        info.addField(DISABLEDCOLOR_KEY, OBJECT_TYPE);

        //info.addField(SELECTED_KEY, BOOLEAN_TYPE);
        info.addField(SEPARATOR_KEY, BOOLEAN_TYPE);
        info.addField(ENABLED_KEY, BOOLEAN_TYPE);
        info.addField(STATE_KEY, BOOLEAN_TYPE);

        info.addField(DATA_KEY, OBJECT_TYPE);
    }

    /** Archives the MenuItem instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(SUBMENU_KEY, submenu);
        encoder.encodeObject(SUPERMENU_KEY, supermenu);

        encoder.encodeString(COMMAND_KEY, command);
        encoder.encodeString(TITLE_KEY, title);
        encoder.encodeObject(TARGET_KEY, target);
        encoder.encodeChar(COMMANDKEY_KEY, commandKey);

        encoder.encodeObject(FONT_KEY, font);
        encoder.encodeObject(CHECKEDIMAGE_KEY, checkedImage);
        encoder.encodeObject(UNCHECKEDIMAGE_KEY, uncheckedImage);
        encoder.encodeObject(IMAGE_KEY, image);
        encoder.encodeObject(SELECTEDIMAGE_KEY, selectedImage);
        encoder.encodeObject(SELECTEDCOLOR_KEY, selectedColor);
        encoder.encodeObject(SELECTEDTEXTCOLOR_KEY, selectedTextColor);
        encoder.encodeObject(TEXTCOLOR_KEY, textColor);
        encoder.encodeObject(DISABLEDCOLOR_KEY, disabledColor);

        //encoder.encodeBoolean(SELECTED_KEY, selected);
        encoder.encodeBoolean(SEPARATOR_KEY, separator);
        encoder.encodeBoolean(ENABLED_KEY, enabled);
        encoder.encodeBoolean(STATE_KEY, state);

        encoder.encodeObject(DATA_KEY, data);
    }

    /** Unarchives the MenuItem instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        submenu = (Menu)decoder.decodeObject(SUBMENU_KEY);
        supermenu = (Menu)decoder.decodeObject(SUPERMENU_KEY);

        command = (String)decoder.decodeString(COMMAND_KEY);
        title = (String)decoder.decodeString(TITLE_KEY);
        target = (Target)decoder.decodeObject(TARGET_KEY);
        commandKey = (char)decoder.decodeChar(COMMANDKEY_KEY);

        font = (Font)decoder.decodeObject(FONT_KEY);
        checkedImage = (Image)decoder.decodeObject(CHECKEDIMAGE_KEY);
        uncheckedImage = (Image)decoder.decodeObject(UNCHECKEDIMAGE_KEY);
        image = (Image)decoder.decodeObject(IMAGE_KEY);
        selectedImage = (Image)decoder.decodeObject(SELECTEDIMAGE_KEY);
        selectedColor = (Color)decoder.decodeObject(SELECTEDCOLOR_KEY);
        selectedTextColor = (Color)decoder.decodeObject(SELECTEDTEXTCOLOR_KEY);
        textColor = (Color)decoder.decodeObject(TEXTCOLOR_KEY);
        disabledColor = (Color)decoder.decodeObject(DISABLEDCOLOR_KEY);

        //selected = (boolean)decoder.decodeBoolean(SELECTED_KEY);
        separator = (boolean)decoder.decodeBoolean(SEPARATOR_KEY);
        enabled = (boolean)decoder.decodeBoolean(ENABLED_KEY);
        state = (boolean)decoder.decodeBoolean(STATE_KEY);

        data = (Object)decoder.decodeObject(DATA_KEY);
    }

    /** Finishes the MenuItem's unarchiving.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
