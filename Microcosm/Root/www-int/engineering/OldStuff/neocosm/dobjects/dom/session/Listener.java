/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/Listener.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import java.io.IOException;
import java.net.*;

/**
 * Thread to wait for an 'accept' on a socket.  
 *
 * @see ServerView
 * @see ListenerHandler
 *
 * @author Scott Lewis
 */
public class Listener extends Thread
{
    ServerSocket mySocket;
    int myListenPort = 0;
    ListenerHandler myListenerHandler;

    public Listener(ListenerHandler handler, int port)
    {
        super("Listener");
        myListenPort = port;
        myListenerHandler = handler;
    }

    public void start()
    {
        try {
            mySocket = new ServerSocket(myListenPort);
            super.start();
        } catch (Exception e) {
            System.out.println("Listener.start");
            e.printStackTrace();
        }
    }

    public void run()
    {
        while (mySocket != null) {
            try {
                Socket con = mySocket.accept();
                myListenerHandler.connectionReceived(con);
            } catch (Exception e) {
                System.out.println("Listener.run");
                e.printStackTrace();
            }
        }
    }
}
