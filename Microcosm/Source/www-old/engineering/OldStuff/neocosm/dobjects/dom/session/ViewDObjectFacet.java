/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ViewDObjectFacet.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;

import dom.net.*;
import dom.serial.*;
import dom.util.Enqueable;

import java.io.IOException;

/**
 * Facet that the View provides the DObject upon construction.  This is provided
 * so that the DObject has certain minimal capabilities provided by the View
 * (e.g.) getting it's id, querying about group membership, sending data
 * reliably and with FIFO ordering to our remote presences.  
 * <p>
 * The class is declared final, meaning that no subclasses can be created.  This
 * guarantees certain minimal functionality for the DObject that receives this
 * facet.
 *
 * @author Scott Lewis
 */
public final class ViewDObjectFacet
{
    private View myView;
    private DObject myDObject;
    private boolean revoked;

    /**
     * Constructor takes as a parameter the View instance that this facet protects. 
     * If this parameter is null, this facet cannot be created, and so throws a 
     * NullFacetException.
     * <p>
     * Declared protected so that only code in the dom.session package has access
     * to creating instances of this facet class.  Currently this is done only
     * in the DObject root class constructor.
     * <p>
     * Note that the method <i>setDObjectTarget()</i> is called by the DObject class constructor
     * to set the DObject that is the target for this facet.  Once set in the
     * DObject class constructor, it cannot be changed (it is settable only once).
     *
     * @param aView the View instance that this facet is a public interface for
     * @exception NullFacetException thrown if either parameter is null upon construction
     */
    protected ViewDObjectFacet(View aView)
        throws NullFacetException
    {
        if (aView == null) throw new NullFacetException();
        myView = aView;
        revoked = false;
    }
    
    /**
     * Set up DObject target for this facet class.  This method allows the DObject
     * initialization code to set the target DObject instance for this facet.  This
     * is called *once* from the DObject class constructor, and can only be called
     * successfully that one time.  Any subsequent attempts to reset the target
     * DObject for this facet will be ignored.
     *
     * @param theObject the DObject to set as this facet's target DObject
     */
    protected void setDObjectTarget(DObject theObject)
    {
        if (myDObject == null && !revoked) {
            myDObject = theObject;
        }
    }

    /**
     * Helper function to determine whether this facet is in a 'revoked' state
     * or not
     *
     * @return true if facet has been revoked, false otherwise
     */
    private boolean revoked()
    {
        return revoked;
    }
    
    /**
     * Public method to allow View (or anything else that has a handle on this
     * facet for that matter) to revoke this facet and make it inactive.  Normally,
     * this is called by the View, as part of its cleanup process when a DObject
     * is removed.
     *
     */
    public void revoke()
    {
        revoked = true;
        // Free up reference
        myDObject = null;
    }
    
    /**
     * Get the SessionViewID associated with this View facet.  This lets the receiving
     * DObject ask what the SessionViewID for the the local View instance.
     *
     * @return SessionViewID that is the unique id associated with the local View
     */
    public SessionViewID getID()
    {
        // Always allow this to be accessed
        return myView.getID();
    }
    
    /**
     * Allow receivers of this facet to ask whether the owner View of a facet is
     * a server or not (i.e. the machine that does message fanout).  This is only
     * informational...i.e. it allows the DObject to *ask*, but the underlying
     * View is not obligated to tell the truth (although I can't really think of
     * a reason why it wouldn't tell the truth).
     *
     * @return true if View is a server (message fanout machine), false otherwise
     * @exception NullFacetException thrown if this facet has been revoked
     */
    public boolean isServer() throws NullFacetException
    {
        if (revoked) throw new NullFacetException();
        return myView.isServer();
    }
    
    /**
     * Get a run queue that a DObject can use to execute its code.  This is called
     * by the DObject constructor, to get an Enqueable object so that Closures
     * can be put on this run queue.  This gives the View the power to determine 
     * the characteristics of the run queue that DObjects use to execute their code.
     * The View can determine if it is one run queue (and one thread) used for all 
     * DObjects or a separate run queue (and thread of execution) for every
     * DObject.  
     *
     * @return Enqueable for DObject to use.  Null is a possibility if request 
     * for Enqueable is refused by View
     *
     */
    protected Enqueable getDObjectRunQueue()
    {
        if (revoked()) return null;
        return myView.getDObjectRunQueue(myDObject);
    }
    
    /**
     * Send given Data object to all remote presences known to be in the group.
     *
     * @param data any Data to be sent to all presences
     * @exception IOException thrown if data cannot be sent (e.g. not in group 
     * at all)
     */
    public void sendDataToPresences(Data data)
        throws IOException
    {
        if (revoked()) return;
        myView.sendDataToPresences(new DObjectPacket(myDObject.getID(), data));
    }
    
    /**
     * Send given Data object to presence at given remote presence.  
     *
     * @param id the SessionViewID that we are directing the data to
     * @param data any Data to be sent to all presences
     * @exception IOException thrown if data cannot be sent (e.g. not in group
     * at all)
     */
    public void sendDataToPresenceAtView(SessionViewID id, Data data)
        throws IOException
    {
        if (revoked()) return;
        myView.sendDataToPresenceAtView(id, new DObjectPacket(myDObject.getID(), data));
    }
    
    /**
     * Send a create message from the local presence to a remote view.  This will
     * likely result in a remote client presence being created.
     *
     * @param id the SessionViewID to send the create request to
     * @param data the CreateDObjectData information needed to construct
     * the remote presence
     */
    public void sendCreateMsg(SessionViewID id, CreateDObjectData data) 
    {
        // If this facet has been revoked, then the create message is ignored
        if (revoked()) return;
        try {
            myView.sendCreateMsg(id, data);
        } catch (Exception e) {
            // This is OK, it means we may be racing with a group partition
            // So we will ignore except for debug
            myDObject.debug("ViewDObjectFacet.sendCreateMsg.  Exception sending create message, client for "+myDObject.getID()+" will not be created on view "+id);
        }
    }
        
    /**
     * Generic meta-interface for DObjects to get facets on the local view.
     * This interface can be used to make arbitrary requests of the local
     * view securely.  The untrusted subclass of this class cannot call the
     * view.getViewFacet method directly, but rather only through this
     * call.
     *
     * @param request a String identifying the desired service from the view
     * @param data arbitrary data associated with the request
     * @return BaseFacet that is a facet on the local view.  Can be null if
     * view does not wish to provide a facet for the service.
     */
    public BaseFacet getViewFacet(String request, Object data)
    {
        if (revoked()) return null;
        return myView.getViewFacet(myDObject, request, data);
    }
    
    // THE FOLLOWING METHODS ARE DECLARED PROTECTED AND FINAL, SO THAT *ONLY* THE
    // DOBJECT CLASS CODE MAY CALL THEM (UNTRUSTED DOBJECT SUBCLASSES MAY NOT).
    /**
     * Notification from DObject (parameter is provided as key to assure
     * identity), that DObject has received and processed the 'created'
     * message from the local view.  This is passed on to the view list
     * manager, so that it can deal with this notification as desired
     * (typically by making some other state change on the DObject).
     *
     */
    protected void notifyDObjectCreated()
    {
        myView.notifyDObjectCreated(myDObject);
    }
    
    /**
     * Notification from DObject (parameter is provided as key to assure
     * identity), that DObject has received and processed the 'activated'
     * message from the local view.  This is passed on to the view list
     * manager, so that it can deal with this notification as desired
     * (typically by making some other state change on the DObject).
     *
     */
    protected void notifyDObjectActivated()
    {
        myView.notifyDObjectActivated(myDObject);
    }
    
    /**
     * Notification from DObject (parameter is provided as key to assure
     * identity), that DObject has received and processed the 'deactivated'
     * message from the local view.  This is passed on to the view list
     * manager, so that it can deal with this notification as desired
     * (typically by making some other state change on the DObject).
     *
     */
    protected void notifyDObjectDeactivated()
    {
        myView.notifyDObjectDeactivated(myDObject);
    }
    
    /**
     * Notification from DObject (parameter is provided as key to assure
     * identity), that DObject has received and processed the 'destroyed'
     * message from the local view.  This is passed on to the view list
     * manager, so that it can deal with this notification as desired
     * (typically by making some other state change on the DObject).
     *
     */ 
    protected void notifyDObjectDestroyed()
    {
        myView.notifyDObjectDestroyed(myDObject);
    }
    
    /**
     * Method for DObjects to make requests on other DObjects for capabilities.
     * This method serves as the meta interface for all DObject capability
     * negotiation.
     *
     * @param id the DObjectID of the DObject we wish to make the request of (grantor)
     * @param requestName the request name (cannot be null)
     * @param data any Object representing data specific to the request
     * @exception Exception thrown under different failure conditions
     * @see View#requestDObjectCapability
     * @see DObject#requestCapability
     * @see ViewDObjectFacet#receiveCapability
     * @see View#giveDObjectCapability
     * @see DObject#receiveCapability
     */
    protected void requestCapability(DObjectID target, String requestName, Object data)
        throws Exception
    {
        // Do checks on parameters
        if (target == null || requestName == null) throw new NullPointerException();
        myView.requestDObjectCapability(myDObject, target, requestName, data);
    }
    
    /**
     * Method called by DObject that is *granting* a capability request, if it
     * chooses to do so.  DObjects will receive requests for capabilities
     * (via the View#requestDObjectCapability method).  If capabilities are
     * granted in response to these requests, the grantor should:
     *
     * a) Create an instance of a DObjectFacet (or some subclass)
     * b) Call the receipt.setFacet(DObjectFacet) method to set the 
     * facet for the given receipt
     * c) Call this method on its ViewDObjectFacet
     *
     * This method will then call the View.receiveDObjectCapability method,
     * which will, in turn, deliver the contained DObjectFacet back to the
     * the original requestor (if they are still there)
     *
     * @param aReceipt the CapabilityReceipt originally provided the grantor
     * from the View.  If null, a NullPointerException will be thrown
     * @exception Exception thrown under different failure conditions
     */
    protected void receiveCapability(CapabilityReceipt aReceipt)
        throws Exception
    {
        if (aReceipt == null) throw new NullPointerException();
        myView.giveDObjectCapability(aReceipt);
    }
    
    /**
     * Interface for DObjects to request that they be added as listeners for other
     * DObject arrival.  This is invoked by the method DObject.addDObjectListener
     * and it in turn invokes the method on the underlying View object.
     *
     */
    protected void addDObjectListener()
    {
        myView.addDObjectListener(myDObject);
    }
    
    /**
     * Interface for DObjects to request that they be removed as listeners for other
     * DObject arrival/departure.  This is invoked by the method DObject.removeDObjectListener
     * and itin turn invokes the method on the underlying View object.
     *
     */
    protected void removeDObjectListener()
    {
        myView.removeDObjectListener(myDObject);
    }
    
    /**
     * Destroy the DObject that has access to this facet.  This is the method
     * that a DObject calls on its ViewDObjectFacet to permanently destroy
     * itself.  After calling this, the DObject will eventually die.
     *
     * @exception Exception thrown if some problem with eliminating DObject
     */
    protected final void recycleDObject() throws Exception
    {
        if (revoked()) return;
        myView.destroyDObject(myDObject.getID());
    }
    
}

