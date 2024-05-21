// MouseEvent.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Event subclass used for all mouse events.
  */

public class MouseEvent extends netscape.application.Event {
    /** The MouseEvent's X-coordinate, relative to the origin of the View in
      * which the event occurred.
      */
    public int                  x;

    /** The MouseEvent's Y-coordinate, relative to the origin of the View in
      * which the event occurred.
      */
    public int                  y;

    int                  clickCount, modifiers;

    /** Mouse "down" event. */
    public final static int     MOUSE_DOWN = -1;
    /** Mouse "dragged" event. */
    public final static int     MOUSE_DRAGGED = -2;
    /** Mouse "up" event. */
    public final static int     MOUSE_UP = -3;
    /** Mouse "entered" event. */
    public final static int     MOUSE_ENTERED = -4;
    /** Mouse "moved" event. */
    public final static int     MOUSE_MOVED = -5;
    /** Mouse "exited" event. */
    public final static int     MOUSE_EXITED = -6;


    /** Constructs a MouseEvent.
      */
    public MouseEvent() {
        super();
    }

    /** Constructs a MouseEvent of type <b>type</b>, with coordinates
      * (<b>x</b>, <b>y</b>), and the modifier bitmask <b>modifiers</b>,
      * representing the modifier keys the user held down during the
      * mouse event, such as the Shift key.
      */
    public MouseEvent(long timeStamp, int type, int x, int y, int modifiers) {
        this();

        this.timeStamp = timeStamp;

        if (type < MOUSE_EXITED || type > MOUSE_DOWN) {
            throw new IllegalArgumentException("Invalid MouseEvent type: " +
                                               type);
        }

        this.type = type;
        this.x = x;
        this.y = y;
        this.modifiers = modifiers;
    }

    /** Sets the MouseEvent's "click" count.  An event type of type MOUSE_DOWN
      * and click count of 2 represents a double-click, for example.
      */
    public void setClickCount(int count) {
        clickCount = count;
    }

    /** Returns the MouseEvent's click count.
      * @see #setClickCount
      */
    public int clickCount() {
        return clickCount;
    }

    /** Sets the MouseEvent's modifiers, the bitmask describing the keys the
      * user held down when the mouse event occurred. The keyboard modifiers
      * are described in KeyEvent.
      * @see KeyEvent
      */
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    /** Returns the MouseEvent's modifier bitmask.
      * @see #setModifiers
      */
    public int modifiers() {
        return modifiers;
    }

    /** Returns <b>true</b> if the user held down the Shift key during the
      * mouse event.
      */
    public boolean isShiftKeyDown() {
        return (modifiers & KeyEvent.SHIFT_MASK) != 0;
    }

    /** Returns <b>true</b> if the user held down the Control key during the
      * mouse event.
      */
    public boolean isControlKeyDown() {
        return (modifiers & KeyEvent.CONTROL_MASK) != 0;
    }

    /** Returns <b>true</b> if the user held down the Meta key during the
      * mouse event.
      */
    public boolean isMetaKeyDown() {
        return (modifiers & KeyEvent.META_MASK) != 0;
    }

    /** Returns <b>true</b> if the user held down the Alternate key during the
      * mouse event.
      */
    public boolean isAltKeyDown() {
        return (modifiers & KeyEvent.ALT_MASK) != 0;
    }

    /** Sets the RootView in which  the MouseEvent occurred. */
    public void setRootView(RootView rootView) {
        processor = rootView;
    }

    /** Returns the RootView in which the MouseEvent occurred.
      * @see #setRootView
      */
    public RootView rootView() {
        return (RootView)processor;
    }

    /** Returns the MouseEvent's String representation. */
    public String toString() {
        String typeString;

        switch (type) {
            case MOUSE_DOWN:
                typeString = "Down";
                break;
            case MOUSE_DRAGGED:
                typeString = "Dragged";
                break;
            case MOUSE_UP:
                typeString = "Up";
                break;
            case MOUSE_ENTERED:
                typeString = "Entered";
                break;
            case MOUSE_MOVED:
                typeString = "Moved";
                break;
            case MOUSE_EXITED:
                typeString = "Exited";
                break;
            default:
                typeString = "Unknown Type";
                break;
        }

        return "MouseEvent: " + typeString +
                      " at: (" + x + "," + y + ")" +
               " modifiers: " + modifiers +
                  " clicks: " + clickCount;
    }
}
