/*
  ENetConnect.java -- Classes to manage network communications connections.

  Chip Morningstar
  based on earlier work by Eric Messick and Gordie Freedman
  25-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net;
import ec.e.start.Seismologist;
import ec.e.start.Tether;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A steward object which represents an active communications pathway to and
 * from another party on the network.
 */
class ENetConnectionSteward implements ENetConnection {
    private Tether myConnectionThreadHolder;
    private boolean amEnabled = false;

    /**
     * The connection steward holds onto the connection thread with a tether.
     */
    ENetConnectionSteward(Tether connectionThreadHolder) {
        myConnectionThreadHolder = connectionThreadHolder;
    }

    /**
     * Configure this connection to actually participate in communications
     * with our local vat. This method must be called before a connection
     * becomes usable, but may only be called once on a given connection.
     *
     * @param receiver The object which is to receive incoming (low-level)
     *  messages received on this connection.
     * @param keeper The object which is to be notified about errors and other
     *  problems with respect to this connection.
     */
    public void enable(ENetBlobReceiver receiver, ENetConnectionKeeper keeper){
        if (amEnabled)
            throw new ConnectionAlreadyEnabledException();
        amEnabled = true;

        RtEnvelope msg; msg <- ENetConnectionKeeper.enable(this);
        EConnectionKeeper keeperSuccessor;
        keeper <- EObject.order(msg, &keeperSuccessor);
        Tether keeperHolder = myConnectionThreadHolder.vat().
            makeFragileRoot((Seismologist) keeperSuccessor);
        RtNetConnectionThread connectionThread =
            (RtNetConnectionThread) myConnectionThreadHolder.held();
        connectionThread.start(receiver, keeperHolder);
    }

    /**
     * Send a (low-level) message to the remote party.
     *
     * @param blobData An array of bytes to send.
     */
    public void sendBlob(int length, byte blobData[]) {
        RtNetConnectionThread connectionThread =
            (RtNetConnectionThread) myConnectionThreadHolder.held();
        connectionThread.sendBlob(length, blobData);
    }

    /**
     * Terminate this connection and cease communicating with the remote party.
     */
    public void shutdown() {
        RtNetConnectionThread connectionThread =
            (RtNetConnectionThread) myConnectionThreadHolder.held();
        connectionThread.shutdown();
    }
}

/**
 * A separate thread in which to actually communicate with another party over
 * the network.
 *
 * This thread does protocol setup and message sends itself, and creates yet
 * another thread for handling message receives.
 */
class RtNetConnectionThread extends Thread {
    private Socket mySocket;
    private ENetAddr myLocalAddress;
    private ENetAddr myRemoteAddress;
    private boolean amIncoming;
    private ENetBlobReceiver myReceiver;
    private Tether myKeeperHolder;
    private boolean myTerminateFlag = false;
    private Object mySendQueueLock = new Object();
    private RtMsgQEntry mySendQueueHead = null;
    private RtMsgQEntry mySendQueueTail = null;
    private RtNetConnectionReceiveThread myReceiveThread;
    private BufferedOutputStream myOutputStream;
    private BufferedInputStream myInputStream;

    /**
     * Construct a new connection thread for an outgoing connection. In this
     * case we'll have to open the socket ourselves.
     *
     * @param localAddress Our local address.
     * @param remoteAddress The address we are connecting to.
     */
    RtNetConnectionThread(ENetAddr remoteAddress) {
        myLocalAddress = new LocalENetAddr(0);
        myRemoteAddress = remoteAddress;
        amIncoming = false;
    }

    /**
     * Construct a new connection thread for in incoming connection. In this
     * case we are given the socket to communicate with.
     *
     * @param socket An already-open socket to communicate with.
     * @param localAddress Our local address.
     * @param remoteAddress The address that is connecting to us.
     */
    RtNetConnectionThread(Socket socket, ENetAddr localAddress,
                          ENetAddr remoteAddress) {
        mySocket = socket;
        myLocalAddress = localAddress;
        myRemoteAddress = remoteAddress;
        amIncoming = true;
    }

    /**
     * The actual connection thread -- the asynchronous part.
     */
    public void run() {
        /* At this level, all errors are considered unrecoverable. We simply
           catch'em and tell the keeper about'em. */
        try {
            /* First, make sure we can actually do I/O */
            if (!myTerminateFlag) {
                setupStreams();
            }

            /* Then start yet another thread to do the reads (we will do the
               writes in this thread). */
            if (!myTerminateFlag) {
                myReceiveThread = new RtNetConnectionReceiveThread(
                    myInputStream, myKeeperHolder, myReceiver);
                myReceiveThread.start();
            }

            /* Loop on the send queue until somebody tells us to stop or we
               get blown away by an error. */
            while (!myTerminateFlag) {
                try {
                    RtMsgQEntry entry;
                    synchronized (mySendQueueLock) {
                        while (mySendQueueHead == null)
                            mySendQueueLock.wait();
                        if (myTerminateFlag) /* If shutdown() during wait */
                            break;
                        entry = mySendQueueHead;
                        mySendQueueHead = mySendQueueHead.getNext();
                        if (mySendQueueHead == null)
                            mySendQueueTail = null;
                    }
                    myOutputStream.write(entry.getblob(), 0,
                                         entry.getLength());
                } catch (InterruptedException e) {
                    /* Just swallow this and try again */
                }
            }
        } catch (Exception e) {
            /* Normally IOException is the only thing we will catch here, but
               in any case it's not our problem, it's the keeper's problem. */
            if (!myTerminateFlag) { /* If we're dead, don't bother */
                RtEnvelope msg; msg <-
                    ENetConnectionKeeper.noticeProblem(e);
                informMyKeeper(msg);
            }
        }
        if (mySocket != null){/* Since we can land here prior to socket open */
            try {
                mySocket.close();
            } catch (IOException e) {
                /* Permit close to fail; there's nothing we can do about it and
                   as soon as we return it will be moot anyway. */
            }
        }
    }

    /**
     * Send a blob of bytes out on this connection. <p>
     *
     * Note that this method runs in the Vat thread, not the connection thread.
     * Thus it "sends" by putting the blob on the connection thread's send
     * queue and notifying.
     *
     * @param length The number of bytes to send.
     * @param blobData An array containing the data bytes to be sent.
     */
    void sendBlob(byte blobData[]) {
        RtMsgQEntry entry = new RtMsgQEntry(length, blobData);
        synchronized (mySendQueueLock) {
            if (mySendQueueTail != null)
                mySendQueueTail.setNext(entry);
            else
                mySendQueueHead = entry;
            mySendQueueTail = entry;
            mySendQueueLock.notify();
        }
    }

    /**
     * Shutdown this thread and the receive thread that goes with it.
     */
    void shutdown() {
        myTerminateFlag = true;
        myReceiveThread.shutdown(); /* And your little dog, too */
    }

    /**
     * Start up the connection thread.
     *
     * @param reciever The object which is to get any blobs received.
     * @param keeperHolder A tether to the E object that is to be notified
     *  about interesting events (errors) that happen while communicating.
     */
    void start(ENetBlobReceiver receiver, Tether keeperHolder) {
        myReceiver = receiver;
        myKeeperHolder = keeperHolder;
        super.start();
    }

    /**
     * Send an E message to my keeper, taking care of all the grody logic for
     * ordered message send. Messages to the keeper need to be ordered, since
     * they concern events in the external world that have intrinsic
     * temporality.
     */
    private void informMyKeeper(RtEnvelope msg) {
        ENetConnectionKeeper keeperSuccessor;
        ENetConnectionKeeper keeper =
            (ENetConnectionKeeper) myKeeperHolder.held();
        keeper <- EObject.order(msg, &keeperSuccessor);
        myKeeperHolder =
          myKeeperHolder.vat().makeFragileRoot((Seismologist) keeperSuccessor);
    }

    /**
     * Set up or input and output streams so that we can actually engage in
     * communications. If we're the ones originating the connection we also
     * have to establish our socket.
     */
    private void setupStreams() throws IOException {
        if (!amIncoming)
            mySocket = new Socket(myRemoteAddress.getInetAddress(),
                                  myRemoteAddress.getPortNumber());
        myOutputStream = new BufferedOutputStream(mySocket.getOutputStream());
        myInputStream = new BufferedInputStream(mySocket.getInputStream());
    }
}

/**
 * A thread to receive communications over a connection. This is a separate
 * thread since the receive operation can block. It operates in cahoots with an
 * RtNetConnectionThread, which is the sole object possessing a reference to
 * it.
 */
class RtNetConnectionReceiveThread extends Thread {
    private boolean myTerminateFlag = false;
    private BufferedInputStream myInputStream;
    private Tether myKeeperHolder;
    private ENetBlobReceiver myReceiver;

    /**
     * Construct a thread to receive communications.
     *
     * @param inputStream The input stream to read on.
     * @param keeperHolder A tether to our keeper.
     * @param receiver The object will processed blobs that we receive.
     */
    RtNetConnectionReceiveThread(BufferedInputStream inputStream,
            Tether keeperHolder, ENetBlobReceiver receiver) {
        myInputStream = inputStream;
        myKeeperHolder = keeperHolder;
        myReceiver = receiver;
    }

    /**
     * The actual connection receive thread -- the asynchronous part.
     */
    public void run() {
        /* At this level, all errors are considered unrecoverable. We catch
           them all and pitch them to our keeper. */
        try {
            /* Loop reading blobs until somebody tells us to stop or we get
               blown away by an error. */
            while (!myTerminateFlag) {
                byte message[] = new byte[myInputStream.buf.length];
                int readLength = myInputStream.read(message);
                if (readLength < 0)
                    throw new EOFException();
                /* Actually dealing with it is somebody else's business */
                myReceiver.receiveBlob(readLength, message);
            }
        } catch (Exception e) {
            /* If the connection thread exits, it will close the socket, which
               in turn will cause us to catch an IOException here. But that
               should not be treated as an error, since it's the normal way we
               get unblocked during a shutdown. So don't bother telling the
               keeper if we catch an exception and the terminate flag is set.
               Suicide is not an error (here). */
            if (!myTerminateFlag) {
                RtEnvelope msg; msg <-
                    ENetConnectionKeeper.noticeProblem(e);
                informMyKeeper(msg);
            }
        }
    }

    /**
     * Shutdown this thread.
     */
    void shutdown() {
        myTerminateFlag = true;
    }

    /**
     * Send an E message to my keeper, taking care of all the grody logic for
     * ordered message send. Messages to the keeper need to be ordered, since
     * they concern events in the external world that have intrinsic
     * temporality.
     */
    private void informMyKeeper(RtEnvelope msg) {
        ENetConnectionKeeper keeperSuccessor;
        ENetConnectionKeeper keeper =
            (ENetConnectionKeeper) myKeeperHolder.held();
        keeper <- EObject.order(msg, &keeperSuccessor);
        myKeeperHolder =
          myKeeperHolder.vat().makeFragileRoot((Seismologist) keeperSuccessor);
    }
}

class ConnectionAlreadyEnabledException extends RuntimeException {
    public ConnectionAlreadyEnabledException() { super(); }
}
