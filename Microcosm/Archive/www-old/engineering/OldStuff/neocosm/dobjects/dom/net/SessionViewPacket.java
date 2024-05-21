/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/net/SessionViewPacket.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/

package dom.net;

import dom.serial.*;
import java.io.*;

import dom.session.SessionViewID;

/**
 * A packet of arbitrary information for messages between session
 * views.  Provides a common abstraction for session view-session view level 
 * of communication.  Provides a common data format for all within-group
 * (session-based) communication.
 * <p>
 * SessionView-to-SessionView Protocol is the following.  More detail to
 * follow soon:
 * <p>
 * Header (one byte)<p>
 * Sequence ID (long)<p>
 * Sender ID (SessionViewID instance)<p>
 * Receiver ID (SessionViewID instance...null if intended for *all* known views)
 * Msg Type (int) (See the SessionViewMsg class for types)
 * DataLength (int)
 * Data (of length DataLength in bytes)
 * <p>
 * Note that the fact that the DataLength variable is an int limits the length of the
 * Data to the size of an int (2147483647 bytes).  That's pretty big
 * though for one message.
 *
 * @see dom.session.View#queueSessionViewPacket
 * @see dom.session.ClientView#queueSessionViewPacket
 * @see dom.session.ServerView#queueSessionViewPacket
 * @see dom.session.GroupConnectionHandler
 * @see TCPConnection
 *
 * @author Scott Lewis
 */
public class SessionViewPacket implements Serializable {

    // We'll make the header byte = 1
    private static byte myHeader;
    private static long mySendCount;

    /**
     * Initialize sequence counter.
     */
    static {
        myHeader = 1;
        mySendCount = Long.MIN_VALUE;
    }

    /**
     * Method to get next number for packet sequence counter
     */
    protected static final long getNextSequence()
    {
        return mySendCount++;
    }

    // Instance variables
    SessionViewID myFromID;
    SessionViewID myToID;
    long mySequenceNumber;
    int myMsg;
    SessionViewData myData;

    /**
     * Create new instance of a SessionViewPacket.  Instances of this object are
     * created in the View class and the ServerView class.  No other code currently
     * creates instances of this class.
     *
     * @param fromID the SessionViewID that is sending this packet
     * @param toID the SessionViewID that is to receive this packet
     * @param msg the int that identifies the *type* of this packet (see the SessionViewMsg)
     * for the types of packets
     * @param data the SessionViewData (in the form of a dictionary) that represents the
     * actual data to send in this packet
     * 
     * @see dom.session.SessionViewMsg
     * @see dom.serial.SessionViewData
     * @see dom.session.View
     * @see dom.session.ServerView
     */
    public SessionViewPacket(SessionViewID fromID, SessionViewID toID, int msg, SessionViewData data)
    {
        myFromID = fromID;
        myToID = toID;
        mySequenceNumber = getNextSequence();
        myMsg = msg;
        myData = data;
    }

    /**
     * Get the SessionViewID that sent this packet
     *
     * @return SessionViewID of the view that sent this packet
     */
    public SessionViewID getFromSessionViewID()
    {
        return myFromID;
    }

    /**
     * Get the SessionViewID of the view this packet is intended for (if null, is intended
     * for all receivers)
     *
     * @return SessionViewID of the view that is to receive packet.  If null, this packet
     * is intended for all views currently in group
     */
    public SessionViewID getToSessionViewID()
    {
        return myToID;
    }

    /**
     * Get the sequence number associated with this packet.
     *
     * @return long the sequence number associated with this packet.  Only guaranteed to
     * be unique relative to a given sender.
     */
    public long getSeqNumber()
    {
        return mySequenceNumber;
    }

    /**
     * Get the message type information associated with this packet.
     *
     * @return int message type information for this packet
     */
    public int getMsg()
    {
        return myMsg;
    }

    /**
     * Get the SessionViewData associated with this packet.
     *
     * @return SessionViewData the dictionary that represents the data for this
     * packet
     */
    public SessionViewData getMsgData()
    {
        return myData;
    }

    /**
     * Get an instance of a DObjectPacket for this message.  This method is not
     * really necessary, and is sort of a hack.  All it does is assume that this
     * packet is of type SessionViewMsg.SEND_DOBJECT_PACKET and tries to get
     * an instance of DObjectPacket from the element "0" of the SessionViewData
     * dictionary.  Might want to remove this sometime, as it's sort of hacky.
     *
     * @return DObjectPacket that this packet contains.  Throws IOException if
     * the dictionary data associated with this packet does not contain an instance
     * of a DObjectPacket as element indexed by the string "0".
     * @exception IOException thrown if an instance of DObjectPacket is not in position
     * keyed by the string "0".
     */
    public DObjectPacket getDObjectPacket() throws IOException
    {
        DObjectPacket oPacket;
        try {
            oPacket = (DObjectPacket) myData.get(0);
        } catch (Exception e) {
            throw new IOException("No DObjectPacket in data");
        }
        return oPacket;
    }
    
    /**
     * Return String representation of this packet.  Mostly used for debugging.
     *
     * @return String to represent this packet
     */
    public String toString()
    {
        return "fromID:"+myFromID+";toID:"+myToID+";seq:"+mySequenceNumber+";msg:"+myMsg+";data:"+myData;
    }
    
}