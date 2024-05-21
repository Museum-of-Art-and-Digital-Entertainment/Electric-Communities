package ec.ui;

import ec.e.inspect.*;
import ec.ifc.app.ECEvent;

/**

 * We use an IFC event to call up the Inspector to inspect an objet
 * with a given name since the IFC based Inspector must run in the IFC
 * thread and the only way to accomplish this is to send it an IFC
 * event.

 */

public class InspectorEvent extends ECEvent {
    
    static final int RUNLET_CONTROL_DOUBLE_CLICK_EVENT = 0;
    static final int RUNLET_SHIFT_DOUBLE_CLICK_EVENT = 1;
    static final int RUNLET_DOUBLE_CLICK_EVENT = 2;
    static final int RUNLET_CONTROL_SINGLE_CLICK_EVENT = 3;
    static final int RUNLET_SHIFT_SINGLE_CLICK_EVENT = 4;
    static final int RUNLET_SINGLE_CLICK_EVENT = 5;
    static final int RUNLET_RELEASE_EVENT = 6;

    private Object myObject;
    private String myName;
    private long   myTimeInQueue;
    private int    myQueueLength;

    InspectorEvent(Object object, String name) {
        this(Inspector.INSPECT, object, name, -1, 0);
    }

    InspectorEvent(int eventType, Object object) {
        this(eventType, object, null, -1, 0);
    }

    InspectorEvent(int eventType, String name) {
        this(eventType, null, name, -1, 0);
    }

    InspectorEvent(int eventType, Object object, String name) {
        this(eventType, object, name, -1, 0);
    }

    InspectorEvent(int eventType, String name, long executionTime) {
        this(eventType, null, name, executionTime, 0);
    }

    InspectorEvent(int eventType, String name, long executionTime, int queueLength) {
        this(eventType, null, name, executionTime, queueLength);
    }

    InspectorEvent(int eventType, Object object, String name, long executionTime) {
        this(eventType, object, name, executionTime, 0);
    }

    InspectorEvent(int eventType, Object object, String name, long time, int queueLength) {
        super(eventType);
        myName = name;
        myObject = object;
        myTimeInQueue = time;
        myQueueLength = queueLength;
    }

    String getName() { return myName; }
    Object getObject() { return myObject; }
    long   getTimeInQueue() { return myTimeInQueue; }
    int    getQueueLength() { return myQueueLength; }
}



