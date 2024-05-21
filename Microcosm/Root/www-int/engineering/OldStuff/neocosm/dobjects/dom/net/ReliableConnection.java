/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/net/ReliableConnection.java $
    $Revision: 1 $
    $Date: 12/27/97 4:09p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author Scott Lewis
 */
public interface ReliableConnection {

    public void start();
    public void connect() throws IOException;
    public void close() throws IOException;
    public InetAddress getAddress();
    public Socket getSocket();

    public void queuePacket(SessionViewPacket packet) throws IOException;
    public void setHandler(ReliableConnectionEventHandler handler);
}

