package ec.e.timer;

import ec.util.EThreadGroup;
import ec.util.NestedException;

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
 * provide two instances, one whose timeouts survive quakes, and one whose timeouts
 * do not. <p>
 *
 * The TimerThreadOwner constructor takes a QuakeProof flag which indicates whether
 * or not the TimerThreadOwner's timeouts survive quakes. This flag is used
 * to set a state variable, and to determine whether on construction (and revival
 * from a quake) if the TimerThreadOwner should share the TimerThread's queue of
 * events, such that on subsequent revivals, a new TimerThread can be created
 * maintaining the same queue of timeouts. <p>
 *
 * Note that the actual TimerThreadOwner Objects always survive a quake (so
 * references to them are valid after revival), even if the queue of timeouts
 * they maintain does not. <p>
 *
 * This class has no public API but is documented to describe the behavior of
 * instances after revival from a quake.
 *
 * @see ec.e.timer.ClockController
 * @see ec.e.timer.Timer
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
            System.err.println(violated);
            EThreadGroup.reportException(e);
            throw new NestedException(violated, e);
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
 * @see ec.e.timer.TimerThreadOwner
 * @see #TheQuakeProofTimer
 */
 
public final class Timer extends TimerThreadOwner implements Syncologist
{
    /** The one instance of the QuakeProof Timer */
    private static Timer QuakeProofTimer;
    /** The one instance of the Smashing Timer   */
    private static Timer SmashingTimer;
                        
    /**
     * Static method to initialize the Timers. Shouldn't be
     * called from user code, only called once at startup
     */
    // XXX - Need to make these sturdy roots?   
    public static void makeTheTimers (EEnvironment env) {
        if (QuakeProofTimer != null)  {
            throw new Error("Attempt to make Vat Timers twice");
        }
        QuakeProofTimer = new Timer(env, true);
        SmashingTimer = new Timer(env, false);
    }
    
    /**
     * Returns the QuakeProofTimer whose timeouts survive quakes
     */ 
    public static Timer TheQuakeProofTimer () {
        return QuakeProofTimer;     
    }
    
    /**
     * Returns the SmashingTimer whose timeouts get smashed in quakes
     */ 
    public static Timer TheSmashingTimer () {
        return SmashingTimer;       
    }
  
    /**
     * Forces the E Runloop to get pinged in n milliseconds
     */ 
    public static void Ping (int millis)  {
        EBoolean dummy = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor dummy_dist = EUniChannel.getDistributor(dummy);
        QuakeProofTimer.setTimeout(millis, etrue, dummy_dist);
    }
  
    /**
     * Sets a timeout for the specified number of milliseconds. After the timer
     * expires, dist is forwarded to target (dist <- forward(target)).
     * Returns a unique timer id that can be used to cancel the timeout.
     */ 
    // XXX - Bug since hand Vat Objects to Crew without holding onto them ourself?
    // (Only for Smashing, QuakeProof implicitly holds via entryInfo)   
    public int setTimeout (long millis, EObject target, EResult dist) {
        RtTimerThread timerThread = getCrew();
        return timerThread.setTimeout(false, millis, target, dist);
    }

    /**
     * Cancels timeout indicated by unique tid. Tid is a value
     * returned from a prior call to setTimeout()
     */ 
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
    
    /**
     * This method is only called by the superclass in order to
     * get a String name for the Thread created on behalf of a
     * Timer instance.
     */ 
    protected String threadName() {
        if (myQuakeProof)  {
            return "QuakeProofVatTimer";
        }
        else {
            return "SmashingVatTimer";
        }
    }   
    
    /**
     * Private constructor
     */ 
    private Timer (EEnvironment env, boolean quakeProof) {
        super(env, quakeProof); 
    }   
}   

/**
 * The ClockController class provides two ClockControllers to guests in the Vat, 
 * a QuakeProof ClockController and a Smashing ClockController. <p>
 *
 * A ClockController is used to create Clocks which can be used by guests in the
 * Vat. A QuakeProof ClockController's Clocks will survive quakes (as would be
 * expected) in the same state they were in before the quake (running or stopped). <p>
 *
 * A SmashingClockController will terminate all of its Clocks such that they cannot
 * be used after revival from a quake, although they can still be referenced.
 *
 * @see ec.e.timer.TimerThreadOwner
 */
public final class ClockController extends TimerThreadOwner implements Syncologist
{
    /** The one instance of the QuakeProof ClockController */
    private static ClockController QuakeProofClockController;
    /** The one instance of the Smashing ClockController   */
    private static ClockController SmashingClockController;
        
    /** Used to keep track of all Clocks */ 
    private Vector clocks = new Vector();
        
    /**
     * Static method to initialize the ClockControllers. Shouldn't be
     * called from user code, only called once at startup
     */         
    // XXX - Need to make these sturdy roots?   
    public static void makeTheClockControllers (EEnvironment env) {
        if (QuakeProofClockController != null)  {
            throw new Error("Attempt to make Vat Clock Controllers twice");
        }
        QuakeProofClockController = new ClockController(env, true);
        SmashingClockController = new ClockController(env, false);
    }
 
    /**
     * Returns the QuakeProofClockController whose clocks survive quakes
     */     
    public static ClockController TheQuakeProofClockController () {
        return QuakeProofClockController;       
    }

    /**
     * Returns the SmashingClockController whose clocks are all stopped
     * after revival from a quake.
     */         
    public static ClockController TheSmashingClockController () {
        return SmashingClockController;     
    }
    
    /**
     * Returns a new Clock using the given resolution as the tick interval,
     * sending tick notifications to the target with arg passed to the 
     * handleTick() method
     */
    public Clock newClock (long resolution, ETickHandling target, Object arg) {
        Clock c = new Clock(resolution, target, arg, this);
        clocks.addElement(c);
        return c;
    }
    
    /**
     * Terminates the Clock such that it cannot be used
     * anymore, freeing resources allocated to the Clock
     */ 
    /* package */ void terminateClock (Clock clock) {
        clocks.removeElement(clock);
    }
    
    /**
     * Package method used by any Clocks owned by this Controller. When 
     * a Clock is started, it sets a repeating timeout for millis. The Clock
     * itself is the target to get the timeouts which it converts into ticks
     * and sends off the the ETickHandling target.
     */ 
    /* package */ int setTimeout (long millis, RtTimeoutHandling target) {
        RtTimerThread timerThread = getCrew();
        return timerThread.setTimeout(true, millis, target, null);
    }

    /**
     * Package method used by any Clocks owned by this Controller. When
     * a Clock is stopped, it cancels the repeating timeout it had
     * previously set when it was started.
     */ 
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
       
    /**
     * This method is only called by the superclass in order to
     * get a String name for the Thread created on behalf of a
     * ClockController instance.
     */  
    protected String threadName() {
        if (myQuakeProof)  {
            return "QuakeProofVatClockController";
        }
        else {
            return "SmashingVatClockController";
        }
    }   
    
    /**
     * Private constructor
     */ 
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
 * Clocks created by the SmashingClockController are always terminated after
 * revival from a Quake. Clocks created by the QuakeProofClockController
 * are revived from a Quake in whatever state (running, stopped, terminated)
 * they were in before the Quake.
 *
 * A Clock is bound to the TimerThread of the ClockController that creates
 * it, rather than creating a separate TimerThread for each Clock. <p>
 *
 * @see ec.e.timer.ClockController
 * @see ec.e.timer.ETickHandling
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
    
    /**
     * Package private Constructor, called by ClockController's newClock() method
     */ 
    /* package */ Clock (long res, ETickHandling target, Object arg, ClockController cc) {
        myResolution = res;
        myTid = -1;
        myTicks = 0;
        myTarget = target;
        myArg = arg;
        myClockController = cc;
    }
    
    /**
     * Starts the Clock from the current tick.
     * If the Clock has been terminated, raises a TerminatedClockException.
     */ 
    public synchronized void start () {
        start(myTicks);
    }

    /**
     * Starts the Clock, setting it's current tick to fromTick.
     * If the Clock has been terminated, raises a TerminatedClockException.
     */ 
    public void start (int fromTick) {
        if (myTid != -1) return; // XXX (GJF): Throw?
        if (myClockController == null)  {
            throw new TerminatedClockException("Attempt to start a terminated Clock");
        }
        myTicks = fromTick;
        myTid = myClockController.setTimeout(myResolution, this);
    }

    /** 
     * Stops the clock from ticking.
     */    
    public void stop () {
        if (myTid == -1) return; // XXX (GJF): Throw?
        myClockController.cancelTimeout(myTid);
        myTid = -1;
    }
    
    /**
     * Called after a Quake by the Smashing ClockController. The 
     * Smashing ClockController will forget about all its Clocks, and 
     * tell them all to terminate. This method is not called on Clocks
     * that were created by the QuakeProof ClockController.
     */ 
    /* package */ void afterQuake () {
        myTid = -1;
        myClockController = null;
    }
    
    /**
     * Sets the clock's current tick to toTick. If the Clock is
     * running, it is stopped
     */ 
    public void set (int toTick) {
        if (myTid != -1) {
            myClockController.cancelTimeout(myTid);
        }
        myTicks = toTick;
    }

    /**
     * Resets the Clock's current tick to 0
     */ 
    public void reset () {
        set(0);
    }

    /**
     * Gets the current tick
     */ 
    public int getTicks () {
        return myTicks;
    }

    /**
     * This method is only public in order to allow the Clock to leverage
     * internal logic in this package to handle timeouts directly.
     */     
    public void handleTimeout (Object arg, int id) {
        if (myTid == -1) return; // Toss it, we're stopped
        myTicks++;
        ekeep (null) {
            myTarget <- handleTick(myTicks, this, myArg);
        }
    }
    
    /**
     * Stops the Clock, and frees up resources used by the Clock.
     * Once a Clock is terminated, it can no longer be used, and will
     * throw a TerminatedClock exception if it's start method is called.
     */
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
 * @see ec.e.timer.Clock
 */
einterface ETickHandling  {
    /**
     * Called by Clocks on their targets after each tick.
     * Arg is an argument supplied by the creator of the 
     * Clock. Tick is the current tick count for the Clock,
     * and Clock is, well, the Clock.
     */
    handleTick(int tick, Clock clock, Object arg);
}

/**
 * TerminatedClockException is a special exception raised
 * when an attempt is made to start a terminated Clock.
 */
public class TerminatedClockException extends RuntimeException
{
    /** 
     * Uninteresting Constructor
     */
    /* package */ TerminatedClockException (String reason)  {
        super(reason);
    }
}   
