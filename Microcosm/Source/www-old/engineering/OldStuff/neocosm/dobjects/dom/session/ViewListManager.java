/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ViewListManager.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;
import dom.util.*;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Manager of all of lists associated with the View (group of Views, and DObjects).  This
 * includes the list of SessionViewIDs identifying the current group membership,
 * and the list of DObjects in the group (which can be in one of several states
 * depending upon whether the DObject has been constructed/activated/destroyed, etc.)
 * Most methods on this class are synchronized...it is an implementation of the
 * 'fully synchronized object' pattern described by Doug Lea in his book "Concurrent
 * Programming in Java".  The View has the only reference to this object, but
 * it is declared final anyway to prevent any unexpected concurrency problems.
 * <p>
 * NOTE:  NONE OF THIS CODE SHOULD CALL EXTERNAL CODE.
 * THE CODE BELOW THAT DOES THIS (FOR NOTIFICATION) IS GUARANTEED NOT TO BLOCK
 * OR BE SYNCHRONIZED ON ANY OTHER OBJECT.  THEREFORE, THIS OBJECT WILL NOT
 * RESULT IN DEADLOCK IFF ALL THE CODE BELOW AVOIDS CALLING EXTERNAL METHODS.
 *
 * @see View
 *
 * @author Scott Lewis
 */
final class ViewListManager
{
    //public static final boolean debug = true;
    public static final boolean debug = false;

    // This hashtable contains DObjectID (key) -> LoadingDObject (value) mappings.
    // The entries in this hashtable are DObjects that are currently loading,
    // but are not yet ready for prime time ;-)
    private Hashtable myLoadingList;
    // This hashtable contains DObjectID (key) -> DObject (value) mappings.
    // The entries in this hashtable are DObjects that have been created
    // and initialized (for client presences they already have their initial
    // state).  This list can be considered a 'staging' area for new
    // presences of an object in the local view...or an area for objects
    // from 'suspect' processes in the group
    private Hashtable myInactiveList;
    // This hashtable contains DObjectID (key) -> DObject (value) mappings.
    // The entries in this hashtable are DObjects that have been created
    // and activated and are currently 'present' in the view provided by
    // the local View object.
    private Hashtable myActiveList;

    private Hashtable myDestroyedList;

    // The View we are the manager for
    private View myView;

    // Vector of SessionViewIDs, that identify the currently known group membership.
    // This is the Full Monty of group membership for the local view...right here
    /* SessionViewID */
    private Vector myGroup;

    /* DObjectID -> DObjectNotification */
    private Hashtable myDObjectNotifications;

    /**
     * Constructor
     *
     * @param aView the View that we are a manager for
     */
    ViewListManager(View aView)
    {
        myLoadingList = new Hashtable();
        myInactiveList = new Hashtable();
        myActiveList = new Hashtable();
        myDestroyedList = new Hashtable();
        myView = aView;
        myGroup = new Vector();
        myDObjectNotifications = new Hashtable();
        // Add our view's id to our group membership
        addGroupMember(aView.getID());
    }

    /**
     * Method to get at the group membership information for this View.
     * Returns an array of Objects that contains SessionViewIDs of all view
     * currently known to be in the group.
     *
     * @return SessionViewID[] that contains SessionViewIDs identifying all the
     * Views currently known to be in the group.
     */
    synchronized SessionViewID[] getGroupMembership()
    {
        SessionViewID arr[] = new SessionViewID[myGroup.size()];
        myGroup.copyInto(arr);
        return arr;
    }

    /**
     * Add a new view id to the set of known group members.  Does this atomically,
     * so that no other membership changes can occur while this change is being
     * made.
     *
     * @param id the SessionViewID to add
     */
    synchronized void addGroupMember(SessionViewID id)
    {
        // If same id is added, this method does nothing.  Makes this
        // operation idempotent
        if (myGroup.contains(id)) return;

        // Otherwise, go ahead
        myGroup.addElement(id);

        // Notify any/all loading dobjects
        for(Enumeration e= myLoadingList.elements(); e.hasMoreElements(); ) {
            DObject object = (DObject) e.nextElement();
            notifyClientCreation(object, id);
        }
        // Notify any/all inactive dobjects
        for(Enumeration e= myInactiveList.elements(); e.hasMoreElements(); ) {
            DObject object = (DObject) e.nextElement();
            notifyClientCreation(object, id);
        }

        // Notify any/all active dobjects
        for(Enumeration e= myActiveList.elements(); e.hasMoreElements(); ) {
            DObject object = (DObject) e.nextElement();
            notifyClientCreation(object, id);
        }
    }

    /**
     * Remove a view from the set of views known to be in this group.
     * Calls destroyDObjectsFrom to remove any/all client presences of
     * the objects associated with this SessionViewID.
     *
     * @param id the SessionViewID that is leaving the group.
     */
    synchronized void removeGroupMember(SessionViewID id)
    {
        myGroup.removeElement(id);
        destroyDObjectsFrom(id);
    }

    /**
     * Get current number of group members
     *
     * @return number of group members in group
     */
    synchronized int getGroupSize()
    {
        return myGroup.size();
    }

    /**
     * Add new DObject to those known to this view.  Does this atomically, so
     * that view change cannot occur while this is happening.
     *
     * @param object the DObject to be added
     * @param activate whether the DObject being added should be activated
     * @return false if some problem...should not be a problem if data
     * is valid.
     */
    synchronized boolean addNewDObject(DObject object, boolean activate)
    {
        DObjectID id = object.getID();
        // First, add given DObject to inactive list...this will
        addDObjectToInActive(id, object, null);
        // Get the current group membership
        SessionViewID remotes[] = getGroupMembership();
        // Notify new object that it might want to create clients
        // on all known remote view (of currently known view membership)
        for(int i = 0; i < remotes.length; i++) {
            if (!remotes[i].equals(myView.getID()))
                    notifyClientCreation(object, remotes[i]);
        }

        // If asked to activate, then do that as well
        if (activate)
        {
            try {
                activateDObject(id);
            } catch (Exception e) {
                // SHOULD NOT HAPPEN, as we just put it on the inactive list
                // in this synchronized method...no one should have changed
                // that state
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return true;
    }

    private void notifyClientCreation(DObject object, SessionViewID id)
    {
        // Get access facet and send appropriate message
        try {
            object.getAccessFacet().createClient(getCreateClientInfo(id));
        } catch (Exception e) {
            dumpStack(e, "notifyClientCreation");
        }
    }

    private CreateClientInfo getCreateClientInfo(SessionViewID id)
    {
        return new CreateClientInfo(id);
    }

    /**
     * Add given LoadingDObject to myLoadingList.  Does not add the object
     * if the id associated with the object is already in myLoadingList.
     */
    synchronized void addDObjectToLoading(LoadingDObject object)
    {
        // Only add to loading list if not already there with same id
        // This makes this operation idempotent
        DObjectID id = object.getID();
        if (!isLoading(id)) {
            // Add id->LoadingDObject to myLoadingList
            myLoadingList.put(id, object);
            // Now start the loading object in doing its thing
            object.start();
            if (debug) dumpListInfo("addDObjectToLoading", id);
        }
    }

    /**
     * Remove the DObject with the given DObjectID from the loading list.
     *
     * @param id the DObjectID of the DObject to remove
     * @exception DObjectNotFoundException if the DObject isn't found on the
     * loading list
     */
    synchronized void removeDObjectFromLoading(DObjectID id)
        throws DObjectNotFoundException
    {
        LoadingDObject object = (LoadingDObject) myLoadingList.remove(id);
        if (debug) dumpListInfo("removeDObjectFromLoading", id);
        if (object != null) {
            try {
                // Get access facet and then notify that the
                // it has been destroyed so it can deal with it
                object.getAccessFacet().destroy(getDestroyedNotifyInfo());
            } catch (Exception e) {         // Should NOT happen
                dumpStack(e, "ViewListManager.removeDObjectFromLoading");
                System.exit(-1);
            }
        } else {
            throw new DObjectNotFoundException(id);
        }
    }

    /**
     * Moves the given DObject object from the loading list to the
     * inactive list.  Assumes the given DObject is currently on the
     * loading list, if it is not, then a DObjectNotFoundException
     * is thrown.
     *
     * @param id the DObjectID of the DObject
     * @param object the DObject in question
     * @exception DObjectNotFoundException if DObject not on loading list
     */
    synchronized void createDObject(DObjectID id, DObject object)
        throws DObjectNotFoundException
    {
        LoadingDObject loadingDObject = (LoadingDObject) myLoadingList.get(id);
        if (loadingDObject == null || object == null) {
            throw new DObjectNotFoundException(id);
        }
        removeDObjectFromLoading(id);
        addDObjectToInActive(id, object, loadingDObject);
    }

    /**
     * Add given DObject to the inactive list of objects.  Also removes the
     * LoadingDObject from the loading list.  This method does the entire operation
     * atomically.
     *
     * @param id the DObjectID of the object
     * @param object the DObject of the object to add to the inactive
     * list
     * @param loadingDObject the LoadingDObject to remove from the loading list
     */
    synchronized void addDObjectToInActive(DObjectID id, DObject object, LoadingDObject loadingDObject)
    {
        try {
            if (loadingDObject != null) myLoadingList.remove(loadingDObject.getID());
            myInactiveList.put(id, object);
            object.getAccessFacet().create(getCreatedNotifyInfo());
            if (debug) dumpListInfo("addDObjectToInActive", id);
        } catch (Exception e) {             // Should NOT happen
            dumpStack(e, "ViewListManager.addDObjectToInActive");
            System.exit(-1);
        }
    }

    /**
     * Notify other interested objects that a new DObject has been created.
     *
     * @param object the just created DObject
     */
    synchronized void notifyDObjectCreated(DObject object)
    {
        // In this case, there is nothing to do
    }

    /**
     * Moves the given DObject object from the inactive list to the
     * active list.  Assumes the given DObject is currently on the
     * inactive list, if it is not, then a DObjectNotFoundException
     * is thrown.
     *
     * @param id the DObjectID of the DObject
     * @param object the DObject in question
     * @exception DObjectNotFoundException if DObject not on inactive list
     */
    synchronized void activateDObject(DObjectID id)
        throws DObjectNotFoundException
    {
        DObject object = (DObject) myInactiveList.get(id);
        if (object == null) throw new DObjectNotFoundException(id);
        try {
            object = (DObject) myInactiveList.remove(id);
            myActiveList.put(id, object);
            object.getAccessFacet().activate(getActivatedNotifyInfo());
            if (debug) dumpListInfo("activateDObject", id);
        } catch (Exception e) {         // Should NOT happen
            dumpStack(e, "ViewListManager.activateParty");
            System.exit(-1);
        }
    }

    /**
     * Notify other interested objects that a new DObject has been activated.
     *
     * @param object the just activated DObject
     */
    synchronized void notifyDObjectActivated(DObject object)
    {
        privateNotifyActivated(object);
    }

    /**
     * Notify all DObjectNotification objects that are available.  This method
     * expects that it should only be called from a method synchronized on
     * this ViewListManager object.
     *
     * @para object the object that has been activated
     */
    void privateNotifyActivated(DObject object)
    {
        for (Enumeration enum=myDObjectNotifications.elements(); enum.hasMoreElements(); )
        {
            try {
                DObjectNotification notify = (DObjectNotification) enum.nextElement();
                notify.otherActivated(object.getID());
            } catch (Exception e) {
                dumpStack(e, "ViewListManager.privateNotifyActivated");
            }
        }
    }

    /**
     * Moves the given DObject object from the active list to the
     * inactive list.  Assumes the given DObject is currently on the
     * active list, if it is not, then a DObjectNotFoundException
     * is thrown.
     *
     * @param id the DObjectID of the DObject
     * @param object the DObject in question
     * @exception DObjectNotFoundException if DObject not on active list
     */
    synchronized void deactivateDObject(DObjectID id)
        throws DObjectNotFoundException
    {
        DObject object = (DObject) myActiveList.get(id);
        if (object == null) throw new DObjectNotFoundException(id);
        try {
            object = (DObject) myActiveList.remove(id);
            myInactiveList.put(id, object);
            object.getAccessFacet().deactivate(getDeactivatedNotifyInfo());
            if (debug) dumpListInfo("dectivateDObject", id);
        } catch (Exception e) {         // Should NOT happen
            dumpStack(e, "ViewListManager.deactivateParty");
            System.exit(-1);
        }
    }

    /**
     * Notify other interested objects that a new DObject has been deactivated.
     *
     * @param object the just deactivated DObject
     */
    synchronized void notifyDObjectDeactivated(DObject object)
    {
        privateNotifyDeactivated(object);
    }

    /**
     * Notify all DObjectNotification objects that are available.  This method
     * expects that it should only be called from a method synchronized on
     * this ViewListManager object.
     *
     * @para object the object that has been deactivated
     */
    void privateNotifyDeactivated(DObject object)
    {
        for (Enumeration enum=myDObjectNotifications.elements(); enum.hasMoreElements(); )
        {
            try {
                DObjectNotification notify = (DObjectNotification) enum.nextElement();
                notify.otherDeactivated(object.getID());
            } catch (Exception e) {
                dumpStack(e, "ViewListManager.privateNotifyDeactivated");
            }
        }
    }

    synchronized void destroyDObject(DObjectID id)
        throws DObjectNotFoundException
    {
        DObject object = (DObject) myInactiveList.get(id);
        if (object == null) {
            object = (DObject) myActiveList.get(id);
            if (object == null) throw new DObjectNotFoundException(id);
            deactivateDObject(id);
        }
        try {
            object = (DObject) myInactiveList.remove(id);
            myDestroyedList.put(id, object);
            object.getAccessFacet().destroy(getDestroyedNotifyInfo());
            if (debug) dumpListInfo("destroyDObject", id);
        } catch (Exception e) {             // Should NOT happen
            dumpStack(e, "ViewListManager.destroyDObject");
            System.exit(-1);
        }
    }

    /**
     * Notify other interested objects that a new DObject has been destroyed.
     *
     * @param object the just destroyed DObject
     */
    synchronized void notifyDObjectDestroyed(DObject object)
    {
        DObjectID id = object.getID();
        // Facet Cleanup
        // Ask our view to remove any pending receipts that
        // involve this DObject...either as sender or receiver of
        // request
        myView.removeReceiptsForID(id);
        // Ask our view to remove any/all facets that exist for this object
        // either as facet receiver or facet owner
        myView.removeFacetsForID(id);
        // End facet cleanup
        // Cleanup of notification
        removeDObjectNotify(id);
        // Now make it go away for good
        myDestroyedList.remove(id);
    }

    /**
     * Destroy all DObjects that have id as their home SessionViewID
     *
     * @param id the SessionViewID of interest
     */
    synchronized void destroyDObjectsFrom(SessionViewID id)
    {
        destroyDObjectsFrom(id, true);
    }

    /**
     * Destroy all DObjects that don't have their home id the same as
     * our view's (clients).  Does this atomically.
     */
    synchronized void destroyClientPresences()
    {
        destroyDObjectsFrom(myView.getID(), false);
    }

    /**
     * Destroy everything known to us
     */
    synchronized void clear()
    {
        destroyDObjectsFrom(null, true);
    }

    /**
     * Destroy all DObjects whose home ID matches the one given (or not,
     * determined by whether the match parameter is true or false).
     * Must call only from synchronized methods in this class.
     *
     * @param id the SessionViewID identifying a particular view
     * @param match if true, destroy all DObjects who think their
     * home is id; if false, destroy all DObjects who think their home
     * id is different than id
     */
    private void destroyDObjectsFrom(SessionViewID id, boolean match)
    {
        try {
            Enumeration enum = getDObjectIDs(id,match).elements();
            while (enum.hasMoreElements()) {
                DObjectID objectID = (DObjectID) enum.nextElement();
                if (isLoading(objectID)) {
                    removeDObjectFromLoading(objectID);
                } else {
                    destroyDObject(objectID);
                }
            }
        } catch (DObjectNotFoundException e) {
            dumpStack(e, "destroyDObjectsFrom");
        }
    }

    /**
     * Activate all inactive DObjects.
     */
    synchronized void activate()
    {
        for (Enumeration enum = myInactiveList.keys(); enum.hasMoreElements(); ) {
            DObjectID objectID = (DObjectID) enum.nextElement();
            try {
                activateDObject(objectID);
            } catch (Exception e) {
                dumpStack(e, "ViewListManager.activate");
            }
        }
    }

    /**
     * Deactivate all active DObjects.
     */
    void deactivate()
    {
        for (Enumeration enum = myActiveList.keys(); enum.hasMoreElements(); ) {
            DObjectID id = (DObjectID) enum.nextElement();
            try {
                deactivateDObject(id);
            } catch (Exception e) {
                dumpStack(e, "ViewListManager.deactivate");
            }
        }
    }


    /**
     * Get a Vector of DObjectIDs that either believe their home view id
     * to be the same as the given SessionViewID, or not, based upon the
     * value of the match parameter
     *
     * @param id the SessionViewID to compare to
     * @param match if true, this method returns the Vector of DObjectIDs
     * whose home view id is the same as id; if false, returns the Vector
     * of DObjectIDs whose home view id is *not* the same as id
     */
    synchronized Vector getDObjectIDs(SessionViewID id, boolean match)
    {
        VectorEx newVect = new VectorEx(new HomeViewIDFilter(myLoadingList, id, match));
        newVect.addAll(new HomeViewIDFilter(myInactiveList, id, match));
        newVect.addAll(new HomeViewIDFilter(myActiveList, id, match));
        return newVect;
    }


    ///////////////////  Utility and Access Methods ///////////////////////////////////

    /**
     * Verify that given DObject is in one of our lists (other than the
     * destroyed list).  Makes sure parameter is not null, then checks to
     * see that the object's id is found somewhere, and that the matching
     * DObject is the same entity that was passed in.
     *
     * @param object the DObject to verify
     * @return true if found somewhere in our lists, false otherwise
     */
    boolean verifyDObject(DObject object)
    {
        return (object != null) && (getDObjectFromAnywhere(object.getID()) == object);
    }

    /**
     * Access method to add the notification facet to our list of
     * entities to notify about the arrival/departure of DObjects.
     *
     * @param notify the DOBjectNotification to add
     */
    synchronized void addDObjectNotify(DObject object)
    {
       try {
         myDObjectNotifications.put(object.getID(), new DObjectNotification(object));
       } catch (NullFacetException e) {
        // If this happens just ignore and don't do add as DObject has
        // broken ontological commitment
       }
    }

    /**
     * Access method to remove the notification facet from our list of
     * entities to notify about the arrival/departure of DObjects.
     *
     * @param notify the DOBjectNotification to remove
     */
    synchronized void removeDObjectNotify(DObjectID notifyID)
    {
       myDObjectNotifications.remove(notifyID);
    }

    /**
     * Given provided id key, get the corresponding DObject.  User might
     * want to then call getAccessFacet to get a facet for sending messages
     * to the DObject.
     *
     * @param id the DObjectID of the DObject we are looking for
     * @return DObject found corresponding to DObjectID.  Null if not found.
     */
    DObject getDObjectFromAnywhere(DObjectID id)
    {
        DObject result = getDObjectFromActive(id);
        if (result == null) result = getDObjectFromInactive(id);
        if (result == null) result = getDObjectFromLoading(id);
        // Don't look on destroyed list
        return result;
    }
    
    /**
     * Given provided id key, get the corresponding DObjectViewFacet.  
     *
     * @param id the DObjectID of the DObject we are looking for
     * @return DObject found corresponding to DObjectID.  Null if not found.
     */
    DObjectViewFacet getDObjectViewFacetFromAnywhere(DObjectID id)
    {
        DObject object = getDObjectFromAnywhere(id);
        if (object != null) {
            try {
                return object.getAccessFacet();
            } catch (NullFacetException e) {
                return null;
            }
        }
        else return null;
    }
    

    /**
     * Given provided id key, get the corresponding DObject from the
     * loading list.
     *
     * @param id the DObjectID of the DObject we are looking for
     * @return DObject found corresponding to DObjectID.  Null if not found.
     */
    DObject getDObjectFromLoading(DObjectID id)
    {
        return (DObject) myLoadingList.get(id);
    }

    /**
     * Given provided id key, get the corresponding DObject from the
     * active list.
     *
     * @param id the DObjectID of the DObject we are looking for
     * @return DObject found corresponding to DObjectID.  Null if not found.
     */
    DObject getDObjectFromActive(DObjectID id)
    {
        return (DObject) myActiveList.get(id);
    }

    /**
     * Given provided id key, get the corresponding DObject from the
     * inactive list.
     *
     * @param id the DObjectID of the DObject we are looking for
     * @return DObject found corresponding to DObjectID.  Null if not found.
     */
    DObject getDObjectFromInactive(DObjectID id)
    {
        return (DObject) myInactiveList.get(id);
    }

    /**
     * Given provided id key, get the corresponding DObject from the
     * destroyed list.
     *
     * @param id the DObjectID of the DObject we are looking for
     * @return DObject found corresponding to DObjectID.  Null if not found.
     */
    DObject getDObjectFromDestroyed(DObjectID id)
    {
        return (DObject) myDestroyedList.get(id);
    }

    /**
     * Get ViewNotifyInfo for sending the created message to a newly created
     * DObject.
     *
     * @return ViewNotifyInfo for passing to created message
     */
    private ViewNotifyInfo getCreatedNotifyInfo()
    {
        // XXX may want to have this ask the View
        // For now, just return null
        return null;
    }

    /**
     * Get ViewNotifyInfo for sending the activated message to a newly created
     * DObject.
     *
     * @return ViewNotifyInfo for passing to activated message
     */
    private ViewNotifyInfo getActivatedNotifyInfo()
    {
        // XXX may want to have this ask the View
        // For now, just return null
        return null;
    }

    /**
     * Get ViewNotifyInfo for sending the deactivated message to a newly created
     * DObject.
     *
     * @return ViewNotifyInfo for passing to deactivated message
     */
    private ViewNotifyInfo getDeactivatedNotifyInfo()
    {
        // XXX may want to have this ask the View
        // For now, just return null
        return null;
    }

    /**
     * Get ViewNotifyInfo for sending the destroyed message to a newly created
     * DObject.
     *
     * @return ViewNotifyInfo for passing to destroyed message
     */
    private ViewNotifyInfo getDestroyedNotifyInfo()
    {
        // XXX may want to have this ask the View
        // For now, just return null
        return null;
    }

    /**
     * Determine whether an object with a given id is already on the loading
     * list.  Returns true if so, false otherwise.
     *
     * @param id the DObjectID we are looking for
     * @return true if is currently on myLoadingList, false otherwise.
     */
    boolean isLoading(DObjectID id)
    {
        return myLoadingList.containsKey(id);
    }

    /**
     * Determine whether DObject corresponding to given DObjectID is
     * currently on active list
     *
     * @param id the DObjectID of the DObject we're looking for
     */
    boolean isActive(DObjectID id)
    {
        return myActiveList.containsKey(id);
    }

    /**
     * Determine whether DObject corresponding to given DObjectID is
     * currently on inactive list
     *
     * @param id the DObjectID of the DObject we're looking for
     */
    boolean isInactive(DObjectID id)
    {
        return myInactiveList.containsKey(id);
    }


    /**
     * Show the contents of our various lists.
     *
     * @param methodName the method where we are doing this
     * @param id the DObjectID of the object concerned
     */
    private void dumpListInfo(String methodName, DObjectID id)
    {
        System.out.println(methodName+"("+id+"):");
        System.out.println("    Active:   "+myActiveList);
        System.out.println("    InActive: "+myInactiveList);
        System.out.println("    Loading:  "+myLoadingList);
        System.out.println("    Destroyed:"+myDestroyedList);
    }

    /**
     * Routine to dump stack.  Simply passes it on to our View object.
     *
     * @param e the Exception involved
     * @param location a String identifying where in the code this took place
     */
    private void dumpStack(Exception e, String location)
    {
        myView.dumpStack(e, location);
    }
}

/**
 * An enumeration that returns DObjectID's for those DObjects
 * who think their home ID matches the a given SessionViewID.
 */
class HomeViewIDFilter implements Enumeration
{
    DObjectID next;
    SessionViewID homeViewID;
    Enumeration enum;
    boolean match;

    public boolean hasMoreElements()
    {
        if (next == null) next = findNext();
        return (next != null);
    }

    public Object nextElement()
    {
        if (hasMoreElements()) {
            DObjectID value = next;
            next = null;
            return value;
        } else {
            throw new NoSuchElementException("HomeViewIDFilter");
        }
    }

    DObjectID findNext()
    {
        while (enum.hasMoreElements()) {
            DObject object = (DObject) enum.nextElement();
            if (homeViewID == null || (match ^ !object.getHomeID().equals(homeViewID))) {
                return object.getID();
            }
        }
        return null;
    }

    HomeViewIDFilter(Hashtable source, SessionViewID homeID, boolean match)
    {
        this.homeViewID = homeID;
        this.match = match;
        next = null;
        enum = source.elements();
    }
}


