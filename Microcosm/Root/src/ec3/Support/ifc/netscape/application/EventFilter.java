// EventFilter.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Interface implemented by objects interested in filtering Events.
  * An EventFilter's <b>filterEvents()</b> method should examine the
  * contents of the events Vector and modify it as appropriate (add Events,
  * remove Events, reorder Events, etc.).
  */
public interface EventFilter {
    /** Called to invoke Event filtering on the collection of Events contained
      * in <b>events</b>.  This method can add, remove, reorder the Vector's
      * Events.  The return value will be the return value from EventLoop's
      * <b>filterEvents()</b> method.
      * The implementation of this method should be as simple as possible
      * since the event queue is locked during its execution. This method
      * method should not call some IFC widgets methods since a deadlock
      * can happen in some situations.
      * @see EventLoop#filterEvents
      */
    public Object filterEvents(Vector events);
}
