// DragSession.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass that implements "drag-and-drop" within the IFC. A
  * DragSession allows an Image to represent some data, and the user can
  * drag that Image from one View to another. For example, in the case of
  * dragging Colors, a red square Image would visually represent the red
  * Color instance, declared as the DragSession's <B>data</b>.<br><br>A
  * drag-and-drop session begins with a mouse down event in a View. That View's
  * <b>mouseDown()</b>
  * creates a DragSession, and from then on the DragSession's Image moves as
  * the user moves the mouse.  All Views it passes over receive
  * <B>acceptsDrag()</B> messages allowing them to return an object to act as
  * the DragSession's DragDestination. A DragSession receives
  * <b>dragEntered()</b>, <b>dragMoved()</b> and <b>dragExited()</b> messages
  * as the Image traverses the View. On release, the current DragDestination
  * receives a <B>dragDropped()</b> message, in which it should accept the
  * DragSession's data by returning <b>true</b>, or reject it by returning
  * <b>false</b>. If rejected, the DragSession Image optionally animates back
  * to its origin. Ultimately, the DragSource receives a notification
  * that the drag session has completed.
  * @note 1.0 added private method to support creating the drag view
  * @note 1.0 mouseDrag/mouseup went public with a private tag
  * @note 1.0 some point conversion fixes
  * @note 1.0 added accessor to isAccepting. added private tag
  */
public class DragSession {
    String              dataType;
    Object              data;
    Image               image;
    int                 initialX, initialY;
    int                 mouseDownX, mouseDownY;
    int                 mouseX, mouseY;
    DragSource          source;
    DragDestination     destination;
    DragView            dragView;
    int                 modifiers;
    RootView            rootView;
    View                sourceView;
    View                destinationView;
    boolean             isAccepting;

    /** Drag session modifier flag. */
    public static final int     SHIFT_MASK = KeyEvent.SHIFT_MASK;
    /** Drag session modifier flag. */
    public static final int     CONTROL_MASK = KeyEvent.CONTROL_MASK;
    /** Drag session modifier flag. */
    public static final int     META_MASK = KeyEvent.META_MASK;
    /** Drag session modifier flag. */
    public static final int     ALT_MASK = KeyEvent.ALT_MASK;

    /** @private */
    public DragSession(DragSource source, Image image,
                       int initialX, int initialY,
                       int mouseDownX, int mouseDownY,
                       String dataType, Object data,
                       boolean createDragView) {
        Point initialPoint = new Point(), mousePoint = new Point();

        sourceView = source.sourceView(this);
        rootView = sourceView.rootView();

        sourceView.convertToView(null, initialX, initialY, initialPoint);
        sourceView.convertToView(null, mouseDownX, mouseDownY, mousePoint);

        /// ALERT - Check this out
        if (rootView.windowClipView() != null) {
            rootView.convertPointToView(rootView.windowClipView(),
                                         initialPoint, initialPoint);
            rootView.convertPointToView(rootView.windowClipView(),
                                         mousePoint, mousePoint);
        }

        this.source = source;
        this.image = image;
        this.initialX = initialPoint.x;
        this.initialY = initialPoint.y;
        this.mouseDownX = mousePoint.x;
        this.mouseDownY = mousePoint.y;
        this.dataType = dataType;
        this.data = data;

        if (createDragView) {
            dragView = new DragView(this);
        }
    }

    /** Constructs a DragSession, represented visually by <b>image</b>.
      * <b>initialX</b> and <b>initialY</b> specify the Image's initial
      * location, and <b>mouseDownX</b> and <b>mouseDownY</b> specify the
      * mouse's initial location. Both points should be expressed in
      * terms of the coordinate system of the View returned by <b>source</b>.
      * This source View also determines the RootView in which the
      * DragSession's Image appears.  <b>dataType</b> allows for additional
      * information about the drag data <b>data</b>.
      */
    public DragSession(DragSource source, Image image,
                       int initialX, int initialY,
                       int mouseDownX, int mouseDownY,
                       String dataType, Object data) {
        this(source, image, initialX, initialY, mouseDownX, mouseDownY,
             dataType, data, true);
    }

    /** Returns the DragSession's <b>data</b>.
      * @see #setData
      */
    public Object data() {
        return data;
    }

    /** Sets the DragSession's <b>data</b>, the data being dragged in
      * the drag session.
      */
    public void setData(Object data) {
        this.data = data;
    }

    /** Returns the DragSession's data's type.
      * @see #setDataType
      */
    public String dataType() {
        return dataType;
    }

    /** Sets the DragSession's data type information to <b>dataType</b>.  The
      * data type allows for additional information about the drag data.
      */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /** Returns the DragSession's "source."  A session's drag source cannot
      * change once a session has begun.
      */
    public DragSource source() {
        return source;
    }

    /** Returns the DragSession's current DragDestination. The DragSession
      * determines the current destination by calling the <b>acceptsDrag()</b>
      * method of the View under the mouse.
      * @see View#acceptsDrag
      */
    public DragDestination destination() {
        return destination();
    }

    /** Returns an integer representing the modifier keys (Shift, Control,
      * Meta, Alternate) held down during the most recent mouse event.
      * Bitwise AND this value with one of the class' modifier flags, or call
      * one of the explicit methods such as <b>isShiftKeyDown()</b>.
      * @see #isShiftKeyDown
      * @see #isControlKeyDown
      * @see #isMetaKeyDown
      * @see #isAltKeyDown
      */
    public int dragModifiers() {
        return modifiers;
    }

    /** Returns <b>true</b> if the Shift Key was held down during the most
      * recent mouse event.
      */
    public boolean isShiftKeyDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }

    /** Returns <b>true</b> if the Control Key was held down during the most
      * recent mouse event.
      */
    public boolean isControlKeyDown() {
        return (modifiers & CONTROL_MASK) != 0;
    }

    /** Returns <b>true</b> if the Meta Key was held down during the most
      * recent mouse event.
      */
    public boolean isMetaKeyDown() {
        return (modifiers & META_MASK) != 0;
    }

    /** Returns <b>true</b> if the Alternate Key was held down during the most
      * recent mouse event.
      */
    public boolean isAltKeyDown() {
        return (modifiers & ALT_MASK) != 0;
    }

    void updateModifiers(MouseEvent currentEvent) {
        modifiers = 0;
        if (currentEvent == null) {
            return;
        }

//        x = currentEvent.x + bounds.x;
//        y = currentEvent.y + bounds.y;

        if (currentEvent.isShiftKeyDown()) {
            modifiers += SHIFT_MASK;
        }
        if (currentEvent.isControlKeyDown()) {
            modifiers += CONTROL_MASK;
        }
        if (currentEvent.isMetaKeyDown()) {
            modifiers += META_MASK;
        }
        if (currentEvent.isAltKeyDown()) {
            modifiers += ALT_MASK;
        }
    }

    /** @private */
    public void mouseDragged(MouseEvent event) {
        int x, y;
        DragDestination dest = null;
        View view;
        Point aPoint = new Point(event.x, event.y);

        if (dragView != null) {
            dragView.convertPointToView(null, aPoint, aPoint);
        }
        x = aPoint.x;
        y = aPoint.y;

        updateModifiers(event);
        view = rootView.viewForMouse(x, y);

        /* Cannot dragEntered() a view that is not in the
         * current modal session if any.
         */
        if( rootView.viewExcludedFromModalSession(view))
            view = null;

        if (view != null) {
            Point point = Point.newPoint();

            rootView.convertToView(view, x, y, point);
            dest = view.acceptsDrag(this, point.x, point.y);

            // Go up the view hierarchy looking for the first view
            // that can accept the drag.
            while (dest == null && view._superview != null) {
                point.x += view.bounds.x;
                point.y += view.bounds.y;
                view = view._superview;
                dest = view.acceptsDrag(this, point.x, point.y);
            }
            Point.returnPoint(point);
        }

        if (destination == null && dest != null) {
            destination = dest;
            destinationView = view;
            isAccepting = destination.dragEntered(this);
        } else if (destination != null && dest == null) {
            destination.dragExited(this);
            destination = null;
            destinationView = null;
        } else if (destination != dest) {
            destination.dragExited(this);
            destination = dest;
            destinationView = view;
            destination.dragEntered(this);
        } else if (destination != null) {
            isAccepting = destination.dragMoved(this);
        }
    }

    /** @private */
    public void mouseUp(MouseEvent event) {
        boolean         accepted = false, animateBack = false;

        /* the try-finally will force the DragView to get removed from the
         * screen even if user code throws an exception.  This is what you
         * want
         */
        try {
            updateModifiers(event);

            if (destination != null && isAccepting) {
                accepted = destination.dragDropped(this);
            }

            if (accepted) {
                source.dragWasAccepted(this);
            } else {
                animateBack = source.dragWasRejected(this);
            }
        } finally {
            isAccepting = false;

            if (dragView != null) {
                if (animateBack) {
                    dragView.startAnimatingRejectedDrag();
                } else {
                    dragView.stopDragging();
                }
            }
        }
    }

    /** Returns the View currently designated the DragSession's destination, or
      * <b>null</b> if there is no such View.
      */
    public View destinationView() {
        return destinationView;
    }

    /** Returns the bounds of the dragged Image, in the current destination
      * View's coordinate system.
      */
    public Rect destinationBounds() {
        if (destinationView == null) {
            return null;
        } else {
            Rect bounds = absoluteBounds();

            rootView.convertRectToView(destinationView, bounds, bounds);
            return bounds;
        }
    }

    /** Returns the bounds of the dragged Image, in the DragSession's
      * RootView's coordinate system.
      */
    public Rect absoluteBounds() {
        Rect rect = new Rect(0, 0, image.width(), image.height());

        if (dragView != null) {
            dragView.convertRectToView(null, rect, rect);
        }
        return rect;
    }

    /** Returns the mouse's location, in the DragSession's RootView's
      * coordinate system.
      */
    public Point absoluteMousePoint() {
        if (dragView != null) {
            Point point = new Point(dragView._lastX, dragView._lastY);

            dragView.superview().convertPointToView(null, point, point);
            return point;
        } else {
            return new Point(0, 0);
        }
    }

    /** Returns the mouse's location, in the current destination View's
      * coordinate system.
      */
    public Point destinationMousePoint() {
        if (destinationView == null) {
            return null;
        } else if (dragView != null) {
            Point point = new Point(dragView._lastX, dragView._lastY);

            dragView.superview().convertPointToView(destinationView, point, point);
            return point;
        } else {
            return new Point(0, 0);
        }
    }

    /** @private */
    public boolean destinationIsAccepting() {
        return isAccepting;
    }
}
