// DragDestination.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/** Interface for objects interested in receiving dragged objects.
  * A DragSession, an object representing the object currently being dragged
  * and managing the modal drag session itself, calls these methods at various
  * times during a drag session.  The current DragDestination is the object
  * returned by the <b>acceptsDrag()</b> method of the View currently under the
  * mouse.
  * @see View#acceptsDrag
  * @see DragSession
  */
public interface DragDestination {
    /** Called when the user drags an object into a View.  The receiver should
      * return <b>true</b> if it would accept the object if the user released
      * it at its current location.
      */
    public boolean dragEntered(DragSession session);

    /** Called when the user drags an object within a View.  The receiver
      * should return <b>true</b> if it would accept the object if the user
      * released it at its current location.
      */
    public boolean dragMoved(DragSession session);

    /** Called when the user drags an object out of a View.
      */
    public void dragExited(DragSession session);

    /** Called when the user drops an object on a View.
      */
    public boolean dragDropped(DragSession session);
}
