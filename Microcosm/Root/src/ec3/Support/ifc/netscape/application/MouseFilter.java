// MouseFilter.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;

class MouseFilter implements EventFilter {
    public Object filterEvents(Vector events) {
        int count = events.count(), i;
        MouseEvent mouseEvent = null;

        for (i = 0; i < count; i++) {
            Event event = (Event)events.elementAt(i);

            if (event instanceof MouseEvent) {
                int type = event.type();

                if (type == MouseEvent.MOUSE_DRAGGED ||
                    type == MouseEvent.MOUSE_MOVED) {
                    mouseEvent = (MouseEvent)event;
                    events.removeElementAt(i);
                    count--;
                    i--;
                }
            } else if (!(event.processor() instanceof Timer)) {
                break;
            }
        }
        return mouseEvent;
    }
}
