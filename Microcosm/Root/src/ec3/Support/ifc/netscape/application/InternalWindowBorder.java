// InternalWindowBorder.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Border subclass that draws a border around an InternalWindow.  This border
  * includes the InternalWindow's title and resize bars.
  * @note 1.0 changes to suppoort menus
  */
public class InternalWindowBorder extends Border {
    static Image      indentLeftImage, indentRightImage,
                      leftResizeImage, rightResizeImage;
    InternalWindow    window;

    final static int            TITLE_BAR_INDENT_OFFSET = 1;

    final static String         WINDOW_KEY = "window";

    /** Constructs an InternalWindowBorder.  This constructor is only useful
      * during unarchiving.
      */
    public InternalWindowBorder() {
        this(null);
    }

    /** Constructs an InternalWindowBorder for the InternalWindow
      * <b>aWindow</b>.
      */
    public InternalWindowBorder(InternalWindow aWindow) {
        super();

        window = aWindow;
        indentLeftImage = Bitmap.bitmapNamed(
                                "netscape/application/TitleBarLeft.gif");
        indentRightImage = Bitmap.bitmapNamed(
                                "netscape/application/TitleBarRight.gif");
        leftResizeImage = Bitmap.bitmapNamed(
                                "netscape/application/ResizeLeft.gif");
        rightResizeImage = Bitmap.bitmapNamed(
                                "netscape/application/ResizeRight.gif");
    }

    /** Sets the InternalWindowBorder's InternalWindow.
      */
    public void setWindow(InternalWindow aWindow) {
        window = aWindow;
    }

    /** Returns the InternalWindowBorder's InternalWindow.
      * @see #setWindow
      */
    public InternalWindow window() {
        return window;
    }

    /** Returns the InternalWindowBorder's left margin. */
    public int leftMargin() {
        return 3;
    }

    /** Returns the InternalWindowBorder's right margin. */
    public int rightMargin() {
        return 2;
    }

    /** Returns the InternalWindowBorder's top margin (the title bar height).
      */
    public int topMargin() {
        return 22;
    }

    /** Returns the InternalWindowBorder's bottom margin (the resize bar or
      * bottom border height).
      */
    public int bottomMargin() {
        if (window.isResizable()) {
            return 11;
        } else {
            return 2;
        }
    }

    /** Returns the width of the InternalWindowBorder's resize controls,
      * appearing along the InternalWindowBorder's bottom border.  Clicking
      * and dragging within these controls lets the user change both the
      * InternalWindow's width and height, while dragging between the controls
      * constrains resizing to just the vertical dimension.  Override this
      * method if you implement your own bottom border painting and use a
      * resize control with a different size.
      */
    public int resizePartWidth() {
        if (!window.isResizable()) {
            return 0;
        }
        return leftResizeImage.width();
    }

   /** Draws the InternalWindowBorder's title bar and border.
     */
    public void drawTitleBar(Graphics g, int x, int y, int width,
                                 int height) {
        Rect            tmpRect, titleBarRect;
        int             maxX, maxY, topMargin, imageX, imageWidth;
        boolean         isMain;

        topMargin = topMargin();

        titleBarRect = Rect.newRect(x, y, width, topMargin);
        if (!g.clipRect().intersects(titleBarRect)) {
            Rect.returnRect(titleBarRect);
            return;
        }
        Rect.returnRect(titleBarRect);

        maxX = width - 1;
        maxY = topMargin - 1;

        g.setColor(Color.lightGray);
        g.fillRect(x + 1, y + 1, maxX - 1, maxY - 1);

        g.setColor(Color.gray153);
        g.drawPoint(x, y);
        g.drawLine(x + 1, y, maxX, y);    // Top line
        g.drawPoint(x + 1, maxY);
        g.drawLine(x, y + 1, x, maxY);    // Left line

//      We got these from the fillRect above
//      g.setColor(Color.lightGray);
//      g.drawPoint(x+1, y+1);
//      g.drawPoint(maxX-1, y+1);
//      g.drawPoint(x+1, maxY-1);

        g.setColor(Color.white);
        g.drawLine(x + 2, y + 1, maxX - 2, y + 1);  // top inner line
        g.drawPoint(x + 2, y + 2);
        g.drawLine(x + 1, y + 2, x + 1, maxY - 2);  // left inner line

        g.setColor(Color.gray153);
        g.drawLine(x + 2, maxY - 1, maxX - 2, maxY - 1);    // top bottom line
        g.drawPoint(maxX - 2, maxY - 2);
        g.drawLine(maxX - 1, y + 2, maxX - 1, maxY - 2);    // inner right line

        g.setColor(Color.gray102);
        g.drawLine(x + 2, maxY, maxX, maxY);
        g.drawLine(maxX, y + 1, maxX, maxY);
        g.drawPoint(maxX - 1, maxY - 1);

        /* title indentation */
        imageX = 25 + indentLeftImage.width();
        imageWidth = width - 25 - indentRightImage.width() - imageX;
        isMain = window.isMain();
        if (isMain) {
            indentLeftImage.drawAt(g, 25, TITLE_BAR_INDENT_OFFSET);
            indentRightImage.drawAt(g,
                                width - indentRightImage.width() - 24,
                                TITLE_BAR_INDENT_OFFSET);
            tmpRect = Rect.newRect(imageX,
                                   TITLE_BAR_INDENT_OFFSET + 2,
                                   imageWidth, indentLeftImage.height() - 4);
            g.setColor(Color.gray153);
            g.drawLine(tmpRect.x, tmpRect.y+1, tmpRect.maxX(), tmpRect.y+1);
            g.setColor(Color.white);
            g.drawLine(tmpRect.x+1, tmpRect.maxY() - 2, tmpRect.maxX(),
                       tmpRect.maxY() - 2);
            Rect.returnRect(tmpRect);
        }

        /* title */
        tmpRect = Rect.newRect(imageX, TITLE_BAR_INDENT_OFFSET - 1,
                               imageWidth, indentLeftImage.height() - 2);
        g.pushState();
        g.setClipRect(tmpRect);

        g.setColor(Color.darkGray);
        g.setFont(window.font());
        g.drawStringInRect(window.title(), tmpRect, Graphics.CENTERED);
        g.popState();
        Rect.returnRect(tmpRect);
    }

    /** Draws the InternalWindowBorder's left border.
      */
    public void drawLeftBorder(Graphics g, int x, int y, int width,
                                   int height) {
        Rect    borderRect;
        int     startY;

        borderRect = Rect.newRect(0, 0, leftMargin(), height);
        if (!g.clipRect().intersects(borderRect)) {
            Rect.returnRect(borderRect);
            return;
        }
        Rect.returnRect(borderRect);

        startY = topMargin();

        g.setColor(Color.gray153);
        g.drawLine(0, startY, 0, height -1 );

        g.setColor(Color.white);
        g.drawLine(1, startY, 1, height - 2);

        g.setColor(Color.lightGray);
        g.drawLine(2, startY, 2, height - 3);

        MenuView menuView = window.menuView();
        if (menuView != null) {
            g.setColor(Color.gray102);
            g.drawLine(1, startY + menuView.height() - 1, 2,
                       startY + menuView.height() - 1);
        }
    }

    /** Draws the InternalWindowBorder's right border.
      */
    public void drawRightBorder(Graphics g, int x, int y, int width,
                                int height) {
        Rect    borderRect;
        int     startY, bottomY;

        borderRect = Rect.newRect(width - rightMargin(), 0,
                                  rightMargin(), height);
        if (!g.clipRect().intersects(borderRect)) {
            Rect.returnRect(borderRect);
            return;
        }
        Rect.returnRect(borderRect);

        startY = topMargin();
        bottomY = height - bottomMargin();

        g.setColor(Color.gray102);
        g.drawLine(width - 1, startY, width - 1, bottomY);

        g.setColor(Color.gray153);
        g.drawLine(width - 2, startY, width - 2, bottomY - 1);

        MenuView menuView = window.menuView();
        if (menuView != null) {
            g.setColor(Color.gray102);
            g.drawPoint(x + width - 2, startY + menuView.height() - 1);
        }
    }

    /** Draws the InternalWindowBorder's bottom border.
      */
    public void drawBottomBorder(Graphics g, int x, int y, int width,
                                 int height) {
        Rect    borderRect;
        int     marginHeight, startY;

        borderRect = Rect.newRect(0, height - bottomMargin(),
                                  width, bottomMargin());
        if (!g.clipRect().intersects(borderRect)) {
            Rect.returnRect(borderRect);
            return;
        }
        Rect.returnRect(borderRect);

        marginHeight = bottomMargin();
        startY = height - marginHeight;

        g.setColor(Color.gray102);
        g.drawLine(1, height - 1, width - 1, height - 1);

        g.setColor(Color.gray153);
        g.drawLine(2, height - 2, width - 2, height - 2);

        g.setColor(Color.lightGray);
        g.fillRect(2, startY, width - 3, marginHeight - 2);

        // Fill right border--the part not filled by "draw right border",
        // if the margin is taller than the right resize image, which
        // otherwise covers the right edge of the bottom margin.
        g.setColor(Color.gray102);
        g.drawLine(width - 1, startY, width - 1, height - 1);
        g.setColor(Color.gray153);
        g.drawLine(width - 2, startY, width - 2, height - 2);


        if (window.isResizable() && leftResizeImage != null &&
            rightResizeImage != null) {
            leftResizeImage.drawAt(g, 0, height-leftResizeImage.height());
            rightResizeImage.drawAt(g, width - rightResizeImage.width(),
                                    height-rightResizeImage.height());
        }
    }

    /** Draws the InternalWindowBorder in the given Rect.  Calls
      * <b>drawTitleBar</b>, <b>drawLeftBorder</b>, <b>drawRightBorder</b> and
      * <b>drawBottomBorder</b>.
      */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        drawTitleBar(g, x, y, width, height);
        drawLeftBorder(g, x, y, width, height);
        drawRightBorder(g, x, y, width, height);
        drawBottomBorder(g, x, y, width, height);
    }

    /** Describes the InternalWindowBorder class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);
        info.addClass("netscape.application.InternalWindowBorder", 1);
        info.addField(WINDOW_KEY, OBJECT_TYPE);
    }

    /** Encodes the InternalWindowBorder instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);
        encoder.encodeObject(WINDOW_KEY, window);
    }

    /** Decodes the InternalWindowBorder instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);
        window = (InternalWindow)decoder.decodeObject(WINDOW_KEY);
    }
}
