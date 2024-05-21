// XXX - In great reorg, change to ec.e.timer
package ec.e.run;

import java.util.Vector;

import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.TimeQuake;
import ec.e.start.SmashedException;
import ec.e.start.Syncologist;

/**
 * TimerThreadOwner is the base class for both the Vat Timer and Vat ClockController
 * classes. TimerThreadOwner handles restarting the crew timer thread (held on to
 * by a Tether) upon revival from a quake. Subclasses of TimerThreadOwner typically
 * provide two instances, one whose timeouts survive quakes, and one that's timeouts
 * do not.
 *
 * The TimerThreadOwner constructor takes a QuakeProof flag which indicates whether
 * or not the TimerThreadOwner's timeouts survive quakes. This flag is used
 * to set a state variable, and to determine whether on construction (and revival
 * from a quake) if the TimerThreadOwner should share the TimerThread's queue of
 * events, such that on subsequent revivals, a new TimerThread can be created
 * maintaining the same queue of timeouts.
 *
 * Note that the actual TimerThreadOwner Objects always survive a quake (so
 * references to them are valid after revival), even if the queue of timeouts
 * they maintain does not.
 *
 * This class has no public API but is documented to describe the behavior of
 * instances after revival from a quake.
 */
public abstract class TimerThreadOwner implements Syncologist
{
    /** Holds onto TimerThread  */
    protected Tether myTimerTether;
    
    /** E Environment */    
    protected EEnvironment myEnv;
    
    /** TimerQueue shared with TimerThread */   
    protected TimerQueueEntryInfo myEntryInfo;
    
    /** QuakeProof (true) or smashing (false)? */   
    protected boolean myQuakeProof; 

    /**
     * Constuctor
     */ 
    protected TimerThreadOwner (EEnvironment env, boolean quakeProof) {
        myEnv = env;
        myQuakeProof = quakeProof;
        if (myQuakeProof) {
            myEntryInfo = new TimerQueueEntryInfo();
        }
        else {
            myEntryInfo = null;
        }
        createTimerThread();
    }
    
    /**
     * Syncologist method
     */
    public void noticeQuakeSync(TimeQuake quake) {
        createTimerThread();
    }       
    
    /**
     * Informational method to provide a name for the TimerThread
     */ 
    abstract protected String threadName();
    
    /**
     * Common method to create TimerThread and set a Tether to it. As luck
     * would have it, this is also a convenient place to set up the Object
     * as a synchronous Quake noticer, so it does. The TimerThread is started
     * in this method.
     */ 
    protected void createTimerThread() {
        RtTimerThread timerThread;
        if (myQuakeProof) {
            timerThread = new RtTimerThread(threadName(), myEntryInfo, myEnv.vat().vatLock());
        }
        else {
            timerThread = new RtTimerThread(threadName());
        }
        timerThread.start();
        myTimerTether = new Tether (myEnv.vat(), timerThread);
        myEnv.vat().setSyncQuakeNoticer(this);  
    }
    
    /**
     * Gets TimerThread from Tether, throws an exception if it
     * is smashed, as the synchronous quake sync method should
     * reestablish it before this routine is ever called.
     */ 
    protected RtTimerThread getCrew() {
        RtTimerThread timerThread = null;
        try {
            timerThread = (RtTimerThread)myTimerTether.held();
        } catch (SmashedException e) {
            String violated = "Invariant violated - Vat Timer Tether's framework smashed";
            System.out.println(violated);
            e.printStackTrace();
            throw new Error(violated + ":" + e);
        }
        return timerThread;
    }   
}   

/**
 * The Timer class provides two Timers to guests in the Vat, a QuakeProof
 * Timer and a Smashing Timer <p>
 *
 * A Timer can be used to set timeouts which occur after a set amount of
 * milliseconds. When the timeout occurs, a Distributor is forwarded to an EObject.
 * Both the Distributor and EObject are provided to the call to set the timeout.
 * Timeouts do not repeat. <p>
 *
 * A Timeout can be cancelled, however there is no guarantee that the Timeout
 * has not already occured (and hence queued the forward message to the 
 * Distributor in the run queue) at the time it is cancelled.
 *
 * @see ec.e.run.TimerThreadOwner
 */
 
public final class Timer extends TimerThreadOwner implements Syncologist
{
    /** The one instance of the QuakeProof Timer */
    private static Timer QuakeProofTimer;
    /** The one instance of the Smashing Timer   */
    private static Timer SmashingTimer;
                        
    // XXX - Need to make these sturdy roots?   
    public static void makeTheTimers (EEnvironment env) {
        if (QuakeProofTimer != null)  {
            throw new Error("Attempt to make Vat Timers twice");
        }
        QuakeProofTimer = new Timer(env, true);
        SmashingTimer = new Timer(env, false);
    }
    
    public static Timer TheQuakeProofTimer () {
        return QuakeProofTimer;     
    }
    
    public static Timer TheSmashingTimer () {
        return SmashingTimer;       
    }
    
    // XXX - Bug since hand Vat Objects to Crew without holding onto them ourself?
    // (Only for Smashing, QuakeProof implicitly holds via entryInfo)   
    public int setTimeout (long millis, EObject target, EDistributor dist) {
        RtTimerThread timerThread = getCrew();
        return timerThread.setTimeout(false, millis, target, dist);
    }

    public boolean cancelTimeout (int tid) {
        RtTimerThread timerThread = getCrew();
        return timerThread.cancelTimeout(tid);
    }
    
    /**
     * Syncologist method
     */
    public void noticeQuakeSync(TimeQuake quake) {
        super.noticeQuakeSync(quake);
        if (myQuakeProof) {
            QuakeProofTimer = this;
        }
        else {
            SmashingTimer = this;
        }
    }   
    
    protected String threadName() {
        if (myQuakeProof)  {
            return "QuakeProofVatTimer";
        }
        else {
            return "SmashingVatTimer";
        }
    }   
    
    private Timer (EEnvironment env, boolean quakeProof) {
        super(env, quakeProof); 
    }   
}   

/**
 * The ClockController class provides two ClockControllers to guests in the Vat, 
 * a QuakeProof ClockController and a Smashing ClockController. <p>
 *
 * A ClockController is used to create Clocks (@see Clock) which can be used by guests
 * in the Vat. A QuakeProof ClockController's Clocks will survive quakes (as would
 * be expected) in the same state they were in before the quake (running or stopped). <p>
 *
 * A SmashingClockController will terminate all of its Clocks such that they cannot
 * be used after revival from a quake, although they can still be referenced.
 *
 * @see ec.e.run.TimerThreadOwner
 */
public final class ClockController extends TimerThreadOwner implements Syncologist
{
    /** The one instance of the QuakeProof ClockController */
    private static ClockController QuakeProofClockController;
    /** The one instance of the Smashing ClockController   */
    private static ClockController SmashingClockController;
        
    /** Used to keep track of all Clocks */ 
    private Vector clocks = new Vector();
            
    // XXX - Need to make these sturdy roots?   
    public static void makeTheClockControllers (EEnvironment env) {
        if (QuakeProofClockController != null)  {
            throw new Error("Attempt to make Vat Clock Controllers twice");
        }
        QuakeProofClockController = new ClockController(env, true);
        SmashingClockController = new ClockController(env, false);
    }
    
    public static ClockController TheQuakeProofClockController () {
        return QuakeProofClockController;       
    }
    
    public static ClockController TheSmashingClockController () {
        return SmashingClockController;     
    }
    
    public Clock newClock (long resolution, ETickHandling target, Object arg) {
        Clock c = new Clock(resolution, target, arg, this);
        clocks.addElement(c);
        return c;
    }
    
    /* package */ void terminateClock (Clock clock) {
        clocks.removeElement(clock);
    }
    
    /* package */ int setTimeout (long millis, RtTimeoutHandling target) {
        RtTimerThread timerThread = getCrew();
        return timerThread.setTimeout(true, millis, target, null);
    }

    /* package */ boolean cancelTimeout (int tid) {
        RtTimerThread timerThread = getCrew();
        return timerThread.cancelTimeout(tid);
    }
        
    /**
     * Syncologist method
     */
    public void noticeQuakeSync(TimeQuake quake) {
        super.noticeQuakeSync(quake);
        if (myQuakeProof) {
            QuakeProofClockController = this;
        }
        else {
            SmashingClockController = this;
            // Smashing ClockController terminates all of its
            // clocks upon revival and forgets about them.
            int size = clocks.size();
            for (int i = 0; i < size; i++) {
                Clock clock = (Clock)clocks.elementAt(i);
                clock.afterQuake();
            }
            clocks = new Vector();
        }
    }       
        
    protected String threadName() {
        if (myQuakeProof)  {
            return "QuakeProofVatClockController";
        }
        else {
            return "SmashingVatClockController";
        }
    }   
    
    private ClockController (EEnvironment env, boolean quakeProof) {
        super(env, quakeProof); 
    }   
}   

/**
 * A Clock is an Object which sends the message handleTick to a target EObject
 * every 'n' milliseconds. Clocks can only be created by calling the method
 * newClock on a ClockController instance. <p>
 *
 * A Clock can be started and stopped, and also terminated. If a Clock is
 * terminated, it is stopped and can no longer be started. <p>
 *
 * A Clock is bound to the TimerThread of the ClockController that creates
 * it, rather than creating a separate TimerThread for each Clock. <p>
 *
 * @see ec.e.run.ClockController
 * @see ec.e.run.ETickHandling
 */
public class Clock implements RtTimeoutHandling
{
    /** Tick resolution in milliseconds */
    private long myResolution;
    
    /** Timer ID if started */  
    private int myTid;
    
    /** Current tick number */  
    private int myTicks; 
    
    /** Target EObject */   
    private ETickHandling myTarget;
    
    /** Arg to be passed in handleTick() */ 
    private Object myArg;
    
    /** ClockController */  
    private ClockController myClockController;
    
    /* package */ Clock (long res, ETickHandling target, Object arg, ClockController cc) {
        myResolution = res;
        myTid = -1;
        myTicks = 0;
        myTarget = target;
        myArg = arg;
        myClockController = cc;
    }
    
    public synchronized void start () {
        start(myTicks);
    }

    public synchronized void start (int fromTick) {
        if (myTid != -1) return; // XXX (GJF): Throw?
        if (myClockController == null)  {
            throw new RuntimeException("Attempt to start a terminated Clock");
        }
        myTicks = fromTick;
        myTid = myClockController.setTimeout(myResolution, this);
    }
    
    public void stop () {
        if (myTid == -1) return; // XXX (GJF): Throw?
        myClockController.cancelTimeout(myTid);
        myTid = -1;
    }
    
    /**
     * After a Quake, the Smashing ClockController will forget
     * about all its Clocks, and tell us to terminate. This won't
     * be called if we're created by the QuakeProofClockController.
     */ 
    /* package */ void afterQuake () {
        myTid = -1;
        myClockController = null;
    }
    
    public synchronized void set (int toTick) {
        if (myTid != -1) {
            myClockController.cancelTimeout(myTid);
        }
        myTicks = toTick;
    }

    public synchronized void reset () {
        set(0);
    }

    public synchronized int getTicks () {
        return myTicks;
    }

    public void handleTimeout (Object arg, int id) {
        if (myTid == -1) return; // Toss it, we're stopped
        myTicks++;
        RtEnvelope envelope;
        envelope <- ETickHandling.handleTick(myTicks, this, myArg);
        RtRun.enqueue(myTarget, envelope, null);
    }
    
    public void terminate () {
        stop();
        myClockController.terminateClock(this);
        myClockController = null;
    }   
}   

/**
 * The ETickHandling einterface is implemented by E classes that
 * want to handle repeated ticks from a Clock.
 *
 * @see ec.e.run.Clock
 */
einterface ETickHandling  {
    handleTick(int tick, Clock clock, Object arg);
}
