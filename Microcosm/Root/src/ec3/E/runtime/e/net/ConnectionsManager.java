package ec.e.net;

import ec.e.file.EStdio;
import ec.util.CompletionNoticer;
import ec.e.start.SmashedException;
import ec.e.start.Vat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;
import java.io.EOFException;
import java.io.PrintStream;

/**
 * Manages a Registrar's collection of open connections to other machines on
 * the network. Also manages the closely related task of listening for new
 * incoming connections.
 */
final class ConnectionsManager {
    static private final Trace tr = new Trace("ec.e.net.ConnectionsManager");

    private EListener myListener;
    private Hashtable myConnections;
    private Hashtable myActiveConnections;
    private Hashtable mySuspendingConnections;
    private Registrar myRegistrar;
    private Vat myVat;
    private ListenerInterest myListenerInterest = (ListenerInterest) EUniChannel.construct(ListenerInterest.class);
    private EUniDistributor myListenerInterest_dist = EUniChannel.getDistributor(myListenerInterest);
    private long myMessageSerialNumber = 0;
    private int myActiveConnectionLimit;
    private boolean myPreparingToHibernate;
    private CompletionNoticer myHibernationNoticer;
    private boolean myCheck = false;
    private boolean killerhack;

    /*package*/ Hashtable myFastStrings;
    /*package*/ Hashtable myFastStringsInverse;
    
    private ConnectionsManager() {}
    
    /**
     * Construct a new connections manager.
     *
     * @param vat The vat we are running in
     * @param registrar The Registrar we are teamed with
     */
    ConnectionsManager(Vat vat, Registrar registrar, int activeConnectionLimit, boolean killerhack) {
        myVat = vat;
        myRegistrar = registrar;
        myConnections = new Hashtable();
        myActiveConnections = new Hashtable();
        mySuspendingConnections = new Hashtable();
        myListener = null;
        myActiveConnectionLimit = activeConnectionLimit;
        this.killerhack = killerhack;
        initFastStrings();
    }

    /**
     * Provide a connection to a remote machine, indicated by registrar ID.
     * Will open a new connection to the appropriate remote machine if
     * necessary.
     *
     * @param remoteRegistrarID The registrar ID of the desired registrar
     * @param searchPath The search path for trying to find the remote machine
     */
    EConnection connection(String remoteRegistrarID, String searchPath[]) {
        return connection(remoteRegistrarID, searchPath, false);
    }
    
    /**
     * Provide a connection to a remote machine, indicated by registrar ID.
     * Will open a new connection to the appropriate remote machine if
     * necessary.  Handles incoming connections.
     *
     * @param remoteRegistrarID The registrar ID of the desired registrar
     * @param searchPath The search path for trying to find the remote machine
     * @param incoming true if they're calling us
     */
    EConnection connection(String remoteRegistrarID, String searchPath[],
                           boolean incoming) {
        if (tr.debug && Trace.ON) {
            tr.debugm("asking for connection " + remoteRegistrarID + " incoming = " + incoming, new Exception("where?"));
        }
        EConnection connection;
        if (!myConnections.containsKey(remoteRegistrarID)) {
            if (remoteRegistrarID.equals(myRegistrar.registrarID())) {
                return null; // don't give anyone a connection to us!
            }
            if (tr.event && Trace.ON) {
                tr.eventm("starting new connection to " + remoteRegistrarID + Trace.arrayToString(searchPath, ", searchPath", "", ", ", "") + ", incoming=" + incoming);
/*
                if (tr.verbose && Trace.ON)
                    tr.verboseReportException(new Exception("where?"), "why is this connection being created?");
*/
                if (tr.event && Trace.ON)
                    tr.eventReportException(new Exception("where?"), "why is this connection being created?");
            }
            if (killerhack && !incoming) tr.errorm("outgoing connection being created on PLS!!!!!!!", new Exception("where?"));
            connection = new EConnection(myRegistrar, remoteRegistrarID,
                                         searchPath, this, incoming);
            myConnections.put(remoteRegistrarID, connection);
            if (tr.debug && Trace.ON) dumpConnections("creating connection", connection);
        } else {
            connection = (EConnection) myConnections.get(remoteRegistrarID);
        }
        if (myCheck) consistancyCheck();
        if (tr.debug && Trace.ON) tr.debugm("returning " + connection);
        return connection;
    }

    /**
     * Start listening for incoming connections over the network.
     *
     * @listenAtAddr The local address (port actually) on which to listen.
     */
    void listenForConnections(String listenAtAddr)
    throws RegistrarException {
        if (myListener == null) {
            myListener = new EListener(myVat, this, listenAtAddr, myRegistrar);
            // listening() should be called synchronously before this
            // constructor returns, at this point the real listenAddr
            // is known.  -- NO, listening() will be called in the
            // ListenThread at some later time. -emm
        } else {
            throw new RegistrarException("already listening");
        }
    }

    /**
     * Callback when we actually start listening.
     */
    void listening(String listenAddr) {
        myRegistrar.listening(listenAddr);
        myListenerInterest <- listening(listenAddr);
    }

    /**
     * Register interest in listening.
     */
    // XXX this isn't being called, but probably should be somewhere...
    public void interestedInListening(ListenerInterest interest) {
        myListenerInterest_dist <- forward(interest);
    }

    void noticeConnectionActive(EConnection conn) {
        myActiveConnections.put(conn, conn);
        mySuspendingConnections.remove(conn);
        while (myActiveConnections.size() > myActiveConnectionLimit) {
            EConnection lru = leastRecentlyUsed();
            if (tr.debug && Trace.ON) tr.debugm("too many active connections, suspending " + lru);
            try {
                lru.suspend(); // should call noticeConnectionSuspending()
            }
            catch (IOException e) {
                noticeProblem(lru, e);
                break; // avoid infinite loop if this connection won't suspend.
            }
        }
        if (tr.debug && Trace.ON) dumpConnections("active", conn);
//tr.errorm(statistics());
    }

    void noticeConnectionInactive(EConnection conn) {
//tr.errorm(statistics());
        myActiveConnections.remove(conn);
        checkReadyForHibernation();
        if (tr.debug && Trace.ON) dumpConnections("inactive", conn);
    }

    void noticeConnectionSuspending(EConnection conn) {
//tr.errorm(statistics());
        mySuspendingConnections.put(conn, conn);
        myActiveConnections.remove(conn);
        if (tr.debug && Trace.ON) dumpConnections("suspending", conn);
    }

    void noticeConnectionSuspended(EConnection conn) {
        mySuspendingConnections.remove(conn);
        checkReadyForHibernation();
        if (tr.debug && Trace.ON) dumpConnections("suspended", conn);
    }
    
    void checkReadyForHibernation() {
        if (myPreparingToHibernate && myActiveConnections.size() == 0 
                && mySuspendingConnections.size() == 0) {
            myHibernationNoticer.noticeCompletion(null);
            myPreparingToHibernate = false;
        }
    }

    void noticeConnectionShutdown(EConnection connection) {
//tr.errorm(statistics());
        String remoteRegistrarID = connection.remoteRegistrarID();
        if (myConnections.containsKey(remoteRegistrarID)) {
            myConnections.remove(remoteRegistrarID);
        }
        myActiveConnections.remove(connection);
        mySuspendingConnections.remove(connection);
        checkReadyForHibernation();
        if (tr.debug && Trace.ON) dumpConnections("shutdown", connection);
    }

    private EConnection leastRecentlyUsed() {
        EConnection ret = null;
        long lowestSerial = myMessageSerialNumber;
        Enumeration en = myActiveConnections.elements() ;
        while (en.hasMoreElements()) {
            EConnection conn = (EConnection)en.nextElement();
            if (conn.myMessageSerialNumber < lowestSerial) {
                lowestSerial = conn.myMessageSerialNumber;
                ret = conn;
            }
        }
        return ret;
    }

    /**
     * Stop listening for incoming connections over the network.
     */
    void stopListeningForConnections() throws RegistrarException {
        if (myListener == null) {
            throw new RegistrarException("not listening");
        } else {
            if (tr.debug && Trace.ON) tr.debugm("shutting down listener");
            myListener.shutdown();
            myListener = null;
        }
    }

    /**
     * Suspend all connections prior to an orderly shutdown.
     */
    void prepareToHibernate(CompletionNoticer noticer) {
        myPreparingToHibernate = true;
        myHibernationNoticer = noticer;

        if (myListener != null) {
            myListener.suspend();
        }
        Enumeration connections = myConnections.elements();
        while (connections.hasMoreElements()) {
            EConnection connection = (EConnection) connections.nextElement();
            connection.prepareToHibernate();
            try {
                connection.suspend();
            }
            catch (IOException e) {
                noticeProblem(connection, e);
            }
        }
        checkReadyForHibernation(); // in case there are no active connections.
    }

    void reviveFromHibernation() {
        if (myListener != null) {
            myListener.resume();
        }
        Enumeration connections = myConnections.elements();
        while (connections.hasMoreElements()) {
            EConnection connection = (EConnection) connections.nextElement();
            connection.reviveFromHibernation();
        }
    }

    void noticeProblem(EConnection connection, Throwable t) {
        String msg = t.getMessage();
        boolean complain = true;
        String whom;

        if (connection == null) {
            whom = "incoming connection";
        }
        else {
            whom = connection.toString();
        }
        
        // Normal ways for a connection to terminate.  Note the
        // cause, but don't complain.
        if (t instanceof EOFException) {
            complain = false;
        }
        if (t instanceof EConnectionRecordException) {
            EConnectionRecordException ev = (EConnectionRecordException)t;
            for (int i=0; i<ev.size(); i++) {
                Throwable evt = ev.getThrowable(i);
                if (evt instanceof SmashedException) {
                    complain = false; // Due to Quake revival OK
                }
            }
        }
        if (t instanceof SmashedException) {
            complain = false; // Due to Quake revival OK
        }
        if (msg != null) {
            // should be java.net.SocketException, but we can't see that in the Vat.
            if (t instanceof Exception && msg.equals("Connection reset by peer")) complain = false;
            if (t instanceof IOException && msg.equals("Broken pipe")) complain = false;
        }

        if (complain) {
            tr.errorm("Connection problem on " + whom, t);
        }
        else {
            tr.verbosem("Connection problem on " + whom, t);
        }
    }

    void updateMessageSerialNumber(EConnection conn) {
        conn.myMessageSerialNumber = myMessageSerialNumber++;
        if (tr.verbose && Trace.ON) tr.verbosem("updated " + conn + " serial number to " + conn.myMessageSerialNumber);
    }

    public boolean consistancyCheck() {
        boolean ret = true;
        EConnection conn;
        Enumeration en = myActiveConnections.keys();
        while (en.hasMoreElements()) {
            conn = (EConnection)en.nextElement();
            if (mySuspendingConnections.containsKey(conn)) {
                tr.errorm("connection in both myActiveConnections and mySuspendingConnections: " + conn);
                ret = false;
            }
            if (conn.getState() != EConnection.STATE_RUNNING) {
                tr.errorm("connection in myActiveConnections not in state RUNNING: " + conn);
                ret = false;
            }
        }
        en = mySuspendingConnections.keys();
        while (en.hasMoreElements()) {
            conn = (EConnection)en.nextElement();
            if (conn.getState() != EConnection.STATE_SUSPENDING) {
                tr.errorm("connection in mySuspendingConnections not in state SUSPENDING: " + conn);
                ret = false;
            }
        }
        return ret;
    }

    static final private String[] theState = {"???","BLD","RUN","SIP","SUS","DED"};

    /**
     * Return an enumeration which provides statistics for the current connections
     *
     * @return an Enumeration returning ConnectionData
     */

    public String statistics() {
        StringBuffer ret = new StringBuffer(500);
        Enumeration en = myConnections.elements();
        ret.append("       RemoteRegistrarID        Sta MsgOut  MsgIn BytesOut "
                    +" BytesIn MaxOut  Protocol\n");
        while (true) {
            ret.append("\n");
        if (!en.hasMoreElements()) break;
            EConnection conn = (EConnection)en.nextElement();
            ret.append(conn.remoteRegistrarID()).append(" ");
            ret.append(theState[conn.getState()]).append(" ");
            ret.append(fixLenString(conn.messagesSent, 6)).append(" ");
            ret.append(fixLenString(conn.messagesReceived, 6)).append(" ");
            ret.append(fixLenString(conn.lineBytesSent, 8)).append(" ");
            //long pct = 0;
            //if (conn.bytesSent > 0) {
            //    pct = (conn.lineBytesSent*100)/conn.bytesSent;
            //}
            //ret.append(fixLenString(pct, 3)).append("% ");
            ret.append(fixLenString(conn.lineBytesReceived, 8)).append(" ");
            //if (conn.bytesReceived > 0) {
            //  pct = (conn.lineBytesReceived*100)/conn.bytesReceived;
            //}
            //ret.append(fixLenString(pct, 3)).append("% ");
            ret.append(fixLenString(conn.maxMessageSize, 6));
            ret.append(" ").append(conn.myAgreededProtocol).append("-");
            ret.append(conn.myCypherSuite);
        }
        ret.setLength(ret.length()-1);  // Remove trailing newline
        return ret.toString();
    }

    static final private String theBlanks = "                        ";
    private String fixLenString(long val, int len) {
        String sval = String.valueOf(val);
        if (sval.length() >= len) return sval;
        int pad = len - sval.length();
        String preface = "";
        while (pad > theBlanks.length()) {
            preface += theBlanks;
        }
        preface += theBlanks.substring(0,pad);
        return preface + sval;
    }

    void dumpConnections(String type, EConnection connection) {
        StringBuffer msg = new StringBuffer(500);
        msg.append("\nConnection status change to ").append(type).append("\n");
        msg.append(connection);
        msg.append("\n\nDump of current connections:\n");
        Enumeration en = myConnections.elements();
        while (en.hasMoreElements()) {
            EConnection conn = (EConnection)en.nextElement();
            if (conn == connection) continue;
            msg.append(conn);
            if (myActiveConnections.containsKey(conn)) {
                msg.append(" ACTIVE");
            }
            if (mySuspendingConnections.containsKey(conn)) {
                msg.append(" SUSPENDING");
            }
            msg.append("\n");
        }
        msg.append("End of current connections dump\n");
        tr.debugm(msg.toString());
    }

    private static String[] theFastStrings = {
            "[Lec.e.hold.DataHolder;",
            "[Ljava.lang.Object;",
            "[Ljava.lang.String;",
            "confirmCapability$async(Lec/e/run/EObject_$_Intf;Lec/cosm/objects/eiConfirmCapability_$_Intf;)V",
            "confirmCapability$async(Z)V",  // Count=1
            "ec.cert.Certificate",
            "ec.cert.CryptoHash",
            "ec.cosm.gui.dynamics.DERegionStartupData",
            "ec.cosm.gui.dynamics.Dynamo2D",
            "ec.cosm.gui.dynamics.Dynamo3D",
            "ec.cosm.objects.FloorPlane",
            "ec.cosm.objects.Location",
            "ec.cosm.objects.MoveContext",
            "ec.cosm.objects.PresentationState",
            "ec.cosm.objects.SpaceMap",
            "ec.cosm.objects.SpatialState",
            "ec.cosm.objects.SurfaceLocation",
            "ec.cosm.objects.eConversationInputFacet_$_Proxy",
            "ec.cosm.objects.eConversationOutputFacet_$_Proxy", // Count=1
            "ec.cosm.objects.eIdentity_$_Sealer",
            "ec.cosm.objects.efCbsupCInterestFacet_$_Impl", // Count=1
            "ec.cosm.objects.efConfirmCapability_$_Proxy",
            "ec.cosm.objects.eiConfirmCapability_$_Sealer", // Count=1
            "ec.cosm.objects.efCsupCbInterestFacet_$_Impl",
            "ec.cosm.objects.efCsupCb_$_Proxy",
            "ec.cosm.objects.efRsupCb_$_Proxy",
            "ec.cosm.objects.istAvatarBodyInHand",
            "ec.cosm.objects.istBehaviorManager",
            "ec.cosm.objects.istBodyLinkToAvatar",
            "ec.cosm.objects.istCamera",
            "ec.cosm.objects.istChangeRegion",
            "ec.cosm.objects.istCloneable",
            "ec.cosm.objects.istCloner",
            "ec.cosm.objects.istCompositable",
            "ec.cosm.objects.istContainership",
            "ec.cosm.objects.istDescriber",
            "ec.cosm.objects.istDestination",
            "ec.cosm.objects.istEventable",
            "ec.cosm.objects.istInterface",
            "ec.cosm.objects.istModifier",
            "ec.cosm.objects.istPortable",
            "ec.cosm.objects.istProperty",
            "ec.cosm.objects.istPutable",
            "ec.cosm.objects.istRegionBehavior",
            "ec.cosm.objects.istRegionContainable",
            "ec.cosm.objects.istRootCompositor",
            "ec.cosm.objects.istTeleportPad",
            "ec.cosm.objects.istTexturizable",
            "ec.cosm.objects.istVerbManager",
            "ec.cosm.objects.jChangeRegionBehaviorWrapper_$_Proxy", // Count=1
            "ec.cosm.objects.jContainerInfo",
            "ec.cosm.objects.jEditableCapabilityGroup",
            "ec.cosm.objects.jIdentity",
            "ec.cosm.objects.jNotifyTransitionBehaviorWrapper_$_Proxy", // Count=1
            "ec.cosm.objects.jRootUnumTree",
            "ec.cosm.objects.jUnumBundle",
            "ec.cosm.objects.jUnumKey",
            "ec.cosm.objects.jUnumTree",    // Count=1
            "ec.cosm.objects.pkCompositableClient$kind_$_Sealer",   // Count=1
            "ec.cosm.objects.pkRegionContainableHost$kind_$_Sealer",    // Count=1
            "ec.cosm.objects.pkRootContainerClient$kind_$_Sealer",
            "ec.cosm.objects.pskAvatarBodyClient$kind_$_Proxy", // Count=1
            "ec.cosm.objects.pskAvatarBodyHost$kind_$_Proxy",   // Count=1
            "ec.cosm.objects.pskCameraClient$kind_$_Proxy", // Count=1
            "ec.cosm.objects.pskPropClient$kind_$_Proxy",   // Count=1
            "ec.cosm.objects.pskRegionClient$kind_$_Proxy", // Count=1
            "ec.cosm.objects.pskSurfaceClient$kind_$_Proxy",    // Count=1
            "ec.cosm.objects.uimAvatarBodyGeneric$ui",  // Count=1
            "ec.cosm.objects.pskAvatarBodyHost$kind_$_Proxy",
            "ec.cosm.objects.pskCameraHost$kind_$_Proxy",
            "ec.cosm.objects.pskPropHost$kind_$_Proxy",
            "ec.cosm.objects.pskRegionHost$kind_$_Proxy",
            "ec.cosm.objects.pskSurfaceHost$kind_$_Proxy",
            "ec.cosm.objects.pskTeleportPadHost$kind_$_Proxy",
            "ec.cosm.objects.uimAvatarBodyGeneric$ui",
            "ec.cosm.objects.uimPropGeneric$ui",
            "ec.cosm.objects.uimRegion$ui",
            "ec.cosm.objects.uimSimpleCamera$ui",
            "ec.cosm.objects.uimSurfaceFloor3D$ui",
            "ec.cosm.objects.uimTeleportPadGeneric$ui",
            "ec.cosm.objects.ukAddUnum$kind_$_Proxy",
            "ec.cosm.objects.ukAddUnum$kind_$_Sealer",  // Count=1
            "ec.cosm.objects.ukAvatarChangeRegion$kind_$_Sealer",
            "ec.cosm.objects.ukAvatarNotifyTransitionBehavior$kind_$_Sealer",
            "ec.cosm.objects.ukBasicContainableNotify$kind_$_Sealer",
            "ec.cosm.objects.ukCbsupC$kind_$_Proxy",    // Count=1
            "ec.cosm.objects.ukCbsupC$kind_$_Sealer",
            "ec.cosm.objects.ukConversationInput$kind_$_Sealer",    // Count=1
            "ec.cosm.objects.ukCsupCb$kind_$_Sealer",   // Count=1
            "ec.cosm.objects.ukConversationInput$kind_$_Intf",
            "ec.cosm.objects.ukSetNewParent$kind_$_Sealer",
            "ec.cosm.objects.ukfRsupCb$kind_$_Proxy",
            "ec.cosm.objects.ukTeleportPad$kind_$_Sealer",  // Count=1
            "ec.cosm.objects.uskAvatarBody$kind",
            "ec.cosm.ui.presenter.DEStartupData",
            "ec.cosm.ui.presenter.PresenterStartupData",
            "ec.cosm.ui.presenter.TeleportTransition",
            "ec.e.hold.DataHolderSteward",
            "ec.e.hold.Fulfiller",
            "ec.e.lang.jObjectFuture_$_Proxy",
            "ec.e.net.SturdyRef",
            "ec.e.net.SturdyRefFollower_$_Sealer",
            "ec.e.run.ECatchClosure_$_Proxy",
            "ec.e.run.EDistributor_$_Proxy",
            "ec.e.run.EObject_$_Sealer",    // Count=1
            "ec.e.run.EResult_$_Sealer",
            "ec.e.run.ETrue_$_Impl",    // Count=1
            "ec.e.run.EWhenClosure_$_Proxy",    // Count=1
            "ec.e.run.RtEnvelope",
            "ec.e.run.RtExceptionEnv",
            "ec.misc.graphics.Point2DInt",
            "ec.misc.graphics.Point3D",
            "ec.misc.graphics.Polygon2DFloat",
            "ec.misc.graphics.PolygonSet2DFloat",
            "ec.pl.runtime.PresenceHost_$_Sealer",
            "ec.pl.runtime.UnumReceiver_$_Sealer",
            "ec.pl.runtime.UnumRouter",
            "ec.pl.runtime.eUnumTarget_$_Proxy",
            "forward$async(Ljava/lang/Object;)V",
            "java.lang.Boolean",    // Count=1
            "java.lang.Class",
            "java.lang.Float",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Object",
            "java.lang.String",
            "java.util.Hashtable",
            "java.util.Vector",
            "moveto",
            "newOtherPresence$async(Lec/pl/runtime/Presence_$_Intf;J)V",    // Count=27
            "pClientSetMood$async(Ljava/lang/String;)V",    // Count=1
            "pGetAllTeleportPads$async(Lec/e/run/EResult_$_Intf;)V",    // Count=1
            "pHostSendUnum$async(Lec/pl/runtime/UnumReceiver_$_Intf;Ljava/lang/Object;Ljava/lang/Object;)V",
            "pRootContainableMove$async(Lec/cosm/objects/jUnumKey;Lec/cosm/objects/jUnumKey;Ljava/lang/Object;Ljava/lang/Object;)V",
            "sendToObjectID$async(Ljava/lang/String;Lec/e/run/RtEnvelope;)V",
            "uAddUnum$async(Lec/cosm/objects/jUnumTree;Ljava/lang/Object;Lec/cosm/objects/ukSetNewParent$kind_$_Intf;Lec/cosm/objects/jUnumKey;Lec/e/run/EResult_$_Intf;Lec/cosm/objects/ukCbsupCInterest$kind_$_Intf;Ljava/lang/Object;Lec/pl/runtime/UnumReceiver_$_Intf;)V", // Count=1
            "uAddedToContainer$async(Lec/cosm/objects/jIdentity;Lec/cosm/objects/jContainerInfo;)V",
            "uChangeRegion$async(Lec/cosm/objects/ukAddUnum$kind_$_Intf;Lec/cosm/ui/presenter/RegionTransition;Lec/cosm/objects/SurfaceLocation;Lec/e/run/EResult_$_Intf;)V",
            "uNotifyTransitionComplete$async(Ljava/lang/String;)V",
            "uNotifyTransitionStarted$async(Ljava/lang/String;)V",
            "uReceiveUnum$async(Lec/pl/runtime/UnumRouter;Ljava/lang/Object;Ljava/lang/Object;)V",
            "uRequestContainableMove$async(Ljava/lang/Object;Ljava/lang/Object;)V", // Count=1
            "uSendToContainedUnum$async(Lec/e/run/RtEnvelope;)V",
            "uSetNewParent$async(Ljava/lang/Object;Lec/pl/runtime/UnumReceiver_$_Intf;Ljava/lang/Object;Ljava/lang/Object;Lec/cosm/objects/ukfRsupCb$kind_$_Intf;Lec/cosm/objects/jEditableCapabilityGroup;)V",
            "uSetOutput$async(Lec/cosm/objects/ukConversationOutput$kind_$_Intf;)V",    // Count=1
            "uTeleport$async(Ljava/util/Hashtable;Lec/e/run/EResult_$_Intf;)V", // Count=1
            "upnAvatarBodyClient",
            "upnRegionClient",
            "when$async(Lec/e/run/EResult_$_Intf;)V"    // Count=1
     };




    private void initFastStrings() {
        myFastStrings = new Hashtable(theFastStrings.length);
        myFastStringsInverse = new Hashtable(theFastStrings.length);

        for (int i=0; i<theFastStrings.length; i++) {
            String s = theFastStrings[i];
            Integer index = new Integer(-(i+2));
            myFastStrings.put(s, index);
            myFastStringsInverse.put(index, s);
        }
    }
}

einterface ListenerInterest {
    listening(String listenAddr);
}
