package ec.e.net.crew;

import ec.util.NestedException;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.NetworkConnectionError;
import ec.e.net.steward.ByteConnectionKludge;
import ec.e.start.Tether;
import ec.e.start.SmashedException;
import ec.e.start.FragileRootHolder;

/**
   Crew representing a socket connection.  The in-vat counterpart of
   this class is ByteConnection.  Responsible for the establishment of
   outbound connections, and creating SendThread and RecvThread for
   both incoming and outgoing connections.<p>

   For an outgoing connection, a ByteConnection will create the
   RawConnection, giving the constructor a FragileRootHolder to the
   ByteConnection, and another to the innerReceiver, as well as the
   address to make the connection to.  The RawConnection constructs
   and start()'s a ConnectThread to make the connection.  The
   ConnectThread calls RawConnection.outgoingSetup() when the socket
   open completes.  outgoingSetup makes the SendThread and
   RecvThread, and calls ByteConnection.outgoingSetup().  The
   outgoingSetup's allow the NetworkSender chain to be built (the
   receiver chain was constructed on the way in).  When the top level
   sender is built, it should call enable() on it's outerSender.  The
   outerSender.enable() reaches the SendThread, crosses over to
   RawConnection.enable() which start()'s the RecvThread.<p>

   For an incoming connection, a ListenThread constructs the
   RawConnection, which can immediately construct the SendThread and
   RecvThread using the provided socket.  The ListenThread has
   preconstructed a ByteConnection in a messy asymmetry intended to
   minimize the creation of FragileRootHolder's and Tether's.  With
   all of this code as trusted stewards and crew, this could perhaps
   be normalized to the same senerio used for higher layers, but I
   haven't examined that possibility seriously.  When
   ByteConnection.incomingSetup() gets called by ByteListener, it
   calls RawConnection.getSender() so that it can create the
   ByteSender.  When the incoming connection propogates out to a layer
   that's actually willing to receive bytes, it calls
   outerSender.enable(), as for the outgoing connection.
*/
public class RawConnection {
    private static final Trace tr = new Trace("ec.e.net.RawConnection");
    
    // length of byte[] allocated for each RecvThread read
    // XXX probably needs to be tuned
    private static final int bufLen = 1024;

    private FragileRootHolder myInnerConnection;
    /*package*/ String myRemoteAddr;
    private String myLocalAddr;
    private RecvThread myReceiver;
    private SendThread mySender;
    private Socket mySocket;
    private ListenThread myListener;
    
    /**
       Handle creation of a new incoming connection.

       @param s the socket on which the incoming connection is to be established.
       @param localAddr the address of our end of the socket.
       @param remoteAddr the address of the far end of the socket.
       @param innerConnection the ByteConnection we should hook up to.
       @param innerReceiver a ByteReceiver to which RecvThread should send bytes.
      */
    public RawConnection(Socket s, String localAddr, String remoteAddr, FragileRootHolder innerConnection, FragileRootHolder innerReceiver, ListenThread listener, boolean killerhack) {
        myInnerConnection = innerConnection;
        myLocalAddr = localAddr;
        myRemoteAddr = remoteAddr;
        myListener = listener;

        constructSenderAndReceiver(s, innerReceiver, killerhack);
    }

    /**
       Handle creation of a new outgoing connection.  Starts a new
       thread to perform the connection attempt.  Any problems are
       reported later via ByteConnection.noticeProblem().

       @param remoteAddr the address of the far end of the socket.
       @param innerConnection the ByteConnection we should hook up to.
       @param innerReceiver a ByteReceiver to which RecvThread should send bytes.
      */
    public RawConnection(String remoteAddr, FragileRootHolder innerConnection, FragileRootHolder innerReceiver) {
        myInnerConnection = innerConnection;
        myRemoteAddr = remoteAddr;

        if (tr.debug && Trace.ON) tr.$("Attempting outgoing connection to " + remoteAddr);
        ConnectThread conn = new ConnectThread(remoteAddr, innerReceiver, this);
        conn.start();
    }

    /**
       Continue outgoing connection setup after socket open completes.
       Runs in ConnectThread.  We don't need to save innerReceiver (we
       just hand it to RecvThread), so the ConnectThread keeps it and
       hands it back to us here.

       @param localAddr the address of our end of the socket.
       @param s the socket on which the outgoing connection has been established.
       @param innerReceiver a ByteReceiver to which RecvThread should send bytes.
      */
    void outgoingSetup(String localAddr, Socket s, FragileRootHolder innerReceiver) {
        if (tr.debug && Trace.ON) tr.$("Acquired outgoing connection to " + myRemoteAddr + " from " + localAddr);
        myLocalAddr = localAddr;
        constructSenderAndReceiver(s, innerReceiver, false);
        Tether newSendThreadHolder = new Tether(myInnerConnection.vat(), mySender);
        synchronized (myInnerConnection.vatLock()) {
            try {
                ((ByteConnectionKludge) myInnerConnection.held()).outgoingSetOuterSender(newSendThreadHolder, localAddr);
            } catch (SmashedException crunch) {
                // not much we can do with this exception
                // the only interested party has just been smashed.
            }
        }
    }

    /**
       Pass an exception to someone who cares.  Either we or a peer of
       us has caught an exception.  Since they're probably sitting at
       the bottom level of some thread, they have no parent to throw
       to, so they pitch it over to us.  We need to get it into the
       vat if possible.  After a quake, there shouldn't be any crew to
       catch SmashedException's but there isn't anything to do about
       it anyway.

       @param t the problem.
      */
    public void noticeProblem(Throwable t) {
        if (tr.debug && Trace.ON) tr.debugm("got exception", t);
        if (myInnerConnection == null) {
            tr.verboseReportException(t, "problem on torn down connection");
            return;
        }
        
        if (t instanceof NetworkConnectionError) {
            // NetworkConnectionError means the connection is not
            // fully set up at this point.
            if (myListener != null) {
                t = ((NetworkConnectionError) t).getNestedThrowable();
                myListener.noticeProblem(t, false);
            } else {
                shutdown();
                // if the connection is outgoing (no listener) and not
                // fully set up (NetworkConnectionError), then it must
                // be a crossed connection being torn down.  ignore
                // it.
            }
            return;
        }
        
        synchronized (myInnerConnection.vatLock()) {
            try {
                if (myInnerConnection != null) {
                    ((NetworkConnection) myInnerConnection.held()).noticeProblem(t);
                }
            } catch (SmashedException crunch) {
                // not much we can do with this exception
                // the only interested party has just been smashed.
            } catch (NetworkConnectionError e) {
                // couldn't report this via the usual channels, try
                // the back door.
                if (myListener != null) {
                    t = ((NetworkConnectionError) e).getNestedThrowable();
                    myListener.noticeProblem(t, false);
                }
            }
        }
        shutdown();
    }

    /**
       Close a connection.  Called by both SendThread and RecvThread
       just before they exit.  Notifies both SendThread and
       RecvThread, and then sends the noticeShutdown() along the
       NetworkConnection chain.  Silently eats any later shutdown()'s.
      */
    public void shutdown() {
        Socket closing = mySocket;
        mySocket = null;
        byte[] sendIV = null;
        byte[] receiveIV = null;
        
        if (closing != null) {
            try {
                if (tr.debug && Trace.ON) tr.$("Closing connection to " + myRemoteAddr + " from " + myLocalAddr);
                mySender.close();
            } catch (IOException e) {
                noticeProblem(e); // one could argue that this should be ignored...
            }
            sendIV = mySender.getIV();
            mySender = null;
            myReceiver.shutdown();
            receiveIV = myReceiver.getIV();
            myReceiver = null;
            try {
                closing.close();
            } catch (IOException e) {
                noticeProblem(e);
            }
        }

        if (myInnerConnection != null) {
            synchronized (myInnerConnection.vatLock()) {
                try {
                    if (myInnerConnection != null) {
                        ((NetworkConnection) myInnerConnection.held()).noticeShutdown(
                                sendIV, receiveIV);
                    }
                } catch (SmashedException crunch) {
                    // not much we can do with this exception
                    // the only interested party has just been smashed.
                }
            }
        }
        myInnerConnection = null;
    }

    /**
       Return the SendThread associated with this RawConnection.  Only
       needed on RawConnection, not part of NetworkConnection
       interface.

       @return the SendThread.
      */
    public SendThread getSender() {
        return mySender;
    }

    /**
       Start the RecvThread.  Only called from SendThread when it
       receives an enable() from it's ByteSender.
      */
    void enable() {
        myReceiver.start();
    }

    /**
       Reconfigure the protocol.  The new values are parameters.
      */
    /*package*/ void changeProtocol(
                boolean isAggragating,
                String macType,
                byte[] macKey, 
                String encryptionType,
                byte[] encryptionKey,
                byte[] outIV, 
                byte[] inIV,
                boolean isCompressing,
                boolean useSmallZip) throws IOException {
        myReceiver.changeProtocol(isAggragating, macType, macKey,
                                    encryptionType, encryptionKey, outIV, inIV,
                                    isCompressing, useSmallZip);
    }

    /**
      Send line statistics up the protocol stack
     
      @param messageLength the length of the comm protocol or E message.
      @param compressedLength the length sent on the socket.
     */
    /*package*/void updateSendCounts(int messageLength, int compressedLength) {
        if (myInnerConnection != null) {
            synchronized (myInnerConnection.vatLock()) {
                try {
                    if (myInnerConnection != null) {
                        ((NetworkConnection) myInnerConnection.held())
                            .updateSendCounts(messageLength, compressedLength);
                    }
                } catch (SmashedException crunch) {
                    // not much we can do with this exception
                    // the only interested party has just been smashed.
                }
            }
        }
    }


    /**
      Send line statistics up the protocol stack
     
      @param messageLength the length of the comm protocol or E message.
      @param compressedLength the length sent on the socket.
     */
    /*package*/void updateReceivedCounts(int messageLength, int compressedLength) {
        if (myInnerConnection != null) {
            synchronized (myInnerConnection.vatLock()) {
                try {
                    if (myInnerConnection != null) {
                        ((NetworkConnection) myInnerConnection.held())
                            .updateReceivedCounts(messageLength, compressedLength);
                    }
                } catch (SmashedException crunch) {
                    // not much we can do with this exception
                    // the only interested party has just been smashed.
                }
            }
        }
    }

    private void constructSenderAndReceiver(Socket s, FragileRootHolder innerReceiver, boolean killerhack) {
        mySocket = s;
        try {
            mySender = new SendThread(s.getOutputStream(), this);
            (new Thread(mySender, "SendThread-" + myRemoteAddr)).start();
            myReceiver = new RecvThread(s.getInputStream(), innerReceiver, this, bufLen, myRemoteAddr, killerhack);
            // myReceiver is started by enable along send chain
        } catch (IOException e) {
            noticeProblem(e);
        }
    }
}

/**
   A thread for making outgoing connections.  The RawConnection
   constructor for an outgoing connection can return immediately after
   setting this thread into motion.  The DNS lookup (if necessary) to
   resolve the IP address happens here, as does the block waiting for
   the connection to go through.  When the connection goes through,
   control is passed back to RawConnection for further processing.
  */
class ConnectThread extends Thread {
    String myRemoteAddr;
    FragileRootHolder myInnerReceiver;
    RawConnection myConnection;

    /**
       Construct a thread to block waiting for an outgoing connection
       to go through.

       @param remoteAddr where to connect to.
       @param innerReceiver hold this so RawConnection doesn't have to.
       @param connection who to notify when the socket open is done.
      */
    ConnectThread(String remoteAddr, FragileRootHolder innerReceiver, RawConnection connection) {
        super("ConnectThread-" + remoteAddr);
        myRemoteAddr = remoteAddr;
        myInnerReceiver = innerReceiver;
        myConnection = connection;
    }

    /**
       Actually open the socket.
      */
    public void run() {
        Socket clientSocket;
        NetAddr localNetAddr;

        try {
            NetAddr remoteNetAddr = new NetAddr(myRemoteAddr);
            clientSocket = new Socket(remoteNetAddr.getInetAddress(),
                                      remoteNetAddr.getPortNumber());
            localNetAddr = new NetAddr(clientSocket.getLocalPort());
            myConnection.outgoingSetup(localNetAddr.toString(), clientSocket, myInnerReceiver);
        } catch (Exception e) {
            myConnection.noticeProblem(e);
        }
    }
}
