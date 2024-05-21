// DebugGraphics.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

/** Graphics subclass supporting graphics debugging. Overrides most methods
  * from Graphics. You rarely create a DebugGraphics, instead a View creates
  * them in response to use of its <b>setDebugGraphics()</b> method, which
  * registers that View, and its subviews, for graphics debugging.
  * @see View#setDebugGraphics
  */
public class DebugGraphics extends Graphics {
    static ExternalWindow       debugWindow;
    /** Log graphics operations. */
    public static final int     LOG_OPTION   = 1 << 0;
    /** Flash graphics operations. */
    public static final int     FLASH_OPTION = 1 << 1;
    /** Show buffered operations in an ExternalWindow. */
    public static final int     BUFFERED_OPTION = 1 << 2;
    /** Don't debug graphics operations. */
    public static final int     NONE_OPTION = -1;


    /** Constructs a DebugGraphics for <b>view</b>. */
    public DebugGraphics(View view) {
        super(view);
        setDebugOptions(view.shouldDebugGraphics());
    }

    /** Constructs a DebugGraphics for <b>aBitmap</b>. */
    public DebugGraphics(Bitmap aBitmap) {
        super(aBitmap);
    }

    /** Sets the Color used to flash drawing operations.
      */
    public static void setFlashColor(Color flashColor) {
        info().flashColor = flashColor;
    }

    /** Returns the Color used to flash drawing operations.
      * @see #setFlashColor
      */
    public static Color flashColor() {
        return info().flashColor;
    }

    /** Sets the time delay of drawing operation flashing.
      */
    public static void setFlashTime(int flashTime) {
        info().flashTime = flashTime;
    }

    /** Returns the time delay of drawing operation flashing.
      * @see #setFlashTime
      */
    public static int flashTime() {
        return info().flashTime;
    }

    /** Sets the number of times that drawing operations will flash.
      */
    public static void setFlashCount(int flashCount) {
        info().flashCount = flashCount;
    }

    /** Returns the number of times that drawing operations will flash.
      * @see #setFlashCount
      */
    public static int flashCount() {
        return info().flashCount;
    }

    /** Sets the stream to which the DebugGraphics logs drawing operations.
      */
    public static void setLogStream(java.io.PrintStream stream) {
        info().stream = stream;
    }

    /** Returns the stream to which the DebugGraphics logs drawing operations.
      * @see #setLogStream
      */
    public static java.io.PrintStream logStream() {
        return info().stream;
    }

    /** Creates an entry in the Graphics state stack. Each call to
      * <b>pushState()</b> should be paired with a call to <b>popState()</b>.
      * Any changes to the Graphics object between the calls will be flushed
      * after the <b>popState()</b>.
      * @see #popState
      */
    public void pushState() {
        if (state().debugLog()) {
            info().log(toShortString() + " -> state");
        }
        super.pushState();
    }

    /** Restores the Graphics object to its condition before the most recent
      * <b>pushState()</b> call. All fonts, colors, clipping rectangles and
      * translations will be restored.
      */
    public void popState() {
        if (state().debugLog()) {
            info().log(toShortString() + " <- state");
        }
        super.popState();
    }

    /** Sets the Font used for text drawing operations.
      */
    public void setFont(Font aFont) {
        if (state().debugLog()) {
            info().log(toShortString() + " Setting font: " + aFont);
        }
        super.setFont(aFont);
    }

    /** Sets the color to be used for drawing and filling lines and shapes.
      */
    public void setColor(Color aColor) {
        if (state().debugLog()) {
            info().log(toShortString() + " Setting color: " + aColor);
        }
        super.setColor(aColor);
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

        if (state.debugLog()) {
            info().log(toShortString() +
                " Translating by: " + new Point(x, y) +
                " to: " + new Point(x + state.xOffset, y + state.yOffset));
        }
        super.translate(x, y);
    }

    /** Sets the rectangle within which drawing can occur.  Any drawing
      * occurring outside of this rectangle will not appear. If
      * <b>intersect</b> is <b>true</b>, this method intersects
      * <b>rect</b> with the current clipping rectangle to produce the new
      * clipping rectangle.
      */
    public void setClipRect(Rect rect, boolean intersect) {
        GraphicsState state = state();

        super.setClipRect(rect, intersect);

        if (state().debugLog()) {
            info().log(toShortString() +
                " Setting clipRect: " + rect +
                " New clipRect: " + clipRect());
        }
    }

    /** Sets the paint mode to overwrite the destination with the current
      * color.
      * @see #setXORMode
      */
    public void setPaintMode() {
        if (state().debugLog()) {
            info().log(toShortString() + " Setting paint mode");
        }
        super.setPaintMode();
    }

    /** Sets the paint mode to alternate between the current color and
      * <b>aColor</b>.
      * @see #setPaintMode
      */
    public void setXORMode(Color aColor) {
        if (state().debugLog()) {
            info().log(toShortString() + " Setting XOR mode: " + aColor);
        }
        super.setXORMode(aColor);
    }

    /** Draws the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) using
      * the current color.
      */
    public void drawRect(int x, int y, int width, int height) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing rect: " +
                      new Rect(x, y, width, height));
        }

        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawRect(x, y, width, height);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.drawRect(x, y, width, height);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.drawRect(x, y, width, height);
    }

    /** Fills the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) using the current
      * color.
      */
    public void fillRect(int x, int y, int width, int height) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Filling rect: " +
                      new Rect(x, y, width, height));
        }

        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.fillRect(x, y, width, height);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.fillRect(x, y, width, height);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.fillRect(x, y, width, height);
    }

    /** Draws a rectangle with rounded corners in the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>), as determined by
      * <b>arcWidth</b> and <b>arcHeight</b>.
      */
    public void drawRoundedRect(int x, int y, int width, int height,
                                int arcWidth, int arcHeight) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing rounded rect: " +
                      new Rect(x, y, width, height) +
                      " arcWidth: " + arcWidth +
                      " archHeight: " + arcHeight);
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawRoundedRect(x, y, width, height,
                                              arcWidth, arcHeight);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.drawRoundedRect(x, y, width, height,
                                      arcWidth, arcHeight);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.drawRoundedRect(x, y, width, height, arcWidth, arcHeight);
    }

    /** Fills a rectangle with rounded corners in the rectangle
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>), as determined by
      * <b>arcWidth</b> and <b>arcHeight</b>.
      */
    public void fillRoundedRect(int x, int y, int width, int height,
                                int arcWidth, int arcHeight) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing rounded rect: " +
                      new Rect(x, y, width, height) +
                      " arcWidth: " + arcWidth +
                      " archHeight: " + arcHeight);
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.fillRoundedRect(x, y, width, height,
                                              arcWidth, arcHeight);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.fillRoundedRect(x, y, width, height,
                                      arcWidth, arcHeight);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.fillRoundedRect(x, y, width, height, arcWidth, arcHeight);
    }

    /** Draws a line from the point (<b>x1</b>, <b>y1</b>) to the
      * point (<b>x2</b>, <b>y2</b>) in the current color.
      */
    public void drawLine(int x1, int y1, int x2, int y2) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing line: " +
                      new Rect(x1, y1, x2, y2));
        }

        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawLine(x1, y1, x2, y2);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.drawLine(x1, y1, x2, y2);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.drawLine(x1, y1, x2, y2);
    }

    /** Draws the point (<b>x</b>, <b>y</b>) in the current color.
      */
    public void drawPoint(int x, int y) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing point: " +
                      new Point(x, y));
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawLine(x, y, x, y);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.drawLine(x, y, x, y);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.drawLine(x, y, x, y);
    }

    /** Draws an oval inside the rect
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) in the
      * current color.
      */
    public void drawOval(int x, int y, int width, int height) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing oval: " +
                      new Rect(x, y, width, height));
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawOval(x, y, width, height);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.drawOval(x, y, width, height);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.drawOval(x, y, width, height);
    }

    /** Fills an oval inside the rect
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>) in the
      * current color.
      */
    public void fillOval(int x, int y, int width, int height) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Filling oval: " +
                      new Rect(x, y, width, height));
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.fillOval(x, y, width, height);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.fillOval(x, y, width, height);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.fillOval(x, y, width, height);
    }

    /** Draws an arc in <b>aRect</b> from <b>startAngle</b> for
      * <b>arcAngle</b> degrees.
      */
    public void drawArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing arc: " +
                      new Rect(x, y, width, height) +
                      " startAngle: " + startAngle +
                      " arcAngle: " + arcAngle);
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawArc(x, y, width, height,
                                      startAngle, arcAngle);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.drawArc(x, y, width, height, startAngle, arcAngle);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    /** Fills an arc in <b>aRect</b> from <b>startAngle</b> for
      * <b>arcAngle</b> degrees.
      */
    public void fillArc(int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Filling arc: " +
                      new Rect(x, y, width, height) +
                      " startAngle: " + startAngle +
                      " arcAngle: " + arcAngle);
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.fillArc(x, y, width, height,
                                      startAngle, arcAngle);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.fillArc(x, y, width, height, startAngle, arcAngle);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    /** Draws the polygon described by <b>xPoints</b> and <b>yPoints</b>
      * using the current color. Both arrays must have at least
      * <b>nPoints</b> integers.
      */
    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing polygon: " +
                      " nPoints: " + nPoints +
                      " X's: " + xPoints +
                      " Y's: " + yPoints);
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawPolygon(xPoints, yPoints, nPoints);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.drawPolygon(xPoints, yPoints, nPoints);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.drawPolygon(xPoints, yPoints, nPoints);
    }

    /** Fills the polygon described by <b>xPoints</b> and <b>yPoints</b>
      * using the current color. Both arrays must have at least
      * <b>nPoints</b> integers.
      */
    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Filling polygon: " +
                      " nPoints: " + nPoints +
                      " X's: " + xPoints +
                      " Y's: " + yPoints);
        }
        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.fillPolygon(xPoints, yPoints, nPoints);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            Color oldColor = state.color;
            int i, count = (info.flashCount * 2) - 1;

            for (i = 0; i < count; i++) {
                super.setColor((i % 2) == 0 ? info.flashColor : oldColor);
                super.fillPolygon(xPoints, yPoints, nPoints);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
            super.setColor(oldColor);
        }
        super.fillPolygon(xPoints, yPoints, nPoints);
    }

    /** Displays <b>bitmap</b> at the point (<b>x</b>, <b>y</b>).
      */
    public void drawBitmapAt(Bitmap bitmap, int x, int y) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing bitmap: " + bitmap +
                      " at: " + new Point(x, y));
        }

        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawBitmapAt(bitmap, x, y);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            int i, count = (info.flashCount * 2) - 1;
            ImageProducer oldProducer
                = AWTCompatibility.awtImageProducerForBitmap(bitmap);
            ImageProducer newProducer
                = new FilteredImageSource(oldProducer,
                                new DebugGraphicsColorFilter(info.flashColor));
            Bitmap newBitmap =
                    AWTCompatibility.bitmapForAWTImageProducer(newProducer);

            for (i = 0; i < count; i++) {
                super.drawBitmapAt((i % 2) == 0 ? newBitmap : bitmap, x, y);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }
        }
        super.drawBitmapAt(bitmap, x, y);
    }

    /** Draws <b>bitmap</b> at the point (<b>x</b>, <b>y</b>), scaled in the
      * X-dimension to have width <b>width</b> and the Y-dimension to
      * have height <b>height</b>.
      */
    public void drawBitmapScaled(Bitmap bitmap, int x, int y,
                                 int width, int height) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugLog()) {
            info().log(toShortString() +
                      " Drawing scaled bitmap: " + bitmap +
                      " in rect: " + new Rect(x, y, width, height));
        }

        if (isDrawingBuffer()) {
            if (state.debugBuffered()) {
                Graphics debugGraphics = debugGraphics();

                debugGraphics.drawBitmapScaled(bitmap, x, y, width, height);
                debugGraphics.dispose();
            }
        } else if (state.debugFlash()) {
            int i, count = (info.flashCount * 2) - 1;
            ImageProducer oldProducer
                = AWTCompatibility.awtImageProducerForBitmap(bitmap);
            ImageProducer newProducer
                = new FilteredImageSource(oldProducer,
                                new DebugGraphicsColorFilter(info.flashColor));
            Bitmap newBitmap =
                    AWTCompatibility.bitmapForAWTImageProducer(newProducer);

            for (i = 0; i < count; i++) {
                super.drawBitmapScaled((i % 2) == 0 ? newBitmap : bitmap,
                                       x, y, width, height);
                AWTCompatibility.awtToolkit().sync();
                sleep(info.flashTime);
            }

        }
        super.drawBitmapScaled(bitmap, x, y, width, height);
    }

    /** Draws <b>aString</b> at the point (<b>x</b>, <b>y</b>) using the
      * current font and color.
      * @see #drawStringInRect
      */
    public void drawString(String aString, int x, int y) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugOptions != 0) {
            if (state.font == null || !state.font.wasDownloaded()) {
                if (state.debugLog()) {
                    info().log(toShortString() +
                                " Drawing string: \"" + aString +
                                "\" at: " + new Point(x, y));
                }

                if (isDrawingBuffer()) {
                    if (state.debugBuffered()) {
                        Graphics debugGraphics = debugGraphics();

                        debugGraphics.drawString(aString, x, y);
                        debugGraphics.dispose();
                    }
                } else if (state.debugFlash()) {
                    Color oldColor = state.color;
                    int i, count = (info.flashCount * 2) - 1;

                    for (i = 0; i < count; i++) {
                        super.setColor((i % 2) == 0 ? info.flashColor
                                                    : oldColor);
                        super.drawString(aString, x, y);
                        AWTCompatibility.awtToolkit().sync();
                        sleep(info.flashTime);
                    }
                    super.setColor(oldColor);
                }
            }
        }
        super.drawString(aString, x, y);
    }

    /** Draws <b>length</b> bytes of <b>data</b>, beginning <b>offset</b> bytes
      * into the array, using the current font and color, at the point
      * (<b>x</b>, <b>y</b>).
      */
    public void drawBytes(byte data[], int offset, int length, int x, int y) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugOptions != 0) {
            Font font = font();

            if (font == null || !font.wasDownloaded()) {
                if (state.debugLog()) {
                    info().log(toShortString() +
                              " Drawing bytes at: " + new Point(x, y));
                }

                if (isDrawingBuffer()) {
                    if (state.debugBuffered()) {
                        Graphics debugGraphics = debugGraphics();

                        debugGraphics.drawBytes(data, offset, length, x, y);
                        debugGraphics.dispose();
                    }
                } else if (state.debugFlash()) {
                    Color oldColor = state.color;
                    int i, count = (info.flashCount * 2) - 1;

                    for (i = 0; i < count; i++) {
                        super.setColor((i % 2) == 0 ? info.flashColor
                                                    : oldColor);
                        super.drawBytes(data, offset, length, x, y);
                        AWTCompatibility.awtToolkit().sync();
                        sleep(info.flashTime);
                    }
                    super.setColor(oldColor);
                }
            }
        }
        super.drawBytes(data, offset, length, x, y);
    }

    /** Draws <b>length</b> characters of <b>data</b>, beginning <b>offset</b>
      * characters into the array, using the current font and color, at the
      * point (<b>x</b>, <b>y</b>).
      */
    public void drawChars(char data[], int offset, int length, int x, int y) {
        DebugGraphicsInfo info = info();
        GraphicsState state = state();

        if (state.debugOptions != 0) {
            Font font = font();

            if (font == null || !font.wasDownloaded()) {
                if (state.debugLog()) {
                    info().log(toShortString() +
                              " Drawing chars at " +  new Point(x, y));
                }

                if (isDrawingBuffer()) {
                    if (state.debugBuffered()) {
                        Graphics debugGraphics = debugGraphics();

                        debugGraphics.drawChars(data, offset, length, x, y);
                        debugGraphics.dispose();
                    }
                } else if (state.debugFlash()) {
                    Color oldColor = state.color;
                    int i, count = (info.flashCount * 2) - 1;

                    for (i = 0; i < count; i++) {
                        super.setColor((i % 2) == 0 ? info.flashColor
                                                    : oldColor);
                        super.drawChars(data, offset, length, x, y);
                        AWTCompatibility.awtToolkit().sync();
                        sleep(info.flashTime);
                    }
                    super.setColor(oldColor);
                }
            }
        }
        super.drawChars(data, offset, length, x, y);
    }

    void copyArea(int x, int y, int width, int height, int destX, int destY) {
        if (state().debugLog()) {
            info().log(toShortString() +
                      " Copying area from: " +
                      new Rect(x, y, width, height) +
                      " to: " + new Point(destX, destY));
        }
        super.copyArea(x, y, width, height, destX, destY);
    }

    final void sleep(int mSecs) {
        try {
            Thread.sleep(mSecs);
        } catch (Exception e) {
        }
    }

    /** Enables/disables diagnostic information about every graphics
      * operation. The value of <b>debug</b> indicates how this information
      * should be displayed. DEBUG_LOG causes a text message to be printed.
      * DEBUG_FLASH causes the drawing to flash several times. DEBUG_BUFFERED
      * creates an ExternalWindow that shows each operation on an
      * offscreen buffer. The value of <b>debug</b> is bitwise OR'd into
      * the current value. To disable debugging use DEBUG_NONE.
      */
    public void setDebugOptions(int debugOptions) {
        if (debugOptions != 0) {
            GraphicsState state = state();

            if (debugOptions == NONE_OPTION) {
                if (state.debugOptions != 0) {
                    System.err.println(toShortString() + " Disabling debug");
                    state.debugOptions = 0;
                }
            } else {
                if (state.debugOptions != debugOptions) {
                   state.debugOptions |= debugOptions;
                   if (state.debugLog()) {
                       System.err.println(toShortString() + " Enabling debug");
                   }
               }
           }
        }
    }

    /** Returns the current debugging options.
      * @see setDebugOptions
      */
    public int debugOptions() {
        return state().debugOptions;
    }

    void setDebug(View view) {
        setDebugOptions(viewDebug(view));
    }

    /** Returns the state of graphics debugging.
      * @see #setDebug
      */
    public int debug() {
        return state().debugOptions;
    }

    private Graphics debugGraphics() {
        Graphics                debugGraphics;
        DebugGraphicsInfo       info = info();
        GraphicsState           state = state();
        Size                    windowSize;
        Rect                    windowBounds;
        ExternalWindow          debugWindow;

        if (info.debugWindow == null) {
            info.debugWindow = new ExternalWindow();
            info.debugWindow.setResizable(false);
        }
        debugWindow = info.debugWindow;
        windowSize = debugWindow.windowSizeForContentSize(
                            primaryClipRect.width, primaryClipRect.height);
        windowBounds = debugWindow.bounds();
        if (windowBounds.width > windowSize.width) {
            windowSize.width = windowBounds.width;
        }
        if (windowBounds.height > windowSize.height) {
            windowSize.height = windowBounds.height;
        }
        debugWindow.sizeTo(windowSize.width, windowSize.height);
        debugWindow.show();
        debugGraphics = new DebugGraphics(debugWindow.rootView());
        debugGraphics.setFont(state.font);
        debugGraphics.setColor(state.color);
        debugGraphics.translate(state.xOffset, state.yOffset);
        debugGraphics.setClipRect(clipRect());
        if (state.debugFlash()) {
            debugGraphics.setDebugOptions(FLASH_OPTION);
        }
        return debugGraphics;
    }

    static DebugGraphicsInfo info() {
        return Graphics.info();
    }
}
