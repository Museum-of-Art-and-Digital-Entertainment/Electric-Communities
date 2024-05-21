// TextFilter.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Interface implemented by objects interested in filtering key events
  * receiver by "text objects" (TextField and TextView).  A text object calls
  * its filter's <b>acceptsEvent()</b> method upon receiving a key event but
  * before processing it.  A TextFilter can leave the event unfiltered, or
  * replace it with whatever key event sequences it deems appropriate.
  * @see TextField#setFilter
  * @see TextView#setFilter
  */

public interface TextFilter {

    /** Called by <b>textObject</b> upon receiving a KeyEvent.  <b>event</b> is
      * the KeyEvent and <b>events</b> is the text object's key event Vector,
      * the Vector it uses to collect KeyEvents.  Returning <b>true</b>
      * instructs the text object to place the KeyEvent in the event vector
      * (no filtering) and process it.  A TextFilter that wants to filter the
      * key event should return <b>false</b> and add the replacement
      * KeyEvent(s) to <b>events</b>.  Access to the text object's event vector
      * allows a TextFilter to turn a single KeyEvent into several.  For
      * example, a TextFilter can turn the "t" key into "the" by creating
      * events for "h" and "e," adding all three events to <b>events</b> and
      * returning <b>false</b>.
      * The implementation of this method should be as simple as possible
      * since the event queue is locked during its execution. This method
      * should not call some IFC widget methods since a deadlock
      * can happen in some situations.
      */
    public boolean acceptsEvent(Object textObject, KeyEvent event,
                                Vector events);
}

