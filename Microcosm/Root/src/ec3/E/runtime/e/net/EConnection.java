package ec.e.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.start.SmashedException;
import ec.util.assertion.Assertion;
import ec.util.NestedException;
import ec.util.NestedExceptionVector;
import ec.util.CompletionNoticer;
import ec.e.file.EStdio;
import ec.e.net.steward.Proxy;

/**
 * Represents a E-level connection to another machine on the network.
 *
 * Note that at this level we cease to be a NetworkConnection class!
 */
public class EConnection {
    static private final Trace tr = new Trace("ec.e.net.EConnection");

    // RobJ 971215 for handoff debugging
    static private final Trace trHandoff = new Trace("ec.e.net.EConnection.handoff");

    private int myState;
    static final int STATE_WAITING_FOR_CONNECT = 1 ;
    static final int STATE_RUNNING = 2 ;
    static final int STATE_SUSPENDING = 3 ;
    static final int STATE_SUSPENDED = 4 ;
    static final int STATE_DEAD = 5 ;

    private ConnectionsManager myConnectionsManager;
    private Hashtable myHandoffProxyTable = null;
    private Registrar myLocalRegistrar;
    private EReceiver myReceiver;
    private String myRemoteRegistrarID;
    private DynamicCollection mySearchCollection;
    private EConnectionRecordException myProblemAccumulator;
    private Throwable myDeathCause;
    private CompletionNoticer myShutdownNoticer;

    // XXX handling of this is not quite right.  should be whole array
    // with 0th element being where we last found them.
    private String myRemoteSearchPath;

    private ESender mySender;

    private MsgSender myNewMsgSender;

    private DynamicCollectionEnumeration mySiteSearch;
    private ImportExportTables myTables;
    /* package */ long myMessageSerialNumber;

    private Vector myProxyInterests;

    // The following fields contain the resume state for the connection
    private long myIncomingSuspendID;
    private long myOutgoingSuspendID;

    /*
     * Values for crypto parameters version:
     */
    final /*package*/ static String CRYPTO_NONE          = "None";
    final /*package*/ static String CRYPTO_3DES_SDH      = "3DES_SDH";
    final /*package*/ static String CRYPTO_3DES_SDH_ZIP  = "3DES_SDH_ZIP";
    final /*package*/ static String CRYPTO_3DES_SDH_MZIP = "3DES_SDH_MZIP";
    final /*package*/ static String CRYPTO_3DES_SDH_M    = "3DES_SDH_M";
    /*package*/ String myCypherSuite = CRYPTO_NONE;
    /*package*/ String myAgreededProtocol = "2";

    /*package*/ byte[] myMACKey;
    /*package*/ byte[] myDESKeys;

    // The following two fields are used by RecvThread and SendThread 
    // respectivly.  When the connection shuts down, these copies are updated
    // so the connection can be resumed from suspension
    /*package*/ byte[] myIncomingIV;    // Note that these are modified
    /*package*/ byte[] myOutgoingIV;    //  with each message received/sent

    // The following fields contain performance counters
    /*package*/ long bytesSent = 0;     // Uncompressed bytes
    /*package*/ long bytesReceived = 0;     // Uncompressed bytes
    /*package*/ long lineBytesSent = 0;     // Compressed bytes
    /*package*/ long lineBytesReceived = 0;     // Compressed bytes
    /*package*/ long messagesSent = 0;
    /*package*/ long messagesReceived = 0;
    /*package*/ int maxMessageSize = 0;


    private EConnection() {}

    /**
     * Construct a new E-level connection to another machine.
     *
     * @param localRegistrar Our local registrar
     * @param remoteRegistrarID The registrar ID of the process to connect to.
     * @param searchPath The search path for trying to find the remote machine.
     * @param connectionsManager where to get other connections from (for
     *  handoffs)
     * @param incoming are they calling us?
     */
    EConnection(Registrar localRegistrar, String remoteRegistrarID,
                String searchPath[], ConnectionsManager connectionsManager, boolean incoming) {
        if (localRegistrar == null || connectionsManager == null) {
            throw new SecurityException("need real Registrar/ConnectionsManager to create EConnection");
        }
        if (tr.debug && Trace.ON)
            tr.debugm("new EConnection; RID=" + remoteRegistrarID + ", incoming=" + incoming);
        myReceiver = new EReceiver(this, connectionsManager);
        mySender = new ESender(remoteRegistrarID, this, connectionsManager);
        myConnectionsManager = connectionsManager;
        myLocalRegistrar = localRegistrar;
        myRemoteRegistrarID = remoteRegistrarID;
        mySearchCollection = new DynamicCollection(searchPath);
        myTables = new ImportExportTables(this);
        startConnectionAttempts(incoming);
        connectionsManager.updateMessageSerialNumber(this);
    }

    private void startConnectionAttempts(boolean incoming) {
        mySiteSearch = mySearchCollection.elems();
        myState = STATE_WAITING_FOR_CONNECT;
        if (mySiteSearch.hasMoreElements()) {
            String site = (String) mySiteSearch.nextElement();
            myRemoteSearchPath = site;
            if (!incoming) {
                if (tr.debug && Trace.ON)
                    tr.debugm("looking for " + myRemoteRegistrarID + " at ipaddr " + site);
                new MsgConnection(myRemoteRegistrarID, 
                                  myLocalRegistrar, site,
                                  this, myOutgoingSuspendID);
            }
        } else {
            noticeProblem(new EConnectionAttemptFailed("empty search path"));
        }
    }

    /**
       get the lock object for this vat.  used by proxies to lock the
       vat in finalize().
    */
    public Object vatLock() {
        return myLocalRegistrar.vat().vatLock();
    }

    /**
       A MsgConnection for this EConnection has succeeded at the
       startup protocol.  We can now start using it for messages.

       @param outerSender where to send those messages.
      */
    void enable(MsgSender outerSender) throws IOException {
        Assertion.test(myNewMsgSender == outerSender,
                "enable(", outerSender, ") with myNewMsgSender=", myNewMsgSender, ", on ", this);
        myNewMsgSender = null; // just keep this around during GO-GOTOO negotiation
        mySender.enable(outerSender);
        myProblemAccumulator = null;
        myState = STATE_RUNNING ;
        myOutgoingSuspendID = 0 ;
        myIncomingSuspendID = 0 ;
        myConnectionsManager.updateMessageSerialNumber(this);   // Try to avoid immediate suspension
        myConnectionsManager.noticeConnectionActive(this);
    }

    boolean newMsgSenderLives(MsgSender outerSender, boolean incoming) {
        boolean ret;
        if (tr.debug && Trace.ON) tr.debugm("newMsgSenderLives(" + outerSender + ", " + incoming + ") on " + this);
        if (STATE_RUNNING == myState) {
            if (tr.debug && Trace.ON) tr.debugm("newMsgSenderLives(" 
                            + outerSender + ") returning false");
            return false;       // Kill ghosts of continuing connection attempts
        }
        if (myNewMsgSender == null) {
            if (tr.debug && Trace.ON) tr.debugm("newMsgSenderLives initial setting " + outerSender);
            myNewMsgSender = outerSender ;
            return true;
        }
        if (myNewMsgSender == outerSender) {
            if (tr.debug && Trace.ON) tr.debugm("newMsgSenderLives identical setting " + outerSender);
            return true;
        }
        // decide which MsgSender to nuke.
        ret = (incoming == (0 < myRemoteRegistrarID.compareTo(myLocalRegistrar.registrarID()) ));
        //XXX----------------------
        //tr.errorm("newMsgSenderLives called with differing arguments on " + this);
        //tr.errorm("incoming = " + incoming + " myRemoteRegistrarID = " + myRemoteRegistrarID + " hash = " + myRemoteRegistrarID.hashCode());
        //tr.errorm("myLocalRegistrar = " + myLocalRegistrar + " rid = " + myLocalRegistrar.registrarID() + " hash = " + myLocalRegistrar.registrarID().hashCode());
        //tr.errorm("myNewMsgSender = " + myNewMsgSender + " outerSender = " + outerSender);
        //tr.errorReportException(new Error("where?"), "where");
        //XXX----------------------
        if (tr.debug && Trace.ON) tr.debugm("newMsgSenderLives(" + outerSender + ") returning " + ret);
        if (ret) {
            myNewMsgSender.disown();
            myNewMsgSender = outerSender;
        }
        return ret;
    }

    /**
     * Private method to handle when the connection goes away
     */
    private void noticeConnectionDeath () {
        // Go through the list of Objects interested in Proxy death
        if (myProxyInterests != null) {
            synchronized(myProxyInterests) {
                Enumeration interests = myProxyInterests.elements();
                while (interests.hasMoreElements()) {
                    ProxyInterest interest = (ProxyInterest)interests.nextElement();
                    interest.noteProxyDeath();
                }
            }
        }
    }

    /**
       Our MsgConnection has been shut down.  Check to see if we need
       to continue to the next element of our search path.
    */
    void noticeShutdown(byte[] sendIV, byte[] receiveIV) {
        if (myState == STATE_SUSPENDING) {
            if (tr.debug && Trace.ON) tr.debugm("noticeShutdown on connection being suspended: " + this);
            myState = STATE_SUSPENDED;
            myConnectionsManager.noticeConnectionSuspended(this);
            saveIV(sendIV,receiveIV);
        }
        else if (myState == STATE_SUSPENDED) {
            if (tr.debug && Trace.ON) tr.debugm("noticeShutdown on suspended connection: " + this);
            saveIV(sendIV,receiveIV);
        }
        else if (myState == STATE_DEAD) {
            if (tr.debug && Trace.ON) tr.debugm("noticeShutdown on dead connection: " + this);
        }
        else if (myState == STATE_RUNNING) {
            if (tr.debug && Trace.ON) tr.debugm("noticeShutdown on running connection: " + this);
            if (tr.verbose && Trace.ON) tr.verbosem("Locator: ", new Throwable());
            if (myDeathCause == null) {
                myDeathCause = new Error("connection shutdown with no problem indicated");
            }
            killConnection(myDeathCause);
        }
        else if (myState == STATE_WAITING_FOR_CONNECT) {
            if (tr.debug && Trace.ON) tr.debugm("noticeShutdown on connection attempt: " + this);
            if (mySiteSearch.hasMoreElements()) {
                String site = (String) mySiteSearch.nextElement();
                myRemoteSearchPath = site;
                if (tr.debug && Trace.ON)
                    tr.debugm("continuing search for " + myRemoteRegistrarID +
                         " at ipaddr " + site);
                new MsgConnection(myRemoteRegistrarID, myLocalRegistrar, site,
                                  this, myOutgoingSuspendID);
            } else {
                myRemoteSearchPath = null;
                noticeProblem(new EConnectionAttemptFailed("search path exhausted"));
                killConnection(myDeathCause);
            }
        } else { // myState == confused
            throw new RuntimeException("EConnection in confused state: " + myState);
        }
        myConnectionsManager.noticeConnectionInactive(this);
        if (myShutdownNoticer != null) {
            myShutdownNoticer.noticeCompletion(this);
            if (tr.debug && Trace.ON) tr.debugm(myShutdownNoticer.toString());
            myShutdownNoticer = null;
        }
        if (myState == STATE_SUSPENDED && mySender.haveQueuedMessages()) {
            resume();
        }
    }

    private void saveIV(byte[] sendIV, byte[] receiveIV) {
        if (null != sendIV) {
            myOutgoingIV = sendIV;
        }
        if (null != receiveIV) {
            myIncomingIV = receiveIV;
        }
    }

    void killConnection(Throwable cause) {
        try {
            mySender.disable(cause);
        } catch (IOException e) {
            if (e instanceof SmashedException) {
            } else {
                tr.errorm("Exception disabling already shutdown connection: " + this + ": " + e);
                if (tr.verbose && Trace.ON) tr.verbosem("Exception disabling already shutdown connection: " + this, e);
            }
        }
        noticeConnectionDeath();
        myState = STATE_DEAD;
        myConnectionsManager.noticeConnectionShutdown(this);
    }

    void shutdownConnection(CompletionNoticer noticer) {
        if (myState == STATE_DEAD) {
            if (noticer != null) {
                noticer.noticeCompletion(this);
            }
        }
        else {
            myShutdownNoticer = noticer;
            myOutgoingSuspendID = 1;
            try {
                mySender.sendSuspend(myOutgoingSuspendID);
            }
            catch (IOException e) {
                throw new NestedException("problem sending shutdown packet", e);
            }
        }
    }

    public void unregisterUniquelyExportedObject(Object obj)  {
        Integer id;
        id = (Integer)myTables.uniqueExports().remove(obj);
        if (id != null)  {
            myTables.uniqueExportsByID().remove(id);
        }
        int idnum = -(id.intValue());
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(outbuf);
        try {
            msgOut.writeByte(Msg.UNREGISTER_UNIQUE);
            msgOut.writeInt(idnum);
            mySender.sendPacket(outbuf);
        } catch (Exception e) {
            // @@@ XXX some kind of error
        }
    }

    public void unregisterExportedObject (long identity)  {
        unregisterExportedObject(identity, true);
    }

    private void unregisterExportedObject (long identity, boolean tellOtherSide)  {
        myTables.exports().unregister(identity);
        if (tr.debug && Trace.ON) tr.debugm("UnregisterExportedObject for id " + identity + ", tell other side " + tellOtherSide);
        if (tellOtherSide)  {
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(outbuf);
            try {
                msgOut.writeByte(Msg.UNREGISTER_IMPORT);
                msgOut.writeLong(identity);
                mySender.sendPacket(outbuf);
            } catch (Exception e) {
                // @@@ XXX some kind of error
            }
        }
    }

    private void unregisterExportedObject(byte packetBytes[])  {
        long identity;
        DataInputStream msgIn =
            new DataInputStream(new ByteArrayInputStream(packetBytes));
        try {
            byte header = msgIn.readByte(); /* already seen, discard */
            identity = msgIn.readLong();
        } catch (Exception e) {
            if (tr.debug && Trace.ON)  {
                EStdio.reportException(e);
            }
            return;
        }
        if (tr.debug && Trace.ON) tr.debugm("UnregisterExportedObject received from other side for id " + identity);
        unregisterExportedObject(identity, false);
    }

    private void unregisterUniquelyImportedObject(byte packetBytes[])  {
        int idnum;
        Object obj;
        DataInputStream msgIn =
            new DataInputStream(new ByteArrayInputStream(packetBytes));
        try {
            byte header = msgIn.readByte(); /* already seen, discard */
            idnum = msgIn.readInt();
        } catch (Exception e) {
            if (tr.debug && Trace.ON)  {
                EStdio.reportException(e);
            }
            return;
        }

        obj = myTables.uniqueImportsByID().remove(new Integer(idnum));
        if (obj != null)  {
            myTables.uniqueImports().remove(obj);
        }
    }

    private void unregisterImportedObject(byte packetBytes[])  {
        long identity;
        DataInputStream msgIn =
            new DataInputStream(new ByteArrayInputStream(packetBytes));
        try {
            byte header = msgIn.readByte(); /* already seen, discard */
            identity = msgIn.readLong();
        } catch (Exception e) {
            if (tr.debug && Trace.ON)  {
                EStdio.reportException(e);
            }
            return;
        }
        if (tr.debug && Trace.ON) tr.debugm("UnregisterImportedObject received from other side for id " + identity);
        unregisterImportedObject(identity, true);
    }

    public void unregisterImportedObject (EObject proxy)  {
        Proxy actual = Proxy.getProxyTarget(proxy);
        if (actual == null) {
            if (tr.error) {
                tr.errorm("Ignoring non-proxy: " + proxy);
            }
            return;
        }
        long identity = actual.getIdForConnection(this);
        if (tr.debug && Trace.ON) tr.debugm("UnregisterImportedObject for Proxy: " + actual + ", for id " + identity);
        unregisterImportedObject(identity, false);
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(outbuf);
        try {
            msgOut.writeByte(Msg.UNREGISTER_EXPORT);
            msgOut.writeLong(identity);
            mySender.sendPacket(outbuf);
        } catch (Exception e) {
            // @@@ XXX some kind of error
        }
    }

    private void unregisterImportedObject (long identity, boolean notifyInterests)  {
        Vector interests = null;
        if (tr.debug && Trace.ON) tr.debugm("UnregisterImportedObject for id " + identity + ", notifyInterests " + notifyInterests);
        Object proxy = myTables.imports().get(identity);
        myTables.imports().unregister(identity);
        if ((proxy != null) && (myProxyInterests != null))  {
            synchronized(myProxyInterests)  {
                for (int i = 0; i < myProxyInterests.size();) {
                    ProxyInterest interest = (ProxyInterest)myProxyInterests.elementAt(i);
                    if (interest.isInterestForProxy(proxy))  {
                        if (tr.debug && Trace.ON) tr.debugm("Found interest " + interest + ", for id " + identity);
                        if (notifyInterests)  {
                            if (interests == null)  {
                                interests = new Vector();
                            }
                            interests.addElement(interest);
                        }
                        myProxyInterests.removeElementAt(i);
                    }
                    else {
                        i++;
                    }
                }
            }
            if (interests != null)  {
                int size = interests.size();
                for (int i = 0; i < size; i++) {
                    ProxyInterest interest = (ProxyInterest)interests.elementAt(i);
                    if (tr.debug && Trace.ON) tr.debugm("Notifying interest " + interest);
                    interest.noteProxyDeath();
                }
            }
        }
    }

    /**
     * Registers the interest in the Proxy
     */
    public void registerInterestInProxy (ProxyInterest interest, long identity) {
        // If we're already dead, indicate it
        if (myState == STATE_DEAD) {
            interest.noteProxyDeath();
        }
        // If Proxy no longer imported (because Object on other side
        // revoked it's export to us), indicate it. Note the unexpected
        // behavior of registering an interest in some random made up
        // unrelated Proxy - it's dead, Jim
        Object proxy = myTables.imports().get(identity);
        if (proxy == null)  {
            interest.noteProxyDeath();
        }
        // If we're still alive, add to notifyee list
        else {
            if (myProxyInterests == null) {
                myProxyInterests = new Vector();
            }
            synchronized(myProxyInterests) {
                myProxyInterests.addElement(interest);
            }
        }
    }

    /**
     * Unregisters the interest in the Proxy
     */
    public void unregisterInterestInProxy (ProxyInterest interest) {
        if (myProxyInterests != null) {
            synchronized(myProxyInterests) {
                myProxyInterests.removeElement(interest);
            }
        }
    }

    /**
       The outgoing MsgConnection has reached a location server.
       Remember it's response so we'll try to connect to those
       locations.

       @param tryPath "places;to;try"
      */
    void extendSearchPath(String tryPath) {
        mySiteSearch.addElems(EARL.parseSearchPath(tryPath));
    }

    /**
       Reveal our EReceiver to the MsgConnection once the startup
       protocol has succeeded.
    */
    EReceiver getReceiver() {
        return myReceiver;
    }

    /**
     * Indicate to the remote machine that one of its objects is suspected to
     * be garbage here for purposes of DGC.
     *
     * @param id The import id number of the object
     * @param referenceCount Its reference count;
     */
    public void dgcSuspectTrash(long id, int referenceCount) {
        /* XXX Public only for benefit of ec.e.net.steward.Proxy */
        myTables.imports().gotFinalized(id);
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(outbuf);
        try {
            msgOut.writeByte(Msg.SUSPECT_TRASH);
            msgOut.writeLong(id);
            msgOut.writeInt(referenceCount);
            mySender.sendPacket(outbuf);
        } catch (Exception e) {
            // @@@ XXX some kind of error
        }
    }

    /**
     * Indicate to the remote machine that it can remove its reference to one
     * of our exported objects (for purposes of DGC).
     *
     * @param id The export id number of the object.
     */
    public void dgcWRemoveMe(long id) {
        /* XXX Public only for benefit of ec.e.net.steward.Proxy */
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(outbuf);
        myTables.exports().remove(id); /* I am sure it is dereferenced */
        try {
            msgOut.writeByte(Msg.W_REMOVE_ME);
            msgOut.writeLong(id);
            mySender.sendPacket(outbuf);
        } catch (Exception e) {
            // @@@ XXX some kind of error
        }
    }

    /**
     * Produce a proxy to a proxy so that we can hand a reference to an
     * object we got from one connection to a different connection.
     *
     * MsgReceiver calls this on recipient side of a handoff to setup the
     * proxy. If there is no connection in place, we establish the connection.
     * If there is a connection, but the object isn't imported yet, we tell the
     * originator on the other side to export it for handoff.
     *
     * @param proxyID The import id number of the object on this connection
     * @param theClass The object's class
     * @param originatorRegistrarID The registrar ID of the process that
     *  originally created the object
     * @param originatorPath The search path to get to that process
     * @param senderRegistrarID The registrar ID of the process to export it
     *  from
     */
    Object getProxyForHandoff(long proxyID, Class theClass,
                              String originatorRegistrarID,
                              String originatorPath,
                              String senderRegistrarID) {
                              
        // handoff spamola
        if (trHandoff.debug && Trace.ON) trHandoff.debugm("Getting proxy for handoff, class "+theClass.getName()+", originator "+originatorRegistrarID+", sender "
          +senderRegistrarID+"\n THE SEALER IS "+MsgReceiver.getCurrentlyReceivedSealer());
                              
        String originatorPathArray[] = new String[1];
        originatorPathArray[0] = originatorPath;
        EConnection sideConnection =
            myConnectionsManager.connection(originatorRegistrarID,
                                            originatorPathArray);
        ImportExportTables sideTables = sideConnection.importExportTables();
        Object object = sideTables.imports().get(proxyID, false);
        if (object != null) {
            return object;
        } else {
            Proxy proxy = sideTables.imports().registerProxy(
                theClass, proxyID);
            addProxyToProxyToHandoffTable(proxyID, proxy);
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(outbuf);
            try {
                msgOut.writeByte(Msg.EXPORT_OBJECT_REQUEST);
                msgOut.writeLong(proxyID);
                msgOut.writeUTF(senderRegistrarID);

                // this packet needs to be queued if the connection
                // has just been established.
                sideConnection.sender().sendPacket(outbuf);
            } catch (Exception e) {
                // XXX @@@ what exceptions can get us here?
                // let's find out.
                if (tr.error) EStdio.reportException(e);
                removeHandoffProxyToProxy(proxyID);
                return null;
            }
            return proxy.getPrimeDeflector();
        }
    }

    /**
     * Return the import/export tables for this connection.
     */
    ImportExportTables importExportTables() {
        return myTables;
    }

    /**
     * Return a reference to the registrar at this end of the connection.
     */
    Registrar localRegistrar() {
        return myLocalRegistrar;
    }

    /**
     * Callback for when an exception condition arises on this connection.
     *
     * @param problem The exception that is the problem.
     */
    void noticeProblem(Throwable problem) {
        if (myState == STATE_RUNNING) {
            myDeathCause = problem;

            myConnectionsManager.noticeProblem(this, problem);
        }
        else if (myState == STATE_DEAD) {
            throw new NestedException("noticeProblem on dead connection", problem);
        }
        else if (myState == STATE_SUSPENDING) {
            myConnectionsManager.noticeProblem(this, problem);
        }
        else if (myState == STATE_SUSPENDED) {
            myConnectionsManager.noticeProblem(this, problem);
        }
        else { // myState == STATE_WAITING_FOR_CONNECT
            if (myProblemAccumulator == null) {
                myProblemAccumulator = new EConnectionRecordException(myRemoteRegistrarID);
            }
            if (tr.debug && Trace.ON) tr.debugm("accumulating problem report: " + this + ": ", problem);
            if (tr.verbose && Trace.ON) EStdio.reportException(problem, true);
            myProblemAccumulator.addThrowable(myRemoteSearchPath, problem);
            if (problem instanceof EConnectionAttemptFailed) {
                myConnectionsManager.noticeProblem(this, myProblemAccumulator);
                myDeathCause = myProblemAccumulator;
                myProblemAccumulator = null;
                try {
                    mySender.disable(myDeathCause);
                }
                catch (IOException e) {
                    if (tr.debug && Trace.ON) tr.debugm("exception disabling mySender: " + e);
                    if (tr.verbose && Trace.ON) EStdio.reportException(e);
                }
            }
        }
    }

    /**
     * Callback for when we have all the information we need to actually
     * setup an outgoing connection. At this point we initiate the connection
     * startup protocol.
     *
     * @param outerSender The MsgSender for this connection
     */
    void outgoingSetup(MsgSender outerSender) {
        throw new Error("EConnection.outgoingSetup should not be called");
        /*
        try {
            mySender.enable(outerSender);
        } catch (IOException e) {
            // XXX probably need smarter recovery here
            noticeProblem(e);
        }
        */
    }

    /**
     * Process one of the low-level (i.e., non-E) message that we are
     *  responsible for.
     *
     * @param packetBytes The packet containing the message
     */
    void processPacket(byte packetBytes[]) {
        switch (packetBytes[0]) {
            case Msg.EXPORT_OBJECT_REQUEST:
                processExportObjectRequest(packetBytes);
                break;
            case Msg.EXPORT_OBJECT_REPLY:
                processExportObjectReply(packetBytes);
                break;
            case Msg.SUSPECT_TRASH:
                processSuspectTrash(packetBytes);
                break;
            case Msg.W_REMOVE_ME:
                processWRemoveMe(packetBytes);
                break;
            case Msg.SUSPEND:
                noticeRemoteSuspend(packetBytes);
                break;
            case Msg.UNREGISTER_IMPORT:
                unregisterImportedObject(packetBytes);
                break;
            case Msg.UNREGISTER_UNIQUE:
                unregisterUniquelyImportedObject(packetBytes);
                break;
            case Msg.UNREGISTER_EXPORT:
                unregisterExportedObject(packetBytes);
                break;
            default:
                // XXX might want a better exception here.
                throw new Error("protocol error:  unsupported packet type: " +
                                packetBytes[0]);
        }
    }

    /**
     * Return a reference to the registrar at the other end of this connection.
     */
    SturdyRefFollower remoteSturdyRefFollower() {
        return myTables.remoteSturdyRefFollower();
    }

    /**
     * Return the ID of the registrar at the other end of this connection.
     */
    String remoteRegistrarID() {
        return myRemoteRegistrarID;
    }

    /**
     * Return the path to the other end of this connection.
     */
    String remoteSearchPath() {
        return myRemoteSearchPath;
    }

    /**
     * Restart a suspended connection.
     */
    void resume() {
        if (myState == STATE_SUSPENDED) {
            startConnectionAttempts(false);
        }
    }

    /**
     * Return the sender for this connection
     */
    public ESender sender() {
        /* XXX Public only for benefit of ec.e.net.steward.Proxy */
        return mySender;
    }

    void prepareToHibernate() {
        mySender.prepareToHibernate();
    }

    void reviveFromHibernation() {
        mySender.reviveFromHibernation();
        if (tr.debug && Trace.ON) tr.debugm("running consistancyCheck on " + this);
        myTables.imports().consistancyCheck();
    }

    /**
     * Suspend connection prior to an orderly shutdown.
     */
    void suspend() throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("suspending connection " + this);
        if (myState == STATE_SUSPENDED) {
            myConnectionsManager.noticeConnectionInactive(this);
            return;
        }
        myState = STATE_SUSPENDING ;
        if (myOutgoingSuspendID == 0) {
            myOutgoingSuspendID = NetIdentityMaker.nextIdentity();
            mySender.sendSuspend(myOutgoingSuspendID);
        }
        myConnectionsManager.noticeConnectionSuspending(this);
    }

    private void noticeRemoteSuspend(byte packet[]) {
        DataInputStream packetIn =
            new DataInputStream(new ByteArrayInputStream(packet));
        try {
            byte header = packetIn.readByte(); /* already seen, discard */
            myIncomingSuspendID = packetIn.readLong();
        }
        catch (IOException e) {
            throw new NestedException("Problem reading SUSPEND packet", e);
        }
        if (myIncomingSuspendID == 1) { // doing a shutdown, not a suspend
            if (tr.debug && Trace.ON) tr.debugm("got shutdown request for connection " + this);
            mySender.doShutdown(new EOFException());
        }
        else {
            if (tr.debug && Trace.ON) tr.debugm("got suspend request for connection " + this + ", id = " + myIncomingSuspendID);
            try {
                suspend();
            }
            catch (IOException e) {
                throw new NestedException("Problem suspending connection for remote suspension request", e);
            }
            mySender.doSuspend();
        }
    }

    boolean noticeRemoteResume(long incomingSuspendID) {
        if (myIncomingSuspendID != incomingSuspendID) {
            myDeathCause = new Error("incorrect connection resumption state, (remote)" 
                        + Long.toHexString(incomingSuspendID) + " != (local)" 
                        + Long.toHexString(myIncomingSuspendID));
            myConnectionsManager.noticeProblem(this, myDeathCause);
            killConnection(myDeathCause);
            return false;
        }
        return true;
    }


    /**
     * Hang onto a proxy to proxy around to avoid premature dgc before the
     * originator knows about a handoff.
     *
     * @param proxyID The object's id number
     * @param proxy The object's proxy
     */
    private void addProxyToProxyToHandoffTable(long proxyID, Object proxy) {
        if (myHandoffProxyTable == null) {
            myHandoffProxyTable = new Hashtable(10);
        }
        myHandoffProxyTable.put(new Long(proxyID), proxy);
    }

    /**
     * Export an object we got from a different connection for handoff to this
     * connection.
     *
     * This is invoked on the originating connection for an object that has
     * been handed off - the recipient sends us a message asking us to do this,
     * giving us the registrar ID which is the broker that handed off the
     * object, and the proxyID for the object.
     *
     * @param registrarID The registrarID of the process we got the object from
     * @param proxyID The object's ID number on that connection
     */
    private void exportObjectForHandoff(String registrarID, long proxyID) {
        Exportable object = myTables.exports().get(proxyID);

        /* If we already exported it on this connection, we're done. */
        if (object != null) {
            return;
        }

        EConnection sideConnection;
        try {
            /* should already be connected, don't need path */
            sideConnection =
                myConnectionsManager.connection(registrarID, null);
        } catch (Exception e) {
            // XXX @@@ some kind of error
            return;
        }
        object = sideConnection.importExportTables().exports().get(proxyID);
        if (object == null) {
            /* XXX  try all the other connections for this ComMonitor */
        } else {
            myTables.exports().register(object);
        }
        /* Tell the receiver of the handoff that we've exported the Object to
           it (or failed), so it can clean up its proxy to a proxy for dgc. */
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(outbuf);
        try {
            msgOut.writeByte(Msg.EXPORT_OBJECT_REPLY);
            msgOut.writeLong(proxyID);
            msgOut.writeUTF(registrarID);
            mySender.sendPacket(outbuf);
        } catch (Exception e) {
            // @@@ XXX what can happen here?
        }
    }

    /**
     * Process a received EXPORT_OBJECT_REPLY message.
     *
     * @param packetBytes The message itself.
     */
    private void processExportObjectReply(byte packetBytes[]) {
        DataInputStream msgIn =
            new DataInputStream(new ByteArrayInputStream(packetBytes));
        try {
            byte header = msgIn.readByte(); /* already seen, discard */
            long proxyID = msgIn.readLong();
            String registrarID = msgIn.readUTF();
            /* Should already be connected, don't need path */
            EConnection sideConnection =
                myConnectionsManager.connection(registrarID, null);
            sideConnection.removeHandoffProxyToProxy(proxyID);
        } catch (Exception e) {
            // XXX @@@ some kind of error
        }
    }

    /**
     * Process a received EXPORT_OBJECT_REQUEST message.
     *
     * @param packetBytes The message itself.
     */
    private void processExportObjectRequest(byte packetBytes[]) {
        DataInputStream msgIn =
            new DataInputStream(new ByteArrayInputStream(packetBytes));
        try {
            byte header = msgIn.readByte(); /* already seen, discard */
            long proxyID = msgIn.readLong();
            String registrarID = msgIn.readUTF();
            exportObjectForHandoff(registrarID, proxyID);
        } catch (Exception e) {
            // XXX @@@ some kind of error
        }
    }

    /**
     * Process a received SUSPECT_TRASH message.
     *
     * @param packetBytes The message itself.
     */
    private void processSuspectTrash(byte packetBytes[]) {
        DataInputStream msgIn =
            new DataInputStream(new ByteArrayInputStream(packetBytes));
        try {
            byte header = msgIn.readByte(); /* already seen, discard */
            long forID = msgIn.readLong();
            int referenceCount = msgIn.readInt();
            myTables.exports().dgcSuspectTrash(forID, referenceCount);
        } catch (Exception e) {
            // @@@ XXX some kind of error
        }
    }

    /**
     * Process a received W_REMOVE_ME message.
     *
     * @param packetBytes The message itself.
     */
    private void processWRemoveMe(byte packetBytes[]) {
        DataInputStream msgIn =
            new DataInputStream(new ByteArrayInputStream(packetBytes));
        try {
            byte header = msgIn.readByte(); /* already seen, discard */
            long forID = msgIn.readLong();
            myTables.imports().unregister(forID);
        } catch (Exception e) {
            // @@@ XXX some kind of error
        }
    }

    /**
     * Cease holding onto a proxy to a proxy, as we are no longer afraid of
     * DGC happening to it.
     *
     * @param proxyID The id number of the proxy object.
     */
    private void removeHandoffProxyToProxy(long proxyID) {
        Object proxy = myHandoffProxyTable.remove(new Long(proxyID));
        if (myHandoffProxyTable.size() == 0) {
            myHandoffProxyTable = null;
        }
    }

    /**
     * Return the standard string compression table 
     */
    /*package*/ Hashtable getTheFastStrings() {
        return myConnectionsManager.myFastStrings;
    }

    /**
     * Return the standard string inverse compression table 
     */
    /*package*/ Hashtable getTheFastStringsInverse() {
        return myConnectionsManager.myFastStringsInverse;
    }

    public String toString() {
        String superString = super.toString();
        String state = "[UnknownState]";
        if (myState == STATE_WAITING_FOR_CONNECT) {
            state = "[Waiting for connect]" ;
        }
        else if (myState == STATE_SUSPENDED) {
            state = "[Suspended]" ;
        }
        else if (myState == STATE_SUSPENDING) {
            state = "[Suspending]" ;
        }
        else if (myState == STATE_RUNNING) {
            state = "[Running]" ;
        }
        else if (myState == STATE_DEAD) {
            state = "[Dead]" ;
        }
        return superString + " to e://" + myRemoteSearchPath + "/" + myRemoteRegistrarID + state ;
    }

    /**
     * Get the state of the connection
     *
     * @returns the current state of the connection
     */
    /*package*/ int getState() {
        return myState;
    }
}

public class EConnectionRecordException extends NestedExceptionVector
{
    private String myRemoteRegistrarId;

    public EConnectionRecordException(String rrid) {
        super(rrid == null ?
              "An incoming connection encountered the following problems:\n" :
              "Connection startup with " + rrid + " encountered the following problems:\n");
    }
}

public class EConnectionAttemptFailed extends IOException
{
    public EConnectionAttemptFailed(String msg) {
        super(msg);
    }
}
