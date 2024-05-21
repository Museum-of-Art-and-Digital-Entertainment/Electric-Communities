/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/net/ReliableMultipointProtocol.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.net;

import dom.net.DObjectPacket;
import dom.session.DObject;

import dom.session.SessionViewID;

import java.io.IOException;

/**
 * This is the interface (provided by the SessionView) that allows
 * a DObject to communicate with its remote presences reliably.  It
 * allows arbitrary data to be sent to those presence (DObjectPacket)
 *
 * @see dom.session.DObject
 * @see dom.session.View
 * @author Scott Lewis
 */
public interface ReliableMultipointProtocol
{
    /**
     * Send data contained in DObjectPacket to any/all other remote views
     * known to exist to this view.
     *
     * @param sender the DObject that wishes to send the data
     * @param data the DObjectPacket containing any data to send
     * @exception IOException thrown if data cannot be sent
     * @exception SecurityException thrown if sender not allowed to send
     * data
     */
    public void sendDataToPresences(DObject sender, DObjectPacket data)
        throws IOException, SecurityException;
    /**
     * Send data contained in DObjectPacket to the view specified in the
     * 'to' parameter.
     *
     * @param sender the DObject that wishes to send the data
     * @param to the SessionViewID that the data is going to
     * @param data the DObjectPacket containing any data to send
     * @exception IOException thrown if data cannot be sent
     * @exception SecurityException thrown if sender not allowed to send
     * data
     */
    public void sendDataToView(DObject sender, SessionViewID to, DObjectPacket data)
        throws IOException, SecurityException;
}