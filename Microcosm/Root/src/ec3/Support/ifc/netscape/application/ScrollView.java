// ScrollView.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import java.lang.Math;
import netscape.util.*;


/** View subclass that manages the display of a larger View. This View,
  * the ScrollView's "contentView," is a subview of the ScrollView. A
  * ScrollView can be told to essentially move its contentView's origin
  * to different locations, resulting in different portions of the contentView
  * becoming visible through the ScrollView's bounds. A scroll command
  * causes the entire visible portion of the contentView to be redrawn. If
  * the ScrollView has been configured to use a drawing buffer, it uses the
  * buffer to hold the visible portion of the contentView. Scroll requests
  * copy as much as possible from the buffer, and ask the contentView to
  * redraw just the newly exposed regions. Whenever a ScrollView moves
  * its contentView, it sends a message to all of its ScrollBars so that
  * they can update their knob size and position. A ScrollView maintains a
  * Vector of ScrollBars (or other objects) wishing to receive these
  * notifications. ScrollView's <b>addScrollBar()</b> and
  * <b>removeScrollBar()</b> methods manage this Vector. ScrollGroups typically
  * instantiate ScrollViews, but need not be. You can create a
  * ScrollView and a ScrollBar, and connect them by calling the ScrollBar's
  * <b>setScrollableObject()</b> method and ScrollView's <b>addScrollBar()</b>
  * method.
  * @see ScrollBar
  * @see ScrollGroup
  * @see Scrollable
  * @note 1.0 draws to dirtyRect
  */


public class ScrollView extends View implements Scrollable {
    View        contentView;
    Color       backgroundColor;
    Rect        clipRect;
    Vector      scrollBars;
    boolean     transparent = false;

    private boolean     scrollBarUpdatesEnabled = true;

    final static String         CONTENTVIEW_KEY = "contentView",
                                BACKGROUNDC_KEY = "backgroundColor",
                                SCROLLERS_KEY = "scrollBars",
                                SCROLLERUPDATES_KEY = "scrollBarUpdatesEnabed",
                                TRANSPARENT_KEY = "transparent";


    /** Constructs a ScrollView with origin (<b>0</b>, <b>0</b>) and zero
      * width and height.
      */
    public ScrollView() {
        this(0, 0, 0, 0);
    }

    /** Constructs a ScrollView with bounds <B>rect</B>.
      */
    public ScrollView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a ScrollView with bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public ScrollView(int x, int y, int width, int height) {
        super(x, y, width, height);

        scrollBars = new Vector();

        backgroundColor = Color.lightGray;

        setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
    }

    /** Overridden to prevent multiple Views from being added to the
      * ScrollView.  Use the <b>setContentView()</b> method to set the View
      * scrolled by the ScrollView.
      * @see #setContentView
      */
    public void addSubview(View aView) {
        subviews().removeAllElements();
        super.addSubview(aView);
    }

    /** Sets the View scrolled by the ScrollView to <b>aView</b>. */
    public void setContentView(View aView) {
        if (contentView != null) {
//            contentView.setWantsAutoscrollEvents(false);
            contentView.removeFromSuperview();
        }

        contentView = aView;
        if (contentView != null) {
            contentView.moveTo(0, 0);
            addSubview(contentView);
//            contentView.setWantsAutoscrollEvents(true);
        }

        updateScrollBars();
    }

    /** Returns the View scrolled by the ScrollView.
      * @see #setContentView
      */
    public View contentView() {
        return contentView;
    }

    /** Overridden to return the ScrollView's contentView's cursor for this
      * point.
      */
    public int cursorForPoint(int x, int y) {
        Point        tmpPoint;
        int        cursor;

        if (contentView == null) {
            return ARROW_CURSOR;
        }

        tmpPoint = Point.newPoint(x, y);
        convertPointToView(contentView, tmpPoint, tmpPoint);

        cursor = contentView.cursorForPoint(tmpPoint.x, tmpPoint.y);

        Point.returnPoint(tmpPoint);

        return cursor;
    }

   /** Returns the ScrollView's background Color. If not transparent, the
     * ScrollView uses the background Color to paint the portions of itself
     * not covered by its contentView.
     */
    public void setBackgroundColor(Color aColor) {
        if (aColor != null) {
            backgroundColor = aColor;
        }
    }

    /** Returns the ScrollView's background Color.
      * @see #setBackgroundColor
      */
    public Color backgroundColor() {
        return backgroundColor;
    }

    /** Sets the ScrollView to be transparent or opaque.
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
    }

    /** Overridden to return <b>true</b> if the ScrollView is transparent.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Adds <b>aScrollBar</b> to the ScrollView's Vector of Targets interested
      * in scroll changes.  Whenever the ScrollView scrolls its contentView,
      * it sends the ScrollBar.UPDATE command to all Targets in this
      * Vector, with itself as the object.  A Target need not be a ScrollBar.
      */
    public void addScrollBar(Target aScrollBar) {
        scrollBars.addElementIfAbsent(aScrollBar);
    }

    /** Removes <b>aScrollBar</b> from its Vector of Targets interested in
      * scroll changes.
      * @see #addScrollBar
      */
    public void removeScrollBar(Target aScrollBar) {
        scrollBars.removeElement(aScrollBar);
    }

   /** Enables (or disables) the ScrollView to (from) send messages to Targets
     * in its ScrollBar Targets Vector when the ScrollView scrolls its
     * contentView.
     * @see #addScrollBar
     */
   public void setScrollBarUpdatesEnabled(boolean flag) {
        scrollBarUpdatesEnabled = flag;
    }

    /** Returns <b>true</b> if ScrollBar updates are enabled.
      * @see #setScrollBarUpdatesEnabled
      */
    public boolean scrollBarUpdatesEnabled() {
        return scrollBarUpdatesEnabled;
    }

   /** If ScrollBar updates are enabled, sends the ScrollBar.UPDATE command
     * to all Targets in the ScrollView's ScrollBar Vector with itself as
     * the object. Upon receiving this command, a ScrollBar should verify
     * that its knob's size and position accurately reflect the ScrollView's
     * state.
     */
    public void updateScrollBars() {
        Target        nextScrollBar;
        int             i, count;

        if (!scrollBarUpdatesEnabled) {
            return;
        }

        count = scrollBars.count();
        for (i = 0; i < count; i++) {
            nextScrollBar = (Target)scrollBars.elementAt(i);
            nextScrollBar.performCommand(ScrollBar.UPDATE, this);
        }
    }

    /** Overridden to return this ScrollView.
      */
    View scrollingView() {
        return this;
    }

    /** Overridden to scroll the contentView such that <b>aRect</b> within the
      * contentView becomes visible. Calls <b>scrollBy()</b>.
      * @see #scrollBy
      */
    public void scrollRectToVisible(Rect contentRect) {
        int     dx = 0, dy = 0;

        if (contentRect == null || contentView == null) {
            return;
        }

//        if (contentRect.x < 0) {
//            dx = -contentRect.x;
//        } else if (contentRect.maxX() > bounds.width
//          && contentRect.width <= bounds.width) {
//            dx = bounds.width - contentRect.maxX();
//        }
//        if (contentRect.y < 0) {
//            dy = -contentRect.y;
//        } else if (contentRect.maxY() >= bounds.height
//          && contentRect.height <= bounds.height) {
//            dy = bounds.height - contentRect.maxY();
//        }
//

        dx = positionAdjustment(bounds.width, contentRect.width, contentRect.x);
        dy = positionAdjustment(bounds.height, contentRect.height, contentRect.y);

        if (dx != 0 || dy != 0) {
            scrollBy(dx, dy);
        }
    }

    /**  This method is used by the scrollToRect method to determine the
      *  proper direction and amount to move by. The ivars here are named
      *  width, but this is applicable to height also. The code assumes that
      *  parentWidth/childWidth are positive and childAt can be negative.
      */
    private int positionAdjustment(int parentWidth, int childWidth, int childAt)    {
//      System.err.println("" + parentWidth + ":" + childWidth + ":" + childAt);

        //   +-----+
        //   | --- |     No Change
        //   +-----+
        if( childAt >= 0 && childWidth + childAt <= parentWidth)    {
            return 0;
        }

        //   +-----+
        //  ---------   No Change
        //   +-----+
        if(childAt <= 0 && childWidth + childAt >= parentWidth) {
            return 0;
        }

        //   +-----+          +-----+
        //   |   ----    ->   | ----|
        //   +-----+          +-----+
        if(childAt > 0 && childWidth <= parentWidth)    {
            return -childAt + parentWidth - childWidth;
        }

        //   +-----+             +-----+
        //   |  --------  ->     |--------
        //   +-----+             +-----+
        if(childAt >= 0 && childWidth >= parentWidth)   {
            return -childAt;
        }

        //   +-----+          +-----+
        // ----    |     ->   |---- |
        //   +-----+          +-----+
        if(childAt <= 0 && childWidth <= parentWidth)   {
            return -childAt;
        }

        //   +-----+             +-----+
        //-------- |      ->   --------|
        //   +-----+             +-----+
        if(childAt < 0 && childWidth >= parentWidth)    {
            return -childAt + parentWidth - childWidth;
        }

        return 0;
    }


    /** Moves the contentView's origin to (<b>x</b>, <b>y</b>).  If
      * the ScrollView has a drawing buffer, scrolling moves as much of the
      * contentView's area
      * as possible and then calls <b>draw()</b> to redraw the newly exposed
      * portions, otherwise just calls the contentView's <b>draw()</b> method
      * to completely redraw the contentView's visible portion.
      */
    public void scrollTo(int x, int y) {
        Rect    tmpRect = null;
        int     dx, dy, myX, myY;
        boolean canCopyBits;

        if (contentView == null) {
            return;
        }

        setClipRect(null);

        /* don't allow the content view to scroll too far */
        if (x > 0 ||
            bounds.width >= contentView.bounds.width) {
            x = 0;
        } else if (x < bounds.width - contentView.bounds.width) {
            x = bounds.width - contentView.bounds.width;
        }

        if (y > 0 || bounds.height >= contentView.bounds.height) {
            y = 0;
        } else if (y < bounds.height - contentView.bounds.height) {
            y = bounds.height - contentView.bounds.height;
        }

        dx = x - contentView.bounds.x;
        dy = y - contentView.bounds.y;

        /* nothing to scroll */
        if (dx == 0 && dy == 0) {
            updateScrollBars();
            return;
        }

        /* if we're scrolling in only one direction, and we're allowed to
         * copy bits, then figure out the newly exposed rectangle which will
         * need to be drawn; someday we might get fancy and maintain two
         * update rects for efficient scrolling in both dimensions
         * simultaneously
         */
        canCopyBits = isBuffered() && drawingBufferValid;
        if (canCopyBits) {
            if (dx != 0 && dy == 0 && Math.abs(dx) < bounds.width) {
                if (dx < 0) {
                    tmpRect = Rect.newRect(bounds.width + dx, 0,
                                           -dx, bounds.height);
                } else {
                    tmpRect = Rect.newRect(0, 0, dx, bounds.height);
                }
            } else if (dx == 0 && dy != 0 &&
                       Math.abs(dy) < bounds.height) {
                if (dy < 0) {
                    tmpRect = Rect.newRect(0, bounds.height + dy,
                                           bounds.width, -dy);
                } else {
                    tmpRect = Rect.newRect(0, 0, bounds.width, dy);
                }
            }

            if (tmpRect != null) {
                contentView.moveTo(x,y);  /** Call contentView.moveTo before changing the cliprect */
                setClipRect(tmpRect);
                Rect.returnRect(tmpRect);
            } else
                contentView.moveTo(x,y);
        } else
            contentView.moveTo(x, y);

        if (scrollBarUpdatesEnabled) {
            updateScrollBars();
        }

        setDirty(true);
    }

    /** Computes the contentView's new coordinates and calls <b>scrollTo()</b>.
      * @see #scrollTo
      */
    public void scrollBy(int deltaX, int deltaY) {
        if (contentView != null) {
            scrollTo(contentView.bounds.x + deltaX,
                     contentView.bounds.y + deltaY);
        }
    }

    /** Overridden to react to contentView resizes. */
    public void subviewDidResize(View aSubview) {
        if (aSubview != contentView) {
            return;
        }

        /* just scroll to the current position - scrollTo() will move
         * the contentView if it has to to keep it from scrolling out of
         * sight, update the scrollBars, and cause things to redraw.
         */
        scrollBy(0, 0);

        /* draw any newly-exposed region */
        drawBackground();
    }

    /** Overridden to ensure that the contentView is correctly positioned. */
    public void didSizeBy(int deltaWidth, int deltaHeight) {
        super.didSizeBy(deltaWidth, deltaHeight);

        /*
         * make sure the contentView isn't scrolled too far in our new size
         * and update the scrollBars.
         */
        scrollBy(0, 0);
    }

    void setClipRect(Rect aRect) {
        if (clipRect != null) {
            Rect.returnRect(clipRect);
        }
        if (aRect != null) {
            clipRect = Rect.newRect(aRect);
        } else {
            clipRect = null;
        }
    }

    void updateDrawingBuffer(Rect updateRect) {
        if (clipRect != null && isBuffered() && !isTransparent()) {
            Graphics bufferedGraphics = drawingBuffer.createGraphics();

            bufferedGraphics.setDebugOptions(shouldDebugGraphics());
            if (clipRect.height != bounds.height) {
                if (clipRect.y == 0) {
                    bufferedGraphics.copyArea(clipRect.x, clipRect.y,
                                              clipRect.width,
                                              bounds.height
                                                    - clipRect.height,
                                              clipRect.x, clipRect.maxY());
                } else {
                    bufferedGraphics.copyArea(0, clipRect.height,
                                              clipRect.width,
                                              bounds.height
                                                    - clipRect.height,
                                              0, 0);
                }
            } else {
                if (clipRect.x == 0) {
                    bufferedGraphics.copyArea(clipRect.x, clipRect.y,
                                              bounds.width
                                                    - clipRect.width,
                                              clipRect.height,
                                              clipRect.maxX(), clipRect.y);
                } else {
                    bufferedGraphics.copyArea(clipRect.width, 0,
                                              bounds.width
                                                    - clipRect.width,
                                              clipRect.height,
                                              0, 0);
                }
            }

            bufferedGraphics.dispose();
            bufferedGraphics = null;

            updateRect = new Rect(updateRect);

            // The clipRect indicates how much we scrolled and the minimum area
            // necessary for a redraw is. Here we are calculating the existing
            // dirtyRects for the contentView and growing the updateRect to include
            // this space. This will set the clip rect large enough to hold all
            // the redrawing that is needs to take place.
            Rect currentDirtyRect = new Rect(0,0,0,0);
            contentView.getDirtyRect(currentDirtyRect);
            if(currentDirtyRect.isEmpty())  {
                updateRect.intersectWith(clipRect);
            } else {
                currentDirtyRect.unionWith(clipRect);
                updateRect.intersectWith(currentDirtyRect);
            }

            setClipRect(null);
        }

        super.updateDrawingBuffer(updateRect);
    }

    /** Overidden to perform additional clipping. */
    public void computeVisibleRect(Rect aRect) {
        super.computeVisibleRect(aRect);

        if (clipRect != null) {
            aRect.intersectWith(clipRect);
        }
    }

    /** Overidden to draw the area not covered by the contentView, unless
      * transparent.
      */
    public void drawView(Graphics g) {
        Rect            graphicsClipRect;
        Vector          windowClipRects;
        int             width, height;

        if (isTransparent()) {
            return;
        } else if (contentView != null && contentView.isTransparent()) {
            g.setColor(backgroundColor);
            graphicsClipRect = g.clipRect();
            g.fillRect(graphicsClipRect.x, graphicsClipRect.y,
                       graphicsClipRect.width, graphicsClipRect.height);
            return;
        }

        if (contentView != null) {
            width = bounds.width - contentView.bounds.width;
            height = bounds.height - contentView.bounds.height;
        } else {
            width = bounds.width;
            height = bounds.height;
        }

        if (width > 0) {
            g.setColor(backgroundColor);
            g.fillRect(bounds.width - width, 0, width, bounds.height);
        }
        if (height > 0) {
            g.setColor(backgroundColor);
            g.fillRect(0, bounds.height - height, bounds.width, height);
        }

        if (clipRect != null && isBuffered()) {
            if (clipRect.y == bounds.y) {
                drawingBuffer.drawAt(g, clipRect.x, clipRect.maxY());
            } else {
                drawingBuffer.drawAt(g, clipRect.x, -clipRect.height);
            }
        }

        /*  ALERT!  Update rect stuff temporarily removed.

        if (g.isBitmap()) {
            setClipRect(null);
        }

        // This appears to be here in case there is a window overlapping with
        // the scroll view.  In that case you can't just do a copyArea().
        // Hmmm...  A copyArea() also won't work if there is any view
        // overlapping the scroll view.  ALERT!

        if (clipRect != null) {
            windowClipRects = application().windowRects(clipRect, window());
            if (windowClipRects != null && !windowClipRects.isEmpty()) {
                clipRect = null;
            }

            Vector.returnVector(windowClipRects);
        }

        // This doesn't handle horizontal scrolling.  ALERT!

        if (clipRect != null) {
            if (clipRect.y == bounds.y) {
                g.copyArea(clipRect.x, clipRect.y, clipRect.width,
                           bounds.height - clipRect.height,
                           clipRect.x, clipRect.maxY());
            } else {
                g.copyArea(bounds.x, bounds.y + clipRect.height,
                           clipRect.width, bounds.height - clipRect.height,
                           bounds.x, bounds.y);
            }
            g.sync();
        }
        */
    }

    /** Overridden to perform post-drawing processing. */
    public void drawSubviews(Graphics g) {
        super.drawSubviews(g);
        setClipRect(null);
    }

    void drawBackground() {
        Rect    tmpRect;
        int     width, height;

        if (contentView != null) {
            width = bounds.width - contentView.bounds.width;
            height = bounds.height - contentView.bounds.height;
        } else {
            width = bounds.width;
            height = bounds.height;
        }

        tmpRect = Rect.newRect();

        if (width > 0) {
            tmpRect.setBounds(bounds.width - width, 0, width, bounds.height);
            addDirtyRect(tmpRect);
        }
        if (height > 0) {
            tmpRect.setBounds(0, bounds.height - height, bounds.width, height);
            addDirtyRect(tmpRect);
        }

        Rect.returnRect(tmpRect);
    }

    /** Overridden to pass any mouse down Event received by the ScrollView on
      * to its contentView.
      */
    public boolean mouseDown(MouseEvent event) {
        boolean                wantsEvents;

        if (contentView != null) {
            wantsEvents = contentView.mouseDown(
                                convertEventToView(contentView, event));

            if (wantsEvents) {
                rootView().setMouseView(contentView);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /** Overridden to forward the <b>acceptsDrag()</b> message to its
      * contentView.
      */
    public DragDestination acceptsDrag(DragSession session, int x, int y) {
        if (contentView != null) {
            return contentView.acceptsDrag(session,
                                       x - contentView.bounds.x,
                       y - contentView.bounds.y);
        } else {
            return null;
        }
    }


/* Scrollable interface */

    /** Scrollable interface method returning the appropriate bounds value.
      * @see Scrollable
      */
    public int lengthOfScrollViewForAxis(int axis)    {
        if(axis == Scrollable.HORIZONTAL)
            return bounds.width;
        return bounds.height;
    }

    /** Scrollable interface method returning the appropriate contentView
      * bounds value.
      * @see Scrollable
      */
    public int lengthOfContentViewForAxis(int axis)   {
        if(contentView == null)
            return 0;
        if(axis == Scrollable.HORIZONTAL)
            return contentView.bounds.width;
        return contentView.bounds.height;
    }

    /** Scrollable interface method returning the appropriate contentView
      * bounds value.
      * @see Scrollable
      */
    public int positionOfContentViewForAxis(int axis) {
        if(contentView == null)
            return 0;
        if(axis == Scrollable.HORIZONTAL)
            return contentView.bounds.x;
        return contentView.bounds.y;
    }


/* archving */


    /** Describes the ScrollView class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.ScrollView", 1);
        info.addField(CONTENTVIEW_KEY, OBJECT_TYPE);
        info.addField(BACKGROUNDC_KEY, OBJECT_TYPE);
        info.addField(SCROLLERS_KEY, OBJECT_TYPE);
        info.addField(SCROLLERUPDATES_KEY, BOOLEAN_TYPE);
        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the ScrollView instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(CONTENTVIEW_KEY, (Codable)contentView);
        encoder.encodeObject(BACKGROUNDC_KEY, backgroundColor);
        encoder.encodeObject(SCROLLERS_KEY, scrollBars);
        encoder.encodeBoolean(SCROLLERUPDATES_KEY, scrollBarUpdatesEnabled);
        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);
    }

    /** Decodes the ScrollView instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        contentView = (View)decoder.decodeObject(CONTENTVIEW_KEY);
        backgroundColor = (Color)decoder.decodeObject(BACKGROUNDC_KEY);
        scrollBars = (Vector)decoder.decodeObject(SCROLLERS_KEY);

        scrollBarUpdatesEnabled = decoder.decodeBoolean(SCROLLERUPDATES_KEY);
        transparent = decoder.decodeBoolean(TRANSPARENT_KEY);
    }
}
