package ec.e.net;

import ec.e.cap.ERestrictedException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

/**
  Manage the startup and shutdown of connections.

  To start listening for connections:
    Make an RtComMonitor (you only need one per process).
    Make an ENetConnectionFactory.
    Call listen on the RtComMonitor, handing in the port to listen on and the
      factory. You immediately get back an ENetListener, which you can use to
      find out what port you're actually listening on if you asked for
      a dynamically assigned port (port==0).

    When a connection comes in to your port, your factory's manufacture()
     method is called with all the info you need to set up the connection.
     NOTE:  this happens in the accept() thread, so you should fork
     off a new thread immediately in your factory, so you can service
     new connection requests.

   When you're ready to stop listening, call shutdown() on the ENetListener.
     Established connections will NOT be killed, you have to hunt them
     down yourself.

 To connect to someone else:
   Make an RtComMonitor.
   Make an ENetConnectionFactory.
   Call connect() on the RtComMonitor handing in the address to connect to and
     the factory.  connect() will block until the connection is established or
     the attempt fails.  You probably want to do this from its own thread.
   When the connection succeeds, your factory's manufacture() method
     is called.  Whatever manufacture() returns is returned from the
     connect() call as well.
*/
class RtComMonitor {
    static Trace tr = new Trace(false, "[RtComMonitor]");

    private InetAddress myLocalInetAddress;

    RtComMonitor() {
        try {
            myLocalInetAddress = InetAddress.getByName(null);
        } catch (UnknownHostException e) {
            RtUtil.fail("can't resolve localhost", e);
        }
    }

    /**
     * Start a thread to listen for incoming connections from the network.
     *
     * @param requestedPort The local port number to listen on. A value of 0
     *  indicates that you want the OS to assign a port for you.
     * @param factory A connection factory to set up the connection with the
     * @param factory A connection factory to set up the connection with the
     *  appropriate protocol handler for this connection.
     *
     * @return An ENetListener that holds onto the new listener thread and its
     *  associated information.
     *
     * This does not block; rather, it causes a new thread to be started.
     */
    synchronized ENetListener listen(int requestedPort,
            ENetConnectionFactory factory) throws IOException {
        ServerSocket socket = new ServerSocket(requestedPort);
        int port = socket.getLocalPort();
        InetAddress addr = myLocalInetAddress;

        RtComListener listener = new RtComListener(this, socket, addr, port,
                                                   factory);
        listener.start();
        return(new ENetListener(listener, new ENetAddr(addr, port)));
    }

    /**
     * Initiate a connection to another address on the network.
     *
     * @param remoteAddress The address and port to connect to
     * @param factory A connection factory to set up the connection with the
     *  appropriate protocol handler for this connection.
     *
     * @return Whatever the connection factory thought was an appropriate
     *  object to represent the result of setting up this connection.
     *  Presumably whoever called us knows what this should be...
     *
     * This can end up getting called from a number of different threads, but
     * none of them had better be the E run loop thread!
     */
    synchronized Object connect(ENetAddr remoteAddress,
            ENetConnectionFactory factory) throws IOException
    {
        /* @BLOCK METHOD (PRIM) */
        Socket clientSocket;
        Object connection;
        ENetAddr localAddress;

        /* Open a socket to the other machine. */
        if (tr.tracing)
            tr.$("Trying to connect to " + remoteAddress);
        /* Potential problem: this request could take a long time to timeout */
        /* @BLOCK PRIM */
        clientSocket = new Socket(remoteAddress.getInetAddress(),
                                  remoteAddress.getPortNumber());
        localAddress = new ENetAddr(myLocalInetAddress,
                                    clientSocket.getLocalPort());

        try {
            /* @BLOCK */
            return(setUpConnection(clientSocket, localAddress, remoteAddress,
                                   factory, false));
        } catch (ERestrictedException e) {
            throw new InternalError();
        }
    }

    /**
     * Produce a new comm connection between ourselves and somebody else, given
     * an already open socket to them.
     *
     * @param socket The socket that communicates with the other party
     * @param localAddr Our local address and port
     * @param remoteAddr The other guy's address and port
     * @param factory A connection factory to set up the connection with the
     *  appropriate protocol handler for this connection
     * @param incoming Direction of connection establishment: true->incoming
     *  connection from somebody else, false->outgoing connection that we are
     *  setting up ourselves
     *
     * @return Whatever the connection factory thought was an appropriate
     *  object to represent the result of setting up this connection.
     *
     * This method is strictly internal. Folks who think they might want to
     * call this actually want to be calling RtComMonitor.connect() or
     * RtComMonitor.listen().
     */
    Object setUpConnection(Socket socket, ENetAddr localAddr,
            ENetAddr remoteAddr, ENetConnectionFactory factory,
            boolean incoming) throws ERestrictedException, IOException
    {
        /* @BLOCK METHOD */
        Object connection = null;

        try {
            /* @BLOCK */
            connection = factory.manufacture(socket, localAddr, remoteAddr,
                                             incoming);
        } catch (ERestrictedException e) {
            if (tr.tracing)
                tr.$("connection from " + localAddr + " to " + remoteAddr +
                     " rejected.");
            throw e;
        } catch (IOException e) {
            if (tr.tracing)
                tr.$("connection from " + localAddr + " to " + remoteAddr +
                     " got IOException.");
            throw e;
        }

        if (connection != null && tr.tracing)
            tr.$("connection between " + localAddr + " and " + remoteAddr +
                 " established.");
        return(connection);
    }
}

class RtComListener extends Thread {
    static Trace tr = new Trace(false, "[RtComListener]");

    private boolean myTerminateFlag;
    private ENetAddr myLocalAddress;
    private ServerSocket myListenServerSocket;
    private RtComListenerUserThread myUserThread;
    private ENetConnectionFactory myFactory;
    private int myLocalPort;
    private RtComMonitor myMonitor;

    /**
     * Construct a new object to run in its own thread and listen on a
     * ServerSocket for new incoming connections.
     *
     * @param monitor The comm monitor to manage this comm environment
     * @param listenServerSocket The ServerSocket to listen for connections on
     * @param localAddress The local IP address we will listen on
     * @param localPort The local port we will listen on. A value of 0 says to
     *  have the OS assign a port for you.
     * @param factory A connection factory to set up a new connection when
     *  somebody actually connects
     */
    RtComListener(RtComMonitor monitor, ServerSocket listenServerSocket,
                  InetAddress localAddress, int localPort,
                  ENetConnectionFactory factory) {
        super("RtComListener(" + localPort + ")");
        setDaemon(true);
        myTerminateFlag = false;
        myUserThread = new RtComListenerUserThread(localPort);
        myLocalAddress = new ENetAddr(localAddress, localPort);
        myLocalPort = myLocalAddress.getPortNumber();
        myListenServerSocket = listenServerSocket;
        myFactory = factory;
        myMonitor = monitor;
        if (tr.tracing)
            tr.$("listening on " + myLocalAddress);
    }

    /**
     * Start up the RtComListener thread.
     *
     * Public only because Thread implements.  Nobody outside of Java runtime
     * should call this method.
     */
    public void start() {
        /*
          HACK: The RtComListener thread needs to be a daemon thread, because
          if it were a user thread it would be uninteruptible while it was off
          waiting on an accept() (this is due to a flaw in Solaris,
          actually). However, if it's a daemon thread the app can exit even if
          the thread is still running. However, if we're just sitting there
          waiting for connections to arrive over the network, we don't want to
          exit, we want to keep running. Thus we have the
          RtComListenerUserThread, which does NOTHING but wait. Since it's a
          user thread it keeps the app from exiting (and thus allows the
          RtComListener thread to keep running waiting for a connection) and
          since it's not waiting on an accept we can kill it. When we tell the
          RtComListener thread to shutdown (which we now can do since it's a
          daemon thread), it sends a notify() to the RtComListenerUserThread
          whereupon *it* shuts down too. Hallelujah, amen.
        */
        myUserThread.start();
        super.start();
    }

    /**
     * The actual body of the RtComListener thread. <p>
     *
     * Accepts connections on its ServerSocket, handing them off to its
     * RtComMonitor monitor to set up the actual connection objects and set
     * them running in threads of their own. <p>
     *
     * Loops until told to stop by somebody calling 'shutdown()' (which sets
     * an internal flag).
     */ 
    public void run() { /* @BLOCK METHOD (PRIM) -- TOP OF THREAD */
        if (tr.tracing)
            tr.$("started thread listening on port " + myLocalPort);

        while (!myTerminateFlag) {
            if (tr.tracing)
                tr.$("waiting for connection request on port " + myLocalPort);
            try {
                /* @BLOCK PRIM */
                Socket clientSocket = myListenServerSocket.accept();
                if (myTerminateFlag) {
                    if (tr.tracing)
                        tr.$("Accepted, but terminating port " + myLocalPort);
                    clientSocket.close();
                    break;
                }
                ENetAddr remoteAddress =
                    new ENetAddr(clientSocket.getInetAddress(),
                                 clientSocket.getPort());
                if (tr.tracing)
                    tr.$("Connection accepted from " + remoteAddress + " to " +
                         myLocalAddress);

                /* @BLOCK */
                myMonitor.setUpConnection(clientSocket, myLocalAddress,
                                          remoteAddress, myFactory, true);
            } catch (ERestrictedException e) {
                /* XXX bad exception handling -- fix */
                if (tr.tracing)
                    tr.$("ERestrictedException on listener socket on port " +
                         myLocalPort + ": " + e);
            } catch (IOException e) {
                /* XXX really bad exception handling -- fix */
                if (tr.tracing)
                    tr.$("Exception on listener socket on port " +myLocalPort);
            }
        }
        try {
            if (tr.tracing)
                tr.$("Trying to close listener socket on port " + myLocalPort);
            myListenServerSocket.close();
            if (tr.tracing)
                tr.$("Closed the listener socket on port " + myLocalPort);
        } catch (IOException e) {
            /* Swallow complaints from close -- just exit anyway */
            if (tr.tracing)
                tr.$("Exception closing listen server socket on port " +
                     myLocalPort);
        }
    }

    /**
     * Shutdown the RtComListener thread.
     *
     * The RtComListener thread itself will die the next time returns from
     * 'accept' and notices that the terminate flag is set. What is more likely
     * is that the process will quiesce and we will go gently into the night,
     * taking the daemon listener thread away with us when we go. We kill the
     * RtComListenerUser thread here so that that can happen.
     */
    synchronized void shutdown() {
        myTerminateFlag = true;
        if (tr.tracing)
            tr.$("RtComListener - shutting down listener on port " +
                 myLocalPort);
        synchronized (myUserThread) {
            try {
                myUserThread.notify();
                if (tr.tracing)
                    tr.$("Notified user thread to stop");
            } catch (Throwable t) {
                /* Swallow exception from notify -- just exit anyway */
                if (tr.tracing)
                    tr.$("Caught exception notifying user thread to stop");
            }
        }
    }
}

/**
 *  "I have seen things you would not believe..."
 *
 *  @see RtComListener.start()
 *  @see RtComListener.shutdown()
 */
class RtComListenerUserThread extends Thread {
    private int myPort;

    RtComListenerUserThread (int port) {
        super("RtComListenerUserThread(" + port + ")");
        myPort = port;
    }

    public void run () {
        synchronized (this) {
            try {
                wait();
            } catch (Throwable t) {
                if (RtComListener.tr.tracing)
                    RtComListener.tr.$("RtComListenerUserThread(" +
                                       myPort + ") stopped");
            }
        }
    }
}

/**
 * Hold onto a thread that is listening for incoming connections.
 */
public class ENetListener {
    private RtComListener myListener;
    private ENetAddr myLocalAddr;

    /**
     * Construct a new listener thread holder. Note that this constructor is
     * only called from within the ec.e.net package!
     *
     * @param listener The listener thread object
     * @param localAddr The address and port we are listening on
     */
    ENetListener(RtComListener listener, ENetAddr localAddr) {
        myListener = listener;
        myLocalAddr = localAddr;
    }

    /**
     * Return the address and port we are listening on.
     */
    public ENetAddr getLocalAddr() {
        return(myLocalAddr);
    }

    /**
     * Tell our listener to stop listening and terminate.
     */
    public void shutdown() {
        if (myListener != null) {
            myListener.shutdown();
        }
        myListener = null;
        myLocalAddr = null;
    }
}
