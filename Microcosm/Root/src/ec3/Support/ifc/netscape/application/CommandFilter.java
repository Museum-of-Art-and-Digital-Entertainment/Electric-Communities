// CommandFilter.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;

class CommandFilter implements EventFilter {
    Target      target;
    String      command;
    Object      object;

    CommandFilter(Target target, String command, Object object) {
        super();
        this.target = target;
        this.command = command;
        this.object = object;
    }

    final static boolean stringEquals(String string1, String string2) {
        if (string1 != null) {
            return string1.equals(string2);
        } else {
            return string2 == null;
        }
    }

    public Object filterEvents(Vector events) {
        int count = events.count();

        while (count-- > 0) {
            Object event = events.elementAt(count);

            if (event instanceof CommandEvent) {
                CommandEvent commandEvent = (CommandEvent)event;

                if (target == commandEvent.target &&
                    stringEquals(command, commandEvent.command)) {
                    events.removeElementAt(count);
                }
            }
        }
        return null;
    }
}
