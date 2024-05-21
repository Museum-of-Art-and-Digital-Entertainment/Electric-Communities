package ec.e.net;

import ec.util.CompletionNoticer;
import ec.util.CompletionNoticerJoin;
import ec.e.util.SetCollection;
import ec.e.file.EStdio;
import ec.e.timer.Timer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.EOFException;

eclass RegistrarHelper {
    static private final Trace tr = new Trace("ec.e.net.RegistrarHelper");

    // Reregistration must occur before registration times out (in
    // ProcessLocationServerHelper), currently set to 1 hour.
    static private final long REREGISTER_INTERVAL = 50L * 60L * 1000L; // msec (50 minutes)
    
    private Registrar myRegistrar;
    private String myRegistrarID;
    private String myListenAddr;
    private boolean myShutdownConnections;
    private CompletionNoticerJoin myShutdownNoticer;
    private int myTimerId = -1 ;
    private Timer myTimer;
    
    private RegistrarHelper() {}
    
    RegistrarHelper(Registrar reg, String registrarID, boolean shutdownConnections) {
        myRegistrar = reg;
        myRegistrarID = registrarID;
        myShutdownConnections = shutdownConnections;
        myTimer = Timer.TheSmashingTimer();
    }
    
    emethod setListenAddr(String listenAddr) {
        myListenAddr = listenAddr;
    }
    
    emethod registerWith(SetCollection set) {
        EBoolean tick = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor tick_dist = EUniChannel.getDistributor(tick);
        if (myTimerId != -1) {
            myTimer.cancelTimeout(myTimerId);
        }
        this <- doRegisterWith(set);
        myTimerId = myTimer.setTimeout(REREGISTER_INTERVAL, etrue, tick_dist);
        ewhen tick (boolean ignored) {
            this <- registerWith(set);
        }
    }

    emethod doRegisterWith(SetCollection set) {
        int count = set.size();
        EConnectionRecordException problems = new EConnectionRecordException("Process Location Server" + ((count > 1) ? "s" : ""));
        
        Enumeration en = set.enumerate();
        while (en.hasMoreElements()) {
            SturdyRef ref = (SturdyRef)en.nextElement();
            if (tr.debug && Trace.ON) tr.$("registering with PLS at " + ref.myRemoteRID);
            EConnection conn = ref.getEConnection();
            // XXX start watchdog to shutdown conn
            etry {
                ProcessLocationServer plsChannel = (ProcessLocationServer) EUniChannel.construct(ProcessLocationServer.class);
                EUniDistributor plsChannel_dist = EUniChannel.getDistributor(plsChannel);
                ref.followRef(plsChannel_dist);
                ewhen plsChannel (ProcessLocationServer pls) {
                    if (tr.verbose && Trace.ON) tr.$("sending register to PLS at " + ref.myRemoteRID);
                    pls <- register(myRegistrarID, myListenAddr);
                    if (conn != null && myShutdownConnections) {
                        this <- shutdownConnection(conn, false);
                    }
                }
            }
            ecatch (Throwable t) {
                if (tr.debug && Trace.ON) tr.$("problem registering with PLS at " + ref.myRemoteRID + ": " + t);
                problems.addThrowable(ref.myRemoteSearchPath[0] + "/" + ref.myRemoteRID + "/" + ref.myRemoteObjectID, t);
                count--;
                if (count == 0) {
                    myRegistrar.noticeProblem(problems);
                }
                if (conn != null) {
                    this <- shutdownConnection(conn, false);
                }
            }
        }
    }

    emethod unRegisterWith(SetCollection set, CompletionNoticer noticer, Boolean hibernating) {
        boolean needToNotice = false;
        
        if (noticer != null) {
            myShutdownNoticer = new CompletionNoticerJoin(noticer, hibernating, null);
            needToNotice = true;
        }
        Enumeration en = set.enumerate();
        while (en.hasMoreElements()) {
            SturdyRef ref = (SturdyRef)en.nextElement();
            if (tr.debug && Trace.ON) tr.$("unregistering with PLS at " + ref.myRemoteRID);
            EConnection conn = ref.getEConnection();
            if (conn != null && needToNotice) {
                myShutdownNoticer.addElement(conn);
            }
            etry {
                ProcessLocationServer plsChannel = (ProcessLocationServer) EUniChannel.construct(ProcessLocationServer.class);
                EUniDistributor plsChannel_dist = EUniChannel.getDistributor(plsChannel);
                ref.followRef(plsChannel_dist);
                ewhen plsChannel (ProcessLocationServer pls) {
                    pls <- unregister(myRegistrarID);
                    if (conn != null) {
                        this <- shutdownConnection(conn, needToNotice);
                    }
                }
            }
            ecatch (Throwable t) {
                // ignore any problems unregistering.  registration should time out.
                if (conn != null) {
                    this <- shutdownConnection(conn, needToNotice);
                }
            }
        }
        if (myShutdownNoticer != null) {
            this <- enableShutdownNoticer();
        }
    }

    emethod enableShutdownNoticer() {
        if (myShutdownNoticer != null) {
            if (tr.debug && Trace.ON) tr.$("enabling shutdown noticer " + myShutdownNoticer);
            myShutdownNoticer.enable();
        }

    }

    emethod checkShutdown() {
        EStdio.out().println("noticer: " + myShutdownNoticer);
    }
    
    emethod shutdownConnection(EConnection conn, boolean needToNotice) {
        if (needToNotice) {
            if (tr.debug && Trace.ON) tr.$("shutting down connection " + conn + " noticer " + myShutdownNoticer);
            conn.shutdownConnection(myShutdownNoticer);
        }
        else {
            if (tr.debug && Trace.ON) tr.$("shutting down connection " + conn);
            conn.shutdownConnection(null);
        }
    }
}
