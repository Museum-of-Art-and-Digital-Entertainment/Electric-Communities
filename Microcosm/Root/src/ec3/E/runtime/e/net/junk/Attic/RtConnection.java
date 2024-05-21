package ec.e.net;

import ec.e.cap.ERestrictedException;
import ec.e.db.RtStandardDecoder;
import ec.e.db.RtStandardEncoder;
import ec.e.db.StreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class RtConnection extends Thread {
    public static final String TransceiverUp = "TransceiverUp";
    public static final String TransceiverDown = "TransceiverDown";
    public static final String TransceiverFailed = "TransceiverFailed";
    public static final String TransceiverError = "TransceiverError";
    public static final String DeadConnection = "DeadConnection";

    static Trace tr = new Trace(false, "[RtConnection]");
    public static Trace ptr = new Trace(false, "[RtConnectionProfile]");

    private RtNotifier myNotifier = new RtNotifier();

    /* while the connection is suspended (no active transceiver) messages are
       queued here. */
    private RtMsgQEntry myMessageQueueHead = null;
    private RtMsgQEntry myMessageQueueTail = null;

    /* set to true when bringing down current transceiver */
    private boolean myStop = false;

    /* set to false when destroying entire connection */
    private boolean myAlive = true;

    private boolean myReturn = false;
    private boolean myHiPriorityWaiting = false;
    private Hashtable myMsgRequestTable = new Hashtable();
    private long myMsgRequestId = 0;
    private Thread myListenerThread = null;

    private String myRemoteSearchPath;
    private String myRemoteRegistrarId;
    private String myRemotePublisherId = null;
    private String mySearchPath;
    private String myRegistrarId;
    private String myPublisherId;
    private ENet myNetConnector;

    /* raw net capability that lets us do our work */
    private RtTransceiver myTransceiver;

    /* keeps intermediate value for crossconnect resolution */
    private RtTransceiver myNewTransceiver;

    private int myRetryFailedInterval = 60000; /* XXX should be changeable */
    private int myRetryDownInterval = 5000;    /* XXX should be changeable */

    private Vector mySearchVector;
    private Hashtable myHandoffProxyTable = null;

    /* Package scoped so the MsgSender/Receiver can get at them */
    private RtImportTable myImports;
    private RtExportTable myExports;
    private Hashtable myUniqueExports;
    private Hashtable myUniqueImports;
    private Hashtable myUniqueExportsById;
    private Hashtable myUniqueImportsById;
    private int myUniqueIds = 0;

    /* true if we don't need to actively create a transceiver (if we were
       created by an incoming connect request or we've succeeded in our
       outgoing connect request) */
    private boolean myHaveTransceiver;

    /* incoming normal priority messages queued here while high priority
       messages are processed */
    private RtMsgQEntry myNormalTrafficHead;
    private RtMsgQEntry myNormalTrafficTail;

    private RtRegistrarServer myKeeper;
    private EProxy myDirectoryServerProxy;
    private EProxy myConnectionKeeperProxy;
    private EConnectionKeeper myConnectionKeeper;

    private RtMsgReceiver myReceiver;
    private RtMsgSender mySender;

    /* XXX this is questionable. Calls to this should be wrapped in a higher-
       level abstraction. */
    RtImportTable getImports() {
        return myImports;
    }

    /* XXX this is questionable. Calls to this should be wrapped in a higher-
       level abstraction. */
    RtExportTable getExports() {
        return myExports;
    }

    /* XXX this is questionable. Calls to this should be wrapped in a higher-
       level abstraction. */
    Hashtable getUniqueExports() {
        return myUniqueExports;
    }

    /* XXX this is questionable. Calls to this should be wrapped in a higher-
       level abstraction. */
    Hashtable getUniqueImports() {
        return myUniqueImports;
    }

    /* XXX this is questionable. Calls to this should be wrapped in a higher-
       level abstraction. */
    Hashtable getUniqueExportsById() {
        return myUniqueExportsById;
    }

    /* XXX this is questionable. Calls to this should be wrapped in a higher-
       level abstraction. */
    Hashtable getUniqueImportsById() {
        return myUniqueImportsById;
    }

    /* XXX this is questionable. Calls to this should be wrapped in a higher-
       level abstraction. */
    int nextUniqueId() {
        return ++myUniqueIds;
    }

    public static void setupRemoteClassLoading () {
        System.out.println(
        "RtConnection.setupRemoteClassLoading: Remote class loading disabled");
        return;

        /*
        if (classEnvironment != null)
            return;
        classEnvironment = new RtRemoteClassEnvironment();
        classEnvironment.put("java");
        classEnvironment.put("ec.e");
        classEnvironment.put("ec.auth");
        classEnvironment.put("ec.clbless");
        classEnvironment.put("ec.crypt");
        classEnvironment.put("ec.eload");
        classEnvironment.put("ec.util");
        loaderReq = ECLoader.getRequester();
        try {
            LoaderManager.registerClassManager(
                RtRemoteClassManager.theClassManager);
        } catch (Exception e) {
            tr.$("Error registering class loader");
            e.printStackTrace();
        }
        */
    }

    RtConnection(RtRegistrarServer registrarServer, String registrarId,
                 String publisherId, String searchPath,
                 String remoteRegistrarId, String remotePath,
                 boolean haveTransceiver, ENet netConnector) {
        super("RtConnection(" + registrarId + ")");
        if (tr.tracing)
            tr.$("constructing RtConnection");
        myImports = new RtImportTable(this);
        myExports = new RtExportTable(this);
        myUniqueExports = new Hashtable();
        myUniqueExportsById = new Hashtable();
        myUniqueImports = new Hashtable();
        myUniqueImportsById = new Hashtable();

        myKeeper = registrarServer ;
        myRegistrarId = registrarId;
        myPublisherId = publisherId;
        mySearchPath = searchPath;
        myRemoteRegistrarId = remoteRegistrarId;
        myRemoteSearchPath = remotePath;

        myDirectoryServerProxy = getProxyForClass("ec.e.net.EDirectoryServer");
        ((EProxy_$_Impl)RtMagic.asImpl(myDirectoryServerProxy)).
            setConnection(this, 1L);
        myExports.registerDirectory(
            (EObject) registrarServer.getEDirectoryServer());
        myImports.registerDirectory((EProxy) myDirectoryServerProxy);

        myConnectionKeeperProxy =
            getProxyForClass("ec.e.net.EConnectionKeeper");
        ((EProxy_$_Impl)RtMagic.asImpl(myConnectionKeeperProxy)).
            setConnection(this, 2L);
        myImports.registerConnectionKeeper((EProxy) myConnectionKeeperProxy);
        myConnectionKeeper = EConnectionKeeper.makeKeeper(
            this, ((EConnectionKeeper) (EObject)myConnectionKeeperProxy));
        myExports.registerConnectionKeeper((EObject) myConnectionKeeper);

        myReceiver = new RtMsgReceiver(this);
        mySender = new RtMsgSender(this);

        myHaveTransceiver = haveTransceiver;
        myTransceiver = null;
        mySearchVector = new Vector(5);
        mySearchVector.addElement(null);
        extendSearchVector(remotePath);
        myNetConnector = netConnector ;
        setDaemon(false);
    }

    void AskForNotification(ENotificationHandler h) {
        myNotifier.addElement(h);
    }

    void UnAskForNotification(ENotificationHandler h) {
        myNotifier.removeElement(h);
    }

    private boolean connectViaSearchVector() {
        RtTransceiver tran;
        RtTransceiverFactory factory =
            new RtTransceiverFactory(myKeeper, myRegistrarId, myPublisherId,
                                    mySearchPath, myRemoteRegistrarId,
                                    myRemoteSearchPath, myNotifier, this);
        Hashtable tried = new Hashtable();

        while (true) {
            Enumeration en = mySearchVector.elements();
            while (en.hasMoreElements()) {
                String addr = (String) en.nextElement();
                if (addr == null || tried.containsKey(addr))
                    continue;
                tried.put(addr, addr);
                try {
                    tran = (RtTransceiver)myNetConnector.connect(
                        addr, (ENetConnectionFactory)factory);
                } catch (Exception e) {
                    /* XXX bad exception usage -- fix */
                    break;
                }
                if (tran != null) {
                    synchronized (mySearchVector) {
                        mySearchVector.removeElement(addr);
                        mySearchVector.insertElementAt(addr, 0);
                    }
                    return true;
                }
                /* this connection attempt may have added to searchVector,
                   start again. */
                break;
            }
            if (!en.hasMoreElements()) {
                return false;
            }
        }
    }

    /* called from RtTransceiver on connection referral and on creation above */
    void extendSearchVector(String path) {
        String p[] = RtEARL.ParseSearchPath(path);

        synchronized (mySearchVector) {
            mySearchVector.ensureCapacity(mySearchVector.size() + p.length);
            for (int i=p.length-1; i>=0; i--) {
                mySearchVector.removeElement(p[i]);
                mySearchVector.insertElementAt(p[i], 1);
            }
        }
    }

    String getLocalRegistrarId() {
        return myRegistrarId;
    }

    String getRemoteRegistrarId() {
        return myRemoteRegistrarId;
    }

    String getRemoteSearchPath() {
        return myRemoteSearchPath;
    }

    /*
    public Vector getClassesRemotely (Vector requestedClassInfos) {
        Vector info[];
        Vector classes;
        Vector classInfos;
        Vector certificates;
        RtMsgRequest request;
        Class theClass;
        int i;
        int size;
        RtRemoteClassManager classManager =
            RtRemoteClassManager.theClassManager;

        if (requestedClassInfos == null) return null;
        for (i = 0, size = requestedClassInfos.size(); i < size; i++) {
            // If caller lied about Vector of ClassInfos
            // We'll get a class cast exception here! HA!
            ClassInfo ci = (ClassInfo)requestedClassInfos.elementAt(i);
        }
        request = performHighPriorityRequest(requestedClassInfos,
                                             RtMsg.kcClassReferenceRequest);
        if (request == null) {
            tr.$("Error back from high priority request");
            return null;
        }
        if (request.getResult() != null) {
            info = (Vector[])request.getResult();
            classInfos = info[0];
            certificates = info[1];
            tr.$("Adding received certificates");
            //ECLoader.addCertificates(certificates);
            tr.$("Caching received classes with RemoteClassManager");
            RtRemoteClassManager.theClassManager.cacheClasses(classInfos);
        }
        size = requestedClassInfos.size();
        classes = new Vector(size);
        for (i = 0; i < size; i++) {
            theClass = null;
            ClassInfo ci = (ClassInfo)requestedClassInfos.elementAt(i);
            if (tr.tracing) tr.$("Asking LoaderManager to load " +
                                 ci.getName());
            try {
                //theClass = loaderReq.loadClass(ci);
                theClass = null;
                if (tr.tracing)
                    tr.$("Got " + theClass + " back from EC loader for " +
                         ci.getName());
            } catch (Throwable t) {
                if (tr.tracing)
                    tr.$("Exception getting ClassInfo for " + ci.getName() +
                         " " + t);
            }

            if (theClass != null) {
                if (tr.tracing)
                    tr.$("Adding class " + ci.getName() + " to vector");
                classes.addElement(theClass);
            } else {
                if (tr.tracing)
                    tr.$("Got null back from EC loader for " + ci.getName());
            }
        }
        return classes;
    }

    private void dumpClassInfos(Vector classInfos) {
        int i;
        int size;
        String classString = "(DumpClassInfos) Classes:";
        for (i = 0, size = classInfos.size(); i < size; i++) {
            ClassInfo classInfo = (ClassInfo)classInfos.elementAt(i);
            classString = classString + " " + classInfo.getName();
        }
        tr.$(classString);
    }
    */

    void handleIncomingHighPriorityMessage(RtMsgQEntry msg) {
        Vector classInfos = null;
        Hashtable refClassInfos;
        Enumeration en;
        Vector classCertificates = null;
        Vector info[];
        RtStandardDecoder stream =
            new RtStandardDecoder(myReceiver, myReceiver.getTypeTable(),
                                  msg.getMsg());
        long requestID = 0;
        int i;
        int size;

        switch (msg.getMsgCode()) {
            case RtMsg.kcClassReferenceRequest:
                System.out.println(
                "RtConnection: message kcClassReferenceRequest not supported");
                /*
                try {
                    requestID = stream.readLong();
                    classInfos = (Vector)stream.decodeObject();
                } catch (Exception e) {
                    tr.$("Error reading class infos");
                    e.printStackTrace();
                    sendErrorIndication(requestID);
                    return;
                }
                try {
                    tr.$("Asking class loader for referenced classes");
                    if (tr.tracing)
                        dumpClassInfos(classInfos);
                    //refClassInfos =
                    //    loaderReq.getAllReferencedClasses(classInfos,
                    //                                      classEnvironment);
                    refClassInfos = null;
                    en = refClassInfos.elements();
                    while (en.hasMoreElements()) {
                        classInfos.addElement(en.nextElement());
                    }
                    if (tr.tracing)
                        dumpClassInfos(classInfos);
                } catch (Exception e) {
                    tr.$("Exception getting classes for remote request");
                    e.printStackTrace();
                    sendErrorIndication(requestID);
                    return;
                }
                sendHighPriorityMessage(classInfos, requestID,
                                        RtMsg.kcClassReferenceReply);
                */
                break;

            case RtMsg.kcClassLoadRequest:
                System.out.println(
                    "RtConnection: message kcClassLoadRequest not supported");
                /*
                try {
                    requestID = stream.readLong();
                    classInfos = (Vector)stream.decodeObject();
                } catch (Exception e) {
                    tr.$("Error reading classNames");
                    e.printStackTrace();
                    sendErrorIndication(requestID);
                    return;
                }
                info = new Vector[2];
                try {
                    tr.$("Getting classes and certificates");
                    if (tr.tracing) dumpClassInfos(classInfos);
                    //info[0] = loaderReq.getClasses(classInfos);
                    //info[1] = ECLoader.getCertificates(info[0]);
                    info[0] = null;
                    info[1] = null;
                } catch (Exception e) {
                    tr.$("Error getting classes and certificates");
                    e.printStackTrace();
                    sendErrorIndication(requestID);
                    return;
                }
                sendHighPriorityMessage(info, requestID,
                                        RtMsg.kcClassLoadReply);
                */
                break;

            case RtMsg.kcClassReferenceReply:
                System.out.println(
                  "RtConnection: message kcClassReferenceReply not supported");
                /*
                try {
                    requestID = stream.readLong();
                    classInfos = (Vector)stream.decodeObject();
                } catch (Exception e) {
                    tr.$("Error reading classNames");
                    e.printStackTrace();
                }
                if (classInfos == null) {
                    finishHighPriorityRequest(requestID, null);
                    return;
                }
                synchronized (this) {
                    // Don't optimize out <classInfos.size(), size might change
                    for (i = 0; i < classInfos.size(); ) {
                        ClassInfo classInfo =
                            (ClassInfo)classInfos.elementAt(i);
                        try {
                            tr.$("Calling checkForClass for " +
                                 classInfo.getName() + ", hash " +
                                 classInfo.getHash());
                            if (true) {
                                // This changes classInfos.size() -- see above
                                tr.$("Already have " + classInfo.getName());
                                classInfos.removeElementAt(i);
                                continue;
                            }
                        } catch (Exception e) {
                            // We don't care, this means it's not there
                        }
                        i++;
                    }
                }
                if (classInfos.size() == 0) {
                    finishHighPriorityRequest(requestID, null);
                    return;
                }
                sendHighPriorityMessage(classInfos, requestID,
                                        RtMsg.kcClassLoadRequest);
                */
                break;

            case RtMsg.kcClassLoadReply:
                System.out.println(
                    "RtConnection: message kcClassLoadReply not supported");
                /*
                try {
                    requestID = stream.readLong();
                    info = (Vector[]) stream.decodeObject();
                } catch (Exception e) {
                    tr.$("Error reading class info array");
                    e.printStackTrace();
                    info = null;
                }
                if (info[0] != null)
                    if (tr.tracing) dumpClassInfos(info[0]);
                finishHighPriorityRequest(requestID, info);
                */
                break;

            case RtMsg.kcClassError:
                System.out.println(
                    "RtConnection: message kcClassError not supported");
                /*finishHighPriorityRequest(requestID, null); */
                break;

            default:
                tr.$("Unknown high priority message code: " +
                     msg.getMsgCode());
        }
    }

    private static EProxy getProxyForClass(String name) {
        try {
            return((EProxy)Class.forName(name + "_$_Proxy").newInstance());
        } catch (Exception e) {
            return(null);
        }
    }

    private void handleHighPriorityMessage(RtMsgQEntry msg) {
        RtHighPriorityHandler handler;
        String classNames[];

        switch (msg.getMsgCode()) {
            /* Handle these in another thread so we don't get */
            /* too busy in our listening thread */
            case RtMsg.kcClassReferenceRequest:
            case RtMsg.kcClassLoadRequest:
                handler = new RtHighPriorityHandler(this, msg);
                handler.start();
                break;

            /* These can be handled in the current thread */
            case RtMsg.kcClassReferenceReply:
            case RtMsg.kcClassLoadReply:
            case RtMsg.kcClassError:
                handleIncomingHighPriorityMessage(msg);
                break;

            default:
                tr.$("Unknown high priority message code: " +
                     msg.getMsgCode());
            }
    }

    private boolean sendHighPriorityMessage(Object object, long requestID,
                                            int msgType) {
        RtStandardEncoder stream =
            new RtStandardEncoder(mySender, mySender.getTypeTable());
        try {
            stream.writeLong(requestID);
            stream.encodeObject(object);
            byte msgBytes[] = stream.getBytes();
            sendBlobBytes(msgBytes, msgType);
            return true;
        } catch (Exception e) {
            /* XXX bad exception usage -- fix */
            tr.$("Caught exception encoding high priority message");
            e.printStackTrace();
            return false;
        }
    }

    private RtMsgRequest performHighPriorityRequest(Object info, int msgType) {
        Thread thread = Thread.currentThread();
        RtMsgRequest request = new RtMsgRequest(thread, msgType, info);
        long requestID = (long)0;

        synchronized (myMsgRequestTable) {
            requestID = ++myMsgRequestId;
            myMsgRequestTable.put(new Long(requestID), request);
            myHiPriorityWaiting = true;
        }

        /* XXX - Make sure not RtRun thread! */
        if (thread != myListenerThread) {
            synchronized (thread) {
                if (sendHighPriorityMessage(info, requestID, msgType) == true){
                    try {
                        wait(0);
                    } catch (Exception e) {
                        tr.$("Woken up");
                    }
                } else {
                    removeMessageRequest(requestID);
                    return null;
                }
            }
        } else {
            if (sendHighPriorityMessage(info, requestID, msgType) == true)
                readIncomingMessages();
            else {
                removeMessageRequest(requestID);
                return null;
            }
        }
        return request;
    }

    private void finishHighPriorityRequest(long requestID, Object result) {
        RtMsgRequest req = removeMessageRequest(requestID);
        req.setResult(result);
        Thread reqThread = req.getThread();
        if (reqThread == myListenerThread) {
            myReturn = true;
        } else {
            synchronized (reqThread) {
                reqThread.notify();
            }
        }
    }

    private RtMsgRequest removeMessageRequest(long requestID) {
        RtMsgRequest req = null;
        synchronized (myMsgRequestTable) {
            req = (RtMsgRequest) myMsgRequestTable.remove(new Long(requestID));
            if (myMsgRequestTable.isEmpty() == true) {
                myHiPriorityWaiting = false;
            }
        }
        return req;
    }

    private void sendErrorIndication(long requestID) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        try {
            stream.writeLong(requestID);
            sendBlob(byteStream, RtMsg.kcClassError);
        } catch (Exception e) {
            tr.$("Error sending class error message");
            e.printStackTrace();
        }
    }

    /* Need to keep the proxy to proxy around to avoid premature dgc */
    /* before the originator knows about the handoff ... */
    private synchronized void addProxyToProxyToHandoffTable(long proxyID,
                                                            Object proxy) {
        if (myHandoffProxyTable == null) {
            if (tr.tracing)
                tr.$("Making handoff to handoff table");
            myHandoffProxyTable = new Hashtable(10);
        }
        if (tr.tracing)
            tr.$("Adding proxy " + proxy +
                 " to handoff to handoff table for id " + proxyID);
        myHandoffProxyTable.put(new Long(proxyID), proxy);
    }

    private synchronized void removeHandoffProxyToProxy(long proxyID) {
        if (myHandoffProxyTable == null) {
            System.out.println(
                "RemoveHandoffProxyToProxy: Error, no handoff table, trying to remove proxy " + proxyID);
            if (tr.tracing)
                tr.$("RemoveHandoffProxyToProxy: Error, no handoff table, trying to remove proxy " + proxyID);
        } else {
            if (tr.tracing)
                tr.$("Removing handoff to handoff proxy for id " + proxyID);
            Object proxy = myHandoffProxyTable.remove(new Long(proxyID));
            if (proxy == null) {
                if (tr.tracing)
                    tr.$("Proxy to proxy not found in table for id " +
                         proxyID);
            }
            if (myHandoffProxyTable.size() == 0) {
                if (tr.tracing)
                    tr.$("Removing handoff to handoff table");
                myHandoffProxyTable = null;
            } else {
                if (tr.tracing)
                    tr.$("Leaving handoff to handoff table, size is " +
                         myHandoffProxyTable.size());
            }
        }
    }

    private void handleExportHandoffRequest(RtMsgQEntry msg) {
        ByteArrayInputStream byteStream =
            new ByteArrayInputStream(msg.getMsg());
        DataInputStream stream = new DataInputStream(byteStream);
        try {
            String rid;
            long proxyID;
            proxyID = stream.readLong();
            rid = stream.readUTF();
            if (tr.tracing)
                tr.$("Exporting proxy " + proxyID + " from " + rid);
            exportObjectForHandoff(rid, proxyID);
        } catch (Exception e) {
            if (tr.tracing)
                tr.$("HandleExportHandoffRequest: Error receiving export request on handoff");
            e.printStackTrace();
        }
    }

    private void handleExportHandoffReply(RtMsgQEntry msg) {
        ByteArrayInputStream byteStream =
            new ByteArrayInputStream(msg.getMsg());
        DataInputStream stream = new DataInputStream(byteStream);
        try {
            String rid;
            long proxyID;
            proxyID = stream.readLong();
            rid = stream.readUTF();
            if (tr.tracing)
                tr.$("Originator exported proxy " + proxyID + " from " + rid);
            RtConnection conn =/*should already be connected, don't need path*/
                myKeeper.getConnectionToRegistrar(rid, null, null, false);
            if (conn == null) {
                if (tr.tracing)
                    tr.$("HandleExportHandoffReply: Error, handoff reply referencing unknown connection:" + rid);
            }
            conn.removeHandoffProxyToProxy(proxyID);
        } catch (Exception e) {
            if (tr.tracing)
                tr.$("HandleExportHandoffReply: Error handling export reply on handoff");
            e.printStackTrace();
        }
    }

    private void handleNewClasses(RtMsgQEntry msg) {
        /* XXX - Add perf trace to this ... */
        int i;
        ByteArrayInputStream byteStream =
            new ByteArrayInputStream(msg.getMsg());
        DataInputStream stream = new DataInputStream(byteStream);
        try {
            int size = stream.readInt();
            for (i = 0; i < size; i++) {
                String name = stream.readUTF();
                int code = stream.readInt();
                if (tr.tracing)
                    tr.$("Read in class name " + name + " for code " + code);
                if (!myReceiver.getTypeTable().registerClassByName(name,
                                                                   code)) {
                    tr.$("Error registering class " + name);
                    /* XXX Invoke remote class load getClassesRemotely() here*/
                    throw new ClassNotFoundException(" Can't find class " +
                                                     name);
                }
            }
        } catch (Exception e) {
            tr.$("Error receiving new classes");
            e.printStackTrace();
        }
    }

    private boolean handleMessage(RtMsgQEntry msg) {
        ENetAddr address;
        long proxyID;

        switch (msg.getMsgCode()) {
            case RtMsg.kcExportObjectRequest:
                handleExportHandoffRequest(msg);
                break;

            case RtMsg.kcExportObjectReply:
                handleExportHandoffReply(msg);
                break;

            case RtMsg.kcNewClasses:
                handleNewClasses(msg);
                break;

            default:
                myConnectionKeeper <- handleIncomingMessage(msg);
                break;
            }
        return true;
    }

    /*
      Called back by ConnectionKeeper from within the EVM
    */
    void handleIncomingMessage(RtMsgQEntry msg) {
        try {
            myReceiver.handleEnvelopeMessage(msg);
        } catch (StreamFormatException e) {
            tr.$("Stream format exception on incoming message");
            e.printStackTrace();
        }
    }

    public void run() {
        myListenerThread = Thread.currentThread();
        boolean should_notify = true;

        if (tr.tracing)
            tr.$("new RtConnection run() loop");
        while (myAlive) {
            if (myTransceiver == null) {
                if (!myHaveTransceiver) {
                    if (tr.tracing)
                    tr.$("no transceiver, trying to connect via search vector");
                    if (connectViaSearchVector()) {
                        if (tr.tracing)
                            tr.$("got a transceiver from connection");
                        myHaveTransceiver = true;
                    } else {
                        if (should_notify) {
                            myNotifier.notify(TransceiverFailed, this,
                                              myRemoteRegistrarId);
                            should_notify = false ;
                        }
                        try {
                            if (tr.tracing)
                                tr.$("connection attempt failed, waiting " +
                                     myRetryFailedInterval);
                            synchronized (this) {
                                if (myAlive)
                                    wait(myRetryFailedInterval);
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                }
                if (myAlive && myHaveTransceiver && myTransceiver == null) {
                    if (tr.tracing)
                        tr.$("waiting for transceiver to be set");
                    synchronized (this) {
                        while (myAlive && myTransceiver == null) {
                            try {
                                wait();/* setTransceiver will tell us when set*/
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    if (tr.tracing)
                        tr.$("transceiver has been set (we hope)");
                }
            }
            if (myAlive && myTransceiver != null) {
                should_notify = true ;
                myStop = false;
                if (tr.tracing)
                    tr.$("transceiver up");
                myNotifier.notify(TransceiverUp, this, myRemoteRegistrarId);
                readIncomingMessages();
                if (tr.tracing) tr.$("transceiver down");
                myNotifier.notify(TransceiverDown, this, myRemoteRegistrarId);

                /* The following line needs to be removed when connections can
                   negotiate to see if they're in sync.  Until then, we assume
                   they're out of sync, so just kill the connection. */
                myAlive = false;

                setTransceiver(null);
                try {
                    /* XXX may want to randomize to minimize crossconnects */
                    synchronized (this) {
                        if (myAlive)
                            wait(myRetryDownInterval);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        if (tr.tracing)
            tr.$("connection dead: notifying and unregistering");
        myNotifier.notify(DeadConnection, this, myRemoteRegistrarId);
        myUniqueExports.clear();
        myUniqueImports.clear();
        myUniqueExportsById.clear();
        myUniqueImportsById.clear();
        myExports.unregisterAll();
        myImports.unregisterAll();
        myKeeper.unRegisterConnection(this, myRemoteRegistrarId,
                                      myRemotePublisherId);
    }

    void setRidPid(String registrarId, String publisherId) {
        if (tr.tracing) tr.$("registrarId = " + registrarId +
                             ", publisherId = " + publisherId);
        myRemoteRegistrarId = registrarId;
        myRemotePublisherId = publisherId;
    }

    synchronized boolean newTransceiverLives(RtTransceiver tran,
                                            boolean incoming) {
        boolean ret;

        if (myNewTransceiver == null) {
            if (tr.tracing)
                tr.$("newTransceiverLives initial setting " + tran);
            myNewTransceiver = tran;
            return true;
        }
        if (myNewTransceiver == tran) {
            if (tr.tracing)
                tr.$("newTransceiverLives identical setting " + tran);
            return true;
        }
        /* decide which transceiver to nuke. */
        ret = (incoming ==
               (myRemoteRegistrarId.hashCode() > myRegistrarId.hashCode()));
        if (tr.tracing)
            tr.$("newTransceiverLives(" + tran + ") returning " + ret);
        return ret;
    }

    synchronized void setTransceiver(RtTransceiver tran) {
        if (tr.tracing) tr.$("setTransceiver " + tran);
        myTransceiver = tran;
        myNewTransceiver = null;
        notifyAll();
        if (myTransceiver != null)
            sendQueuedMessages();
    }

    private void readIncomingMessages() {
        RtMsgQEntry msg;

        while ((myReturn == false) && (myStop == false)) {
            while ((myNormalTrafficHead != null) &&
                   (myHiPriorityWaiting == false)) {
                /* Call routine to handle the message */
                if (handleMessage(myNormalTrafficHead)) {
                    /* This is true if we should dequeue */
                    myNormalTrafficHead = myNormalTrafficHead.getNext();
                }
            }
            if (myNormalTrafficHead == null)
                myNormalTrafficTail = null ;

            /* I know there's got to be a better way to code this */
            if (myReturn || myStop)
                break;
            msg = myTransceiver.getMessage();
            if (myStop)
                break;
            if (msg == null)
                continue;

            if (msg.getMsgCode() < 0) {
                tr.$("Have a high priority message");
                handleHighPriorityMessage(msg);
                tr.$("Returning after high priority message");
            } else {
                if (myNormalTrafficTail != null)
                    myNormalTrafficTail.setNext(msg);
                else
                    myNormalTrafficHead = msg;
                myNormalTrafficTail = msg;
            }
        }
        myReturn = false;
    }

    public synchronized void suspendConnection() {
        tr.$("Entered suspendConnection");
        if (myStop == true) {
            tr.$("Already stopped");
        } else {
            tr.$("suspendConnection(), setting stop & trying to close socket");
            myStop = true;
            if (myTransceiver != null) {
                myTransceiver.teardown();
            }
        }
    }

    public synchronized void shutdownConnection() {
        if (tr.tracing)
            tr.$("shutdownConnection()");
        myConnectionKeeper <- initiateShutdown();
    }

    public synchronized void killConnection() {
        if (tr.tracing)
            tr.$("killConnection()");
        myAlive = false;
        suspendConnection();
    }

    public boolean isProxyOnConnection(Object object) {
        if ((object != null) && (object instanceof EObject_$_Impl)) {
            EObject_$_Impl eo = (EObject_$_Impl)object;
            return RtMagic.isProxyOnConnection(eo, this);
        }
        return false;
    }

    public synchronized void removeUniqueMappingForObject(Object object) {
        Integer eid = (Integer)myUniqueExports.remove(object);
        Integer iid = (Integer)myUniqueImports.remove(object);
        if (eid != null) {
            eid = new Integer(-(eid.intValue()));
            myUniqueExportsById.remove(eid);
        }
        if (iid != null) {
            myUniqueImportsById.remove(iid);
        }
    }

    private synchronized void sendQueuedMessages() {
        RtTransceiver tran = myTransceiver;
        if (tran == null) {
            if (tr.tracing)
                tr.$("transceiver reset to null too fast to dequeue messages");
            return;
        }
        if (tr.tracing)
            tr.$("sending queued messages");
        while (myMessageQueueHead != null) {
            try {
                if (tr.tracing)
                    tr.$("sending a queued message");
                tran.reallySendBlobBytes(myMessageQueueHead.getMsg(),
                                         myMessageQueueHead.getMsgCode());
            } catch (Exception e) {
                /* transceiver has been reset to null, keep currently queued
                   messages */
                if (tr.tracing)
                    tr.$("exception while sending queued message, returning");
                return;
            }
            myMessageQueueHead = myMessageQueueHead.getNext() ;
        }
        /* queue completly drained. */
        if (tr.tracing)
            tr.$("finished sending queued messages");
        myMessageQueueTail = null;
    }

    public void sendBlobBytes(byte msg[], int msgType)
            throws RtConnectionFailedException {
        RtTransceiver tran;

        while (msg != null) {
            tran = myTransceiver;
            if (tran == null) {
                synchronized (this) {
                    tran = myTransceiver ;
                    if (tran == null) {
                        RtMsgQEntry entry =
                            new RtMsgQEntry(msgType, msg.length, 0, msg);
                        if (tr.tracing)
                            tr.$("Queueing message type " +
                                 RtMsg.stringForMessage(entry.getMsgCode()) +
                                 " length " + entry.getMsg().length);
                        if (myMessageQueueTail != null)
                            myMessageQueueTail.setNext(entry);
                        else
                            myMessageQueueHead = entry;
                        myMessageQueueTail = entry;
                        msg = null;
                    }
                }
            }
            if (msg != null && tran != null) {
                try {
                    tran.reallySendBlobBytes(msg, msgType);
                    msg = null;
                }
                catch (Exception e) {
                    /* exception in reallySendBlobBytes should have resulted in
                       setTransceiver(null); */
                }
            }
        }
    }

    public void sendBlob(ByteArrayOutputStream stream, int msgCode)
            throws RtConnectionFailedException {
        byte msgData[] = stream.toByteArray();
        sendBlobBytes(msgData, msgCode);
    }

    EDirectoryServer getDirectoryServerProxy() {
        return((EDirectoryServer) (EObject)myDirectoryServerProxy);
    }

    synchronized void ensureRunning () {
        if (myTransceiver == null) {
            if (tr.tracing)
                tr.$("Thread (" + Thread.currentThread() +
                     ") waiting for connection to start up");
            try {
                this.wait(0);
            } catch (Exception e) {
                /* This is the idea, I want to get InterruptedException ... */
            }
        }
    }

    /*
      This is called by the MsgReceiver if a message can't be delivered
      because a handoff failed. We raise an EException if the exEnv
      is not null (if it is, nobody would catch the exception so we
      skip it), but since we want to send the exception to the decoded
      exception environment, we enqueue a message to ourselves to do it
    */
    void throwHandoffFailed(RtExceptionEnv exEnv) {
        if (exEnv == null)
            return; /* Nobody to catch, so skip */
        RtEnvelope env;
        env <- EConnectionKeeper.handleHandoffFailure();
        RtRun.enqueue(myConnectionKeeper, env, exEnv);
    }

    /*
      This is invoked on the originating connection for an object
      that has been handed off - the recipient sends us a message
      asking us to do this, giving us the RegistrarId which is the
      broker that handed off the object, and the proxyID for the object.
    */
    void exportObjectForHandoff(String rid, long proxyID) {
        EObject_$_Impl object = myExports.get(proxyID);

        /* See if we already exported it on this connection */
        if (object != null) {
            if (tr.tracing)
                tr.$("Already exported handoff object " + proxyID);
            return;
        }

        RtConnection conn;
        try {
            /* should already be connected, don't need path */
            conn = myKeeper.getConnectionToRegistrar(rid, null, null, false);
        } catch (RtConnectionFailedException e) {
            if (tr.tracing)
                tr.$("Connection ( " + rid +
                     ") allegedly handing off object " + proxyID +
                     " not found:  connection failed");
            return;
        }
        object = conn.myExports.get(proxyID);
        if (object == null) {
            /* XXX  try all the other connections for this ComMonitor */
            if (tr.tracing)
                tr.$("Object " + proxyID +
                     " not found in brokering connection");
        } else {
            if (tr.tracing)
                tr.$("Registering " + object + " with identity " +
                     object.getIdentity() + " in exports table for handoff");
            myExports.register(object);
        }
        /* Tell the receiver of the handoff that we've exported the Object to
           it (or failed), so it can clean up its proxy to a proxy for dgc. */
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream outS = new DataOutputStream(byteStream);
        try {
            outS.writeLong(proxyID);
            outS.writeUTF(rid);
            sendBlob(byteStream, RtMsg.kcExportObjectReply);
        } catch (Exception e) {
            if (tr.tracing)
                tr.$("ExportObjectForHandoff: Error replying to receiver for handoff object");
            e.printStackTrace();
            return;
        }
    }

    /*
      MsgReceiver calls this on recipient side of a handoff to setup the
      proxy. If there is no connection in place, it establishes the connection,
      if there is a connection, but the object isn't imported yet it tells the
      originator on the other side to exportObjectForHandoff.
    */
    Object getProxyForHandoff(long proxyID, Class theClass,
                              String originatorRegistrarId,
                              String originatorPath,
                              String senderRegistrarId) {
        RtConnection connection;
        try {
            connection =
                myKeeper.getConnectionToRegistrar(originatorRegistrarId, null,
                                                  originatorPath, false);
        } catch (Exception e) {
            connection = null;
        }
        if (connection == null) {
            if (tr.tracing)
                tr.$("GetProxyForHandoff: Couldn't get connection to originator for handoff");
            return null;
        }

        Object proxy = connection.myImports.get(proxyID, false);
        if (proxy != null) {
            if (tr.tracing)
                tr.$("Found proxy for handoff, using it");
            return proxy;
        } else {
            proxy = connection.myImports.registerProxy(theClass, proxyID);
            addProxyToProxyToHandoffTable(proxyID, proxy);
            if (tr.tracing)
                tr.$("Asking originator " + originatorRegistrarId +
                     " to export proxy for " + proxyID + " from " +
                     senderRegistrarId);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream outS = new DataOutputStream(byteStream);
            try {
                outS.writeLong(proxyID);
                outS.writeUTF(senderRegistrarId);
                connection.sendBlob(byteStream, RtMsg.kcExportObjectRequest);
            } catch (Exception e) {
                if (tr.tracing)
                    tr.$("GetProxyForHandoff: Error asking originator to handoff object");
                e.printStackTrace();
                removeHandoffProxyToProxy(proxyID);
                return null;
            }
            return proxy;
        }
    }

    public void sendEnvelope(long told, RtEnvelope env)
        throws Exception {
            long time = 0;
            if (ptr.tracing) time = System.currentTimeMillis();
            mySender.sendEnvelope(told, env);
            if (ptr.tracing)
                ptr.$("Took " + (System.currentTimeMillis() - time) +
                      " milliseconds to send envelope");
        }

    public String cleanupStackTrace(String trace) {
        return "\t[Comm message received on " + this + "]\n";
    }

    /* ---------------------------------------------------------- */
    /*             Distributed Garbage Collection messages        */
    /* ---------------------------------------------------------- */

    /*
      Send a WRemoveMe.
    */
    public void dgcWRemoveMe (long id) {
        if (tr.tracing)
            tr.$("Sending out a dgcWRemove me over the wire: "+ id);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream outS = new DataOutputStream(byteStream);
        myExports.remove(id); /* I am sure it is dereferenced */
        try {
            outS.writeLong(id);
            sendBlob(byteStream, RtMsg.kcWRemoveMe);
        } catch (Exception e) {
            if (tr.tracing)
                e.printStackTrace();
        }
    }

    public void dgcSuspectTrash(long id, int referenceCount) {
        if (tr.tracing)
            tr.$("Sending out a dgcSuspectTrash me over the wire: " + id);
        myImports.gotFinalized(id);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream outS = new DataOutputStream(byteStream);
        try {
            outS.writeLong(id);
            outS.writeInt(referenceCount);
            sendBlob(byteStream, RtMsg.kcSuspectTrash);
        } catch (Exception e) {
            if (tr.tracing)
                e.printStackTrace();
        }
    }
}

class RtMsgRequest {
    private Thread myThread;
    private int myRequest;
    private Object myInfo;
    private Object myResult = null;

    private RtMsgRequest() {
    }

    RtMsgRequest(int request, Object info) {
        this(Thread.currentThread(), request, info);
    }

    RtMsgRequest(Thread thread, int request, Object info) {
        myThread = thread;
        myRequest = request;
        myInfo = info;
    }

    Object getInfo() {
        return myInfo;
    }

    int getRequest() {
        return myRequest;
    }

    Object getResult() {
        return myResult;
    }
    void setResult(Object result) {
        myResult = result;
    }

    Thread getThread() {
        return myThread;
    }
}

class RtHighPriorityHandler extends Thread {
    private RtMsgQEntry myMsg;
    private RtConnection myConnection;

    private RtHighPriorityHandler() {
        super("RtHighPriorityHandler()");
    }

    RtHighPriorityHandler(RtConnection connection, RtMsgQEntry msg) {
        myConnection = connection;
        myMsg = msg;
    }

    public void run() {
        myConnection.handleIncomingHighPriorityMessage(myMsg);
    }
}

class RtTransceiverFactory implements ENetConnectionFactory {
    private RtRegistrarServer myRegistrarServer;
    private String myRegistrarId;
    private String myPublisherId;
    private String mySearchPath;
    private String myRemoteRegistrarId;
    private String myRemotePath;
    private RtNotifier myNotifier;
    private RtConnection myConnection;

    RtTransceiverFactory(RtRegistrarServer registrarServer, String registrarId,
                        String publisherId, String searchPath,
                        String remoteRegistrarId, String remotePath,
                        RtNotifier notifier, RtConnection connection) {
        myRegistrarServer = registrarServer;
        myRegistrarId = registrarId;
        myPublisherId = publisherId;
        mySearchPath = searchPath;
        myRemoteRegistrarId = remoteRegistrarId;
        myRemotePath = remotePath;
        myNotifier = notifier;
        myConnection = connection;
    }

    public Object manufacture(Socket sock, ENetAddr localAddr,
                              ENetAddr remoteAddr, boolean incoming)
            throws ERestrictedException, IOException {
        RtTransceiver transceiver;

        transceiver = new RtTransceiver(myRegistrarServer, myRegistrarId,
            myPublisherId, mySearchPath, sock, localAddr, remoteAddr,
            myRemoteRegistrarId, myRemotePath, incoming);
        if (incoming) {
            RtTransceiverListener listener =
                new RtTransceiverListener(transceiver, myNotifier);
            listener.start();
        } else {
            try {
                if (transceiver.startup()) {
                    return (Object)transceiver;
                }
                return null;
            } catch (Throwable t) {
                myNotifier.notify(RtConnection.TransceiverError, myConnection,
                                  t.toString());
                return null;
            }
        }
        return (Object)transceiver;
    }
}

class RtTransceiverListener extends Thread {
    private RtTransceiver myTransceiver;
    private RtNotifier myNotifier;

    RtTransceiverListener(RtTransceiver transceiver, RtNotifier notifier) {
        super("RtTransceiverListener()");
        myTransceiver = transceiver;
        myNotifier = notifier;
    }

    public void run() {
        try {
            myTransceiver.startup();
        } catch (Throwable t) {
            myNotifier.notify(RtConnection.TransceiverError, null,
                              t.toString());
        }
    }
}

class RtHandoffException extends RuntimeException {
    public RtHandoffException () {
    }
}
