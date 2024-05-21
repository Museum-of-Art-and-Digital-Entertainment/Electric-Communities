/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ClientView.java $
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
 * Client View in 'star' topology.  Implements the View functionality for 'non
 * group manager' Views in a client-server (star) network topology.
 *
 * @see View
 * @see ServerView
 *
 * @author Scott Lewis
 */
public class ClientView extends View
{

    ReliableConnection myClientConnection;
    
    public ClientView(SessionViewID myID)
    {
        super(myID);
    }

    /**
     * Called by DObjects to determine whether this view is a server view
     *
     * @return true if is a server view, false otherwise.  Abstract because
     * it has to be determined by subclasses
     */
    protected boolean isServer()
    {
        return false;
    }

    /**
     * Base method for sending a session packet's worth of data to a remote
     * session view.
     *
     * @param packet the SessionViewPacket that we wish to send
     * @exception IOException thrown if some problem sending packet
     */
    protected void queueSessionViewPacket(SessionViewPacket packet)
        throws IOException
    {
        synchronized (this) {
            if (myClientConnection == null) throw new IOException("No connection to group");
            myClientConnection.queuePacket(packet);
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
     * @param target the SessionViewID that identifies the target 
     * group manager for our connection
     * @exception IOException thrown if some problem making connection to
     * remote group
     */
    protected void reallyJoinGroup(SessionViewID groupManager)
        throws IOException
    {
        URL targetURL = groupManager.getURL();
        try {
            // Create new pt2pt connection to remote server
            myClientConnection = new TCPConnection(myConnectionManager, targetURL.getHost(), targetURL.getPort());
            // Make connection...will block until success or IOException is thrown
            myClientConnection.connect();
            // Send join group message
            sendJoinMsg(groupManager, getJoinMsgData(groupManager));
        } catch (IOException e) {
            myClientConnection.close();
            myClientConnection = null;
            throw e;
        }
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
        // Client (in client-server network topology) should never receive this
        // message.  We throw.
        throw new IOException("Not group manager");
    }
    
    /**
     * End our view's status of being in a 'connecting' state.  This method
     * is synchronized on the view object, so that a) the transition to
     * a new state for this view (either connected or not) is atomic to
     * this view; b) notifyAll() can be called so any waiting threads
     * (joinGroup or leaveGroup methods on view) can be allowed to 
     * continue. 
     *
     * If the groupManager is null, this means that we have left the group,
     * and in that case we attempt to permanently close the connection to
     * the group.  If the groupManager is non-null, we have just entered a
     * group successfully, and things may continue.
     
     * NOTE:  Any subclass override of this method should also be synchronized,
     * and should call super.endConnecting when it has completed its own work.
     *
     * @param groupManager the SessionViewID of our group manager.  If null,
     * this means that we have successfully left a group.  If non-null, we
     * have successfully been added to a group
     * @param rank our rank in the group (provided by manager)
     */
    protected void endConnecting(SessionViewID groupManager, int rank)
    {
        // We just have left group.  Close connection for good
        if (groupManager == null && myClientConnection != null) {
            // Close connection
            debug("Trying to close connection "+myClientConnection);
            try {
                myClientConnection.close();
            } catch (IOException e) {
                dumpStack(e, "ClientView.endConnecting()");
            }
            debug("Connection "+myClientConnection+" closed");
        }
        
        synchronized (this) {
            // Our group manager has gone away...kill all client presences!
            if (myGroupManager != null && groupManager == null) {
                myListManager.destroyClientPresences();
            }
            // Set state variables for this view
            myGroupManager = groupManager;
            myRank = rank;
            myWaitingToConnect = false;
            // Notify any threads waiting on us
            notifyAll();
        }
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
        // Deal with our state
        synchronized (myListManager) {
            // Close up connection for good
            if (connection == myClientConnection) {
                super.closeConnection(connection);
                myClientConnection = null;
            }
            // Change our state to unconnected
            endConnecting(null, VIEW_NOT_GROUP_MEMBER);
        }
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
        // Ignore!  For client, this is a nop!  Shocking!
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
        // Ignore!  For client, this is also a nop!
    }
    
    
    
}

