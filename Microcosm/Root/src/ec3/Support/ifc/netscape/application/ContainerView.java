// ContainerView.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;


// ALERT!!!s
// Should we archive the title textField?

import netscape.util.*;

/** View subclass that fills itself with a Color or an Image, and can draw a
  * Border around its perimeter.  The Image can be centered, scaled, or
  * tiled.
  * @note 1.0 Removed Component interface
  * @note 1.0 minSize is now boundingbox of contained views
  * @note 1.0 all draws->dirtyRect
  * @note 1.0 layout removes title view before being called
  * @note 1.0 Added FormElement interface for browser needs
  */
public class ContainerView extends View implements FormElement {
    private String      title = "";
    private Font        titleFont;
    private Image       image;
    private TextField   titleField;
    private Color       backgroundColor = Color.lightGray;
    private Color       titleColor = Color.black;
    private Border      border = BezelBorder.groovedBezel();
    private int         imageDisplayStyle = Image.CENTERED;
    private boolean     transparent = false;

    static Vector    _fieldDescription = null;

    final static String TITLE_KEY = "title";
    final static String TITLE_FONT_KEY = "titleFont";
    final static String BACKGROUND_COLOR_KEY = "backgroundColor";
    final static String TITLE_COLOR_KEY = "titleColor";
    final static String BORDER_KEY = "border";
    final static String IMAGE_KEY = "image";
    final static String IMAGE_DISPLAY_STYLE_KEY = "imageDisplayStyle";
    final static String TRANSPARENT_KEY = "transparent";


/* constructors */

    /** Constructs a ContainerView with origin (0, 0) and zero width and
      * height.
      */
    public ContainerView() {
        this(0, 0, 0, 0);
    }

    /** Constructs a ContainerView with bounds <B>rect</B>.
      */
    public ContainerView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a ContainerView with
      * bounds (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public ContainerView(int x, int y, int width, int height) {
        super(x, y, width, height);


        titleField = new TextField(0, 0, 10, 18);
        titleField.setBorder(null);
        titleField.setTransparent(true);
        titleField.setEditable(false);
        titleField.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        titleField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        titleField.setJustification(Graphics.CENTERED);

        titleFont = Font.fontNamed("Helvetica", Font.BOLD, 12);
        layoutParts();
    }

    void layoutParts() {
        int leftM, rightM;
        titleField.removeFromSuperview();

        titleField.setStringValue(title);
        titleField.setFont(titleFont);
        titleField.sizeToMinSize();

        if(border != null)  {
            leftM = border.leftMargin();
            rightM = border.rightMargin();
        } else  {
            leftM = 0;
            rightM = 0;
        }

        if (!titleField.isEmpty()) {
            titleField.moveTo(leftM, 0);
            titleField.sizeTo(width() - leftM - rightM, titleField.height());
            addSubview(titleField);
        }
    }

    /** Returns the ContainterView's minimum size, which is governed by the
      * space needed to fully all it's subviews.  Absolute minimum size is
      * (2, 2).
      */
    public Size minSize()
    {
        Size basesize = super.minSize();
        Vector subviews = this.subviews();
        int maxx = 0, maxy=0, i, count;
        View v;

        // Respect a setMinSize() if there is one
        if(basesize.width != 0 || basesize.height != 0)
            return basesize;

        // Determine the minSize for the titleField
        if(title != null && !("".equals(title)))    {
            titleField.setStringValue(title);
            titleField.setFont(titleFont);
            maxx = titleField.minSize().width + 6;
            maxy = titleField.minSize().height + 2;
        }

        // Force all the children to be arranged.
        layoutView(0,0);

        // loop through all the children, and find the maximum
        // coordinates for the lower-right corner of the view.
        count = subviews.count();
        for(i = 0; i < count; i++) {
            v = (View)subviews.elementAt(i);
            if (v.bounds().maxX() > maxx)
                maxx = v.bounds().maxX();
            if (v.bounds().maxY() > maxy)
                maxy = v.bounds().maxY();
        }

        basesize.width = maxx;
        basesize.height = maxy;

        // Adjust for the border
        if (border != null) {
            basesize.width += border.rightMargin();
            basesize.height += border.bottomMargin();
        }

        return basesize;
    }


    /** Returns the area not used by the ContainerView to draw its bounds
      * and title.
      */
    public Rect interiorRect() {
        Rect    tmpRect;

        if (border != null)
            tmpRect = border.interiorRect(0, 0, width(), height());
        else
            tmpRect = new Rect(0, 0, width(), height());

        if (titleField._superview != null) {
            tmpRect.y += titleField.bounds.height;
            tmpRect.height -= titleField.bounds.height;
        }

        return tmpRect;
    }

    /** Sets the ContainerView's title string and then calls its
      * <B>draw()</B> method to redraw it.  If you do not want to immediately
      * redraw the ContainerView, you should first call its
      * <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setTitle(String aString) {
        if (aString != null) {
            title = aString;
        } else {
            title = "";
        }
        layoutParts();
        setDirty(true);
    }

    /** Returns the ContainerView's title.
      * @see #setTitle
      */
    public String title() {
        return title;
    }

    /** Sets the ContainerView's title string Color and then calls its
      * <B>draw()</B> method to redraw it.  If you do not want to immediately
      * redraw the ContainerView, you should first call its
      * <B>disableDrawing()</B> method.
      * @see #setTitle
      * @see View#disableDrawing
      */
    public void setTitleColor(Color aColor) {
        if (aColor == null || aColor.equals(titleColor)) {
            return;
        }

        titleColor = aColor;
        titleField.setTextColor(aColor);
        setDirty(true);
    }

    /** Returns the ContainerView's title string Color.
      * @see #setTitleColor
      */
    public Color titleColor() {
        return titleColor;
    }

    /** Sets the ContainerView's title string Font and then calls its
      * <B>draw()</B> method to redraw it.  If you do not want to immediately
      * redraw the ContainerView, you should first call its
      * <B>disableDrawing()</B> method.
      * @see #setTitle
      * @see View#disableDrawing
      */
    public void setTitleFont(Font aFont) {
        if (aFont == null) {
            titleFont = Font.fontNamed("Helvetica", Font.BOLD, 12);
        } else {
            titleFont = aFont;
        }
        layoutParts();
        setDirty(true);
    }

    /** Returns the ContainerView's title string Font.
      * @see #setTitleFont
      */
    public Font titleFont() {
        return titleFont;
    }

    /** Sets the ContainerView's background Color and then calls its
      * <B>draw()</B> method to redraw it.  If you do not want to immediately
      * redraw the ContainerView, you should first call its
      * <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setBackgroundColor(Color aColor) {
        backgroundColor = aColor;
        setDirty(true);
    }

    /** Returns the ContainerView's background Color.
      * @see #setBackgroundColor
      */
    public Color backgroundColor() {
        return backgroundColor;
    }

    /** Sets the ContainerView's Border and then calls its
      * <B>draw()</B> method to redraw it.  If you do not want to immediately
      * redraw the ContainerView, you should first call its
      * <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setBorder(Border newBorder) {
        if (newBorder == null) {
            newBorder = EmptyBorder.emptyBorder();
        }

        border = newBorder;
        layoutParts();
        setDirty(true);
    }

    /** Returns the ContainerView's Border.
      * @see #setBorder
      */
    public Border border() {
        return border;
    }

    /** Sets the ContainerView's Image and then calls its
      * <B>draw()</B> method to redraw it.  If you do not want to immediately
      * redraw the ContainerView, you should first call its
      * <B>disableDrawing()</B> method.
      * @see #setImageDisplayStyle
      * @see View#disableDrawing
      */
    public void setImage(Image anImage) {
        image = anImage;
        setDirty(true);
    }

    /** Returns the ContainerView's Image.
      * @see #setImage
      */
    public Image image() {
        return image;
    }

    /** Sets the style the ContainerView uses to display its Image
      * (Image.CENTERED, Image.TILED, or Image.SCALED).
      * @see #setImage
      */
    public void setImageDisplayStyle(int aStyle) {
        if (aStyle != Image.CENTERED && aStyle != Image.TILED &&
            aStyle != Image.SCALED) {
            throw new InconsistencyException("Unknown image display style: " +
                aStyle);
        }

        imageDisplayStyle = aStyle;
        setDirty(true);
    }

    /** Returns the style the ContainerView uses to display its Image.
      * @see #setImageDisplayStyle
      */
    public int imageDisplayStyle() {
        return imageDisplayStyle;
    }

    /** Sets the ContainerView to be transparent or opaque.
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
    }

    /** Overridden to return <b>true</b> if the ContainerView is transparent.
      * This will also return true if we have a title.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent || !titleField.isEmpty();
    }

    /** Draws the ContainerView's background, using its Image and
      * background Color.  If the ContainerView is transparent, or its
      * background Color is <b>null</b>, it only draws the Image.
      * You never call this method directly, but
      * should override it to produce custom background drawing.
     */
    public void drawViewBackground(Graphics g) {
        Rect    tmpRect;

        /* draw background color only if color has been set and image doesn't
         * completely fill our bounds
         */
        if (!transparent && (image == null ||
            imageDisplayStyle == Image.CENTERED && backgroundColor != null)) {

            if (image == null) {
                tmpRect = Rect.newRect();
            } else {
                tmpRect = Rect.newRect(0, 0, image.width(), image.height());
            }
            if (!tmpRect.contains(bounds)) {
                tmpRect.setBounds(0, 0, width(), height());
                if (titleField.isInViewHierarchy()) {
                    tmpRect.moveBy(0, titleField.bounds.height / 2);
                    tmpRect.sizeBy(0, -titleField.bounds.height / 2);
                }
                g.setColor(backgroundColor);
                g.fillRect(tmpRect);
            }
            Rect.returnRect(tmpRect);
        }

        if (image != null) {
            image.drawWithStyle(g, 0, 0, width(), height(), imageDisplayStyle);
        }
    }

    /** Draws the ContainerView's Border, including its title.  You never call
      * this method directly, but should override it to produce custom
      * border drawing.
      * @see #setBorder
      * @see #setTitle
      */
    public void drawViewBorder(Graphics g) {
        Rect    borderRect, titleRect, clipRect;
        Size    titleSize;

        if (border == null)
            return;

        borderRect = Rect.newRect(0, 0, width(), height());

        if (titleField.isInViewHierarchy()) {
            borderRect.moveBy(0, titleField.bounds.height / 2);
            borderRect.sizeBy(0, -titleField.bounds.height / 2);
        }

        /* avoid drawing the border where the title will show up.  If the
         * title isn't to be drawn, then things are nice and easy.
         */
        if (!titleField.isInViewHierarchy()) {
            border.drawInRect(g, borderRect);
            Rect.returnRect(borderRect);
            return;
        }

        titleSize = titleField.minSize();
        titleRect = Rect.newRect(titleField.bounds);
        titleRect.x = titleRect.midX() - titleSize.width / 2 - 4;
        titleRect.width = titleSize.width + 8;

        clipRect = Rect.newRect(borderRect);

        /* draw the left part of the border */
        clipRect.width = titleRect.x - borderRect.x;

        g.pushState();
        g.setClipRect(clipRect);
        border.drawInRect(g, borderRect);
        g.popState();

        /* draw the right part */
        clipRect.x = titleRect.maxX();
        clipRect.width = borderRect.maxX() - titleRect.maxX();
        g.pushState();
        g.setClipRect(clipRect);
        border.drawInRect(g, borderRect);
        g.popState();

        /* draw the bottom part */
        clipRect.x = titleRect.x;
        clipRect.y = titleRect.maxY();
        clipRect.width = titleRect.width;
        clipRect.height = borderRect.maxY() - titleRect.maxY();
        g.pushState();
        g.setClipRect(clipRect);
        border.drawInRect(g, borderRect);
        g.popState();

        Rect.returnRect(clipRect);
        Rect.returnRect(titleRect);
        Rect.returnRect(borderRect);
    }

    /** Draws the ContainerView's background.  Calls
      * <b>drawViewBackground()</b>.
      * @see #drawViewBackground
      */
    public void drawView(Graphics g) {
        drawViewBackground(g);
    }

    /** Draws the ContainerView's border, after drawing its subviews.  Calls
      * <b>drawViewBorder()</b>.
      * @see #drawViewBorder
      */
    public void drawSubviews(Graphics g) {
        super.drawSubviews(g);
        drawViewBorder(g);
    }


    /** Overridden to get the titleField out of the way, before
      * a LayoutManager can get a hold of it and mangle it's location.
      * It is put back afterwards if necessary.
      */
    public void layoutView(int deltaX, int deltaY)  {
        if(titleField.isInViewHierarchy())
            titleField.removeFromSuperview();
        super.layoutView(deltaX, deltaY);
        layoutParts();
    }

/* archiving */



    /** Describes the ContainerView class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.ContainerView", 1);
        info.addField(TITLE_KEY, STRING_TYPE);
        info.addField(TITLE_FONT_KEY, OBJECT_TYPE);
        info.addField(BACKGROUND_COLOR_KEY, OBJECT_TYPE);
        info.addField(TITLE_COLOR_KEY, OBJECT_TYPE);
        info.addField(IMAGE_KEY, OBJECT_TYPE);
        info.addField(IMAGE_DISPLAY_STYLE_KEY, INT_TYPE);
        info.addField(BORDER_KEY, OBJECT_TYPE);
        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the ContainerView instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        boolean         addField = false;

        if (titleField.superview() != null) {
            titleField.removeFromSuperview();
            addField = true;
        }

        super.encode(encoder);

        encoder.encodeString(TITLE_KEY, title);
        encoder.encodeObject(TITLE_FONT_KEY, titleFont);
        encoder.encodeObject(BACKGROUND_COLOR_KEY, backgroundColor);
        encoder.encodeObject(TITLE_COLOR_KEY, titleColor);
        encoder.encodeObject(IMAGE_KEY, image);
        encoder.encodeInt(IMAGE_DISPLAY_STYLE_KEY, imageDisplayStyle);

        if (border instanceof EmptyBorder) {
            encoder.encodeObject(BORDER_KEY, null);
        } else {
            encoder.encodeObject(BORDER_KEY, border);
        }

        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);

        if (addField) {
            addSubview(titleField);
        }
    }

    /** Decodes the ContainerView instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        title = decoder.decodeString(TITLE_KEY);
        setTitleFont((Font)decoder.decodeObject(TITLE_FONT_KEY));
        backgroundColor = (Color)decoder.decodeObject(BACKGROUND_COLOR_KEY);
        setTitleColor((Color)decoder.decodeObject(TITLE_COLOR_KEY));
        image = (Image)decoder.decodeObject(IMAGE_KEY);
        imageDisplayStyle = decoder.decodeInt(IMAGE_DISPLAY_STYLE_KEY);
        setBorder((Border)decoder.decodeObject(BORDER_KEY));
        transparent = decoder.decodeBoolean(TRANSPARENT_KEY);
    }

    /** Finishes the ContainerView instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        String  unarchTitle;

        super.finishDecoding();

        unarchTitle = title;
        title = null;
        setTitle(unarchTitle);
    }

    /** Implementation of the FormElement interface
      *
      */
    public String formElementText() {
        if(titleField != null)
            return titleField.stringValue();
        return "";
    }
}
