// ScrollBarOwner.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/** Interface implemented by objects interested in changes to a ScrollBar's
  * state.
  */

public interface ScrollBarOwner {
   /** Called when the ScrollBar becomes active.
     * @see ScrollBar#setActive
     */
    public void scrollBarDidBecomeActive(ScrollBar aScrollBar);

   /** Called when the ScrollBar becomes inactive.
     * @see ScrollBar#setActive
     */
    public void scrollBarDidBecomeInactive(ScrollBar aScrollBar);

   /** Called when the ScrollBar becomes enabled.
     * @see ScrollBar#setEnabled
     */
    public void scrollBarWasEnabled(ScrollBar aScrollBar);

   /** Called when the ScrollBar becomes disabled.
     * @see ScrollBar#setEnabled
     */
    public void scrollBarWasDisabled(ScrollBar aScrollBar);
}
