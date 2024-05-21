/**
 * SLWindowBorder.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 */
package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Subclass of Border that's used on SLWindows. Since SLWindows are always
 * created with this border, it shouldn't be necessary to access this class
 * outside of this package.
 */
class SLWindowBorder extends Border {

    private static final int WINDOW_BORDER_COLOR_WIDTH = 
        StoneLook.windowBorderThickness();

    private static Color highlightColor
        = StoneLook.windowBorderHighlightColor();
    private static Color shadowColor
        = StoneLook.windowBorderShadowColor();

    private static Color backgroundHighlights[] = 
        StoneLook.windowBorderBackgroundHighlightColors();
    private static Color backgroundShadows[] =
        StoneLook.windowBorderBackgroundShadowColors();
    private static Color rightColors[] =
        StoneLook.windowBorderRightColors(); 

    private SLWindow myWindow = null;

/**
 * Creates a new SLWindowBorder for the specified SLWindow.
 */
    public SLWindowBorder(SLWindow window) {
        // gotta know what window we're on so we can draw title image properly
        myWindow = window;
    }

    /**
     * Draws colored bezel outside, background bezel inside, and window's
     * title image in the thick right side of the colored bezel.
     */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        
        int indentX, indentY;
        
        // get clip so we can check whether we need to draw anything at all
        Rect clipRect = g.clipRect();
        
        // see if we can short-circuit the entire thing
        // first bail out if clip rect is entirely outside border
        if (!clipRect.intersects(x, y, width, height)) {
            return;
        }
        
        // now bail out if clip rect is entirely inside border
        if (interiorRect(x, y, width, height).contains(clipRect)) {
            return;
        }

        
        // fill the entire border with a texture; much of this
        // will then be drawn over
        Bitmap texture = StoneLook.windowBorderTexture();
        if (texture != null) {
            texture.drawTiled(g, x, y, width, topMargin());
            texture.drawTiled(g, x, y, leftMargin(), height);
            texture.drawTiled(g, x + width - rightMargin(), y,
                                 rightMargin(), height);
            texture.drawTiled(g, x, y + height - bottomMargin(),
                                 width, bottomMargin());            
        }
        
        // fill the inner rect (donut since it's a border) that will have
        // background colors over it, so if the number of highlights doesn't
        // match the number of shadows, no border texture will show through
        Color backgroundColor = null;
        if (backgroundHighlights.length > 0) {
            backgroundColor = 
                backgroundHighlights[backgroundHighlights.length - 1];
        }
        else if (backgroundShadows.length > 0) {
            backgroundColor = backgroundShadows[0];
        }
        if (backgroundColor != null) {
            int donutLeft = x + WINDOW_BORDER_COLOR_WIDTH;
            int donutTop = y + WINDOW_BORDER_COLOR_WIDTH;
            int donutWidth = x + width - 2*WINDOW_BORDER_COLOR_WIDTH;
            int donutHeight = y + height - 2*WINDOW_BORDER_COLOR_WIDTH;
            Rect donutEdge = new Rect(donutLeft, donutTop,
                                      donutWidth, donutHeight);

            g.setColor(backgroundColor);

            // fill top and bottom edges of donut           
            donutEdge.height = topMargin() - WINDOW_BORDER_COLOR_WIDTH;
            g.fillRect(donutEdge);
            donutEdge.y = y + height - bottomMargin();
            donutEdge.height = bottomMargin() - WINDOW_BORDER_COLOR_WIDTH;
            g.fillRect(donutEdge);

            // restore donut bounds         
            donutEdge.y = donutTop;         
            donutEdge.height = donutHeight;
            
            // fill left and right edges of donut
            donutEdge.width = leftMargin() - WINDOW_BORDER_COLOR_WIDTH;
            g.fillRect(donutEdge);
            donutEdge.width = rightMargin() - WINDOW_BORDER_COLOR_WIDTH;
            donutEdge.x = x + width - rightMargin();
            g.fillRect(donutEdge);
        }

        // draw the background highlights on top and left
        for (int index = 0; index < backgroundHighlights.length; index += 1) {
            g.setColor(backgroundHighlights[index]);

            indentX = indentY = WINDOW_BORDER_COLOR_WIDTH + index;
            g.drawLine(x + indentX, y + indentY,
                       x + width - indentX, y + indentY);
            g.drawLine(x + indentX, y + indentY,
                       x + indentX, y + height - indentY);
        }

        // draw the background shadowing on bottom and right
        for (int index = 0; index < backgroundShadows.length; index += 1) {
            g.setColor(backgroundShadows[index]);

            indentX = indentY = 
                WINDOW_BORDER_COLOR_WIDTH + backgroundShadows.length - index;
            g.drawLine(x + indentX, y + height - indentY,
                       x + width - indentX, y + height - indentY);

            g.drawLine(x + width - indentX, y + indentY,
                       x + width - indentX, y + height - indentY);
        }

        // draw the highlight and shadow lines on top, left, and bottom
        g.setColor(highlightColor);
        g.drawLine(x, y, x + width - 1, y);
        g.drawLine(x, y, x, y + height - 1);
        g.drawLine(x + WINDOW_BORDER_COLOR_WIDTH,
                   y + height - WINDOW_BORDER_COLOR_WIDTH,
                   x + width - WINDOW_BORDER_COLOR_WIDTH,
                   y + height - WINDOW_BORDER_COLOR_WIDTH);

        g.setColor(shadowColor);
        indentX = indentY = WINDOW_BORDER_COLOR_WIDTH - 1;
        g.drawLine(x + indentX, y + indentY,
                   x + width - indentX, y + indentY);
        g.drawLine(x + indentX, y + indentY,
                   x + indentX, y + height - indentY);
        g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);


        // draw the highlights and shadows on the right
        for (int i = 0; i < rightColors.length; i += 1) {
            g.setColor(rightColors[i]);
            int lineX, lineY, lineMaxY;
            if (i < rightColors.length/2) {
                lineX = x + width - WINDOW_BORDER_COLOR_WIDTH + i;
                lineY = y + WINDOW_BORDER_COLOR_WIDTH;
                lineMaxY = y + height - WINDOW_BORDER_COLOR_WIDTH - 1;
            }
            else {
                lineX = x + width - (rightColors.length - i);
                lineY = y;
                lineMaxY = y + height - 1;
            }
            g.drawLine(lineX, lineY, lineX, lineMaxY);
        }

        // draw bitmaps in the corners
        Bitmap bitmap;
        bitmap = StoneLook.windowBorderNW();
        if (bitmap != null) {
            g.drawBitmapAt(bitmap, 0, 0);
        }
        bitmap = StoneLook.windowBorderNE();
        if (bitmap != null) {
            g.drawBitmapAt(bitmap, x + width - bitmap.width(), 0);
        }
        bitmap = StoneLook.windowBorderSW();
        if (bitmap != null) {
            g.drawBitmapAt(bitmap, x, y + height - bitmap.height());        
        }
        bitmap = StoneLook.windowBorderSE();
        if (bitmap != null) {
            g.drawBitmapAt(bitmap, x + width - bitmap.width(),
                                   y + height - bitmap.height());
        }
    }

    /** Overridden to compute margin properly */
    public int bottomMargin() {
        return WINDOW_BORDER_COLOR_WIDTH + backgroundShadows.length;
    }

    /** Overridden to compute margin properly */
    public int leftMargin() {
        return WINDOW_BORDER_COLOR_WIDTH + backgroundHighlights.length;
    }

    /** Overridden to compute margin properly */
    public int rightMargin() {
        return WINDOW_BORDER_COLOR_WIDTH + backgroundShadows.length;
    }

    /** Overridden to compute margin properly */
    public int topMargin() {
        return WINDOW_BORDER_COLOR_WIDTH + backgroundHighlights.length;
    }
}
