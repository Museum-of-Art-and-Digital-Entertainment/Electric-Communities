// DragView.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Window subclass that implements "drag and drop" within the IFC library.
  */


class DragView extends InternalWindow {
    int         animationCount, animationDeltaX, animationDeltaY;
    boolean     _animatingBack, _wasAccepted;
    Timer       timer;
    DragSession session;

    /* constructors */

    /** Instantiates a DragView with origin (0, 0) and zero width and height.
      * This constructor is only useful when decoding.
      */
    public DragView() {
        super();
    }

     /** Constructs the DragView to have bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      * (<b>mouseDownX</b>, <b>mouseDownY</b>) is the location of the mouse
      * click that initiated the drag-and-drop session.  <B>dragSource</b> is
      * the View that initiated the drag-and-drop session.  If not
      * <b>null</b>, this object will receive a <b>dragComplete()</b> message
      * once the drag-and-drop session has finished, and if <B>dragImage</b>
      * is <b>null</b>, it will receive multiple
      * <b>drawContentsOfDragView()</b>,
      * asking the View to draw the DragView's contents.  <B>dragItem</b> is
      * the data being dragged in the drag-and-drop session, and if
      * <b>animateBack</b> is <b>true</b>, the DragView will animate back
      * to its origin at the end of the drag session if the DragView's
      * <B>dragItem</b> is not accepted.  <b>isTransparent</b> indicates
      * whether the item being dragged should be placed within a
      * transparent, rather than opaque, window.  All coordinates are
      * expected to be absolute (that of the dragSource's rootView).
      */
    public DragView(DragSession session) {
        super();

        setBounds(session.initialX, session.initialY,
                  session.image.width(), session.image.height());

        this.session = session;
        _lastX = session.mouseDownX;
        _lastY = session.mouseDownY;

        setType(BLANK_TYPE);
        setTransparent(session.image.isTransparent());
        contentView().setTransparent(true);
        setLayer(DRAG_LAYER);
        _dragStart = true;
        setRootView(session.rootView);
        show();
        rootView().setMouseView(this);

        draw();
    }

    /** DragViews receive the command "animateRejectedDrag" from a Timer while
      * animating the slide back resulting from a rejected drag.
      */
    public void performCommand(String command, Object data) {
        if ("animateRejectedDrag".equals(command))
            animateRejectedDrag();
        else
            super.performCommand(command, data);
    }

    private void animateRejectedDrag() {
        int animX, animY;
        MouseEvent dragEvent;

        // End the rejected drag animation when the count reaches 0.

        if (animationCount <= 0) {
            timer.stop();
            timer = null;
            _animatingBack = false;
            animationCount = 0;
            animationDeltaX = 0;
            animationDeltaY = 0;
            stopDragging();

            return;
        }

        // Compute the next location and send in a drag event.

        animationCount--;

        // The drag event needs to be in the relative coord system
        animX = _lastX - (animationDeltaX + bounds.x);
        animY = _lastY - (animationDeltaY + bounds.y);
        dragEvent = new MouseEvent(0, MouseEvent.MOUSE_DRAGGED, animX, animY,
                                   0);
        mouseDragged(dragEvent);
    }

    void startAnimatingRejectedDrag() {
        float   deltaX, deltaY;
        int     increment, count;

        _animatingBack = true;

        if (_lastX - session.mouseDownX != 0 &&
            _lastY - session.mouseDownY != 0) {
            deltaX = _lastX - session.mouseDownX;
            deltaY = _lastY - session.mouseDownY;

            if (deltaX > deltaY) {
                increment = 1 + (int)(Math.abs(deltaY) / 5);
                count = (int)(Math.abs(deltaY) / increment);

                deltaX /= Math.abs(deltaY);

                if (deltaY < 0) {
                    deltaY = -1.0F;
                } else {
                    deltaY = 1.0F;
                }
            } else if (deltaY > deltaX) {
                increment = 1 + (int)(Math.abs(deltaX) / 5);
                count = (int)(Math.abs(deltaX) / increment);

                deltaY /= Math.abs(deltaX);

                if (deltaX < 0) {
                    deltaX = -1.0F;
                } else {
                    deltaX = 1.0F;
                }
            } else {
                count = 0;
                increment = 0;
            }

            if (count > 0) {
                animationCount = count;
                animationDeltaX = (int)(increment * deltaX);
                animationDeltaY = (int)(increment * deltaY);

                timer = new Timer(this, "animateRejectedDrag", 25);
                timer.start();
            } else {
                _animatingBack = false;
                stopDragging();
            }
        } else {
            _animatingBack = false;
            stopDragging();
        }
    }

    /** Overridden to return <b>false</b>.
      * @see InternalWindow#canBecomeMain
      */
    public boolean canBecomeMain() {
        return false;
    }

    /** Overridden to return <b>false</b>.
      * @see InternalWindow#canBecomeDocument
      */
    public boolean canBecomeDocument() {
        return false;
    }

    /** Moves the DragView as the user moves the mouse.  Sends
      * <B>dragViewEntered()</b>, <b>dragViewMoved</b>, and
      * <B>dragViewExited</b> messages to
      * interested Views as the mouse passes over them.
      */
    public void mouseDragged(MouseEvent event) {
        super.mouseDragged(event);

        if (!_animatingBack) {
            session.mouseDragged(event);
        }
    }

    void stopDragging() {
        /* if we're already offscreen, we're done */
        if (!isVisible()) {
            return;
        }

        hide();

        setBuffered(false);

        rootView.redraw(bounds);
    }

    /** Ends the drag session by notifying the appropriate View that the
      * DragView has been released over it.
      */
    public void mouseUp(MouseEvent event) {
        super.mouseUp(event);
        session.mouseUp(event);
    }

    /** Draws the DragView's contents using its <B>dragImage</b>, or, if
      * <B>null</b>, by asking its <B>dragSource</b> to do so.
      */
    public void drawView(Graphics g) {
        session.image.drawAt(g, 0, 0);
    }

    public View viewForMouse(int x, int y) {
        return null;
    }
}
