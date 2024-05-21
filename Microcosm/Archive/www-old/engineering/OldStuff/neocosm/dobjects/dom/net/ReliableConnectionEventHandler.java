/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ReliableConnectionEventHandler.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.net;

import dom.util.Queue;
import java.io.*;

/**
 * @author Scott Lewis
 */
public interface ReliableConnectionEventHandler
{
    /**
     * Message from transport when a particular connection is suspect.
     *
     * @param connection the connection under suspicion
     * @param e the Exception leading to the suspicion (may be null)
     * @param unsent a Queue of unsent SessionViewPackets
     */
    public void handleSuspect(ReliableConnection connection, Exception e, Queue unsent);
    
    /**
     * Message from transport when a connection has been severed.
     *
     * @param connection the connection that has been severed
     * @param e the Exception leading to the disconnect
     * @param unsent a Queue of unsent SessionViewPackets
     */
    public void handleDisconnect(ReliableConnection connection, Exception e, Queue unsent);
    
    /**
     * Message from transport when data has been received.
     *
     * @param connection the connection responsible for the data
     * @param packet the SessionViewPacket that has been received
     * @exception IOException thrown if some problem reading from
     * the packet
     */
    public void handleNetInput(ReliableConnection connection, SessionViewPacket packet)
        throws IOException;
}

