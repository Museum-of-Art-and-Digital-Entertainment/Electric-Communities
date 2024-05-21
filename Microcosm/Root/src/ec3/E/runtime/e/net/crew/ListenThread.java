/*
  ENetListen.java -- Classes to manage listening for incoming connections.

  Chip Morningstar
  based on earlier work by Eric Messick and Gordie Freedman
  25-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net.crew;

import ec.e.net.steward.NetworkListener;
import ec.e.net.steward.ByteListenerKludge;
import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.ByteConnection;
import ec.e.net.steward.ByteReceiver;
import ec.e.start.SmashedException;
import ec.e.start.Seismologist;
import ec.e.start.Tether;
import ec.e.start.FragileRootHolder;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A separate thread in which to actually listen for incoming connections,
 * since that is most certainly an operation which blocks.
 */
public class ListenThread extends Thread {
    static Trace tr = new Trace("ec.e.net.ListenThread");
    
    private boolean myTerminateFlag = false;
    private String myLocalAddress;
    private UserThread myUserThread;
    private Tether myInnerListener;
    private Object myUserThreadLock = new Object();
    private boolean mySuspended = false;

    private boolean killerhack;
    
    /**
     * Construct a new object to run in its own thread and listen on a
     * ServerSocket for new incoming connections.
     *
     * @param localAddress The local address we will listen on.
     * @param innerListener A tether to an E object to be notified about
     *  interesting events (new connections and errors) that happen while
     *  listening.
     */
    public ListenThread(String localAddress, Tether innerListener, boolean killer) {
        setDaemon(true);
        myLocalAddress = localAddress;
        myUserThread = new UserThread(myUserThreadLock);
        myInnerListener = innerListener;
        killerhack = killer;
        if (tr.verbose && Trace.ON) tr.verbosem("ListenThread started because...", new Error("where?"));
    }

    /**
     * The actual body of the listener thread. <p>
     *
     * Opens a ServerSocket and accepts connections on it. Each connection
     * spawns a new ByteConnection object that is handed off to the innerListener
     * to be dealt with. <p>
     *
     * Loops until told to stop by somebody calling 'shutdown()' (which sets
     * an internal flag).
     */ 
    public void run() {
        try {
            int localPort = (new NetAddr(myLocalAddress)).getPortNumber();
            ServerSocket listenServerSocket = new ServerSocket(localPort);
            myLocalAddress =
                (new NetAddr(listenServerSocket.getLocalPort())).toString();
            this.setName("ListenThread-" + myLocalAddress);
            myUserThread.setThreadName(myLocalAddress);
            if (tr.debug && Trace.ON)
                tr.debugm("listening at " + myLocalAddress);
            synchronized (myInnerListener.vatLock()) {
                try {
                    ((NetworkListener) myInnerListener.held()).listening(myLocalAddress);
                } catch (SmashedException crunch) {
                    // not much we can do with this exception
                    // the only interested party has just been smashed.
                }
            }
            while (!myTerminateFlag) {
                Socket clientSocket;
                try {
                    clientSocket = listenServerSocket.accept();
                }
                catch (IOException e) {
                    tr.errorm("exception in ListenThread accept()", e);
                    continue;
                }
                if (myTerminateFlag) {
                    clientSocket.close();
                    break;
                }
                if (mySuspended) {
                    clientSocket.close();
                }
                else {
                    setupNewConnection(clientSocket);
                }
            }
            if (tr.debug && Trace.ON)
                tr.debugm("I've been asked to shutdown");
            listenServerSocket.close();
        } catch (IOException e) {
            if (tr.debug && Trace.ON)
                tr.debugm("caught exception", e);
            noticeProblem(e, true);
        } finally {
            myInnerListener = null;
        }
        
        if (tr.debug && Trace.ON)
            tr.debugm("terminated");
    }

    /**
     * Create a new connection to handle the new socket.
     *
     * First, extract the address of the other side from the socket.
     * Then create a ByteConnection inside the vat.
     * Extract the ByteReceiver from the ByteConnection so it can
     *  be plumbed to the RecvThread that the RawConnection is about to build.
     * Make a new Rawconnection, giving it the ByteConnection and ByteReceiver
     *  so the RawConnection and RecvThread can point out to them (via Roots).
     * Extract the SendThread from the RawConnection so the ByteSender
     *  can point to it (via a Tether).
     * Tell the inner listener about the new ByteConnection, passing on the
     *  SendThread.
     */
    private void setupNewConnection(Socket clientSocket) {
        String remoteAddress = (new NetAddr(clientSocket.getInetAddress(),
                                           clientSocket.getPort())).toString();
        if (tr.debug && Trace.ON)
            tr.debugm("new incoming connection from " + remoteAddress);
        ByteConnection newByteConnection = new ByteConnection(remoteAddress);
        ByteReceiver newByteReceiver =
           (ByteReceiver)((NetworkConnection) newByteConnection).getReceiver();
        FragileRootHolder byteConnectionHolder =
            myInnerListener.vat().makeFragileRoot((Object) newByteConnection,
                                             (Seismologist) newByteConnection);
        FragileRootHolder byteReceiverHolder =
            myInnerListener.vat().makeFragileRoot((Object) newByteReceiver,
                                             (Seismologist) newByteConnection);
        RawConnection newRawConnection =
            new RawConnection(clientSocket, myLocalAddress, remoteAddress,
                              byteConnectionHolder, byteReceiverHolder, this, killerhack);
        SendThread newSendThread = newRawConnection.getSender();
        Tether newSendThreadHolder =
            new Tether(myInnerListener.vat(), newSendThread);
        synchronized (myInnerListener.vatLock()) {
            try {
                ((ByteListenerKludge) myInnerListener.held()).noticeConnection(
                    newByteConnection, myLocalAddress, remoteAddress,
                    newSendThreadHolder);
            } catch (SmashedException e) {
                // not much we can do with this exception
                // the only interested party has just been smashed.
            }
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
    public void shutdown() {
        if (tr.debug && Trace.ON) tr.debugm("shutdown");
        myTerminateFlag = true;
        synchronized (myUserThreadLock) {
            myUserThreadLock.notify();
        }
    }

    public void suspendListenThread() {
        mySuspended = true;
    }

    public void resumeListenThread() {
        mySuspended = false; // not really necessary, as this state is not saved
    }

    /**
       Start the listener thread.<p>
       
       HACK: The listener thread needs to be a daemon thread, because
       if it were a user thread it would be uninteruptible while it
       was off waiting on an accept() (this is due to a flaw in
       Solaris, actually).  However, if it's a daemon thread the app
       can exit even if the thread is still running. However, if we're
       just sitting there waiting for connections to arrive over the
       network, we don't want to exit, we want to keep running. Thus
       we have the ec.e.net.crew.UserThread, which does NOTHING but
       wait. Since it's a user thread it keeps the app from exiting
       (and thus allows the listener thread to keep running waiting
       for a connection) and since it's not waiting on an accept we
       can kill it. When we tell the listener thread to shutdown
       (which we now can do since it's a daemon thread), it sends a
       notify() to the listener user thread whereupon *it* shuts down
       too. Hallelujah, amen.
    */
    public void startup() {
        myUserThread.start();
        super.start();
    }

    public void noticeProblem(Throwable t, boolean listenProblem) {
        synchronized (myInnerListener.vatLock()) {
            try {
                ((NetworkListener) myInnerListener.held()).noticeProblem(t, listenProblem);
            } catch (SmashedException crunch) {
                // not much we can do with this exception
                // the only interested party has just been smashed.
            }
        }
    }
}

/**
 *  "I have seen things you would not believe..."
 *
 *  @see ListenThread.startup()
 *  @see ListenThread.shutdown()
 */
class UserThread extends Thread {
    private Object myLock;

    UserThread(Object lock) {
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

    void setThreadName(String listenAddr) {
        this.setName("UserThread-" + listenAddr);
    }
}
