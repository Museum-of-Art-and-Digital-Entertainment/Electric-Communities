// WindowContentView.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** View subclass used by an InternalWindow to hold its contents.<p>
  * @note 1.0 added accessor for color attribute
  */
public class WindowContentView extends View {
    Color       _color;
    boolean     transparent = false;

    static final String         COLOR_KEY = "color";
    static final String         TRANSPARENT_KEY = "transparent";


    /* constructors */

    /** Constructs a WindowContentView with origin (0, 0) and zero width and
      * height.
      */
    public WindowContentView() {
        this(0, 0, 0, 0);
    }

    /** Convenience constructor for instantiating a WindowContentView with
      * bounds <B>rect</B>.
      */
    public WindowContentView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Convenience constructor for instantiating a WindowContentView with
      * bounds (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public WindowContentView(int x, int y, int width, int height) {
        super(x, y, width, height);

        _color = Color.lightGray;
    }

    /** Sets the background color displayed by the WindowContentView.  By
      * default, this color is Color.lightGray.
      * This is IFC 1.0 API that has been replaced by setBackgroundColor()
      * @see #setBackgroundColor
      */
    public void setColor(Color aColor) {
        setBackgroundColor(aColor);
    }

    /** Sets the background color displayed by the WindowContentView.  By
      * default, this color is Color.lightGray.
      *
      */
    public void setBackgroundColor(Color aColor) {
        _color = aColor;
    }

    /** Returns the background color displayed by the WindowContentView.
      *
      */
    public Color backgroundColor() {
        return _color;
    }

    /** Sets the WindowContentView to be transparent or opaque.
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
    }

    /** Overridden to return <b>true</b> if the WindowContentView is
      * transparent.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Fills the WindowContentView with its color, unless it or its
      * InternalWindow is transparent.
      */
    public void drawView(Graphics g) {
        InternalWindow  theWindow;
        View            nextView;
        Rect            nextRect, myBounds;
        Vector          tmpVector;
        int             i, j;

        theWindow = window();
        if (_color == null || isTransparent() || theWindow.isTransparent()) {
            return;
        }

        g.setColor(_color);

        i = subviewCount();

        /* DMM - ALERT! - This isn't really what you want.  It's trying to
            avoid filling the rectangle for a non-transparent window that
            is obscured by subviews.  But it doesn't check for the
            opacity of the subviews.  It's cheaper anyway to just fill
            the whole window, than several smaller parts.  So I'm forcing it
            to always draw the background color.  The transparent case is
            caught above
        if (i == 0) {
            g.fillRect(0, 0, width(), height());
        } else {
            myBounds = Rect.newRect(0, 0, width(), height());

            while (i-- > 0) {
                nextView = (View)_subviews.elementAt(i);
                tmpVector = VectorCache.newVector();
                myBounds.computeDisunionRects(nextView.bounds, tmpVector);
                if (tmpVector != null) {
                    j = tmpVector.count();
                    while (j-- > 0) {
                        nextRect = (Rect)tmpVector.elementAt(j);
                        g.fillRect(nextRect);
                    }
                }

                Vector.returnVector(tmpVector);
            }

            Rect.returnRect(myBounds);
        }
        */

        g.fillRect(0, 0, width(), height());
    }

/* archiving */

    /** Describes the WindowContentView class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.WindowContentView", 1);
        info.addField(COLOR_KEY, OBJECT_TYPE);
        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the WindowContentView.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(COLOR_KEY, _color);
        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);
    }

    /** Decodes the WindowContentView.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        _color = (Color)decoder.decodeObject(COLOR_KEY);
        transparent = decoder.decodeBoolean(TRANSPARENT_KEY);
    }
}
