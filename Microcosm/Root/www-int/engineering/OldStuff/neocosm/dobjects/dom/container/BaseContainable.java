/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/container/BaseContainable.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.container;

import dom.session.ComponentDObject;
import dom.session.DObjectFacet;
import dom.session.BaseComponent;
import dom.id.DObjectID;
import dom.session.SessionViewID;

import dom.util.DOMException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import dom.session.NullFacetException;

/**
 * Root class to represent the 'containable' side of a container-containable
 * relation.
 *
 * @see BaseContainerFacet
 * @see BaseContainer
 * @see BaseContainableFacet
 * @see ComponentDObject
 *
 * @author Scott Lewis
 */
public class BaseContainable extends BaseComponent {
    
    // The state variable that determines the id of our current container.
    // This can be null...in that case we don't have any container.
    protected DObjectID myContainer;
    // The object that represent the facet to our container.  Only valid if
    // the myContainer variable above is non-null
    protected BaseContainerFacet myContainerFacet;
    // Whether or not we are currently in transit.  This variable serves as a guard
    // to the add/remove methods, so that we cannot get ourselves in a confused
    // state about our container
    protected boolean myInTransit;
    // Variable to hold the initial container id for our clients.  In host, this
    // will always be null, but can be non-null in clients.  This is set by 
    // clients upon construction (setInitialState), and allows the host to give
    // its clients information about their initial containment state.
    protected DObjectID myInitialContainer;
    
    public BaseContainable(ComponentDObject owner, String identifier)
    {
        super(owner, identifier);
        myContainer = null;
        myContainerFacet = null;
        myInTransit = false;
    }
    
    /**
     * Entry point method for adding a containable to a container.  The desired
     * container is identified by the containerID param.  The data param is
     * any required/desired data for passing TOS requirements imposed by the
     * container.  This method will initiate a protocol between this containable
     * and the container identified by containerID.  If we cannot access the
     * BaseContainerFacet facet for this id, a NullFacetException will be thrown.
     * If the facet is found, then an 'add' message will be sent to the 
     * owner of the BaseContainer DObject, and we will enter a 'waiting' state
     * to determine the outcome of the add (dependent upon the container's 
     * decision).  The outcome notification will come in the form of the 'asynchAdded'
     * message below...if the resultException is null, it means that we were
     * added successfully.  If non-null, the Exception identifies the error
     * associated with the add.
     *
     * @param containerID the DObjectID that we wish to be our container
     * @param data any data that is required for delivery to the container for
     * admittance checks
     * @exception NullFacetException thrown if we can't find a facet for the given id
     * @exception ContainableException thrown if containable is not in correct
     * state for add (i.e. if it's already contained by something else, first
     * it must be removed from the other container, then added to the new
     * container).
     */
    public void addToContainer(DObjectID containerID, Serializable data) 
        throws NullFacetException, ContainableException
    {
        addToContainer(getContainerFacetForID(containerID), data);
    }
    
    /**
     * Alternative implementation of addToContainer.  See addToContainer
     * above.
     *
     * @param container the BaseContainerFacet we are going to add ourselves to
     * @param data the Serializable data we are sending to the container to
     * meet its TOS needs (if any).  May be null.
     * <p>
     * Synchronized so that if multiple threads call this method, that they
     * are serialized.  Not likely to be an issue, given that this code is likely
     * to only be called by the owner DObject's run queue thread...but just in
     * case (and for testing).  The runtime cost of doing this is insignificant
     * (compared to the messaging).
     *
     * @param container the BaseContainerFacet for the container of interest
     * @param data the Serializable data sent to the container to meet its
     * TOS requirements (if any).  May be null.
     * @exception NullFacetException thrown if BaseContainerFacet is revoked
     * at any time while this is running.
     * @exception ContainableException thrown if containable is not in correct
     * state for add (i.e. if it's already contained by something else, first
     * it must be removed from the other container, then added to the new
     * container).
     */
    public synchronized void addToContainer(BaseContainerFacet container, Serializable data)
        throws NullFacetException, ContainableException
    {
        debug("BaseContainable.addToContainer for "+this+".  Container facet is "+container+", data is "+data);
        // Sanity check for parameter
        if (container == null || container.isRevoked()) throw new NullFacetException();
        // Get the facet we have given to the container previously
        BaseContainableFacet ourFacet = (BaseContainableFacet) getDeliveredFacet(container.getID());
        if (ourFacet == null) throw new NullFacetException();
        // The following is to prevent us from being added to a new container without
        // being removed from our old container.  In addition, we cannot be in transit.
        if (myContainer != null || myContainerFacet != null || myInTransit) throw new ContainableException();
        // We are officially in transit now
        myInTransit = true;
        // Set our putative container facet to the facet we are communicating with.
        // This does not mean we consider ourselves contained.  That only happens when
        // we receive the asynchAdded notification from the container, signalling us
        // that agreement about containment has been reached
        myContainerFacet = container;
        // We send the actual message to the container to start things off
        container.add(ourFacet, data);
    }
        
    /**
     * Entry point method for removing a containable from a container.  This method 
     * will initiate a protocol between this containable and the container it is
     * currently contained by.  If we cannot access the BaseContainerFacet facet for 
     * this containable's container, a NullFacetException will be thrown. If the facet 
     * is found, then an 'remove' message will be sent to the owner of the 
     * BaseContainer DObject, and we will enter a 'waiting' state to determine the 
     * outcome of the remove (dependent upon the container's decision, although remove 
     * cannot fail).  The outcome notification will come in the form of the 'asynchRemoved' 
     * message below...if the resultException is null, it means that we were removed successfully.  If 
     * non-null, the Exception identifies the error associated with the remove.  In 
     * either case, however, in the case of a remove the actual remove *will happen*.
     *
     * @exception NullFacetException thrown if we can't find a facet for the given id
     * @exception ContainableException thrown if containable is not in correct
     * state for remove (i.e. if it's not already contained by something else).
     */
    public synchronized void removeFromContainer()
        throws NullFacetException, ContainableException
    {
        debug("BaseContainable.removeFromContainer for "+this+".  Container facet is "+myContainerFacet);
        // If our facet is null, then we are not contained, and must throw
        if (myContainer == null || myContainerFacet == null || myInTransit) throw new ContainableException();
        
        BaseContainableFacet ourFacet = (BaseContainableFacet) getDeliveredFacet(myContainerFacet.getID());
        if (ourFacet == null) throw new NullFacetException();
        // The following is to prevent us from being removed from a new container without
        // actually believing we are currently in a container.  In addition, we cannot 
        // be in transit.
        // We are officially in transit now
        myInTransit = true;
        // We send a message to the container to start things off
        myContainerFacet.remove(ourFacet);
    }
    
    /**
     * This is the notification (received asynchronously) from a container that we have
     * been added.  The add was successful if the second parameter was null.  If the second
     * parameter was non-null, then the add failed, and the second parameter contains the
     * exception type of the failure.
     *
     * @param containerID the DObjectID of our new container
     * @param resultException the Exception experienced by the container as a result of
     * the add attempt.  If null, means that add was successful.  If non-null, identifies
     * the problem
     * @param resultFacet a DObjectFacet that the container has the option of presenting
     * to us upon successful (or unsuccessful) completion of the add operation
     */
    public final void asynchAdded(DObjectID containerID, Exception resultException, DObjectFacet resultFacet)
    {
        if (resultException != null) {
            // Some error occurred.
            // In this case, we do not consider ourselves contained, so we null out
            // our containment state variables...
            reallyDoTheRemove();
            // Then let subclasses get this notification and do with it what they will
            addFailed(containerID, resultException, resultFacet);
        } else {
            reallyDoTheAdd(containerID, myContainerFacet);
            // Then let subclasses get this notification and deal with any resulting facets
            addSucceeded(resultFacet);
        }
    }
    
    /**
     * Helper method to do the actual add.  This changes the relevant state variables,
     * indicating that we have now been officially added to the given container.
     *
     * @param containerID the DObjectID of the container that now contains us
     * @param containerFacet the facet (may be null) provided to us by the container as
     * part of the containment protocol
     */
    private synchronized void reallyDoTheAdd(DObjectID containerID, BaseContainerFacet containerFacet)
    {
        debug("BaseContainable.reallyDoTheAdd.  Containable "+this+" is OFFICIALLY ADDED to container "+containerID);
        // We are now officially contained
        myContainer = containerID;
        myContainerFacet = containerFacet;
        // No longer in transit
        myInTransit = false;
    }
    
    /**
     * Helper method to do the actual remove.  This changes the relevant state variables,
     * indicating that we have now been officially removed from a container.
     *
     */
    private synchronized void reallyDoTheRemove()
    {
        debug("BaseContainable.reallyDoTheRemove.  Containable "+this+" is OFFICIALLY REMOVED from container "+myContainer);
        myContainer = null;
        myContainerFacet = null;
        myInTransit = false;
    }
    
    /**
     * Subclass overrideable method that is called when we have been successfully
     * addd to a new container.  This method is called from asychAdded.
     *
     * @param resultFacet the DObjectFacet passed to us by the container as part of 
     * the add protocol (may be null)
     */
    protected void addSucceeded(DObjectFacet resultFacet)
    {
        debug("BaseContainable.addSucceeded.  Add to "+myContainer+" SUCCEEDED with facet "+resultFacet);
    }
    
    /**
     * Subclass overrideable method that is called when we have been unsuccessfully
     * addd to a new container.  This method is called from asychAdded.
     *
     * @param containerID the DObjectID of the container we failed to be added to
     * @param resultException the Exception that occurred during the add protocol
     * @param resultFacet the DObjectFacet passed to us by the container as part of 
     * the failed add protocol (may be null)
     */
    protected void addFailed(DObjectID containerID, Exception resultException, DObjectFacet resultFacet)
    {
        debug("BaseContainable.addFailed.  Add to "+containerID+" FAILED with exception "+resultException+" and facet "+resultFacet);
    }

    /**
     * This is the notification (received asynchronously) from a container that we have
     * been removed.  The remove was successful if the second parameter was null.  If the second
     * parameter was non-null, then the remove failed, and the second parameter contains the
     * exception type of the failure.  In the case of a remove, however, if an exception
     * occurred during the remove protocol *both sides* (us and the container) are to assume
     * that the remove completed successfully.  In other words, if an exception occurred
     * during the protocol, then both sides assume that the containable (us) has been
     * removed from the container.
     *
     * @param containerID the DObjectID of our former container
     * @param resultException the Exception experienced by the container as a result of
     * the remove attempt.  If null, means that the remove was successful.  If non-null, identifies
     * the problem
     * @param resultFacet a DObjectFacet that the container has the option of presenting
     * to us upon successful (or unsuccessful) completion of the remove operation
     */
    public final void asynchRemoved(DObjectID containerID, Exception resultException, DObjectFacet resultFacet)
    {
        // Sanity check.  We should only be receiving this notification from our current
        // container.  If not, we ignore it.
        if (myContainer != null && myContainer.equals(containerID)) {
            // In this case, we're going to assume that we were removed, whether an 
            // exception occurred or not.  This is an agreement between us and the BaseContainer
            // component, that if some exception happens during the add/remove containment
            // protocol, that *both sides* will consider the containment broken
            debug("BaseContainable.removed.  containerID="+containerID+", resultException="+resultException+", resultFacet="+resultFacet);
            reallyDoTheRemove();
            if (resultException != null) {
                removeFailed(containerID, resultException, resultFacet);
            } else {
                removeSucceeded(containerID, resultFacet);
            }
        }
    }
    
    /**
     * Subclass overrideable method that is called when we have been successfully
     * removed from a container.  This method is called from asychRemoved.
     *
     * @param containerID the container we have been removed from
     * @param resultFacet the DObjectFacet passed to us by the container as part of 
     * the remove protocol (may be null)
     */
    protected void removeSucceeded(DObjectID containerID, DObjectFacet resultFacet)
    {
        debug("BaseContainable.removeSucceeded.  Remove from "+containerID+" SUCCEEDED with facet "+resultFacet);
    }
    
    /**
     * Subclass overrideable method that is called when we have been unsuccessfully
     * removed from a new container.  This method is called from asychRemoved.
     *
     * @param containerID the DObjectID of our former container
     * @param resultException the Exception that occurred during the remove protocol
     * @param resultFacet the DObjectFacet passed to us by the container as part of 
     * the failed remove protocol (may be null)
     */
    protected void removeFailed(DObjectID containerID, Exception resultException, DObjectFacet resultFacet)
    {
        debug("BaseContainable.removeFailed.  Remove from "+containerID+" FAILED with exception "+resultException+" and facet "+resultFacet);
    }
    
    public void asynchOtherAdded(DObjectID containerID, DObjectID otherID)
    {
        // DEBUG
        System.out.println("BaseContainable.otherAdded.  containerID="+containerID+", otherID="+otherID);
    }
    
    public void asynchOtherRemoved(DObjectID containerID, DObjectID otherID)
    {
        // DEBUG
        System.out.println("BaseContainable.otherRemoved.  containerID="+containerID+", otherID="+otherID);
    }
    
    /**
     * Get the BaseContainerFacet for the given id.  This just calls the 
     * ComponentDObject.getFacetForID method, and then casts the result to a BaseContainerFacet.
     * If this doesn't cast right, it returns null.
     *
     * @param id the DObjectID of the facet we're looking for
     * @return BaseContainerFacet for the given id
     * @exception NullFacetException thrown if some problem with this
     */
    protected BaseContainerFacet getContainerFacetForID(DObjectID id)
        throws NullFacetException
    {
        try {
            return (BaseContainerFacet) getFacetForID(id);
        } catch (ClassCastException e) {
            removeFacetFromCache(id);
            return null;
        }
    }
    
    // Overrides of BaseComponent methods
    
    /**
     * Handle the departure of the DObject identified by the id parameter.  This is
     * called for us by the ComponentDObject.notifyComponentsOfDeactivation, which is
     * called in response to the departure of the DObject identified by the given
     * DObjectID.  In response to this, for the BaseContainer class, we assume that
     * if we contain the given DObject, that we should *remove* it from our set of
     * contained DObjects.  If subclasses wish, they may override this behavior, and
     * do something else in response to this message, BUT they cannot assume that
     * any facets they might be holding onto that belong to this id are still valid.
     *
     * @param id the DObjectID of the departing DObject
     */
    public void handleDObjectGone(DObjectID id)
    {
        // If we are currently contained, and the object that went away is our
        // container, then we will now consider ourselves uncontained.  We assume that
        // our container does the same.
        if (myContainer != null && myContainer.equals(id)) {
            debug("BaseContainable.handleDObjectGone.  Our container "+id+" has left according to BaseContainable "+this);
            reallyDoTheRemove();
        }
    }
    
    /**
     * Create a new instance of a facet for delivery to the DObject identified by
     * the id parameter.  This default implementation always returns null, which
     * means that no facet is provided.  Subclasses will want to override this
     * to deliver facets to target DObjects as appropriate.  The data parameter
     * is present so that arbitrary data associated with the request can be passed
     * into this method, so it can make the appropriate judgment about whether a
     * facet should be provided.
     *
     * @param requestor the DObjectID of the requestor DObject
     * @param data any Serializable data required
     */
    public DObjectFacet createNewFacetForID(DObjectID requestor, Serializable data)
    {
        try {
            return new BaseContainableFacet(getOwner(), this);
        } catch (NullFacetException e) {
            return null;
        }
    }
    
    /**
     * Check given facet for addition to our facet cache.  This is called by the
     * ComponentDObject.notifyComponentsOfFacet method.  This method allows the
     * code in this component to decide whether it is interested in caching the
     * facet that has been provided to the owner DObject, potentially for later
     * use.  If this method returns true, then the facet is added to our cache
     * via the addFacetToCache method.  If false, the facet is ignored by this
     * component.  This implementation of this method always returns false, indicating
     * that it is not interested in any facets.  Subclasses can override this
     * functionality as desired.
     *
     * @param aFacet the DObjectFacet that we are entitled to examine
     * @return true if the given DObjectFacet should be cached, false otherwise
     */
    public boolean checkFacetForCache(DObjectFacet aFacet)
    {
        if (aFacet != null && !aFacet.isRevoked() && aFacet instanceof BaseContainerFacet) {
            // If we're a client, and we are expecting this containable (we already
            // contain it), then add it to our set, and remove it from the initial state vector
            if (isClient() && myInitialContainer != null) {
                DObjectID containerID = aFacet.getID();
                synchronized (this) {
                    if (myInitialContainer.equals(containerID)) {
                        debug("BaseContainable.checkFacetForCache.  Found our container!  Containable "+this+" adding "+containerID);
                        reallyDoTheAdd(containerID, (BaseContainerFacet) aFacet);
                        myInitialContainer = null;
                    }
                }
            }
            // Allow our superclass to add this facet to our cache by returning true.
            return true;
        }
        return false;
    }
    
    protected Serializable getStateForClient(SessionViewID id)
    {
        // If we are currently contained, then send id of our container
        // This is all the state we must send!
        return (Serializable) myContainer;
    }
    
    protected void setInitialState(Serializable initialState)
    {
        
        // This should be an instance of DObjectID (our container). 
        // If it's not this will throw a ClassCastException, but the exception
        // will be caught in the ComponentDObject.setInitialState method
        myInitialContainer = (DObjectID) initialState;
    }
    
    
}
