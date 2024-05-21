/*
  ENetListen.java -- Classes to manage listening for incoming connections.

  Chip Morningstar
  based on earlier work by Eric Messick and Gordie Freedman
  25-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net;
import ec.e.start.Seismologist;
import ec.e.start.Tether;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A steward object which represents an active listener that is waiting for
 * incoming connections over the network.
 */
class ENetListenerSteward implements ENetListener {
    private Tether myListenerThreadHolder;

    /**
     * The listener steward holds onto the listener thread with a tether.
     */
    ENetListenerSteward(Tether listenerThreadHolder) {
        myListenerThreadHolder = listenerThreadHolder;
    }

    /**
     * Stop listening for new incoming connections.
     */
    public void shutdown() {
        RtNetListenerThread listenerThread =
            (RtNetListenerThread) myListenerThreadHolder.held();
        listenerThread.shutdown();
    }
}

/**
 * A separate thread in which to actually listen for incoming connections,
 * since that is most certainly an operation which blocks.
 */
class RtNetListenerThread extends Thread {
    private boolean myTerminateFlag = false;
    private ENetAddr myLocalAddress;
    private RtNetListenerUserThread myUserThread;
    private Tether myKeeperHolder;
    private Object myUserThreadLock = new Object();

    /**
     * Construct a new object to run in its own thread and listen on a
     * ServerSocket for new incoming connections.
     *
     * @param localAddress The local address we will listen on.
     * @param keeperHolder A tether to an E object to be notified about
     *  interesting events (new connections and errors) that happen while
     *  listening.
     */
    RtNetListenerThread(ENetAddr localAddress, Tether keeperHolder) {
        setDaemon(true);
        myLocalAddress = localAddress;
        myUserThread = new RtNetListenerUserThread(myUserThreadLock);
        myKeeperHolder = keeperHolder;
    }

    /**
     * The actual body of the listener thread. <p>
     *
     * Opens a ServerSocket and accepts connections on it. Each connection
     * spawns a new ENetConnection object that is handed off to the listener's
     * keeper to be dealt with (presumably by enabling it). <p>
     *
     * Loops until told to stop by somebody calling 'shutdown()' (which sets
     * an internal flag).
     */ 
    public void run() {
        try {
            int localPort = myLocalAddress.getPortNumber();
            ServerSocket listenServerSocket = new ServerSocket(localPort);
            while (!myTerminateFlag) {
                Socket clientSocket = listenServerSocket.accept();
                if (myTerminateFlag) {
                    clientSocket.close();
                    break;
                }
                ENetAddr remoteAddress =
                    new ENetAddr(clientSocket.getInetAddress(),
                                 clientSocket.getPort());
                RtNetConnectionThread connectionThread =
                    new RtNetConnectionThread(clientSocket, myLocalAddress,
                                              remoteAddress);
                Tether connectionThreadHolder 
                    = new Tether(myKeeperHolder.vat(), connectionThread);
                ENetConnection newConnection =
                    new ENetConnectionSteward(connectionThreadHolder);
                RtEnvelope msg; msg <-
                    ENetListenerKeeper.noticeNewConnection(newConnection);
                informMyKeeper(msg);
            }
            listenServerSocket.close();
        } catch (IOException e) {
            RtEnvelope msg; msg <- ENetListenerKeeper.noticeProblem(e);
            informMyKeeper(msg);
        }
    }

    /**
     * Shutdown the thread. <p>
     *
     * The listener thread itself will die the next time it returns from
     * 'accept' and notices that the terminate flag is set. What is more
     * likely, however, is that the process will quiesce and we will go gently
     * into the night, taking the daemon listener thread away with us when we
     * go. We kill the listener user thread here so that that can happen.
     */
    void shutdown() {
        myTerminateFlag = true;
        synchronized (myUserThreadLock) {
            myUserThreadLock.notify();
        }
    }

    /**
     * Start the listener thread.
     */
    void startup() {
        /*
          HACK: The listener thread needs to be a daemon thread, because if it
          were a user thread it would be uninteruptible while it was off
          waiting on an accept() (this is due to a flaw in Solaris, actually).
          However, if it's a daemon thread the app can exit even if the thread
          is still running. However, if we're just sitting there waiting for
          connections to arrive over the network, we don't want to exit, we
          want to keep running. Thus we have the RtNetListenerUserThread, which
          does NOTHING but wait. Since it's a user thread it keeps the app from
          exiting (and thus allows the listener thread to keep running waiting
          for a connection) and since it's not waiting on an accept we can kill
          it. When we tell the listener thread to shutdown (which we now can do
          since it's a daemon thread), it sends a notify() to the listener user
          thread whereupon *it* shuts down too. Hallelujah, amen.
        */
        myUserThread.start();
        super.start();
    }

    /**
     * Send an E message to my keeper, taking care of all the grody logic for
     * ordered message send. Messages to the keeper need to be ordered, since
     * they concern events in the external world that have intrinsic
     * temporality.
     */
    private void informMyKeeper(RtEnvelope msg) {
        ENetListenerKeeper keeperSuccessor;
        ENetListenerKeeper keeper = (ENetListenerKeeper) myKeeperHolder.held();
        keeper <- EObject.order(msg, &keeperSuccessor);
        myKeeperHolder =
          myKeeperHolder.vat().makeFragileRoot((Seismologist) keeperSuccessor);
    }
}

/**
 *  "I have seen things you would not believe..."
 *
 *  @see RtNetListenerThread.start()
 *  @see RtNetListenerThread.shutdown()
 */
class RtNetListenerUserThread extends Thread {
    private Object myLock;

    RtNetListenerUserThread(Object lock) {
        myLock = lock;
    }

    public void run() {
        synchronized (myLock) {
            try {
                myLock.wait();
            } catch (InterruptedException e) {
                /* Anything that terminates the wait terminates us too */
            }
        }
    }
}
