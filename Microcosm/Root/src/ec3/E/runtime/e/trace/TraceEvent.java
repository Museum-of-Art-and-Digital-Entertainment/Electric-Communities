/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import ec.ifc.app.ECEvent;

/**
 * We use an IFC event to switch to the IFC thread.  IFC 
 * breaks if IFC methods are invoked outside that thread.
 * Life is good.
 * <p>
 * This is in support of TraceDisplay, which will probably
 * never be used.
 */

public class TraceEvent extends ECEvent {
    private TraceMessage message;
    
    // The two types of messages.
    static public final int BEGIN = 0x021960;
    static public final int MESSAGE = 0x022395;

    TraceEvent(int type, TraceMessage message) {
        super(type);
        this.message = message;
    }

    TraceEvent(int type) {
        super(type);
        this.message = null;
    }

    TraceMessage getMessage() { return message; }
}




