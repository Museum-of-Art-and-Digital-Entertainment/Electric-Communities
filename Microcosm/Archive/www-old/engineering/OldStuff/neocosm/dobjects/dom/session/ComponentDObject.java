/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ComponentDObject.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/

package dom.session;

import java.net.URL;
import java.io.*;
import dom.id.DObjectID;
import dom.serial.Data;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * DObject subclass that allows message handling to be done by 'components'
 * or 'ingredients' that are abitrary java object instances 'owned' by the DObject.  This
 * subclass exposes certain methods to the component instances, so that they
 * can send messages to their host/client versions.
 *
 * @author Scott Lewis
 */
public class ComponentDObject extends DObject {

    protected static String MESSAGE_ROUTING_METHOD_NAME = "cmr";
    
    // Hashtable of components.  This hashtable holds the String->BaseComponent relations
    // for the components that make up this type of DObject.  This serves a 
    // function very similar to the message 'routers' from Pluribus.  This should
    // be simpler and more flexible, however, as this Hashtable can be modified
    // at run time.
    Hashtable myComponents;
    
    protected ComponentDObject(ViewDObjectFacet viewFacet,
                      DObjectID newID,
                      SessionViewID homeID,
                      URL codeBase,
                      Serializable params) throws InstantiationException
    {
        super(viewFacet, newID, homeID, codeBase, params);
    }
    
    /**
     * Initialize this instance.  This is called in the DObject constructor, and
     * it allows this subclass to call arbitrary initialization code.  The implementation
     * for this class is to do nothing, as all critical initialization is done
     * within the DObject constructor.
     * <p>
     * NOTE:  If subclasses override this method, they should be sure to call
     * super.initialize() in the subclass version of this method.  Also, more than 
     * likely they should call super.initialize() as the *first* thing in the
     * subclass version
     *
     * @exception InstantiationException can be thrown if some problem with
     * initialization
     */
    protected void initialize() throws InstantiationException
    {
        // Call superclass initialization first
        super.initialize();
        // Create components hashtable
        myComponents = new Hashtable();
        // Add ourselves as a listener for other DObjects
        addDObjectListener();
    }
    
    /**
     * Get state of this ComponentDObject for delivery to new client.  The resulting object
     * must implement the Serializable interface, as it will be sent over the wire
     * to the remote client for initialization.  For this class, what this does it to
     * iterate through all of the components making up this ComponentDObject, and call
     * their 'getStateForClient' methods.  If a given component returns a non-null
     * object, this object is added to a hashtable of component states, which is 
     * serialized, sent over the wire to the new client, and will ultimately be passed
     * to the 'setInitialState' method on the appropriate component.
     * <p>
     * NOTE:  If this method is overridden by a subclass, the subclass should probably
     * also override the 'setInitialState' method below, so that the correct initialization
     * happens for clients.
     *
     * @param id the SessionViewID of the view where the client is being constructed
     * @return the Serializable object that contains the state to be sent to the 
     * new client.  This object will be passed into the constructor of the remote
     * client (last parameter) during the construction of the instance.
     *
     * @see BaseComponent#getStateForClient
     * @see #setInitialState
     */
    protected Serializable getStateForClient(SessionViewID id)
    {
        debug("ComponentDObject.getStateForClient.  Getting state for new client on "+id+" for "+getID());
        // For this ComponentDObject, we create a Hashtable that maps the
        // component identifier (should be unique within this object) to the
        // Serializable object that represents the client state for that component
        Data clientComponentState = new Data();
        // Just to make sure the components set does not change our from underneath us
        synchronized (myComponents) {
            for(Enumeration e=myComponents.keys(); e.hasMoreElements(); ) 
            {
                String compIdentifier = (String) e.nextElement();
                // Get component
                BaseComponent comp = (BaseComponent) myComponents.get(compIdentifier);
                Serializable componentState = comp.getStateForClient(id);
                if (componentState != null) {
                    debug("ComponentDObject.getStateForClient.  Adding object "+componentState+" to data for remote client for "+getID());
                    // put it in hashtable to deliver to client, keyed by the 
                    // component's identifier
                    clientComponentState.put(compIdentifier, componentState);
                }
            }
        }
        if (clientComponentState.size() > 0) {
            return (Serializable) clientComponentState;
        } else {
            return null;
        }
    }
    
    /**
     * Set initial state for all our components.  This method is called inside the 
     * constructor for the DObject instance (host and client), and is passed the 
     * Serializable last parameter.  If this is a client, the host method 
     * 'getStateForClient' determines what the parameter actually is, and is 
     * sent over the wire in the construct message.  For this ComponentDObject class, 
     * the implementation of this is to a) cast the param into a Data object (created
     * by the getStateForClient method above); b) get the (String) component 
     * identifier and the Serializable object for initializing that component; c) call
     * the 'setInitialState' method on the the found component, with the found 
     * Serializable object for its initial state.
     *
     * <p>
     * NOTE:  If this method is overridden, the 'getStateForClient' method above
     * should probably also be overridden.
     *
     * @param params the Serializable object that is passed to this method
     * by the constructor.  For this superclass, the value of params is null.
     * @exception InstantiationException can be thrown if some problem with
     * setting the initial state
     * @see #getStateForClient
     */
    protected void setInitialState(Serializable param) throws InstantiationException
    {
        debug("ComponentDObject.setInitialState.  Called for "+getID()+" with param "+param);
        if (isClient() && param != null) {
            try {
                // Since this is an instance of ComponentDObject, this should cast to the
                // 'Data' class, since it almost certainly came from the 'getStateForClient' method defined
                // above.  If it doesn't, a subclass has overridden the 'getStateForClient'
                // method without overriding this method, and it's probably an error.
                Data theData = (Data) param;
                for(Enumeration e=theData.keys(); e.hasMoreElements(); )
                {
                    String compIdentifier = (String) e.nextElement();
                    // Make sure that valid (non-null) data has been provided by the host instance
                    Serializable compState = (Serializable) theData.get(compIdentifier);
                    if (compState == null) {
                        debug("ComponentDObject.setInitialState.  Bogus (null) data provided for component "+compIdentifier+" for "+getID()+" client");
                        throw new NullPointerException();
                    }
                    // Now make sure we have the appropriate component.  If not, something
                    // is really wrong
                    BaseComponent comp = (BaseComponent) myComponents.get(compIdentifier);
                    // If either of these is null, there is a problem
                    if (comp == null) {
                        debug("ComponentDObject.setInitialState.  No component instance found with identifier "+compIdentifier+" for "+getID()+" client");
                        // Error.  
                        throw new NullPointerException();
                    }
                    // Now we let the component setup it's initial state
                    comp.setInitialState(compState);
                }
            } catch (ClassCastException e) {
                // Error
                debug("ComponentDObject.setInitialState.  Exception "+e+" initializing client for "+getID()+".  Param is "+param);
                throw e;
            }
        }
    }
    
    /**
     * Get our id.
     *
     * @return DObjectID that is our id
     */
    public DObjectID getDObjectID()
    {
        return super.getID();
    }
    
    /**
     * Determine whether we are the client or host instance.
     * 
     * @return true if this is a client, false if host
     */
    public boolean isClient()
    {
        return super.isClientPresence();
    }
    
    public SessionViewID getIDForView()
    {
        return getViewID();
    }
    
    /**
     * Send the given closure to the component on all remotes identified by 
     * the componentIdentifier parameter.
     *
     * @param componentIdentifier the component identifier used to disambiguate
     * which component to use to process the given Closure
     * @param aClosure the Closure to process
     * @exception IOException thrown if Closure cannot be sent
     * @exception NotSerializableException thrown if parameters in aClosure are
     * not serializable
     */
    public void sendToRemoteComponents(String componentIdentifier, Closure aClosure)
        throws IOException, NotSerializableException
    {
        super.sendClosureToRemotes(BaseFacet.createNewClosure(null,
                                               MESSAGE_ROUTING_METHOD_NAME,
                                               Closure.getObjectArrayFromParams(componentIdentifier, aClosure)));
    }

    /**
     * Send the given closure to the component on all the *given* remote.  Same as 
     * above, except that message is only delivered to view specified by first
     * parameter.
     *
     * @param viewID the viewID where this message is destined for
     * @param componentIdentifier the component identifier used to disambiguate
     * which component to use to process the given Closure
     * @param aClosure the Closure to process
     * @exception IOException thrown if Closure cannot be sent
     * @exception NotSerializableException thrown if parameters in aClosure are
     * not serializable
     */
    public void sendToRemoteComponent(SessionViewID viewID, String componentIdentifier, Closure aClosure)
        throws IOException, NotSerializableException
    {
        super.sendClosureToRemote(viewID, BaseFacet.createNewClosure(null,
                                               MESSAGE_ROUTING_METHOD_NAME,
                                               Closure.getObjectArrayFromParams(componentIdentifier, aClosure)));
    }

    /**
     * Send the given closure to the component on all the remote host.  Same as 
     * above, except that message is only delivered to the host view 
     *
     * @param componentIdentifier the component identifier used to disambiguate
     * which component to use to process the given Closure
     * @param aClosure the Closure to process
     * @exception IOException thrown if Closure cannot be sent
     * @exception NotSerializableException thrown if parameters in aClosure are
     * not serializable
     */
    public void sendToHomeComponent(String componentIdentifier, Closure aClosure)
        throws IOException, NotSerializableException
    {
        sendToRemoteComponent(getHomeID(), componentIdentifier, aClosure);
    }
    
    /**
     * Deliver given Closure from given DObjectID to component identified by componentIdentifier
     *
     * @param fromID the DObjectID this Closure is from
     * @param componentIdentifier the component this message is bound for
     * @param aClosure the Closure to deliver
     */
    public void asynchDeliverToComponent(DObjectID fromID, String componentIdentifier, Closure aClosure)
    {
        super.receiveAsynchDObjectMsg(fromID, 
                                      BaseFacet.createNewClosure(null, 
                                                  MESSAGE_ROUTING_METHOD_NAME, 
                                                  Closure.getObjectArrayFromParams(componentIdentifier, aClosure)));
    }

    /**
     * Method invoked to to message routing to the components that make up this
     * ComponentDObject.  Facets wishing to have Closures routed to a component
     * identified by the componentIdentifier should send *this* message to the
     * ComponentDObject instance.  This method will be invoked, and the give closure
     * will be invoked on the appropriate component, if found.
     *
     * @param componentIdentifier the identifier for the target component
     * @param message the Closure to actually be executed on the target component
     * @exception Exception thrown if any problem delivering or executing
     * message
     */
    public void cmr(String componentIdentifier, Closure message)
        throws Exception
    {
        debug("ComponentDObject.cmr.  Using message router on ComponentDObject "+this);
        // First, look up the component for the given identifier
        Object component = getComponentForIdentifier(componentIdentifier);
        // Make sure we have one, and that Closure is non-null
        if (component != null && message != null) {
            // Pass it on here
            debug("ComponentDObject.componentMessageRouter.  Component "+componentIdentifier+" found!  Passing message onto "+component);
            // Try to execute Closure on the component
            message.executeThisClosure(component);
        } else {
            // Not found, do something here
            debug("ComponentDObject.componentMessageRouter.  Component "+componentIdentifier+" not found!");
            // XXX do something else?
            throw new ComponentException("Component "+componentIdentifier+" not found");
        }
    }
    
    // Methods to manipulate the myComponents Hashtable at runtime
    
    protected BaseComponent getComponentForIdentifier(String identifier) 
    {
        if (identifier == null) return null;
        return (BaseComponent) myComponents.get(identifier);
    }
    
    /**
     * Add given BaseComponent with given identifier key to our components hashtable.  The
     * identifier key must be unique within this ComponentDObject...otherwise a
     * ComponentException will be thrown.
     *
     * @param identifier the String identifier for the given component.  Must be unique
     * within this ComponentDObject.  If it is not, a ComponentException will be thrown
     * @param component the BaseComponent that we wish to add to our set of components
     * @exception ComponentException thrown if either of the parameters are null, or
     * if the identifier is already being used.
     */
    protected void addComponent(String identifier, BaseComponent component)
        throws ComponentException
    {
        if (identifier != null && component != null) {
            if (myComponents.containsKey(identifier)) throw new ComponentException("Identifier is not unique");
            myComponents.put(identifier, component);
        } else throw new ComponentException("Components must be non-null");
    }
    
    /**
     * Remove the component with the given identifier.  Ignored if component not found.
     *
     * @param identifier the unique identifier (within this ComponentDObject) to remove
     */
    protected void removeComponent(String identifier)
    {
        if (identifier == null) return;
        myComponents.remove(identifier);
    }
    
    /**
     * Message sent from View to us if we have explicitly asked to be registered
     * as a listener for other DObjects via the addDObjectListener call.
     * This message will be sent to us by the View whenever a DObject has been
     * deactivated.  Subclasses will probably choose to override this so they can
     * respond to this message, if they call addDObjectListener.
     *
     * @param otherID the DObjectID of the DObject that has been deactivated
     * @see DObject#addDObjectListener
     */
    protected void otherDeactivated(DObjectID otherID)
    {
        debug("ComponentDObject OTHER DEACTIVATED with id "+otherID+" on DObject "+getID());
        notifyComponentsOfDeactivation(otherID);
    }
    
    /**
     * Notify all the components that we know about that a DObject with given id
     * has deactivated.  This is so that the components that are interested can
     * clean up after themselves.
     *
     * @param otherID the DObjectID of the other DObject that has deactivated.
     * @see #otherDeactivated
     */
    protected void notifyComponentsOfDeactivation(DObjectID otherID)
    {
        synchronized (myComponents) {
            for(Enumeration e=myComponents.elements(); e.hasMoreElements(); ) {
                BaseComponent comp = (BaseComponent) e.nextElement();
                // First, call subclass overrideable clean up code
                comp.handleDObjectGone(otherID);
                // Then call final code to clean up facets
                comp.cleanUpFacetsWhenDObjectGone(otherID);
            }
        }
    }
    
    /**
     * Override of DObject.receiveCapability.  In this case, whenever we receive
     * a DObjectFacet from another DObject, we just turn around and ask all of 
     * the Components that we have if they would wish to cache this facet for later
     * use.  They can do whatever they want to do with the facet.  This override
     * simply turns around and calls notifyComponentsOfFacet.
     *
     * @param aFacet the DObjectFacet that we have been so kindly given
     * @see #notifyComponentsOfFacet
     */
    protected void receiveCapability(DObjectFacet aFacet)
    {
        debug("ComponentDObject.receiveCapability.  Facet provided is "+aFacet);
        notifyComponentsOfFacet(aFacet);
    }

    /**
     * Notify all the components that a facet from another DObject has been 
     * received.  This is so the Components can (if they wish) store away the
     * relevant facet for potential later use.
     *
     * @param aFacet the DObjectFacet that has been provided to us
     * @see BaseComponent#checkFacetForCache
     */
    protected void notifyComponentsOfFacet(DObjectFacet aFacet)
    {
        synchronized (myComponents) {
            for(Enumeration e=myComponents.elements(); e.hasMoreElements(); ) {
                BaseComponent comp = (BaseComponent) e.nextElement();
                if (comp.checkFacetForCache(aFacet)) {
                    try {
                        comp.addFacetToCache(aFacet);
                    } catch (NullFacetException except) {
                        // Ignore, as it is not completed anyway
                    }
                }
            }
        }
    }
    
}
