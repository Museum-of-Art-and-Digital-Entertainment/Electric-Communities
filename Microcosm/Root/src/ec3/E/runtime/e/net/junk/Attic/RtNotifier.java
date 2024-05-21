package ec.e.net;

import java.util.Enumeration;
import java.util.Vector;

public interface ENotificationHandler {
    void handleNotification(String type, Object arg, Object info);
}

public class RtNotifier extends Vector {
    public RtNotifier() {
        super(1);
    }

    public void notify(String type, Object arg, Object info) {
        Enumeration en = elements();
        ENotificationHandler h;
        Object o;

        while (en.hasMoreElements()) {
            o = en.nextElement();
            if (o instanceof ENotificationHandler) {
                h = (ENotificationHandler)o;
                h.handleNotification(type, arg, info);
            }
        }
    }
}
