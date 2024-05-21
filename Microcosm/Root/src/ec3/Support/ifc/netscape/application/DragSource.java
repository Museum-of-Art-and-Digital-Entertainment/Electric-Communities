// DragSource.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/** Interface implemented by objects interested in initiating drag sessions.
  * These methods provide context for the DragSession managing the drag
  * session, and notification of a drag session's results.
  * @see DragSession
  */
public interface DragSource {
    /** Returns the View from which the drag session originated. This View
      * determines the context for the coordinates supplied to the
      * DragSession.
      * @see DragSession
      */
    public View sourceView(DragSession session);

    /** Called when the user releases a DragSession and the receiving
      * DragDestination accepts the dragged item.
      */
    public void dragWasAccepted(DragSession session);

    /** Called when the user releases a DragSession without it being accepted
      * by a DragDestination.  This method should return <b>true</b> if the
      * DragSession's image should animate back to the drag session's origin.
      */
    public boolean dragWasRejected(DragSession session);
}

