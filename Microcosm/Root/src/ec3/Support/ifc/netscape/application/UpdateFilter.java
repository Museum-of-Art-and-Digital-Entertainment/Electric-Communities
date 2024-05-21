// UpdateFilter.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;

class UpdateFilter implements EventFilter {
    public Rect _rect;
    RootView rootView;

    UpdateFilter(Rect rect) {
        super();
        _rect = rect;
    }

    public Object filterEvents(Vector events) {
        int count = events.count();

        while (count-- > 0) {
            Event event = (Event)events.elementAt(count);

            if ((event instanceof ApplicationEvent) &&
                (event.type == ApplicationEvent.UPDATE) &&
                (event.processor() == rootView)) {
                ApplicationEvent updateEvent = (ApplicationEvent)event;
                Rect newRect = updateEvent.rect();

                _rect.unionWith(newRect);
                events.removeElementAt(count);
            }
        }
        return null;
    }
}

