/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/container/BaseContainer.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.container;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import java.io.Serializable;

import dom.session.ComponentDObject;
import dom.session.DObjectFacet;
import dom.session.Closure;
import dom.session.BaseFacet;
import dom.session.BaseComponent;

import dom.session.NullFacetException;
import dom.id.DObjectID;
import dom.session.SessionViewID;

import dom.util.TOSViolationException;
import dom.util.VectorEx;

/**
 * Root abstract class to represent the inter-DObject container-containable relation.
 * This class is used by the 'container' side of the container-containable
 * relation.  It produces an appropriate BaseContainerFacet, that can be
 * delivered to the BaseContainable, for the inter-DObject protocol to
 * establish containment.
 *
 * @see BaseContainerFacet
 * @see BaseContainable
 * @see BaseContainableFacet
 * @see ComponentDObject
 *
 * @author Scott Lewis
 */
public class BaseContainer extends BaseComponent {
    
    public static boolean debug = true;
    //public static boolean debug = false;
    
    // Host<->Client messages.  Each of these message strings corresponds to
    // a method name on this class (the handler for the message).  If any of
    // these strings are modified, the appropriate method name should also
    // be modified, so the message will continue to work
    
    // This message is sent client->host when a client has received a request
    // from a containable for an add
    public static String CLIENT_ADD_REQUEST_METHOD_NAME = "asynchAddReq";
    // This message is sent host->client in response to the message above
    public static String CLIENT_ADD_RESULT_METHOD_NAME = "asynchHostAddResult";
    // This messae is sent client->host when a client has received a request from a 
    // containable for a remove
    public static String CLIENT_REMOVE_REQUEST_METHOD_NAME = "asynchRemoveReq";
    // This message is sent host->client in response to the message above
    public static String CLIENT_REMOVE_RESULT_METHOD_NAME = "asynchHostRemoveResult";
    
    // Message sent Client->Host when a) the host sends a CLIENT_ADD_RESULT_METHOD_NAME 
    // to a client; b) the client cannot comply with the host's request to add a
    // containable.  When this message is received by the host, then the host should
    // do something about it (e.g. remove the containable that couldn't be added)
    public static String CLIENT_ADD_FAILURE = "asynchClientAddFailure";
    
    // The actual container variable.  This contains the mapping between a DObjectID (key)
    // and the BaseContainableFacet (value) that belongs to that id.  
    Hashtable myElements;
    // Hashtable of entities to notify about *other* things being added/removed
    // from this container.  By default, all entries in myElements are included in
    // notifications.
    Hashtable myNotify; 
    // The following vector is the vector of DObject ids that are delivered from
    // the host to the client.  For the host, this is always null.  For clients,
    // The host will package up the currently known contained ids, send it to
    // the client from within getStateForClient, and the client will get this
    // VectorEx as part of it's initialization in setInitialState.
    VectorEx myClientElements;
    
    /**
     * Constructor for a BaseContainer.  The two params are:  1) the ComponentDObject
     * that owns this functionality; 2) The unique identifier associated with this
     * instance.  The identifier is here so that the ComponentDObject owner can have
     * multiple instances of this class, and have some way to disambiguate them,
     * internal to the ComponentDObject (for example, one ComponentDObject might have
     * several of these BaseContainer instances to manage several different kinds of
     * containment semantics).
     *
     * @param owner the ComponentDObject that is to have the BaseContainer semantics
     * @param uniqueIdentifier the String that uniquely identifies the particular
     * instance of the BaseContainer *within the ComponentDObject*.  That is, since
     * this is code *internal* to the ComponentDObject owner, the owner must guarantee
     * uniqueness only internally...it does not have to guarantee uniqueness externally.
     * This is so that the ComponentDObject can do the appropriate message routing.
     *
     * @param owner the ComponentDObject owner of this this containment functionality
     * @param uniqueIdentifier a String the owner can use to disambiguate multiple
     * instance of the BaseContainer class.  Can be null, if (e.g.) the ComponentDObject
     * has only one instance of this class to work with.
     */
    public BaseContainer(ComponentDObject owner, String uniqueIdentifier)
    {
        super(owner, uniqueIdentifier);
        myElements = new Hashtable();
        myNotify = new Hashtable();
    }

////////////////////// Methods for adding containables to this container /////////////
    /**
     * Entry point method for adding a containable to this container.  This is the 
     * method that should be called when a message is sent to the owner DObject from
     * any containables that have been given our BaseContainerFacet.  Only receivers
     * of the BaseContainerFacet have the ability to send us this message.
     * <p>
     * The containable must also provide some 'magic' bits in the 'data' parameter.
     * This parameter can be used by subclasses of this class to implement arbitrary
     * TOS checking for containment.  See the method 'checkAddRequest' in this class.
     * <p>
     * If the owner of this functionality is a host, then the reception of this 
     * message will result in the a direct call to 'doHostAdd', which does the real
     * work of adding the given containable to our set of contained items
     *
     * @param containable the BaseContainableFacet we are being requested to add
     * @param data the Serializable data that the containable is passing in to satisfy
     * our TOS checks
     *
     * @see #checkAddRequest
     * @see #doHostAdd
     */
    public void asynchAdd(BaseContainableFacet containable, Serializable data)
    {
        debug("BaseContainer.add. containable is "+containable+" data is "+data);
        // If we're the host, then call doHostAdd directly
        if (!isClient()) {
            try {
                doHostAdd(containable, data, true);
            } catch (Exception e) {
                // Send fail info back to local containable
                hostAddFailed(containable.getID(), e);
            }
        } else {
            // We're a client, and we need to send request to host
            sendClientAddRequest(myOwner.getIDForView(), containable.getID(), data);
        }
    }

    /**
     * Method for client to send an 'add' request to the host instance of this
     * BaseContainable.  
     *
     * @param fromID the SessionViewID where the originating client is located
     * @param id the DObjectID of the containable to add
     * @param data any/all data to use for TOS checking by the host
     */
    protected void sendClientAddRequest(SessionViewID fromID, DObjectID id, Serializable data)
    {
        debug("BaseContainer.sendClientAddRequest.  Client sending add "+id+" request to host for "+this);
        try {
            myOwner.sendToHomeComponent(myIdentifier, getClientAddClosure(fromID, id, data));
        } catch (Exception e) {
            // Ignore.
            // XXX Spam debug information for now.  Eventually remove.
            debug("BaseContainer.sendClientAddRequest. Exception in send to remote components for container"+this+" and containable "+id);
        }
        
    }

    /**
     * Get the Closure for the client to send an 'add' message to the host instance
     * of this BaseContainer.  Subclasses may override this method and determine
     * for themselves which message is sent from client to host on an add request.
     *
     * @param fromID the SessionViewID where the initiating client is located
     * @param id the DObjectID of the BaseContainable to add
     * @param data any/all data to pass for TOS checking on the add
     * @return Closure that represents the message for the client to send to the host
     */
    protected Closure getClientAddClosure(SessionViewID fromID, DObjectID id, Serializable data)
    {
        return BaseFacet.createNewClosure(null, CLIENT_ADD_REQUEST_METHOD_NAME,
                                Closure.getObjectArrayFromParams(fromID, id, data));
    }

    /**
     * Message received by host (from client) to request an add.  This msg is
     * sent by clients, and should only be received by the host.  If received by
     * a client, it is *ignored* (may eventually have clients forward to the
     * process where they think the host is, to deal with host location changes)
     *
     * @param fromID the SessionViewID where the add request initiated.  If add not
     * successful, this is used to report failure back to requesting client (only)
     * @param containable the DObjectID of the containable to be added
     * @param data arbitrary data that is provided with request.  This can be
     * used by the host to validate the containment request (i.e. tos).
     */
    public void asynchAddReq(SessionViewID fromID, DObjectID containableID, Serializable data)
    {
        // First, check to verify that we are client.  
        // If not, forward on to who *we* think the host is.
        if (isClient()) {
            debug("BaseContainer.asynchAddReq.  Container client "+this+" got asynchAddReq from "+fromID+".  Forwarding on to who *we* think the host is");
            // Forward on to who we think is the host
            sendClientAddRequest(fromID, containableID, data);
            return;
        }
        debug("BaseContainer.asynchAddReq.  Got request to add "+containableID+" with data "+data+" to "+this);
        try {
            if (containableID == null) throw new NullFacetException();
            // Try to do the actual add here on the host (us)
            // This will either succeed or throw...no in between
            doHostAdd(getContainableFacetForID(containableID), data, true);
        } catch (Exception e) {
            // If exception is thrown during the add, then we must send failure
            // info back to originating client (only)
            debug("BaseContainer.asynchAddReq.  Exception "+e+" adding id "+containableID+" to "+this);
            sendAddFailResultToClientAtView(containableID, fromID, e);
        }
    }

    /**
     * Do the add on a host instance of BaseContainer.  This method is called either
     * directly (if the host gets a request directly to add a containable), or
     * via the cAddReq message handler above (request comes from client to us).
     * This method will either succeed, or throw an exception...there is no in between.
     *
     * @param containable the BaseContainableFacet that will be added.  This param is
     * checked to validate that it is non-null.  If null, a NullFacetException is thrown.
     * @param data any Serializable data that was sent along with the request.  This can
     * be used for TOS checks, etc.
     * @param updateClients a boolean indicating whether the code in this method should
     * update its clients.  This param is here so that it is possible for the host code
     * to operate host-only (no clients are updated).
     *
     * @exception Exception thrown if BaseContainableFacet turns out to be inaccessible,
     * we refuse to do the add because TOS checks not passed.
     */
    protected void doHostAdd(BaseContainableFacet containable, Serializable data, boolean updateClients)
        throws Exception
    {
        if (containable == null || containable.isRevoked()) throw new NullFacetException();
        // OK, proceed with add.  We are the host, containable is non-null
        debug("BaseContainer.doHostAdd.  With containable facet "+containable+" and data "+data+" on "+this);
        // First, check to be sure that containable passes our TOS restrictions, if any
        if (!checkAddRequest(containable, data)) {
            // TOS not passed.  Throw
            throw new TOSViolationException();
        } else {
            // Go ahead with things. This does the actual add.  The last parameter 
            // indicates whether or not we wish to have clients notified about update
            doTheAdd(containable, updateClients); 
        }    
    }

    /**
     * Actually do the add to this container locally.  This can be called by both host
     * and client code.  Client code should be sure to specify 'false' for the updateClients
     * parameter.  This method does the actual add of the given containable to this
     * container.  It verifies that the id associated with the containable facet is not
     * already present in container (throws ContainerException if id is already contained).
     * 
     *
     * @param containable the BaseContainableFacet being added
     * @param updateClients boolean indicating whether this add should result in 
     * client notification
     * @exception ContainerException thrown if some problem doing add
     */
    protected final void doTheAdd(BaseContainableFacet containable, boolean updateClients)
        throws ContainerException
    {
        // Real action here
        debug("BaseContainer. doTheAdd.  Containable: "+containable+" for container "+this);
        // First, call our local method for notification
        notifyBeforeAdd(containable);
        // Then get the thing's id from its facet
        DObjectID containableID = containable.getID();
        // Then check to be sure ID is not already in our set of contained items
        if (myElements.contains(containableID)) {
            // It's already there, so we can't continue, so we throw a ContainerException
            debug("BaseContainer.doTheAdd.  Failure to add "+containable+" to "+this+".  ID already present.");
            throw new ContainerException();
        } else {
            // Put elements in our hashtable.  After this, we officially contain
            // the containable. This is synchronized on the elements Hashtable *just 
            // in case* multiple threads might be able to get to this code (not expected, 
            // but just in case)
            synchronized (myElements) {
                myElements.put(containableID, containable);
                // Then we notify our clients (if appropriate)
                if (updateClients) {
                    sendAddSuccessResultToClients(containableID);
                }
                // Then do local notification of containable and other interested parties
                notifyAfterAdd(containable);
            }
            // Did it locally!
            debug("BaseContainer.doTheAdd.  Success in adding "+containable+" to "+this);
            // XXX TESTING
            System.out.println(showContents());
        }
    }
    
    /**
     * Method to send a result message to clients.  May only be sent from host.
     * Sends closure with method name CLIENT_ADD_RESULT_METHOD_NAME to clients
     *
     * @param containable the DObjectID of the containable that the client must
     * add
     */
    protected void sendAddSuccessResultToClients(DObjectID containable)
    {
        try {
            myOwner.sendToRemoteComponents(myIdentifier, getAddClosure(containable, null));
        } catch (Exception e) {
            // Ignore.
            // XXX Debug information for now.  Eventually remove
            debug("BaseContainer.sendAddSuccessResultToClients. Exception in send to remote components for container"+this+" and containable "+containable);
        }
    }

    /**
     * Method to send a fail result message back to the originating client.
     * May only be sent from host.  The method 'getAddClosure' is called to get the
     * closure to deliver back to the client.  The Exception associated with the
     * failure (result) is passed into the getAddClosure method...this method
     * will determine what message is actually sent back to the originating client
     * upon add failure.
     *
     * @param containable the DObjectID of the containable that failed to be admitted
     * @param toID the SessionViewID where this message is directed (the view that
     * originated the request)
     * @param result the Exception that occurred during host add, to be returned to the
     * client
     * @see #getAddClosure
     */
    protected void sendAddFailResultToClientAtView(DObjectID containable, SessionViewID toID, Exception result)
    {
        try {
            myOwner.sendToRemoteComponent(toID, myIdentifier, getAddClosure(containable, result));
        } catch (Exception e) {
            // Ignore.
            // XXX Debug information for now.  Eventually remove
            debug("BaseContainer.sendAddFailResultToClientAtView. Exception in send to remote components for container"+this+" and containable "+containable);
        }
    }

    /**
     * Get the Closure that will be sent to our remote client instances.  If the
     * 'result' parameter is null, it means that the add is successful, if non-null
     * the add was not successful.
     *
     * @param containableID the DObjectID of the containable just added
     * @param result the Exception result of the add.  If null, add was successful
     */
    protected Closure getAddClosure(DObjectID containableID, Exception result)
    {
        return BaseFacet.createNewClosure(null, 
                           CLIENT_ADD_RESULT_METHOD_NAME, 
                           Closure.getObjectArrayFromParams(containableID, result));
    }

    /**
     * Method called on client in response to Closure sent from BaseContainer host.  Message
     * is actually sent from host execution of sendAddSuccessResultToClients method above.
     *
     * @param containableID the id of the containable that the host has added (or not)
     * and notified us about.
     * @param result the Exception associated with the add described by this message.  If
     * null, means that add by host was successful.  If non-null, describes the failure.
     */
    public void asynchHostAddResult(DObjectID containableID, Exception result)
    {
        debug("BaseContainer.hostAddResult.  containableID is "+containableID+" and result is "+result);
        if (result == null) {
            // Add successful according to host.
            hostAddSuccessful(containableID);
        } else {
            // Add failed according to host.
            // We don't change our
            hostAddFailed(containableID, result);
        }
    }

    /**
     * Message handler on the client side.  This is the handler for a message sent
     * from the host to a client (us) when an add was successful.  If we cannot
     * find the facet to the BaseContainable that was added, we report the problem
     * back to our host via the sendClientAddFailure method.  If everything is OK,
     * we simply update our local cache of the containment state.
     *
     * @param containableID the DObjectID of the containable just added by the host
     */
    protected void hostAddSuccessful(DObjectID containableID)
    {
        // XXX this is a test to try our failure condition
        //sendClientAddFailure(containableID, new NullFacetException());
        try {
            // Do the add locally, do local notification, etc.
            // Last parameter prevents this code from updating clients.  This code
            // is client code, and host has already sent notification to all other
            // clients, so we don't need/want to
            doTheAdd(getContainableFacetForID(containableID), false);
        } catch (Exception e) {
            debug("BaseContainer.clientAddSuccessful.  ERROR in local add of "+containableID+ "for container "+this);
            // Facet not found or revoked!
            // This sends an error message back to host, and in response host should 
            // kick containable out
            sendClientAddFailure(containableID, e);
        }
    }

    /**
     * Message handler on client for dealing with message back from host that add
     * attempt failed.  This will be sent to us by our host if we initiated an
     * add request, and the add attempt failed on the host for whatever reason.
     *
     * @param containableID the DObjectID of the containable that was *not* added
     * @param result the Exception that was generated as part of the add failure
     */
    protected void hostAddFailed(DObjectID containableID, Exception result)
    {
        try {
            getContainableFacetForID(containableID).added(result, getAddedFacet(containableID, result));
        } catch (Exception e) {
            // In this case, the attempt to add failed.  If we can't get the
            // BaseContainableFacet for the target containable then they are out of
            // luck, and won't be notified of the failure.
            // So, in other words, we just ignore this failure
            // XXX Spam for now...just to test.  Can remove this later
            debug("BaseContainer.hostAddFailure.  Exception reporting failure to "+containableID);
        }
    }

    /**
     * This method is called if the host successfully adds a containable, and then
     * sends an add message to its clients...and one (or several) are unable to do
     * the actual add themselves.  In that case, (determined in the client's doTheAdd
     * code), the client will call this method to notify the host instance about 
     * the failure.
     *
     * @param containableID the DObjectID for the BaseContainable that we (client) are
     * having trouble with
     * @param e the Exception describing the client's problem
     */
    protected void sendClientAddFailure(DObjectID containableID, Exception e)
    {
        debug("BaseContainer.sendClientAddFailure.  Client add FAILED for containable "+containableID+" on container "+this+".  Sending notification to host");
        try {
            myOwner.sendToHomeComponent(myIdentifier, getClientAddFailureClosure(containableID, e));
        } catch (Exception except) {
            // Ignore.  If this happens, the we (as the client) should be going away anyway
            debug("BaseContainer.sendClientAddFailure. Exception "+except+" in client add failure message to host for container"+this);
        }
    }
    
    /**
     * Get the Closure that represents the message to our host, when a client fails
     * to accomplish an add.  This is called from within the sendClientAddFailure when
     * a client, upon request of the host, cannot comply with the host's request to
     * add a given containable to its set of contained items.  This will result in
     * the method corresponding to the string defined by CLIENT_ADD_FAILURE to be
     * invoked on the host.  The host must deal with this notification appropriately
     * (typically by removing the containable).
     *
     * @param containableID the DObjectID of the containable that failed
     * @param e the Exception generated on the client when the add was attempted
     * @return Closure that represents the CLIENT_ADD_FAILURE message to the host
     */
    protected Closure getClientAddFailureClosure(DObjectID containableID, Exception e)
    {
        return BaseFacet.createNewClosure(null, CLIENT_ADD_FAILURE,
                                Closure.getObjectArrayFromParams(myOwner.getIDForView(), containableID, e));
    }

    /**
     * This is the message handler on the host invoked when a client reports that it
     * was unable to accomplish an add request from us.  By default, this code removes
     * the given containable from us, so that their is no ambiguity about whether
     * the containable is contained or not.  This is critical such that we the host
     * can maintain consistency of the containment state of all of our clients.
     *
     * @param viewID the SessionViewID of the view where the client failed to be added
     * @param containableID the DObjectID of the containable that failed to be added
     * @param e the Exception that occurred on the client
     */
    public void asynchClientAddFailure(SessionViewID viewID, DObjectID containableID, Exception e)
    {
        debug("BaseContainer.asynchClientAddFailure.  Client "+viewID+" reported exception "+e+" in add of "+containableID+" to "+this);
        // The only thing we can do is a hard remove.  Subclasses may choose to 
        // override this behavior
        try {
            doHostRemove(getContainableFacetForID(containableID), true);
        } catch (Exception except) {
            debug("BaseContainer.asynchClientAddFailure.  Exception "+e+" trying to remove "+containableID+" from "+this);
        }
    }
    
    /**
     * Entry point method for removing a containable from this container.  This is the 
     * method that should be called when a message is sent to the owner DObject from
     * any containables that have been given our BaseContainerFacet.  Only receivers
     * of the BaseContainerFacet have the ability to send us this message.
     * <p>
     * If the owner of this functionality is a host, then the reception of this 
     * message will result in the a direct call to 'doHostRemove', which does the real
     * work of removing the given containable from our set of contained items
     *
     * @param containable the BaseContainableFacet we are being requested to remove
     *
     * @see #doHostRemove
     */
    public void asynchRemove(BaseContainableFacet containable)
    {
        debug("BaseContainer.remove. containable is "+containable);
        // If we're the host, then call directly
        if (!isClient()) {
            try {
                doHostRemove(containable, true);
            } catch (Exception e) {
                // Send fail info back to local containable
                // Should not happen, be here just in case subclasses wish to
                // override this behavior
                hostRemoveFailed(containable.getID(), e);
            }
        } else {
            sendClientRemoveRequest(myOwner.getIDForView(), containable.getID());
        }
    }
    
    /**
     * Method for client to send an 'remove' request to the host instance of this
     * BaseContainable
     *
     * @param fromID the SessionViewID where the originating client is located
     * @param id the DObjectID of the containable to remove
     */
    protected void sendClientRemoveRequest(SessionViewID fromID, DObjectID id)
    {
        debug("BaseContainer.sendClientRemoveRequest.  Client sending remove "+id+" request to host");
        try {
            myOwner.sendToHomeComponent(myIdentifier, getClientRemoveClosure(fromID, id));
        } catch (Exception e) {
            // Ignore.
            // XXX Debug information for now.  Eventually remove
            debug("BaseContainer.sendClientRemoveRequest. Exception in send to remote components for container"+this+" and containable "+id);
        }
        
    }
    
    /**
     * Get the Closure for the client to send a 'remove' message to the host instance
     * of this BaseContainer.  Subclasses may override this method and determine
     * for themselves which message is sent from client to host on a remove request.
     *
     * @param fromID the SessionViewID where the initiating client is located
     * @param id the DObjectID of the BaseContainable to add
     * @return Closure that represents the message for the client to send to the host
     */
    protected Closure getClientRemoveClosure(SessionViewID fromID, DObjectID id)
    {
        return BaseFacet.createNewClosure(null, CLIENT_REMOVE_REQUEST_METHOD_NAME,
                                Closure.getObjectArrayFromParams(fromID, id));
    }
    
    /**
     * Message received by host (from client) to request a remove.  This msg is
     * sent by clients, and should only be received by the host.  If received by
     * a client, it is *ignored* (may eventually have clients forward to the
     * process where they think the host is, to deal with host location changes)
     *
     * @param fromID the SessionViewID where the remove request initiated.  If remove not
     * successful, this is used to report failure back to requesting client (only)
     * @param containable the DObjectID of the containable to be added
     */
    public void asynchRemoveReq(SessionViewID fromID, DObjectID containableID)
    {
        // First, check to verify that we are the host.  
        // If not, forward on to who *we* think the host is.
        if (isClient()) {
            debug("BaseContainer.asynchRemoveReq.  Container client "+this+" got asynchRemoveReq from "+fromID+".  Forwarding on to who *we* think the host is");
            // Forward on to who we think is the host
            sendClientRemoveRequest(fromID, containableID);
            return;
        }
        debug("BaseContainer.asynchRemoveReq.  Got request to remove "+containableID+" from "+this);
        try {
            if (containableID == null) throw new NullFacetException();
            // Try to do the actual add here on the host (us)
            // This will either succeed or throw...no in between
            doHostRemove(getContainableFacetForID(containableID), true);
        } catch (Exception e) {
            // If exception is thrown during the add, then we must send failure
            // info back to originating client (only)
            debug("BaseContainer.asynchRemoveReq.  Exception "+e+" adding id "+containableID+" to "+this);
            sendRemoveFailResultToClientAtView(containableID, fromID, e);
        }
    }
    
    /**
     * Do the remove on a host instance of BaseContainer.  This method is called either
     * directly (if the host gets a request directly to remove a containable), or
     * via the cRemoveReq message handler above (request comes from client to us).
     * This method will either succeed, or throw an exception...there is no in between.
     *
     * @param containable the BaseContainableFacet that will be added.  This param is
     * checked to validate that it is non-null.  If null, a NullFacetException is thrown.
     * @param updateClients a boolean indicating whether the code in this method should
     * update its clients.  This param is here so that it is possible for the host code
     * to operate host-only (no clients are updated).
     *
     * @exception Exception thrown if BaseContainableFacet turns out to be inaccessible     */
    protected void doHostRemove(BaseContainableFacet containable, boolean updateClients)
        throws Exception
    {
        if (containable == null || containable.isRevoked()) throw new NullFacetException();
        // OK, proceed with remove.  We are the host, containable is non-null
        debug("BaseContainer.doHostRemove.  With containable facet "+containable+" from "+this);
        // Go ahead with things. This does the actual remove.  The last parameter 
        // indicates whether or not we wish to have clients notified about update
        doTheRemove(containable, updateClients); 
    }

    /**
     * Actually do the remove from this container locally.  This can be called by both 
     * host and client code.  Client code should be sure to specify 'false' for the 
     * updateClients parameter.  This method does the actual remove of the given 
     * containable from this container.  It verifies that the id associated with 
     * the containable facet is already present in container (throws 
     * ContainerException if id is not already contained).
     * 
     *
     * @param containable the BaseContainableFacet being remove
     * @param updateClients boolean indicating whether this remove should result in 
     * client notification
     * @exception ContainerException thrown if some problem doing remove
     */
    protected final void doTheRemove(BaseContainableFacet containable, boolean updateClients)
        throws ContainerException
    {
        // Real action here
        debug("BaseContainer. doTheRemove.  Containable: "+containable+" for container "+this);
        // First, call our local method for notification
        notifyBeforeRemove(containable);
        // Then get the thing's id from its facet
        DObjectID containableID = containable.getID();
        // Then check to be sure ID is not already in our set of contained items
        if (!myElements.containsKey(containableID)) {
            // It's not already there, so we can't continue, so we throw a ContainerException
            debug("BaseContainer.doTheRemove.  Failure to remove "+containable+" from "+this+".  ID already present.");
            throw new ContainerException();
        } else {
            // Remove element from our hashtable.  After this, we officially 
            // do not contain the containable. This is synchronized on the elements 
            // Hashtable *just in case* multiple threads might be able to get to 
            // this code (not expected, but just in case)
            synchronized (myElements) {
                myElements.remove(containableID);
                // Then we notify our clients (if appropriate)
                if (updateClients) {
                    sendRemoveSuccessResultToClients(containableID);
                }
                // Then do local notification of containable and other interested parties
                notifyAfterRemove(containable);
            }
            // Did it locally!
            debug("BaseContainer.doTheRemove.  Success in removing "+containable+" from "+this);
            // XXX TESTING
            System.out.println(showContents());
        }
    }

    /**
     * Actually do the remove from this container locally.  This can be called by both 
     * host and client code.  This cannot fail to remove the given DObjectID.
     *
     * @param containableID the DObjectID of the containable being removed
     * @param updateClients boolean indicating whether this remove should result in 
     * client notification
     */
    protected final void doHardRemove(DObjectID containableID, boolean updateClients)
    {
        // Real action here
        debug("BaseContainer. doHardRemove.  ContainableID: "+containableID+" for container "+this);
        // First, call our local method for notification
        notifyBeforeRemove(containableID);
        // Remove element from our hashtable.  After this, we officially 
        // do not contain the containable. This is synchronized on the elements 
        // Hashtable *just in case* multiple threads might be able to get to 
        // this code (not expected, but just in case)
        synchronized (myElements) {
            myElements.remove(containableID);
            // Then we notify our clients (if appropriate)
            if (updateClients) {
                sendRemoveSuccessResultToClients(containableID);
            }
            // Then do local notification of containable and other interested parties
            notifyAfterHardRemove(containableID);
        }
        // Did it locally!
        debug("BaseContainer.doHardRemove.  Success in removing "+containableID+" from "+this);
    }
    
    /**
     * Method to send a result message to clients.  May only be sent from host.
     * Sends closure with method name CLIENT_REMOVE_RESULT_METHOD_NAME to clients
     *
     * @param containable the DObjectID of the containable that the client must
     * remove
     */
    protected void sendRemoveSuccessResultToClients(DObjectID containable)
    {
        try {
            myOwner.sendToRemoteComponents(myIdentifier, getRemoveClosure(containable, null));
        } catch (Exception e) {
            // Ignore.
            // XXX Debug information for now.  Eventually remove
            debug("BaseContainer.sendRemoveSuccessResultToClients. Exception in send to remote components for container"+this+" and containable "+containable);
        }
    }
    
    /**
     * Method to send a fail result message back to the originating client.
     * May only be sent from host.  The method 'getRemoveClosure' is called to get the
     * closure to deliver back to the client.  The Exception associated with the
     * failure (result) is passed into the getRemoveClosure method...this method
     * will determine what message is actually sent back to the originating client
     * upon add failure.
     *
     * @param containable the DObjectID of the containable that failed to be removed
     * @param toID the SessionViewID where this message is directed (the view that
     * originated the request)
     * @param result the Exception that occurred during host remove, to be returned to the
     * client
     * @see #getRemoveClosure
     */
    protected void sendRemoveFailResultToClientAtView(DObjectID containable, SessionViewID toID, Exception result)
    {
        try {
            myOwner.sendToRemoteComponent(toID, myIdentifier, getRemoveClosure(containable, result));
        } catch (Exception e) {
            // Ignore.
            // XXX Debug information for now.  Eventually remove
            debug("BaseContainer.sendRemoveFailResultToClientAtView. Exception in send to remote components for container"+this+" and containable "+containable);
        }
    }
    
    /**
     * Get the Closure that will be sent to our remote client instances.  If the
     * 'result' parameter is null, it means that the remove was successful, if non-null
     * the remove was not successful.
     *
     * @param containableID the DObjectID of the containable just removed
     * @param result the Exception result of the remove.  If null, remove was successful
     */
    protected Closure getRemoveClosure(DObjectID containableID, Exception result)
    {
        return BaseFacet.createNewClosure(null, 
                           CLIENT_REMOVE_RESULT_METHOD_NAME, 
                           Closure.getObjectArrayFromParams(containableID, result));
    }

    /**
     * Method called on client in response to Closure sent from BaseContainer host.  Message
     * is actually sent from host execution of sendRemoveSuccessResultToClients method above.
     *
     * @param containableID the id of the containable that the host has removed (or not)
     * and notified us about.
     * @param result the Exception associated with the removed described by this message.  If
     * null, means that remove by host was successful.  If non-null, describes the failure.
     */
    public void asynchHostRemoveResult(DObjectID containableID, Exception result)
    {
        debug("BaseContainer.asynchHostRemoveResult.  containableID is "+containableID+" and result is "+result);
        if (result == null) {
            // Remove successful according to host.
            hostRemoveSuccessful(containableID);
        } else {
            // Remove failed according to host.
            hostRemoveFailed(containableID, result);
        }
    }
    
    /**
     * Message handler on the client side.  This is the handler for a message sent
     * from the host to a client (us) when a remove was successful.  
     *
     * @param containableID the DObjectID of the containable just removed by the host
     */
    protected void hostRemoveSuccessful(DObjectID containableID)
    {
        try {
            // Do the remove locally, do local notification, etc.
            // Last parameter prevents this code from updating clients.  This code
            // is client code, and host has already sent notification to all other
            // clients, so we don't need/want to
            doTheRemove(getContainableFacetForID(containableID), false);
        } catch (Exception e) {
            debug("BaseContainer.hostRemoveSuccessful.  Exception "+e+" in local remove of "+containableID+" for "+this);
        }
    }
    

    /**
     * Message handler on client for dealing with message back from host that remove
     * attempt failed.  This will be sent to us by our host if we initiated a
     * remove request, and the remove attempt failed on the host for whatever reason.
     * Actually, this should never happen, and should never be sent to us by the host,
     * but subclasses may wish to override this behavior, and have the opportunity to
     * do so.
     *
     * @param containableID the DObjectID of the containable that was *not* removed
     * @param result the Exception that was generated as part of the remove failure
     */
    protected void hostRemoveFailed(DObjectID containableID, Exception result)
    {
        try {
            getContainableFacetForID(containableID).removed(result, getRemovedFacet(containableID, result));
        } catch (Exception e) {
            // In this case, the attempt to remove failed.  If we can't get the
            // BaseContainableFacet for the target containable then they are out of
            // luck, and won't be notified of the failure.
            // So, in other words, we just ignore this failure
            // XXX Spam for now...just to test.  Can remove this later
            debug("BaseContainer.hostRemoveFailed.  Exception reporting failure to "+containableID);
        }
    }
            
    // Methods to send notifications of success/failure of add and remove
    
    /**
     * Method called by add routines that does notification *before* add has occurred.
     * The parameter notify may be null.
     *
     * @param containable the BaseContainableFacet that has been added to us.
     * If null, nothing will be added to the notification vector.
     */
    protected void notifyBeforeAdd(BaseContainableFacet containable)
    {
        debug("BaseContainer.notifyBeforeAdd for container "+this+" and containable "+containable);
    }

    /**
     * Method called by remove routines that does notification *before* remove
     * has occurred.
     *
     * @param containable the BaseContainableFacet that has been removed from us.
     */
    protected void notifyBeforeRemove(BaseContainableFacet containable)
    {
        debug("BaseContainer.notifyBeforeRemove for container "+this+" and containable "+containable);
    }

    /**
     * Method called by remove routines that does notification *before* remove
     * has occurred.
     *
     * @param containableID the DObjectID of the containable that has been removed from us.
     */
    protected void notifyBeforeRemove(DObjectID containableID)
    {
        debug("BaseContainer.notifyBeforeRemove for container "+this+" and containable with id "+containableID);
    }
    
    /**
     * Method called by add routines that does notification *after* add has occurred.
     *
     * @param containable the BaseContainableFacet that has been added to us.
     */
    protected void notifyAfterAdd(BaseContainableFacet containable)
    {
        debug("BaseContainer.notifyAfterAdd for container "+this+" and containable "+containable);
        // Send notification to containable itself that it has been added
        try {
            containable.added(null, getAddedFacet(containable.getID(), null));
        } catch (NullFacetException e) {
            // Ignore.  If we get a null facet exception on this access, it means that
            // this code is racing against the DObject owner for this facet *going away*.
            // In that case, we will be notified about the DObject's departure, and will
            // clean up in the notification message
        }
        
        // OK, we don't want any notifications to others throwing NullFacetExceptions,
        // and stopping our progress
        for(Enumeration e=myNotify.keys(); e.hasMoreElements(); ) {
            DObjectID id = (DObjectID) e.nextElement();
            BaseContainableFacet aFacet = (BaseContainableFacet) myNotify.get(id);
            try {
                // Send otherAdded message to containable with container id and with
                // the id of the containable just added
                aFacet.otherAdded(containable.getID());
            } catch (NullFacetException except) {
                // Ignore.  If this occurs, we're expecting to be notified about
                // the owner of this facet going away in a separate message.  We
                // will do facet clean up in response to that
            }
        }
        // Then put new containable in notification list for future notifications
        addToNotify(containable);
    }
    
    /**
     * Method called by remove routines that does notification *after* remove has occurred.
     *
     * @param containable the BaseContainableFacet that has been removed from us.
     */
    protected void notifyAfterRemove(BaseContainableFacet containable)
    {
        debug("BaseContainer.notifyAfterRemove for container "+this+" and containable "+containable);
        // Send notification to containable itself that it has been removed
        try {
            containable.removed(null, getRemovedFacet(containable.getID(), null));
        } catch (NullFacetException e) {
            // Ignore.  If we get a null facet exception on this access, it means that
            // this code is racing against the DObject owner for this facet *going away*.
            // In that case, we will be notified about the DObject's departure, and will
            // clean up in the notification message
        }
        
        // Then remove containable from notification list so it doesn't receive
        // any future notifications
        removeFromNotify(containable.getID());
        
        // OK, we don't want any notifications to others throwing NullFacetExceptions,
        // and stopping our progress, but if they *do* throw, we will ask the revoked 
        // exception handler to do something about it right away
        for(Enumeration e=myNotify.keys(); e.hasMoreElements(); ) {
            DObjectID id = (DObjectID) e.nextElement();
            BaseContainableFacet aFacet = (BaseContainableFacet) myNotify.get(id);
            try {
                // Send otherAdded message to containable with container id and with
                // the id of the containable just added
                aFacet.otherRemoved(containable.getID());
            } catch (NullFacetException except) {
                // Ignore.  If this occurs, we're expecting to be notified about
                // the owner of this facet going away in a separate message.  We
                // will do facet clean up in response to that
            }
        }
    }
    
    /**
     * Method called by remove routines that does notification *after* hard 
     * remove has occurred.
     *
     * @param containableID the DObjectID that has been removed from us.
     */
    protected void notifyAfterHardRemove(DObjectID containableID)
    {
        debug("BaseContainer.notifyAfterHardRemove for container "+this+" and containableID "+containableID);
        // Then put new containable in notification list for future notifications
        removeFromNotify(containableID);
        // OK, we don't want any notifications to others throwing NullFacetExceptions,
        // and stopping our progress, but if they *do* throw, we will ask the revoked 
        // exception handler to do something about it right away
        for(Enumeration e=myNotify.keys(); e.hasMoreElements(); ) {
            DObjectID id = (DObjectID) e.nextElement();
            BaseContainableFacet aFacet = (BaseContainableFacet) myNotify.get(id);
            try {
                // Send otherAdded message to containable with container id and with
                // the id of the containable just added
                aFacet.otherRemoved(containableID);
            } catch (NullFacetException except) {
                // Ignore.  If this occurs, we're expecting to be notified about
                // the owner of this facet going away in a separate message.  We
                // will do facet clean up in response to that
            }
        }
    }
    
    protected DObjectFacet getAddedFacet(DObjectID containableID, Exception error)
        throws NullFacetException
    {
        // This can be overridden to provide a facet back to containable when it
        // has been added to this container.  The default, however, is to not give
        // any more facets back to containable when containment is achieved, and
        // so we return null.  Subclasses may override as appropriate.
        return null;
    }

    protected DObjectFacet getRemovedFacet(DObjectID containableID, Exception error)
        throws NullFacetException
    {
        // This can be overridden to provide a facet back to containable when it
        // has been removed from this container.  The default, however, is to not give
        // any more facets back to containable it has been removed, and
        // so we return null.  Subclasses may override as appropriate.
        return null;
    }
    
    // Utilities for dealing directly with data structures
    
    /**
     * Add given BaseContainableFacet to our notification vector.
     *
     * @param notify the BaseContainableFacet to add to our notification vector.
     */
    private void addToNotify(BaseContainableFacet notify)
    {
        myNotify.put(notify.getID(), notify);
    }
    
    /**
     * Remove given BaseContainableFacet element from our notification
     * vector.
     *
     * @param notifyID the DObjectID of the BaseContainableFacet to remove.
     */
    private void removeFromNotify(DObjectID notifyID)
    {
        myNotify.remove(notifyID);
    }
    
    /**
     * Entry point method for TOS checks for adds to this container.  This method
     * is called doHostAdd method to do TOS check before adding given id with
     * provided data.
     *
     * @param containable the BaseContainableFacet of the containable to putatively be added
     * @param data any data that the id has sent along.  If we require some specific
     * information to approve containment, then it would be passed in this parameter.
     * May be null.
     * @return true if add is approved, false if rejected
     */
    protected boolean checkAddRequest(BaseContainableFacet containable, Serializable data)
    {
        // This is where add tos policy would go
        // Subclasses may override as appropriate
        return true;
    }
    
    /**
     * Utility method to get a particular type of DObjectFacet appropriate for
     * this Component (i.e. BaseContainableFacet).  Simply a wrapper for
     * BaseComponent.getFacetForID + class cast.
     *
     * @param id the DObjectID to find the facet for
     * @return BaseContainableFacet corresponding to given id.  Null if none
     * exists
     * @exception NullFacetException thrown if facet is bogus
     */
    protected BaseContainableFacet getContainableFacetForID(DObjectID id)
        throws NullFacetException
    {
        try {
            return (BaseContainableFacet) getFacetForID(id);
        } catch (ClassCastException e) {
            removeFacetFromCache(id);
            return null;
        }
    }
    
    // Debugging support
    
    protected String showContents()
    {
        String newString = "BaseContainer.SHOW CONTENTS:";
        boolean first = true;
        for(Enumeration e=myElements.keys(); e.hasMoreElements(); ) {
            if (!first) {
                newString += ",";
                first = false;
            }
            DObjectID id = (DObjectID) e.nextElement();
            newString += id;
        }
        return newString;
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
        // For the default (BaseContainer) case, we will automatically forcibly
        // remove the containable with the given id from our container.  Subclasses
        // may wish to override this behavior, to do other things upon detection
        // of an induced separation between the container (us) and its containable
        // identified by id.  
        debug("BaseContainer.handleDObjectGone.  ComponentDObject with id "+id+" has left according to BaseContainer "+this);
        if (id != null && myElements.containsKey(id)) {
            // If we are host, we must remove it
            debug("BaseContainer.handleDObjectGone.  ComponentDObject with id "+id+" being removed from container "+this);
            // Do the remove.  We *don't* update clients, because they will get
            // exit notification themselves, and are responsible for removing the
            // given containable with their own code
            doHardRemove(id, false);
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
            return new BaseContainerFacet(getOwner(), this);
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
        if (aFacet != null && !aFacet.isRevoked() && aFacet instanceof BaseContainableFacet) {
            // If we're a client, and we are expecting this containable (we already
            // contain it), then add it to our set, and remove it from the initial state vector
            if (isClient() && myClientElements != null) {
                DObjectID containableID = aFacet.getID();
                synchronized (myClientElements) {
                    if (myClientElements.contains(containableID)) {
                        debug("BaseContainer.checkFacetForCache.  Found containable we contain!  Container "+this+" adding "+containableID);
                        // Remove from list of ids we have not yet added
                        myClientElements.removeElement(containableID);
                        // Put in elements hashtable...we now officially contain it locally
                        myElements.put(containableID, aFacet);
                        // Add to notification, so if it is removed, notification occurs properly
                        addToNotify((BaseContainableFacet) aFacet);
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    protected Serializable getStateForClient(SessionViewID id)
    {
        // Get Vector of currently contained ids.  This will be the initial state
        // for our clients
        VectorEx vect = new VectorEx();
        vect.addAll(myElements.keys());
        return (Serializable) vect;
    }
    
    protected void setInitialState(Serializable initialState)
    {
        // This should be an instance of VectorEx. 
        // If it's not this will throw a ClassCastException, but the exception
        // will be caught in the ComponentDObject.setInitialState method
        // Set initial state vector
        myClientElements = (VectorEx) initialState;
        if (isClient()) {
            debug("BaseContainer.setInitialState.  Setting client elements vector to "+initialState);
        }
    }
    
    // End of overrides of BaseComponent class
    
}