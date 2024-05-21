/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/net/TCPConnection.java $
    $Revision: 1 $
    $Date: 12/27/97 4:09p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.net;

import java.net.*;
import java.io.*;

import dom.util.Queue;
import dom.serial.*;

/**
 * Class to represent a TCP connection between two SessionViews.  This provides
 * a wrapper for a Socket that implements the ReliableConnection interface.
 *
 * @see ReliableConnection
 * @author Scott Lewis
 */
public class TCPConnection implements ReliableConnection
{
    InetAddress myAddress;
    int myPort;

    Socket mySocket;

    Sender mySender;
    Thread mySenderThread;
    Receiver myReceiver;
    Thread myReceiverThread;

    ObjectOutput myOutputStream;
    ObjectInput myInputStream;

    ReliableConnectionEventHandler myHandler;
    Queue myOutgoingQueue;

    public TCPConnection(ReliableConnectionEventHandler handler, Socket aSocket) throws IOException
    {
        myHandler = handler;
        mySocket = aSocket;
        myAddress = mySocket.getInetAddress();
        setupStreams();
        myOutgoingQueue = new Queue();
        setupThreads();
    }

    public TCPConnection(ReliableConnectionEventHandler handler, InetAddress addr, int port)
    {
        myHandler = handler;
        myAddress = addr;
        myPort = port;
        myOutgoingQueue = new Queue();
    }

    public TCPConnection(ReliableConnectionEventHandler handler, String name, int port)
        throws UnknownHostException
    {
        myHandler = handler;
        myAddress = InetAddress.getByName(name);
        myPort = port;
        myOutgoingQueue = new Queue();
    }

    public void queuePacket(SessionViewPacket aPacket) throws IOException
    {
        if (myOutgoingQueue == null || !myOutgoingQueue.enqueue(aPacket))
            throw new IOException("Outgoing queue not available");
    }

    public void connect() throws IOException, UnknownHostException
    {
        System.out.println("Connecting to "+myAddress+" port "+myPort+"...");
        mySocket = new Socket(myAddress, myPort);
        setupStreams();
        setupThreads();
        start();
    }

    private void setupThreads()
    {
        myReceiver = new Receiver(this, myHandler, (ObjectInput) myInputStream, myOutgoingQueue);
        myReceiverThread = new Thread(myReceiver,"Receiver for "+mySocket);
        mySender = new Sender(this, myHandler, myOutgoingQueue, (ObjectOutput) myOutputStream);
        mySenderThread = new Thread(mySender,"Sender for "+mySocket);
    }

    private void setupStreams() throws IOException
    {
        myOutputStream = new ObjectOutputStream(new BufferedOutputStream(mySocket.getOutputStream()));
        myOutputStream.flush();
        myInputStream = new ObjectInputStream(new BufferedInputStream(mySocket.getInputStream()));
    }

    /**
     * Start up both sender and receiver threads.
     */
    public void start()
    {
        if (myReceiverThread != null) {
            myReceiverThread.start();
        }
        if (mySenderThread != null) {
            mySenderThread.start();
        }
    }

    public synchronized void setHandler(ReliableConnectionEventHandler handler)
    {
        myHandler = handler;
        mySender.setHandler(myHandler);
        myReceiver.setHandler(myHandler);
    }

    /**
     * Stop our running threads.
     */
    private void stopThreads()
    {
        if (mySender != null) {
            mySender.stopRunning();
            mySender = null;
        }
        if (myReceiver != null) {
            myReceiver.stopRunning();
            myReceiver = null;
        }
    }

    /**
     * Close our socket.  First stop running threads, then try to close the
     * socket.
     *
     * @exception IOException if socket cannot be closed.
     */
    public void close() throws IOException
    {
        stopThreads();
        if (mySocket != null) mySocket.close();
        mySocket = null;
    }

    /**
     * Return the InetAddress for this connection.
     * @return InetAddress of the connected system
     */
    public InetAddress getAddress()
    {
        return myAddress;
    }

    /**
     * Return socket for this connection
     * @returns the socket this monitor is servicing
     */
    public Socket getSocket()
    {
        return mySocket;
    }
}

/** 
 * This class defines the 'run' method for the thread that is charged with
 * sending SessionViewPackets passed to the TCPConnection object defined above
 *
 * @see TCPConnection
 */
class Sender implements Runnable
{
    //static final boolean debug = true;
    static final boolean debug = false;

    Queue myQueue = null;
    ObjectOutput myOutputStream = null;

    ReliableConnectionEventHandler myHandler = null;
    TCPConnection myConnection = null;
    boolean myRunning = true;

    Sender(TCPConnection connection,
        ReliableConnectionEventHandler handler,
        Queue queue,
        ObjectOutput outStream)
    {
        myConnection = connection;
        myHandler = handler;
        myQueue = queue;
        myOutputStream = outStream;
    }

    protected void setHandler(ReliableConnectionEventHandler handler)
    {
        myHandler = handler;
    }

    protected void stopRunning()
    {
        myRunning = false;
        myQueue.close();
    }

    /**
     * Real action here.  This just peels off SessionViewPackets from the
     * Queue (myQueue), and writes them out to the wire.  Also provides
     * partition handling by calling the appropriate methods on the connection
     * handler (will likely be the local View).
     */
    public void run()
    {
        SessionViewPacket p;
        while ((p = (SessionViewPacket) myQueue.peekQueue()) != null) {
            try {
                myOutputStream.writeObject(p);
                myOutputStream.flush();
                if (debug) System.out.println("Wrote packet "+p);
                myQueue.removeFirstElement();
            } catch (Exception e) {
                if (debug) {
                    System.out.println("Exception sending packet "+p);
                    e.printStackTrace();
                }
                // Else it was an exception, and we will have our handler
                // deal with it...this will most likely result in this
                // thread being stopped, because of things get this far
                // then we likely have a partition, and disconnect will
                // result
                myHandler.handleDisconnect(myConnection, e, myQueue);
                if (!myRunning) {
                    if (debug) System.out.println("Sender thread stopped by exception handler");
                    return;
                }
            }
        }
        if (debug) System.out.println("Sender thread done");
    }
}

/** 
 * This class defines the 'run' method for the thread that is charged with
 * receiving SessionViewPackets on the TCPConnection object defined above
 *
 * @see TCPConnection
 */
class Receiver implements Runnable
{
    //static final boolean debug = true;
    static final boolean debug = false;

    TCPConnection myConnection = null;
    ObjectInput myInputStream = null;
    ReliableConnectionEventHandler myHandler = null;
    boolean myRunning = true;
    Queue myOutGoing;

    Receiver(TCPConnection connection,
        ReliableConnectionEventHandler handler, ObjectInput inStream, Queue out)
    {
        myConnection = connection;
        myHandler = handler;
        myInputStream = inStream;
        myOutGoing = out;
    }

    protected void setHandler(ReliableConnectionEventHandler handler)
    {
        myHandler = handler;
    }

    public void stopRunning()
    {
        myRunning = false;
    }

    /**
     * Real action here.  This just reads SessionViewPackets from off the
     * wire forever and sends the processNetInput message to the connection
     * handler defined in the myHandler instance variable.
     */
    public void run()
    {
        while (myRunning) {
            try {
                SessionViewPacket p = (SessionViewPacket) myInputStream.readObject();
                if (debug) System.out.println("Received packet "+p);
                myHandler.handleNetInput(myConnection, p);
            } catch (Exception e) {
                if (debug) {
                    System.out.println("Exception receiving packet");
                    e.printStackTrace();
                }
                myHandler.handleDisconnect(myConnection, e, myOutGoing);
                if (!myRunning) {
                    if (debug) System.out.println("Receiver thread stopped by exception handler");
                    return;
                }
            }
        }
        if (debug) System.out.println("Receiver thread done");
    }
}


