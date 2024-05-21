// View.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;


/** A View is a rectangular entity capable of drawing on the screen and
  * receiving Events. All objects that need to perform one or both of these
  * functions (Buttons, Sliders, Windows) are View subclasses. Views are
  * arranged in a tree-like hierarchy, with each View having zero or more
  * subviews, called descendants. Before a View can draw or receive Events,
  * it must be placed within this hierarchy, through some form of the
  * <b>addSubview()</b> method. A RootView instance sits at the top
  * of the hierarchy, and all other Views descend from it.<p>
  * Each View has an origin and size, defined by the View's <B>bounds</B>
  * instance variable. A View's origin is defined in its superview's
  * coordinates. A RootView has an origin of (0, 0) and a size as
  * defined by the HTML document that invoked the Application, or
  * ExternalWindow containing the RootView. The
  * coordinate system's positive Y-axis points down. For example, a subview
  * of the RootView with its origin 50 pixels below and 100 pixels
  * to the right of the RootView's origin has an origin of (100, 50).<p>
  * A View subclass that wants to draw must override the <b>drawView()</b>
  * method. The Graphics object passed into this method has its clip rect set
  * such that the View cannot accidentally draw outside of its bounds. The
  * Graphics object's coordinate system has been altered, allowing the View to
  * draw using its own relative coordinate system.  Using the subview in the
  * above example, the following code draws a red square at the View's origin:
  * <PRE>
  *     g.setColor(Color.red);
  *     g.fillRect(0, 0, 20, 20);
  * </PRE> <p>
  * While Views encompass rectangular regions within the View hierarchy, they
  * can simulate non-rectangular entities through the notion of transparency.
  * Overriding the method <b>isTransparent()</b> to return <b>true</b> tells
  * the IFC's drawing machinery that at
  * least some portion of the View's drawing area is not redrawn by its
  * <b>drawView()</b> method. Whenever the View needs to be redrawn, the
  * drawing machinery assures that the transparent View's superview, or
  * some other opaque ancestor, redraws the region behind the transparent
  * View before calling the transparent View's <b>drawView()</b> method.
  * In general, you do not need to know the exact mechanism - just
  * have your View's <b>isTransparent()</b> method return <b>true</b> and
  * everything else happens as it
  * should. By default, <b>isTransparent()</b> returns <b>true</b>, so if
  * your View is not transparent, you should override this method to return
  * <b>false</b>.<p>
  * You can also make a View &quot;buffered,&quot; meaning that
  * all drawing performed by the View goes first to an offscreen buffer
  * and then onscreen. With a buffered View, you get flicker-free drawing,
  * but at the cost of slightly reduced performance.<p>
  * To force a View to draw itself,
  * you call the View's <b>draw()</b> method. All drawing is synchronous,
  * meaning that a <b>draw()</b> request is peformed
  * immediately (<b>draw()</b> does not return until all drawing has
  * completed).<p>
  * Views interested in processing mouse events must override one of the mouse
  * event methods and take appropriate action.  Event locations are presented
  * in terms of the View's coordinate system.<p>
  * @see #addSubview
  * @see #removeFromSuperview
  * @see #draw
  * @see #drawView
  * @see #isTransparent
  * @see #setBuffered
  * @note 1.0 added support for asian input managers.
  * @note 1.0 It is now possible to call setFocusedView() on a view that's not
  *           connected into the view hierarchy. When the view or it's parent
  *           get connected, it gets the focus. If more than one view
  *           request the focus, the last view added to view hierarchy get
  *           the focus.
  * @note 1.0 Added keyboard UI api.
  * @note 1.0 added CENTER as primative layout type
  * @note 1.0 archiving changed
  */

public class View implements Codable {
    View                _superview;
    Size                _minSize;
    Bitmap              drawingBuffer;
    private Vector      subviews;
    private Vector      kbdOrder;
    LayoutManager       layoutManager;
    Hashtable           _keyboardBindings;
    /** The View's bounding rectangle, in its superview's coordinate
      * system. The <b>bounds</b> field should only be used for reading.
      * You should never modify this field directly. To move or resize a
      * View call <b>setBounds()</b>.
      * @see #setBounds
      * @see #bounds()
      * @see #localBounds
      */
    public Rect         bounds = new Rect();

    Rect                dirtyRect;
    byte                resizeInstr = (byte)DEFAULT_RESIZE_INSTR;
    int                 drawingDisabled = 0;
    boolean             autoResizeSubviews = true,
                        buffered;
    boolean             drawingBufferValid;
    boolean             drawingBufferIsBitCache;
    boolean             isDirty;
    boolean             needFocus;
    boolean             focusPaused;
    boolean             wantsKeyboardArrow;

    /** Horizontal resize instruction.    */
    public final static int     RIGHT_MARGIN_CAN_CHANGE = 0;
    /** Horizontal resize instruction.    */
    public final static int     LEFT_MARGIN_CAN_CHANGE = 1;
    /** Horizontal resize instruction.    */
    public final static int     WIDTH_CAN_CHANGE = 2;
    /** Horizontal resize instruction.
      *
      */
    public final static int     CENTER_HORIZ = 32;

    /** Vertical resize instruction. */
    public final static int     BOTTOM_MARGIN_CAN_CHANGE = 4;
    /** Vertical resize instruction. */
    public final static int     TOP_MARGIN_CAN_CHANGE = 8;
    /** Vertical resize instruction. */
    public final static int     HEIGHT_CAN_CHANGE = 16;
    /** Vertical resize instruction.
      *
      */
    public final static int     CENTER_VERT = 64;

    private final static int    DEFAULT_RESIZE_INSTR =
                                        RIGHT_MARGIN_CAN_CHANGE |
                                        BOTTOM_MARGIN_CAN_CHANGE;
    private final static int    VERT_MASK = BOTTOM_MARGIN_CAN_CHANGE |
                                            TOP_MARGIN_CAN_CHANGE |
                                            HEIGHT_CAN_CHANGE |
                                            CENTER_VERT;
    private final static int    HORZ_MASK = RIGHT_MARGIN_CAN_CHANGE |
                                            LEFT_MARGIN_CAN_CHANGE |
                                            WIDTH_CAN_CHANGE |
                                            CENTER_HORIZ;

    final static String         MINSIZE_KEY = "minSize",
                                BOUNDS_KEY = "bounds",
                                SUBVIEWS_KEY = "subviews",
                                RESIZE_KEY = "resizeInstr",
                                DRAWINGDISABLED_KEY = "drawingDisabled",
                                AUTORESIZE_KEY = "autoResizeSubviews",
                                BUFFERED_KEY = "buffered",
                                LAYOUTMANAGER_KEY = "layoutManager",
                                KEYBOARD_BINDINGS_KEY = "keyboardBindings";

    private final static String  KBD_COMMAND_KEY = "kbdCmd";
    private final static String  KBD_WHEN        = "when";
    private final static String  KBD_DATA_KEY = "kbdData";

    static final int  DEFAULT_CURSOR = -1;
    /** Arrow cursor. */
    public static final int  ARROW_CURSOR = java.awt.Frame.DEFAULT_CURSOR;
    /** Crosshair cursor. */
    public static final int  CROSSHAIR_CURSOR =
                                            java.awt.Frame.CROSSHAIR_CURSOR;
    /** Text cursor. */
    public static final int  TEXT_CURSOR = java.awt.Frame.TEXT_CURSOR;
    /** Wait cursor. */
    public static final int  WAIT_CURSOR = java.awt.Frame.WAIT_CURSOR;
    /** Southwest resize cursor. */
    public static final int  SW_RESIZE_CURSOR =
                                            java.awt.Frame.SW_RESIZE_CURSOR;
    /** Southeast resize cursor. */
    public static final int  SE_RESIZE_CURSOR =
                                            java.awt.Frame.SE_RESIZE_CURSOR;
    /** Northwest resize cursor. */
    public static final int  NW_RESIZE_CURSOR =
                                            java.awt.Frame.NW_RESIZE_CURSOR;
    /** Northeast resize cursor. */
    public static final int  NE_RESIZE_CURSOR =
                                            java.awt.Frame.NE_RESIZE_CURSOR;
    /** North resize cursor. */
    public static final int  N_RESIZE_CURSOR = java.awt.Frame.N_RESIZE_CURSOR;
    /** South resize cursor. */
    public static final int  S_RESIZE_CURSOR = java.awt.Frame.S_RESIZE_CURSOR;
    /** West resize cursor. */
    public static final int  W_RESIZE_CURSOR = java.awt.Frame.W_RESIZE_CURSOR;
    /** East resize cursor. */
    public static final int  E_RESIZE_CURSOR = java.awt.Frame.E_RESIZE_CURSOR;
    /** Hand cursor. */
    public static final int  HAND_CURSOR = java.awt.Frame.HAND_CURSOR;
    /** Move cursor. */
    public static final int  MOVE_CURSOR = java.awt.Frame.MOVE_CURSOR;


    /** Flags to descrive when to receive keyboard UI commands **/
    /** The view should receive the command only the selected
      *
      */
    public static final int WHEN_SELECTED = 0;

    /** The view should receive the command only when it is in the main window
      * The command is also sent when the view is selected.
      *
      **/
    public static final int WHEN_IN_MAIN_WINDOW = 1;

    /** Always send the command. You should be careful with this option since
      * the command will always be sent, even when a modal session is running.
      * The command is also sent when the component is in the main window or is
      * selected.
      *
      */
    public static final int ALWAYS = 2;


    /* constructors */

    /** Constructs a View with origin (<b>0</b>, <b>0</b>) and zero width
      * and height.
      */
    public View() {
        this(0, 0, 0, 0);
    }

    /** Constructs a View with bounds <B>rect</B>.
      */
    public View(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a View with bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public View(int x, int y, int width, int height) {
        super();
        _setBounds(x, y, width, height);

        // init() ALERT!  It would be great if this were the case...
        // setBounds(x, y, width, height);
    }

    void _setBounds(int x, int y, int width, int height) {
        bounds.setBounds(x, y, width, height);
    }

    /* attributes */

    /** This is a hook we use to avoid making the subviews Vector until
      * needed. There is very little code in IFC which blindly walks down
      * the View hierarchy. Where such code occurs it should call this
      * method to get the subviewCount. Calling subviews().count() will
      * create a Vector even if there are no subviews.
      */
    int subviewCount() {
        if (subviews == null)
            return 0;

        return subviews.count();
    }

    /** Returns the View's subviews. Do not modify the Vector's contents.
      */
    public Vector subviews() {
        if (subviews == null)
            subviews = new Vector();

        return subviews;
    }

    /** Returns <b>subview</b>'s "peers," the Views in the same level in the
      * View hierarchy.  "RadioButtons" call this method to locate all
      * RadioButtons with the same superview.  This method returns the View's
      * subviews, if <b>subview</b> is a subview of the View, an empty Vector
      * otherwise.  Override this method to return a different list of peers.
      * @private
      */
    public Vector peersForSubview(View subview) {
        Vector          newVector;

        if (subviewCount() == 0 || !subviews().contains(subview)) {
            return new Vector();
        }

        newVector = new Vector();
        newVector.addElementsIfAbsent(subviews);
        return newVector;
    }

    /** Returns <B>true</B> if the View is a descendant of or equals
      * <B>aView</B>.
      */
    public boolean descendsFrom(View aView) {
        if(aView == this)
            return true;
        if (_superview == null || aView == null) {
            return false;
        } else if (_superview == aView) {
            return true;
        }

        return _superview.descendsFrom(aView);
    }

    /** Returns the View's InternalWindow, or <B>null</B> if a subview of
      * the RootView.
      */
    public InternalWindow window() {
        if (_superview == null) {
            return null;
        } else {
            return _superview.window();
        }
    }

   /** Returns a newly-allocated copy of the View's bounding rectangle, which
     * defines the View's size and position within its superview's coordinate
     * system.
     * @see #setBounds
     * @see #moveBy
     * @see #sizeBy
     * @see #moveTo
     * @see #sizeTo
     * @see #localBounds
     */
    public Rect bounds() {
        return new Rect(bounds);
    }

    /** Returns the View's x location.
      * @see #bounds()
      */
    public int x() {
        return bounds.x;
    }

    /** Returns the View's y location.
      * @see #bounds()
      */
    public int y() {
        return bounds.y;
    }

    /** Returns the View's width.
      * @see #bounds()
      */
    public int width() {
        return bounds.width;
    }

    /** Returns the View's height.
      * @see #bounds()
      */
    public int height() {
        return bounds.height;
    }

    /** Returns the View's superview.
      * @see #addSubview
      * @see #removeFromSuperview
      */
    public View superview() {
        return _superview;
    }

   /** Sets the View's horizontal resize instruction, an integer value that
     * represents the various ways in which a View can change its size in
     * response to its superview's resizing. The default horizontal
     * resize instruction is <B>RIGHT_MARGIN_CAN_CHANGE</B>, which keeps
     * the View's left margin and width a fixed number of pixels.
     */
    public void setHorizResizeInstruction(int instruction) {
        if (instruction != RIGHT_MARGIN_CAN_CHANGE
            && instruction !=  LEFT_MARGIN_CAN_CHANGE
            && instruction != WIDTH_CAN_CHANGE
            && instruction != CENTER_HORIZ)
            throw new IllegalArgumentException(
                        "invalid horz resize instruction " + instruction);
        resizeInstr &= VERT_MASK;       // Clear the HORZ bits by and'ed them
                                        // with zeros
        resizeInstr |= instruction;     // Set the bits properly
    }

    /** Returns the View's horizontal resize instruction.
      * @see #setHorizResizeInstruction
      */
    public int horizResizeInstruction() {
         return resizeInstr & HORZ_MASK;
    }

    /** Sets the View's vertical resize instruction, an integer value
      * respresenting the various ways in which a View can change its size
      * in response to its superview's resizing. The default vertical
      * resize instruction is <B>BOTTOM_MARGIN_CAN_CHANGE</B>, which keeps
      * the View's top margin and height a fixed number of pixels.
      */
    public void setVertResizeInstruction(int instruction) {
        if (instruction != BOTTOM_MARGIN_CAN_CHANGE
            && instruction !=  TOP_MARGIN_CAN_CHANGE
            && instruction !=  HEIGHT_CAN_CHANGE
            && instruction != CENTER_VERT)
            throw new IllegalArgumentException(
                            "invalid vert resize instruction " + instruction);
        resizeInstr &= HORZ_MASK;       // Clear the VERT bits by and'ed them
                                        // with zeros
        resizeInstr |= instruction;     // Set the bits properly
    }

    /** Returns the View's vertical resize instruction.
      * @see #setVertResizeInstruction
      */
    public int vertResizeInstruction() {
        return resizeInstr & VERT_MASK;
    }

    /** Returns <B>true</B> if the View wants to automatically receive
      * mouse dragged events when the user clicks and drags outside of its
      * bounds.  Default implementation returns <b>false</b>.  Views
      * that want to allow autoscrolling should override to return <b>true</b>.
      */
    public boolean wantsAutoscrollEvents() {
        return false;
    }

    /** Returns the object which should act as the destination of a
      * DragSession.  Returns <b>null</b>.
      * @see DragSession
      * @see DragDestination
      */
    public DragDestination acceptsDrag(DragSession session, int x, int y) {
        return null;
    }

    /** Tells the IFC that this View should automatically resize
      * and reposition its subviews when resized.
      */
    public void setAutoResizeSubviews(boolean flag) {
        autoResizeSubviews = flag;
    }

    /** Returns <b>true</b> if the View automatically resizes and repositions
      * its subviews when it is resized.
      * @see #setAutoResizeSubviews
      */
    public boolean doesAutoResizeSubviews() {
        return autoResizeSubviews;
    }

    /* size/bounds */

    /** Called by View's implementation of <b>setBounds()</b>. Subviews
      * override this method to learn when they change position.
      */
    public void didMoveBy(int deltaX, int deltaY) {
    }

    /** Called by <b>setBounds()</b> to resize a View's subviews. View
      * subclasses requiring specialized resizing behavior should override
      * this method.
      */
    public void didSizeBy(int deltaWidth, int deltaHeight) {
        if (!autoResizeSubviews)
            return;
        layoutView(deltaWidth, deltaHeight);
    }

    /** Convenience method for setting the bounds with a Rect. Equivalent
      * to the code:
      * <pre>
      *     setBounds(rect.x, rect.y, rect.width, rect.height);
      * </pre>
      * @see #setBounds(int, int, int, int)
      */
    public void setBounds(Rect rect) {
        setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    /** Primitive method for changing a View's bounds Rect. Sets the
      * View's bounding rectangle and then calls <b>didMoveBy()</b>
      * with the old origin, and <b>didSizeBy()</b> with the old size. It also
      * adjusts the size of its drawingBuffer, if any, and notifies its
      * superview if this View's size has changed.
      * @see #bounds()
      * @see #localBounds()
      */
    public void setBounds(int x, int y, int width, int height) {
        // ALERT!...We shouldn't let the bounds change while we are drawing to
        // the view...this might be hard because we don't have a focus stack.
        // ALERT!...We should handle properly a negative width or height.
        Rect    tmpRect;
        int     dx, dy, dw, dh;
        boolean didMove, didResize;

        dx = x - bounds.x;
        dy = y - bounds.y;
        dw = width - bounds.width;
        dh = height - bounds.height;

        didMove = (dx != 0 || dy != 0);
        didResize = (dw != 0 || dh != 0);

        if (!didMove && !didResize)
            return;

        _setBounds(x, y, width, height);

        if (buffered && didResize) {
            if (drawingBuffer != null) {
                drawingBuffer.flush();
            }

            if (width > 0 && height > 0) {
                drawingBuffer = createBuffer();
            } else {
                drawingBuffer = null;
            }
            drawingBufferValid = false;
        }

        if (didMove) {
            if (_superview != null) {
                _superview.subviewDidMove(this);
            }
            didMoveBy(dx, dy);
        }

        if (didResize) {
            if (_superview != null) {
                _superview.subviewDidResize(this);
            }
            didSizeBy(dw, dh);
        }
    }

    /** Convenience method to translate the View's origin by <B>deltaX</B>
      * and <B>deltaY</B>. Calls <b>setBounds()</b>.
      */
    public void moveBy(int deltaX, int deltaY) {
        setBounds(bounds.x + deltaX, bounds.y + deltaY,
            bounds.width, bounds.height);
    }

    /** Convenience method to translate the View's origin. Calls
      * <b>setBounds()</b>.
      */
    public void moveTo(int x, int y) {
        setBounds(x, y, bounds.width, bounds.height);
    }

    /** Convenience method for changing the View's size.  Calls
      * <b>setBounds()</b>.
      */
    public void sizeBy(int deltaWidth, int deltaHeight) {
        setBounds(bounds.x, bounds.y, bounds.width +
            deltaWidth, bounds.height + deltaHeight);
    }

    /** Convenience method for setting the View's size.  Call
      * <b>setBounds()</b>.
      */
    public void sizeTo(int width, int height) {
        setBounds(bounds.x, bounds.y, width, height);
    }

    /** Convenience method for setting the View's minimum size to
      * (<B>width</B>, <B>height</B>).  This is the size that will be
      * returned from the <b>minSize()</b> method.  Normally, <b>minSize()</b>
      * computes a View subclass' minimum based on current conditions.  Setting
      * a minimum size of (-1, -1) erases the previous minimum size set.
      * @see #minSize
      */
    public void setMinSize(int width, int height) {
        if (width == -1 || height == -1) {
            _minSize = null;
        } else {
            _minSize = new Size(width, height);
        }
    }

    /** Returns the View's minimum size. If the minimum size has not been
      * set, returns a Size instance with zero width and height.
      * @see #setMinSize
      */
    public Size minSize() {
        if (_minSize != null) {
            return new Size(_minSize);
        }

        return new Size();
    }

    /** Resizes the View to the minimum size needed to display its contents.
      * Calls the <B>minSize()</B> method to get the View's minimum size
      * information.
      * @see #minSize
      */
    public void sizeToMinSize() {
        Size minSize = minSize();
        sizeTo(minSize.width, minSize.height);
    }



/* size change notification methods */

    /** Notifies a View that one of its subviews has changed size. This
      * information is important to ScrollViews and other View subclasses
      * that need to know if a descendant has changed size. The default
      * implementation simply passes the notification up to the View's
      * superview.
      */
    public void subviewDidResize(View aSubview) {
        if (_superview != null) {
            _superview.subviewDidResize(aSubview);
        }
    }

    /** Notifies a View that one of its subviews has moved. This
      * information is important to ScrollViews and other View subclasses
      * that need to know if a descendant has moved. The default
      * implementation simply passes the notification up to the View's
      * superview.
      */
    public void subviewDidMove(View aSubview) {
        if (_superview != null) {
            _superview.subviewDidMove(aSubview);
        }
    }

    /* view hierarchy */

    /** Sets the View's superview. You never call this method, but you might
      * override it to perform some action when the superview changes.
      * @see #addSubview
      * @see #removeFromSuperview
      */
    private void setSuperview(View aView) {
        RootView        rView;

        _superview = aView;
        ancestorWasAddedToViewHierarchy(aView);

        rView = rootView();
        if (rView != null) {
            rView.updateCursorLater();
            rView.viewHierarchyChanged();
        }
    }

    /** Adds <B>aView</B> to the Application's View hierarchy, as a subview
      * of this View.
      * @see #removeFromSuperview
      * @see #ancestorWasAddedToViewHierarchy
      */
    public void addSubview(View aView) {
        if (aView == null)
            return;
        invalidateKeyboardSelectionOrder();
        if (subviews == null)
            subviews = new Vector();
        else if (subviews.contains(aView))
            return;

        subviews.addElement(aView);
        aView.setSuperview(this);
        if(layoutManager != null)
            layoutManager.addSubview(aView);

    }

    /** Called when the View or one of its ancestors has been added to the
      * Application's View hierarchy.
      * @see #addSubview
      * @see #ancestorWillRemoveFromViewHierarchy
      */
    protected void ancestorWasAddedToViewHierarchy(View addedView) {
        View    nextView;
        int     i;

        if (buffered) {
            setBuffered(true);
        }

        if(needFocus)
            setFocusedView();

        i = subviewCount();
        while (i-- > 0) {
            nextView = (View)subviews.elementAt(i);
            nextView.ancestorWasAddedToViewHierarchy(addedView);
        }
    }

    protected void removeSubview(View aView) {
        invalidateKeyboardSelectionOrder();
        if (subviews != null)
            subviews.removeElement(aView);
        if(layoutManager != null)
            layoutManager.removeSubview(aView);
    }

    /** Removes the View from the Application's View hierarchy, setting its
      * superview to becomes <b>null</b>.
      * @see #addSubview
      * @see #ancestorWillRemoveFromViewHierarchy
      */
    public void removeFromSuperview() {
        RootView        rView;

        if (_superview != null) {
            rView = rootView();
            if (rView != null) {
                rView.updateCursorLater();
            }

            ancestorWillRemoveFromViewHierarchy(this);

            _superview.removeSubview(this);

            _superview = null;

            if(rView != null)
                rView.viewHierarchyChanged();
        }
    }

    /** Called when the View or one of its ancestors has been removed from
      * the Application's View hierarchy.
      * You should call
      *     super.ancestorWillRemoveFromViewHierarchy(removedView);
      * before returning.
      * @see #removeFromSuperview
      * @see #ancestorWasAddedToViewHierarchy
      */
    protected void ancestorWillRemoveFromViewHierarchy(View removedView) {
        RootView        rView;
        View            nextView;
        int             i;

        if (drawingBuffer != null) {
            drawingBuffer.flush();
            drawingBuffer = null;
        }

        rView = rootView();
        if (rView != null) {
            if (rView.mouseView() == this) {
                rView.setMouseView(null);
            }
            if (rView._moveView == this) {
                rView._moveView = null;
            }
        }

        if (isDirty) {
            setDirty(false);
        }

        i = subviewCount();

        while (i-- > 0) {
            nextView = (View)subviews.elementAt(i);
            nextView.ancestorWillRemoveFromViewHierarchy(removedView);
        }
    }

    /* event handling */


    /** Returns <B>true</B> if the View's <B>bounds</B> contains the point
      * (<B>x</B>, <B>y</B>).
      */
    public boolean containsPoint(int x, int y) {
        return Rect.contains(0, 0, width(), height(), x, y);
    }

    /** Returns <B>true</B> if the View contains the point (<B>x</B>,
      * <B>y</B>) within its visible rect.
      * @see #computeVisibleRect
      */
    public boolean containsPointInVisibleRect(int x, int y) {
        Rect            visibleRect;
        boolean         containsPoint;

        visibleRect = Rect.newRect();
        computeVisibleRect(visibleRect);

        containsPoint = visibleRect.contains(x, y);
        Rect.returnRect(visibleRect);

        return containsPoint;
    }

    /** Returns the smallest opaque View that completely contains <b>aRect</b>;
      * <b>aRect</b> is in the callee's superview's coordinate system.  If
      * <b>fromView</b> is <b>null</b>, it is assumed that the callee is the
      * RootView.
      */
    View _viewForRect(Rect aRect, View fromView) {
        View    nextView, viewForRect = null;
        Rect    tmpRect;
        int     i;

        if (aRect == null) {
            return null;
        }

        if (fromView != null) {
            if (!bounds.contains(aRect)) {
                return null;
            }

            /* convert to our coordinate system */
            tmpRect = Rect.newRect();
            fromView.convertRectToView(this, aRect, tmpRect);
        } else {
            /* already absolute */
            tmpRect = Rect.newRect(aRect);
        }

        i = subviewCount();
        while (i-- > 0) {
            nextView = (View)subviews.elementAt(i);
            viewForRect = nextView._viewForRect(tmpRect, this);
            if (viewForRect != null) {
                break;
            }
        }

        Rect.returnRect(tmpRect);

        if (viewForRect != null) {
            return viewForRect;
        } else if (isTransparent() && fromView != null) {
            return null;
        }

        return this;
    }

    /** Returns the View containing the point (<B>x</B>, <B>y</B>). The
      * View first checks its subviews to see if they contain the point,
      * and if not, checks itself.
      */
    public View viewForMouse(int x, int y) {
        View    hitView;
        int     i;
        Point   point = null, subPoint = null;

      /* have to make sure the point falls in our bounds first (we clip
       * our subviews)
       */
        if (!containsPoint(x, y)) {
            return null;
        }

        i = subviewCount();
        while (i-- > 0) {
            View subView = (View)subviews.elementAt(i);

            if (subView instanceof InternalWindow) {
                continue;
            }

            hitView = subView.viewForMouse(x - subView.bounds.x,
                                           y - subView.bounds.y);
            if (hitView != null) {
                return hitView;
            }
        }
        return this;
    }

    /** Returns the cursor that should appear when the mouse is over
      * the point (<b>x</b>, <b>y</b>) in the View's coordinate system.  By
      * default, this method returns ARROW_CURSOR.  Subclassers
      * should override this method to implement custom cursor behavior.
      */
    public int cursorForPoint(int x, int y) {
        return ARROW_CURSOR;
    }



    /* events */

    /** Called when the user clicks the mouse in the View. You should override
      * this method to return <b>true</b> if you want to receive subsequent
      * <b>mouseDragged()</b> and <b>mouseUp()</b> messages.  By default, this
      * method returns <b>false</b>.
      * @see #mouseDragged
      * @see #mouseUp
      */
    public boolean mouseDown(MouseEvent event) {
        return false;
    }

    /** Called when the user drags the mouse (moves it with the mouse button
      * depressed) after having initially clicked in the View. The mouse
      * down View will receive <B>mouseDragged()</B> messages until the
      * user releases the mouse button, even if the user drags the mouse
      * outside the mouse down View's bounds.
      * @see #mouseDown
      * @see #mouseUp
      */
    public void mouseDragged(MouseEvent event) {
    }

    /** Called when the user releases the mouse button.
      * @see #mouseDown
      */
    public void mouseUp(MouseEvent event) {
    }

    /** Called when the mouse enters the View's bounds.
      * @see #mouseMoved
      * @see #mouseExited
      */
    public void mouseEntered(MouseEvent event) {
    }


    /** Called when the mouse moves within the View's bounds, after an initial
      * <b>mouseEntered()</b> message.
      * @see #mouseEntered
      * @see #mouseMoved
      */
    public void mouseMoved(MouseEvent event) {
    }

    /** Called when the mouse exits the View's bounds, after an initial
      * <b>mouseEntered()</b> message.
      * @see #mouseEntered
      * @see #mouseMoved
      */
    public void mouseExited(MouseEvent event) {
    }

    /** Called when the user presses a key. The View must register itself with
      * the Application by calling its <b>setFocusedView()</b> method before it
      * can receive key down and key up events.
      * @see #setFocusedView
      * @see #keyUp
      */
    public void keyDown(KeyEvent event) {
        if (_superview != null) {
            _superview.keyDown(event);
        }
    }

    /** Called when the user releases a key, after an initial key down message.
      * @see #keyDown
      */
    public void keyUp(KeyEvent event) {
        if (_superview != null) {
            _superview.keyUp(event);
        }
    }

    /** Returns the View acting as this View's "scroll view," the View that
      * service its <b>scrollRectToVisible()</b> requests.  A View acting
      * as a "scroll view" should override this method and return itself.
      * @see #scrollRectToVisible
      */
    View scrollingView() {
        if (_superview != null) {
            return _superview.scrollingView();
        }

        return null;
    }

    /** Forwards the <b>scrollRectToVisible()</b> message to the View's
      * superview. Views that can service the request, such as a ScrollView,
      * override this method and perform the scrolling.
      *
      * @see ScrollView
      */
    public void scrollRectToVisible(Rect aRect) {
        if (_superview != null) {
            _superview.scrollRectToVisible(
                convertRectToView(_superview, aRect));
        }
    }


    /* drawing */

    /** Disables drawing within the View and its subviews.  Call
      * <b>reenableDrawing()</b> to enable drawing.
      * <b>disableDrawing()</b>/<b>reenableDrawing()</b> pairs can be nested,
      * and must be balanced.
      * @see #reenableDrawing
      */
    public void disableDrawing() {
        drawingDisabled++;
    }

    /** Reenables drawing within the View and its subviews.
      * @see #disableDrawing
      */
    public void reenableDrawing() {
        drawingDisabled--;
        if (drawingDisabled < 0) {
            drawingDisabled = 0;
        }
    }

    /** Returns <B>true</B> if drawing is enabled within the View.
      * @see #disableDrawing
      */
    public boolean isDrawingEnabled() {
        return (drawingDisabled == 0);
    }

    /** Returns <B>true</B> if the View is a member of the Application's View
      * hierarchy.
      */
    public boolean isInViewHierarchy() {
        RootView rootView = rootView();

        return (rootView != null) && rootView.isVisible();
    }

    /** Returns the View's RootView, or <b>null</b> if the View isn't
      * currently in the View hierarchy.
      */
    public RootView rootView() {
        if (_superview == null)
            return null;
        else
            return _superview.rootView();
    }

    /** Returns <b>true</b> if the View is a member of the Application's View
      * hierarchy and drawing for the View is enabled.
      * @see #isDrawingEnabled
      */
    public boolean canDraw() {
        if (drawingDisabled > 0 || _superview == null) {
            return false;
        } else {
            return _superview.canDraw();
        }
    }

    /** Computes the View's "visible rect," the intersection
      * of the View's and all of its ancestors' bounding rectangles, placing
      * it in <b>visibleRect</b>.
      * @see Rect
      */
    public void computeVisibleRect(Rect visibleRect) {

        if (_superview == null) {
            visibleRect.setBounds(0, 0, width(), height());
        } else {
            _superview.computeVisibleRect(visibleRect);
            _superview.convertRectToView(this, visibleRect, visibleRect);
            visibleRect.intersectWith(0, 0, width(), height());
        }
    }

    /** Returns <B>true</B> if the View is transparent. A View that's
      * transparent has a <b>drawView()</B> method that doesn't paint all of
      * the bits within the View's bounds. By default, this method returns
      * <b>true</b>. Views which are totally opaque should override this method
      * to return <b>false</b> to improve drawing performance. It is always
      * safe to return <b>true</b>, but the View's superviews may be drawn
      * unnecessarily.
      */
    public boolean isTransparent() {
        return true;
    }

    /** Returns <B>true</B> if the View wants the IFC to
      * coalesce mouse move or drag events, instead of sending each
      * individual event to the View. Returns <B>true</B> unless overridden.
      * @see Window
      */
    public boolean wantsMouseEventCoalescing() {
        return true;
    }

    View opaqueAncestor() {
        if (isTransparent() && _superview != null) {
            return _superview.opaqueAncestor();
        }

        return this;
    }

    // All View code needs to be scrutinized to determine where View needs to
    // call addDirtyRect() and setDirty() on itself.  ALERT!

    /** Adds a rectangle to be drawn after the current Event is processed.
      * Calling <b>addDirtyRect(null)</b> dirties the entire View and is
      * equivalent to <b>setDirty(true)</b>.
      *
      * @see #setDirty
      * @see RootView#drawDirtyViews
      */
    public void addDirtyRect(Rect rect) {
        RootView rootView;

        if (rect == null) {
            setDirty(true);
            return;
        }

        // To keep from always storing a dirtyRect, (isDirty && dirtyRect ==
        // null) means that the whole View is dirty.  If we aren't in the View
        // hierarchy, then we never get dirty.

        if (isDirty) {
            if (dirtyRect != null)
                dirtyRect.unionWith(rect);
        } else {
            rootView = rootView();
            if (rootView != null) {
                dirtyRect = new Rect(rect);
                rootView.markDirty(this);
                isDirty = true;
            }
        }
    }

    /** Registers the View to be drawn after processing the current Event.
      * If <b>flag()</b> is <b>true</b>, the entire View is
      * marked as needing to be redrawn. If <b>flag()</b> is
      * <b>false</b>, then the View is marked as not needing to be drawn.
      * RootView's <b>drawDirtyViews()</b> method calls
      * <b>setDirty(false)</b> on each dirty View after it has been drawn.
      * @see RootView#drawDirtyViews
      */
    public void setDirty(boolean flag) {
        RootView rootView;

        // Only set the dirty bit if we are in the View hierarchy and can
        // register for a draw with our RootView.

        if (flag) {
            if (!isDirty) {
                rootView = rootView();
                if (rootView != null) {
                    rootView.markDirty(this);
                    isDirty = true;
                }
            }
        } else {
            if (isDirty) {
                rootView = rootView();
                if (rootView != null) {
                    rootView.markClean(this);
                    isDirty = false;
                }
            }
        }

        // No matter what, we don't need to keep a dirtyRect.  When isDirty
        // is true and dirtyRect is null, then the whole View is dirty.

        dirtyRect = null;
    }

    /** Returns <b>true</b> if the View will be redrawn after the current
      * Event has been processed.
      * @see #addDirtyRect
      * @see #setDirty
      */
    public boolean isDirty() {
        return isDirty;
    }

    /** Draws the View's contents. You rarely call this method directly,
      * but you will override it to implement any View subclass drawing.
      * The IFC sets the Graphics' clipping rectangle to the region requiring
      * a redraw, and the Graphics object's origin to correspond to the View's
      * coordinate system origin. This method draws only the View's contents,
      * not its subviews.  The default implementation does nothing.
      * @see #draw
      */
    public void drawView(Graphics g) {
    }

    /** Calls the <b>drawView()</b> method of each of the View's subviews.
      * You never call this method directly, but you can override it to
      * implement special drawing. For example, the following code draws a
      * blue rectangle around the View's perimeter, on top of any drawing its
      * subviews may have performed.
      * <pre>
      *     super.drawSubviews(g);
      *     g.setColor(Color.blue);
      *     g.drawRect(0, 0, width(),height());
      * </pre>
      * Placing the <b>drawRect()</b> within the View's <b>drawView()</b>
      * gives its subviews a chance to draw over the rect. The IFC sets the
      * passed-in Graphics' clipping rectangle to the region that requires a
      * redraw.
      */
    public void drawSubviews(Graphics g) {
        View            nextView;
        Rect            clipRect, subClipRect = null;
        int             count, i;
        boolean         canDraw;

        if (drawingDisabled > 0) {
            return;
        }

        clipRect = g.clipRect();
        count = subviewCount();
        for (i = 0; i < count; i++) {
            nextView = (View)subviews.elementAt(i);

            canDraw = nextView.isDrawingEnabled();

            if (subClipRect == null) {
                subClipRect = Rect.newRect();
            }

            convertRectToView(nextView, clipRect, subClipRect);
            subClipRect.intersectWith(0, 0,
                                      nextView.bounds.width,
                                      nextView.bounds.height);
            if (canDraw && !subClipRect.isEmpty()) {
                nextView._drawView(g, subClipRect, false);
            }
        }

        if (subClipRect != null) {
            Rect.returnRect(subClipRect);
        }
    }

    void _drawView(Graphics g, Rect drawRect, boolean isTopLevelView) {
        if (drawingDisabled > 0) {
            return;
        }

        g.pushState();
        g.setDebug(this);

        if (!isTopLevelView) {
            g.translate(bounds.x, bounds.y);
        }

        if (drawRect == null) {
            Rect clipRect = Rect.newRect(0, 0, width(), height());

            g.setClipRect(clipRect);
            Rect.returnRect(clipRect);
        } else {
            g.setClipRect(drawRect);
        }

        // draw from our drawing buffer (if we have one) *only if* we're the
        // View at the top-level being asked to draw, and we're not drawing
        // into a drawingBuffer; if we're a subview of a View that initiated
        // the _drawView() recursion, we never get a chance to draw to our
        // buffer before blitting it out, so we can't trust its current
        // contents

        if (drawingBuffer != null && isTopLevelView &&
            !g.isDrawingBuffer()) {
            drawingBuffer.drawAt(g, 0, 0);
        } else {
            drawView(g);
            drawSubviews(g);
        }
        g.popState();
    }

    void clipAndDrawView(Graphics g, Rect clipRect) {
        Vector       windowClipRectVector, clipVector, disunionRectVector,
                     newVector, newClipVector, tmpVector;
        Rect         currentRect, nextRect, newRect, nextClipRect,
                     windowClipRect, otherRect, ancestorVisibleRect;
        int          i, j, count;
        boolean      rectClipped;

        /* clip to our bounds if drawing into a buffer, otherwise clip to
         * visible rect
         */
        if (g.isDrawingBuffer()) {
            ancestorVisibleRect = Rect.newRect(0, 0, width(), height());
        } else {
            ancestorVisibleRect = Rect.newRect();
            computeVisibleRect(ancestorVisibleRect);
        }

        ancestorVisibleRect.intersectWith(clipRect);
        clipRect = ancestorVisibleRect;

        /* don't bother with windows if we're the bground view drawing our
         * complete contents, or we're drawing into a drawing buffer (in
         * that case, there are no windows to obscure us)
         */
        if (this == rootView() &&
            clipRect.x == 0 && clipRect.y == 0 &&
            clipRect.width == width() && clipRect.height == height() ||
            g.isDrawingBuffer()) {
            windowClipRectVector = null;
        } else {
            windowClipRectVector =
                  rootView().windowRects(convertRectToView(null, clipRect),
                                         window());
        }

        if (windowClipRectVector == null || windowClipRectVector.isEmpty()) {
            _drawView(g, clipRect, true);

            Rect.returnRect(clipRect);
        } else {
            disunionRectVector = VectorCache.newVector();
            clipVector = VectorCache.newVector();

            // We will be converting the other rects from the absolute system
            clipRect.x += absoluteX();
            clipRect.y += absoluteY();

            clipVector.addElement(clipRect);
            newClipVector = VectorCache.newVector();

            i = windowClipRectVector.count();
            while (i-- > 0) {
                windowClipRect = (Rect)windowClipRectVector.elementAt(i);

                j = clipVector.count();
                while (j-- > 0) {
                    nextClipRect = (Rect)clipVector.elementAt(j);
                    nextClipRect.computeDisunionRects(windowClipRect,
                                                      disunionRectVector);

                    if (!disunionRectVector.isEmpty()) {
                        newClipVector.addElementsIfAbsent(disunionRectVector);
                        disunionRectVector.removeAllElements();
                    } else if (!windowClipRect.contains(nextClipRect)) {
                        newClipVector.addElement(nextClipRect);
                        clipVector.removeElement(nextClipRect);
                    }
                }

                tmpVector = clipVector;
                clipVector = newClipVector;
                newClipVector = tmpVector;
                Rect.returnRects(newClipVector);
            }
            VectorCache.returnVector(disunionRectVector);
            VectorCache.returnVector(newClipVector);

            count = clipVector.count();

            /* do any clip rects mark the same area? */
            for (i = 0; i < count; i++) {
                nextRect = (Rect)clipVector.elementAt(i);

                j = count;
                while (j-- > 0) {
                    otherRect = (Rect)clipVector.elementAt(j);

                    if (otherRect == nextRect) {
                        continue;
                    }

                    if (nextRect.contains(otherRect)) {
                        Rect.returnRect((Rect)clipVector.removeElementAt(j));
                        count--;
                        i = -1;
                        break;
                    }
                }
            }

            i = clipVector.count();

            while (i-- > 0) {
                clipRect = (Rect)clipVector.elementAt(i);

                if (!clipRect.isEmpty()) {
                    // Convert to our coord system
                    clipRect.x -= absoluteX();
                    clipRect.y -= absoluteY();
                    _drawView(g, clipRect, true);
                }
            }
            Rect.returnRects(clipVector);
            VectorCache.returnVector(clipVector);
        }

        Rect.returnRects(windowClipRectVector);
        VectorCache.returnVector(windowClipRectVector);
    }

    void _draw(Graphics g, Rect clipRect) {
        boolean         transparent;

        /* transparent views must make sure their superview has drawn; we do
         * this unless we have a DrawingBuffer and we're not drawing into it
         */
        transparent = (drawingBuffer != null && (!g.isDrawingBuffer())) ?
                                                    false : isTransparent();

        if (transparent && !(this instanceof InternalWindow)) {
            Rect ancestorClipRect = Rect.newRect();
            View opaqueAncestor = opaqueAncestor();

            convertRectToView(opaqueAncestor, clipRect, ancestorClipRect);
            g.pushState();
            g.translate(clipRect.x - ancestorClipRect.x,
                        clipRect.y - ancestorClipRect.y);
            opaqueAncestor.draw(g, ancestorClipRect);
            Rect.returnRect(ancestorClipRect);
            g.popState();

            return;
        }

        /* we're ready to go - make sure all buffers are valid */
        updateInvalidDrawingBuffers(clipRect);

        clipAndDrawView(g, clipRect);
    }

    /** Primitive method, instructing the View to draw the <B>clipRect</B>
      * portion of itself and its subviews to the Graphics <B>g</B>. If
      * <B>clipRect</B> is <B>null</B>, draws the entire View. If <B>g</B>
      * is <B>null</B>, uses the RootView's <B>graphics()</B>.
      * <B>draw()</B> sets the Graphics' clipping rectangle and ultimately
      * calls the View's and its subviews' <B>drawView()</B> methods. All
      * drawing occurs synchronously (that is, this method does not return
      * until the requested drawing has been performed).<p> You should
      * only call this form of <B>draw()</B> if you have to draw to a specific
      * Graphics. If not, use one of the more generic versions.
      */
    public void draw(Graphics g, Rect clipRect) {
        View            bufferView, nextView;
        Rect            tmpRect, absoluteClip;
        Point           point;
        int             count, i;
        boolean         canDraw;

        /* figure out what we're drawing */
        if (clipRect != null) {
            clipRect = Rect.newRect(clipRect);
            clipRect.intersectWith(0, 0, width(), height());

            if (clipRect.isEmpty()) {
                Rect.returnRect(clipRect);
                return;
            }
        } else {
            clipRect = Rect.newRect(0, 0, width(), height());
        }

        canDraw = canDraw();

        if (g == null || !g.isDrawingBuffer()) {
/* ALERT! - post1.0 - add the following line and change the if statement to:
            bufferView = ancestorWithDrawingBuffer();

            if (drawingBuffer == null || bufferView != this) {
*/
            if (drawingBuffer == null) {
                if (!canDraw) {
                    Rect.returnRect(clipRect);
                    return;
                }

                /* if an ancestor has a buffer, ask it to draw us */
/* ALERT! - post1.0 - comment out/remove the following line */
                bufferView = ancestorWithDrawingBuffer();
                if (bufferView != null && bufferView != this) {
                    tmpRect = Rect.newRect();
                    computeVisibleRect(tmpRect);
                    tmpRect.intersectWith(clipRect);

                    convertRectToView(bufferView, tmpRect, tmpRect);
                    bufferView.drawingBufferValid = false;
                    if (g == null) {
                        g = bufferView.createGraphics();
                        bufferView.draw(g, tmpRect);
                        g.dispose();
                        g = null;
                    } else {
                        point = new Point(0, 0);
                        convertPointToView(bufferView, point, point);
                        g.pushState();
                        try {
                            g.translate(-point.x, -point.y);
                            bufferView.draw(g, tmpRect);
                        } finally {
                            g.popState();
                        }
                    }

                    Rect.returnRect(tmpRect);
                    Rect.returnRect(clipRect);

                    return;
                }
            } else {
                /* if we're not drawing to a DrawingBuffer but we have one,
                 * we need to blit ourselves to our buffer before proceeding
                 */

                if (!drawingBufferIsBitCache) {
                    updateDrawingBuffer(clipRect);
                }
            }

            absoluteClip = convertRectToView(rootView(), clipRect);
            rootView().redrawTransparentWindows(absoluteClip, window());
        }

        /* time to really draw something */
        if ((g != null && g.isDrawingBuffer()) || canDraw) {
            if (g == null) {
                g = createGraphics();
                _draw(g, clipRect);
                g.dispose();
                g = null;
            } else {
                _draw(g, clipRect);
            }
        }

        Rect.returnRect(clipRect);
    }

    /** Convenience method for drawing the <B>clipRect</B> portion of the
      * View. Equivalent to the code:
      * <pre>
      *     draw(createGraphics(), clipRect);
      * </pre>
      */
    public void draw(Rect clipRect) {
        if (isInViewHierarchy()) {
            draw(null, clipRect);
        }
    }

    /** Convenience method for drawing the entire View. Equivalent to
      * the code:
      * <pre>
      *     draw(createGraphics(), null);
      * </pre>
      */
    public void draw() {
        if (isInViewHierarchy()) {
            draw(null, null);
        }
    }

    /* drawing buffer support */


    /** Calling <b>setBuffered(true)</b> causes the View to allocate
      * an offscreen drawing buffer. The results of all drawing operations
      * performed within the View's <b>drawView()</b> method, and those of
      * its subviews, go first to the buffer and then to the screen, reducing
      * drawing flicker at the cost of speed and memory. In general,
      * reasonably flicker-free drawing can be achieved without drawing
      * buffers by careful attention to redrawing the minimum amount
      * necessary.
      */
    public void setBuffered(boolean flag) {
        buffered = flag;

        if (flag && drawingBuffer == null /*&&
            ancestorWithDrawingBuffer() == null*/) {
            if (bounds.width != 0 && bounds.height != 0) {
                drawingBuffer = createBuffer();
            }
            drawingBufferValid = false;
        } else if (!flag && drawingBuffer != null) {
            drawingBuffer.flush();
            drawingBuffer = null;
        }
    }

    /** Returns <b>true</b> if the View has an offscreen drawing buffer.
      * @see #setBuffered
      */
    public boolean isBuffered() {
        return buffered;
    }

    /** Returns the View's offscreen drawing buffer, if any.
      * @see #setBuffered
      */
    public Bitmap drawingBuffer() {
        return drawingBuffer;
    }

    void updateDrawingBuffer(Rect updateRect) {
        Graphics bufferedGraphics;

        if (!updateRect.intersects(0, 0, width(), height())) {
            return;
        }

        if (drawingBuffer != null) {
            synchronized(drawingBuffer) {
               /* update the drawing buffer; we need to say that it's valid,
                * before we actually do any drawing, otherwise if we're on
                * the rootView, we'll get into a loop where the
                * background tries to update our buffer before drawing itself
                * and we ask the background to draw itself in order to
                * validate our buffer
                */
                bufferedGraphics = Graphics.newGraphics(drawingBuffer);
                drawingBufferValid = true;

                bufferedGraphics.setDebugOptions(shouldDebugGraphics());
                draw(bufferedGraphics, updateRect);
                if (!canDraw()) {
                    drawingBufferValid = false;
                }
                bufferedGraphics.dispose();
                bufferedGraphics = null;
            }
        }
    }

    void updateInvalidDrawingBuffers(Rect clipRect) {
        View            nextView;
        Rect            subClipRect = null;
        int             i;

        i = subviewCount();
        while (i-- > 0) {
            nextView = (View)subviews.elementAt(i);

            if (subClipRect == null) {
                subClipRect = Rect.newRect();
            }

            convertRectToView(nextView, clipRect, subClipRect);
            if (!subClipRect.intersects(0, 0,
                                        nextView.width(),nextView.height())) {
                continue;
            }

            if (nextView.drawingBuffer != null
                && !nextView.drawingBufferValid) {
                nextView.updateDrawingBuffer(subClipRect);
            }

            nextView.updateInvalidDrawingBuffers(subClipRect);
        }

        if (subClipRect != null) {
            Rect.returnRect(subClipRect);
        }
    }

    View ancestorWithDrawingBuffer() {
        if (drawingBuffer != null) {
            return this;
        } else if (_superview == null) {
            return null;
        }

        return _superview.ancestorWithDrawingBuffer();

/* ALERT! - post1.0 - use this new version:
        View    bufferedView = null;

        if (_superview != null) {
            bufferedView = _superview.ancestorWithDrawingBuffer();
        }
        if (bufferedView != null) {
            return bufferedView;
        } else if (drawingBuffer != null) {
            return this;
        }

        return null;
*/
    }

    void _startFocus() {
        if(focusPaused) {
            focusPaused = false;
            resumeFocus();
        } else {
            startFocus();
        }
    }

    void _stopFocus() {
        focusPaused=false;
        stopFocus();
    }

    void _pauseFocus() {
        focusPaused=true;
        pauseFocus();
    }

    /** Tells the View that it has become the focus of KeyEvents.
      * @see #stopFocus
      */
    public void startFocus() {
    }

    /** Tells the View that it has ceased being the focus of KeyEvents.
      * @see #startFocus
      */
    public void stopFocus() {
    }

    /** Tells the View that it has temporarily ceased being the focus of
      * KeyEvents, such as when the user begins working with another
      * application.  The View will receive a <b>resumeFocus()</b> message when
      * it again regains focus.
      * @see #resumeFocus
      */
    public void pauseFocus() {
    }

    /** Tells the View that it has regained the KeyEvent focus.
      * @see #pauseFocus
      */
    public void resumeFocus() {
    }

    /** Tells a View that itself or one of its descendents wants to become the
      * focus of KeyEvents. By default, this method simply forwards the
      * message to its superview.  It will eventually reach the RootView
      * which will take appropriate action.  You should never call this method
      * directly on the RootView because there may be other Views between
      * a View and the RootView that want to know about focused View
      * change requests, such as an InternalWindow.
      */
    void setFocusedView(View view) {
        if (_superview != null )
            _superview.setFocusedView(view);
    }

    /** Tells a View that it should become the focus of KeyEvents.  The
      * View must be part of the View hierarchy in order to receive
      * KeyEvents.
      */
    public void setFocusedView() {
        if (_superview != null && (isInViewHierarchy() || window() != null)) {
            _superview.setFocusedView(this);
            needFocus = false;
        } else {
          needFocus = true;
        }
    }


    /* archiving */

    /** Describes the View class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.View", 2);
        info.addField(BOUNDS_KEY, OBJECT_TYPE);
        info.addField(MINSIZE_KEY, OBJECT_TYPE);
        info.addField(SUBVIEWS_KEY, OBJECT_TYPE);
        info.addField(RESIZE_KEY, BYTE_TYPE);
        info.addField(DRAWINGDISABLED_KEY, INT_TYPE);
        info.addField(AUTORESIZE_KEY, BOOLEAN_TYPE);
        info.addField(BUFFERED_KEY, BOOLEAN_TYPE);
        info.addField(LAYOUTMANAGER_KEY, OBJECT_TYPE);
        info.addField(KEYBOARD_BINDINGS_KEY, OBJECT_TYPE);
    }

    /** Encodes the View instance.
      * @see Codable#decode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(BOUNDS_KEY, bounds);
        encoder.encodeObject(MINSIZE_KEY, _minSize);

        if (subviewCount() == 0) {
            encoder.encodeObject(SUBVIEWS_KEY, null);
        } else {
            encoder.encodeObject(SUBVIEWS_KEY, subviews);
        }

        encoder.encodeByte(RESIZE_KEY, resizeInstr);
        encoder.encodeInt(DRAWINGDISABLED_KEY, drawingDisabled);
        encoder.encodeBoolean(AUTORESIZE_KEY, autoResizeSubviews);
        encoder.encodeBoolean(BUFFERED_KEY, buffered);
        encoder.encodeObject(LAYOUTMANAGER_KEY, layoutManager);
        encoder.encodeObject(KEYBOARD_BINDINGS_KEY,_keyboardBindings);
    }

    /** Decodes the View instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        int version = decoder.versionForClassName("netscape.application.View");
        Object          nextObject;
        boolean         flag;

        bounds = (Rect)decoder.decodeObject(BOUNDS_KEY);
        _minSize = (Size)decoder.decodeObject(MINSIZE_KEY);
        nextObject = decoder.decodeObject(SUBVIEWS_KEY);
        if (nextObject != null) {
            subviews = (Vector)nextObject;
        }

        resizeInstr = decoder.decodeByte(RESIZE_KEY);
        drawingDisabled = decoder.decodeInt(DRAWINGDISABLED_KEY);

        flag = decoder.decodeBoolean(AUTORESIZE_KEY);
        if (flag) {
            setAutoResizeSubviews(flag);
        }
        flag = decoder.decodeBoolean(BUFFERED_KEY);
        if (flag) {
            setBuffered(flag);
        }

        layoutManager = (LayoutManager)decoder.decodeObject(LAYOUTMANAGER_KEY);

        if(version >= 2) {
            _keyboardBindings = (Hashtable) decoder.decodeObject(KEYBOARD_BINDINGS_KEY);
        }
    }

    /** Finishes the View instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        View    nextView;
        int     i;

        i = subviewCount();
        while (i-- > 0) {
            nextView = (View)subviews.elementAt(i);
            nextView.setSuperview(this);
            nextView._setBounds(nextView.bounds.x, nextView.bounds.y,
                                nextView.bounds.width, nextView.bounds.height);
        }
        _setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }


    /** Convenience method, returning the View's Application instance. */
    Application application() {
        return Application.application();
    }

    /** Converts the <B>x</B> and <B>y</B> to <B>otherView</B>'s
      * coordinate system, and stores the result in <B>destPoint</B>. If
      * <B>otherView</B> is <b>null</b>, <b>destPoint</b> will be in the
      * absolute coordinate system.
      */
    public void convertToView(View otherView, int x, int y, Point destPoint) {
        int destX = x, destY = y;

        if (_superview == otherView) {
            destX += bounds.x;
            destY += bounds.y;
        } else if (otherView != null && otherView._superview == this) {
            destX -= otherView.bounds.x;
            destY -= otherView.bounds.y;
        } else {
            View sView;

            for (sView = this;
                 sView._superview != null;
                 sView = sView._superview) {
                destX += sView.bounds.x;
                destY += sView.bounds.y;
            }

            if (otherView != null) {
                View otherSView;

                for (otherSView = otherView;
                        otherSView._superview != null;
                        otherSView = otherSView._superview) {
                    destX -= otherSView.bounds.x;
                    destY -= otherSView.bounds.y;
                }

                if (sView != otherSView) {
                    throw new InconsistencyException("Can't convert between " +
                        this + " and " + otherView + ", no common ancestor");
                }
            }
        }

        destPoint.x = destX;
        destPoint.y = destY;
    }

    /** Converts the <B>x</B> and <B>y</B> to <B>otherView</B>'s
      * coordinate system, and returns the result. If
      * <B>otherView</B> is <b>null</b>, <b>destPoint</b> will be in the
      * absolute coordinate system.
      */
    public Point convertToView(View otherView, int x, int y) {
        Point point = new Point();

        convertToView(otherView, x, y, point);
        return point;
    }

    /** Converts the <B>sourceRect</B> to <B>otherView</B>'s coordinate
      * system, and stores the result in <B>destRect</B>. If <B>otherView</B>
      * is <b>null</b>, <B>destRect</B> will be in the absolute coordinate
      * system.
      */
    public void convertRectToView(View otherView,
                                  Rect sourceRect, Rect destRect) {
        Point destPoint = Point.newPoint();

        convertToView(otherView, sourceRect.x, sourceRect.y, destPoint);
        destRect.setBounds(destPoint.x, destPoint.y,
                           sourceRect.width, sourceRect.height);
        Point.returnPoint(destPoint);
    }

    /** Converts the <B>sourcePoint</B> to <B>otherView</B>'s coordinate
      * system, and stores the result in <B>destPoint</B>. If <B>otherView</B>
      * is <b>null</b>, <B>destPoint</B> will be in the absolute coordinate
      * system.
      */
    public void convertPointToView(View otherView,
                                   Point sourcePoint, Point destPoint) {
        convertToView(otherView, sourcePoint.x, sourcePoint.y, destPoint);
    }

    /** Returns the rectangle containing <B>sourceRect</B> converted
      * to <B>otherView</B>'s coordinate system. If <B>otherView</B> is
      * <b>null</b>, the returned Rect is in the absolute coordinate system.
      */
    public Rect convertRectToView(View otherView, Rect sourceRect) {
        Rect destRect = new Rect();

        convertRectToView(otherView, sourceRect, destRect);
        return destRect;
    }

    /** Returns a rectangle containing <B>srcPoint</B> converted
      * to <B>otherView</B>'s coordinate system. If <B>otherView</B> is
      * <b>null</b>, the returned Rect is in the absolute coordinate system.
      */
    public Point convertPointToView(View otherView, Point sourcePoint) {
        Point destPoint = new Point();

        convertPointToView(otherView, sourcePoint, destPoint);
        return destPoint;
    }

    /** Returns a MouseEvent similar to <b>sourceEvent</b> except that its x
      * and y members have been converted to <B>otherView</B>'s coordinate
      * system.  If <B>otherView</B> is <b>null</b>, the
      * returned MouseEvent is in the absolute coordinate system.
      */
    public MouseEvent convertEventToView(View otherView,
                                         MouseEvent sourceEvent) {
        Point destPoint = Point.newPoint();
        MouseEvent dstEvent = (MouseEvent)sourceEvent.clone();

        convertToView(otherView, sourceEvent.x, sourceEvent.y, destPoint);
        dstEvent.x = destPoint.x;
        dstEvent.y = destPoint.y;

        Point.returnPoint(destPoint);
        return dstEvent;
    }

    /** Enables or disables diagnostic information about every graphics
      * operation performed within the View or one of its subviews. The
      * value of <b>debug</b> determines how the View should display this
      * information:
      * <ul>
      * <li>DebugGraphics.LOG_OPTION - causes a text message to be printed.
      * <li>DebugGraphics.FLASH_OPTION - causes the drawing to flash several
      * times.
      * <li>DebugGraphics.BUFFERED_OPTION - creates an ExternalWindow that
      * displays the operations performed on the View's offscreen buffer.
      * </ul>
      * <b>debug</b> is bitwise OR'd into the current value.
      * DebugGraphics.NONE_OPTION disables debugging.
      * A value of 0 causes no changes to the debugging options.
      */
    public void setGraphicsDebugOptions(int debugOptions) {
        Graphics.setViewDebug(this, debugOptions);
    }

    /** Returns the state of graphics debugging.
      * @see #setGraphicsDebugOptions
      */
    public int graphicsDebugOptions() {
        return Graphics.viewDebug(this);
    }

    /** Returns <b>true</b> if debug information is enabled for this View
      * or one if its ancestors.
      */
    int shouldDebugGraphics() {
        return Graphics.shouldViewDebug(this);
    }

    int absoluteX() {
        int x = 0;
        View sView;

        for (sView = this; sView != null; sView = sView._superview) {
            x += sView.bounds.x;
        }
        return x;
    }

    int absoluteY() {
        int y = 0;
        View sView;

        for (sView = this; sView != null; sView = sView._superview) {
            y += sView.bounds.y;
        }
        return y;
    }

    /** Sets the View's LayoutManager, the object responsible for sizing
      * and positioning the View's subviews.
      */
    public void setLayoutManager(LayoutManager value)       {
        layoutManager = value;
    }

    /** Returns the View's LayoutManager.
      * @see #setLayoutManager
      */
    public LayoutManager layoutManager() {
        return layoutManager;
    }

    /** Sizes and positions the View's subviews.  By default, a View acts as
      * its own LayoutManager.
      */
    public void layoutView(int deltaWidth, int deltaHeight) {
        if(layoutManager == null)       {
            relativeLayoutView(deltaWidth, deltaHeight);
        } else {
            layoutManager.layoutView(this, deltaWidth, deltaHeight);
        }
    }

    private void relativeLayoutView(int deltaWidth, int deltaHeight) {
        int i;
        int x, y, w, h;
        View subview;

        i = subviewCount();
        while (i-- > 0) {
            subview = (View)subviews.elementAt(i);

            x = subview.bounds.x;
            y = subview.bounds.y;
            w = subview.bounds.width;
            h = subview.bounds.height;

            switch (subview.horizResizeInstruction()) {
                case RIGHT_MARGIN_CAN_CHANGE:
                    break;
                case LEFT_MARGIN_CAN_CHANGE:
                    x += deltaWidth;
                    break;
                case WIDTH_CAN_CHANGE:
                    w += deltaWidth;
// ALERT - I don't think clipping to the min size is the right thing to do
//                    min = subview.minSize();
//                    if (w < min.width)
//                        w = min.width;
                    break;
                case CENTER_HORIZ:
                    x = (bounds.width - subview.bounds.width) / 2 ;
                    break;
                default:
                    throw new InconsistencyException("invalid horz resize instruction: " + subview.horizResizeInstruction());
            }

            switch (subview.vertResizeInstruction()) {
                case BOTTOM_MARGIN_CAN_CHANGE:
                    break;
                case TOP_MARGIN_CAN_CHANGE:
                    y += deltaHeight;
                    break;
                case HEIGHT_CAN_CHANGE:
                    h += deltaHeight;
// ALERT - ditto
//                    if (min == null)
//                        min = subview.minSize();
//                    if (h < min.height)
//                        h = min.height;
                    break;
                case CENTER_VERT:
                    y = (bounds.height - subview.bounds.height) / 2 ;
                    break;
                default:
                    throw new InconsistencyException("invalid vert resize instruction: " + subview.vertResizeInstruction());
            }

            subview.setBounds(x, y, w, h);
        }
    }

    /** Returns the rectangle (0, 0, <b>width()</b>, <b>height()</b>).
      * @see #bounds()
      */
    public Rect localBounds() {
        return new Rect(0, 0, bounds.width, bounds.height);
    }

    /** Creates a Graphics object for the View.  The caller must call
      * <b>dispose()</b> on this Graphics to free its resources.  Subclasses
      * of View can override this method to return custom subclasses of
      * Graphics.<p>
      * This method throws an exception if the View is not in the View
      * hierarchy.
      * @see Graphics#dispose()
      */
    public Graphics createGraphics() {
        return Graphics.newGraphics(this);
    }

    /** Creates a Bitmap for the View to use as a drawing buffer.  View
      * subclasses can override this method to return custom subclasses of
      * Bitmap.
      */
    protected Bitmap createBuffer() {
        return new Bitmap(width(), height());
    }

    /** Returns the View's string representation. */
    public String toString() {
        return super.toString() + bounds.toString();
    }

    /** Keyboard UI support **/

    /** Return whether this view can become the selected view
      * when the user is moving from view to views with the keyboard
      * The default implementation returns false.
      *
      */
    public boolean canBecomeSelectedView() {
        return false;
    }

    /** Return whether this view hides its subviews from the keyboard ui
      * main system The default implementation returns false. Override this
      * method and return true if your view provides a different keyboard
      * UI strategy for its subviews.
      *
      */
    public boolean hidesSubviewsFromKeyboard() {
        return false;
    }

    /** Return the View that should become selected when the user
      * press the tab key. If the result is null, the keyboard UI
      * system will select the next available view.
      * The default implementation returns null.
      *
      */
    public View nextSelectableView() {
        return null;
    }

    /** Return the View that should become selected when the user
      * press the backtab key. If the result is null, the keyboard UI
      * system will select the next available view.
      * The default implementation returns null.
      *
      */
    public View previousSelectableView() {
        return null;
    }

    /** You should call this method if the result of nextSelectableView() or
      * previousSelectableView() changes.
      *
      */
   public void invalidateKeyboardSelectionOrder() {
       kbdOrder = null;
   }

    /** Inform the view that it is about to become the selected view
      * The default implementation requests RootView to display the
      * keyboard arrow.
      *
      */
    public void willBecomeSelected() {
        wantsKeyboardArrow = true;
    }

    /** Inform the view that it will no longer be the selected view
      * The default implementation requests RootView to hide the
      * keyboard arrow.
      */
    public void willBecomeUnselected() {
        wantsKeyboardArrow = false;
    }

    /** Inform the keyboard UI system that the receiving view should
      * receive the command <b>aCommand</b> with data <b>cmdData</b>
      * when the key <b>aKey</b> is pressed with the modifier <b>modifier</b>.
      * <b>when</b> can be View.WHEN_SELECTED, View.WHEN_IN_MAIN_WINDOW or
      * View.ALWAYS If <b>aCommand</b> is null, this method removes the
      * binding.  <b>aKey</b> can be one of the constants defined into the
      * KeyEvent class
      * @see KeyEvent
      *
      */
    public void setCommandForKey(String aCommand,Object cmdData,
                                 int key,int modifiers,int when) {
        KeyStroke keyStroke = new KeyStroke(key,modifiers);
        if(_keyboardBindings == null)
            _keyboardBindings = new Hashtable();

        if(aCommand == null)
            _keyboardBindings.remove(keyStroke);
        else {
            Hashtable data;

            data = new Hashtable();
            data.put(KBD_COMMAND_KEY,aCommand);
            data.put(KBD_WHEN,"" + when);
            if(cmdData != null)
                data.put(KBD_DATA_KEY,cmdData);
            _keyboardBindings.put(keyStroke,data);
        }
    }

    /** Convenience to add <b>aCommand</b> when the key <b>aKey</b> is pressed.
      * This method calls setCommandForKey() with cmdData set to the
      * receiver and modifiers set to null.
      *
      */
    public void setCommandForKey(String aCommand,int aKey,int when) {
        setCommandForKey(aCommand,this,aKey,KeyEvent.NO_MODIFIERS_MASK,when);
    }

    /** Convenience to remove a command associated with a key.
      *
      */
    public void removeCommandForKey(int aKey) {
        setCommandForKey(null,null,aKey,KeyEvent.NO_MODIFIERS_MASK,0);
    }

    /** Remove all the command for all the keys **/
    public void removeAllCommandsForKeys() {
        _keyboardBindings = null;
    }

    boolean hasKeyboardBindings() {
        if(_keyboardBindings != null && _keyboardBindings.count() > 0)
            return true;
        else
            return false;
    }

    boolean performCommandForKeyStroke(KeyStroke aKeyStroke,int condition) {
        if(!(this instanceof Target))
            return false;

        if(_keyboardBindings!=null) {
            Enumeration keys = _keyboardBindings.keys();
            KeyStroke ks;

            while(keys.hasMoreElements()){
                ks = (KeyStroke) keys.nextElement();
                if(ks.equals(aKeyStroke)) {
                    boolean sendCommand = false;
                    Hashtable data = (Hashtable)_keyboardBindings.get(ks);
                    String s = (String)data.get(KBD_WHEN);
                    int when = Integer.parseInt(s);

                    switch(when) {
                    case WHEN_SELECTED:
                        if(condition == WHEN_SELECTED)
                            sendCommand = true;
                        break;
                    case WHEN_IN_MAIN_WINDOW:
                        if(condition == WHEN_SELECTED || condition == WHEN_IN_MAIN_WINDOW)
                            sendCommand = true;
                        break;
                    case ALWAYS:
                        sendCommand = true;
                        break;
                    default:
                        throw new InconsistencyException("Wrong condition:" + when);
                    }
                    if(sendCommand) {
                        String command = (String)data.get(KBD_COMMAND_KEY);
                        ((Target)this).performCommand(command,data.get(KBD_DATA_KEY));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Return the rect that should be used to show the keyboard UI
     *  arrow. The default implementation returns localBounds()
     */
    public Rect keyboardRect() {
        return localBounds();
    }

    /** Return the subview that is on the top left corner **/
    View _firstSubview(Vector subViews) {
        int  minX,minY;
        View minXView,minYView,view;
        int i,c;

        if(subViews.count() == 0)
            return null;
        minYView = (View)subViews.elementAt(0);
        minY = minYView.y();
        for(i=1,c=subViews.count();i<c;i++) {
            view = (View)subViews.elementAt(i);
            if(view.y() < minY) {
                minYView = view;
                minY = view.y();
            }
        }

        minXView = minYView;
        minX = minYView.x();
        for(i=0,c=subViews.count();i<c;i++) {
            view = (View)subViews.elementAt(i);
            if(view == minXView)
                continue;

            if((int)Math.sqrt((double)((view.y() - minXView.y()) *
                                       (view.y() - minXView.y()))) <= 10) {
                if(view.x() < minX) {
                    minXView = view;
                    minX = view.x();
                }
            }
        }

        return minXView;
    }


   private void validateKeyboardOrder() {
       if(kbdOrder==null){
           int i,c;
           Vector  sv = new Vector();
           View v,nv;

           for(i=0,c=subviews.count();i<c;i++)
               sv.addElement(subviews.elementAt(i));

           kbdOrder = new Vector();
           while(sv.count() > 0) {
               v = _firstSubview(sv);
               kbdOrder.addElement(v);
               sv.removeElement(v);
               while((nv = v.nextSelectableView()) != null) {
                   if(sv.indexOfIdentical(nv) != -1) {
                       kbdOrder.addElement(nv);
                       sv.removeElement(nv);
                       v = nv;
                   } else
                       break;
               }
           }
       }
   }

    View firstSubview() {
        validateKeyboardOrder();
        if(kbdOrder.count() > 0)
            return (View)kbdOrder.elementAt(0);
        else
            return null;
    }

    View lastSubview() {
        validateKeyboardOrder();
        if(kbdOrder.count() > 0)
            return (View)kbdOrder.elementAt(kbdOrder.count() - 1);
        else
            return null;
    }

    View viewAfter(View anotherView) {
        int index;
        validateKeyboardOrder();

        index = kbdOrder.indexOfIdentical(anotherView);
        if(index != -1 && index < (kbdOrder.count()-1))
            return (View)kbdOrder.elementAt(index + 1);
        else
            return null;
    }

    View viewBefore(View anotherView) {
        int index;
        validateKeyboardOrder();

        index = kbdOrder.indexOfIdentical(anotherView);
        if(index > 0)
            return (View)kbdOrder.elementAt(index - 1);
        else
            return null;
    }

    boolean wantsKeyboardArrow() {
        return wantsKeyboardArrow;
    }

    /** This method will collect the dirtyRects from this view and it's children
      * and union them into <b>rect</b>.
      */
    void getDirtyRect(Rect rect) {
        if (isDirty() && dirtyRect == null) {
            if (rect.isEmpty()) {
                rect.setBounds(bounds);
            } else {
                rect.unionWith(bounds);
            }
        } else {
            int count;

            if (dirtyRect != null) {
                if (rect.isEmpty()) {
                    rect.setBounds(dirtyRect.x + bounds.x, dirtyRect.y + bounds.y,
                                   dirtyRect.width, dirtyRect.height);
                } else {
                    rect.unionWith(dirtyRect.x + bounds.x, dirtyRect.y + bounds.y,
                                   dirtyRect.width, dirtyRect.height);
                }
            }

            count = subviewCount();
            if (count != 0) {
                rect.moveBy(-bounds.x, -bounds.y);
                while (count-- > 0) {
                    ((View)subviews.elementAt(count)).getDirtyRect(rect);
                }
                rect.moveBy(bounds.x, bounds.y);
            }
        }
    }

}






