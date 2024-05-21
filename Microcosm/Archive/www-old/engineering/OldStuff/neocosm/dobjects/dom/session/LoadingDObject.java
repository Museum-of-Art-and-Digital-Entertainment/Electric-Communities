/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/LoadingDObject.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;
import dom.util.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Represents a DObject on a local View while the class for the
 * real DObject is being loaded (can be an arbitrarily long operation).
 * This class is declared final so that no subclasses can be created, and
 * the constructor is declared protected so that only the View class (within
 * this package) can create instances of this object.  It is simply used
 * as a 'standin' for the putative DObject while the DObject's class(es) 
 * are being loaded...yes, this works!
 *
 * @author Scott Lewis
 */
final class LoadingDObject extends DObject
{
    SessionViewID myCreatingViewID;
    CreateDObjectData myData;
    View myView;

    public static final String CREATINGDONEMETHODNAME = "createClientDone";
    
    Thread myThread;
    
    protected LoadingDObject(View aView, ViewDObjectFacet viewFacet, SessionViewID viewID,CreateDObjectData data) 
        throws InstantiationException
    {
        super(viewFacet, data.getID(), data.getHomeID(), data.getCodeBase(), null);
        myCreatingViewID = viewID;
        myData = data;
        myView = aView;
    }

    /**
     * Called by the list manager to actually start the loading process.
     */
    public void start()
    {
        if (myThread == null) {
            myThread = new Thread(new LoadingDObjectRunner(this));
            myThread.start();
        }
    }
    
    void sendCreateDone(Exception e)
    {
        // Debug output
        if (e == null) debug("LoadingDObject.sendCreateDone.  Success.  Sending to "+myCreatingViewID+" for "+getID());
        else debug("LoadingDObject.sendCreateDone.  Failure.  Sending "+e+" back to "+myCreatingViewID+" for "+getID());
        
        // Create object array for closure
        Object arr[] = new Object[2];
        arr[0]=myView.getID();arr[1]=e;
        // Create closure and send it.  In this case we don't specify the class to 
        // use for this Closure in order to save bandwidth.  This means that the 
        // receiver will have to look on all its super classes for the CREATINGDONEMETHODNAME method
        try {
            sendClosureToRemote(myCreatingViewID, BaseFacet.createNewClosure(null, CREATINGDONEMETHODNAME, arr));
        } catch (Exception except) {
            // If this fails for whatever reason, forget about it because we're history
            debug("LoadingDObject.sendCreateDone.  Send failed with exception "+except.getMessage());
        }
    }
    
}

/**
 * Code that is run by the thread responsible for loading a new DObject.
 *
 * @author Scott Lewis
 */
class LoadingDObjectRunner implements Runnable {
    
    LoadingDObject myObject;
    
    public LoadingDObjectRunner(LoadingDObject obj) 
    {
        myObject = obj;
    }
    
    public void run() {
        View view = myObject.myView;
        CreateDObjectData data = myObject.myData;
        SessionViewID remoteID = myObject.myCreatingViewID;
        
        DObject newObject = null;
        debug("LoadingDObject thread creating new DObject with id "+myObject.getID());
        try {
            // Have view create new DObject instance
            newObject = view.loadDObject(data.getID(),data.getHomeID(),data.getCodeBase(),
                                         data.getClassName(), data.getParam());
        } catch (Exception e) {
            dumpStack(e, "LoadingDObject thread:  DObject creation failed.");
            myObject.sendCreateDone(e);
            view.removeDObjectFromLoading(myObject.getID());
            return;
        }
        debug("LoadingDObject thread created new DObject");
        // Now we remove ourselves and add 
        try {
            view.moveLoadingToInactive(newObject.getID(), newObject);
            if (data.getActivate()) {
                // Activate now if remote asked us to
                view.moveInactiveToActive(newObject.getID());
            }
        } catch (DObjectNotFoundException e) {
            dumpStack(e, "LoadingDObject thread:  LoadingDObject not found in list manager.");
            myObject.sendCreateDone(e);
            view.removeDObjectFromLoading(myObject.getID());
            return;
        }
        
        debug("LoadingDObject thread:  New DObject created and added to inactive list");
        myObject.sendCreateDone(null);
    }
    
    void debug(String message)
    {
        myObject.debug(message);
    }
    
    void dumpStack(Exception e, String message)
    {
        myObject.dumpStack(e, message);
    }
}
