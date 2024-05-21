package ec.e.net;

import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.NetworkConnectionError;
import ec.e.net.steward.NetworkSender;
import ec.e.net.steward.MakeDSAPublicKey;
import ec.e.net.steward.RegistrarIDGenerator;
import ec.e.net.steward.ByteConnection;
import ec.e.start.Vat;
import ec.e.file.EStdio;
import ec.util.NativeSteward;
import ec.util.HexStringUtils;
import java.io.IOException;
import java.io.EOFException;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Vector;


/**
 * Represents a message-level connection to another machine on the network.
 */
public class MsgConnection implements NetworkConnection {
    private static final Trace tr = new Trace("ec.e.net.MsgConnection");

    /*package*/ String myAgreededProtocol = ""; // Negotiated E protocol version


    private EConnection myInnerConnection;
    private MsgSender mySender;
    private MsgReceiver myReceiver;
    private String myRemoteRegistrarID;
    private Registrar myLocalRegistrar;
    private long myOutgoingSuspendID;

    static final byte OUTBOUND_ID = 0;
    static final byte INBOUND_ID  = 1;
    static final byte HANDOFF_ID  = 2;

    /*package*/ static final String theCryptoProtocols =
                    EConnection.CRYPTO_3DES_SDH_M 
                    + "," + EConnection.CRYPTO_3DES_SDH_MZIP 
                    + "," + EConnection.CRYPTO_3DES_SDH_ZIP
                    + "," + EConnection.CRYPTO_3DES_SDH
                    + "," + EConnection.CRYPTO_NONE;


 //WARNING - WARNING - WARNING
 // In order to use ECSecureRandom to calculate each signature in mySignature
 // we directly set the random signature seed.  If we ever calculate two
 // signatures with the same seed, we have blown the security of the secret
 // key.
 // I (WSF) think it will be easier to avoid this problem if mySignature
 // remains private, and signatures are only calculated one place in the code.
    private Signature mySignature = null;

    private PublicKey myHisPublicKey = null;


    private MsgConnection() {}
    
    /**
     * Simple access to the remote registrar ID.
     * RobJ 971217 to let MsgSender.send spam where a message is going.
     */
    public String remoteRegistrarID () {
      return myRemoteRegistrarID;
    }

    /**
     * Construct a new message-level connection on top of an existing, more
     * primitive connection. This is used for handling new incoming
     * connections.
     *
     * @param outerConnection The more primitive connection we're based on
     */
    public MsgConnection(NetworkConnection outerConnection, String localAddr,
                         String remoteAddr, Registrar localRegistrar) {
        myLocalRegistrar = localRegistrar;
        myState = ST_INCOMING_EXPECT_IWANT;
        myReceiver = new MsgReceiver(this, remoteAddr);
        NetworkSender outerSender =
            outerConnection.incomingSetup(this, myReceiver);
        mySender = new MsgSender(this, outerSender, remoteAddr);
        try {
            mySender.enable(); // start the setup protocol rolling
        } catch (IOException e) {
            noticeProblem(e);
        }
    }

    /**
     * Construct a new outgoing message-level connection to another machine.
     *
     * @param vat The Vat we are running in
     * @param connectTo The domain name or IP addr of the site to connect to.
     * @param innerConnection The EConnection for which we provide services
     * @param innerReceiver The EReceiver that will get incoming messages
     */
    public MsgConnection(String remoteRegistrarID, Registrar localRegistrar,
                         String connectTo, EConnection innerConnection,
                         long outgoingSuspendID) {
        myRemoteRegistrarID = remoteRegistrarID;
        myLocalRegistrar = localRegistrar;
        myInnerConnection = innerConnection;
        myOutgoingSuspendID = outgoingSuspendID;
        myReceiver = new MsgReceiver(this, connectTo);
        new ByteConnection(myLocalRegistrar.vat(), connectTo,
                          (NetworkConnection) this, (OutputStream) myReceiver);
    }

    /**
     * Return the receiver for this connection.
     */
    public OutputStream getReceiver() {
        return myReceiver;
    }

    /**
     * Callback for when we have all the information we need to actually
     * setup an incoming connection. Note that at the message-level this
     * method is not allowed, since we are the innermost NetworkConnection.
     * We must support this interface in order to be a proper NetworkConnection
     * object; however, our implementation will of it will be to gag and die.
     *
     * @param innerConnection The non-existent next higher level connection.
     * @param innerReceiver The non-existent next higher level receiver.
     * @returns Nothing
     */
    public NetworkSender incomingSetup(NetworkConnection innerConnection,
                                       OutputStream innerReceiver) {
        throw new Error(
            "MsgConnection must be innermost NetworkConnection level");
    }

    /**
      Set send line statistics
     
      @param messageLength the length of the comm protocol or E message.
      @param compressedLength the length sent on the socket.
     */
    public void updateSendCounts(int messageLength, int compressedLength) {
        if (myInnerConnection != null) {
            myInnerConnection.bytesSent += messageLength;
            myInnerConnection.lineBytesSent += compressedLength;
        }
    }

    /**
      Set send line statistics
     
      @param messageLength the length of the comm protocol or E message.
      @param compressedLength the length received on the socket.
     */
    public void updateReceivedCounts(int messageLength, int compressedLength) {
        if (myInnerConnection != null) {
            myInnerConnection.bytesReceived += messageLength;
            myInnerConnection.lineBytesReceived += compressedLength;
        }
    }

    /**
     * Callback for when an exception condition arises on this connection.
     *
     * @param problem The exception that is the problem.
     */
    public void noticeProblem(Throwable problem) {
        if (myInnerConnection != null) {
            myInnerConnection.noticeProblem(problem);
        } else {
            throw new NetworkConnectionError("problem on partially set up connection", problem);
        }
    }

    /**
     * Callback for when we have all the information we need to actually
     * setup an outgoing connection.
     *
     * @param outerSender The send stream for the next lower level connection.
     */
    public void outgoingSetup(NetworkSender outerSender, String localAddr) {
        try {
            mySender = new MsgSender(this, outerSender,
                                     "XXX need the remoteAddr here");
            mySender.enable(); // start the setup protocol rolling.
            ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
            DataOutputStream os = new DataOutputStream(baos);
            os.writeByte(Msg.PROTOCOL_VERSION);
            os.writeInt(Msg.VersionInt);        //Obsolete, for compatibility w/version 2
            for (int i=0; i<Msg.Version.length; i++) {
                os.writeUTF(Msg.Version[i]);
            }
            mySender.sendPacket(baos.toByteArray());
            sendStartupPacket(TOK_IWANT, myRemoteRegistrarID);
            myState = ST_OUTGOING_EXPECT_IAM;
        } catch (IOException e) {
            noticeProblem(e);
        }
    }

    /**
     * Check the initator's protocol version.
     *
     * @param packetBytes[] The PROTOCOL_VERSION packet.
     */

    void checkProtocolVersion(byte packetBytes[]) throws IOException {
        if (tr.debug && Trace.ON) tr.debugm(HexStringUtils.byteArrayToReadableHexString(packetBytes));
        DataInputStream packetIn = new DataInputStream(new ByteArrayInputStream(packetBytes));
        byte header = packetIn.readByte(); /* already seen, discard */
        Vector versions = new Vector(10);   // To save their supported versions in case of error
        String protocol = "";
        packetIn.readInt();     // Discard integer version.  W/version 3 we only use strings
        try {
            while (true) {
                String theirVersion = packetIn.readUTF();
                if (!"".equals(theirVersion)) { 
                    versions.addElement(theirVersion);
                    for (int i=0; i<Msg.Version.length; i++) {
                        if (theirVersion.equals(Msg.Version[i])) {
                            protocol = theirVersion;
                        }
                    }
                }
            }
        } catch(EOFException e) {   // Handle end of input string
            if (tr.debug && Trace.ON) {
                String err = "Incoming protocol versions ";
                int size = versions.size();
                for (int i=0; i <size-1; i++) {
                    err += (versions.elementAt(i) + ", ");
                }
                if (size>0) err += versions.lastElement();
                tr.debugm(err+" picked "+protocol);
            }
            if ("" == protocol) {       // Can't agree on a protocol (java interns strings)
                String err = "incoming protocol versions ";
                int size = versions.size();
                for (int i=0; i <size-1; i++) {
                    err += (versions.elementAt(i) + ", ");
                }
                if (size>0) err += versions.lastElement();
                err += " are not supported, use versions ";
                for (int i=0; i<Msg.Version.length-1; i++) {
                    err += (Msg.Version[i] + ", ");
                }
                err += Msg.Version[Msg.Version.length-1];
                sendPacket(Msg.STARTUP, TOK_ERR_PROTOCOL, err, null, null);
                throw new IOException(err);
            }
        }
        if (!protocol.equals("2")) {  // ver 2 doesn't understand PROTOCOL_ACCEPTED
            sendPacket(Msg.PROTOCOL_ACCEPTED, 0, protocol, null, null);
        }
        myAgreededProtocol = protocol;
        if (null != myInnerConnection) {
            myInnerConnection.myAgreededProtocol = protocol;
        }
        packetIn.close();
    }


    /**
     * Callback for when this connection is being shut down.
     */
    public void noticeShutdown(byte[] sendIV, byte[] receiveIV) {
        if (myInnerConnection != null) {
            if (EConnection.STATE_WAITING_FOR_CONNECT == myInnerConnection.getState()
                    || ST_EXPECT_MESSAGE == myState) {
                myInnerConnection.noticeShutdown(sendIV, receiveIV);
            } else {
                tr.errorm("innerState="+myInnerConnection.getState()
                          +" myState="+myState, new Throwable());
            }
        }
        // and if it IS null???
    }

    /**
       Startup protocol has succeeded, let the messages flow.
      */
    void enableMessageTraffic() throws IOException {
        if ("" == myAgreededProtocol) { //Java interns constant strings
            myAgreededProtocol = "2";   // Default to version 2
        }
        mySender.setInnerConnection(myInnerConnection,
                    myInnerConnection.localRegistrar().getEncodeParameters());
        myReceiver.setInnerReceiver(myInnerConnection.getReceiver(),
                    myInnerConnection.localRegistrar().getDecodeParameters());
        if (!EConnection.CRYPTO_NONE.equals(myInnerConnection.myCypherSuite)) {
            // Other end wants encrypted session
            boolean isAggragating = EConnection.CRYPTO_3DES_SDH_M.equals
                                        (myInnerConnection.myCypherSuite)
                                    || EConnection.CRYPTO_3DES_SDH_MZIP.equals
                                        (myInnerConnection.myCypherSuite);
            boolean isZipping     =  EConnection.CRYPTO_3DES_SDH_ZIP.equals
                                        (myInnerConnection.myCypherSuite)
                                    || EConnection.CRYPTO_3DES_SDH_MZIP.equals
                                        (myInnerConnection.myCypherSuite);

            mySender.changeProtocol(
                            isAggragating,
                            "SHA1",
                            myInnerConnection.myMACKey,
                            "3DES",
                            myInnerConnection.myDESKeys,
                            myInnerConnection.myOutgoingIV,
                            myInnerConnection.myIncomingIV,
                            isZipping,
                            EConnection.CRYPTO_3DES_SDH_MZIP.equals
                                (myInnerConnection.myCypherSuite));
        }
        myInnerConnection.myAgreededProtocol = myAgreededProtocol;
        if (tr.usage && Trace.ON) tr.usagem("Link to " + myRemoteRegistrarID
                    + " uses crypto " + myInnerConnection.myCypherSuite
                    + ", Emsg protocol=" + myAgreededProtocol);
        myInnerConnection.enable(mySender);
    }

    /***********************************************************************
     * Below here are variables and methods having to do with executing the
     * connection startup protocol.
     **********************************************************************/

    /*
     * The connection startup protocol (Alice is connecting to Bob):
     *
     * Alice: IWANT <bobRegistrarID>
     *
     *   Bob: IAM <bobRegistrarID>          (continue)
     *    or  NOTME                         (try next site in search path)
     *    or  TRY <possibleAlternatePath>   (add to search path, try next site)
     *
     * Alice: GIVEINFO <aliceRegistrarID> <alicesPathToAlice>
     *
     *   Bob: REPLYINFO <bobsPathToBob>     (continue)
     *    or  DUP                           (stop)
     *    or  PROBLEM                       (stop)
     *
     * Alice: GO
     *
     *   Bob: GOTOO                         (start using the message protocol)
     *    or  BYE                           (stop)
     *
     * Thence both can send each other E messages according to the message
     * protocol.
     */
    /***********************************************************************
     * The following are extensions to the above protocol to negotiate a
     * shared cryptographic state.
     ***********************************************************************
     *
     * When Bob says, "IAM" he includes his public key:
     *
     *   Bob: IAM <bobRegisterID><bobPublicKey>
     *
     * When Alice replies GIVEINFO, she includes her public key:
     *
     * Alice: GIVEINFO <aliceRegistrarID> <alicesPathToAlice> <alicePublicKey>
     *
     * When Bob replies, "REPLYINFO" we extend the protocol to allow him to say:
     *
     *   Bob: REPLYINFO <bobsPathToBob><protocols>  (continue)
     *
     * Where <protocols> is a comma separated list of crypto protocols that
     *   Bob knows, in order from most favored to least favored.
     *
     * When Alice says: GO, we extend the protocol to allow her to say:
     * Alice: GO <crypto parameters>
     *
     *   Bob: GOTOO                         (don't encrypt session)#
     *        GOTOO <crypto parameters>     (Start encrypted session)
     *
     *   # The validity of this response will be removed in a later version,
     *     requiring encrypted sessions.
     *
     * <crypto parameters> are:
     *      <version> <version specific parameters>
     *
     * The values of <version> are:
     *      <no crypto> - No crypto used. No version specific parameters.
     *      <3DES_SDH>  - Triple DES with DH key agreement.  The DH parameters
     *                    shall be signed with a DSA signiture.
     *      <3DES_SDH_ZIP> - Triple DES with DH key agreement.  The DH
     *                    parameters shall be signed with a DSA signiture.
     *                    The messages will use ZipStream compression.
     *      <CRYPTO_3DES_SDH_MZIP> - Triple DES with DH key agreement.  The DH
     *                    parameters shall be signed with a DSA signiture.
     *                    The messages will use ZipStream compression.  More
     *                    than one E message may be included a an encrypted
     *                    packet.
     *      <CRYPTO_3DES_SDH_M> - Triple DES with DH key agreement.  The DH
     *                    parameters shall be signed with a DSA signiture.
     *                    More than one E message may be included a an
     *                    encrypted packet.
     *
     * The <version specific parameters> are shipped as a comma separated
     * list of decimal values.  They are:
     *
     *  3DES_SDH -
     *      <g**x mod m>,<DSA signature on g**x mod m>
     */
    /*
     * States for the connection startup state machine.
     */
    final private static int ST_UNSTARTED                 = 0;
    final private static int ST_INCOMING_EXPECT_IWANT     = 1;
    final private static int ST_OUTGOING_EXPECT_IAM       = 2;
    final private static int ST_INCOMING_EXPECT_GIVEINFO  = 3;
    final private static int ST_OUTGOING_EXPECT_REPLYINFO = 4;
    final private static int ST_INCOMING_EXPECT_GO        = 5;
    final private static int ST_OUTGOING_EXPECT_GOTOO     = 6;
    final private static int ST_EXPECT_MESSAGE            = 7;
    final private static int ST_DEAD                      = 8;
    final private static int ST_TRY_NEXT                  = 9;

    private int myState = ST_UNSTARTED; /* Current state of state machine */

    /*
     * Tokens used in the connection startup protocol
     */
    final private static int TOK_BYE                =  1;
    final private static int TOK_DUP                =  2;
    final private static int TOK_GIVEINFO           =  3;
    final private static int TOK_GO                 =  4;
    final private static int TOK_GOTOO              =  5;
    final private static int TOK_IAM                =  6;
    final public  static int TOK_IWANT              =  7; // used by LSConnection
    final public  static int TOK_NOT_ME             =  8; // used by LSConnection
    final private static int TOK_QUIT               =  9;
    final private static int TOK_REPLYINFO          = 10;
    final public  static int TOK_TRY                = 11; // used by LSConnection
    final private static int TOK_RESUME             = 12;

    final private static String[] tokNames = {
        "TOK_BYE",
        "TOK_DUP",
        "TOK_GIVEINFO",
        "TOK_GO",
        "TOK_GOTOO",
        "TOK_IAM",
        "TOK_IWANT",
        "TOK_NOT_ME",
        "TOK_QUIT",
        "TOK_REPLYINFO",
        "TOK_TRY",
        "TOK_RESUME"
    };

    final private static int TOK_ERR_UNKNOWN_STATE  = -1;
    final public  static int TOK_ERR_PROTOCOL       = -2; // used by LSConnection
    final private static int TOK_ERR_WRONG_ID       = -3;

    final private static String[] errTokNames = {
        "TOK_ERR_UNKNOWN_STATE",
        "TOK_ERR_PROTOCOL",
        "TOK_ERR_WRONG_ID"
    };

    static String tokName(int tok) {
        if (tok < 0) {
            tok = -tok ;
            if (tok > errTokNames.length) {
                return "TOK_ERR_???[-" + tok + "]" ;
            }
            return errTokNames[tok-1] + "[-" + tok + "]" ;
        }
        if (tok == 0) {
            return "TOK_???[0]" ;
        }
        if (tok > tokNames.length) {
            return "TOK_???[" + tok + "]" ;
        }
        return tokNames[tok-1] + "[" + tok + "]" ;
    }

    /**
     * Process the next packet of the connection startup protocol.
     */
    void processStartupPacket(byte packetArray[]) {
        ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(packetArray);
        DataInputStream packetIn = new DataInputStream(byteArrayIn);
        Exception problem = null;

        try {
            try {
                byte header = packetIn.readByte(); /* already seen, discard */
                int token = packetIn.readInt();
                String arg1 = packetIn.readUTF();
                String arg2 = packetIn.readUTF();
                String arg3;
                if (0 != byteArrayIn.available()) {
                    arg3 = packetIn.readUTF();  // New style setup
                } else {
                    arg3 = "";                  // Old style setup
                }
                packetIn.close();
                String packet = tokName(token) + " " + arg1 + " " + arg2 + " " + arg3;

                if (tr.debug && Trace.ON)
                    tr.debugm("received startup packet " + packet);

                if (token < 0) {
                    startupLocalError("Error " + tokName(token)
                    + " from other side: " + arg1 + " " + arg2 + " " + arg3);
                }

                switch (myState) {
                    case ST_INCOMING_EXPECT_IWANT:
                        handleStateIncomingExpectIWANT(token, arg1, arg2, arg3, packet);
                        break;
                    case ST_OUTGOING_EXPECT_IAM:
                        handleStateOutgoingExpectIAM(token, arg1, arg2, arg3, packet);
                        break;
                    case ST_INCOMING_EXPECT_GIVEINFO:
                        handleStateIncomingExpectGIVEINFO(token, arg1, arg2, arg3, packet);
                        break;
                    case ST_OUTGOING_EXPECT_REPLYINFO:
                        handleStateOutgoingExpectREPLYINFO(token, arg1, arg2, arg3, packet);
                        break;
                    case ST_INCOMING_EXPECT_GO:
                        handleStateIncomingExpectGO(token, arg1, arg2, arg3, packet);
                        break;
                    case ST_OUTGOING_EXPECT_GOTOO:
                        handleStateOutgoingExpectGOTOO(token, arg1, arg2, arg3, packet);
                        break;
                    case ST_DEAD:
                        if (tr.debug && Trace.ON) tr.debugm("dead MsgConnection ignoring startup packet " + packet);
                        break;
                    default:
                        startupLocalError("state machine confused, in state " +
                                          myState);
                        break;
                }
            } catch (IOException e) {
                startupError(TOK_ERR_PROTOCOL,
                             "Exception handling packet: ", e);
            }
        } catch (ConnectionStartupException e) {
            try {
                if (e.problem != null) {
                    sendStartupPacket(e.errorToken,
                                      e.getMessage() + e.problem);
                    EStdio.reportException(e.problem, true);
                } else {
                    sendStartupPacket(e.errorToken, e.getMessage());
                }
            } catch (IOException e2) {
                /* Swallow exception, because if we get one here it means we
                   had a problem telling the other guy we had a problem, and
                   the normal action in response to a problem is to tell the
                   other guy that we've had a problem...
                   */
            }
            problem = e;
        } catch (ConnectionStartupLocalException e) {
            problem = e;
        }
        if (problem != null) {
            noticeProblem(problem);
            myState = ST_DEAD;
            try {
                mySender.close(new ConnectionDeadEException("Died in startup", problem));
            } catch (IOException e) {
                EStdio.reportException(e);
                // XXX I have no clue what to do here.
            }
        }
    }

    void disown() {
        myInnerConnection = null;
        myState = ST_DEAD;
    }

    /*
     * IWANT <localRegistrarID>
     */
    private void handleStateIncomingExpectIWANT(int token,
            String wantedRegistrarID, String arg2, String arg3, String packet)
    throws ConnectionStartupException, IOException {
        if (token == TOK_IWANT) { /* IWANT <id> */
            if (wantedRegistrarID.equals(myLocalRegistrar.registrarID())) {
                sendStartupPacket(TOK_IAM, wantedRegistrarID, myLocalRegistrar.myPublicKey);
                myState = ST_INCOMING_EXPECT_GIVEINFO;
            } else {
                if (tr.debug && Trace.ON) tr.$("got request for " + wantedRegistrarID + " when I am " + myLocalRegistrar.registrarID());
                startupError(TOK_NOT_ME, "I don't know " + wantedRegistrarID);
            }
        } else {
            startupError(TOK_ERR_PROTOCOL, "Expected " + tokName(TOK_IWANT) + " got " + packet);
        }
    }

    /*
     * IAM <remoteRegistrarID> <myPublicKey>
     *   -or-
     * TRY <path>
     *   -or-
     * NOTME
     */
    private void handleStateOutgoingExpectIAM(int token, String remoteRegistrarID,
            String hisKey, String arg3, String packet)
    throws ConnectionStartupException, ConnectionStartupLocalException, IOException {
        if (token == TOK_IAM) {
            if (remoteRegistrarID.equals(myRemoteRegistrarID)) {
                decodeHisPublicKey(remoteRegistrarID, hisKey);
                myState = ST_OUTGOING_EXPECT_REPLYINFO;
                sendStartupPacket(TOK_GIVEINFO,
                                  myLocalRegistrar.registrarID(),
                                  myLocalRegistrar.flattenedSearchPath(),
                                  myLocalRegistrar.myPublicKey);
            } else {
                startupError(TOK_ERR_WRONG_ID, "You're not who I asked for");
            }
        } else if (token == TOK_TRY) {
            String tryPath = remoteRegistrarID;
            myInnerConnection.extendSearchPath(tryPath);
            myState = ST_TRY_NEXT;
            startupLocalError("got " + packet);
        } else if (token == TOK_NOT_ME) {
            myState = ST_TRY_NEXT;
            startupLocalError("got " + packet);
        } else {
            startupError(TOK_ERR_PROTOCOL, "Expected " + tokName(TOK_IAM) + " got " + packet);
        }
    }

    /*
     * GIVEINFO <remoteRegistrarID> <remotePath> <hisPublicKey>
     */
    private void handleStateIncomingExpectGIVEINFO(int token, String remoteRegistrarID,
            String remoteSearchPath, String hisKey, String packet)
    throws ConnectionStartupException, IOException {
        if (token == TOK_GIVEINFO) {
            myRemoteRegistrarID = remoteRegistrarID;
            decodeHisPublicKey(remoteRegistrarID, hisKey);

            myInnerConnection = myLocalRegistrar.connectionsManager().connection(
                myRemoteRegistrarID,
                EARL.parseSearchPath(remoteSearchPath),
                true);
            myInnerConnection.extendSearchPath(remoteSearchPath);
            if (myInnerConnection.newMsgSenderLives(mySender, true)) {
                myState = ST_INCOMING_EXPECT_GO;
                String protocols = myLocalRegistrar.myCryptoProtocols;
                sendStartupPacket(TOK_REPLYINFO,
                        myLocalRegistrar.flattenedSearchPath(), protocols);
            } else {
                myInnerConnection = null;
                startupError(TOK_DUP, "Crossed connections");
            }
        } else {
            startupError(TOK_ERR_PROTOCOL, "Expected " + tokName(TOK_GIVEINFO) + " got " + packet);
        }
    }

    /*
     * REPLYINFO <remotePath>
     *   -or-
     * DUP
     */
    private void handleStateOutgoingExpectREPLYINFO(int token, String remoteSearchPath,
            String protocols, String arg3, String packet)
    throws ConnectionStartupException, ConnectionStartupLocalException, IOException {
        if (token == TOK_REPLYINFO) {
            myInnerConnection.extendSearchPath(remoteSearchPath);
            if (myInnerConnection.newMsgSenderLives(mySender, false)) {
                myState = ST_OUTGOING_EXPECT_GOTOO;
                if (myOutgoingSuspendID >= NetIdentityMaker.MIN_ID) {
                    sendStartupPacket(TOK_RESUME, Long.toString(myOutgoingSuspendID));
                    return;
                }
                for (;;) {
                    int i = protocols.indexOf(',');
                    String protocol;
                    if (i < 0) {
                        protocol = protocols;
                        protocols = "";
                    } else {
                        protocol = protocols.substring(0,i);
                        protocols = protocols.substring(i+1);
                    }
                    if (null != myLocalRegistrar.myCryptoProtocolsTable.get(
                                protocol.toUpperCase())) {
                        String dhparm = firstDH();
                        sendStartupPacket(TOK_GO,
                                          protocol,
                                          dhparm + ","
                                                 + sign(dhparm, token));
                        break;
                    } else if (EConnection.CRYPTO_NONE.equals(protocol)) {
                        sendStartupPacket(TOK_GO,
                                              EConnection.CRYPTO_NONE);
                        break;
                    } else if (0 == protocols.length()) { //No agreement
                        sendStartupPacket(TOK_GO);
                        break;
                    }
                    // XXX we probably want to abort the connection if
                    // XXX we can't agree on a crypto protocol.
                }
            }
            else {
                myInnerConnection = null;
                startupError(TOK_QUIT, "Crossed connections");
            }
        } else if (token == TOK_DUP) {
            startupLocalError("got " + packet);
        } else {
            startupError(TOK_ERR_PROTOCOL, "Expected " + tokName(TOK_REPLYINFO) + " got " + packet);
        }
    }

    /*
     * GO
     */
    private void handleStateIncomingExpectGO(int token, String arg1, String arg2,
            String arg3, String packet)
    throws ConnectionStartupException, ConnectionStartupLocalException, IOException {
        if (token == TOK_GO) {
            if (myInnerConnection.newMsgSenderLives(mySender, true)) {
                if (myInnerConnection.noticeRemoteResume(0)) {
                    if (null != myLocalRegistrar.myCryptoProtocolsTable.get(
                                arg1.toUpperCase())) {
                        // We're doing crypto and other end wants encrypted session
                        String dhparm = firstDH();
                        sendStartupPacket(TOK_GOTOO,
                                          arg1,
                                          dhparm + "," + sign(dhparm, token));
                        myInnerConnection.myCypherSuite = arg1;
                        secondDH(arg2, token);
                    } else {
                        sendStartupPacket(TOK_GOTOO);
                    }
                    myState = ST_EXPECT_MESSAGE;
                    enableMessageTraffic();
                }
                else {
                    // the old connection has been killed at this
                    // point, so we could perhaps just grab a fresh
                    // new one and try again, but it's simpler at this
                    // point to force the other side to destroy it's
                    // conneciton and retry it.  -emm
                    startupError(TOK_BYE, "discarded resumable connection");
                }
            } else {
                myInnerConnection = null;
                startupError(TOK_BYE, "Crossed connections");
            }
        } else if (token == TOK_RESUME) {
            long incomingSuspendID = Long.parseLong(arg1);
            if (myInnerConnection.noticeRemoteResume(incomingSuspendID)) {
                sendStartupPacket(TOK_GOTOO);
                myState = ST_EXPECT_MESSAGE;
                enableMessageTraffic();
            }
            else {
                startupError(TOK_BYE, "wrong suspend id");
            }
        } else if (token == TOK_QUIT) {
            startupLocalError("got " + packet);
        } else {
            startupError(TOK_ERR_PROTOCOL, "Expected " + tokName(TOK_GO) + " got " + packet);
        }
    }

    /*
     * GOTOO
     *   -or-
     * BYE
     */
    private void handleStateOutgoingExpectGOTOO(int token, String arg1, String arg2,
             String arg3, String packet)
    throws ConnectionStartupException, ConnectionStartupLocalException {
        if (token == TOK_GOTOO) {
            if (myInnerConnection.newMsgSenderLives(mySender, false)) {
                try {
                    if (null != myLocalRegistrar.myCryptoProtocolsTable.get(
                                arg1.toUpperCase())) {
                        myInnerConnection.myCypherSuite = arg1;
                        secondDH(arg2, token);
                    }
                    enableMessageTraffic();
                } catch (IOException e) {
                    noticeProblem(e);
                    startupLocalError("exception enabling mySender: " + e);
                }
                myState = ST_EXPECT_MESSAGE;
            }
            else {
                myInnerConnection = null;
                throw new RuntimeException("newMsgSender died in GOTOO");
            }
        } else if (token == TOK_BYE) {
            startupLocalError("got " + packet);
        } else {
            startupError(TOK_ERR_PROTOCOL, "Expected " + tokName(TOK_GOTOO) + " got " + packet);
        }
    }

    /**
     * Send a packet as part of the connection startup protocol.
     *
     * @param token The token at the start of the packet
     */
    private void sendStartupPacket(int token) throws IOException {
        sendPacket(Msg.STARTUP, token, null, null, null);
    }

    /**
     * Send a packet as part of the connection startup protocol.
     *
     * @param token The token at the start of the packet
     * @param arg An argument string
     */
    private void sendStartupPacket(int token, String arg) throws IOException {
        sendPacket(Msg.STARTUP, token, arg, null, null);
    }

    /**
     * Send a packet as part of the connection startup protocol.
     *
     * @param token The token at the start of the packet
     * @param arg1 First argument string
     * @param arg2 Second argument string
     */
    private void sendStartupPacket(int token, String arg1, String arg2)
        throws IOException {
            sendPacket(Msg.STARTUP, token, arg1, arg2, null);
    }

    /**
     * Send a packet as part of the connection startup protocol.
     *
     * @param token The token at the start of the packet
     * @param arg1 First argument string
     * @param arg2 Second argument string
     * @param arg3 Third argument string
     */
    private void sendStartupPacket(int token, String arg1, String arg2, String arg3)
        throws IOException {
            sendPacket(Msg.STARTUP, token, arg1, arg2, arg3);
    }

    /**
       Send a startup packet with an arbitrary packet type.

       @param packetType The packet type (from Msg.xxx) for this packet
       @param token The token at the start of the packet
       @param arg1 First argument string
       @param arg2 Second argument string
     * @param arg3 Third argument string
    */
    void sendPacket(byte packetType, int token, String arg1, String arg2, String arg3)
    throws IOException {
        if (tr.tracing)
            tr.$("sending: " + tokName(token) + ", " + arg1 + ", " + arg2 + ", " + arg3);
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(outbuf);
        msgOut.writeByte(packetType);
        msgOut.writeInt(token);
        msgOut.writeUTF((arg1==null) ? "" : arg1);
        msgOut.writeUTF((arg2==null) ? "" : arg2);
        msgOut.writeUTF((arg3==null) ? "" : arg3);
        mySender.sendPacket(outbuf);
    }


    /**
     * Terminate the connection setup protocol with an error that will get
     * passed to the remote end too.
     */
    private void startupError(int errorToken, String msg)
    throws ConnectionStartupException {
        throw new ConnectionStartupException(msg, errorToken, null);
    }

    private void startupError(int errorToken, String msg, Throwable t)
    throws ConnectionStartupException {
        throw new ConnectionStartupException(msg, errorToken, t);
    }

    /**
     * Terminate the connection setup protocol with an error.
     */
    private void startupLocalError(String msg)
             throws ConnectionStartupLocalException {
         throw new ConnectionStartupLocalException(msg);
    }

    // Data for the Diffie Hellman protocol
    BigInteger x = null;
    static final BigInteger g = new BigInteger("2");
    static final BigInteger modulus =
        new BigInteger("11973791477546250983817043765044391637751157152328012"
                        + "72278994477192940843207042535379780702841268263028"
                        + "59486033998465467188646855777933154987304015680716"
                        + "74391647223805124273032053960564348124852668624831"
                        + "01273341734490560148744399254916528366159159380290"
                        + "29782321539388697349613396698017627677439533107752"
                        + "978203");

    private String firstDH() {
        long startTime = tr.event ? NativeSteward.queryTimer() : 0;
        x = new BigInteger(256, myLocalRegistrar.mySecureRandom);
        String ans = g.modPow(x,modulus).toString();
        if (tr.event && Trace.ON) tr.eventm("FirstDiffieHellman time "
                + (NativeSteward.queryTimer() - startTime) + " microseconds");
        return ans;
    }

    private void secondDH(String dhAndSig, int token)
    throws ConnectionStartupException {
        long startTime = tr.event ? NativeSteward.queryTimer() : 0;
        int ndx = dhAndSig.indexOf(',');
        if (-1 == ndx) startupError(token, "Expected dh,sig got: " + dhAndSig);
        String dhs = dhAndSig.substring(0, ndx);
        String sig = dhAndSig.substring(ndx+1);
        checkSig(dhs, sig, token);  // Throws ConnectionStartupException on fail
        BigInteger dh = new BigInteger(dhs);
        //byte[] dhSecret = dh.modPow(x,modulus).toByteArray();
            BigInteger test = dh.modPow(x,modulus);
            byte[] dhSecret = test.toByteArray();
        // Now calculate the various keys from dhSecret
        MessageDigest md5 = null; // javac doesn't know startupError always throws
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch(NoSuchAlgorithmException e) {
            startupError(token, "Unable to build MD5", e);
        }
        // Calculate the MD5 key
        byte[] macKey = new byte[64];
        System.arraycopy(md5Hash(0x11,dhSecret,md5), 0, macKey, 0, 16);
        System.arraycopy(md5Hash(0x22,dhSecret,md5), 0, macKey, 16, 16);
        System.arraycopy(md5Hash(0x33,dhSecret,md5), 0, macKey, 32, 16);
        System.arraycopy(md5Hash(0x44,dhSecret,md5), 0, macKey, 48, 16);
        myInnerConnection.myMACKey = macKey;
        // Calculate the three DES keys
        byte[] desKey = new byte[24];
        System.arraycopy(md5Hash(0x55,dhSecret,md5), 0, desKey, 0, 16);
        System.arraycopy(md5Hash(0xaa,dhSecret,md5), 0, desKey, 16, 8);
        myInnerConnection.myDESKeys = desKey;
        byte[] ivs = md5Hash(0x99, dhSecret, md5);
        if (TOK_GO == token) { // We are receipent
            myInnerConnection.myOutgoingIV = subbytearray(ivs, 8, 8);
            myInnerConnection.myIncomingIV = subbytearray(ivs, 0, 8);
        } else {                                  // We are initiator
            myInnerConnection.myOutgoingIV = subbytearray(ivs, 0, 8);
            myInnerConnection.myIncomingIV = subbytearray(ivs, 8, 8);
        }
        if (tr.event && Trace.ON) tr.eventm("SecondDiffieHellman time "
                + (NativeSteward.queryTimer() - startTime) + " microseconds");
    }

    private byte[] md5Hash(int pad, byte[] data, MessageDigest md5) {
        byte[] mdConst = new byte[16];
        for (int i=0; i<mdConst.length; i++) mdConst[i] = (byte)pad;
        md5.reset();                    //Initialize a new hash
        md5.update(mdConst);
        return md5.digest(data);
    }

    private byte[] subbytearray(byte[] bytes, int offset, int len) {
        byte[] ans = new byte[len];
        System.arraycopy(bytes, offset, ans, 0, len);
        return ans;
    }

    static String prtbarray (byte[] in) {   //XXX testing routine
        BigInteger bi = new BigInteger(in);
        String ans = bi.toString(16);
        return ans;
    }

    private void decodeHisPublicKey(String registrarID, String hisKey)
    throws ConnectionStartupException {
        try {
            myHisPublicKey = MakeDSAPublicKey.make(
                    new BigInteger(hisKey, 36).toByteArray());
            if (!registrarID.equals(
                    RegistrarIDGenerator.calculateRegistrarID(myHisPublicKey))) {
                startupError(TOK_ERR_WRONG_ID, "Your key is not for your registrar ID");
            }
        } catch(InvalidKeyException e) {
            tr.debugm("InvalidKeyException", e);
            // if no key, leave myHisPublicKey == null
            // XXX Replace with startupError when everyone sends keys or 11/1/97
            // startupError(TOK_IAM, "You provided an invalid public key");
        } catch(NumberFormatException e) {
            tr.debugm("NumberFormatException", e);
            // if no key, leave myHisPublicKey == null
            // XXX Replace with startupError when everyone sends keys or 11/1/97
            // startupError(TOK_IAM, "You did not provide a public key");
        }
    }

    private String sign(String data, int token)
    throws ConnectionStartupException {
        long startTime = tr.event ? NativeSteward.queryTimer() : 0;
        if (null == mySignature) {
            try {
                mySignature = Signature.getInstance("DSA");
            } catch(NoSuchAlgorithmException e) {
                startupError(token, "Unable to build DSA", e);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            new DataOutputStream(baos).writeUTF(data);
        } catch(IOException e) {
            startupError(token, "IOException to a byte array???", e);
        }
        byte[] byteData = baos.toByteArray();
        try {
 //WARNING - WARNING - WARNING
 // In order to use ECSecureRandom to calculate each signature in mySignature
 // we directly set the random signature seed.  If we ever calculate two
 // signatures with the same seed, we have blown the security of the secret
 // key.
 // I (WSF) think it will be easier to avoid this problem if mySignature
 // remains private, and signatures are only calculated one place in the code.
            PrivateKey pk = myLocalRegistrar.myKeyPair.getPrivate();
            BigInteger q = ((java.security.interfaces.DSAPrivateKey)pk).getParams().getQ();
            while (true) {
                BigInteger rn = new BigInteger(160, myLocalRegistrar.mySecureRandom);
                if ((rn.signum() > 0) && (rn.compareTo(q) < 0)) {
                    mySignature.setParameter("KSEED", rn.toByteArray());
                    break;
                }
            }
            mySignature.initSign(pk);
        } catch(InvalidKeyException e) {
            startupError(token, "Invalid private key???", e);
        }
        byte[] signature = null;
        try {
            mySignature.update(byteData);
            signature = mySignature.sign();
        } catch(SignatureException e) {
            startupError(token, "Unable to sign???", e);
        }
        String ans = new BigInteger(signature).toString(36);
        if (tr.event && Trace.ON) tr.eventm("Signing time "
                + (NativeSteward.queryTimer() - startTime) + " microseconds");
        return ans;
    }

    private void checkSig(String data, String sig, int token)
    throws ConnectionStartupException {
        if ("xxx".equals(sig)) return;  //Old style crypto
        //XXX Remove above when everone uses signatures or before 11/1/97
        if (null == mySignature) {
            try {
                mySignature = Signature.getInstance("DSA");
            } catch(NoSuchAlgorithmException e) {
                startupError(token, "Unable to build DSA", e);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            new DataOutputStream(baos).writeUTF(data);
        } catch(IOException e) {
            startupError(token, "IOException to a byte array???", e);
        }
        byte[] byteData = baos.toByteArray();
        if (null == myHisPublicKey) {
            startupError(token, "No public key from other end");
        }
        try {
            mySignature.initVerify(myHisPublicKey);
        } catch(InvalidKeyException e) {
            startupError(token, "Invalid His Public Key", e);
        }
        byte[] signature = new BigInteger(sig, 36).toByteArray();
        try {
            mySignature.update(byteData);
            if (!mySignature.verify(signature)) {
                startupError(token, "Invalid signature");
            }
        } catch(SignatureException e) {
            startupError(token, "Unable to sign???", e);
        }
        tr.eventm("Signature checked as valid");
        return;
    }

}

public class ConnectionStartupLocalException extends Exception {
    ConnectionStartupLocalException(String msg) {
        super(msg);
    }
}

public class ConnectionStartupException extends Exception {
    int errorToken;
    Throwable problem;

    ConnectionStartupException(String msg, int errToken, Throwable t) {
        super(msg);
        errorToken = errToken;
        problem = t;
    }
}
