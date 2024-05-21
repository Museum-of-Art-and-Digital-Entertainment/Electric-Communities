// KeyEvent.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.awt.Event;

/** Event subclass used for all key up and key down events.
  * @note 1.0 added additional special keys for keyboard UI
  */

public class KeyEvent extends netscape.application.Event {
    /** The key that was pressed or released. */
    public int                  key;

    /** The modifier keys that the user held down when the KeyEvent was
      * generated.
      */
    public int                  modifiers;

    /** Key "down" event. */
    public final static int     KEY_DOWN = -11;
    /** Key "up" event. */
    public final static int     KEY_UP = -12;

    /** Modifiers **/

    /** No modifiers
      *
      */
    public static final int     NO_MODIFIERS_MASK = 0;

    /** The Alternate key modifier bitmask. */
    public static final int     ALT_MASK = java.awt.Event.ALT_MASK;

    /** The Control key modifier bitmask. */
    public static final int     CONTROL_MASK = java.awt.Event.CTRL_MASK;

    /** The Shift key modifier bitmask. */
    public static final int     SHIFT_MASK = java.awt.Event.SHIFT_MASK;

    /** The Meta key modifier bitmask. */
    public static final int     META_MASK = java.awt.Event.META_MASK;

    /** Special keys **/

    /** Return key **/
    public final static int            RETURN_KEY = 10;

    /** Backspace key
      *
      */
    public final static int            BACKSPACE_KEY = 8;

    /** Delete key
      *
      */
    public final static int            DELETE_KEY = 127;

    /** Escape key
      *
      */
    public final static int            ESCAPE_KEY = 27;

    /** Tab key
      *
      */
    public final static int            TAB_KEY = 9;

    /** Up arrow key
      *
      */
    public final static int            UP_ARROW_KEY = java.awt.Event.UP;

    /** Down arrow key
      *
      */
    public final static int            DOWN_ARROW_KEY = java.awt.Event.DOWN;

    /** Left arrow key
      *
      */
    public final static int            LEFT_ARROW_KEY = java.awt.Event.LEFT;

    /** Right arrow key
      *
      */
    public final static int            RIGHT_ARROW_KEY = java.awt.Event.RIGHT;

    /** Home key
      *
      */
    public final static int            HOME_KEY = java.awt.Event.HOME;

    /** End key
      *
      */
    public final static int            END_KEY = java.awt.Event.END;

    /** Page up key
      *
      */
    public final static int            PAGE_UP_KEY = java.awt.Event.PGUP;

    /** Page down key
      *
      */
    public final static int            PAGE_DOWN_KEY = java.awt.Event.PGDN;

    /** F1 Key
      *
      */
    public final static int            F1_KEY = java.awt.Event.F1;

    /** F2 Key
      *
      */
    public final static int            F2_KEY = java.awt.Event.F2;

    /** F3 Key
      *
      */
    public final static int            F3_KEY = java.awt.Event.F3;

    /** F4 Key
      *
      */
    public final static int            F4_KEY = java.awt.Event.F4;

    /** F5 Key
      *
      */
    public final static int            F5_KEY = java.awt.Event.F5;

    /** F6 Key
      *
      */
    public final static int            F6_KEY = java.awt.Event.F6;

    /** F7 Key
      *
      */
    public final static int            F7_KEY = java.awt.Event.F7;

    /** F8 Key
      *
      */
    public final static int            F8_KEY = java.awt.Event.F8;

    /** F9 Key
      *
      */
    public final static int            F9_KEY = java.awt.Event.F9;

    /** F10 Key
      *
      */
    public final static int            F10_KEY = java.awt.Event.F10;

    /** F11 Key
      *
      */
    public final static int            F11_KEY = java.awt.Event.F11;

    /** F12 Key
      *
      */
    public final static int            F12_KEY = java.awt.Event.F12;


    /** Constructs a KeyEvent.
      */
    public KeyEvent() {
        super();
    }

     /** Constructs a KeyEvent to hold key press information for the
       * specified key.  <b>modifiers</b> is the bitmask representing the
       * modifier keys held down during the key press.  <b>down</b> specifies
       * whether the event represents a key up or key down event.
       */
    public KeyEvent(long timeStamp, int key, int modifiers, boolean down) {
        this();

        this.timeStamp = timeStamp;
        if (down) {
            type = KEY_DOWN;
        } else {
            type = KEY_UP;
        }
        this.key = key;
        this.modifiers = modifiers;
    }

    /** Returns <b>true</b> if the Shift key was held down during the key
      * event.
      */
    public boolean isShiftKeyDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }

    /** Returns <b>true</b> if the Control key was held down during the key
      * event.
      */
    public boolean isControlKeyDown() {
        return (modifiers & CONTROL_MASK) != 0;
    }

    /** Returns <b>true</b> if the Meta key was held down during the key event.
      */
    public boolean isMetaKeyDown() {
        return (modifiers & META_MASK) != 0;
    }

    /** Returns <b>true</b> if the Alt key was held down during the key event.
      */
    public boolean isAltKeyDown() {
        return (modifiers & ALT_MASK) != 0;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Return key.
      */
    public boolean isReturnKey() {
        return key == RETURN_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Backspace key.
      */
    public boolean isBackspaceKey() {
        return key == BACKSPACE_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Delete key.  In
      * general, the key used to delete characters is the "Backspace" key in
      * the upper-right corner of the keyboard (the Mac calls it "Delete").
      * @see #isBackspaceKey
      */
    public boolean isDeleteKey() {
        return key == DELETE_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Escape key.
      */
    public boolean isEscapeKey() {
        return key == ESCAPE_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Tab key.
      */
    public boolean isTabKey() {
        return (key == TAB_KEY && !isShiftKeyDown());
    }

    /** Returns <b>true</b> if the KeyEvent represents the BackTab
      * (Shift + Tab) key.
      */
    public boolean isBackTabKey() {
        return (key == TAB_KEY && isShiftKeyDown());
    }

    /** Returns <b>true</b> if the KeyEvent represents the Up Arrow key.
      */
    public boolean isUpArrowKey() {
        return key == UP_ARROW_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Down Arrow key.
      */
    public boolean isDownArrowKey() {
        return key == DOWN_ARROW_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Left Arrow key.
      */
    public boolean isLeftArrowKey() {
        return key == LEFT_ARROW_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Right Arrow key.
      */
    public boolean isRightArrowKey() {
        return key == RIGHT_ARROW_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Arrow key.
      */
    public boolean isArrowKey() {
        return (key == UP_ARROW_KEY) || (key == DOWN_ARROW_KEY) ||
               (key == LEFT_ARROW_KEY) || (key == RIGHT_ARROW_KEY);
    }

    /** Returns <b>true</b> if the KeyEvent represents the Home key.
      */
    public boolean isHomeKey() {
        return key == HOME_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the End key.
      */
    public boolean isEndKey() {
        return key == END_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Page Up key.
      */
    public boolean isPageUpKey() {
        return key == PAGE_UP_KEY;
    }

    /** Returns <b>true</b> if the KeyEvent represents the Page Down key.
      */
    public boolean isPageDownKey() {
        return key == PAGE_DOWN_KEY;
    }

    /** Returns the function key number or <b>0</b> if the KeyEvent does not
      * represent a function key.
     */
    public int isFunctionKey() {
        if (key == F1_KEY) {
            return 1;
        } else if (key == F2_KEY) {
            return 2;
        } else if (key == F3_KEY) {
            return 3;
        } else if (key == F4_KEY) {
            return 4;
        } else if (key == F5_KEY) {
            return 5;
        } else if (key == F6_KEY) {
            return 6;
        } else if (key == F7_KEY) {
            return 7;
        } else if (key == F8_KEY) {
            return 8;
        } else if (key == F9_KEY) {
            return 9;
        } else if (key == F10_KEY) {
            return 10;
        } else if (key == F11_KEY) {
            return 11;
        } else if (key == F12_KEY) {
            return 12;
        }

        return 0;
    }

    /** Returns <b>true</b> if the KeyEvent represents a printable ASCII
      * character.
      */
    public boolean isPrintableKey() {
        return !((key < ' ') || isArrowKey() || isHomeKey() ||
                 isEndKey() || (isFunctionKey() != 0));
    }

    /** Sets the RootView associated with the KeyEvent. */
    public void setRootView(RootView rootView) {
        processor = rootView;
    }

    /** Returns the RootView associated with the KeyEvent.
      * @see #setRootView
      */
    public RootView rootView() {
        return (RootView)processor;
    }

    /** Returns the KeyEvent's String representation.
      */
    public String toString() {
        String          typeString;

        if (type == KEY_DOWN) {
            typeString = "KeyDown";
        } else {
            typeString = "KeyUp";
        }

        if (key < ' ') {
            return typeString + ":\'\' (0x" +
                   Integer.toString(key, 16) + ")\':" + modifiers;
        }

        return typeString + ":\'" + (char)key + "\' (0x" +
               Integer.toString(key, 16) + ")\':" + modifiers;
    }
}
