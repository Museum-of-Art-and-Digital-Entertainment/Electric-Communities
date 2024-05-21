// ResizeFilter.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;

class ResizeFilter implements EventFilter {
    public ApplicationEvent lastEvent;

    public Object filterEvents(Vector events) {
        int count = events.count(), i;

        for (i = 0; i < count; i++) {
            Event event = (Event)events.elementAt(i);

            if ((event instanceof ApplicationEvent) &&
                (event.type == ApplicationEvent.RESIZE) &&
                (event.processor == lastEvent.processor)) {
                lastEvent = (ApplicationEvent)event;

                events.removeElementAt(i);
                i--;
                count--;
            }
        }
        return null;
    }
}
