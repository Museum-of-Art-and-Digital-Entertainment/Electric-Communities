// DrawingSequenceOwner.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;


/** Interface implemented by objects interested in learning when a
  * DrawingSequence's current frame number has changed and must be redrawn, or
  * when a DrawingSequence has reached its final frame.
  * @see DrawingSequence
  */

public interface DrawingSequenceOwner {
    /** Sent by a DrawingSequence to its owner when its current
      * frame number changes.
      */
    public void drawingSequenceFrameChanged(DrawingSequence aSequence);

    /** Sent by a DrawingSequence to its owner after reaching its
      * final frame.
      */
    public void drawingSequenceCompleted(DrawingSequence aSequence);
}

