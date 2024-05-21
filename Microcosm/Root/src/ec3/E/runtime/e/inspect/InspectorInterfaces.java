package ec.e.inspect;

/** The interface InspectorUI defines all the operations that an
  * inspector user interface must implement. We currently define an
  * IFC-based UI and expect currently only to create a text-based UI
  * beyond that.  */

public interface InspectorUI {
    public void inspectObject(Object iobj, String name);
    public void refreshRunqueueDisplay(Object runlet);
    public void refreshHoldState(int holdState);
    public void invalidateRunlet(Object runlet);
    public void profileRunqueue(String objName, long timeInQueue, int queueLength);
    public void profileExecution(String objName, long timeToExecute);
}

/**
 * The interface Inspectable defines what Objects must implement to 
 * take over (semantic) inspectability. This is currently not used.
 */

// public interface Inspectable {
//     public Inspector createInspector();
// }

