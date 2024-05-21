/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/GroupConnectionHandler.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import java.net.*;
import java.io.*;

import dom.net.*;
import dom.util.*;
import dom.id.*;

/**
 * @author Scott Lewis
 */
class GroupConnectionHandler implements ReliableConnectionEventHandler
{
    private View myView;

    protected GroupConnectionHandler(View aView)
    {
        myView = aView;
    }

    /**
     * Message from transport when a particular connection is suspect.
     *
     * @param connection the connection under suspicion
     * @param e the Exception leading to the suspicion (may be null)
     * @param unsent a Queue of unsent SessionViewPackets
     */
    public void handleSuspect(ReliableConnection connection, Exception e, Queue unsent)
    {
        // XXX TODO
    }

    /**
     * Message from transport when a connection has been severed.
     *
     * @param connection the connection that has been severed
     * @param e the Exception leading to the disconnect
     * @param unsent a Queue of unsent SessionViewPackets
     */
    public void handleDisconnect(ReliableConnection connection, Exception e, Queue unsent)
    {
        myView.handleDisconnect(connection, e, unsent);
    }

    /**
     * Message from transport when data has been received.
     *
     * @param connection the connection responsible for the data
     * @param packet the SessionViewPacket that has been received
     * @exception IOException thrown if some problem reading from
     * the packet
     */
    public void handleNetInput(ReliableConnection connection, SessionViewPacket packet)
        throws IOException
    {
        // Here's the message router at the session view level
        // First, get message type from packet
        SessionViewID fromID = packet.getFromSessionViewID();
        if (fromID == null) throw new IOException();
        
        switch (packet.getMsg()) {
            case SessionViewMsg.VIEW_JOIN:
                handleJoinMsg(fromID, connection, packet);
                break;
            case SessionViewMsg.VIEW_LEAVE:
                handleLeaveMsg(fromID, connection, packet);
                break;
            case SessionViewMsg.VIEW_CHANGE:
                handleChangeMsg(fromID, packet);
                break;
            case SessionViewMsg.CREATE_DOBJECT:
                handleCreateMsg(fromID, packet);
                break;
            case SessionViewMsg.SEND_DOBJECT_PACKET:
                handleDObjectPacketMsg(fromID, packet);
                break;
            default:
                throw new IOException("Invalid packet: "+packet+" received from id "+fromID);
        }
    }
    
    void handleJoinMsg(SessionViewID fromID, ReliableConnection connection, SessionViewPacket packet)
        throws IOException
    {
         // Assert that packet is addressed to us, and that we are
         // the current group manager.  Drop packet if either is not true.
         if (!myView.isGroupManager()) {
            // Drop packet, as we are not supposed to be receiving this message
            // XXX TODO LOG THIS FACT
            debug("handleJoinMsg: join packet received by non-group manager or received packet not addressed to us");
            return;
         }
         
         debug("handleJoinMsg: Request for admission from view "+fromID+" received by group manager");
         
         // Here, we actually get SessionViewData and fromID from the packet, and
         // make protected call on view to check whether id (and data) provided
         // is ok for entry.  This is the opportunity for the group manager to
         // introduce *any* security checks that it requires for group admission
         GroupAdmissionCheck result = myView.checkForAdmission(fromID,packet.getMsgData());
         // Get and check data from packet.  Determine whether we
         // should accept request.                                                    
         if (result.isAdmitted()) {
            // Admission granted, so send out change info to requester and all others
            debug("handleJoinMsg:  Request to join ACCEPTED for "+fromID);
            myView.sendJoinAccept(connection, result);
         } else {
            debug("handleJoinMsg:  Request to join REJECTED for "+fromID);
            // Admission rejected, so send out change info to requester only
            myView.sendJoinFailure(connection, result);
         }
    }

    void handleLeaveMsg(SessionViewID fromID, ReliableConnection connection, SessionViewPacket packet)
        throws IOException
    {
        if (!myView.isGroupManager() || !packet.getToSessionViewID().equals(myView.getID())) {
           // Drop packet, as we are not supposed to be receiving this message
           // XXX TODO LOG THIS FACT
           debug("handleLeaveMsg: Join packet received by non-group manager or received packet not addressed to us");
           return;
        }
        debug("handleLeaveMsg: Request for departure from view "+fromID+" received by group manager");
        // Here we send accept message back to requestor and remove view from group.
        // We'll just reuse the GroupAdmissionCheck class to hold the id, etc., as
        // we can't refuse another view's offer to leave the group...
        myView.sendLeaveAccept(connection, new GroupAdmissionCheck(fromID, true, null));
    }
    
    void handleChangeMsg(SessionViewID fromID, SessionViewPacket packet)
        throws IOException
    {
        // Assert that we are *not* group manager (should never *receive* these if
        // we are not)
        if (myView.isGroupManager()) throw new IOException("Group manager cannot receive view change message");
        
        ViewChange viewChangeInfo = null;
        try {
            viewChangeInfo = (ViewChange) packet.getMsgData().getData();
        } catch (ClassCastException e) {
            // If any exception...i.e. cast exception, etc, simply throw IOException
            throw new IOException("Invalid data...cast to ViewChange failed");
        }
        debug("handleChangeMsg: View change message received");
        
        SessionViewID newID = viewChangeInfo.getChangeID();
        if (newID == null) {
            // It's us!
            if (viewChangeInfo.isJoining()) {
                // This says that we were asking to join...
                if (viewChangeInfo.isSuccessful()) {
                    // Success!
                    debug("handleChangeMsg: Addition approved by group manager.  Calling View.addGroupSuccessful");
                    myView.addGroupSuccessful(fromID, viewChangeInfo);
                } else {
                    // They refused...what to do?
                    debug("handleChangeMsg: Addition not approved by group manager;.  Calling View.addGroupFailure");
                    myView.addGroupFailure(fromID, viewChangeInfo);
                }
            } else {
                // This says we were asking to leave
                // Just make sure this is right, and call view to do the rest
                if (viewChangeInfo.isSuccessful()) {
                    myView.leaveGroupSuccessful(fromID, viewChangeInfo);
                }
            }
        } else {
            // Somebody else has arrived/departed!
            if (viewChangeInfo.isJoining()) {
                // They are joining...just make sure it was successful and add them
                if (viewChangeInfo.isSuccessful()) {
                    // In this case, we simply add the new group member to our
                    // known set.  This will result in notification to all
                    // our DObjects of the existance of this new group member
                    myView.addGroupMember(null, newID);
                }
            } else {
                // Otherwise it was a remove, and we must remove this view id from
                // our known set (and any dependent DObjects)
                myView.removeGroupMember(null, newID);
            }
        }
    }
    
    void handleCreateMsg(SessionViewID fromID, SessionViewPacket packet)
        throws IOException
    {
        CreateDObjectData data=null;
        try {
            SessionViewData viewData = packet.getMsgData();
            if (viewData == null) throw new NullPointerException();
            data = (CreateDObjectData) viewData.get(0);
            if (data == null) throw new NullPointerException();
        } catch (Exception e) {
            throw new IOException("GroupConnectionHandler.handleCreateMsg.  Invalid Create Msg Data");
        }
        // Pass on to view for the real handling
        myView.handleCreateMsg(fromID, packet.getToSessionViewID(), data);
    }
    
    void handleDObjectPacketMsg(SessionViewID fromID, SessionViewPacket packet)
        throws IOException
    {
        // If sender does not address, then they are outta here
        DObjectPacket objectPacket = packet.getDObjectPacket();
        if (objectPacket == null) throw new IOException("Invalid data in SEND_DOBJECT_DATA message");
        // Hand to view for real processing
        myView.handleDObjectPacketMsg(fromID, packet.getToSessionViewID(), objectPacket);            
    }
    
    
    void debug(String message)
    {
        myView.debug(message);
    }

}