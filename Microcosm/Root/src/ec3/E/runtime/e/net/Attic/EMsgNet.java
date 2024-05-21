/*
  EMsgNet.java -- Implementation of the message-level ENet interface for the
                  Vat

  Chip Morningstar
  based on earlier work by Eric Messick and Gordie Freedman
  31-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net;
import ec.e.start.Seismologist;
import ec.e.start.Tether;
import ec.e.start.Vat;

/**
 * This class wraps the primitive (i.e., undifferentiated byte blob oriented)
 * ENetSteward with a more message-oriented implementation. ENetSteward gives
 * you no control over the boundary between blobs, leaving that interpretation
 * to a higher-level protocol. This class provides that higher-level protocol;
 * the blobs this class sends and receives are sent and received as individual,
 * discrete virtual packets whose sizes are known before being handed to the
 * object that is to handle them.
 */
class ENetMsgSteward implements ENet {
    private ENet myPrimitiveNet;

    /**
     * Represents the (message-level) network inside a vat.
     */
    ENetMsgSteward(Vat vat) {
        myPrimitiveNet = new ENetSteward(vat);
    }

    /**
     * Establish a new outgoing (message-level) connection.
     *
     * @param remoteAddress The remote address to connect to.
     * @returns An object with which to make use of this new connection.
     */
    public ENetConnection connect(ENetAddr remoteAddress) {
        /* Wrap a primitive connection in a message connection. */
        return new EMsgConnection(myPrimitiveNet.connect(remoteAddress),
                                  false);
    }

    /**
     * Listen for new incoming (message-level) connections.
     *
     * @param localAddress The local address to listen for connections on.
     * @param keeper An object that will be notified of new connections.
     * @returns An object which can be used to stop listening on this address.
     */
    public ENetListener listen(ENetAddr localAddress,
                               ENetListenerKeeper keeper) {
        /* Primitively listen, but use our own wrapped keeper. */
        return myPrimitiveNet.listen(localAddress,
                                     new EMsgListenerKeeper(keeper));
    }
}

/**
 * Provides an ENetConnection that operates in terms of known-sized virtual
 * message packets.
 */
class EMsgConnection implements ENetConnection {
    private ENetConnection myPrimitiveConnection;
    private boolean myIncoming;

    /**
     * Construct an EMsgConnection by wrapping a more primitive connection
     * (canonically an ENetConnection).
     *
     * @param primitiveConnection The primitive connection we are to wrap.
     * @param incoming true->this is an incoming connection; false->we are
     *  establishing an outbound connectoin.
     */
    EMsgConnection(ENetConnection primitiveConnection, boolean incoming) {
        myPrimitiveConnection = primitiveConnection;
        myIncoming = incoming;
    }

    /**
     * Configure this connection to actually participate in communications
     * with our local vat. This method must be called before a connection
     * becomes usable, but may only be called once on a given connection.
     *
     * @param messageReceiver The object which is to receive incoming messages
     *  received on this connection.
     * @param keeper The object which is to be notified about errors and other
     *  problems with respect to this connection.
     */
    void enable(ENetBlobReceiver messageReceiver,
                ENetConnectionKeeper keeper) {
        /* We enable the connection by wrapping our own blob receiver around
           the one provided. However, we can still use the caller's keeper,
           since the message-level connection does not have any new concerns
           with respect to error events. */
        myPrimitiveConnection.enable(
            new EMsgBlobReceiver(myPrimitiveConnection, messageReceiver,
                                 keeper, myIncoming), keeper);
    }

    /**
     * Send a message to the remote party. Messages are send using the Blob
     * Protocol: an int length followed by that many bytes.
     *
     * @param length The number of bytes to send.
     * @param blobData An array containing the bytes to be sent.
     */
    void sendBlob(int length, byte blobData[]) {
        byte lengthBytes[] = intToByteArray(length);
        myPrimitiveConnection.sendBlob(lengthBytes.length, lengthBytes);
        myPrimitiveConnection.sendBlob(length, blobData);
    }

    /**
     * Terminate this connection and cease communicating with the remote party.
     */
    void shutdown() {
        myPrimitiveConnection.shutdown();
    }

    /**
     * Return a byte array containing an int in network byte order.
     */
    private byte[] intToByteArray(int num) {
        ByteArrayOutputStream numBuffer = new ByteArrayOutputStream(4);
        DataOutputStream numStream = new DataOutputStream(numBuffer);
        numStream.writeInt(num);
        return numBuffer.toByteArray();
    }
}

/**
 * The keeper object which a listener uses to deal with new incoming
 * connections.
 */
eclass EMsgListenerKeeper implements ENetListenerKeeper {
    ENetListenerKeeper myOuterKeeper;

    /**
     * Construct a wrapped listener keeper. We hold onto the outer keeper so
     * we can pass events on out to it after we have massaged them.
     *
     * @param outKeeper The original listen() caller's keeper.
     */
    EMsgListenerKeeper(ENetListenerKeeper outerKeeper) {
        myOuterKeeper = outerKeeper;
    }

    /**
     * Sent to enable a listener keeper by informing it who its listener is.
     *
     * @param listener The keeper's listener.
     */
    emethod enable(ENetListener listener) {
        /* Since we are using the primitive listener, just tell the outer
           keeper about that. */
        myOuterKeeper <- enable(listener);
    }

    /**
     * Sent when a new inbound connection is established.
     *
     * @param connection The new connection itself.
     */
    emethod noticeNewConnection(ENetConnection connection) {
        /* We wrap the new primitive connection in a message connection and
           tell the outer keeper about *that* instead. */
        myOuterKeeper <-
            noticeNewConnection(new EMsgConnection(connection));
    }

    /**
     * Sent when an error condition or other problem develops in the listener.
     *
     * @param problem The problem, represented as an Exception.
     */
    emethod noticeProblem(Exception problem) {
        /* Just tell the outer keeper about it, since we don't really care. */
        myOuterKeeper <- noticeProblem(problem);
    }
}

/**
 * Provides the message-boundary handling abstraction to the outer blob
 * receiver. In order to do this, it also needs to handle the EC message-
 * connection setup protocol with the remote party. Alas the latter is a
 * synchronous, ASCII-encoded, line-oriented protocol (since at the point we
 * open the socket we have not yet even established that we are talking to
 * another E process) which sets up for an asynchronous, binary,
 * packet-oriented protocol. Consequently, this class is somewhat
 *  schizophrenic.
 */
class EMsgBlobReceiver implements ENetBlobReceiver {
    ENetConnection myPrimitiveConnection;
    ENetBlobReceiver myOuterReceiver;
    ENetConnectionKeeper myKeeper;
    boolean myIncoming;
    ByteArrayOutputStream myBlobBuffer = new ByteArrayOutputStream();
    String myLocalProcessId = TODO;
    String myLocalSearchPath = TODO;
    String myRemoteProcessId  = TODO;
    String myRemoteSearchPath = TODO;

    /**
     * Construct a message-oriented blob receiver.
     *
     * @param connection The connection we are receiving on
     * @param outerReceiver The user's blob receiver (which is expecting to
     *  see message-oriented blobs).
     * @param incoming true->this is an incoming connection; false->we are
     *  establishing an outbound connection. The connection direction is
     *  important because it tells us which half of the connection setup
     *  protocol to engage in.
     */
    EMsgBlobReceiver(ENetConnection primitiveConnection,
                     ENetBlobReceiver outerReceiver,
                     ENetConnectionKeeper keeper, boolean incoming) {
        myPrimitiveConnection = primitiveConnection;
        myOuterReceiver = outerReceiver;
        myKeeper = keeper;
        myIncoming = incoming;
    }

    /**
     * Receive a primive blob and make it look like the appropriate kind of
     * message blob. Note that a message blob may be spread across multiple
     * primitive blobs, or a primitive blob might contain multiple message
     * blobs. We have to do the buffering.
     *
     * We also have to do the connection setup protocol here. This is
     * necessarily kind of yucky.
     *
     * @param length Length of the primitive blob.
     * @param blobData The blob data bytes themselves.
     */
    void receiveBlob(int length, byte blobData[]) {
        myBlobBuffer.write(blobData, 0, length);
        while (processBlobs())
            ;
    }

    final private static int ST_UNSTARTED          = 0;
    final private static int ST_EXPECT_IWANT_REQ   = 1;
    final private static int ST_EXPECT_IWANT_REPLY = 2;
    final private static int ST_EXPECT_INFO_REQ    = 3;
    final private static int ST_EXPECT_INFO_REPLY  = 4;
    final private static int ST_EXPECT_GO_REQ      = 5;
    final private static int ST_EXPECT_GO_REPLY    = 6;
    final private static int ST_STARTUP_MAX_STATE  = ST_EXPECT_GO_REPLY;
    final private static int ST_EXPECT_PACKET_LEN  = 7;
    final private static int ST_EXPECT_PACKET_DATA = 8;
    final private static int ST_DEAD               = 9;

    int myState = ST_UNSTARTED;

    private boolean processBlobs() {
        if (myState == ST_UNSTARTED) {
            if (myIncoming) {
                myState = ST_EXPECT_IWANT_REQ;
            } else {
                sendLine("IWANT " + myRemoteProcessId);
                myState = ST_EXPECT_IWANT_REPLY;
            }
        } else if (myState == ST_DEAD) {
            return false;
        }
        if (myState <= ST_STARTUP_MAX_STATE) {
            return processStartupBlob();
        } else {
            return processPacketBlob();
        }
    }

    /*
     * The connection setup protocol (Alice is connecting to Bob):
     *
     * Alice: IWANT <bobProcessId>
     *
     *   Bob: 220 IAM <bobProcessId>            (continue) -or-
     *        221 Sorry, not me.                (stop)     -or-
     *        210 TRY <possibleAlternatePath>   (stop)
     *
     * Alice: INFO <aliceProcessId> <alicesPathToAlice>
     *
     *   Bob: 223 INFO <bobsPathToBob>          (continue) -or-
     *        229 DUP                           (stop)     -or-
     *        5xx <problem report>              (stop)
     *
     * Alice: GO
     *
     *   Bob: 225 GO                            (continue) -or-
     *        228 BYE                           (stop)
     *
     * Thence both can send each other blobs according to the "Blob Protocol"
     */

    private int parseToken(String cmd) {
        try {
            token = Integer.parseInt(cmd);
        } catch (NumberFormatException e) {
            if (cmd.equalsIgnoreCase("iwant")) {
                token = TOK_IWANT_STR;
            } else if (cmd.equalsIgnoreCase("info")) {
                token = TOK_INFO_STR;
            } else if (cmd.equalsIgnoreCase("go")) {
                token = TOK_GO_STR;
            } else if (cmd.equalsIgnoreCase("quit")) {
                token = TOK_QUIT_STR;
            } else {
                token = TOK_UNKNOWN_STR;
            }
        }
        return token;
    }

    /*
     * IWANT <localProcessId>
     */
    private void handleStateExpectIWANTReq(int token, String arg, String line){
        if (token == TOK_IWANT_STR) { /* "IWANT <id>" */
            if (arg.equals(myLocalProcessId)) {
                sendLine(TOK_IAM + " IAM " + arg);
                myState = ST_EXPECT_INFO_REQ;
            } else {
                startupError(TOK_NOT_ME, "Sorry, not me.");
            }
        } else {
            startupError(TOK_ERR_PROTOCOL,
                         "Protocol error, expected IWANT, got: " + line);
        }
    }

    /*
     * 220 IAM <remoteProcessId>
     *   -or-
     * 210 TRY <path>
     *   -or-
     * 221 Sorry, not me.
     */
    handleStateExpectIWANTReply(int token, String arg, String line) {
        /* @@@ myConnectionThread.extendSearchVector() */
        if (token == TOK_IAM) { /* "220 IAM" */
            if (arg.equals(myRemoteProcessId)) {
                myState = ST_EXPECT_INFO_REPLY;
                sendLine("INFO " + myLocalProcessId + " " + myLocalSearchPath);
            } else {
                startupError(TOK_ERR_WRONG_ID,
                             "wrong id in IAM, expected " + myRemoteProcessId
                             + " got: " + line);
            }
        } else if (token == TOK_TRY) { /* "210 TRY" */
            myConnectionThread.extendSearchVector(arg);
            startupLocalError(TOK_TRY, line);
        } else if (token == TOK_NOT_ME) { /* "221 Sorry, not me" */
            startupLocalError(TOK_NOT_ME, line);
        } else {
            startupError(TOK_ERR_PROTOCOL,
                         "Protocol error, expected IAM or TRY, got: " + line);
        }
    }

    /*
     * INFO <remoteProcessId> <remotePath>
     */
    private void handleStateExpectINFOReq(int token, String remoteProcessId,
            String remoteSearchPath, String line) {
        /* @@@ myConnection myRegistrarServer.getConnectionToRegistrar()*/
        /* @@@ myConnectionThread.extendSearchVector() */
        if (token == TOK_INFO_STR) { /* INFO Rid path */
            myConnection = myRegistrarServer.getConnectionToProcess(
                remoteProcessId, remoteSearchPath, true);
            myConnectionThread.extendSearchVector(remoteSearchPath);
            myRemoteProcessId = remoteProcessId;
            myRemoteSearchPath = remoteSearchPath;
            
            if (myConnection.newTransceiverLives(this, amIncoming)) {
                myState = ST_EXPECT_GO_REQ;
                sendLine(TOK_INFO + " INFO " + myLocalProcessId + " " +
                         myLocalSearchPath);
            } else {
                startupError(TOK_DUP, "Duplicate connection, dropping.");
            }
        } else {
            startupError(TOK_ERR_PROTOCOL,
                         "Protocol error, expected INFO, got: " + line);
        }
    }

    /*
     * 223 INFO <remotePath>
     *   -or-
     * 229 DUP
     */
    private void handleStateExpectINFOReply(int token, String remoteSearchPath,
                                            String line) {
        /* @@@ myConnection myRegistrarServer.getConnectionToRegistrar()*/
        /* @@@ myConnectionThread.extendsSearchVector() */
        if (token == TOK_INFO) { /* "223 INFO Rid path" */
            myConnection = myRegistrarServer.getConnectionToProcess(
                myRemoteProcessId, remoteSearchPath, true);
            myConnectionThread.extendSearchVector(remoteSearchPath);
            myRemoteSearchPath = remoteSearchPath;
            
            myState = ST_EXPECT_GO_REPLY;
            sendLine("GO");
        } else if (token == TOK_DUP) { /* "229 Dup. connection" */
            startupLocalError(TOK_DUP, line);
        } else {
            startupError(TOK_ERR_PROTOCOL,
                         "Protocol error, expected INFO, got: " + line);
        }
    }

    /*
     * GO
     */
    private void handleStateExpectGOReq() {
        if (token == TOK_GO_STR) {
            sendLine(TOK_GO + " GO");
            myState = ST_EXPECT_PACKET_LEN;
        } else if (token == TOK_QUIT_STR) {
            startupLocalError(TOK_NONE, "quit");
        } else {
            startupError(TOK_ERR_PROTOCOL,
                         "Protocol error, expected GO, got: " + line);
        }
    }

    /*
     * 225 GO
     *   -or-
     * 228 BYE
     */
    private void handleStateExpectGOReply() {
        if (token == TOK_GO) { /* "225 GO" */
            myState = ST_EXPECT_PACKET_LEN;
        } else if (token == TOK_BYE) { /* "228 BYE" */
            startupLocalError(TOK_BYE, "228 BYE");
        } else {
            startupError(TOK_ERR_PROTOCOL,
                         "Protocol error, expected GO, got: " + line);
        }
    }

    private boolean processStartupBlob() {
        if (!haveLine())
            return false;
        String args[] = parseLine();
        String line = args[0];
        Exception problem = null;

        try {
            int token = parseToken(args[1]);
            if (TOK_ERR_MIN <= token && token <= TOK_ERR_MAX) {
                startupLocalError(token, line);
                break;
            }
            switch (myState) {
                case ST_EXPECT_IWANT_REQ:
                    handleStateExpectIWANTReq(token, args[2], line);
                    break;
                case ST_EXPECT_IWANT_REPLY:
                    handleStateExpectIWANTReply(token, args[3], line);
                    break;
                case ST_EXPECT_INFO_REQ:
                    handleStateExpectINFOReq(token, args[2], args[3], line);
                    break;
                case ST_EXPECT_INFO_REPLY:
                    handleStateExpectINFOReply(token, args[3], line);
                    break;
                case ST_EXPECT_GO_REQ:
                    handleStateExpectGOReq();
                    break;
                case ST_EXPECT_GO_REPLY:
                    handleStateExpectGOReply();
                    break;
                default:
                    startupLocalError(TOK_NONE, 
                                      "EMsgConnection startup: unknown state: "
                                      + myState);
                    break;
            }
        } catch (RtConnectionStartupErrorException e) {
            sendLine(e.getMessage());
            problem = e;
        } catch (RtConnectionStartupLocalErrorException e) {
            problem = e;
        }
        if (problem != null) {
            myPrimitiveConnection.shutdown();
            myState = ST_DEAD;
            myKeeper <- noticeProblem(problem);
            return false;
        } else {
            myState = ST_EXPECT_PACKET_LEN;
            return true;
        }
    }

    private void sendLine(String str) {
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream outStream = new DataOutputStream(outbuf);
        outStream.writeUTF(str + "\n");
        myPrimitiveConnection.sendBlob(outbuf.count, outbuf.buf);
    }

    /**
     * Terminate the connection setup protocol with an error.
     */
    private void startupLocalError(int errorToken, String message)
             throws RtConnectionStartupLocalErrorException {
         if (errorToken != TOK_NONE)
             message = errorToken + " " + message;
         throw new RtConnectionStartupLocalErrorException(message);
    }

    /**
     * Terminate the connection setup protocol with an error that will get
     * passed to the remote end too.
     */
    private void startupError(int errorToken, String message)
             throws RtConnectionStartupErrorException {
         if (errorToken != TOK_NONE)
             message = errorToken + " " + message;
         throw new RtConnectionStartupErrorException(message);
    }
}


