
package ec.ifc.app;

import netscape.application.Event;

public class ECEvent extends Event {

    public ECEvent (int type) {
        super();
        setType(type);
    }

    public int getType () {
        return type();
    }
}

