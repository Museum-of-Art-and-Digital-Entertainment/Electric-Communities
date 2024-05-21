// ApplicationEvent.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;
import java.awt.Graphics;
import java.awt.Rectangle;

/** @note 1.0 update events with null clip rects will draw alot
  * @note 1.0 added PRINT event
  * @note 1.0 converted data to object from Rect, added convience methods
  */
class ApplicationEvent extends Event {
    /* application events */
    static final int GOT_FOCUS = -21,
                  LOST_FOCUS = -22,
                  UPDATE = -23,
                  RESIZE = -24,
                  STOP = -25,
                  APPLET_STOPPED = -26,
                  APPLET_STARTED = -27,
                  PRINT = -28;
    Object data;

    static ApplicationEvent newResizeEvent(int width, int height) {
        ApplicationEvent event = new ApplicationEvent();

        event.type = RESIZE;
        event.data = new Rect(0, 0, width, height);
        return event;
    }

    static ApplicationEvent newUpdateEvent(java.awt.Graphics g) {
        ApplicationEvent event = new ApplicationEvent();
        Rectangle r = g.getClipRect();

        event.type = UPDATE;
        if (r == null) {
            event.data = new Rect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            event.data = new Rect(r.x, r.y, r.width, r.height);
        }
        return event;
    }

    static ApplicationEvent newFocusEvent(boolean gotFocus) {
        ApplicationEvent event = new ApplicationEvent();

        event.type = gotFocus ? GOT_FOCUS : LOST_FOCUS;
        return event;
    }

    static ApplicationEvent newPrintEvent(java.awt.Graphics g) {
        ApplicationEvent event = new ApplicationEvent();

        event.type = PRINT;
        event.data = g;
        return event;
    }

    public String toString() {
        String typeString;

        switch (type) {
        case GOT_FOCUS:
            typeString = "GotFocus";
            break;
        case LOST_FOCUS:
            typeString = "LostFocus";
            break;
        case UPDATE:
            typeString = "Update";
            break;
        case RESIZE:
            typeString = "Resize";
            break;
        case STOP:
            typeString = "Stop";
            break;
        case APPLET_STOPPED:
            typeString = "AppletStopped";
            break;
        case APPLET_STARTED:
            typeString = "AppletStarted";
            break;
        case PRINT:
            typeString = "Print";
            break;
        default:
            typeString = "Unknown Type";
            break;
        }

        return "ApplicationEvent: " + typeString;
    }

    Rect rect() {
        return (Rect)data;
    }

    java.awt.Graphics graphics() {
        return (java.awt.Graphics)data;
    }
}


