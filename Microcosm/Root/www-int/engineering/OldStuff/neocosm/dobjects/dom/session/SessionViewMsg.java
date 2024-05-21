/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/SessionViewMsg.java $
    $Revision: 1 $
    $Date: 1/27/98 8:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

/**
 * Class to represent an 'enum'...for associating an int message type with a
 * string.  
 *
 * @see dom.net.SessionViewPacket
 *
 * @author Scott Lewis
 */
public final class SessionViewMsg
{
    // join request
    // this message is responded to by the group coordinator with the
    // view change message
    public final static int VIEW_JOIN                           = -1;
    // leave message
    // this message is responded to by the group coordinator with the
    // view change message
    public final static int VIEW_LEAVE                          = -4;
    // view change message sent by group coordinator
    public final static int VIEW_CHANGE                         = -7;

    // Send packet to remote presences of DObject
    public final static int SEND_DOBJECT_PACKET                 = -1001;

    // Create remote presences of DObjects  
    public final static int CREATE_DOBJECT                      = -1002;
}
