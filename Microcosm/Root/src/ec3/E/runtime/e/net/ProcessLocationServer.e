package ec.e.net;

import ec.trace.Trace;
import ec.util.NestedException;
import ec.e.run.Vat;
import ec.e.run.Seismologist;
import ec.e.run.TimeQuake;
import ec.e.run.EEnvironment;
import ec.e.run.FragileRootHolder;
import ec.e.net.NetworkListener;
import ec.e.net.NetworkConnection;
import ec.e.net.ByteListener;
import ec.e.util.ExpireCollection;
import ec.e.util.ExpireCollectionEnumeratorEntry;
import ec.e.file.EStdio;
import ec.e.lang.EString;
import ec.e.timer.Timer;
import java.util.Enumeration;

eclass ProcessLocationServer {
    private ProcessLocationServerHelper myHelper;
    private EString myListenAddr;
    
    public ProcessLocationServer(EEnvironment env, String listenAddr, DoCommit committer) {
        myHelper = new ProcessLocationServerHelper(env, listenAddr, committer);
        myListenAddr = new EString(listenAddr);
    }

    public ProcessLocationServer(ProcessLocationServerHelper helper, String listenAddr) {
        myHelper = helper;
        myListenAddr = new EString(listenAddr);
    }
    
    emethod register(String registrarID, String searchPath) {
        myHelper.register(registrarID, searchPath);
    }

    emethod unregister(String registrarID) {
        myHelper.unregister(registrarID);
    }

    emethod getSearchPathElement(EResult result) {
        result <- forward(myListenAddr);
    }

    protected Object value() {
        return this;
    }
}

eclass PLSExpireHelper implements Seismologist {
    static private final Trace tr = new Trace("ec.e.net.PLSExpireHelper");
    private ProcessLocationServerHelper myPLSHelper;
    private Timer myTimer;
    private Vat myVat;
    private FragileRootHolder myMeHolder;
    
    PLSExpireHelper(ProcessLocationServerHelper plsHelper, Vat vat) {
        if (tr.debug && Trace.ON) tr.debugm("creating new PLSExpireHelper " + this);
        myPLSHelper = plsHelper;
        myVat = vat;
        this <- noticeQuake(null);
    }

    emethod noticeQuake(TimeQuake quake) {
        if (tr.debug && Trace.ON) tr.debugm("noticed a quake " + this);
        myMeHolder = myVat.makeFragileRoot(this);
        myTimer = Timer.TheSmashingTimer();

        // don't send an expire the first time.  It would be left on
        // the queue when we hibernate, resulting in two calls to
        // expire() on revival (this one and the quake one)
        if (quake != null) {
            this <- expire();
        }
        
    }

    emethod noticeCommit() {
        if (tr.debug && Trace.ON) tr.debugm("noticed a commit " + this);
    }
    
    emethod expire() {
        if (tr.debug && Trace.ON) tr.debugm("got expire() " + this);
        EBoolean tick = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor tick_dist = EUniChannel.getDistributor(tick);
        myPLSHelper.expire();
        myTimer.setTimeout(ProcessLocationServerHelper.RegistrationTick, etrue, tick_dist);
        ewhen tick (boolean ignored) {
            if (tr.debug && Trace.ON) tr.debugm("got timer tick, sending expire() " + this);
            this <- expire();
        }
    }
}

public einterface DoCommit {
    emethod commit();
}

public class ProcessLocationServerHelper implements NetworkListener
{
    static private final Trace tr = new Trace("ec.e.net.ProcessLocationServerHelper");

    static private final boolean check = false;

    private long myLastNewRegistrations = 0;
    private long myNewRegistrations = 0;
    private long myLastReRegistrations = 0;
    private long myReRegistrations = 0;
    private long myLastRegistrationCount = 0;
    private long myRegistrationCount = 0;
    private long myLastDeadUnRegistrations = 0;
    private long myDeadUnRegistrations = 0;
    private long myLastLiveUnRegistrations = 0;
    private long myLiveUnRegistrations = 0;
    private long myLastUnRegistrations = 0;
    private long myUnRegistrations = 0;
    private long myLastUnsuccessfulGets = 0;
    private long myUnsuccessfulGets = 0;
    private long myLastSuccessfulGets = 0;
    private long mySuccessfulGets = 0;
    private long myLastGets = 0;
    private long myGets = 0;
    private long myLastExpired = 0;
    private long myExpired = 0;
    private long myLastSize = 0;
    private long newSize = 0;
    
    private String myListenAddr;
    private NetworkListener myByteListener;
    private ExpireCollection myRegistrations;
    private Registrar myRegistrar;
    private Vat myVat;
    private DoCommit myCommitter;
    private long now;

    // Reregistration (in RegistrarHelper) must occur before the
    // registration times out.  It's currently set to 50 minutes.
    static final long RegistrationDuration = 60 ; //    60 ticks        == 1 hour
    static final long RegistrationTick = 60000;   // 60000 milliseconds == 1 minute
    
    // listenAddr should specify a stable port, not 0
    public ProcessLocationServerHelper(EEnvironment env, String listenAddr, DoCommit committer) {
        try {
            myRegistrar = Registrar.summon(env);
        }
        catch (Exception e) {
            throw new NestedException("cannot summon Registrar", e);
        }
        myCommitter = committer ;
        myVat = env.vat();
        myListenAddr = listenAddr;
        myByteListener = new ByteListener(myVat, listenAddr, (NetworkListener)this);
        myRegistrations = new ExpireCollection();
        now = 0;
        new PLSExpireHelper(this, myVat);
    }

    // XXX need box/unbox protocol to make this secure
    void register(String registrarID, String searchPath) {
        if (tr.debug && Trace.ON) tr.$("register(" + registrarID + ", " + searchPath + ")");
        Object old = myRegistrations.put(registrarID, searchPath, now + RegistrationDuration);
        if (check) myRegistrations.consistancyCheck(EStdio.err());
        if (old == null) {
            myNewRegistrations++;
        }
        else {
            myReRegistrations++;
        }
        myRegistrationCount++;
    }

    void unregister(String registrarID) {
        Object old = myRegistrations.remove(registrarID);
        if (check) myRegistrations.consistancyCheck(EStdio.err());
        if (tr.debug && Trace.ON) tr.$("unregister(" + registrarID + "), was " + old);
        if (old == null) {
            myDeadUnRegistrations++;
        }
        else {
            myLiveUnRegistrations++;
        }
        myUnRegistrations++;
    }

    String get(String registrarID) {
        String ret = (String)myRegistrations.get(registrarID);
        if (tr.debug && Trace.ON) tr.$("get(" + registrarID + ") == " + ret);
        if (ret == null) {
            myUnsuccessfulGets++;
        }
        else {
            mySuccessfulGets++;
        }
        myGets++;
        return ret;
    }
    
    void expire() {
        if (tr.verbose && Trace.ON) tr.$("expire " + now);
        int oldSize = myRegistrations.size();
        myRegistrations.expire(now);
        if (check) myRegistrations.consistancyCheck(EStdio.err());
        int newSize = myRegistrations.size();
        int expired = oldSize - newSize;
        myExpired += expired;
        if (tr.verbose && Trace.ON) tr.$("removed " + expired + " entries, leaving " + newSize + " total entries");
        if ((now % 60) == 0) {
            // report stats once an hour
            reportStats((long)newSize);
        }
        if ((now % 5) == 0) {
            // commit every 5 min
            myCommitter <- commit();
        }
        now++;
    }

    void reportStats(long newSize) {
        long NewRegistrations = myNewRegistrations - myLastNewRegistrations;
        myLastNewRegistrations = myNewRegistrations;
        
        long ReRegistrations = myReRegistrations - myLastReRegistrations;
        myLastReRegistrations = myReRegistrations;
        
        long RegistrationCount = myRegistrationCount - myLastRegistrationCount;
        myLastRegistrationCount = myRegistrationCount;
        
        long DeadUnRegistrations = myDeadUnRegistrations - myLastDeadUnRegistrations;
        myLastDeadUnRegistrations = myDeadUnRegistrations;
        
        long LiveUnRegistrations = myLiveUnRegistrations - myLastLiveUnRegistrations;
        myLastLiveUnRegistrations = myLiveUnRegistrations;
        
        long UnRegistrations = myUnRegistrations - myLastUnRegistrations;
        myLastUnRegistrations = myUnRegistrations;
        
        long UnsuccessfulGets = myUnsuccessfulGets - myLastUnsuccessfulGets;
        myLastUnsuccessfulGets = myUnsuccessfulGets;
        
        long SuccessfulGets = mySuccessfulGets - myLastSuccessfulGets;
        myLastSuccessfulGets = mySuccessfulGets;
        
        long Gets = myGets - myLastGets;
        myLastGets = myGets;

        long Expired = myExpired - myLastExpired;
        myLastExpired = myExpired;

        long Size = newSize - myLastSize;
        myLastSize = newSize;

        if (tr.event && Trace.ON) {
            tr.$("PLS Statistics, now = " + now);
            tr.$("   NewRegistrations = " + myNewRegistrations + " (" + NewRegistrations + "/hour)");
            tr.$("    ReRegistrations = " + myReRegistrations + " (" + ReRegistrations + "/hour)");
            tr.$("  RegistrationCount = " + myRegistrationCount + " (" + RegistrationCount + "/hour)");
            tr.$("DeadUnRegistrations = " + myDeadUnRegistrations + " (" + DeadUnRegistrations + "/hour)");
            tr.$("LiveUnRegistrations = " + myLiveUnRegistrations + " (" + LiveUnRegistrations + "/hour)");
            tr.$("    UnRegistrations = " + myUnRegistrations + " (" + UnRegistrations + "/hour)");
            tr.$("   UnsuccessfulGets = " + myUnsuccessfulGets + " (" + UnsuccessfulGets + "/hour)");
            tr.$("     SuccessfulGets = " + mySuccessfulGets + " (" + SuccessfulGets + "/hour)");
            tr.$("               Gets = " + myGets + " (" + Gets + "/hour)");
            tr.$("            Expired = " + myExpired + " (" + Expired + "/hour)");
            tr.$("Total Registrations = " + newSize + " (" + Size + "/hour)");
        }
    }
    

    // start of NetworkListener interface
    public void listening(String listenAddr) {
        // ignore
    }
    
    public void noticeConnection(NetworkConnection outer, String localAddr, String remoteAddr) {
        new PLSConnection(this, outer, localAddr, remoteAddr);
    }
    
    public void noticeProblem(Throwable t, boolean listenProblem) {
        // XXX probably want to hand this out to someone
        EStdio.reportException(t, true);
    }
    
    public void shutdown() {
        myByteListener.shutdown();
    }

    public void suspend() {
        myByteListener.suspend();
    }

    public void resume() {
        myByteListener.resume();
    }

    /* package */ void ctl_command(String line, EResult result) {
            String tokens[] = new String[3];
            int i = 0 ;
            while (i < 3) {
                int space = line.indexOf(' ');
                if (space < 0) {
                    tokens[i] = line;
                    break;
                }
                tokens[i] = line.substring(0, space);
                line = line.substring(space+1);
                i++;
            }
            
            if (tokens[0].equals("connect") || line.equals("")) {
                if (tr.debug && Trace.ON) tr.$("ctl_connect");
                result <- forward(new EString("connected to PLS listening at " + myListenAddr));
            }
            else if (tokens[0].equalsIgnoreCase("dump")) {
                if (tr.debug && Trace.ON) tr.$("ctl_dump");
                result <- forward (new EString("entries currently registered at " + myListenAddr + "\n[" + now + "] <-- now")) ;
                Enumeration en = myRegistrations.enumerate();
                while (en.hasMoreElements()) {
                    ExpireCollectionEnumeratorEntry ecee = (ExpireCollectionEnumeratorEntry)en.nextElement();
                    result <- forward(new EString("[" + ecee.expirationDate + "] " + ecee.key + " --> " + ecee.value + ".")) ;
                }
                result <- forward(new EString("end of registrations"));
            }
            else if (tokens[0].equalsIgnoreCase("lookup")) {
                // Interceptor.e depends on the format of this command, and it's reply
                if (tokens[1] == null) {
                    result <- forward(new EString("lookup requires <rid> argument"));
                }
                else {
                    if (tr.debug && Trace.ON) tr.$("ctl_lookup " + tokens[1]);
                    String ret = (String)myRegistrations.get(tokens[1]);
                    result <- forward(new EString(tokens[1] + " --> " + ret + "."));
                }
            }
            else if (tokens[0].equalsIgnoreCase("remove")) {
                if (tokens[1] == null) {
                    result <- forward(new EString("remove requires <rid> argument"));
                }
                else {
                    if (tr.debug && Trace.ON) tr.$("ctl_remove " + tokens[1]);
                    unregister(tokens[1]); // XXX will need other code when this verifies signature
                    result <- forward(new EString("unregistered " + tokens[1]));
                }
            }
            else if (tokens[0].equalsIgnoreCase("add")) {
                if (tokens[1] == null || tokens[2] == null) {
                    result <- forward(new EString("add requires <rid> and <addr> arguments"));
                }
                else {
                    if (tr.debug && Trace.ON) tr.$("ctl_add " + tokens[1] + ", " + tokens[2]);
                    register(tokens[1], tokens[2]); // XXX will need other code when this verifies signature
                    result <- forward(new EString("registered " + tokens[1] + " at " + tokens[2]));
                }
            }
            else {
                result <- forward(new EString("unrecognized command: " + tokens[0]));
            }
    }
}

eclass PLSControllerServer {
    private PLSControllerHelper myHelper;

    public PLSControllerServer(PLSControllerHelper helper) {
        myHelper = helper;
    }

    emethod command(String line, EResult result) {
        myHelper.command(line, result);
    }
}

public class PLSControllerHelper {
    private ProcessLocationServerHelper myHelper;

    public PLSControllerHelper(ProcessLocationServerHelper helper) {
        myHelper = helper;
    }

    public void command(String line, EResult result) {
        myHelper.ctl_command(line, result);
    }
}
