/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/facet/DObjectViewFacet.java $
    $Revision: 1 $
    $Date: 1/22/98 10:00a $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.session.*;
import dom.id.*;
import dom.serial.*;

/**
 * Facet that the new DObject presence provides to the local View object
 * upon construction.  It allows the View to send notifications about
 * activation/deactivation/death/etc. to the DObject asynchronously.
 * It is only given out to the View, and only used by the View.
 *
 * @see DObject
 * @see View#createDObject
 *
 * @author Scott Lewis
 */
public class DObjectViewFacet extends BaseFacet {

    private ViewDObjectFacet myViewFacet;

    public static final Class DOBJECT_CLASS = DObject.class;

    /** State Change Messages From View Object **/
    /** All of these have a single parameter, of type ViewNotifyInfo **/
    public static final String CREATE_METHOD_NAME = "create";
    public static final String ACTIVATE_METHOD_NAME = "activate";
    public static final String DEACTIVATE_METHOD_NAME = "deactivate";
    public static final String DESTROY_METHOD_NAME = "destroy";
    
    /** Replication Messages from View Object **/
    /** The createClient message takes a single parameter, of type ClientCreateInfo **/
    public static final String CREATECLIENT = "createClient";
    /** This message takes a single parameter, of type ClientCreateResultInfo **/
    public static final String CLIENTCREATIONDONE = "clientCreationDone";
    /** This message takes two parameters, the SessionViewID and the Data **/
    /** This is the message that receives all data from remote presences **/
    public static final String RECEIVEDATAFROMREMOTE = "receiveDataFromRemote";
    /** This message is sent when another DObject requests a capability from our
     * owner
     */
    public static final String REQUESTCAPABILITY = "requestCapability";
    public static final String RECEIVECAPABILITY = "receiveCapability";
    
    public static final String OTHERACTIVATED = "otherActivated";
    public static final String OTHERDEACTIVATED = "otherDeactivated";

    /**
     * Constructor for session msg facets.
     *
     * @param aSession the ViewDObjectFacet for the View that is sending the message.
     * @exception NullFacetException thrown if some aSession or owner are null
     */
    public DObjectViewFacet(ViewDObjectFacet viewFacet, DObject owner) throws NullFacetException
    {
        super(owner);
        if (viewFacet == null) throw new NullFacetException();
        myViewFacet = viewFacet;
    }

    /**
     * Revoke this object's reference to its owner.  This functionality is
     * provided without question to any object that has access to this type
     * of facet on the owner unum...this facet should therefore *only* be
     * delivered to the View that is allowed to do this revocation
     * (and must be able to).
     *
     * @exception NullFacetException thrown if this facet has already been
     * revoked
     */
    public void revoke() throws NullFacetException
    {
        super.revoke(getOwner());
    }

    /**
     * Utility for subclasses to use to give Closure to our owner.  Owner can decide
     * how to handle asynch invocation from View.
     *
     * @param closure the Closure to pass to our owner.
     * @exception Exception thrown if some problem in delivery
     */
    public void realAsynchInvokeMethod(Closure closure)
        throws Exception
    {
        exceptionIfRevoked();
        getOwner().receiveAsynchViewMsg(myViewFacet, closure);
    }

    /**
     * Utility for subclasses to use to give Closure to our owner.  Owner can decide
     * how to handle synch invocation from View.
     *
     * @param closure the Closure to pass to our owner.
     * @exception Exception thrown if some problem in delivery
     */
    private void realSynchInvokeMethod(Closure closure)
        throws Exception
    {
        exceptionIfRevoked();
        getOwner().receiveSynchViewMsg(myViewFacet, closure);
    }

    /**
     * Determine if underlying DObject is currently a client presence or not
     *
     * @return true if underlying DObject is a client presence, false otherwise
     * @exception NullFacetException thrown if this facet has already been
     * revoked
     */
    public boolean isClientPresence()
        throws NullFacetException
    {
        exceptionIfRevoked();
        return getOwner().isClientPresence();
    }

    /**
     * Tell DObject that it has been successfully created.
     *
     * @param notifyInfo an instance of DObjectNotifyInfo that can contain
     * arbitrary information from the View sending this message
     * @exception Exception thrown if (e.g.) this facet has already been
     * revoked
     */
    public void create(ViewNotifyInfo notifyInfo)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, CREATE_METHOD_NAME, Closure.getObjectArrayFromParam(notifyInfo)));
    }

    /**
     * Tell DObject that it has been successfully activated.
     *
     * @param notifyInfo an instance of DObjectNotifyInfo that can contain
     * arbitrary information from the View sending this message
     * @exception Exception thrown if (e.g.) this facet has already been
     * revoked
     */
    public void activate(ViewNotifyInfo notifyInfo)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, ACTIVATE_METHOD_NAME, Closure.getObjectArrayFromParam(notifyInfo)));
    }

    /**
     * Tell DObject that it has been successfully deactivated.
     *
     * @param notifyInfo an instance of DObjectNotifyInfo that can contain
     * arbitrary information from the View sending this message
     * @exception Exception thrown if (e.g.) this facet has already been
     * revoked
     */
    public void deactivate(ViewNotifyInfo notifyInfo)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, DEACTIVATE_METHOD_NAME, Closure.getObjectArrayFromParam(notifyInfo)));
    }

    /**
     * Tell DObject that it has been successfully destroyed.
     *
     * @param notifyInfo an instance of DObjectNotifyInfo that can contain
     * arbitrary information from the View sending this message
     * @exception Exception thrown if (e.g.) this facet has already been
     * revoked
     */
    public void destroy(ViewNotifyInfo notifyInfo)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, DESTROY_METHOD_NAME, Closure.getObjectArrayFromParam(notifyInfo)));
    }

    /**
     * Tell DObject that it should make a client instance of itself.
     *
     * @param notifyInfo an instance of CreateClientInfo with info
     * necessary to create a remote client
     * @exception Exception thrown if (e.g.) this facet has already been
     * revoked
     */
    public void createClient(CreateClientInfo notifyInfo)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, CREATECLIENT, Closure.getObjectArrayFromParam(notifyInfo)));
    }

    /**
     * Receive some data from a remote.  This method is called by the View to
     * deliver a DObjectPacket to us from a remote.
     *
     * @param fromID the SessionViewID of the presence that sent this
     * data
     * @param data the Data that was sent
     * @exception Exception thrown if some problem handling this message
     */
    public void receiveDataFromRemote(SessionViewID fromID, Data data)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, 
                                                RECEIVEDATAFROMREMOTE, 
                                                Closure.getObjectArrayFromParams(fromID, data)));
    }
    
    /**
     * Method called by View (only) to deliver a request for capability
     * from another DObject
     *
     * @param receipt the CapabilityReceipt from the View
     * @exception Exception thrown if some problem handling this msg
     * @see View#requestDObjectCapability
     * @see DObjectViewFacet#requestCapability
     */
    public void requestCapability(CapabilityReceipt receipt)
        throws Exception
    { 
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, REQUESTCAPABILITY,
                                                Closure.getObjectArrayFromParam(receipt)));
    }
    
    /**
     * Method called by View (only) to respond to *our* request for a capability
     * from another DObject (via ViewDObjectFacet.requestDObjectCapability)
     *
     * @param aFacet DObjectFacet that View is returning to us from the grantor
     * Can be null, if grantor does not wish to give anything back to us.
     * @exception Exception thrown if some problem handling this message
     * @see DObjectViewFacet#receiveCapability
     * @see View#giveDObjectCapability
     */
    public void receiveCapability(DObjectFacet aFacet)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, RECEIVECAPABILITY,
                                               Closure.getObjectArrayFromParam(aFacet)));
    }
    
    /**
     * Message sent from View to DObject to let it know that another DObject has
     * been activated.  This message is only sent if the DObject has explicitly
     * requested receiving this information by calling the DObject.addDObjectListener
     * method.
     *
     * @param otherID the DObjectID of the other DObject that has been activated
     * @exception Exception thrown if one of several things go wrong
     * @see DObject#addDObjectListener
     */
    public void otherActivated(DObjectID otherID)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, OTHERACTIVATED,
                                                Closure.getObjectArrayFromParam(otherID)));
    }

    /**
     * Message sent from View to DObject to let it know that another DObject has
     * been deactivated.  This message is only sent if the DObject has explicitly
     * requested receiving this information by calling the DObject.addDObjectListener
     * method.
     *
     * @param otherID the DObjectID of the other DObject that has been deactivated
     * @exception Exception thrown if one of several things go wrong
     * @see DObject#addDObjectListener
     */
    public void otherDeactivated(DObjectID otherID)
        throws Exception
    {
        exceptionIfRevoked();
        realAsynchInvokeMethod(createNewClosure(DOBJECT_CLASS, OTHERDEACTIVATED,
                                                Closure.getObjectArrayFromParam(otherID)));
    }

}
