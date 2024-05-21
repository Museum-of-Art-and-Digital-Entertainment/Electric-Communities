// Image.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Abstract Object subclass representing objects that have an intrinsic
  * width and height and can draw themselves to a Graphics object. Bitmap and
  * DrawingSequence are two concrete implementations of the abstract Image
  * class. Many user interface elements take Images to customize their
  * appearance. By implementing the three primitive Image methods, you can
  * create custom Images and use them with any component that accepts Images.
  * Image also provides several convenience methods for drawing itself
  * centered, scaled and tiled.
  */


public abstract class Image implements Codable {
    /** Display option that centers the Image within a rectangle. */
    public static final int CENTERED = 0;

    /** Display option that scales the Image within a rectangle. */
    public static final int SCALED = 1;

    /** Display option that tiles the Image within a rectangle. */
    public static final int TILED = 2;

    /** Drag type.*/
    public static final String IMAGE_TYPE = "netscape.application.Image";


    /* static methods */


    /** Returns the Image instance with name <b>name</b>.  This method
      * assumes that <b>name</b> consists of an Image subclass name,
      * followed by the Image's path, separated by a forward or backward
      * slash.  For example, the name:
      * <pre>
      *     ImageSequence/myApp/Tire.anim
      * </pre>
      * describes an instance of class "ImageSequence" with the name
      * "myApp/Tire.anim."  <b>imageNamed</b> locates the specified class,
      * creates an instance of it, and calls its <b>imageWithName()</b> method,
      * passing in the Image name (in this case, "myApp/Tire.anim").  Image
      * subclasses should override the <b>imageWithName()</b> method to locate
      * and return the requested Image by name.<p>
      * <i><b>Note:</b> <b>imageNamed()</b> only calls Bitmap's
      * <b>imageWithName()</b> method if <b>name</b> begins with "Bitmap."
      * That is, it doesn't attempt to call the static <b>bitmapNamed()</b>
      * method on Bitmap.  If you know that you're requesting a Bitmap, you
      * should call <b>Bitmap.bitmapNamed()</b> explicitly.</i>
      * @see #imageWithName
      * @see Bitmap#bitmapNamed
      * @private
      */
    public static Image imageNamed(String name) {
        Class   theClass;
        Image   theImage;
        String  typeName, className = "";
        int     slash;

        slash = name.indexOf('/');
        if (slash == -1) {
            slash = name.indexOf('\\');
        }

        if (slash == -1) {
            return null;
        }

        typeName = name.substring(0, slash);

        try {
            className = "netscape.application." + typeName;
            theClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            theClass = null;
        }

        if (theClass == null) {
            try {
                className = typeName;
                theClass = Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                theClass = null;
            }
        }

        if (theClass == null) {
            return null;
        }

        try {
            theImage = (Image)theClass.newInstance();
        } catch (InstantiationException e) {
            throw new InconsistencyException("Unable to instantiate class \"" +
                                         className + "\" -- " +
                                         e.getMessage());
        } catch (IllegalAccessException e) {
            throw new InconsistencyException("Illegal access to class \"" +
                                         className + "\" -- " +
                                         e.getMessage());
        }

        return theImage.imageWithName(name.substring(slash + 1));
    }

    /** Returns the Image's width.  Subclassers must implement this method
      * to return the subclass instance's width.
      */
    public abstract int width();

    /** Returns the Image's height.  Subclassers must implement this method
      * to return the subclass instance's height.
      */
    public abstract int height();

    /** Draws the Image at the given location.  Subclassers must implement
      * this method to draw the subclass instance using the Graphics <b>g</b>.
      */
    public abstract void drawAt(Graphics g, int x, int y);

    /** Draws the Image scaled to fit the supplied rectangle.  This method
      * will not correctly scale most Image subclasses.  In cases where this
      * method produces incorrect scaling, subclassers should override and
      * implement their own scaling algorithm.
      */
    public void drawScaled(Graphics g, int x, int y, int width, int height) {
        drawCentered(g, x, y, width, height);
    }

    /** Returns the Image's name.  Default implementation returns <b>null</b>.
      * Subclassers should override to return the Image's name.
      */
    public String name() {
        return null;
    }

    /** Draws the Image centered in the supplied rectangle.  Computes a
      * location and calls <b>drawAt()</b>.
      */
    public void drawCentered(Graphics g, int x, int y, int width, int height) {
        drawAt(g, x + (width - width()) / 2, y + (height - height()) / 2);
    }

    /** Draws the Image centered in the Rect <b>rect</b>.  Calls the primitive
      * <b>drawCentered()</b>.
      * @see #drawCentered(Graphics, int, int, int, int)
      */
    public void drawCentered(Graphics g, Rect rect) {
        if (rect == null)
            return;

        drawCentered(g, rect.x, rect.y, rect.width, rect.height);
    }

    /** Draws the Image scaled in the Rect <b>rect</b>.  Calls the primitive
      * <b>drawScaled()</b>.
      * @see #drawScaled(Graphics, int, int, int, int)
      */
    public void drawScaled(Graphics g, Rect rect) {
        if (rect == null)
            return;

        drawScaled(g, rect.x, rect.y, rect.width, rect.height);
    }

    /** Draws the Image tiled in the supplied rectangle.  Calls
      * <b>drawAt()</b> for each tile.
      */
    public void drawTiled(Graphics g, int x, int y, int width, int height) {
        Rect clipRect;
        int imageWidth, imageHeight, minX, minY, maxX, maxY;

        clipRect = g.clipRect();
        imageWidth = width();
        imageHeight = height();

        if (imageWidth <= 0 || imageHeight <= 0)
            return;

        g.pushState();
        g.setClipRect(new Rect(x, y, width, height));

        if (x > clipRect.x)
            minX = x;
        else
            minX = x + imageWidth * ((clipRect.x - x) / imageWidth);

        if ((x + width) < clipRect.maxX())
            maxX = x + width;
        else
            maxX = clipRect.maxX();

        if (y > clipRect.y)
            minY = y;
        else
            minY= y + imageHeight * ((clipRect.y - y) / imageHeight);

        if ((y + height) < clipRect.maxY())
            maxY = (y + height);
        else
            maxY = clipRect.maxY();

        for (x = minX; x < maxX; x += imageWidth) {
            for (y = minY; y < maxY; y += imageHeight) {
                drawAt(g, x, y);
            }
        }

        g.popState();
    }

    /** Draws the Image tiled in the Rect <b>rect</b>.  Calls the primitive
      * <b>drawTiled()</b>.
      * @see #drawTiled(Graphics, int, int, int, int)
      */
    public void drawTiled(Graphics g, Rect rect) {
        if (rect == null)
            return;

        drawTiled(g, rect.x, rect.y, rect.width, rect.height);
    }

    /** Calls <b>drawCentered()</b>, <b>drawScaled()</b>, or
      * <b>drawTiled()</b> based on the <b>style</b> parameter.  <b>style</b>
      * can be one of CENTERED, SCALED or TILED.
      */
    public void drawWithStyle(Graphics g, int x, int y, int width, int height,
                              int style) {

        switch (style) {
            case CENTERED:
                drawCentered(g, x, y, width, height);
                break;
            case SCALED:
                drawScaled(g, x, y, width, height);
                break;
            case TILED:
                drawTiled(g, x, y, width, height);
                break;
            default:
                throw new InconsistencyException("Unknown style: " + style);
        }
    }

    /** Convenience method for calling <b>drawWithStyle()</b> with Rect
      * <b>rect</b>. Equivalent to the code:
      * <pre>
      *     drawWithStyle(g, rect.x, rect.y, rect.width, rect.height, style).
      * </pre>
      * @see #drawWithStyle(Graphics, int, int, int, int, int)
      */
    public void drawWithStyle(Graphics g, Rect rect, int style) {
        drawWithStyle(g, rect.x, rect.y, rect.width, rect.height, style);
    }

    /** Called by the static <b>imageNamed()</b> method to retrieve the Image
      * with name <b>name</b>.  This method returns <b>null</b>.  Subclassers
      * should override this method and implement it such that it locates
      * and returns the requested Image.
      * @private
      * @see Image#imageNamed
      */
    public Image imageWithName(String name) {
        return null;
    }

    /** Returns <B>true</B> if the Image is transparent. Images are assumed
      * to be transparent unless the subclass overrides this method. Returning
      * <b>true</b> is always safe, but an Image user may
      * be able to avoid drawing the region under the Image if this method
      * returns <b>false</b>.
      */
    public boolean isTransparent() {
        return true;
    }

    // The coding methods are here just for convenience.  Stateless Image
    // subclasses won't need to implement these stubs.

    /** Defined so that Images are Codable.  Stateless Images need not
      * implement.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
    }

    /** Defined so that Images are Codable.  Stateless Images need not
      * implement.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
    }

    /** Defined so that Images are Codable.  Stateless Images need not
      * implement.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
    }

    /** Defined so that Images are Codable.  Stateless Images need not
      * implement.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
