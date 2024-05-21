// Graphics.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.lang.Thread;

/** Object subclass representing a drawing context, either onscreen or
  * offscreen. Onscreen Graphics are created either for the Applet frame
  * or ExternalWindows by passing a RootView to the constructor.
  * Offscreen Graphics are created for Bitmaps, by passing the Bitmap to
  * the constructor. Graphics maintain a stack of graphics states,
  * manipulated by the <b>pushState()</b> and <b>popState()</b> methods. When a
  * state is popped, the graphics state reverts to whatever it was before
  * the most recent push (i.e. color, font, clip rectangle, etc).<p>
  * You typically never construct Graphics instances directly, rather you
  * work with them inside of View <b>drawView()</b> methods to draw to
  * the screen.
  * @note 1.0 minor fix to catch null object during graphic debugging
  */

public class Graphics {
    Vector              graphicsStates = new Vector();
    java.awt.Graphics   primaryAwtGraphics;
    java.awt.Graphics   currentAwtGraphics;
    static int          graphicsCount = 0;
    int                 graphicsID = graphicsCount++;
    Rect                primaryClipRect;
    Bitmap              buffer;

    /** String display style. */
    public static final int     LEFT_JUSTIFIED = 0;
    /** String display style. */
    public static final int     CENTERED = 1;
    /** String display style. */
    public static final int     RIGHT_JUSTIFIED = 2;

/* constructors */

    /** Constructs a Graphics object suitable for drawing onscreen. It is
      * untranslated, so the (0, 0) coordinate is in the upper-left
      * corner of the RootView. You should explictly destroy Graphics
      * objects, using the <b>dispose()</b> method, because they consume
      * resources and are limited in number.
      */
    public Graphics(View view) {
        Rect absoluteBounds = view.convertRectToView(null, view.bounds);

        absoluteBounds.x -= view.bounds.x;
        absoluteBounds.y -= view.bounds.y;

        buffer = null;
        init(absoluteBounds, view.rootView().panel.getGraphics());
    }

    /** Constructs a Graphics object suitable for drawing offscreen. It is
      * untranslated, so the (0, 0) coordinate is in the Bitmap's upper-left
      * corner.
      */
    public Graphics(Bitmap aBitmap) {
        buffer = aBitmap;
        init(new Rect(0, 0, aBitmap.width(), aBitmap.height()),
             aBitmap.awtImage.getGraphics());
    }

    Graphics(Rect bounds, java.awt.Graphics awtGraphics) {
        init(new Rect(bounds), awtGraphics);
    }

    static Graphics newGraphics(View view) {
        return debugViewCount() == 0 ? new Graphics(view)
                                     : new DebugGraphics(view);
    }

    static Graphics newGraphics(Bitmap bitmap) {
        return debugViewCount() == 0 ? new Graphics(bitmap)
                                     : new DebugGraphics(bitmap);
    }

/* initializers */

     void init(Rect clipRect, java.awt.Graphics graphics) {
        GraphicsState state;

        state = new GraphicsState();
        graphicsStates.addElement(state);
        state.absoluteClipRect = clipRect;
        primaryAwtGraphics = graphics;
        currentAwtGraphics = primaryAwtGraphics;
        primaryAwtGraphics.clipRect(clipRect.x, clipRect.y, clipRect.width,
                                    clipRect.height);
        primaryClipRect = clipRect;
        state.xOffset = clipRect.x;
        state.yOffset = clipRect.y;
    }

    /** Creates an entry in the Graphics state stack. Each call to
      * <b>pushState()</b> should be paired with a call to <b>popState()</b>.
      * Any changes to the Graphics object between the calls will be flushed
      * after the <b>popState()</b>.
      * @see #popState
      */
    public void pushState() {
        GraphicsState lastState = state(), newState;

        if (lastState != null) {
            newState = (GraphicsState)lastState.clone();
        } else {
            // THIS SHOULD NEVER HAPPEN ALERT!
            newState = new GraphicsState();
        }

        graphicsStates.addElement(newState);
    }

    final void restoreAwtGraphics(java.awt.Graphics awtGraphics,
                                          GraphicsState state) {
        if (state.font != null) {
            if (state.font.wasDownloaded())
                awtGraphics.setFont(null);
            else
                awtGraphics.setFont(state.font._awtFont);
        }
        if (state.color == null) {
            awtGraphics.setColor(null);
        } else {
            awtGraphics.setColor(state.color._color);
            if (state.xorColor != null)
                awtGraphics.setXORMode(state.xorColor._color);
            else
                awtGraphics.setPaintMode();
        }
    }

    /** Restores the Graphics object to its condition before the most recent
      * <b>pushState()</b> call. All fonts, colors, clipping rectangles and
      * translations will be restored.
      */
    public void popState() {
        GraphicsState state = state();

        graphicsStates.removeLastElement();

        if (state.awtGraphics != null) {
            state.awtGraphics.dispose();
            currentAwtGraphics = awtGraphics();
        }

        state = state();
        if (state != null)
            restoreAwtGraphics(currentAwtGraphics, state);
    }

    final GraphicsState state() {
        return (GraphicsState)graphicsStates.lastElement();
    }

    java.awt.Graphics awtGraphics() {
        int iState = graphicsStates.count();

        while (iState-- > 0) {
            GraphicsState state;

            state = (GraphicsState)graphicsStates.elementAt(iState);
            if (state.awtGraphics != null)
                return state.awtGraphics;
        }
        return primaryAwtGraphics;
    }

    /** Sets the Font used for text drawing operations.
      */
    public void setFont(Font aFont) {
        GraphicsState state = state();

        state.font = aFont;

        if (aFont == null || aFont.wasDownloaded())
            currentAwtGraphics.setFont(null);
        else
            currentAwtGraphics.setFont(aFont._awtFont);
    }

    /** Returns the Font used for drawing operations.
      * @see #setFont
      */
    public Font font() {
        return state().font;
    }

    /** Sets the color to be used for drawing and filling lines and shapes.
      */
    public void setColor(Color aColor) {
        GraphicsState state = state();

        state.color = aColor;
        currentAwtGraphics.setColor(aColor == null ? null : aColor._color);
    }

    /** Returns the color used for drawing lines and filling regions.
      * @see #setColor
      */
    public Color color() {
        return state().color;
    }

    /** Alters the coordinate system so that all drawing, filling, and
      * clipping operations implicitly have <b>x</b> and <b>y</b> added to
      * their positions. If there is any current translation, the total
      * translation is the sum of the old and new translations.
      * @see #xTranslation
      * @see #yTranslation
      * @see #translation
      */
    public void translate(int x, int y) {
        GraphicsState state = state();

        state.xOffset += x;
        state.yOffset += y;
        state.clipRect = null;
    }

    /** Returns the X-coordinate of the Graphics' coordinate system origin.
      * @see #translate
      */
    public int xTranslation() {
        GraphicsState state = state();

        return state.xOffset;
    }

    /** Returns the Y-coordinate of the Graphics' coordinate system origin.
      * @see #translate
      */
    public int yTranslation() {
        GraphicsState state = state();

        return state.yOffset;
    }

    /** Returns the amount of translation performed on the current graphics
      * context.  Each <b>pushState()</b> creates a context with no
      * translation.  Subsequent calls to <b>translate</b> offset all
      * drawing operations by the aggregate amount of translation.
      * @see #translate
      */
    public Point translation() {
        GraphicsState state = state();

        return new Point(state.xOffset, state.yOffset);
    }

    /** Sets the rectangle within which drawing can occur.  Any drawing
      * occurring outside of this rectangle will not appear. If
      * <b>intersect</b> is <b>true</b>, this method intersects
      * <b>rect</b> with the current clipping rectangle to produce the new
      * clipping rectangle.
      */
    public void setClipRect(Rect rect, boolean intersect) {
        GraphicsState state = state();
        Rect clipRect, oldClipRect = absoluteClipRect(), newClipRect;

        if (rect == null) {
            clipRect = primaryClipRect;
            newClipRect = clipRect;
            clipRect = new Rect(clipRect);
        } else {
            newClipRect = rect;
            clipRect = new Rect(rect);
            clipRect.moveBy(state.xOffset, state.yOffset);
        }
        if (intersect) {
            clipRect.intersectWith(oldClipRect);
        }

        if (!clipRect.equals(oldClipRect)) {

            if (!intersect && (state.awtGraphics != null)) {
                state.awtGraphics.dispose();
                state.awtGraphics = null;
            }

            if (state.awtGraphics == null) {
                state.awtGraphics = primaryAwtGraphics.create();
                currentAwtGraphics = state.awtGraphics;

                // hack to get around a bug in the interaction between
                // .../src/solaris/sun/sun/awt/motif/X11Graphics.java's
                // create() and setColor()

                if (state.color != null) {
                    currentAwtGraphics.setColor(Color.white._color);
                    currentAwtGraphics.setColor(state.color._color);
                }
            }

            currentAwtGraphics.clipRect(clipRect.x,
                                        clipRect.y,
                                        clipRect.width,
                                        clipRect.height);
            state.absoluteClipRect = clipRect;
            state.clipRect = null;
            restoreAwtGraphics(currentAwtGraphics, state);
        }
    }

    /** Sets the rectangle within which drawing can occur.  Any drawing
      * occurring outside of this rectangle will not appear.  Intersects
      * <b>rect</b> with the current clipping rectangle to produce the new
      * clipping rectangle.
      * @see #setClipRect(Rect, boolean)
      */
    public void setClipRect(Rect rect) {
        setClipRect(rect, true);
    }

    /** Returns the Graphics object's current clipping rectangle.  All drawing
      * operations are clipped to this Rect.
      * @see #setClipRect
      */
    public Rect clipRect() {
        GraphicsState state = state();

        if (state.clipRect != null)
            return state.clipRect;
        else {
            int iState = graphicsStates.count();

            while (iState-- > 0) {
                GraphicsState clipState
                            = (GraphicsState)graphicsStates.elementAt(iState);
                if (clipState.absoluteClipRect != null) {
                    state.clipRect = new Rect(clipState.absoluteClipRect);

                    state.clipRect.moveBy(-state.xOffset, -state.yOffset);
                    return state.clipRect;
                }
            }
            return null;
        }
    }

    /** Returns the Graphics object's current clipping rectangle, in
      * absolute coordinates, that is, those of the RootView or Bitmap the
      * Graphics is drawing into.
      * @see #clipRect
      */
    Rect absoluteClipRect() {
        int iState = graphicsStates.count();

        while (iState-- > 0) {
            GraphicsState state;

            state = (GraphicsState)graphicsStates.elementAt(iState);
            if (state.absoluteClipRect != null) {
                return state.absoluteClipRect;
            }
        }
        return null;
    }

    /** Clears the clipping rectangle so that drawing can occur anywhere
      * within the Graphics' drawing region.
      * @see #setClipRect
      * @see #clipRect
      */
    public void clearClipRect() {
        setClipRect(null, false);
    }


/* attributes */

    /** Returns the Bitmap associated with the Graphics.  Returns
      * <b>null</b> if the Graphics is not drawing into a Bitmap.
      */
    public Bitmap buffer() {
        return buffer;
    }

    /** Returns <b>true</b> if the Graphics was created from a Bitmap.
      */
    public boolean isDrawingBuffer() {
        return buffer != null;
    }


/* actions */

    /** Destroys the Graphics object and any resources associated with it.
      */
    public void dispose() {
        int iState = graphicsStates.count();

/* THIS SHOULD NEVER HAPPEN ALERT!
        if (iState > 1) {
            System.err.println("Disposing of graphics with states: " + this);
            Thread.dumpStack();
        }
*/
        while (iState-- > 0) {
            GraphicsState state;

            state = (GraphicsState) graphicsStates.elementAt(iState);
            if (state.awtGraphics != null)
                state.awtGraphics.dispose();
        }
        graphicsStates.removeAllElements();
        primaryAwtGraphics.dispose();
        primaryAwtGraphics = null;
        currentAwtGraphics = null;
    }

    /** Forces any pending drawing operations to be sent to the native
      * graphics system for onscreen drawing.
      */
    public void sync() {
        Application.application().syncGraphics();
    }


/* color, paint mode */

    /** Sets the paint mode to overwrite the destination with the current
      * color.
      * @see #setXORMode
      */
    public void setPaintMode() {
        GraphicsState state = state();

        state.xorColor = null;
        currentAwtGraphics.setPaintMode();
    }

    /** Sets the paint mode to alternate between the current color and
      * <b>aColor</b>.
      * @see #setPaintMode
      */
    public void setXORMode(Color aColor) {
        if (aColor == null)
            setPaintMode();
        else {
            GraphicsState state = state();

            state.xorColor = aColor;
            currentAwtGraphics.setXORMode(aColor._color);
        }
    }


/* rects */

    /** Draws <b>aRect</b> using the current color.
      */
    public void drawRect(Rect aRect) {
        drawRect(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /** Draws the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) using
      * the current color.
      */
    public void drawRect(int x, int y, int width, int height) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;

       currentAwtGraphics.drawRect(x, y, width - 1, height - 1);
    }

    /** Fills <b>aRect</b> using the current color.
      */
    public void fillRect(Rect aRect) {
        fillRect(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /** Fills the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) using the current
      * color.
      */
    public void fillRect(int x, int y, int width, int height) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        currentAwtGraphics.fillRect(x, y, width, height);
    }

/* rounded rects */

    /** Draws a rectangle with rounded corners in <b>aRect</b>,
      * as determined by <b>arcWidth</b> and <b>arcHeight</b>.
      */
    public void drawRoundedRect(Rect aRect, int arcWidth, int arcHeight) {
        drawRoundedRect(aRect.x, aRect.y, aRect.width, aRect.height,
                        arcWidth, arcHeight);
    }

    /** Draws a rectangle with rounded corners in the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>), as determined by
      * <b>arcWidth</b> and <b>arcHeight</b>.
      */
    public void drawRoundedRect(int x, int y, int width, int height,
                                int arcWidth, int arcHeight) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        currentAwtGraphics.drawRoundRect(x, y, width - 1, height - 1,
                                         arcWidth, arcHeight);
    }

    /** Fills a rectangle with rounded corners in <b>aRect</b>,
      * as determined by <b>arcWidth</b> and <b>arcHeight</b>.
      */
    public void fillRoundedRect(Rect aRect, int arcWidth, int arcHeight) {
        fillRoundedRect(aRect.x, aRect.y, aRect.width, aRect.height,
                         arcWidth, arcHeight);
    }

    /** Fills a rectangle with rounded corners in the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>), as determined by
      * <b>arcWidth</b> and <b>arcHeight</b>.
      */
    public void fillRoundedRect(int x, int y, int width, int height,
                                int arcWidth, int arcHeight) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        currentAwtGraphics.fillRoundRect(x, y, width, height, arcWidth,
                                         arcHeight);
    }



/* lines */

    /** Draws a line from the point (<b>x1</b>, <b>y1</b>) to the
      * point (<b>x2</b>, <b>y2</b>) in the current color.
      */
    public void drawLine(int x1, int y1, int x2, int y2) {
        GraphicsState state = state();

        x1 += state.xOffset;
        y1 += state.yOffset;
        x2 += state.xOffset;
        y2 += state.yOffset;
        currentAwtGraphics.drawLine(x1, y1, x2, y2);
    }



/* points */

    /** Draws the point (<b>x</b>, <b>y</b>) in the current color.
      */
    public void drawPoint(int x, int y) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        currentAwtGraphics.drawLine(x, y, x, y);
    }

/* ovals */

    /** Draws an oval inside <b>aRect</b> in the current color.
      */
    public void drawOval(Rect aRect) {
        drawOval(aRect.x, aRect.y, aRect.width - 1, aRect.height - 1);
    }

    /** Draws an oval inside the rect
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) in the
      * current color.
      */
    public void drawOval(int x, int y, int width, int height) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        currentAwtGraphics.drawOval(x, y, width, height);
    }

    /** Fills an oval inside <b>aRect</b> in the current color.
      */
    public void fillOval(Rect aRect) {
        fillOval(aRect.x, aRect.y, aRect.width, aRect.height);
    }

    /** Fills an oval inside the rect
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) in the
      * current color.
      */
    public void fillOval(int x, int y, int width, int height) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        currentAwtGraphics.fillOval(x, y, width, height);
    }


/* arcs */

    /** Draws an arc in <b>aRect</b> from <b>startAngle</b> for
      * <b>arcAngle</b> degrees.
      */
    public void drawArc(Rect aRect, int startAngle, int arcAngle) {
        drawArc(aRect.x, aRect.y, aRect.width, aRect.height,
                startAngle, arcAngle);
    }

    /** Draws an arc in <b>aRect</b> from <b>startAngle</b> for
      * <b>arcAngle</b> degrees.
      */
    public void drawArc(int x, int y, int width, int height, int startAngle,
                        int arcAngle) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        currentAwtGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    /** Fills an arc in <b>aRect</b> from <b>startAngle</b> for
      * <b>arcAngle</b> degrees.
      */
    public void fillArc(Rect aRect, int startAngle, int arcAngle) {
       fillArc(aRect.x, aRect.y, aRect.width, aRect.height,
                    startAngle, arcAngle);
    }

    /** Fills an arc in <b>aRect</b> from <b>startAngle</b> for
      * <b>arcAngle</b> degrees.
      */
    public void fillArc(int x, int y, int width, int height, int startAngle,
                        int arcAngle) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        currentAwtGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    /** Draws the polygon described by <b>xPoints</b> and <b>yPoints</b>
      * using the current color. Both arrays must have at least
      * <b>nPoints</b> integers.
      */
    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        GraphicsState state = state();

        // This avoids allocating translated arrays of points
        if (state.xOffset != 0 || state.yOffset != 0) {
            currentAwtGraphics.translate(state.xOffset, state.yOffset);
            currentAwtGraphics.drawPolygon(xPoints, yPoints, nPoints);
            currentAwtGraphics.translate(-state.xOffset, -state.yOffset);
        } else
            currentAwtGraphics.drawPolygon(xPoints, yPoints, nPoints);
    }

    /** Draws <b>polygon</b> using the current color.
      */
    public void drawPolygon(Polygon polygon) {
        drawPolygon(polygon.xPoints, polygon.yPoints, polygon.numPoints);
    }

    /** Fills the polygon described by <b>xPoints</b> and <b>yPoints</b>
      * using the current color. Both arrays must have at least
      * <b>nPoints</b> integers.
      */
    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        GraphicsState state = state();

        // This avoids allocating translated arrays of points
        if (state.xOffset != 0 || state.yOffset != 0) {
            currentAwtGraphics.translate(state.xOffset, state.yOffset);
            currentAwtGraphics.fillPolygon(xPoints, yPoints, nPoints);
            currentAwtGraphics.translate(-state.xOffset, -state.yOffset);
        } else
            currentAwtGraphics.fillPolygon(xPoints, yPoints, nPoints);
    }

    /** Fills <b>polygon</b> using the current color.
      */
    public void fillPolygon(Polygon polygon) {
        fillPolygon(polygon.xPoints, polygon.yPoints, polygon.numPoints);
    }

    /** Displays <b>bitmap</b> at the point (<b>x</b>, <b>y</b>).
      */
    public void drawBitmapAt(Bitmap bitmap, int x, int y) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;

        if (bitmap == null) {
            return;
        }

        if (!bitmap.loadsIncrementally()) {
            bitmap.loadData();
        }
        if (!bitmap.isValid()) {
            System.err.println("Graphics.drawBitmapAt() - Invalid bitmap: " +
                               bitmap.name());
            return;
        }

        currentAwtGraphics.drawImage(bitmap.awtImage, x, y,
                                     bitmap.bitmapObserver());
    }

    /** Draws <b>bitmap</b> at the point (<b>x</b>, <b>y</b>), scaled in the
      * X-dimension to have width <b>width</b> and the Y-dimension to
      * have height <b>height</b>.
      */
    public void drawBitmapScaled(Bitmap bitmap, int x, int y, int width,
        int height) {
        GraphicsState state = state();

        if (bitmap == null) {
            return;
        }

        x += state.xOffset;
        y += state.yOffset;

        bitmap.createScaledVersion(width, height);

        if (!bitmap.isValid()) {
            System.err.println(
                        "Graphics.drawBitmapScaled() - Invalid bitmap: " +
                        bitmap.name());
            return;
        }

        currentAwtGraphics.drawImage(bitmap.awtImage, x, y, width, height,
                                     bitmap.bitmapObserver());
    }

    /** Draws <b>aString</b> in the rectangle defined by
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>).
      * <b>justification</b> specifies the text's justification, one of
      * LEFT_JUSTIFIED, CENTERED, or RIGHT_JUSTIFIED.
      * <b>drawStringInRect()</b> does not clip to the rectangle, but instead
      * uses this rectangle and the desired justification to compute the point
      * at which to begin drawing the text.
      * @see #drawString
      */
    public void drawStringInRect(String aString, int x, int y,
                                 int width, int height, int justification) {
        FontMetrics  fontMetrics;
        int          drawWidth, startX, startY, delta;

        if (font() == null) {
            throw new InconsistencyException("No font set");
        }
        fontMetrics = font().fontMetrics();
        if (fontMetrics == null) {
            throw new InconsistencyException("No metrics for Font " + font());
        }

        drawWidth = fontMetrics.stringWidth(aString);
        if (drawWidth > width) {
            drawWidth = width;
        }
        if (justification == CENTERED) {
            startX = x + (width - drawWidth) / 2;
        } else if (justification == RIGHT_JUSTIFIED) {
            startX = x + width - drawWidth;
        } else {
            startX = x;
        }

        delta = (height - fontMetrics.ascent() - fontMetrics.descent()) / 2;
        if (delta < 0) {
            delta = 0;
        }

        startY = y + height - delta - fontMetrics.descent();

        drawString(aString, startX, startY);
    }

    /** Draws <b>aString</b> in <b>aRect</b>, according to
      * the justification <b>justification</b>.
      * @see #drawStringInRect(String, Font, int, int, int, int, int)
      */
    public void drawStringInRect(String aString,
                                 Rect aRect,
                                 int justification) {
        if (aRect == null) {
            throw new InconsistencyException(
                                    "Null Rect passed to drawStringInRect.");
        }

        drawStringInRect(aString, aRect.x, aRect.y, aRect.width,
                         aRect.height, justification);
    }

    /** Draws <b>aString</b> at the point (<b>x</b>, <b>y</b>) using the
      * current font and color.
      * @see #drawStringInRect
      */
    public void drawString(String aString, int x, int y) {
        Font                 font;
        FontMetrics          fontMetrics;
        Vector               glyphVector;
        int                  widthsArray[], i, widthsArrayBase, index;

        if (aString == null) {
            throw new InconsistencyException(
                                    "Null String passed to drawString.");
        }

        font = font();

        if (font == null || !font.wasDownloaded()) {
            GraphicsState state = state();

            x += state.xOffset;
            y += state.yOffset;
            currentAwtGraphics.drawString(aString, x, y);
        } else {
            fontMetrics = font.fontMetrics();
            widthsArray = fontMetrics.widthsArray();
            widthsArrayBase = fontMetrics.widthsArrayBase();
            glyphVector = font.glyphVector();

            y -= fontMetrics.ascent();

            for (i = 0; i < aString.length(); i++) {
                index = (int)aString.charAt(i) - widthsArrayBase;
                if (index < 0 ||
                    index >= widthsArray.length - widthsArrayBase) {
                    if (aString.charAt(i) == ' ') {
                        x += widthsArray[' '];
                    }
                    continue;
                }

                drawBitmapAt((Bitmap)glyphVector.elementAt(index), x, y);
                x += widthsArray[(int)aString.charAt(i)];
            }
        }
    }

    /** Draws <b>length</b> bytes of <b>data</b>, beginning <b>offset</b> bytes
      * into the array, using the current font and color, at the point
      * (<b>x</b>, <b>y</b>).
      */
    public void drawBytes(byte data[], int offset, int length, int x, int y) {
        Font font = font();
        FontMetrics          fontMetrics;
        Vector               glyphVector;
        int                     widthsArray[], i, widthsArrayBase, index;

        if (font == null || !font.wasDownloaded()) {
            GraphicsState state = state();

            x += state.xOffset;
            y += state.yOffset;
            currentAwtGraphics.drawBytes(data, offset, length, x, y);
        } else {
            fontMetrics = font.fontMetrics();
            widthsArray = fontMetrics.widthsArray();
            widthsArrayBase = fontMetrics.widthsArrayBase();
            glyphVector = font.glyphVector();

            y -= fontMetrics.ascent();

            for (i = 0; i < length; i++) {
                index = (int)data[i] - widthsArrayBase;
                if (index < 0 ||
                    index >= widthsArray.length - widthsArrayBase) {
                    if ((char)data[i] == ' ') {
                        x += widthsArray[' '];
                    }
                    continue;
                }

                drawBitmapAt((Bitmap)glyphVector.elementAt(index), x, y);
                x += widthsArray[(int)data[i]];
            }
        }
    }

    /** Draws <b>length</b> characters of <b>data</b>, beginning <b>offset</b>
      * characters into the array, using the current font and color, at the
      * point (<b>x</b>, <b>y</b>).
      */
    public void drawChars(char data[], int offset, int length, int x, int y) {
        Font font = font();
        FontMetrics          fontMetrics;
        Vector               glyphVector;
        int                     widthsArray[], i, widthsArrayBase, index;

        if (font == null) {
            return;
        } else if (!font.wasDownloaded()) {
            GraphicsState state = state();

            x += state.xOffset;
            y += state.yOffset;
            currentAwtGraphics.drawChars(data, offset, length, x, y);
        } else {
            fontMetrics = font.fontMetrics();
            widthsArray = fontMetrics.widthsArray();
            widthsArrayBase = fontMetrics.widthsArrayBase();
            glyphVector = font.glyphVector();

            y -= fontMetrics.ascent();

            for (i = 0; i < length; i++) {
                index = (int)data[i] - widthsArrayBase;
                if (index < 0 ||
                    index >= widthsArray.length - widthsArrayBase) {
                    if ((char)data[i] == ' ') {
                        x += widthsArray[' '];
                    }
                    continue;
                }

                drawBitmapAt((Bitmap)glyphVector.elementAt(index), x, y);
                x += widthsArray[(int)data[i]];
            }
        }
    }

    void copyArea(int x, int y, int width, int height, int destX, int destY) {
        GraphicsState state = state();

        x += state.xOffset;
        y += state.yOffset;
        destX += state.xOffset;
        destY += state.yOffset;
        currentAwtGraphics.copyArea(x, y, width, height, destX - x, destY - y);
    }

    /** Returns the Graphic's String representation.
      */
    public String toString() {
        StringBuffer stringBuffer;
        String          tmpString;
        int             iState = graphicsStates.count();

        tmpString = (buffer != null) ? " for Bitmap " : " ";
        stringBuffer = new StringBuffer("Graphics" + tmpString + iState +
                                  " states:\n");
        while (iState-- > 0) {
            GraphicsState state;

            state = (GraphicsState)graphicsStates.elementAt(iState);
            stringBuffer.append(state.toString());
            stringBuffer.append("\n");
        }
        return stringBuffer.toString();
    }

    String toShortString() {
        StringBuffer buffer = new StringBuffer("Graphics" + (isDrawingBuffer() ? "<B>" : "") + "(" + graphicsID + ")");
        int stateDepth = graphicsStates.count();
        while (stateDepth-- > 0)
            buffer.append("-");
        buffer.append(">");
        return buffer.toString();
    }

    /** Activates Graphics debugging options.  Graphics instances only support
      * a <b>debugOption</b> value of <b>0</b>.
      * The DebugGraphics subclass supports other values to enable diagnostic
      * output on graphics operations.
      * @see DebugGraphics
      */
    public void setDebugOptions(int debugOptions) {
        if (debugOptions != 0) {
            throw new InconsistencyException("Can't set non zero debugOptions on a Graphics.  Use DebugGraphics instead.");
        }
    }

    /** Returns the current debug option setting.  Graphics instances only
      * a debug option value of <b>0</b>.
      * The DebugGraphics subclass supports other values to enable diagnostic
      * output on graphics operations.
      * @see #setDebugOptions
      * @see DebugGraphics
      */
    public int debugOptions() {
        return 0;
    }

    static DebugGraphicsInfo info() {
        Application app = Application.application();

        if (app.debugGraphicsInfo == null) {
            app.debugGraphicsInfo = new DebugGraphicsInfo();
        }
        return app.debugGraphicsInfo;
    }

    void setDebug(View view) {
    }

    static void setViewDebug(View view, int debugOptions) {
        DebugGraphicsInfo info = info();

        info.setViewDebug(view, debugOptions);
    }

    static int shouldViewDebug(View view) {
        Application app = Application.application();

        if (app.debugGraphicsInfo == null) {
            return 0;
        } else {
            DebugGraphicsInfo info = app.debugGraphicsInfo;
            int debugOptions = 0;

            while (view != null) {
                debugOptions |= info.viewDebug(view);
                view = view._superview;
            }

            return debugOptions;
        }
    }

    static int viewDebug(View view) {
        Application app = Application.application();

        if (app.debugGraphicsInfo == null) {
            return 0;
        } else {
            DebugGraphicsInfo info = app.debugGraphicsInfo;

            return info.viewDebug(view);
        }
    }

    static int debugViewCount() {
        Application app = Application.application();

        if (app != null && app.debugGraphicsInfo != null &&
            app.debugGraphicsInfo.viewToDebug != null) {
            return app.debugGraphicsInfo.viewToDebug.count();
        } else {
            return 0;
        }
    }
}
