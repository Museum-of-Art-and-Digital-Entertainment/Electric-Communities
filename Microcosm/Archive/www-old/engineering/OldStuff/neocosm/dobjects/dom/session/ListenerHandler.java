/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ListenerHandler.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import java.net.Socket;

/**
 * @author Scott Lewis
 */
public interface ListenerHandler {

    /**
     * Receive connection on socket given.
     *
     * @param aSocket the socket we have just had a connection on
     */
    public void connectionReceived(Socket aSocket);

}