// EventProcessor.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Interface for objects interested in processing Events as they're removed
  * from an EventLoop.  Each Event has a processor, the object which
  * understands the action that should be taken based on the Event and its
  * data. For example, each MouseEvent specifies a RootView as its processor.
  * As the EventLoop removes each MouseEvent, it calls the RootView's
  * <b>processEvent()</b> method which forwards the MouseEvent to the
  * appropriate View.
  * @see Event#setProcessor
  * @see EventLoop
  */


public interface EventProcessor {

    /** Called by EventLoop on an Event's processor.  Callee should take
      * the appropriate action to "process" the Event.
      */
    public void processEvent(Event event);
}
