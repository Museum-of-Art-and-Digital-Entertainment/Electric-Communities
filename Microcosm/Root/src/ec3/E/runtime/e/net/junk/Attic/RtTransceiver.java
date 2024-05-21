package ec.e.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class via which an RtConnection communicates with the remote machine it is
 * connected to.
 */
class RtTransceiver {
    private long myOutMessageCount = 0; // for perf & diagnostic stats only
    private long myInMessageCount = 0;  // for perf & diagnostic stats only

    static Trace tr = new Trace(false, "[RtTransceiver]");
    public static Trace ptr = new Trace(false, "[RtTransceiverProfile]");
    private static int ourSerialcounter = 1;

    private DataInputStream myInputStream;
    private DataOutputStream myOutputStream;
    private ENetAddr myLocalAddr;
    private ENetAddr myRemoteAddr;
    private RtConnection myConnection;
    private RtRegistrarServer myLocalRegistrarServer;
    private Socket myLocalSocket;
    private String myLocalPidCertificate;
    private String myLocalPublisherId;
    private String myLocalRegistrarId;
    private String myLocalSearchPath;
    private String myRemotePidCertificate;
    private String myRemotePublisherId;
    private String myRemoteRegistrarId;
    private String myRemoteSearchPath;
    private boolean amIncoming;
    private int mySerialNumber;

    /**
     * Construct a new transceiver given lots of parameters...
     *
     * @param registrarServer Our connection's registrar server
     * @param registrarId The universal ID of our registrar
     * @param publisherId The universal ID of our publisher
     * @param searchPath Path for finding things XXX explain better
     * @param socket An open connection socket to the other end
     * @param localAddr Our local IP addr and port
     * @param remoteAddr The IP addr and port at the other end
     * @param remoteRegistrarId The universal ID of the other guy's registrar
     * @param remotePath Path for the other side to find things XXX improve
     * @param incoming true->remote is establishing connection, false->we are
     */
    RtTransceiver(RtRegistrarServer registrarServer, String registrarId,
            String publisherId, String searchPath, Socket socket,
            ENetAddr localAddr, ENetAddr remoteAddr, String remoteRegistrarId,
            String remotePath, boolean incoming) throws IOException {
        mySerialNumber = nextSerialNumber();

        if (tr.tracing)
            tr.$("New RtTransceiver[" + mySerialNumber + "] Rid:" +
                 registrarId + ", Pid:" + publisherId + ", searchPath:" +
                 searchPath + ", myLocalAddr:" + localAddr +
                 ", myRemoteAddr:" + remoteAddr + ", incoming:" + incoming);
        myLocalSocket = socket;
        myInputStream = new DataInputStream(
            new BufferedInputStream(socket.getInputStream()));
        myOutputStream = new DataOutputStream(
            new BufferedOutputStream(socket.getOutputStream()));
        myLocalRegistrarServer = registrarServer;
        myLocalRegistrarId = registrarId;
        myLocalPublisherId = publisherId;
        myLocalSearchPath = searchPath;
        myLocalAddr = localAddr;
        myLocalPidCertificate = "<InsertCertificateHere>";
        myRemoteAddr = remoteAddr;
        myRemoteSearchPath = remotePath;
        myRemoteRegistrarId = remoteRegistrarId;
        myRemotePublisherId = null;
        myRemotePidCertificate = null;
        amIncoming = incoming;
    }

    /**
     * Issue a new transceiver serial number. Note that this is only for
     * tracing and diagnostics as it is not persistent.
     */
    static synchronized private int nextSerialNumber() {
        return ++ourSerialcounter;
    }

    final private static int TOK_IWANT_STR     =   1;
    final private static int TOK_INFO_STR      =   2;
    final private static int TOK_GO_STR        =   3;
    final private static int TOK_QUIT_STR      =   4;
    final private static int TOK_UNKNOWN_STR   =  -1;

    /* Magic numbers in protocol messages.
       XXX Who makes these things up anyway? Is it part of some secret IANA
       conspiracy or is there actually a standard or did Eric just roll
       dice? */
    final private static int TOK_TRY             = 210;
    final private static int TOK_IAM             = 220;
    final private static int TOK_NOT_ME          = 221;
    final private static int TOK_INFO            = 223;
    final private static int TOK_GO              = 225;
    final private static int TOK_BYE             = 228;
    final private static int TOK_DUP             = 229;
    final private static int TOK_ERR_MIN         = 500;
    final private static int TOK_ERR_PROTOCOL    = 500;
    final private static int TOK_ERR_WRONG_ID    = 522;
    final private static int TOK_ERR_ID_MISMATCH = 524;
    final private static int TOK_ERR_BAD_CERT    = 553;
    final private static int TOK_ERR_BAD_CERT2   = 554;
    final private static int TOK_ERR_MAX         = 599;
    final private static int TOK_NONE            =   0;

    final private static int ST_EXPECT_IWANT_REQ   = 1;
    final private static int ST_EXPECT_IWANT_REPLY = 2;
    final private static int ST_EXPECT_INFO_REQ    = 3;
    final private static int ST_EXPECT_INFO_REPLY  = 4;
    final private static int ST_EXPECT_GO_REQ      = 5;
    final private static int ST_EXPECT_GO_REPLY    = 6;

    /**
     * Engage in the transceiver startup protocol with the guy on the other
     * end. Three possible outcomes of this process are success, failure and
     * error. The first two are indicated by the return value, the third by
     * an exception.
     *
     * @return true->success, false->failure
     * @throws RtTransceiverException on error
     *
     * the protocol:
     *
     * << IWANT id
     * >> 220 IAM id                    221 Sorry, not me.   210 TRY path
     * << INFO Rid Pid path cert
     * >> 223 INFO Rid Pid path cert    229 DUP              5xx problem report
     * << GO
     * >> 225 GO                                             228 BYE
     */
    public boolean startup() throws IOException, RtTransceiverException { /*@BLOCK METHOD */
        if (tr.tracing)
            tr.$(this + " startup protocol, incoming:" + amIncoming);

        /* XXX need to worry about blocking by the other side. Perhaps when we
           have interruptable reads we can start a timer thread. */

        int state;
        if (amIncoming) {
            /* In the incoming case, our RtConnection is already established */
            state = ST_EXPECT_IWANT_REQ;
        } else {
            /* In the outgoing case, we make the connection ourselves */
            myConnection = myLocalRegistrarServer.getConnectionToRegistrar(
                myRemoteRegistrarId, null, myRemoteSearchPath, true);
            sendline("IWANT " + myRemoteRegistrarId); /*@BLOCK */
            state = ST_EXPECT_IWANT_REPLY;
        }
        String except = null;
        try {
            while (except == null) {
                String args[] = getline(); /*@BLOCK */
                if (args == null) {
                    return false;
                }
                String line = args[0];
                String cmd  = args[1];
                String arg1 = args[2];
                String arg2 = args[3];
                String arg3 = args[4];
                String arg4 = args[5];
                String arg5 = args[6];
                
                int token;
                try {
                    token = Integer.parseInt(cmd);
                } catch (NumberFormatException e) {
                    if (cmd.equalsIgnoreCase("iwant"))
                        token = TOK_IWANT_STR;
                    else if (cmd.equalsIgnoreCase("info"))
                        token = TOK_INFO_STR;
                    else if (cmd.equalsIgnoreCase("go"))
                        token = TOK_GO_STR;
                    else if (cmd.equalsIgnoreCase("quit"))
                        token = TOK_QUIT_STR;
                    else
                        token = TOK_UNKNOWN_STR;
                }
                if (TOK_ERR_MIN <= token && token <= TOK_ERR_MAX) {
                    except = line;
                    break;
                }
                switch (state) {
                case ST_EXPECT_IWANT_REQ:
                    if (token == TOK_IWANT_STR) { /* "IWANT <id>" */
                        if (arg1.equals(myLocalPublisherId) ||
                                arg1.equals(myLocalRegistrarId)) {
                            sendline(TOK_IAM + " IAM " + arg1); /*@BLOCK */
                            state = ST_EXPECT_INFO_REQ;
                        } else {
                            startupError(TOK_NOT_ME, "Sorry, not me.");
                        }
                    } else {
                        startupError(TOK_ERR_PROTOCOL,
                            "Protocol error, expected IWANT, got: " + line);
                    }
                    break;
                case ST_EXPECT_IWANT_REPLY:
                    if (token == TOK_IAM) { /* "220 IAM" */
                        if (arg2.equals(myRemoteRegistrarId)) {
                            state = ST_EXPECT_INFO_REPLY;
                            sendline("INFO " + myLocalRegistrarId + " " +
                                myLocalPublisherId + " " + myLocalSearchPath +
                                " " + myLocalPidCertificate); /*@BLOCK */
                        } else {
                            startupError(TOK_ERR_WRONG_ID,
                                        "wrong id in IAM, expected " +
                                        myRemoteRegistrarId + " got: " + line);
                        }
                    } else if (token == TOK_TRY) { /* "210 TRY addr" */
                        myConnectionThread.extendSearchVector(arg2);
                        startupFailure();
                    } else if (token == TOK_NOT_ME) { /* "221 Sorry, not me" */
                        startupFailure();
                    } else {
                        startupError(TOK_ERR_PROTOCOL,
                            "Protocol error, expected IAM or TRY, got: " +
                            line);
                    }
                    break;
                case ST_EXPECT_INFO_REQ:
                    if (token == TOK_INFO_STR) { /* INFO Rid Pid path cert */
                        if (tr.tracing)
                            tr.$(this + " looking up connection");
                        myConnection =
                            myLocalRegistrarServer.getConnectionToRegistrar(
                                arg1, arg2, arg3, true);
                        if (tr.tracing)
                            tr.$(this + " extending search vector '" +
                                 arg3 + "'");
                        myConnectionThread.extendSearchVector(arg3);
                        myRemoteRegistrarId = arg1;
                        myRemotePublisherId = arg2;
                        myRemoteSearchPath = arg3;
                        myRemotePidCertificate = arg4;
                        if (!verifyCertificate(myRemotePidCertificate,
                                               myRemoteRegistrarId,
                                               myRemotePublisherId)) {
                            startupError(TOK_ERR_BAD_CERT,
                                         "certificate not accepted");
                            break;
                        }

                        if (myConnection.newTransceiverLives(this,
                                amIncoming)) {
                            state = ST_EXPECT_GO_REQ;
                            sendline(TOK_INFO + " INFO " +
                                myLocalRegistrarId + " " + myLocalPublisherId +
                                " " + myLocalSearchPath + " " +
                                myLocalPidCertificate); /*@BLOCK */
                        } else {
                            startupError(TOK_DUP,
                                         "Duplicate connection, dropping.");
                        }
                    } else {
                        startupError(TOK_ERR_PROTOCOL,
                            "Protocol error, expected INFO, got: " + line);
                    }
                    break;
                case ST_EXPECT_INFO_REPLY:
                    if (token == TOK_INFO) { /* "223 INFO Rid Pid path cert" */
                        if (tr.tracing) tr.$(this + " looking up connection");
                        myConnection =
                            myLocalRegistrarServer.getConnectionToRegistrar(
                                arg2, arg3, arg4, true);
                        if (tr.tracing)
                            tr.$(this + " extending search vector '" +
                                 arg4 + "'");
                        myConnectionThread.extendSearchVector(arg4);
                        if (!myRemoteRegistrarId.equals(arg2) &&
                                !myRemoteRegistrarId.equals(arg3)) {
                            startupError(TOK_ERR_ID_MISMATCH,
                                "id mismatch between IAM and INFO: " + line);
                            break;
                        }
                        myRemoteRegistrarId = arg2;
                        myRemotePublisherId = arg3;
                        myRemoteSearchPath = arg4;
                        myRemotePidCertificate = arg5;
                        if (!verifyCertificate(myRemotePidCertificate,
                                               myRemoteRegistrarId,
                                               myRemotePublisherId)) {
                            startupError(TOK_ERR_BAD_CERT2,
                                         "certificate not accepted");
                            break;
                        }

                        if (myConnection.newTransceiverLives(this,
                                amIncoming)) {
                            state = ST_EXPECT_GO_REPLY;
                            sendline("GO"); /*@BLOCK */
                        } else {
                            if (tr.tracing)
                                tr.$(this + " my side dies at first cut.");
                            startupError(TOK_NONE, "QUIT");
                        }
                    } else if (token == TOK_DUP) { /* "229 Dup. connection" */
                        if (tr.tracing)
                            tr.$(this + " remote duplicate detected.");
                        startupFailure();
                    } else {
                        startupError(TOK_ERR_PROTOCOL,
                            "Protocol error, expected INFO, got: " + line);
                    }
                    break;
                case ST_EXPECT_GO_REQ:
                    if (token == TOK_GO_STR) {
                        if (myConnection.newTransceiverLives(this,
                                amIncoming)) {
                            sendline(TOK_GO + " GO"); /*@BLOCK */
                            if (tr.tracing)
                                tr.$(this + " this transceiver lives.");
                            startupSuccess();
                        } else {
                            sendline(TOK_BYE + " BYE"); /*@BLOCK */
                            if (tr.tracing)
                                tr.$(this + " my side dies at second cut.");
                            startupFailure();
                        }
                    } else if (token == TOK_QUIT_STR) {
                        if (tr.tracing)
                            tr.$(this + " got QUIT from remote side.");
                        startupFailure();
                    } else {
                        startupError(TOK_ERR_PROTOCOL,
                            "Protocol error, expected GO, got: " + line);
                    }
                    break;
                case ST_EXPECT_GO_REPLY:
                    if (token == TOK_GO) { /* "225 GO" */
                        if (myConnection.newTransceiverLives(this,
                                amIncoming)) {
                            if (tr.tracing)
                                tr.$(this + " this transceiver lives.");
                            startupSuccess();
                        } else {
                            if (tr.tracing)
                                tr.$(this + " my side dies at second cut.");
                            startupFailure();
                        }
                    } else if (token == TOK_BYE) { /* "228 BYE" */
                        if (tr.tracing)
                            tr.$(this + " remote duplicate detected.");
                        startupFailure();
                    } else {
                        startupError(TOK_ERR_PROTOCOL,
                            "Protocol error, expected GO, got: " + line);
                    }
                    break;
                default:
                    except = "RtTransceiver startup: unknown state: " + state;
                    break;
                }
            }
        } catch (RtTransceiverStartupErrorException e) {
            String error = e.getMessage();
            sendline(error); /*@BLOCK */
            if (except == null)
                except = "Local end reported error: " + error;
        } catch (RtTransceiverStartupFailureException e) {
            myLocalSocket.close();
            return false;
        } catch (RtTransceiverStartupSuccessException e) {
            myConnection.setTransceiver(this); /*@BLOCK */
            return true;
        }
        if (except == null)
            except = "RtTransceiver: This never happens!";
        myLocalSocket.close();
        throw new RtTransceiverException(except);
    }

    /**
     * Terminate the transceiver protocol startup with failure.
     */
    private void startupFailure() throws RtTransceiverStartupFailureException {
        throw new RtTransceiverStartupFailureException();
    }

    /**
     * Terminate the transceiver protocol startup with success.
     */
    private void startupSuccess() throws RtTransceiverStartupSuccessException {
        throw new RtTransceiverStartupSuccessException();
    }

    /**
     * Terminate the transceiver protocol startup with an error.
     */
    private void startupError(int errorToken, String message)
             throws RtTransceiverStartupErrorException {
         if (errorToken != TOK_NONE)
             message = errorToken + " " + message;
         throw new RtTransceiverStartupErrorException(message);
    }

    /**
     * Send a message line to the remote end of the connection.
     */
    private void sendline(String s) throws IOException { /*@BLOCK METHOD (PRIM) */
        if (tr.tracing)
            tr.$(this + " sending:  '" + s + "'");
        myOutputStream.writeBytes(s + "\n"); /*@BLOCK PRIM */
        myOutputStream.flush(); /*@BLOCK PRIM */
    }

    /**
     * Get and parse a message line from the other end of the connection.
     *
     * @return An array of strings. The zeroth element is the unparsed line.
     *  The remainder are the parsed, space-delimited tokens found in the line.
     *  A null result is returned if we got a null line in.
     */
    private String getline()[] throws IOException { /*@BLOCK METHOD (PRIM) */
        String line = myInputStream.readLine(); /*@BLOCK PRIM */
        if (line == null) {
            if (tr.tracing)
                tr.$(this + " received: EOF");
            return null;
        }
        if (tr.tracing)
            tr.$(this + " received: '" + line + "'");
        int i = 2;
        int space = 0;
        while ((space = line.indexOf(' ', space)) >= 0) {
            i++;
            space++;
        }
        if (i < 7)
            i = 7;
        String ret[] = new String[i];
        ret[0] = line;
        i = 1;
        space = 0;
        int nextspace = 0;
        while ((nextspace = line.indexOf(' ', space)) >= 0) {
            ret[i++] = line.substring(space, nextspace);
            space = nextspace + 1;
        }
        ret[i] = line.substring(space);
        return ret;
    }

    /**
     * Test a certificate authenticating remote registrar and publisher IDs.
     *
     * @param cert The certificate
     * @param rRid The remote registrar ID
     * @param rPid The remote publisher ID
     * @return true iff verification was successful.
     */
    private boolean verifyCertificate(String cert, String rRid, String rPid) {
        /* XXX perhaps we should check something here... */
        return true;
    }

    /**
     * Read an incoming message from the (now started) transceiver. Blocks
     *  until a message is received or an error happens.
     *
     * @return A message queue entry for the message.
     */
    RtMsgQEntry getMessage() { /*@BLOCK METHOD (PRIM) */
        try {
            long requestID;
            int msgType = myInputStream.readInt(); /*@BLOCK PRIM */
            if (tr.tracing)
                tr.$(this + " Message type is " +
                     RtMsg.stringForMessage(msgType));
            if (RtMsg.validMessage(msgType) == false) {
                tr.$(this + " Message type " + msgType +
                     " is invalid, closing connection");
                throw new RtRuntimeException("Invalid message type " +
                                             msgType);
            }
            int msgSize = myInputStream.readInt(); /*@BLOCK PRIM */
            if (tr.tracing)
                tr.$(this + " Message size is " + msgSize);
            if (msgSize > 100000) {
                tr.$(this + " Message size " + msgSize +
                     " is bogusly huge, closing connection");
                throw new RtRuntimeException("Bogus huge message size " +
                                             msgSize);
            }
            byte msg[] = new byte[msgSize];

            myInputStream.readFully(msg, 0, msgSize); /*@BLOCK PRIM */
            /*if (tr.tracing)
                  traceMessage(msg, this + " Read message type " + msgType); */

            if (ptr.tracing)
                ptr.$(this + " Received message " + myInMessageCount++ +
                      " at " + System.currentTimeMillis());
            return new RtMsgQEntry(msgType, msgSize, (long)0, msg);
        } catch (Exception e) {
            tr.$(this + " Connection caught exception reading");
            myConnection.suspendConnection();
            return null;
        }
    }

    /**
     * Send a message on the (now started) transceiver.
     * @param msg The bytes of the message itself
     * @param msgType The type of message.
     * @see class ec.e.net.RtMsg for message type definitions
     */
    synchronized void reallySendBlobBytes(byte msg[], int msgType)
            throws RtConnectionFailedException { /*@BLOCK METHOD (PRIM) */
        try {
            if (tr.tracing)
                tr.$(this + " writing message type " +
                     RtMsg.stringForMessage(msgType));
            /*if (tr.tracing)
                traceMessag(msg, this + " Writing message type " + msgType); */
            if (ptr.tracing)
                ptr.$(this + " Sending message " + myOutMessageCount++ +
                      " at " + System.currentTimeMillis());
            myOutputStream.writeInt(msgType); /*@BLOCK PRIM */
            myOutputStream.writeInt(msg.length); /*@BLOCK PRIM */
            myOutputStream.write(msg); /*@BLOCK PRIM */
            myOutputStream.flush(); /*@BLOCK PRIM */
        } catch (Exception e) {
            if (tr.tracing) {
                tr.$(this + " Exception trying to send msg");
                e.printStackTrace();
            }
            myConnection.suspendConnection();
            throw new RtConnectionFailedException(this +
                " exception sending message: " + e);
        }
    }

    /**
     * Shutdown my connection to the other end. Takes care of informing our
     *  RtConnection that we're gone.
     */
    void teardown() {
        if (tr.tracing)
            tr.$("teardown(), closing socket");
        try {
            myLocalSocket.close();
        } catch (Exception e) {
            /* XXX bad exception usage? -- fix? */
            if (tr.tracing) {
                tr.$(this + " exception closing socket");
                e.printStackTrace();
            }
        }
        myConnection.setTransceiver(null);
    }

    /**
     * Debug and diagnostic method.
     */
    private void traceMessage(byte msg[], String note) {
        int i;
        char msgChars[] = new char[msg.length * 2];
        for (i = 0; i < msg.length; i++) {
            int j = i * 2;
            byte b = msg[i];

            msgChars[j] = makeCharacterForNumber((b >> 4) & 15);
            msgChars[j+1] = makeCharacterForNumber(b & 15);
        }
        String s = new String(msgChars);
        tr.$(note + " (length " + msg.length + ") " + s);
        /* tr.printStackTrace(); */
    }

    /**
     * Debug and diagnostic method.
     */
    private char makeCharacterForNumber(int n) {
        if (n > 9)
            return (char)('a' + n - 10);
        else
            return (char)('0' + n);
    }

    /**
     * Produce a pretty string representation of this transceiver object.
     */
    public String toString() {
        if (myRemoteRegistrarId == null)
            return "RtTransceiver(" + mySerialNumber + ", " +
                myLocalRegistrarId + "<->" + myRemoteAddr + ")";
        else
            return "RtTransceiver(" + mySerialNumber + ", " +
                myLocalRegistrarId + "<->" + myRemoteRegistrarId + ")";
    }
}

public class RtTransceiverException extends Exception {
    public RtTransceiverException(String message) {
        super(message);
    }
}

public class RtTransceiverStartupErrorException extends Exception {
    public RtTransceiverStartupErrorException(String message) {
        super(message);
    }
}

public class RtTransceiverStartupFailureException extends Exception {
    public RtTransceiverStartupFailureException() {
        super();
    }
}

public class RtTransceiverStartupSuccessException extends Exception {
    public RtTransceiverStartupSuccessException() {
        super();
    }
}
