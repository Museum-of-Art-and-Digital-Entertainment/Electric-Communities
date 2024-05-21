// ListItem.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass representing a single ListView entry.
  * @see ListView
  * @note 1.0 add foreground text color, archiving changed
  * @note 1.0 archives from 1.1b1 and 1.1b2 are changed
  */

public class ListItem implements Cloneable, Codable {
    ListView listView;
    String command;
    String title;
    Font font;
    Color selectedColor, textColor;
    Image image;
    Image selectedImage;
    boolean selected;
    boolean enabled = true;
    Object data;

    final static String       LISTVIEW_KEY = "listView",
                              COMMAND_KEY = "command",
                              TITLE_KEY = "title",
                              FONT_KEY = "font",
                              SELECTEDCOLOR_KEY = "selectedColor",
                              TEXTCOLOR_KEY = "textColor",
                              IMAGE_KEY = "image",
                              SELECTEDIMAGE_KEY = "selectedImage",
                              SELECTED_KEY = "selected",
                              ENABLED_KEY = "enabled",
                              DATA_KEY = "data";

    /// This is an old archive ket from version 2, it is functionally
    /// equivelent to TEXT_COLOR_KEY
    final static String       VER_2_TEXTCOLOR_KEY = "foregroundColor";

    /** Constructs an empty ListItem.
      */
    public ListItem() {
        super();
        selectedColor = Color.white;
        textColor = Color.black;
    }

    /** Clones the ListItem. ListView adds addtional rows by cloning its
      * prototype ListItem. Note that if you have <b>setData()</b> on the
      * prototype ListItem, <b>clone()</b> does not clone the data (all
      * ListItem clones will reference the same data).
      */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InconsistencyException(
                                    this + ": clone() not supported :" + e);
        }
    }

    void setListView(ListView aListView) {
        listView = aListView;
    }

    /** Returns the ListView associated with the ListItem. Returns
      * <b>null</b> if the ListItem is not in a ListView.
      */
    public ListView listView() {
        return listView;
    }

    /** Returns <b>true</b> if the ListItem is transparent, allowing
      * ListView to optimize its drawing. ListView only draws the background of
      * transparent ListItems.
      */
    public boolean isTransparent() {
        return (!selected);
    }

    /** Sets the command associated with this ListItem.  ListView sends
      * ListItem's command instead of its own when the ListItem is
      * single-clicked.
      */
    public void setCommand(String newCommand) {
        command = newCommand;
    }

    /** Returns the command associated with this ListItem.
      * @see #setCommand
      */
    public String command() {
        return command;
    }

    /** Sets the title the ListItem displays.
      */
    public void setTitle(String aString) {
        title = aString;
    }

    /** Returns the title the ListItem displays.
      * @see #setTitle
      */
    public String title() {
        return title;
    }

    /** Called by ListView when a ListItem is selected. By default, selected
      * ListItems highlight by painting a white background.
      */
    public void setSelected(boolean flag) {
        if (!enabled)
            flag = false;

        selected = flag;
    }

    /** Returns <b>true</b> if the ListItem is selected.
      * @see #setSelected
      */
    public boolean isSelected() {
        return selected;
    }

    /** Enables or disables the ListItem. Disabled ListItems cannot be
      * selected, and render their title using Color.gray.
      */
    public void setEnabled(boolean flag) {
        enabled = flag;

        if (!enabled)
            selected = false;
    }

    /** Returns <b>true</b> if the ListItem is enabled.
      * @see #setEnabled
      */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets the Image the ListItem displays next to its title.
      * @see #setSelectedImage
      */
    public void setImage(Image anImage) {
        image = anImage;
    }

    /** Returns the Image the ListItem displays next to its title.
      * @see #setImage
      */
    public Image image() {
        return image;
    }

    /** Sets the Image the ListItem displays next to its title when selected.
      * @see #setImage
      */
    public void setSelectedImage(Image anImage) {
        selectedImage = anImage;
    }

    /** Returns the Image the ListItem displays next to its title when
      * selected.
      * @see #setSelectedImage
      */
    public Image selectedImage() {
        return selectedImage;
    }

    /** Sets the Font the ListItem uses to render its title.
      */
    public void setFont(Font aFont) {
        font = aFont;
    }

    /** Returns the Font the ListItem uses to render its title. If no Font has
      * been set, returns <b>Font.defaultFont()</b>.
      * @see Font#defaultFont
      */
    public Font font() {
        if (font == null) {
            font = Font.defaultFont();
        }

        return font;
    }

    /** Sets the color the ListItem uses to draw its background when
      * selected.
      */
    public void setSelectedColor(Color color) {
        selectedColor = color;
    }

    /** Returns the color the ListItem uses to draw its background when
      * selected.
      */
    public Color selectedColor() {
        return selectedColor;
    }

    /** Sets the color the ListItem uses to draw its foreground text.
      *
      */
    public void setTextColor(Color color) {
        textColor = color;
    }

    /** Returns the color the ListItem uses to draw its foreground text.
      *
      */
    public Color textColor() {
        return textColor;
    }

    /** Sets the ListItem's data, a storage place for arbitrary data associated
      * with the ListItem.
      */
    public void setData(Object data)    {
        this.data = data;
    }

    /** Returns the ListItem's data.
      * @see #setData
      */
    public Object data()    {
        return this.data;
    }

    /** Returns the minimum width required to display the ListItem's title
      * and Image, if any.
      */
    public int minWidth() {
        Font         font;
        int             width = 0;

        if (image != null)
            width = image.width();

        if (selectedImage != null)
            if (selectedImage.width() > width)
                width = selectedImage.width();

        font = font();
        if (font != null)
            width += font.fontMetrics().stringWidth(title);

        // Beware of magic constant!  ALERT!
        if (width > 0) {
            width += 3;
        }

        return width;
    }

    /** Returns the minimum height required to display the ListItem's title
      * and Image, if any.
      */
    public int minHeight() {
        Font         font;
        Size            titleSize;
        int             height = 0, titleHeight = 0;

        if (image != null)
            height = image.height();

        if (selectedImage != null)
            if (selectedImage.height() > height)
                height = selectedImage.height();

        font = font();
        if (font != null)
            titleHeight = font.fontMetrics().stringHeight();

        if (titleHeight > height)
            height = titleHeight;

        return height;
    }

    /** Called from <b>drawInRect()</b> to draw the ListItem's background.
      * Subclasses can override this method to draw custom backgrounds.
      */
    protected void drawBackground(Graphics g, Rect boundsRect) {
        if (selected) {
            g.setColor(selectedColor);
            g.fillRect(boundsRect);
        }
    }

    /** Called from <b>drawInRect()</b> to draw the ListItem's title.
      * Subclasses can override this method to draw the title string in a
      * special way.
      */
    protected void drawStringInRect(Graphics g, String title,
                                 Font titleFont, Rect textBounds,
                                 int justification) {
        if (listView().isEnabled() && enabled) {
            g.setColor(textColor);
        } else {
            g.setColor(Color.gray);
        }
        g.setFont(titleFont);
        g.drawStringInRect(title, textBounds, justification);
    }

    /** Called by ListView to draw the ListItem. If the ListItem is
      * transparent, its ListView will have already drawn its background.  If
      * not, the ListItem should entirely fill <b>boundsRect</b>.
      */
    public void drawInRect(Graphics g, Rect boundsRect) {
        Image   theImage;
        Rect    tmpRect;
        int     width, height;

        drawBackground(g, boundsRect);

        if (selected) {
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
    }

    /** Describes the ListItem class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.ListItem", 3);
        info.addField(LISTVIEW_KEY, OBJECT_TYPE);

        info.addField(COMMAND_KEY, STRING_TYPE);
        info.addField(TITLE_KEY, STRING_TYPE);

        info.addField(FONT_KEY, OBJECT_TYPE);
        info.addField(SELECTEDCOLOR_KEY, OBJECT_TYPE);
        info.addField(IMAGE_KEY, OBJECT_TYPE);
        info.addField(SELECTEDIMAGE_KEY, OBJECT_TYPE);

        info.addField(SELECTED_KEY, BOOLEAN_TYPE);
        info.addField(ENABLED_KEY, BOOLEAN_TYPE);

        info.addField(DATA_KEY, OBJECT_TYPE);

        info.addField(TEXTCOLOR_KEY, OBJECT_TYPE);
    }

    /** Archives the ListItem instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(LISTVIEW_KEY, listView);

        encoder.encodeString(COMMAND_KEY, command);
        encoder.encodeString(TITLE_KEY, title);

        encoder.encodeObject(FONT_KEY, font);
        encoder.encodeObject(SELECTEDCOLOR_KEY, selectedColor);
        encoder.encodeObject(IMAGE_KEY, image);
        encoder.encodeObject(SELECTEDIMAGE_KEY, selectedImage);

        encoder.encodeBoolean(SELECTED_KEY, selected);
        encoder.encodeBoolean(ENABLED_KEY, enabled);
        if(data != null && data instanceof Codable)
            encoder.encodeObject(DATA_KEY, data);
        else
            encoder.encodeObject(DATA_KEY, null);

        encoder.encodeObject(TEXTCOLOR_KEY, textColor);
    }

    /** Unarchives the ListItem instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        listView = (ListView)decoder.decodeObject(LISTVIEW_KEY);

        command = (String)decoder.decodeString(COMMAND_KEY);
        title = (String)decoder.decodeString(TITLE_KEY);

        font = (Font)decoder.decodeObject(FONT_KEY);
        selectedColor = (Color)decoder.decodeObject(SELECTEDCOLOR_KEY);
        image = (Image)decoder.decodeObject(IMAGE_KEY);
        selectedImage = (Image)decoder.decodeObject(SELECTEDIMAGE_KEY);

        selected = decoder.decodeBoolean(SELECTED_KEY);
        enabled = decoder.decodeBoolean(ENABLED_KEY);

        data = (Object)decoder.decodeObject(DATA_KEY);

        if (decoder.versionForClassName("netscape.application.ListItem") == 2) {
            textColor = (Color)decoder.decodeObject(VER_2_TEXTCOLOR_KEY);
            // We are no longer supporting the disabledColor attribute
            // on ListItem. It is lost here.
        } else if (decoder.versionForClassName("netscape.application.ListItem") > 2) {
            textColor = (Color)decoder.decodeObject(TEXTCOLOR_KEY);
        }
    }

    /** Finishes the ListItem's unarchiving.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
