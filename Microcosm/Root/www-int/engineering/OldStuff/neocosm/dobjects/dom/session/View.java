/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/View.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;
import dom.net.*;
import dom.util.*;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import java.lang.reflect.*;

/**
 * Base class for all 'Views' of a multipoint group.  This is the local 
 * object that represents the multipoint group
 * to the locally hosted presences of <b>DObjects</b> (objects that are
 * clients of this reliable group messaging services), and serves as their 
 * reliable link to a distributed group of <b>DObject</b> presences (both host and
 * client presences are assumed to be subclasses fo the <b>DObject</b> root class).
 * <p>
 * This is an abstract superclass, that implements the multipoint group 
 * functionality in a network topology independent way.  It's considered the
 * responsibility of <b>subclasses</b> of this class to implement network
 * topology-specific View functionality.  For example, see the <b>ClientView</b>
 * and <b>ServerView</b> subclasses for the implementation of the 'star'
 * topology.  The purpose for this abstract superclass is to allow the
 * the introduction of new network messaging topologies (e.g. multicast IP),
 * WITH the associated reliability/group membership manager, while keeping
 * the abstract interface available to client (DObject) code, totally untouched.
 * Note that their are two <b>roles</b> that this View can take:  group manager
 * or not group manager.  There can only be one group manager per multipoint
 * group.  The group manager is primarily responsible for <i>determining who
 * can enter the existing group</i> and sending the group membership change
 * information to non-group managers.  Note that in the 'star' topology that
 * the group manager will be the one <b>ServerView</b>, but in the multicast
 * tree topology (e.g.), the group manager role is one that is not dictated
 * by the connectivity, and could conceivably even move dynamically (although
 * this would take some work).
 * <p>
 * This class provides access (to untrusted DObjects) to only one resource:  
 * sending data to the other presences of themselves on remote processes.  This 
 * functionality is provided by the methods: sendDataToPresences(...), and 
 * sendDataToPresenceAtView(...).  These are the basic messaging primitives for
 * DObject presences to send data to their <b>other presences only</b>.  Actually,
 * these interfaces are exposed to DObjects via the <i>ViewDObjectFacet</i>, which
 * is a facet on this class that exposes the messaging functionality provided
 * by this View to DObjects upon construction (see the DObject constructor).  
 * <p>
 * Note that all of the methods on this class are declared protected, meaning that
 * in an open turing machine world remotely loaded code (which would be guaranteed
 * by the classloader to be from <B>outside</b> this package), will have <i>no</i> 
 * direct access to these methods, and will only have access to the methods 
 * exposed via facet classes (e.g. the ViewDObjectFacet).  Locally trusted 
 * subclasses, however, will be free to override behavior as necessary.
 * <p>
 * This class also provides access to two 'meta' interfaces for DObjects.  One
 * is the <b>getViewFacet</b> interface, that allows DObjects to explicitly 
 * request access to other services provided by the local runtime (since in 
 * the most restrictive case the DObject has an external reference <i>only</i>
 * to this View, it must ask this View for system resources, etc. as needed).
 * For example, if a DObject presence must get access to the user interface,
 * it would have to explicitly ask via the getViewFacet method, and the local
 * system would have the opportunity to assess the request and respond 
 * appropriately...by, for example, providing access to a facet to a local
 * system resource (e.g. the UI).
 * <p>
 * The second 'meta-interface' is the DObject<->DObject meta-protocol.  
 * The two relevant methods on this class for this meta protocol are:  
 * <b>requestDObjectCapability</b> and <b>giveDObjectCapability</b>.  This
 * meta protocol is to allow local DObject presences to establish direct
 * messaging between each other's presences, as dictated by their semantics, 
 * using a locally trusted third party (this View object).  Essentially, this supports
 * the equivalent of 'Unum facets'.  The meta-protocol is based around the
 * same 'PO Box' model that is used in the secure identity-based capability
 * exchange built by Trevor and myself.
 * 
 * @see DObject
 * @author Scott Lewis
 */
public abstract class View {

    //public static boolean debug = true;
    public static boolean debug = false;
    
    // This defines the value returned by getRank when local view is not
    // connected to any group
    public static final int VIEW_NOT_GROUP_MEMBER = -1;
    public static final int VIEW_IS_GROUP_MANAGER = 0;
    
    // A static array of Class objects, to be used in getting the constructor
    // for a DObject with appropriate expected types.  **This array of types
    // must match the types declared for the DObject class constructor**, so
    // if the DObject constructor signature is changed, this array has to
    // change also to allow the createDObject method to work properly.
    public static final Class[] dobjectConstructorTypes = 
        { ViewDObjectFacet.class, DObjectID.class, SessionViewID.class, URL.class, Serializable.class };
    
    // The SessionViewID for this view (initialized in constructor)
    // This View's globally unique id.  Having this separate id allows there to
    // be a many-to-one relationship between Views and processes
    protected SessionViewID myID;
    // The SessionViewID of the group manager for this group.  Is null
    // if not currently in any group.  For group manager itself, is never null.
    protected SessionViewID myGroupManager;
    // The current rank in the group for this view.  Initialized in constructor,
    // and changed as we join or leave a remote group
    protected int myRank;
    // Variable that (in cooperation with isConnected()) keeps track of group
    // state for this View
    protected boolean myWaitingToConnect;
    // My connection handler.  This is the object that implements our connection
    // with the group (assumed to be reliable and ordered...but can be based
    // on TCP/HTTP (pt2pt) or Multicast.  This object lets us know about
    // changes in group membership (implements a group membership manager).
    protected GroupConnectionHandler myConnectionManager;
    // The list manager for this view
    // This object uses the 'Manager' pattern from the 'Design Patterns' book.
    // It also uses the notions from Doug Lea's book RE: a 'synchronized object'
    // that provides synchronized access to several state variables, but
    // reifying in an object/class synchronizing access to all the state variables.
    protected ViewListManager myListManager;
    
    // Vector of CapabilityReceipt objects.  These are receipts that are
    // pending requests (have not been answered by capability grantor).
    /* CapabilityReceipt */ protected Vector myCapabilityReceipts;
    
    // Vector of DObjectFacet objects.  These are all of the facet that
    // have been granted between all DObjects.  This is used by this View
    // to forcibly kill all facets to a given DObject in case of (e.g.)
    // partition
    /* DObjectFacet */ protected Vector myDObjectFacets;
    
    /**
     * Normal (protected) constructor for this view.  Simply takes SessionViewID 
     * that this view will use and initializes member variables
     * 
     * @param idToUse the SessionViewID to use for this view
     */
    protected View(SessionViewID idToUse)
    {
        if (idToUse == null) throw new NullPointerException("ID for view cannot be null");
        myID = idToUse;
        myGroupManager = null;
        myRank = VIEW_NOT_GROUP_MEMBER;
        myWaitingToConnect = false;
        myListManager = new ViewListManager(this);
        myConnectionManager = new GroupConnectionHandler(this);
        myCapabilityReceipts = new Vector();
        myDObjectFacets = new Vector();
    }
    
    /**
     * Get the SessionViewID for this view.  Always returns the id associated with
     * this view.  Final so that untrusted code can trust that this does the
     * 'right' thing (!).
     *
     * @return SessionViewID that is this views' id
     */
    public final SessionViewID getID()
    {
        return myID;
    }
    
    /**
     * Get the current group membership, in the form of an array of SessionViewID 
     * objects.
     *
     * @return Object[] containing the SessionViewIDs of all of the views 
     * currently in this group
     */
    protected final SessionViewID[] getCurrentGroupMembers()
    {
        return myListManager.getGroupMembership();
    }
    
    /**
     * Get the rank of this view within the group.  This is determined at group
     * membership time...i.e. by the group manager (which might be us).
     * A value of VIEW_NOT_GROUP_MEMBER means that we are not currently connected
     * to a group.
     * @return int that represents my rank in the multipoint group.  A value
     * equal to VIEW_NOT_GROUP_MEMBER means that this view is currently unconnected
     */
    protected final int getMyRank()
    {
        return myRank;
    }
    
    /**
     * Determine whether this view is currently connected to a group.
     *
     * @return true if connected to a group, false otherwise
     */
    protected final boolean isConnected()
    {
        return (myGroupManager != null);
    }

    /**
     * Determine whether this view is currently *trying* to connect to
     * another group (in the waitingToConnect state)
     *
     * @return true if currently waiting to connect, false otherwise
     */
    protected final boolean isConnecting()
    {
        return myWaitingToConnect;
    }
    
    /**
     * Called by DObjects to determine whether this view is a server view.
     * Abstract since only true (or even relevant), when using a 'star'
     * topology and this is a ServerView.
     *
     * @return true if is a server view, false otherwise.  Abstract because 
     * its value has to be determined by subclasses that 'know' about the
     * network topology.
     */
    protected abstract boolean isServer();
    
    /**
     * Determine whether this view is a group manager or not
     *
     * @return true if this view is currently the group manager, false if not
     */
    protected boolean isGroupManager()
    {
        return (getMyRank() == VIEW_IS_GROUP_MANAGER);
    }
    
    ///////////////////////////////////////////////////////////////
    // Primitive interfaces for sending data to remote presences //
    ///////////////////////////////////////////////////////////////
    
    /**
     * Send data contained in DObjectPacket to any/all other remote views
     * known to exist to this view.  This is the primary primitive used by
     * DObjects to send arbitrary data to remote presences of itself.
     *
     * @param data the DObjectPacket containing any data to send
     * @exception IOException thrown if data cannot be sent
     * @see ViewDObjectFacet#sendDataToPresences
     */
    protected void sendDataToPresences(DObjectPacket data)
        throws IOException
    {
        sendSessionViewMsg(null, SessionViewMsg.SEND_DOBJECT_PACKET, 
            new SessionViewData(data));
    }
        
    /**
     * Send data contained in DObjectPacket to the presence at the view 
     * specified in the 'to' parameter.  In addition to sendDataToPresences,
     * this is a primary messaging primitive for DObjects to use to send
     * arbitrary data to their remote presences.
     *
     * @param to the SessionViewID that the data is going to
     * @param data the DObjectPacket containing any data to send
     * @exception IOException thrown if data cannot be sent
     * @see ViewDObjectFacet#sendDataToPresences
     */
    protected void sendDataToPresenceAtView(SessionViewID to, DObjectPacket data)
        throws IOException
    {
        sendSessionViewMsg(to, SessionViewMsg.SEND_DOBJECT_PACKET, 
            new SessionViewData(data));
    }
    
    // End interfaces for sending data to remote presences
    
    /////////////////////////////////////////////////////
    // Methods for dealing with DObject state in group //
    /////////////////////////////////////////////////////

    /**
     * Method to explicitly activate an inactive DObject.  This can be 
     * called by DObjects to explicitly activate themselves. If DObject 
     * the provided is not currently on the inactive list, a 
     * DObjectNotFoundException will be thrown.  Note that this can also
     * be called by trusted (e.g. administration code).
     *
     * @param id the DObjectID of the object to be activated
     * @exception DObjectNotFoundException thrown if the given id is
     * not currently on the inactive list
     */
    protected void activateDObject(DObjectID id)
        throws DObjectNotFoundException, IOException
    {
        activateDObjectLocally(id);
    }
        
    /**
     * Method to explicitly deactivate an active DObject.  This can be 
     * called by DObjects to explicitly deactivate an active presence
     * of a DObject.  If the DObject is not currently on the active
     * list, a DObjectNotFoundException will be thrown.  Note that this
     * can also be called by locally trusted (e.g. administration) code.
     *
     * @param id the DObjectID of the object to be deactivated
     * @exception DObjectNotFoundException thrown if the given id is
     * not currently on the active list
     */
    protected void deactivateDObject(DObjectID id)
        throws DObjectNotFoundException, IOException
    {
        deactivateDObjectLocally(id);
    }
        
    /**
     * Method to explicitly destroy a DObject.  This can be 
     * called by DObjects to explicitly destroy an active presence
     * of a DObject.  If the DObject is not currently on the active
     * list, a DObjectNotFoundException will be thrown.  Note that this
     * can also be called by locally trusted (e.g. administration) code.
     *
     * @param id the DObjectID of the object to be destroyed
     * @exception DObjectNotFoundException thrown if the given id is
     * not currently on the active list
     * @exception SecurityException is thrown if the given DObject (first
     * param) does not have the authority to move the given id
     */
    protected void destroyDObject(DObjectID id)
        throws DObjectNotFoundException, IOException
    {
        destroyDObjectLocally(id);
    }
    
    // End methods for dealing with DObject state within group
    
    ///////// Packet queueing methods ////////////////
    
    /**
     * Base method for sending a session packet's worth of data to a remote
     * session view.  Abstract because the behavior has to be determined
     * by the network-topology-aware subclasses.
     *
     * @param packet the SessionViewPacket that we wish to send
     * @exception IOException thrown if some problem sending packet
     */
    abstract protected void queueSessionViewPacket(SessionViewPacket packet)
        throws IOException;
        
    /**
     * Send the data associated with the given msg to the View with the 
     * given SessionViewID.
     * 
     * @param toID the SessionViewID of the View to receive the message.  If
     * null, then the message is directed to all current members of the 
     * multipoint group.
     * @param msg the msg type
     * @param data the SessionViewData to send (dependent upon msg type)
     */
    final protected void sendSessionViewMsg(SessionViewID toID, int msg, SessionViewData data)
        throws IOException
    {
        SessionViewID ourID = getID();
        // Only queue packet if it is not to us
        if (!ourID.equals(toID)) queueSessionViewPacket(new SessionViewPacket(ourID, toID, msg, data));
    }
                
    //// GroupManager interfaces.      
    /**
     * Primary access to joining an existing multipoint group.  This will
     * block until a) the connection has been formed to the group (still not
     * *admitted* but well on the way...modulo authentication); b) an IO
     * exception (i.e. connection could not be created) is thrown.
     * If access to the group is granted, a "joinOK" message from the 
     * group manager will be received...and that is the final word that
     * we (and potentially all our objects) will be allowed to join the group.
     * If this is called while this view is already a member of a group
     * (before leave is called) then it will throw an exception.
     * <p>
     * Note that this and the <b>leaveGroup</b> methods would be exposed to 
     * the appropriate DObject code (e.g. Avatars or Teleport Pads, etc) to 
     * allow them to determine what group this view was connected to (i.e. 
     * move from 'place' to 'place'.  Note that these are not exposed to all 
     * DObjects by default, rather a facet for this functionality will have 
     * to be created and explicitly given out to the 'deserving' DObjects.
     *
     * @param viewToJoin the SessionViewID that represents the group
     * we wish to join
     * @exception Exception thrown if there is some problem joining group
     * (group unreachable, security disallows join request, already a
     * member of a group, etc.)
     */
    protected synchronized final void joinGroup(SessionViewID viewToJoin)
        throws Exception
    {
        // First, make sure we are neither currently connected, nor
        // waiting to connect.  If either of these is true, then we cannot
        // continue and will throw to caller
        if (isConnected()) throw new ViewConnectionException("Already connected");
        if (isConnecting()) throw new ViewConnectionException("Waiting to connect");
        
        // Now we'll check to make sure that given id has a valid URL, etc.
        
        // XXX this is where *real* name resolution should occur...i.e. using
        // a name service module such as the JNDI interface on top of the
        // PLS, etc.
        URL targetURL = viewToJoin.getURL();
        if (targetURL == null) throw new ViewConnectionException("Invalid URL for SessionViewID");
        // Now do real work.  This is left to subclasses to implement
        // Ok, we're in the correct state, so go ahead an attempt connection.
        // This will block until raw connection with group is established,
        // and throw a IOException if connection cannot be established at all.
        // If connection can be established (still not admitted at that point,
        // however), then after this returns isConnected() will be false, 
        // and isConnecting() will be true
        myWaitingToConnect = true;
        try {
            reallyJoinGroup(viewToJoin);
        } catch (IOException e) {
            // Clean up by resetting state variable and rethrow exception
            myWaitingToConnect = false;
            throw e;
        }
    }
    
    /**
     * Abstract method to really do work of making connection to
     * remote process group.  Should only be called from joinGroup, rather
     * than directly.  Assumes that view is not connected nor is waiting to
     * connect.  This method blocks until transport level connection is made
     * (but group level connection is not yet established...that only happens
     * when response is received from group manager).
     *
     * @param target the SessionViewID that identifies the target group manager 
     * for our connection
     * @exception IOException thrown if some problem making connection to
     * remote group
     */
    protected abstract void reallyJoinGroup(SessionViewID groupManager) throws IOException;
    
    /**
     * Primary method to leave a group.  Will throw an exception if not
     * already in a group, or group cannot be left.
     *
     * @exception Exception thrown if some problem with leaving the group
     */
    protected synchronized final void leaveGroup()
        throws Exception
    {
        // Make sure we're in appropriate state...i.e. connected
        if (!isConnected()) throw new ViewConnectionException("Not connected to group");
        // Send leave message to our currently known group manager
        leaveGroup(myGroupManager);
    }
    
    /**
     * Primary method to leave a group.  Group manager of group to be left is
     * specified as first parameter.  This method will generate a VIEW_LEAVE message
     * to the specified group manager.
     *
     * @exception Exception thrown if some problem with leaving the group
     */
    protected synchronized final void leaveGroup(SessionViewID groupManager)
        throws Exception
    {
        if (myGroupManager.equals(getID())) throw new ViewConnectionException("Cannot leave group because we are group manager");
        // Otherwise, send leave message to specified group manager
        sendSessionViewMsg(groupManager, SessionViewMsg.VIEW_LEAVE, getLeaveMsgData(groupManager));
    }
        
   /////////////////////// Methods that interact with list manager ////////////////
    /**
     * Notification from DObject (parameter is provided as key to assure
     * identity), that DObject has received and processed the 'created'
     * message from the local view.  This is passed on to the view list
     * manager, so that it can deal with this notification as desired
     * (typically by making some other state change on the DObject).
     *
     * @param object the DObject that is notifying us that it has received 
     * and processed the 'created' message
     */
    protected final void notifyDObjectCreated(DObject object)
    {
        myListManager.notifyDObjectCreated(object);
    }
    
    /**
     * Notification from DObject (parameter is provided as key to assure
     * identity), that DObject has received and processed the 'activated'
     * message from the local view.  This is passed on to the view list
     * manager, so that it can deal with this notification as desired
     * (typically by making some other state change on the DObject).
     *
     * @param object the DObject that is notifying us it has received 
     * and processed the 'activated' message
     */
    protected final void notifyDObjectActivated(DObject object)
    {
        myListManager.notifyDObjectActivated(object);
    }
    
    /**
     * Notification from DObject (parameter is provided as key to assure
     * identity), that DObject has received and processed the 'deactivated'
     * message from the local view.  This is passed on to the view list
     * manager, so that it can deal with this notification as desired
     * (typically by making some other state change on the DObject).
     *
     * @param object the DObject that is notifying us it has received 
     * and processed the 'deactivated' message
     */
    protected final void notifyDObjectDeactivated(DObject object)
    {
        myListManager.notifyDObjectDeactivated(object);
    }
    
    /**
     * Notification from DObject (parameter is provided as key to assure
     * identity), that DObject has received and processed the 'destroyed'
     * message from the local view.  This is passed on to the view list
     * manager, so that it can deal with this notification as desired
     * (typically by making some other state change on the DObject).
     *
     * @param object the DObject that is notifying us it has received 
     * and processed the 'destroyed' message
     */ 
    protected final void notifyDObjectDestroyed(DObject object)
    {
        myListManager.notifyDObjectDestroyed(object);
    }
    
    /**
     * Method for activating the given DObject according to local view
     *
     * @param id the DObjectID of the DObject to activate locally.  
     * @exception DObjectNotFoundException thrown if not found on appropriate
     * list
     */
    protected void activateDObjectLocally(DObjectID id) 
        throws DObjectNotFoundException
    {
        myListManager.activateDObject(id);
    }
    
    /**
     * Method for deactivating the given DObject according to local view
     *
     * @param id the DObjectID of the DObject to deactivate locally.  
     * @exception DObjectNotFoundException thrown if not found on appropriate
     * list
     */
    protected void deactivateDObjectLocally(DObjectID id) 
        throws DObjectNotFoundException
    {
        myListManager.deactivateDObject(id);
    }
    /**
     * Method for destroying the given DObject according to local view
     *
     * @param id the DObjectID of the DObject to destroy locally.  
     * @exception DObjectNotFoundException thrown if not found on appropriate
     * list
     */
    protected void destroyDObjectLocally(DObjectID id) 
        throws DObjectNotFoundException
    {
        myListManager.destroyDObject(id);
    }
    
   // End methods that interact with list manager
  
   // Methods to send View-level messages
   
    /**
     * Send group join message to group manager.  Assumes that relevant required
     * data is present in data parameter.  Exactly *what* this data consists of
     * is dependant upon the requirements of the group manager we are connecting to.
     *
     * @param groupManager the SessionViewID of the group manager we are trying
     * to reach
     * @param data the SessionViewData that we are sending to the group manager
     * @exception IOException thrown if some problem with queueing the message
     * @see #getJoinMsgData
     */
    protected void sendJoinMsg(SessionViewID groupManager, SessionViewData data)
        throws IOException
    {
        sendSessionViewMsg(groupManager, SessionViewMsg.VIEW_JOIN, data);
    }

    /**
     * Get data for join message.  This method is charged with the responsibility
     * of retrieving any and all data needed for sending in the group 'join'
     * message.  In general, this will consist of whatever data (e.g. authentication
     * data) that is needed by the group manager to grant/deny entry to the group.
     *
     * @param target the SessionViewID of the group manager to request entry from.  
     * @return SessionViewData the data to send with the join request to the group
     * manager.
     */
    protected SessionViewData getJoinMsgData(SessionViewID groupManager)
    {
        // XXX TODO.  
        // At least need to send URL we are trying to reach, our own view id,
        // and of course subclasses may very well wish to to other things
        return null;
    }
    
    /**
     * Send group leave message to group manager.  Assumes that relevant required
     * data is present in data parameter.  Exactly *what* this data consists of
     * is dependant upon the requirements of the group manager we are leaving.
     *
     * @param groupManager the SessionViewID of the group manager we are trying to 
     * leave
     * @param data the SessionViewData that we are sending to the group manager
     * @exception IOException thrown if some problem with queueing the message
     * @see #getLeaveMsgData
     */
    protected void sendLeaveMsg(SessionViewID groupManager, SessionViewData data)
        throws IOException
    {
       sendSessionViewMsg(groupManager, SessionViewMsg.VIEW_LEAVE, data);
    }
    
    /**
     * Get data for leave message.  This method is charged with the responsibility
     * of retrieving any and all data needed for sending in the group 'join'
     * message.  In general, this will consist of whatever data (e.g. authentication
     * data) that is needed by the group manager to grant/deny entry to the group.
     *
     * @param target the URL that identifies the group manager to request
     * entry from.
     * @return SessionViewData the data to send with the join request to the group
     * manager.
     */
    protected SessionViewData getLeaveMsgData(SessionViewID groupManager)
    {
        // For the leave message, by default we don't need to send anything
        // at all, so we return null.  Subclasses may override as appropriate
        return null;
    }
    
    /**
     * Check for whether given SessionViewID, with given SessionViewData (sent
     * with join request) should be allowed to enter the group.  This method
     * always should return a non-null instance of GroupAdmissionCheck.  Subclasses
     * will implement as necessary.  Caller (GroupConnectionHandler.
     * handleJoinMsg) should guarantee that this view object is the current
     * group manager (e.g. ClientView will throw if not).
     *
     * The implementations of this method in subclasses should *always* either
     * a) throw an exception; b) return a non-null GroupAdmissionCheck object.
     *
     * @param id the SessionViewID that is requesting group admission
     * @param data the data that accompanies the id provided
     * @return GroupAdmissionCheck object that contains the relevant result
     * information
     * @exception IOException thrown if some problem with this message to begin
     * with (i.e. ClientReceives this message).
     * @see GroupAdmissionCheck
     * @see GroupConnectionHandler#handleJoinMsg
     */
    protected abstract GroupAdmissionCheck checkForAdmission(SessionViewID id,
                                                    SessionViewData data)
       throws IOException;
    
    
    // Group manager messages for join requests.  Only the group manager should
    // receive these messages (this is guaranteed by the code that calls this
    // method
    
    /**
     * Send accept to join request.  Takes given group admission check,
     * adds session view id provided by admission check to group, and
     * sends a message to the existing group members about the change,
     * and to the new group member with the acceptance information, and 
     * information about the current contents of the group.  Synchronized
     * on the view list manager, so that we can be sure that the view does
     * not change while this change is being communicated.
     *
     * This method should only be called from the GroupConnectionHandler.
     * handleJoinMsg method.  It assumes that the result field in the 
     * GroupAdmissionCheck object is true (admitted)
     *
     * @param connection the ReliableConnection this request is coming from
     * @param admissionInfo the GroupAdmissionCheck to use for the new
     * group member.
     * @exception IOException thrown if some problem sending messages (should
     * not happen unless something happened with comm layer)
     */
    protected void sendJoinAccept(ReliableConnection connection, GroupAdmissionCheck admissionInfo)
        throws IOException
    {
        if (!isGroupManager()) throw new IOException("Not group manager");
        SessionViewID requestorID = admissionInfo.getViewID();
        // Synchronize on the list manager, to be sure view changes do not occur 
        // while this is taking place.  This is important.
        synchronized (myListManager) {
           // Add to our group
           addGroupMember(connection, requestorID);
           // Queue up response to requestor first
           sendSessionViewMsg(requestorID, SessionViewMsg.VIEW_CHANGE,
                                new SessionViewData(getRequestorAdmittedViewChange(admissionInfo)));
           // Then forward view change to all other views
           forwardFromRemoteViewExcluding(getID(), requestorID, SessionViewMsg.VIEW_CHANGE, 
                                new SessionViewData(getRequestorAdmittedOthersViewChange(admissionInfo)));
        }
    }
    
    /**
     * Get the ViewChange object for admitting a remote view
     *
     * @admissionInfo the GroupAdmissionCheck that has admitted a remote view
     * to a group that we manage
     */
    protected ViewChange getRequestorAdmittedViewChange(GroupAdmissionCheck admissionInfo)
    {
        SessionViewID requestorID = admissionInfo.getViewID();
        // Add all group members so new group member can know whose there
        VectorEx vect = new VectorEx(getCurrentGroupMembers());
        // Remove the receiver, and ourselves from the vector...to save on bandwidth
        vect.removeElement(getID());
        vect.removeElement(requestorID);
        return new ViewChange(null,true,admissionInfo.isAdmitted(),vect,getMaxRank(),admissionInfo.getResultData());
    }
    
    /**
     * Get the ViewChange object for admitting a remote view that will be sent to
     * all of the *other* views
     *
     * @param info the GroupAdmissionCheck that has admitted a remote view
     * to a group that we manage
     */
    private ViewChange getRequestorAdmittedOthersViewChange(GroupAdmissionCheck info)
    {
        return new ViewChange(info.getViewID(),true,info.isAdmitted(),null,getMaxRank(),info.getResultData());
    }
    
    /**
     * Method to increment (first) and then return rank for a view to be added.
     * Only actually implemented by the group manager.  Subclasses must therefore
     * provide implementation.  Client subclasses should always return 
     * VIEW_NOT_GROUP_MEMBER.  Server subclasses should return a valid integer
     * > VIEW_IS_GROUP_MANAGER.
     * 
     * @return int that gives a valid, unique rank in group to a new view
     */
    protected int getNextRank()
    {
        return myListManager.getGroupSize();
    }
       
    /**
     * Get the current maximum rank...this is the rank value of the *last* view
     * added to group.  Only actually used by the group manager.  Returns current
     * size of group -1.
     *
     * @return int maximum rank of the views currently in the group
     */
    protected int getMaxRank()
    {
        return myListManager.getGroupSize()-1;
    }
    
    /**
     * Send failure back to join request.  Takes given group admission check,
     * and sends failure response to failed session view only
     * <p>
     * This method should only be called from the GroupConnectionHandler.
     * handleJoinMsg method.  It assumes that the result field in the 
     * GroupAdmissionCheck object is false (not admitted)
     * <p>
     * @param connection the ReliableConnection this request is coming from
     * @param admissionInfo the GroupAdmissionCheck to use for the failure
     * response
     * @exception IOException thrown if some problem sending message (should
     * not happen unless something happened with comm layer)
     */
    protected void sendJoinFailure(ReliableConnection connection, GroupAdmissionCheck admissionInfo)
        throws IOException
    {
        // Assert that we are group manager...if not, throw
        if (!isGroupManager()) throw new IOException("Not group manager");
        SessionViewID requestorID = admissionInfo.getViewID();
        // Synchronize on the list manager, to be sure view changes do not occur 
        // while this is taking place
        synchronized (myListManager) {
            // Queue up failure response to requestor...they are history
            connection.queuePacket(new SessionViewPacket(getID(), requestorID, SessionViewMsg.VIEW_CHANGE,
                                   new SessionViewData(getRequestorFailedViewChange(admissionInfo))));
            // Call any view subclass code to remove view id appropriately
            removeGroupMember(connection, requestorID);
        }
    }
    
    /**
     * Get the ViewChange object for rejecting a remote view
     *
     * @param info the GroupAdmissionCheck that has admitted a remote view
     * to a group that we manage
     */
    private ViewChange getRequestorFailedViewChange(GroupAdmissionCheck admissionInfo)
    {
        // Just return view change that has appropriate elements
        // from admissionInfo object
        return new ViewChange(null,true,admissionInfo.isAdmitted(),null,VIEW_NOT_GROUP_MEMBER,admissionInfo.getResultData());
    }
    
    // End group manager messages for join requests

    // Responses from group manager to (our) join requests
    
    /**
     * Our request was granted, and we're now in group.  Update group information
     * and local state
     *
     * @param groupManager the SessionViewID of the groupManager
     * @param viewChange the ViewChange object from the groupManager
     * @exception IOException thrown if some problem dealing with this information
     * (i.e. we don't like the ID of this group manager
     */
    protected void addGroupSuccessful(SessionViewID groupManager, ViewChange viewChangeInfo)
        throws IOException
    {
        // Verify that we are still in waiting state.  If we are not, just *ignore*
        // this message...will have to guarantee that connection is closed through
        // some other path
        if (!isConnecting()) return;
        // OK, we're in correct state, so let's go ahead
        // First, get VectorEx of SessionViewIDs from the data
        VectorEx vect = viewChangeInfo.getIDs();
        synchronized (myListManager) {
            // Put id of group manager in group
            addGroupMember(null, groupManager);
            // Put any/all other ids in local list and notify everyone about it
            if (vect != null) {
                try {
                    for(Enumeration e=vect.elements(); e.hasMoreElements(); ) {
                        addGroupMember(null, (SessionViewID) e.nextElement());
                    }
                } catch (ClassCastException e) {
                    // This should never happen...if it does, the group manager
                    // has sent us bogus data, and we're outta here
                    throw new IOException("Invalid data...SessionViewID cast failed");
                }
            }
            // The following method is synchronized on "this", so we must be
            // careful.  The ViewListManager *must not call synchronized methods*,
            // particularly on the View (it doesn't now, but this must be maintained)
            endConnecting(groupManager, viewChangeInfo.getRank());
        }
    }
    
    /** 
     * Our attempt to join a group has failed.
     *
     * @param groupManager the SessionViewID of the group manager that rejected us
     * @param viewChangeInfo the viewChangeInfo sent with the rejection
     */
    protected void addGroupFailure(SessionViewID groupManager, ViewChange viewChangeInfo)
    {
        // Subclasses might want to override to (e.g.) notify caller about the add failure
        // Otherwise, all we have to do is to set appropriate state variables
        endConnecting(null, VIEW_NOT_GROUP_MEMBER);
    }
    
    // End responses from group manager to join requests

    // Group manager messages for leave requests
    
    /**
     * Send leave accept message back to view id (wrapper by result parameter).
     * Atomically (synchronized on list manager) removes view id and then
     * sends message to view telling them that they are now out.
     *
     * @param result the GroupAdmissionCheck instance that wraps the view id
     * that is leaving
     * @exception IOException is thrown if some io problem
     * @see GroupConnectionHandler#handleLeaveMsg
     */
    protected void sendLeaveAccept(ReliableConnection connection, GroupAdmissionCheck result)
        throws IOException
    {
        if (!isGroupManager()) throw new IOException("Not group manager");
        SessionViewID requestorID = result.getViewID();
        synchronized (myListManager) {
            // Queue up failure response to requestor...they are history
            sendSessionViewMsg(requestorID, SessionViewMsg.VIEW_CHANGE, 
                                            new SessionViewData(getLeaveRequestorViewChange(result)));
            // Then forward view change to all other views
            forwardFromRemoteViewExcluding(getID(), requestorID, SessionViewMsg.VIEW_CHANGE, 
                                new SessionViewData(getLeaveOthersViewChange(result.getViewID())));
            // Call any view subclass code to remove view id appropriately
            removeGroupMember(connection, requestorID);
        }
    }
    
    /**
     * Get the view change information for the requestor that is leaving.  This
     * information is returned to the leaving view
     * 
     * @param admissionInfo the admission info associated with the departure.  If
     * this parameter is null, it means that the view left unexpectedly...i.e. 
     * because of a crash
     */
    private ViewChange getLeaveRequestorViewChange(GroupAdmissionCheck admissionInfo)
    {
        // Just return view change that has appropriate elements
        // from admissionInfo object
        return new ViewChange(null,false,true,null,VIEW_NOT_GROUP_MEMBER,null);
    }

    /**
     * Get the view change information for the that are not leaving about a 
     * view that is leaving.  This information is returned to all other views
     * 
     * @param admissionInfo the admission info associated with the departure
     */
    protected ViewChange getLeaveOthersViewChange(SessionViewID goneViewID)
    {
        return new ViewChange(goneViewID,false,true,null,VIEW_NOT_GROUP_MEMBER,null);
    }
    
    // End group manager handlers for leave requests
    
    // Responses from group manager to leave requests (non group manager)
    
    /**
     * Leave request response received and our leave request was successful.  In
     * response to this (client only in client-server topology), we just close
     * the connection.
     *
     * @param groupManager the SessionViewID of the group manager responding to
     * our departure request
     * @param viewChange the ViewChange that the group manager sent back to us
     */
    protected void leaveGroupSuccessful(SessionViewID groupManager, ViewChange viewChangeInfo)
    {
        // Subclasses might want to override to (e.g.) notify caller about the add failure
        // Otherwise, all we have to do is to set appropriate state variables
        debug("Received leave group OK from group manager...closing");
        endConnecting(null, VIEW_NOT_GROUP_MEMBER);
    }
    // End responses from group manager to our leave requests
    
    // Message handler for a DObjectPacket
    /** 
     * Handle a message indicating that some remote DObject presence has sent data
     * to its DObject presence on this machine.  Note this is the message receiver
     * for <b>*all* DObject presence-to-presence messaging</b>.  All this handler
     * does is process the packet by looking for the DObject identified by the
     * appropriate DObjectID and asynchronously giving this packet to the target
     * DObject presence (the actual delivery is synchronized on the list manager
     * to prevent View changes from happening simultaneously.  This is very 
     * important).
     * <p>
     * NOTE:  This is essentially a message demultiplexor.  It takes a generic
     * message intended for this View, interprets some message routing bits (the
     * DObjectID) and then finds and delivers to the corresponding DObject.
     * <p>
     * @param fromID the SessionViewID of the sending view
     * @param toID the SessionViewID of this view
     * @param packet the DObjectPacket that is to be delivered
     */
    protected void handleDObjectPacketMsg(SessionViewID fromID, SessionViewID toID, DObjectPacket packet)
        throws IOException
    {
        debug("Got DObject packet from "+fromID+", packet: "+packet);
        // If packet is null, just ignore.  This should not happen, however.
        if (packet == null) {
            // XXX Might log this
            debug("View.handleDObjectPacketMsg.  Packet is null.  Ignoring.");
            return;
        }
        DObjectID objectID = packet.getFromID();
        // If the packet doesn't have a DObjectID associated with it, it is bogus
        // and so we ignore
        if (objectID == null) {
            debug("View.handleDObjectPacketMsg.  Object fromID is null.  Ignoring.");
            // XXX might log this
            return;
        }
        // OK, now we're ready to actually deliver the packet to the target
        // DObject presence on this machine.
        //
        // Do the following while synchronized on the list manager, so
        // that we can be sure that membership does not change while this is
        // taking place
        synchronized (myListManager) {
            // If the message is directed to all, or to us specifically then we try
            // to deliver it locally
            if (toID == null || toID.equals(getID())) {
                DObjectViewFacet objectFacet = myListManager.getDObjectViewFacetFromAnywhere(objectID);
                if (objectFacet != null) {
                    try {
                        debug("  Delivering packet to DObject "+objectID);
                        objectFacet.receiveDataFromRemote(fromID, packet.getData());
                    } catch (Exception e) {
                        // This should not happen
                        // If it does, however, we're going to kill the connection
                        throw new IOException("View.handleDObjectPacketMsg.  Could not deliver data to local presence");
                    }
                } else {
                    // This means DObject not found locally, so ignore
                    debug("DObject "+objectID+" not found locally, ignoring message "+packet.getData());
                }
            } else {
                debug("  DObject packet from "+objectID+" not meant for us");
                // DObject msg not to be delivered locally...no matter, we can still 
                // serve as a msg router if we are a server...but we might want to log this
            }
            // Do fanout...these handle all fanout (server only).  NOTE:  ClientView implements
            // this as NOP.
            forwardMsg(fromID, toID, SessionViewMsg.SEND_DOBJECT_PACKET, new SessionViewData(packet));
        }
    }
    
    // Messages for creating remote instances of DObjects
    
    /**
     * Send create DObject message to remote view.  Assumes that relevant required
     * data is present in CreateDObjectData data parameter.  Exactly *what* 
     * this data consists of (in addition to the basic required stuff) is dependant 
     * upon the requirements of the group manager we are asking.
     *
     * @param toID the SessionViewID of the view where we are trying to create a DObject on
     * @param data the SessionViewData that we are sending to the remote view
     * @exception IOException thrown if some problem with queueing the message
     * @see #getCreateMsgData
     */
    protected void sendCreateMsg(SessionViewID toID, CreateDObjectData data)
        throws IOException
    {
        sendSessionViewMsg(toID, SessionViewMsg.CREATE_DOBJECT, getCreateMsgData(toID, data));
    }

    /**
     * Get data for create message.  This method is charged with the responsibility
     * of retrieving any and all data needed for sending in the group 'create'
     * message.  In general, this will consist of whatever data (e.g. authentication
     * data) that is needed by the remote to grant/deny entry to the group.
     *
     * @param target the SessionViewID of the remote view to receive the request
     * @param data the CreateDObjectData that will be sent to the remote view
     * @return SessionViewData the data to send with the create request
     */
    protected SessionViewData getCreateMsgData(SessionViewID toID, CreateDObjectData data)
    {
        // XXX TODO.  
        // For now, no data expected, we'll just send createdobjectdata.
        return new SessionViewData(data);
    }
    
    /**
     * Handle create message from remote.  This is the main message handler for
     * the DObject create message, which is responsible for creating <b>all</b>
     * DObject client presences.  This interprets the create message, calls a
     * method (<b>checkCreateDObject</b>) that allows a local security check to
     * be done before the DObject presence is created, and if successful begins
     * the DObject creation (which may include classloading etc., so can't be
     * done synchronously), by creating an instance of LoadingDObject, which is
     * a 'placeholder' object used to represent the new DObject presence while
     * its classes are loaded.
     *
     * @param fromID the SessionViewID of the View where this message originated
     * @param toID the SessionViewID of the View where this message is intended...should
     * be our id
     * @param data the CreateDObjectData wrapper object used to contain all of 
     * the data associated with this create message (all of the data needed to
     * create the DObject presence)
     * @see LoadingDObject
     */
    protected void handleCreateMsg(SessionViewID fromID, SessionViewID toID, CreateDObjectData data)
        throws IOException
    {
        // All of this must take place without group membership changes
        synchronized (myListManager) {
            if (toID == null || toID.equals(getID())) {
                // First, do check on local view to make sure DObject is OK to be created
                if (checkDObjectCreate(fromID, toID, data)) {
                    // Then create a 'chimera' of the DObject that is being created
                    // This object represents the DObject being loaded, but is just
                    // a placeholder for the real object.  The LoadingDObject instance
                    // creates a new thread, calls (blocking) calls to load the classes,
                    // create an instance, and then get state from remote view that made
                    // request (fromID), and then moves the (actual new instance to 
                    // inactive (or active) status on local machine
                    debug("View.handleCreateMsg.  Adding new LoadingDObject with id "+data.getID()+" from view "+fromID);
                    try {
                        myListManager.addDObjectToLoading(new LoadingDObject(this, new ViewDObjectFacet(this), fromID, data));
                    } catch (Exception e) {
                        // Should not happen, but if it does throw and we're outta here
                        dumpStack(e, "View.handleCreateMsg");
                        throw new IOException("View.handleCreateMsg.  Could not create LoadingDObject");
                    }
                } else {
                    // This means that we are not going to allow the object to be created
                    // locally, but we will allow the message to be passed on to
                    // other remotes (server)
                }
            } else {
                debug("Create msg not intended for us.  Forwarding");
            }
            // Do fanout...these handle all fanout (server only).  Client implements
            // this as nop.
            forwardMsg(fromID, toID, SessionViewMsg.CREATE_DOBJECT, new SessionViewData(data));
        }
    }
    
    /**
     * Forward a given msg to either a) all known Views (toID == null), or a specific
     * View (toID != null).
     *
     * @param fromID the SessionViewID of the sending View
     * @param toID the SessionViewID of the receiving View (null means all)
     * @param msg the session view msg identifier (see SessionViewMsg class for 
     * relevant values
     * @param data the SessionViewData (the actual data) for the message
     */
    protected void forwardMsg(SessionViewID fromID, SessionViewID toID, int msg, SessionViewData data)
    {
        if (toID == null) {
            // Now, forward on to everyone else.  Only does anything on a server...
            forwardFromRemoteViewExcluding(fromID, fromID, msg, data);
        } else {
            forwardFromRemoteViewToRemoteView(fromID, toID, msg, data);
        }
    }
    
    /**
     * This is the method called by the DObject creation code, to allow implementors
     * of this class to check the validity of DObject presence creation requests,
     * and potentially refuse them as desired.  Note that this method is the locus
     * the implementation of whatever TOS check are desired wrt to DObject presence
     * creation (i.e. this is not for checking group entry requests, rather this
     * allows checks and potential refusal for each individual client presence that
     * the remote DObject host would like to construct.
     *
     * @param fromID the SessionViewID of the View where request originated from
     * @param toID the SessionViewID of the View where request is intended (can be
     * null, but if non-null should be this View's SessionViewID).
     * @param data the CreateDObjectData that holds all of the relevant information
     * about the DObject to be putatively created
     * @return true if a DObject presence should be allowed, false otherwise
     * @exception IOException thrown if immediate disconnect should occur
     */
    protected boolean checkDObjectCreate(SessionViewID fromID, SessionViewID toID, CreateDObjectData data)
        throws IOException
    { 
        // XXX TODO...this method checks whether a given DObject should be created
        // locally.  If OK, returns true.  If not OK, returns false.  If request
        // should result in immediate disconnect, throws ioexception
        return true;
    }
    
    // Partition handling methods 
    
    /**
     * End our view's status of being in a 'connecting' state.  This method
     * should be synchronized on the view object, so that a) the transition to
     * a new state for this view (either connected or not) is atomic to
     * this view; b) notifyAll() can be called so any waiting threads
     * (joinGroup or leaveGroup methods on view) can be allowed to 
     * continue. 
     * <p>
     * If the groupManager is null, this means that we have left the group,
     * and in that case we attempt to permanently close the connection to
     * the group.  If the groupManager is non-null, we have just entered a
     * group successfully, and things may continue.
     * <p>
     * NOTE:  Any subclass implementation of this method should be synchronized.
     * <p>
     * @param groupManager the SessionViewID of our group manager.  If null,
     * this means that we have successfully left a group.  If non-null, we
     * have successfully been added to a group
     * @param rank our rank in the group (provided by manager)
     */
    protected abstract void endConnecting(SessionViewID groupManager, int rank);
    
    /**
     * Handle disconnect of a connection.  This method deals with any exception
     * from our underlying group messaging protocol.  This is called by the
     * GroupConnectionHandler, and that, in turn, is called by the sender/receiver
     * threads on a given connection.
     * <p>
     * This method *must* ultimately (and synchronously) close the connection 
     * provided in the first parameter.  Before doing so, we close and then deal 
     * with any message on the queue provided as the third parameter...these are 
     * any unsent messages on the queue when the exception was raised.
     *
     * @param connection the Reliable connection that is the source of the exception
     * @param e the Exception that caused this to be called
     * @param unsent the Queue holding any unsent messages on the given connection
     */
    protected void handleDisconnect(ReliableConnection connection, Exception e, Queue unsent)
    {
        // We've received an exception that indicates a disconnect has occurred.
        // Now it's up to us to decide what to do about it.  We *must*, however,
        // ultimately close the connection to impose a 'fail stop' model for dealing
        // with partial failure.  That is, to be able to deal with partial failure
        // we will impose a model which says that if this event occurs, that the
        // local view (group manager or not) must deal with it by forcibly removing 
        // the view corresponding to this connection from the group. 
        
        // We close the Queue first, so that it can't queue any more messages, and 
        // other client code starts to know about the failure
        if (unsent != null) unsent.close();
        // Now deal with any messages still in queue
        handleUnsentMessagesOnDisconnect(unsent);
        // Then close the connection for good.  This will result in the appropriate
        // notification and DObject client presence destruction via the ViewListManager
        closeConnection(connection);
    }
    
    /**
     * Handle any unsent message that exist in queue upon disconnect.  This gives
     * the view an opportunity to deal with such messages by, for example, letting
     * DObjects know about any unsent messages.
     *
     * @param unsent a Queue containing unsent messages.  The Queue will already
     * have been closed, and so nothing can be added to it.
     */
    protected void handleUnsentMessagesOnDisconnect(Queue unsent) 
    {
        // XXX TODO
        // By default, the only thing to do is to drop all the pending messages
        // We probably will want, however, to have code here notifiying client
        // DObjects about any unsent messages that they might have on this queue
        // Not critical right now, however
    }
    
    /**
     * Forcibly close the connection given.  This is called to deal with an 
     * exception reported by the connection's thread.  Subclasses may/should
     * override to do what they need to do to clean up for the given
     * connection.  For example, clients will set their connection to null,
     * server's will remove the given connection from whatever list it is on.
     *
     * @param connection the ReliableConnection in question
     */
    protected void closeConnection(ReliableConnection connection)
    {
        try {
            connection.close();
        } catch (IOException e) {
            dumpStack(e, "closeConnection: Error in close.");
        }
    }
    
    // End partition handling methods

    /**
     * Forwards msg to all group members, using the 'fromID' parameter to 
     * specify where this message is from.  This is implemented by the server
     * only for fanout.  This does all fanout of all messages!  Client implements
     * as a nop.
     *
     * @param fromID the SessionViewID to put in 'fromID' field of the packet
     * @param excluded a SessionViewID to exclude.  If null, everyone is sent the message
     * @param msg the SessionViewMsg to send
     * @param data the SessionViewData to send with the message
     */
    protected abstract void forwardFromRemoteViewExcluding(SessionViewID fromID, 
                                                           SessionViewID excluded,
                                                           int msg, 
                                                           SessionViewData data);
        
    /**
     * Forwards msg to one specific group member, using the 'fromID' parameter to 
     * specify where this message is from.  This is implemented by the server
     * (only).  This does message routing.
     *
     * @param fromID the SessionViewID to put in 'fromID' field of the packet
     * @param toID the SessionViewID to put in the 'toID' field of the packet
     * @param msg the SessionViewMsg to send
     * @param data the SessionViewData to send with the message
     */
    protected abstract void forwardFromRemoteViewToRemoteView(SessionViewID fromID, 
                                                              SessionViewID toID,
                                                              int msg, 
                                                              SessionViewData data);
    
    /**
     * Add a group member to our set of known group members.  The connection is
     * also (sometimes) passed through, so that subclasses that override this
     * method can get at the connection that directly corresponds to the given
     * viewid.
     * <p>
     * NOTE:  If subclasses override this method, they should call super.addGroupMember
     * as part of their code.
     *
     * @param connection the ReliableConnection this group member is coming from
     * (can be null)
     * @param viewID the SessionViewID associated with this new group member
     */
    protected void addGroupMember(ReliableConnection connection, SessionViewID viewID)
    {
        debug("addGroupMember: Adding id "+viewID+" to group with connection "+connection);
        myListManager.addGroupMember(viewID);
    }
    
    /**
     * Remove a group member from our set of known group members.  The connection is
     * also (sometimes) passed through, so that subclasses that override this
     * method can get at the connection that directly corresponds to the given
     * viewid.
     * <p>
     * NOTE:  If subclasses override this method, they should call super.removeGroupMember
     * as part of their code.
     *
     * @param connection the ReliableConnection this group member is coming from
     * (can be null)
     * @param viewID the SessionViewID associated with this group member to remove
     */
    protected void removeGroupMember(ReliableConnection connection, SessionViewID viewID)
    {
        debug("removeGroupMember: Removing id "+viewID+" from group with connection "+connection);
        myListManager.removeGroupMember(viewID);
    }
    
    /**
     * Generic meta-interface for DObjects to get facets on the local view.
     * This interface can be used to make arbitrary requests of the local
     * view securely.  The untrusted subclass of this class cannot call the
     * view.getViewFacet method directly, but rather only through this
     * call.
     * 
     * @param object the DObject making the request
     * @param request a String identifying the desired service from the view
     * @param data arbitrary data associated with the request
     * @return BaseFacet that is a facet on the local view.  Can be null if
     * view does not wish to provide a facet for the service.
     * @see ViewDObjectFacet#getViewFacet
     */
    protected BaseFacet getViewFacet(DObject object, String request, Object data)
    {
        // XXX TODO this will be a generic meta interface for getting
        // additional facets on this view
        return null;
    }
    
    /**
     * Meta interface for responding to DObject initial requests for a run queue
     * to support its own code execution.  This is called by the ViewDObjectFacet 
     * getDObjectRunQueue method, in response to a request from a new DObject.  By
     * changing the contents of this method, a View can control what run queue
     * a DObject uses to run its code.  
     * <p>
     * By default a new instance of a Thread object is created with a new
     * DObjectRunner to define the message processing loop.  Also a new Queue 
     * object is allocated and some implementer of the Enqueable interface
     * is returned (in the default case it is an instance of the Queue class itself.  
     * <p>
     * Note that new types Enqueables can be created and returned such that a
     * single run queue is shared, or whatever combination of single/multiple
     * run queues are desired.
     *
     * @param object the DObject that is making the request
     * @return Enqueable that we wish to provide back to the DObject
     * @see ViewDObjectFacet#getDObjectRunQueue
     * @see DObjectRunner
     * @see dom.util.Queue
     */
    protected Enqueable getDObjectRunQueue(DObject object)
    {
        // XXX This is the one spot to modify when the move is
        // taken to using a single run queue.  Trivial.
        Queue aQueue = new Queue();
        Thread aThread = new Thread(new DObjectRunner(object, aQueue), "RQ for "+object+", id: "+object.getID().toString());
        aThread.start();
        return (Enqueable) aQueue;
    }

    
    
    // Methods to create/add new DObjects
    
    /**
     * Get the classloader for loading in remote classes for DObjects.  Returns 
     * either a valid ViewClassLoader classLoader, or it returns null.  
     * If it returns null, then the null classloader will be used for loading in
     * any required classes.
     *
     * @param id the DObjectID of the DObject that is to be loaded
     * @param homeID the SessionViewID of the 'home' session for this object
     * @param codeBase the URL codebase for the root class
     * @param className the URL classname for the root class
     */
    protected ViewClassLoader getDObjectClassloader(DObjectID id, SessionViewID homeID,
        URL codeBase, String className)
    {
        // For this abstract superclass, always returns null
        // Subclasses have the right to override if they wish
        // and provide their own classloader
        return null;
    }
    
    /**
     * Load class(es) and create a new instance of a DObject (by using CRAPI
     * getConstructor call).  If provided classloader is null, uses Class.forName
     * to load class (locally).
     *
     * @param id the DObjectID of the DObject that is to be loaded
     * @param homeID the SessionViewID of the 'home' session for this object
     * @param codeBase the URL codebase for the root class
     * @param className the URL classname for the root class
     * @param initParams an Object that defines any/all params used by
     * DObject subclasses for init
     * @exception Exception thrown if one of a number of problems occurs
     * @see java.lang.reflect.Constructor#newInstance
     */
    protected DObject loadDObject(DObjectID id, 
                                  SessionViewID homeID, 
                                  URL codeBase, 
                                  String className,
                                  Object initParams)
        throws Exception
    {
        // OK, first, we'll get the classloader for this guy appropriately
        ViewClassLoader loader = getDObjectClassloader(id, homeID, codeBase, className);
        
        Class theClass = null;
        if (loader == null) {
            theClass = Class.forName(className);
        } else {
            theClass = loader.loadClass(codeBase, className);
        }
        // OK, we have the class now.  We create an array of Class object types
        // to find the appropriate constructor.  Find the constructor with the
        // appropriate reflection call, and then call the constructor
        Constructor theConstructor = theClass.getDeclaredConstructor(dobjectConstructorTypes);
        if (theConstructor == null) throw new NoSuchMethodException("Constructor not found on class");
        
        Object params[] = new Object[5];
        params[0]=new ViewDObjectFacet(this);params[1]=id;params[2]=homeID;params[3]=codeBase;params[4]=initParams;
        
        // Make new instance using constructor...it had better be a subclass
        // of DObject, or otherwise this will throw
        // This guarantees that the object will have certain functionality
        // as we expect.  The classloader will guarantee that the DObject
        // class is loaded from the local file system, and the methods that
        // we use are declared final.
        return (DObject) theConstructor.newInstance(params);
    }
    
    /**
     * Move the DObject identified by the first parameter, and with reference
     * given by the second parameter from the loading queue (which has a reference
     * to an instance of LoadingDObject) to the inactive queue.
     *
     * @param id the DObjectID of the DObject
     * @param object the DObject reference
     * @exception DObjectNotFoundException if the given DObject is not found on
     * the loading queue
     */
    protected void moveLoadingToInactive(DObjectID id, DObject object)
        throws DObjectNotFoundException
    {
        myListManager.createDObject(id, object);
    }

    /**
     * Move the DObject identified by the first parameter from the inactive queue 
     * to the active queue.
     *
     * @param id the DObjectID of the DObject
     * @exception DObjectNotFoundException if the given DObject is not found on
     * the loading queue
     */
    protected void moveInactiveToActive(DObjectID id)
        throws DObjectNotFoundException
    {
        myListManager.activateDObject(id);
    }
    
    /**
     * Remove the DObject identified from the loading queue.  This is idempotent
     * and assures that the given DObject is really removed
     * 
     * @param id the DObjectID of the DObject to remove
     */
    protected void removeDObjectFromLoading(DObjectID id)
    {
        try {
            myListManager.removeDObjectFromLoading(id);
        } catch (Exception e) {
            // If this happens, we're going to ignore it
        }
    }
    
    /**
     * Generic interface for creating new DObject instances.  Takes a creator
     * object, so that security checks can be made, a codebase to determine
     * from where to load the root class (can be http, ftp, etc., as well as
     * repository, file system, etc).  Takes a class name that is the root
     * class to load, and and array of objects to represent the parameters
     * to be passed to the constructor.  After doing the classloading, 
     * using CRAPI, this method looks for a constructor with the param types 
     * defined by the types of the objects in the params array, and then 
     * tries to invoke that constructor.  If it fails for any reason then 
     * this failure propogates back up in the form of an exception.  Upon 
     * success, the new object's DObjectID is returned for use by the caller.
     * <p>
     * Calling thread blocks until complete, so this should only be used by 
     * code that is prepared to deal with that.
     *
     * @param creator the DObject that wants to create a new object hosted
     * on the local view.  If null, being created by the system.
     * @param id the DObjectID to use for the new DObject.  If null, new id
     * is created and assigned by this code.
     * @param homeViewID the home view id for the new DObject.  Determines 
     * whether a host instance (homeViewID==getID()), or a client instance
     * (homeViewID != getID()) is being created.
     * @param codebase the URL that identifies the codebase for the new object
     * @param className the String identifying the class name (relative to
     * codebase) of the root class to be loaded (this class MUST be a subclass
     * of DObject or this will fail
     * @param param an Object will be passed to the constructor for the newly 
     * created DObject
     * @param activate if true, DObject is activated as soon as possible, if
     * not, DObject left inactive until explicitly told to activate
     * @exception Exception is thrown for a variety of different reasons
     * @see java.lang.reflect.Constructor
     * @see dom.session.ViewClassLoader
     * @see #loadDObject
     * @see DObject
     */
    protected DObjectID createDObject(DObject creator, 
                                      DObjectID id, 
                                      SessionViewID homeViewID,
                                      URL codebase,
                                      String className, 
                                      Object param,
                                      boolean activate)
        throws Exception
    {
        // If a null id is passed in, then create a new one here.
        if (id == null) id = getNewDObjectID();
        // Load class, call constructor, etc
        DObject newDObject = loadDObject(id, homeViewID, codebase, className, param);
        // addNewDObject
        if (addNewDObject(newDObject, activate)) {
            return newDObject.getID();
        } else return null;
    }
    
    /**
     * Subclass overrideable helper method that returns a new, unique
     * DObjectID.  This is used in the createDObject method above.
     *
     * @return DObjectID a new, fresh DObjectID
     * @exception Exception thrown if some problem creating id
     * @see #createDObject
     */
    protected DObjectID getNewDObjectID() throws Exception
    {
        return DObjectID.makeNewID();
    }
    
    /**
     * Method to directly add a DObject to set known to this view.  If activate is
     * true, will also activate DObject as soon as possible.
     *
     * @param object the DObject to add to the group
     * @param activate if true the DObject is activated immediately, if false,
     * it is left in the inactive list until explicitly activated
     */
    protected boolean addNewDObject(DObject object, boolean activate)
    {
        return myListManager.addNewDObject(object, activate);
    }
    
    ///////////// Capability request interfaces for accessing DObjects
    
    /**
     * Interface for DObject <-> DObject capability requests.  This is the interface
     * for DObjects to make requests for capabilities of other DObjects securely,
     * within the context provided by this view.  This method is only called via
     * the ViewDObjectFacet, and is protected so no untrusted code can call it.
     * <p>
     * If the recipient of this request wishes to respond, it will do so by
     * calling giveCapability on its ViewDObject facet, which will try to give
     * back the capability provided to the requestor via a CapabilityReceipt object.
     *
     * @param requestor the DObject doing the requesting 
     * @param target the DObjectID of the target of the request
     * @param requestName a String identifying the request name
     * @param data any data that the requestor wishes to send along (some targets may
     * require that some data is sent along
     * @exception Exception thrown under different failure conditions
     * @see CapabilityReceipt
     * @see ViewDObjectFacet#requestCapability
     * @see #giveDObjectCapability
     * @see DObject#requestCapability
     */
    protected void requestDObjectCapability(DObject requestor, DObjectID target, String requestName, Object data)
        throws Exception
    {
        // Create receipt
        CapabilityReceipt newReceipt = new CapabilityReceipt(this, requestor.getID(), target, requestName, data);
        DObjectViewFacet targetFacet = myListManager.getDObjectViewFacetFromAnywhere(target);
        if (targetFacet == null) {
            throw new DObjectNotFoundException(target);
        }
        // Now, asynchronously send request to target
        debug("View.requestDObjectCapability.  Created and forwarding receipt "+newReceipt);
        // Add receipt to list of currently pending receipts
        addReceiptToPending(newReceipt);
        try {
            targetFacet.requestCapability(newReceipt);
        } catch (Exception e) {
            removeReceiptFromPending(newReceipt);
            throw e;
        }
    }
    
    /**
     * Add a given CapbilityReceipt to the pending receipts list.  This is done
     * from within the View.requestDObjectCapability call to keep track of 
     * receipts issued, but not redeemed by the capability grantor.
     *
     * @param aReceipt the CapabilityReceipt object to add to the list.  Assumed
     * not to be null
     */
    private void addReceiptToPending(CapabilityReceipt aReceipt)
    {
        myCapabilityReceipts.addElement(aReceipt);
    }

    /**
     * Give a DObject that requested a certain capability a response.  This is
     * called from the ViewDObjectFacet (only), when a capability grantor
     * decides to respond to a capability request made by View.requestDObjectCapability.
     * Receipt ignored if it is not on the pending list...which could happen
     * if original requestor gets removed (is destroyed) before a response
     * happens.  
     *
     * @param receipt the CapabilityReceipt that is the response
     * @exception Exception thrown if (e.g.) the DObjectViewFacet for the 
     * original requestor has been nulled out
     */
    protected void giveDObjectCapability(CapabilityReceipt receipt)
        throws Exception
    {
        // Make sure receipt is still in pending list
        CapabilityReceipt theReceipt = removeReceiptFromPending(receipt);
        if (theReceipt != null) {
            // Only continue if the receipt still in pending list
            // Now get receiver of capability
            DObjectViewFacet aFacet = myListManager.getDObjectViewFacetFromAnywhere(receipt.getRequestorID());
            if (aFacet == null) throw new NullFacetException();
            DObjectFacet theFacet;
            if ((theFacet = theReceipt.getFacet()) != null) {
                // Stamp the facet with ourselves so we can revoke
                theFacet.setView(this);
                // Stamp the facet with the id for the receiver 
                theFacet.setReceiverID(theReceipt.getRequestorID());
                debug("View.giveDObjectCapability.  Delivering capability "+theFacet+" back to "+receipt.getRequestorID());
                // Now deliver the thing...
                aFacet.receiveCapability(theFacet);
                // Only add the facet to our list of delivered facets if the receiver can actually receive the facet
                addDObjectFacet(theFacet);
            }
        }
    }
    
    /**
     * Add given DObjectFacet to the Vector of facets maintained by the View.  Reference
     * to these facets are maintained by this View so that they can be revoked and 
     * removed forcibly by this view in the event of partition.  This guarantees that
     * all DObjectFacets are automatically cleaned up in the event of a partition
     * (trusted or untrusted alike).
     *
     * @param aFacet the DObjectFacet to add
     * @see #revokeAndRemoveDObjectFacet
     */
    protected void addDObjectFacet(DObjectFacet aFacet)
    {
        myDObjectFacets.addElement(aFacet);
    }
    
    /**
     * Call DObjectFacet.revoke(View) and then remove the given facet from our
     * list of facets.  This cleans up the given facet.
     *
     * @param aFacet the DObjectFacet to remove
     * @exception NullFacetException thrown if dobject facet has already been revoked
     */
    protected void revokeAndRemoveDObjectFacet(DObjectFacet aFacet)
        throws NullFacetException
    {
        aFacet.revoke(this);
        // Then remove it from our list.  This allows facet to be garbage collected
        // as long as nothing else (e.g. another DObject that doesn't clean up
        // properly) holds onto the facet.  Even if something else holds onto it,
        // however, the revoke above will assure that the actually references to
        // the DObject instance itself (and all its code) will be freed.
        myDObjectFacets.removeElement(aFacet);
    }
    
    /**
     * Revoke and then remove all DObjectFacets for given ID.
     *
     * @param id the DObjectID for which all DObjectFacets will be revoked
     */
    protected void removeFacetsForID(DObjectID id)
    {
        synchronized (myDObjectFacets) {
            for (Enumeration e=myDObjectFacets.elements(); e.hasMoreElements(); ) {
                DObjectFacet theItem = (DObjectFacet) e.nextElement();
                try {
                    DObjectID facetID = theItem.getID();
                    if (facetID.equals(id)) revokeAndRemoveDObjectFacet(theItem);
                } catch (NullFacetException except) {
                    // Ignore, except for debug
                    dumpStack(except, "View.removeFacetsForID.  Trying to revoke and remove facet");
                }
            }
        }
    }
    
    /**
     * Method to remove a CapabilityReceipt from the pending receipts list.
     * Called from giveDObjectCapability (if grantor responds, one way or the
     * other), and from removeReceiptsForID, which removes the receipts that
     * are pending if one or both of the parties disappear without completing
     * the protocol (on partition, etc.).
     *
     * @param aReceipt the CapabilityReceipt to remove
     * @return the CapabilityReceipt removed (if removed successfully), or null
     * if not present in the list of pending receipts (already removed)
     */
    private CapabilityReceipt removeReceiptFromPending(CapabilityReceipt aReceipt)
    {
        if (myCapabilityReceipts.removeElement(aReceipt)) return aReceipt;
        return null;
    }
        
    /**
     * Remove all pending receipts from/to given DObjectID from the pending
     * capability receipts list.  This is called by the list manager to remove
     * any pending receipts (clean up) when a DObject has left prematurely
     * (i.e. via partition, leaving, etc.).  Removes the receipt if either the 
     * sender or the receiver has left.
     *
     * @param id the DObjectID of the object (sender or receiver of capability
     * request) to be removed
     * @see ViewListManager#destroyDObject
     */
    protected final void removeReceiptsForID(DObjectID id) 
    {
        synchronized (myCapabilityReceipts) {
            for(Enumeration e=myCapabilityReceipts.elements(); e.hasMoreElements(); ) {
                CapabilityReceipt aReceipt = (CapabilityReceipt) e.nextElement();
                if (id.equals(aReceipt.getRequestorID()) ||
                    id.equals(aReceipt.getGrantorID())) {
                        removeReceiptFromPending(aReceipt);
                }
            }
        }
    }
    
    /**
     * Method to add the given DObject as a listener for the arrival/departure
     * of other DObjects.  This will be called by DObject.addDObjectListener
     *
     * @param object the DObject that wishes to be notified when another DObject
     * arrives/departs
     * @see DObject#addDObjectListener
     */
    protected void addDObjectListener(DObject object)
    {
        myListManager.addDObjectNotify(object);
    }
    
    /**
     * Method to remove the given DObject as a listener for the arrival/departure
     * of other DObjects.  This will be called by DObject.removeDObjectListener.
     *
     * @see DObject#addDObjectListener
     */
    protected void removeDObjectListener(DObject object)
    {
        myListManager.removeDObjectNotify(object.getID());
    }
    
    /**
     * Get a handle on actual DObject, given it's DObjectID.  Returns null
     * if not found.  Declared protected so that no untrusted code can get 
     * at this method.  Mostly used for testing.
     *
     * @param id the DObjectID that we're looking for
     * @return DObject that we're looking for.  If not found, returns null.
     */
    protected DObject getDObjectForID(DObjectID id)
    {
        return myListManager.getDObjectFromAnywhere(id);
    }
    
    // Utility methods
    
    /**
     * Print the given message, followed by a stack trace for the given Exception.
     *
     * @param e the Exception
     * @param message the String message to print
     */
    protected void dumpStack(Exception e, String message)
    {
        System.out.println(message);
        e.printStackTrace();
    }
    
    /**
     * Single method to print debugging output.  When moved to Trace system,
     * this is only place that has to change.
     *
     * @param message the String message to print
     */
    protected void debug(String message)
    {
        if (debug) System.out.println(message);
    }
    
    
    
}