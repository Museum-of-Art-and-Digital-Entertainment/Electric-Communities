package netscape.application;
import netscape.util.*;

class ApplicationEventFilter implements EventFilter {
    RootView rootView;

    ApplicationEventFilter(RootView rootView) {
        this.rootView = rootView;
    }

    public Object filterEvents(Vector events) {
        int count = events.count();

        while (count-- > 0) {
            Event event = (Event)events.elementAt(count);

            if (event instanceof ApplicationEvent) {
                ApplicationEvent appEvent = (ApplicationEvent)event;

                if (rootView == appEvent.processor) {
//                    System.out.println("Removing event after dispose");
                    events.removeElementAt(count);
                }
            }
        }
        return null;
    }
}
