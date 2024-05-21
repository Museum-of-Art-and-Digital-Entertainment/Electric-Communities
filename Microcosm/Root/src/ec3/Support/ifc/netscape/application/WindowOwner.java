// WindowOwner.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/** Interface implemented by objects wanting information on important Window
  * events, such as the Window closing.  An object implementing this interface
  * must make itself the Window's "owner" using the Window's <b>setOwner()</b>
  * method.
  * @see Window#setOwner
  */

public interface WindowOwner {
    /** Sent just before a <b>aWindow</b> becomes visible. Returning
      * <b>false</b> prevents the Window from appearing onscreen.
      */
    public boolean windowWillShow(Window aWindow);

    /** Sent just after <b>aWindow</b> has become visible.
      */
    public void windowDidShow(Window aWindow);

    /** Sent just before <b>aWindow</b> is hidden. Returning <b>false</b>
      * prevents the Window from hiding.
      */
    public boolean windowWillHide(Window aWindow);

    /** Sent just after <b>aWindow</b> has hidden.
      */
    public void windowDidHide(Window aWindow);

    /** Sent just after <b>aWindow</b> becomes the "main" Window, the Window
      * that receives keyboard events.
      */
    public void windowDidBecomeMain(Window aWindow);

    /** Sent just after <b>aWindow</b> ceases being the "main" Window.
      */
    public void windowDidResignMain(Window aWindow);

    /** Sent just before <b>aWindow</b> changes size by <b>deltaSize</b>.
      * The Window owner can affect this resize by modifying <b>deltaSize</b>.
      */
    public void windowWillSizeBy(Window aWindow, Size deltaSize);
}
