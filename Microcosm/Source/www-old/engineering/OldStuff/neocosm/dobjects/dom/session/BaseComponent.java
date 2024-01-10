/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/BaseComponent.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.DObjectID;

import java.util.Hashtable;

import java.io.Serializable;

/**
 * Base class for all components (e.g. BaseContainer, BaseContainable)
 * This base class implements the Component interface, and provides basic
 * services for all components, like facet management, and etc.
 *
 * @see ComponentDObject
 *
 * @author Scott Lewis
 */
public class BaseComponent {

    public static boolean debug = true;
    //public static boolean debug = false;
    
    // The owner DObject that this code belongs to
    protected ComponentDObject myOwner;
    // A string that the owner can use to disambiguate this specific instance
    // of the BaseContainer from other BaseContainer instances used in this
    // specific ComponentDObject
    protected String myIdentifier;
    // A Facet cache. This is to hold onto facets that we have been given by other
    // DObjects, that we might potentially use.
    Hashtable myFacetCache;
    // Hashtable of facets that *we* have delivered to other DObjects.  These are facets
    // to this component's functionality, indexed by the receiver of the facet
    Hashtable myDeliveredFacets;

    /**
     * Constructor for BaseComponent.  All components must have a non-null ComponentDObject
     * owner, and a non-null string identifier.  The following constructor guarantees this.
     *
     * @param owner the ComponentDObject owner of this component
     * @param identifier the String identifier, that must be unique within the given
     * ComponentDObject for identifying this component
     */
    public BaseComponent(ComponentDObject owner, String identifier)
    {
        if (owner == null || identifier == null) throw new NullPointerException("ComponentDObject owner and identifier cannot be null");
        myOwner = owner;
        myIdentifier = identifier;
    }

    /**
     * Get the String identifier for this BaseComponent.  It is the ComponentDObject owner's
     * responsibility for making sure that what this method returns is unique within
     * that ComponentDObject
     */
    public final String getIdentifier()
    {
        return myIdentifier;
    }

    /**
     * Get the ComponentDObject owner of this component.
     *
     * @return ComponentDObject owner of this component.
     */
    public final ComponentDObject getOwner()
    {
        return myOwner;
    }

    /**
     * Method called by ComponentDObject code to allow us to clean up our
     * facet cache and myDeliveredFacet entries when a given DObjectID has
     * gone away.  This is called by ComponentDObject.
     *
     * @param id the DObjectID that has gone away
     */
    public final void cleanUpFacetsWhenDObjectGone(DObjectID id)
    {
        // First, remove from our facet cache any facets that we have associated
        // with the given id.
        removeFacetFromCache(id);
        // Also revoke and remove any facets to *ourselves* that we have delivered
        // to other DObjects
        revokeAndRemoveDeliveredFacet(id);
    }

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
        // For base class, do nothing
        // Subclasses may override as appropriate
    }

    /**
     * Get a DObjectFacet to this BaseComponent's functionality, intended for the
     * given requestor.  This is called by the ComponentDObject when it wishes
     * to provide access to the functionality exposed by this Component to the
     * *given* DObject.
     *
     * @param requestor the DObjectID of the other this facet will be delivered to
     * @param data any Serializable data that we wish to require as part of the
     * information required of the requested for facet granting
     * @return DObjectFacet that we wish to provide to the requestor identified by
     * the first parameter.  May be null.  In the case of this root class implementation,
     * null is always returned.  Subclasses may override to get desired behavior.
     */
    public final DObjectFacet getFacetForComponent(DObjectID requestor, Serializable data)
    {
        DObjectFacet aFacet = createNewFacetForID(requestor, data);
        if (aFacet != null) {
            addDeliveredFacet(requestor, aFacet);
        }
        return aFacet;
    }
    
    /** 
     * Add the given facet to the Hashtable of facets that we have delivered to
     * other DObjects.  These are facets to *our* functionality, which we have to
     * clean up on if the receiver of the DObjectFacet goes away.
     *
     * @param id the DObjectID of the receiver of the given facet
     * @param aFacet the facet that we are giving up to id
     */
    public final void addDeliveredFacet(DObjectID id, DObjectFacet aFacet)
    {
        debug("BaseComponent.addDeliveredFacet.  Facet "+aFacet+" will be delivered to "+id+" by component "+this);
        // If first time, make hashtable
        if (myDeliveredFacets == null) {
            myDeliveredFacets = new Hashtable();
        }
        myDeliveredFacets.put(id, aFacet);
    }
    
    /**
     * Get a facet to the other that we have already delivered.
     *
     * @param id the DObjectID of the receiver of the facet
     * @return DObjectFacet the facet we have already given out to id, if found.
     * If not found, null.
     */
    public final DObjectFacet getDeliveredFacet(DObjectID id)
    {
        if (myDeliveredFacets != null) {
            DObjectFacet aFacet = (DObjectFacet) myDeliveredFacets.get(id);
            if (aFacet != null) {
                debug("BaseComponent.getDeliveredFacet.  Facet "+aFacet+" for "+id+" found in delivered facets");
                return aFacet;
            }
        } 
        debug("BaseComponent.getDeliveredFacet.  Facet for "+id+" NOT found in delivered facets");
        return null;
    }
    
    /**
     * Remove any facets that we have delivered to id specified by parameter.  That is,
     * if we have delivered any facets to our functionality to the given id, then
     * revoke any such facet, and then remove it from our Hashtable of delivered facets
     *
     * @param id remove any/all facets associated with this id
     */
    public final void revokeAndRemoveDeliveredFacet(DObjectID id)
    {
        debug("BaseComponent.removeDeliveredFacet.  Facet delivered to "+id+" by component "+this+" is being revoked and removed");
        if (myDeliveredFacets != null) {
            DObjectFacet aFacet = (DObjectFacet) myDeliveredFacets.remove(id);
            if (aFacet != null) {
                try {
                    aFacet.revoke(getOwner());
                } catch (NullFacetException e) { 
                    // IGNORE 
                }
            }
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
        return null;
    }

    // Methods to manage the facet cache.  These methods manipulate our facet cache.

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
        return false;
    }
        
    /**
     * Add given DObjectFacet to the cache maintained by this BaseComponent.
     *
     * @param aFacet the DObjectFacet to add to the cache
     * @exception NullFacetException is thrown if facet provided is null or has been revoked
     */
    public final void addFacetToCache(DObjectFacet aFacet)
        throws NullFacetException
    {
        if (aFacet == null || aFacet.isRevoked()) throw new NullFacetException();
        debug("BaseComponent.addFacetToCache.  DObject facet "+aFacet+" with id "+aFacet.getID()+" being added to facet cache for "+this);
        if (myFacetCache == null) {
            myFacetCache = new Hashtable();
        }
        myFacetCache.put(aFacet.getID(), aFacet);
    }

    /**
     * Remove the DObjectFacet associated with the given id from our facet cache
     *
     * @param id the DObject id of the facet to remove
     */
    public final void removeFacetFromCache(DObjectID id)
    {
        debug("BaseComponent.removeFacetFromCache.  Facet for "+id+" being removed from facet cache for "+this);
        if (myFacetCache != null) {
            myFacetCache.remove(id);
        }
    }

    /**
     * Get the DObjectFacet associated with the given id from our facet cache.  Will return
     * null if DObjectFacet for given id does not exist in this Component's facet cache.
     *
     * @param id the DObjectID of associated with the DObjectFacet we are looking for
     * @exception NullFacetException thrown if facet has been revoked
     */
    public final DObjectFacet getFacetForID(DObjectID id)
        throws NullFacetException
    {
        debug("BaseComponent.getFacetForID.  Looking for "+id+" within facet cache for "+this);
        if (myFacetCache == null) return null;
        
        DObjectFacet aFacet = (DObjectFacet) myFacetCache.get(id);
        if (aFacet == null) return null;

        if (aFacet.isRevoked()) {
            debug("BaseComponent.getFacetForID.  Facet corresponding to id "+id+" not found within facet cache for "+this);
            throw new NullFacetException();
        }
        debug("BaseComponent.getFacetForID.  Facet for id "+id+" found with facet "+aFacet+" for "+this);
        return aFacet;
    }

    /**
     * Get the DObjectID for the owner ComponentDObject of this BaseContainer component.
     *
     * @return DObjectID that is the id for our owner
     */
    protected final DObjectID getMyID()
    {
        return myOwner.getDObjectID();
    }
    
    /**
     * Determine whether our DObject owner is currently a client, or host.
     *
     * @return true if we are a client, false if host
     */
    protected final boolean isClient()
    {
        return myOwner.isClient();
    }
    
    /**
     * Get state of this component for delivery to new client.  The resulting object
     * must implement the Serializable interface, as it will be sent over the wire
     * to the remote client for initialization.  
     *
     * @param id the SessionViewID of the view where the client is being constructed
     * @return the Serializable object that contains the state to be sent to the 
     * new client.  This object will be passed into the constructor of the remote
     * client (last parameter) during the construction of the instance, and then passed
     * to the setInitialState method
     *
     * @see View#loadDObject
     * @see LoadingDObject
     * @see #setInitialState
     */
    protected Serializable getStateForClient(SessionViewID id)
    {
        debug("BaseComponent.getStateForClient.  Request to get state for client on "+id+" of component "+this);
        return null;
    }
    
    /**
     * Set initial state for this component.  This method is called 
     * inside the constructor for the DObject instance, and is passed the Serializable 
     * last parameter.  For this superclass version of the component, the 
     * implementation is to ignore the client state, because the above implementation 
     * of 'getStateForClient' simply provides null.  Subclasses that override this 
     * method should also appropriately override the 'BaseComponent.getStateForClient' 
     * method.
     *
     * @param params the Serializable object that is passed to this component upon
     * construction.  For this superclass, the value is expected to be null.
     * @see #getStateForClient
     */
    protected void setInitialState(Serializable params)
    {
        // As a default we will ignore this, since the default 'get
        debug("BaseComponent.setStateForClient.  Request to set state for component "+this+" with initial state "+params);
    }
    
    /**
     * Send string to debug output.  For now, System, eventually some more sophisticated
     * debugging support.
     *
     * @param aString the String to print out
     */
    public void debug(String aString)
    {
        if (debug) System.out.println(aString);
    }
}