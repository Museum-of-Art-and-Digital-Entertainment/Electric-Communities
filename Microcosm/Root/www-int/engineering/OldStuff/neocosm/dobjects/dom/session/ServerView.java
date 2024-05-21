/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ServerView.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;
import dom.net.*;

import java.io.*;
import java.util.*;

import java.net.*;

/**
 * A 'hub' View subclass for implementing a 'star' topology multipoint group. 
 * This class provides the basic functionality for a multipoint group manager
 * in a 'star' network topology (i.e. a client-server structure).  By the
 * definition of a 'star' topology this class is the group manager for a multipoint
 * group, and is responsible for determining the validity of group membership
 * requests and sending group membership change notifications to all other
 * Views that are members of the group.
 *
 * @see ClientView
 *
 * @author Scott Lewis
 */
public class ServerView extends View implements ListenerHandler
{
    protected Vector myPreadmittedConnections;
    protected Hashtable myCurrentViews;
    
    public ServerView(SessionViewID myID)
    {
        super(myID);
        myPreadmittedConnections =new Vector();
        myCurrentViews = new Hashtable();
        // Since we define the group, we are the group manager
        myGroupManager = getID();
        myRank = VIEW_IS_GROUP_MANAGER;
    }

    /**
     * Called by DObjects to determine whether this view is a server view
     *
     * @return true if is a server view, false otherwise.  Abstract because
     * it has to be determined by subclasses
     */
    protected boolean isServer()
    {
        return true;
    }

    /**
     * Base method for sending a session packet's worth of data to a remote
     * session view.  For the server, this results in sending the data to
     * all known group members.
     *
     * @param packet the SessionViewPacket that we wish to send
     * @exception IOException thrown if some problem sending packet
     */
    protected void queueSessionViewPacket(SessionViewPacket packet)
        throws IOException
    {
        SessionViewID toID = packet.getToSessionViewID();
        if (toID==null) {
            // Send to all
            queueToAllConnections(packet);
        } else {
            if (!toID.equals(getID())) {
                // Synchronize on list manager so we are sure that the group does
                // not change while this is going on
                synchronized (myListManager) {
                    ReliableConnection connection = getConnectionForID(toID);
                    if (connection == null) throw new IOException("Connection not found for view id: "+toID);
                    else connection.queuePacket(packet);
                }
            }
        }
    }
    
    protected ReliableConnection getConnectionForID(SessionViewID id)
    {
        return (ReliableConnection) myCurrentViews.get(id);
    }
    
    /**
     * Queue a given session view packet to all known connections.
     *
     * @param packet the SessionViewPacket to queue
     */
    protected void queueToAllConnections(SessionViewPacket packet)
    {
        // Synchronize on list manager so we are sure that group doesn't
        // change while we are sending the messages
        synchronized (myListManager) {
            for(Enumeration e=myCurrentViews.elements(); e.hasMoreElements();) {
                ReliableConnection connection = (ReliableConnection) e.nextElement();
                try {
                    connection.queuePacket(packet);
                } catch (IOException except) {
                    // This means that connection has closed packet processing
                    // and has called handleException on its handler...which
                    // must result in the connection being shut down, so no use
                    // messaging to this connection anyway...so just ignore,
                    // as this connection is going away anyway.
                }
            }
        }
    }

    /**
     * Since server cannot join another group, the implementation of this
     * method just throws.
     *
     * @param target the SessionViewID that identifies the target 
     * group manager for our connection
     * @exception IOException thrown if some problem making connection to
     * remote group
     */
    protected void reallyJoinGroup(SessionViewID groupManager) throws IOException
    {
        throw new IOException("Server:  Cannot join another group");
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
    protected GroupAdmissionCheck checkForAdmission(SessionViewID id,
                                                    SessionViewData data)
       throws IOException
    {
        if (id == null) return new GroupAdmissionCheck(null, false, null);
        // XXX TODO
        // for now, just always give back a success response
        return new GroupAdmissionCheck(id, true, null);
    }
    
    /**
     * Receive connection on socket given.  This is just for testing, as in reality
     * some other object than the ServerView will be handling this message.
     *
     * @param aSocket the socket we have just had a connection on
     */
    public void connectionReceived(Socket aSocket)
    {
        // Received connection from given socket.
        // First create a wrapper
        ReliableConnection connection = null;
        //debug("Connection received from "+aSocket.getInetAddress());
        try {
            connection = new TCPConnection(myConnectionManager, aSocket);
        } catch (IOException e) {
            dumpStack(e, "connectionReceived");
        }
        // Put on list of not-yet-admitted connections
        myPreadmittedConnections.addElement(connection);
        // Start processing so we can receive join msg
        connection.start();
    }
    
    protected void addGroupMember(ReliableConnection connection, SessionViewID viewID)
    {
        // Remove from list of not-yet-admitted connections
        myPreadmittedConnections.removeElement(connection);
        // Put on list of currently 'active' connections
        myCurrentViews.put(viewID, connection);
        // Now notify everyone, etc., through list manager
        super.addGroupMember(connection, viewID);
    }
    
    protected void removeGroupMember(ReliableConnection connection, SessionViewID viewID)
    {
        debug("ServerView.removeGroupMember with connection "+connection+" and id "+viewID);
        // Remove from list of currently 'active' connections
        myCurrentViews.remove(viewID);
        // Now notify everyone, etc., through list manager
        super.removeGroupMember(connection, viewID);
    }
    
    /**
     * Forcibly close the connection given.  This is called to deal with an 
     * exception reported by the connection's thread.  For this client
     * class, the superclass closeConnection is called, then the
     * listmanager is updated, then the group manager is set to null.
     *
     * @param connection the ReliableConnection in question
     */
    protected void closeConnection(ReliableConnection connection)
    {
        synchronized (myListManager) {
            // First, close connection
            try {
                connection.close();
            } catch (IOException e) {
               dumpStack(e, "closeConnection:  Trouble closing connection "+connection);
            }
            // If it's on one of our preadmitted or closing connections, 
            // just remove from these lists
            myPreadmittedConnections.removeElement(connection);
            SessionViewID viewID = getConnectionFromCurrentViews(connection);
            // Destroy any/all client presences for given view, and remove
            // group member from known set
            if (viewID != null) {
                // Remove group member from known set, then send message
                // about group change to others
                removeGroupMember(connection, viewID);
                // Send view change message to all known group members
                // Then forward view change to all other views
                forwardFromRemoteViewExcluding(getID(), viewID, SessionViewMsg.VIEW_CHANGE, 
                                new SessionViewData(getLeaveOthersViewChange(viewID)));
            }
        }
    }
    
    protected SessionViewID getConnectionFromCurrentViews(ReliableConnection connection)
    {
        for(Enumeration e=myCurrentViews.keys(); e.hasMoreElements(); ) {
            SessionViewID viewID = (SessionViewID) e.nextElement();
            ReliableConnection conn = (ReliableConnection) myCurrentViews.get(viewID);
            if (conn == connection) return viewID;
        }
        return null;
    }
 
    /**
     * This should not be called on a server, as it defines its own group
     * and never is in a connecting or unconnected state WRT the group.
     *
     * @param groupManager the SessionViewID that is the group manager
     * @param rank the integer rank for this view within the group
     */
    protected void endConnecting(SessionViewID groupManager, int rank)
    {
        dumpStack(null, "ServerView.endConnecting called");
    }
    
    /**
     * Forwards msg to all group members, using the 'fromID' parameter to 
     * specify where this message is from.  This is implemented by the server
     * only for fanout.  This does all fanout of all messages!  Client implements
     * as a nop.
     
     * NOTE:  *Assumes that group membership does not change during
     * this operation*...i.e. synchronization on list manager should be guaranteed
     * before this is called.
     *
     * @param fromID the SessionViewID to put in 'fromID' field of the packet
     * @param excluded a SessionViewID to exclude.  If null, everyone is sent the message
     * @param msg the SessionViewMsg to send
     * @param data the SessionViewData to send with the message
     */
    protected void forwardFromRemoteViewExcluding(SessionViewID fromID, 
                                                  SessionViewID excluded,
                                                  int msg, 
                                                  SessionViewData data)
    {
        if (excluded == null) {
            try {
                queueSessionViewPacket(new SessionViewPacket(fromID, null, msg, data));
            } catch (IOException e) {
                dumpStack(e, "ServerView.forwardFromRemoteViewExcluding");
            }
        }
        else {
            synchronized (myListManager) {
                SessionViewID ids[] = getCurrentGroupMembers();
                for(int i=0; i < ids.length; i++) {
                    if (!excluded.equals(ids[i]) && !fromID.equals(ids[i])) {
                        try {
                            queueSessionViewPacket(new SessionViewPacket(fromID, ids[i], msg, data));
                        } catch (IOException e) {
                            // Should not happen...but if it does we want to know about it
                            dumpStack(e, "ServerView.forwardFromRemoteViewExcluding");
                        }
                    }
                }
            }
        }
    }
    
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
    protected void forwardFromRemoteViewToRemoteView(SessionViewID fromID, 
                                                              SessionViewID toID,
                                                              int msg, 
                                                              SessionViewData data)
    {
        try {
            queueSessionViewPacket(new SessionViewPacket(fromID, toID, msg, data));
        } catch (IOException e) {
            // Ignore...this should mean that msg delivery is racing with
            // group membership change that excluded the toID view from the group
            // XXX Might log this
        }
    }
    
    
}

