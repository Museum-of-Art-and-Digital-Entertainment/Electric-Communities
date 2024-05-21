/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/DObject.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;
import dom.util.*;

import dom.serial.*;

import java.net.URL;
import java.io.*;

/**
 * Root class for all distributed objects (objects that live the context of
 * the multipoint group defined by the View class).  This class defines the basic 
 * functionality for all distributed objects.  It allows the containing <b>View</b> 
 * to have some guaranteed functionality (via combination of a classloader that 
 * guarantees that the local representation of this class is loaded from local 
 * disk and final methods in this class).  
 * <p>
 * To create a new distributed object that is a subclass of this class, the 
 * programmer simply needs to 
 * <p>
 * a) Write a valid subclass of this class; <p>
 * b) Override any of the followng methods, as desired:<p>
        <b>constructor</b>:  Called synchronously by View when the object presence is constructed.<p>
        <b>initialized</b>:  Called synchronously by the DObject constructor
        <b>setInitialState</b>:  Called synchronously by DObject constructor.  Called
        with Serializable value from DObject class constructor final parameter
        <b>activated(ViewNotifyInfo notifyInfo)</b>:  Msg sent to DObject by the View 
        (asynchronously) when the DObject has been activated.<p>
        <b>deactivated(ViewNotifyInfo notifyInfo)</b>:  Msg sent to DObject by the View
        (asynchronously) when the DObject has been deactivated. <p>
        <b>otherActivated, otherDeactivated</b>:  Msgs sent to DObject by View to notify
        this DObject of the arrival/departure of other DObjects.  <b>Only sent if 
        DObject subclass makes call to DObject.addDObjectListener()</b>.<p>
        <b>requestCapability(CapabilityReceipt)</b>:  Msg sent to us asynchronously by View
        to request a capability of us, on the behalf of a 'requestor DObject' that has
        initiated a request with us via its own sendRequestForCapability.  In response
        to this message, subclasses can choose to provide capabilities to other 
        DObjects, or not as dictated by the request.<p>
        <b>receiveCapability(DObjectFacet)</b>:  Msg sent to us asynchronously by View to give 
        us a capability from another DObject.  Override this to handle 
        subclass-specific capability exchanges with other DObjects.  Usually, this 
        message will be sent by the View only if initiated by us with a 
        call to sendRequestForCapability.<p>
        <b>createClient(CreateClientInfo)</b>:  Msg sent to us asynchronously by View to let
        us know that group membership has changed, and that we may wish to create
        a remote client on the new group member.  DObject.createClient does create
        a remote client by default, but this behavior may be overridden by the
        subclass handling this message.<p>
        <b>getClientClassName(SessionViewID id)</b>:  This method is called by the 
        DObject.createClient method to get the class name used to construct the
        remote client presence.  Subclasses may override this to specify a particular
        class for this DObject's client presences<p>
        <b>getStateForClient(SessionViewID id)</b>:  This method is called by the
        DObject.createClient method to get the Serializable state to be sent to the
        client presence for initialization.  Subclasses may override to specify
        any initial state for the remote client presence.<p>
        <b>createClientDone(SessionViewID fromID, Exception e)</b>:  Msg sent asynchronously
        from View to let us know that the remote client creation has completed 
        (either successfully or unsuccessfully as determined by the value of the
        second parameter).  Subclasses may override this to deal with this 
        notification in any way desired.<p>
 * c) use the View.createDObject method at run time to actually create the
 *    host presence of the new object.  See the View.createDObject method for details<p>
 * <p>
 * Note this class has a number of final methods.  These are so that local
 * runtime can guarantee that untrusted DObjects (that *must* use this class as
 * it's superclass...guaranteed by construction) have certain contractually
 * required functionality...since the <b>View</b> expects certain behavior
 * out of the DObjects, then this class guarantees that it will get it.
 *
 * @see View
 * @see View#createDObject
 * @see Closure
 * @author Scott Lewis
 */
public abstract class DObject {

    //public static boolean debug = true;
    public static boolean debug = false;
    
    // This is our globally unique identifier, and our publicly available
    // 'identifacet'.  It is given out freely to everyone, and conveys no real
    // authority over us.  This is immutably set by the enclosing View upon construction.
    private DObjectID myID;
    // This is the unique identifier for the View that has the host instance of this
    // dobject.  Set initially upon construction by the View.
    private SessionViewID myHomeViewID;
    // This is the net location of the default codebase for this DObject.  If null, it 
    // means that the class for representing this object (e.g. for remote clients) will
    // be loaded with the null classloader.  If non-null, it indicates *where* to load
    // this object's classes from.  Future use.
    private URL myCodeBase;
    // This is the facet that the View passes to us upon construction
    private DObjectViewFacet myAccessFacet;
    // This is the facet we provide to the View upon construction
    private ViewDObjectFacet mySessionView;
    // Indication of this DObject's current state (see static variables below)
    private int myState;
    // Message run queue for this object
    // The message queue is requested of the View upon construction (see constructor below)
    private Enqueable myQueue;
    /**
     * States any instance (or subclass instance) of DObject can be in
     * according to the local view.  The local view determines what
     * state we are in.
     */
    public static final int CONSTRUCTED_STATE = 0;
    public static final int INACTIVE_STATE = 1;
    public static final int ACTIVE_STATE = 2;
    public static final int DESTROYED_STATE = 3;
    
    public static final String ACTIVATE_OVERRIDEABLE = "activated";
    public static final String DEACTIVATE_OVERRIDEABLE = "deactivated";
    public static final String RECYCLE_CLIENT = "recycleClient";
    
    public static final String DEFAULT_FACET_REQUEST = "defaultFacet";

    /**
     * This constructor is declared protected so that only a) subclasses
     * and b) the View class (within this package) can use it.  This makes
     * it impossible for hostile code (guaranteed not to be from this
     * package), from creating valide (according to the View) instances of 
     * DObject's directly.
     *
     * @param viewFacet a ViewDObjectFacet that defines our local runtime context.
     * If this parameter is null, an InstantiationException is thrown.
     * @param newID a DObjectID that is the new id for this DObject.
     * If this parameter is null, an InstantiationException is thrown.
     * @param homeID a SessionViewID that is the home view id for this DObject
     * If this parameter is null, an InstantiationException is thrown.
     * @param codeBase an URL that identifies the (local or remote) codebase
     * for this DObject.  This parameter may be null.
     * @param params an Object that is used to initialize any subclasses
     * @exception InstantiationException is thrown if any of the critical
     * parameters are null.
     */
    protected DObject(ViewDObjectFacet viewFacet,
                      DObjectID newID,
                      SessionViewID homeID,
                      URL codeBase,
                      Serializable params) throws InstantiationException
    {
        if (viewFacet == null || newID == null || homeID == null)
            throw new InstantiationException("Invalid arguments to DObject constructor");
        // Set primary state variables
        myID = newID;
        myHomeViewID = homeID;
        myCodeBase = codeBase;
        myState = CONSTRUCTED_STATE;
        mySessionView = viewFacet;
        // Now set ourselves as the target on the ViewDObjectFacet
        mySessionView.setDObjectTarget(this);
        // Build our facet to view, and get our run queue
        // from View (note this run queue can be shared, or just for us...it's
        // up to the View to decide
        try {
            // Create facet that we will give to our view so it can access *us*
            // So that it can send us membership notifications, etc.
            myAccessFacet = new DObjectViewFacet(mySessionView, this);
            // Get run queue instance for us.  This should also start (or use an
            // existing) thread in the single run queue case to process messages 
            // that are put on this queue
            myQueue = mySessionView.getDObjectRunQueue();
            if (myQueue == null) throw new InstantiationException("View provided null run queue");
        } catch (NullFacetException e) {
            // can't continue so throw
            throw new InstantiationException("Can't create facets for DObject.  Exception is "+e.getMessage());
        }
        // Call initialize method
        initialize();
        // Provide last parameter to set initial state
        setInitialState(params);
    }

    /**
     * Initialize this instance.  This is called in the DObject constructor, and
     * it allows subclasses to call arbitrary initialization code.  The implementation
     * for this class is to do nothing, as all critical initialization is done
     * within the DObject constructor
     *
     * @exception InstantiationException can be thrown if some problem with
     * initialization
     */
    protected void initialize() throws InstantiationException
    {
        debug("DObject.initialize.  "+getID()+" being initialized");
        // Do nothing.  Subclasses may override as appropriate for initialization
    }
    
    /**
     * Set initial state.  This method is called inside the constructor
     * for DObject instance (host and client), and is passed the Serializable 
     * last parameter.  If this is a client, the host method 'getStateForClient' 
     * determines what the parameter actually is, and is  sent over the wire in
     * the construct message.  For this superclass, the implementation of this
     * method is to ignore the client state, because the above implementation of 
     * 'getStateForClient' simply provides null.  Subclasses that override this 
     * method should also probably override the 'getStateForClient' method.
     *
     * @param params the Serializable object that is passed to this method
     * by the constructor.  For this superclass, the value of params is null.
     * @exception InstantiationException can be thrown if some problem with
     * setting the initial state
     * @see #getStateForClient
     */
    protected void setInitialState(Serializable params) throws InstantiationException
    {
        // Since the getStateForClient call above returns null, we don't do
        // anything with this value.  Subclasses should override as 
        // appropriate for initialization
        debug("DObject.setInitialState.  Request to set state for client for "+getID()+" with initial state "+params);
    }

    // Basic methods available to all
    
    /**
     * Get the DObjectID identity for this DObject.  Declared final so
     * that local View can guarantee that the identity that was set on
     * construction is used in subsequent calls to getID().  Guarantees
     * that once the relationship is defined between a DObjectID and
     * a DObject (upon construction) that the local View and all other
     * views in the group can use the DObjectID as a unique key for 
     * the DObject instance.
     *
     * NOTE:  DObjectID is opaque, and currently is just a GUID.  It could
     * and should be something more substantial...i.e. a certified SturdyRef or
     * etc...basically it could be an identity with all the functionality of
     * the microcosm identity ingredient.
     *
     * @return DObjectID that uniquely identifies this DObject.
     */
    protected final DObjectID getID()
    {
        return myID;
    }
    /**
     * Get the SessionViewID identity for this DObject's 'home' View.
     * Declared final to guarantee that the 'home' identity set on construction
     * is used in subsequent calls to getHomeID().
     *
     * @return SessionViewID that uniquely identifies this DObject's 'home' view
     *
     */
    protected final SessionViewID getHomeID()
    {
        return myHomeViewID;
    }
    /**
     * Get the url that identifies the codebase for this DObject.  This
     * is declared final so that the local view can guarantee that this
     * code is executed when called.
     *
     * @return URL the URL that represents the codebase for this DObject
     */
    protected final URL getCodeBase()
    {
        return myCodeBase;
    }
    /**
     * Get the View known to this DObject.  Declared final so
     * that local view can guarantee that the View set on construction
     * is used in subsequent calls to getSessionView().  
     *
     * @return the ViewDObjectFacet that is the facet to the local
     * view for this DObject
     */
    protected final ViewDObjectFacet getSessionView()
    {
        return mySessionView;
    }
    /**
     * Get the View id where this instance of the DObject is.  Does not have
     * to be the same as the home id...if not, this is a client instance.
     *
     * @return SessionViewID that is the id for the local View
     */
    protected final SessionViewID getViewID()
    {
        return mySessionView.getID();
    }
    /**
     * Get the DObjectViewFacet for this DObject.  This method is called
     * by the local view to get an initial facet for the DObject being created.
     * It allows the local view to have an appropriate level of control (defined
     * by the DObjectViewFacet semantics) over the DObject being created
     * (i.e. the DObject can be removed, deactivated, etc., etc.).
     *
     * @return DObjectViewFacet that allows the local View to
     * appropriately manipulate this DObject
     * @exception NullFacetException thrown if facet not accessible
     */
    protected final DObjectViewFacet getAccessFacet()
        throws NullFacetException
    {
        return myAccessFacet;
    }
    /**
     * Determine whether this instance of this DObject is currently a client
     * or a host presence.  Declared final so that local view can guarantee
     * that this code is executed when isClient is called.
     *
     * @return true if this instance is a client presence, false otherwise.
     */
    protected final boolean isClientPresence()
    {
        return (!getHomeID().equals(getSessionView().getID()));
    }
    /**
     * Revoke our view access facet.  This is called when this object gets notification
     * that it has been destroyed.  It nulls out any reference to this DObject to
     * allow it to be garbage collected.
     */
    private void revokeAccessFacets()
    {
        // Revoke our facet
        try {
            myAccessFacet.revoke();
        } catch (NullFacetException e) {}
        
        // Revoke our View's facet
        // This means no more interaction with our View will be possible
        mySessionView.revoke();
    }

// Methods to deal with messages from View

    /**
     * Receive an asynchronous message from the local View.  This is declared
     * final so that the local View can guarantee the behavior of this
     * method.  This method should enqueue the given Closure on some run
     * queue for later execution.  It should not execute the given Closure
     * directly.
     *
     * @param viewFacet the ViewDObjectFacet sending the message
     * @param closure the Closure representing the message
     */
    protected final void receiveAsynchViewMsg(ViewDObjectFacet viewFacet, Closure closure)
    {
        // Just enqueue message on run queue provided to use by the local View
        myQueue.enqueue(closure);
    }

    /**
     * Receive a synchronous message from the local View.  This is declared
     * final so that the local View can guarantee the behavior of this
     * method.  This method should execute the given Closure synchronously.
     * NOTE:  NOT CURRENTLY USED.  ALL MESSAGES FROM VIEW ARE CURRENTLY DELIVERED
     * ASYNCHRONOUSLY
     *
     * @param viewFacet the ViewDObjectFacet sending the message
     * @param closure the Closure representing the message
     */
    protected final void receiveSynchViewMsg(ViewDObjectFacet viewFacet, Closure closure)
    {
        if (viewFacet != null && viewFacet.getID().equals(getSessionView().getID())) {
            try {
                runClosure(closure);
            } catch (Exception e) {
                // This should not happen, as View should never be sending us messages
                // that we don't know about.  The following is for debugging.
                dumpStack(e, "DObject.receiveSynchViewMsg.  Problem executing closure "+closure);
            }
        }
    }
    /**
     * This will be the handler for all messages sent to us asynchronously through
     * the DObjectFacets that we provide.  Haven't finished this yet, but it will
     * likely look very much like the receiveAsynchViewMsg...
     */
    public final void receiveAsynchDObjectMsg(DObjectID fromID, Closure closure)
    {
        // Just enqueue message on run queue provided to use by the local View
        myQueue.enqueue(closure);
    }

    // Messages that we receive asynchronously from our local View
    // These are notifications of state change from the View
    /**
     * We have just been created, according to our local View
     *
     * @param notifyInfo any info that the view wishes to provide us
     * @see DObjectViewFacet#create
     */
    protected final void create(ViewNotifyInfo notifyInfo)
    {
        myState = INACTIVE_STATE;
        debug("CREATE called with info "+notifyInfo+" on "+this+", with id "+getID());
        // Now have view notify all interested other parties
        getSessionView().notifyDObjectCreated();
    }

    /**
     * We have just been activated, according to our local View
     *
     * @param notifyInfo any info that the view wishes to provide us
     * @see DObjectViewFacet#activate
     */
    protected final void activate(ViewNotifyInfo notifyInfo)
    {
        myState = ACTIVE_STATE;
        debug("ACTIVATE called with info "+notifyInfo+" on "+this+", with id "+getID());
        // Send notification to ourselves about activation.  Subclasses may
        // override the handling of this message we are sending here
        // to do whatever they need to do on activation ('activated' is name of method).
        myQueue.enqueue(BaseFacet.createNewClosure(DObject.class, ACTIVATE_OVERRIDEABLE, 
                    Closure.getObjectArrayFromParam(notifyInfo)));
        // Now have view notify all interested other parties
        getSessionView().notifyDObjectActivated();
    }
    
    /**
     * We have just been deactivated, according to our local View
     *
     * @param notifyInfo any info that the view wishes to provide us
     * @see DObjectViewFacet#deactivate
     */
    protected final void deactivate(ViewNotifyInfo notifyInfo)
    {
        myState = INACTIVE_STATE;
        debug("DEACTIVATE called with info "+notifyInfo+" on "+this+", with id "+getID());
        // Send notification to ourselves about activation.  Subclasses may
        // override the handling of this message to do whatever they need to
        // do on activation ('deactivated' is name of method).
        myQueue.enqueue(BaseFacet.createNewClosure(DObject.class, DEACTIVATE_OVERRIDEABLE, 
                    Closure.getObjectArrayFromParam(notifyInfo)));
        // Now have view notify all interested other parties
        getSessionView().notifyDObjectDeactivated();
    }

    /**
     * We have just been destroyed, according to our local View
     *
     * @param notifyInfo any info that the view wishes to provide us
     * @see DObjectViewFacet#destroy
     */
    protected final void destroy(ViewNotifyInfo notifyInfo)
    {
        debug("DESTROYED called with info "+notifyInfo+" on "+this+", with id "+getID());
        // Close up our run queue...no more messages will be accepted
        // However, any messages still in queue when this message is processed
        // will be processed...but they won't be able to do anything
        myQueue.close();
        myState = DESTROYED_STATE;
        // Now have view notify all interested other parties
        getSessionView().notifyDObjectDestroyed();
        // Revoke access facets...no more interaction with us via the facet
        // will happen, and our untrusted subclasses will not be able to
        // interact with the View (or anything else for that matter) 
        // from this point on
        revokeAccessFacets();
    }
    
    // The following four methods are subclass overrideable notifications (asynch
    // from the View), that either *we* have been activated/deactivated, or that
    // some other DObject has been activated/deactivated.  
    
    /**
     * Subclass overrideable notification that this object has been activated.
     * This is sent by the DObject.activated method (asynchronously).  The
     * default implementation is to do nothing.
     *
     * @notifyInfo the ViewNotifyInfo for the activation
     */
    protected void activated(ViewNotifyInfo notifyInfo)
    {
        debug("Overrideable ACTIVATED received with info "+notifyInfo+" on "+this+", with id "+getID());
    }

    /**
     * Subclass overrideable notification that this object has been deactivated.
     * This is sent by the DObject.deactivated method (asynchronously).  The
     * default implementation is to do nothing.
     *
     * @notifyInfo the ViewNotifyInfo for the activation
     */
    protected void deactivated(ViewNotifyInfo notifyInfo)
    {
        debug("Overrideable DEACTIVATED received with info "+notifyInfo+" on "+this+", with id "+getID());
    }

    /**
     * Message sent from View to us if we have explicitly asked to be registered
     * as a listener for other DObjects via the addDObjectListener call.
     * This message will be sent to us by the View whenever a DObject has been
     * activated.  Subclasses will probably choose to override this so they can
     * respond to this message, if they call addDObjectListener.
     *
     * @param otherID the DObjectID of the DObject that has been activated
     * @see #addDObjectListener
     */
    protected void otherActivated(DObjectID otherID)
    {
        debug("OTHER ACTIVATED with id "+otherID+" on "+this+", with id "+getID());
        // When we get this message, it means that another DObject presence has arrived in the
        // local View, and that the View has notified us.  In response to this message,
        // this class will send a default facet request to the other DObject via
        // sendRequestForCapability.  
        // The other side has the option to respond or not with its default facet.  If it
        // responds our receiveCapability method will be called...and we will have the
        // facet!  Subclasses can use this facet simply by overriding receiveCapability!
        if (otherID != null) {
            try {
                sendRequestForCapability(otherID, DEFAULT_FACET_REQUEST, null);
            } catch (Exception e) {
                // If this doesn't work, it's OK...it's just that we're out of luck 
                // for getting this new DObject's default facet
                dumpStack(e, "DObject.otherActivated.  Default facet request to "+otherID+" failed.");
            }
        }
    }
 
    /**
     * Message sent from View to us if we have explicitly asked to be registered
     * as a listener for other DObjects via the addDObjectListener call.
     * This message will be sent to us by the View whenever a DObject has been
     * deactivated.  Subclasses will probably choose to override this so they can
     * respond to this message, if they call addDObjectListener.
     *
     * @param otherID the DObjectID of the DObject that has been deactivated
     * @see #addDObjectListener
     */
    protected void otherDeactivated(DObjectID otherID)
    {
        debug("OTHER DEACTIVATED with id "+otherID+" on DObject "+this+", with id "+getID());
    }
    
    // End subclass overrideable messages sent from View
    
    // Methods to deal with messages to/from remote presences
    
    /**
     * This message is sent to us by the view when we have received some data
     * from one of our remotes.  This is the central point for the handling of
     * all data from our remote presences, and is declared final to guarantee
     * functionality.
     *
     * @param fromID the SessionViewID of the view where our presence sent this
     * data from
     * @param data the Data that was sent to us
     * @see handleDataFromRemote
     */
    protected final void receiveDataFromRemote(SessionViewID fromID, Data data)
    {
        debug("DObject.receiveDataFromRemote.  DObject "+getID()+" got message from remote "+fromID+" and data "+data);
        // Get first element from Data.  If it's not a Closure, then call handleDataFromRemote
        Closure code = null;
        try {
            code = (Closure) data.get(0);
        } catch (ClassCastException e) {
            // Not Closure...what to do?
            handleDataFromRemote(fromID, data);
            return;
        }
        // It is a closure, so handle appropriately
        receiveClosureFromRemote(fromID, code);
    }
    
    /**
     * Method called by receiveDataFromRemote when a remote presence has sent us
     * some data, it's not a Closure to be executed, but rather is some other data.
     * Subclasses may override as appropriate to deal with message as appropriate.
     *
     * @param id the SessionViewID the data are from
     * @param data the Data sent to us
     * @see #receiveDataFromRemote
     */
    protected void handleDataFromRemote(SessionViewID id, Data data)
    {
        debug("DObject.handleDataFromRemote.  DObject "+getID()+" got message from remote "+id+" with data "+data);
    }
    
    /** 
     * Send the given Data to the remote view specified.  If the view specified
     * is null, the Data is sent to all remote presences.
     *
     * @param toID the SessionViewID we are directing the Data to.  If null,
     * it means that Data is directed to all known presences
     * @param data the Data to send
     * @exception IOException thrown if some problem with queueing the message
     */
    protected final void sendDataToRemote(SessionViewID toID, Data data)
        throws IOException
    {
        if (toID == null) getSessionView().sendDataToPresences(data);
        else getSessionView().sendDataToPresenceAtView(toID, data);
    }
    
    /**
     * Send the given Closure object to the given remote view identified by
     * the toID parameter.  The first parameter may be null (which means all
     * remotes will receive the Closure).  
     *
     * @param toID the SessionViewID we are directing the Closure to.  If null,
     * means Closure is directed to all known presences
     * @param code the Closure to send
     * @exception IOException thrown if some problem with queue the message
     * @exception NotSerializableException thrown if some parameters in the Closure
     * are not declared to be Serializable
     * @see dom.closure.Closure
     */
    protected final void sendClosureToRemote(SessionViewID toID, Closure code)
        throws IOException, NotSerializableException
    {
        // First, check to make sure closure is serializable (params)
        // This will throw NotSerializableException if not
        Closure.checkParamsForSerializable(code);
        // Wrap closure in Data object
        Data myData = new Data();
        myData.put(0, code);
        // Send data to remote(s) via ViewDObjectFacet
        // This will throw IOException if we've been cut off
        sendDataToRemote(toID, myData);
    }
    
    /**
     * Receive and execute a Closure sent to us from one of our remotes.  Simply
     * calls runClosure with the given Closure.
     * 
     * @param fromID the SessionViewID where our presence sent us the Closure from
     * @param code the Closure to run
     * @see receiveDataFromRemote
     * @see runClosure
     */
    protected void receiveClosureFromRemote(SessionViewID fromID, Closure code)
    {
        debug("DObject.receiveClosureFromRemote.  DObject "+getID()+" received closure "+code+" from remote view "+fromID);
        // Do it!
        try {
            runClosure(code);
        } catch (Exception e) {
            dumpStack(e, "DObject.receiveClosureFromRemote.  Exception executing closure "+code+" on "+getID());
        }
    }
    
    // Client creation methods.
    
    /**
     * Msg received when our local view gets a new group member and notifies us
     * that we may want to create a remote client presence of ourselves.
     * This method sends a 'create client' message the new remote View to ask
     * it to create a client presence on our behalf.  The remote View may refuse,
     * or the creation may fail for some other reason, but if so, we will hear
     * back via the createClientDone msg (below).
     *
     * @param notifyInfo info provided by the local view.  This at least contains
     * the remote's SessionViewID so that we can direct messages to it
     *
     * @see ViewListManager#addNewDObject
     * @see #createClientDone
     */
    protected void createClient(CreateClientInfo notifyInfo)
    {
        debug("DObject.createClient.  Called on object "+getID()+" with info "+notifyInfo);
        // Only respond to this if we're a host
        if (!isClientPresence()) {
            SessionViewID id = notifyInfo.getTargetViewID();
            if (id != null) {
                // Send message to create client on remote
                // This code creates all client presences!
                debug("DObject.createClient.  Sending message to create remote client presence");
                try {
                    getSessionView().sendCreateMsg(id, getDataToCreateClient(id));
                } catch (Exception e) {
                    // This is OK if we are racing on a disconnect, but it means no client will be created
                    debug("DObject.createClient.  Exception sending client create request...no client will be created");
                }
            } else {
                debug("DObject.createClient.  Received null SessionViewID, ignoring.");
            }
        } else {
            debug("DObject.createClient.  We're client...ignoring create request");
        }
    }
    
    /**
     * Create an instance of a CreateDObjectData for delivery to remote LoadingDObject
     * instance.  Essentially, the CreateDObjectData instance represents the <b>state</b> of
     * this DObject for use in remote client construction.  It is called from the
     * createClient method.  Subclasses can override this to provide whatever data
     * they wish for creating a remote client.
     *
     * @param id the SessionViewID where the client will be created
     * @see #createClient
     */
    private CreateDObjectData getDataToCreateClient(SessionViewID id)
    {
        return new CreateDObjectData(getID(), getHomeID(), getClientClassName(id),
                                     getCodeBase(), getStateForClient(id), null,
                                     true);
    }
    
    /**
     * Get class name of class to use for client creation.  By default, this returns
     * the same class name as this object, but subclasses may override to use
     * <b>any class desired</b> for the remote client presence of this object (!).
     *
     * @param id the SessionViewID where the client will be created (not currently used)
     */
    protected String getClientClassName(SessionViewID id)
    {
        return this.getClass().getName();
    }
    
    /**
     * Get state of this DObject for delivery to new client.  The resulting object
     * must implement the Serializable interface, as it will be sent over the wire
     * to the remote client for initialization.  
     * <p>
     * NOTE:  Subclasses should never allow this to be called on a client DObject
     * instance.  It should only be called on the current host.
     *
     * @param id the SessionViewID of the view where the client is being constructed
     * @return the Serializable object that contains the state to be sent to the 
     * new client.  This object will be passed into the constructor of the remote
     * client (last parameter) during the construction of the instance.
     *
     * @see View#loadDObject
     * @see LoadingDObject
     * @see #setInitialState
     */
    protected Serializable getStateForClient(SessionViewID id)
    {
        debug("DObject.getStateForClient.  Request to get state for client on view "+id+" for "+getID());
        // By default, we have no 'extra' state (state not initialized upon construction
        // by the local View.  This method can be overriden as needed, however, by 
        // subclasses.
        return null;
    }
    
    /**
     * Get state of this DObject for delivery to client that will serve as new
     * host.  The object returned from this call must implement the Serializable 
     * interface, as it will be sent over the wire to the remote (formerly a client)
     * that will serve as the new host.
     * <p>
     * NOTE:  Subclasses should never allow this to be called on a client DObject
     * instance.  It should only be called on the current host.
     *
     * @param newHomeID the SessionViewID of the view where the new host will be
     * @return the Serializable object that contains the state to be sent to the 
     * new host.  This object will be passed into the 'setStateFromTransfer' method of
     * the remote client.
     *
     */
    protected Serializable getStateForTransfer(SessionViewID newHomeID)
    {
        debug("DObject.getStateForTransfer.  Request to get state for turning client on view "+newHomeID+" into host for "+getID());
        // By default, we have no 'extra' state (state not already cached on the
        // client
        return null;
    }
    
    protected void setStateFromTransfer(Serializable newState)
    {
        debug("DObject.setStateFromTransfer.  Request to set new host state to "+newState+" for "+getID());
    }        
    
    /**
     * Message received from remote presence when the remote client creation is
     * completed.  The remote sends a Closure with this method to this presence
     * and we then get the message.  If the Exception is null, it means everything
     * went OK.  If non-null, it represents the problem that was encountered during
     * creation.  Subclasses can override this to do specific things in response
     * to this event.
     *
     * @param fromID the SessionViewID that was creating a client presence
     * @param e the Exception resulting from the remote client create request.  If
     * null, the client was created properly.  If non-null, identifies problem
     * encounted in remote creation.
     *
     */
    protected void createClientDone(SessionViewID fromID, Exception e)
    {
        debug("CREATECLIENTDONE called from remote "+fromID+ "with exception "+e);
        // Don't know what to do about this...subclasses can override if
        // they wish to or must deal with this notification
    }

    // Methods for DObject destruction.  These can be called by subclasses to
    // commit suicide.
    
    /**
     * Permanently destroy this DObject host.  This is the interface that subclasses
     * may use to make this DObject (and all its client presences) permanently go away.
     * This method may only be successfully called from the host presence.  If
     * called directly on a client presence, it will throw a PresenceException.
     *
     * @exception PresenceException thrown if this method is called on client presence
     */
    protected final void recycleHost() throws PresenceException
    {
        // If this is racing against a host location change, prevent both from
        // being done at the same time right here.  Whichever gets here first
        // is the winner 
        synchronized (myHomeViewID) {
            if (isClientPresence()) throw new PresenceException("recycleHost cannot be called on client presence");
            // First, send message to remote clients to destroy themselves
            try {
                // We send a closure to all remote clients via recycleClient message
                sendClosureToRemotes(BaseFacet.createNewClosure(null, RECYCLE_CLIENT, null));
            } catch (Exception e) {
                // If this fails for whatever reason, then the clients are gone anyway
                debug("Exception "+e+" sending recycle message to clients");
            }
            // Then make ourselves go away.  This will trigger deactivate and
            // destroy messages from the local View...and we're gone
            try {
                getSessionView().recycleDObject();
            } catch (Exception e) {
                // If this goes wrong, we just report as spam
                dumpStack(e, "DObject.recycleHost.  Failure sending recycleDObject msg to View for "+getID());
            }
        }
    }
    
    /**
     * Message from host to client to tell it to go away.  This is the message
     * handler for the client, so that hosts can send the recycle message.
     *
     * @exception PresenceException thrown if this presence is not a client
     */
    protected final void recycleClient() throws PresenceException
    {
        synchronized (myHomeViewID) {
            // First, make sure this is actually a client
            if (!isClientPresence()) throw new PresenceException("recycleClient cannot be called on host presence");
            // Make ourselves go away.  This will trigger local deactivate and
            // destroy messages from View and then we're gone
            try {
                getSessionView().recycleDObject();
            } catch (Exception e) {
                // If this goes wrong, we just report as spam
                dumpStack(e, "DObject.requestCapability.  Failure sending recycleDObject msg to View for "+getID());
            }
        }
    }

    // Messages for the DObject Capability Request mechanism
    
    /**
     * Method called by View (only) to deliver a request for capability
     * from another DObject.  This method is sent asynchronously by the
     * View#requestDObjectCapability method so that we have a chance to
     * receive the request, make a decision about whether to respond to 
     * the request, and then send this receipt *back* to the View so that
     * it can deliver it to the original requestor (via the
     * ViewDObjectFacet#receiveCapability method).  Subclasses should
     * override this as appropriate to provide capabilities exchange
     * semantics as needed.
     *
     * In response to this message, if the receive wishes to respond
     * positively to the request it should:
     * a) Create an instance of a DObjectFacet (or some subclass)
     * b) Call the receipt.setFacet(DObjectFacet) method to set the 
     * facet for the given receipt
     * c) Call the getSessionView().receiveCapability(receipt) method.
     *
     * @param receipt the CapabilityReceipt from the View
     * @see DObjectViewFacet#requestCapability
     * @see View#requestDObjectCapability
     * @see ViewDObjectFacet#receiveCapability
     * @see View#giveDObjectCapability
     * @see DObject#receiveCapability
     */
    protected void requestCapability(CapabilityReceipt receipt)
    {
        // debug("DObject.requestCapability.  Receipt is "+receipt);
        // If the requestor wants our default facet, we will give it to them.
        // We will also automatically ask *them* for their default facet
        // They first have to say the magic word...
        String requestName = receipt.getRequestName();
        if (requestName != null && requestName.equals(DEFAULT_FACET_REQUEST)) {
            // They've said the magic word!  
            debug("DObject.requestCapability.  Received request from "+receipt.getRequestorID()+" for our default facet");
            DObjectFacet defaultFacet = getDefaultDObjectFacet(receipt.getRequestorID());
            if (defaultFacet != null) {
                // Set facet on receipt
                receipt.setFacet(defaultFacet);
                // debug("DObject.requestCapability.  Responding to default facet request with "+defaultFacet);
                // Send back to them via View
                try {
                    getSessionView().receiveCapability(receipt);
                } catch (Exception e) {
                    // If this fails, that's OK, it just means they don't get our facet
                    // But we'll report it for debugging
                    dumpStack(e, "DObject.requestCapability.  Default facet for "+getID()+" failed in delivery to "+receipt.getRequestorID());
                }
            }
            // AND, we'll ask them for *their* default facet.
            // They can, of course, refuse to give it to us, but it doesn't hurt to ask
            // This check prevents infinite recursion
            if (receipt.getData() == null) {
                try {
                    sendRequestForCapability(receipt.getRequestorID(), DEFAULT_FACET_REQUEST, new Boolean(true));
                } catch (Exception e) {
                    // If this doesn't work, it's OK...it's just that we're out of luck 
                    // for getting this new DObject's default facet
                    dumpStack(e, "DObject.requestCapability.  Default facet request to "+receipt.getRequestorID()+" failed.");
                }
            }
        }
    }
    
    /**
     * Get the default DObjectFacet for this DObject.  This is called from
     * the requestCapability method, in case we are asked for a capability,
     * and the subclass does not handle it.  For a default DObjectFacet (one
     * that is given out in response to any request), what this does is 
     * construct a DObjectFacet that exposes any <b>public</b> methods declared
     * by subclasses as asynchronously invokable by the receiver of this facet.
     * Subclasses are free to override to return (e.g.) nothing, or some other
     * default facet policy.
     * 
     * @param requestor the DObjectID of the object making the request
     * @return DObjectFacet that is the default facet for this DObject
     */
    protected DObjectFacet getDefaultDObjectFacet(DObjectID requestor)
    {
        // This will construct a DObjectFacet that exposes all of the *public*
        // methods on this DObject for asynchronous invokation by the receiver
        // of this DObjectFacet.  Uses CRAPI lookup on ourselves to get the
        // array of Method objects.
        try {
            return new DObjectFacet(this);
        } catch (NullFacetException e) {
            return null;
        }
    }
    
    /**
     * Method called by View (only) to respond to *our* request for a capability
     * from another DObject (via ViewDObjectFacet.requestDObjectCapability)
     *
     * @param aFacet DObjectFacet that View is returning to us from the grantor
     * Can be null, if grantor does not wish to give anything back to us.
     * @see DObjectViewFacet#receiveCapability
     * @see View#giveDObjectCapability
     */
    protected void receiveCapability(DObjectFacet aFacet)
    {
        debug("DObject.receiveCapability.  Facet provided is "+aFacet);
    }
    
    /**
     * Entry point method for subclasses to issue requests of other DObject
     * instances for capabilities.  This can be used by DObject subclasses
     * to send an asynchronous request for a capability to another DObject
     * presence (within same view), via the local (trusted) View.  Sure,
     * subclasses could call getAccessFacet() themselves and send the message
     * directly, but this method makes it easier on them ;-).
     *
     * @param id the DObjectID of the DObject we wish to make the request of (grantor)
     * @param requestName the request name (cannot be null)
     * @param data any Object representing data specific to the request
     * @exception Exception thrown under a variety of different conditions
     * @see ViewDObjectFacet#requestCapability
     * @see View#requestDObjectCapability
     * @see DObject#requestCapability
     * @see ViewDObjectFacet#receiveCapability
     * @see View#giveDObjectCapability
     * @see DObject#receiveCapability
     */
    protected final void sendRequestForCapability(DObjectID id, String requestName, Object data)
        throws Exception
    {
        debug("DObject.sendRequestForCapability.  "+getID()+" sending request '"+requestName+"' with data "+data+" to "+id);
        getSessionView().requestCapability(id, requestName, data);
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
    protected final BaseFacet getViewFacet(String request, Object data)
    {
        debug("DObject.getViewFacet.  "+getID()+" sending request '"+request+"' with data "+data+" to View");
        return mySessionView.getViewFacet(request, data);
    }

    /**
     * Interface to request from the View that we be notified of the arrival
     * and departure of other DObjects.  This will be used by subclasses that
     * wish to 'know' of the arrival and departure of other DObjects (so that
     * interaction can begin).  As a result of calling this method (e.g. in
     * the constructor), the subclass of this class will then start receiving
     * asynchronous notifications from the view of the activation and 
     * deactivation of other DObjects.  It can do as it wishes with these 
     * notifications.
     *
     * @see #otherActivated
     * @see #otherDeactivated
     * @see ViewDObjectFacet#addDObjectListener
     * @see ViewDObjectFacet#removeDObjectListener
     */
    protected final void addDObjectListener()
    {
        mySessionView.addDObjectListener();
    }
    
    /**
     * Interface for removing ourselves as listeners for the arrival/departure
     * of other DObjects.
     *
     * @see #otherActivated
     * @see #otherDeactivated
     * @see ViewDObjectFacet#removeDObjectListener
     */
    protected final void removeDObjectListener()
    {
        mySessionView.removeDObjectListener();
    }
    
    // End capability request methods
    
    /**
     * Run (execute) a given closure for this object.
     *
     * @param closureToRun the Closure object to run
     * @exception Exception thrown if some problem running the given closure
     * @see Closure#executeThisClosure
     */
    protected void runClosure(Closure closureToRun)
        throws Exception
    {
        // For these tests, Simply invoke on ourselves
        // Subclasses may wish to override this behavior to process messages
        // in an entirely different fashion (e.g. the ComponentDObject executes
        // its closures on its components rather than directly upon itself).
        closureToRun.executeThisClosure(this);
    }

// Utility methods

    /**
     * Utility function to send the given Closure to our host.  If this code is
     * called by the host instance, it is ignored.  Simply a wrapper for 
     * sendClosureToRemote, with first parameter set to this DObject's homeID.
     * 
     * @param closure the Closure to deliver to the host
     * @exception IOException thrown if we are not connected
     * @exception NotSerializableException thrown if some parameters in the Closure
     * are not declared to be Serializable
     * @see #sendClosureToRemote
     */
    protected void sendClosureToHost(Closure closure)
        throws IOException, NotSerializableException
    {
        SessionViewID homeID = getHomeID();
        if (!getSessionView().getID().equals(homeID)) {
            sendClosureToRemote(homeID, closure);
        }
    }

    /**
     * Utility function call sendClosureToRemote with null parameter, which
     * means that it will be delivered to <b>all</b> other presences.  
     *
     * @param aClosure the Closure to deliver to remotes
     * @exception IOException thrown if we are not connected
     * @exception NotSerializableException thrown if some parameters in the Closure
     * are not declared to be Serializable
     * @see #sendClosureToRemote
     */
    protected void sendClosureToRemotes(Closure aClosure)
        throws IOException, NotSerializableException
    {
        sendClosureToRemote(null, aClosure);
    }

    /**
     * Handle an exception generated during our own message processing.  This
     * allows this DObject some flexibility in dealing with exception conditions
     *
     * @param aClosure the Closure that generated the exception
     * @param aThrowable the Throwable generated
     */
    protected void handleMessageException(Closure aClosure, Throwable aThrowable)
    {
        dumpStack(aThrowable, getID().toString()+" had exception executing "+aClosure);
    }
    
    /**
     * Utility method for use by all DObjects and subclasses to dump the
     * stack.  This gives a single point for debugging output.
     *
     * @param e the Throwable being reported (can be null)
     * @param location a String that is also put out to debugging output
     */
    protected void dumpStack(Throwable e, String location)
    {
        System.out.print(location+": "); System.out.flush();
        System.out.println();
        if (e != null) e.printStackTrace();
    }
    
    protected void debug(String message)
    {
        if (debug) System.out.println(message);
    }

}

