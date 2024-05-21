// XXX - In great reorg, change to ec.e.timer, make this CREW!!!
package ec.e.run;

import ec.e.lang.EInteger;

import java.lang.Object;
import java.lang.String;
import java.lang.Thread;
import java.util.Enumeration;
import java.util.Vector;
import java.lang.Exception;
import java.lang.NoSuchMethodError;
import java.lang.System;

public interface RtTimeoutHandling
{
    void handleTimeout(Object arg, int id);
}

//
// This is the public API to RtTimers
//
public class RtTimer
{
    // Note there is one thread per RtTimer on purpose,
    // if you just want to have one Timer thread, only
    // create one RtTimer and have everything use that...
    private RtTimerThread _thread;
    
    public RtTimer () {
        _thread = new RtTimerThread();
        _thread.start();
    }

    public RtTimer (String name) {
        _thread = new RtTimerThread(name);
        _thread.start();
    }
    
    public RtTimer (boolean debug) {
        _thread = new RtTimerThread();
        this.setDebugging(debug);
        _thread.start();
    }

    public RtTimer (String name, boolean debug) {
        _thread = new RtTimerThread(name);
        this.setDebugging(debug);
        _thread.start();
    }
        
    public int setTimeout (long millis, RtTimeoutHandling target, Object arg) {
        if (_thread == null) return -1; // XXX (GJF): Throw?
        return _thread.setTimeout(false, millis, target, arg);
    }
    
    public boolean cancelTimeout (int tid) {
        if (_thread == null) return false;  // XXX (GJF): Throw?
        return _thread.cancelTimeout(tid);
    }
    
    // This is called terminate to avoid confusion with RtClock stop method
    public void terminate () {
        if (_thread == null) return;    // XXX (GJF): Throw?
        _thread.shutdown();
        _thread = null;
    }
    
    public void setDebugging (boolean state) {
        if (_thread == null) return;    // XXX (GJF): Throw?
        _thread.setDebugging(state);
    } 
}

public interface RtTickHandling
{
    void tick(Object arg, int ticks);
}

class TimerQueueEntryInfo
{
    public RtTQEntry topEntry = null;
}   

//
// This is the public API to RtClocks
//
public class RtClock implements RtTimeoutHandling
{
    // One thread for all clocks
    private static RtTimerThread _thread = null;
    private static Vector _allClocks = new Vector();
    private long _resolution;       // Milliseconds for each tick
    private RtTickHandling _target; // Who gets the ticks
    private Object _arg;            // Arg passed to target in tick()
    private int _tid;               // Timer id, -1 means stopped
    private int _ticks;             // NUmber of ticks elapsed
    
    private void enforceThread () {
        synchronized (_allClocks) {
            if (_thread == null) {
                _thread = new RtTimerThread("ClockThread");
                _thread.start();
            }
            _allClocks.addElement(this);
        }
    }
    
    private void initializeClock (long resolution, RtTickHandling target, Object arg) {
        _resolution = resolution;
        _target = target;
        _arg = arg;
        _tid = -1;
        _ticks = 0;
        if (_arg == null) _arg = this;
    }
    
    public RtClock (long resolution, RtTickHandling target, Object arg) {
        initializeClock(resolution, target, arg);     
        enforceThread();
    }

    public RtClock (long resolution, RtTickHandling target, Object arg, boolean debug) {
        initializeClock(resolution, target, arg);     
        enforceThread();
        this.setDebugging(debug);
    }

    public synchronized void start () {
        start(_ticks);
    }

    public synchronized void start (int fromTick) {
        if (_tid != -1) return; // XXX (GJF): Throw?
        _ticks = fromTick; 
        _tid = _thread.setTimeout(true, _resolution, this, null);
    }
    
    public synchronized void stop () {
        if (_tid == -1) return; // XXX (GJF): Throw?
        _thread.cancelTimeout(_tid);
        _tid = -1;
    }
    
    public synchronized void set (int toTick) {
        if (_tid != -1) {
            _thread.cancelTimeout(_tid);
        }
        _ticks = toTick;
    }

    public synchronized void reset () {
        set(0);
    }

    public synchronized int getTicks () {
        return _ticks;
    }

    public synchronized void handleTimeout (Object arg, int id) {
        if (_tid == -1) return; // Toss it, we're stopped
        _ticks++;
        _target.tick(_arg, _ticks);
    }
    
    public synchronized void setTarget (RtTickHandling target) {
        _target = target;
    }
    
    public synchronized RtTickHandling getTarget () {
        return _target;
    }
    
    public synchronized void setArg (Object arg) {
        _arg = arg;
    }
    
    public synchronized Object getArg () {
        return _arg;
    }
    
    public synchronized void setResolution (long resolution) {
        _resolution = resolution;
    }
    
    public synchronized long getResolution () {
        return _resolution;
    }
    
    public void terminate () {
        stop();
        synchronized (_allClocks) {
            _allClocks.removeElement(this);
            if (_allClocks.isEmpty()) {
                _thread.shutdown();
                _thread = null;
            }
        }
        _target = null;
        _arg = null;
    }
    
    static void terminateAllClocks () {
        synchronized (_allClocks) {
            Enumeration en = _allClocks.elements();
            while (en.hasMoreElements()) {
                RtClock clock = (RtClock)en.nextElement();
                clock.stop(); // Don't terminate, mutates _allClocks!
            }
            _allClocks.removeAllElements();
            if (_thread != null) _thread.shutdown();
            _thread = null;
        }
    }
    
    public void setDebugging (boolean state) {
        if (_thread == null) return;    // XXX (GJF): Throw?
        _thread.setDebugging(state);
    }   
}

public class RtClockTerminator
{
    public static void terminateAllClocks () {
        RtClock.terminateAllClocks();
    }
}

//
// Hidden class to do the dirty work for RtTimers and RtClocks
//
class RtTimerThread extends Thread implements RtFinalizer
{
    boolean _debug = false;
    private TimerQueueEntryInfo entryInfo;
    private boolean _running = true;
    private int _tidCount = 0;
    private Object _externalLock = null;

    private final static int FUDGE = 5; // Get more than 5 repeating timeouts behind, check them    
    private static int timerThreadNumber = 0;
    ////private static Vector allActiveTimerThreads = new Vector (5, 5);
    
    private void setup (TimerQueueEntryInfo info, Object lock) {
        ////System.out.println("TimerThread is" + (isDaemon() ? " " : " not ") + "a daemon");
        this.setPriority(MAX_PRIORITY);
        if (lock == null)  {
            _externalLock = new Object();
        }
        else {
            _externalLock = lock;
        }
        if (info == null)  {
            entryInfo = new TimerQueueEntryInfo();
        } 
        else {
            entryInfo = info;
            ///orderEntries();
        }
    }
    
    private RtTimerThread (String name, TimerQueueEntryInfo info, Object lock, boolean ignored) {
        super(name);
        this.setup(info, lock);
    }
        
    RtTimerThread () {
        this("TimerThread (" + timerThreadNumber + ")", null, null, false);
        timerThreadNumber++;
    }

    RtTimerThread (String name) {
        this("TimerThread for " + name, null, null, false);
    }
    
    RtTimerThread (TimerQueueEntryInfo info, Object lock) {
        this("TimerThread (" + timerThreadNumber + ")", info, lock, false);
        timerThreadNumber++;
    }

    RtTimerThread (String name, TimerQueueEntryInfo info, Object lock) {
        this("TimerThread for " + name, info, lock, false);
    }
    
    synchronized void setDebugging (boolean state) {
        _debug = state;
    }
    
    private synchronized void orderEntries()  {
        synchronized(_externalLock)  {
            RtTQEntry newEntry = null;
            RtTQEntry entry;
            RtTQEntry previous;
            if (_debug) System.out.println("Timer loop reordering queue");
            while (entryInfo.topEntry != null) {
                RtTQEntry winner = entryInfo.topEntry;
                RtTQEntry before = null;
                entry = entryInfo.topEntry._next;
                previous = null;
                while (entry != null) {
                    if (entry._when > winner._when) {
                        winner = entry;
                        before = previous;
                    }
                    previous = entry;
                    entry = entry._next;
                }
                if (before != null) {
                    before._next = winner._next;
                }
                else {
                    entryInfo.topEntry = winner._next;
                }
                winner._next = newEntry;
                newEntry = winner;
                winner = before = null; // Don't trust gc
            }
            entryInfo.topEntry = newEntry;
        } // end synchronized _externalLock
    }
        
    public void run () {
        while (_running) {
            runloop();
        }
        // We've been stopped, clean up and terminate
        entryInfo = null;
    }
    
    private void runloop () {
        long time;
        boolean reorder;
        Vector notifies = null;
        RtTQEntry entry;
        RtTQEntry previous;
            
        synchronized (this) {
            if (entryInfo.topEntry != null) {
                time = (entryInfo.topEntry._when - System.currentTimeMillis()) | 1; // Avoid 0 since will wait forever
            }
            else {
                /*
                synchronized(allActiveTimerThreads) {
                    if (_debug) System.out.println(this + " removing self from allActiveTimerThreads");
                    allActiveTimerThreads.removeElement(this);
                }
                */
                time = 0;
            }
            try {
                if (time >= 0) {
                    if (_debug) {
                        if (time > 0) System.out.println("Sleeping at " + System.currentTimeMillis() + " for " + time + " millis");
                        else System.out.println("Suspending, no entries");
                    }
                    this.wait(time);
                    if (_debug) System.out.println("Timer loop woke up");
                }
                else if (_debug) {
                    System.out.println("No need to sleep");
                }
            }
            catch (Exception e) {
                if (_debug) System.out.println("Notified");
                // No problem - something added or cancelled from queue
            }

            // Only do the next bunch 'o stuff if this timer is still running
            if (_running) {
                // Timer fired, check each element to see if it is time
                long now = System.currentTimeMillis();
                if (_debug) System.out.println("Looking for timeouts, now is "  + now);
                reorder = false;
                entry = entryInfo.topEntry;
                previous = null;
                while (entry != null) {
                    if (entry._when <= now) {
                        if (_debug) System.out.println("Found a timeout, _when is " + entry._when);
                        synchronized(_externalLock)  {
                            if (entry._dist != null) {
                                RtEnvelope env;
                                env <- EDistributor.forward((EObject)entry._nobj);
                                RtRun.enqueue(entry._dist, env, null);
                            }
                            else {
                                if (notifies == null) notifies = new Vector(5, 5);
                                notifies.addElement(entry);
                            }
                            if (entry._repeat) {
                                reorder = true;
                                entry._when = entry._when + entry._delta;
                                if ((entry._when + (entry._delta * FUDGE)) < now) {
                                    // Round up in increments of entry._delta to
                                    // maintain timebase, but _delta from "now"
                                    // "now" being rounded up to the timebase
                                    long dist = (now - entry._when) + entry._delta;
                                    dist = (dist / entry._delta) * entry._delta;
                                    entry._when = entry._when + dist;
                                }
                                if (_debug) System.out.println("Found repeating timer, resetting previous");
                                previous = entry;
                            }
                            else { // Remove it
                                if (previous == null) {
                                    entryInfo.topEntry = entry._next;
                                }
                                else {
                                    previous._next = entry._next;
                                }
                            }
                        } // end synchronized _externalLock
                        entry = entry._next;
                    }
                    else {
                        break;
                    }
                } // end while
                if (reorder) {
                    orderEntries();
                }
            } // end if running
        } // end synchronized this
    
        if (_running == false) {
            if (_debug) System.out.println("Timer loop stopped, exiting");
            return;
        }
            
        // Enumerate over notifies and notify them
        if (notifies != null) {
            int count = notifies.size();
            int i = 0;
            while (_running && (i < count)) {
            entry = (RtTQEntry)notifies.elementAt(i++);
                RtTimeoutHandling target = (RtTimeoutHandling)entry._nobj;
                try {
                    target.handleTimeout(entry._arg, entry._tid);
                } catch (Exception e) {
                    System.err.println("Exception in HandleTimeout method");
                    e.printStackTrace();
                }
                target = null; // Don't trust gc scan
            }
            // Cleanup the notify objects ...
            if (_debug) System.out.println("Timer loop cleaning up notifies");
            notifies.removeAllElements();
            notifies = null;
        }
        // Do some cleanup so no dangling references prevent gc ...
        if (_debug) System.out.println("Timer loop cleaning up stack");
        entry = null;
        previous = null;
    }

    private synchronized void insertEntry(RtTQEntry newEntry) {
        synchronized(_externalLock)  {
            RtTQEntry previous = null;
            RtTQEntry entry = entryInfo.topEntry;
            
            while (entry != null) {
                //if (_debug) System.out.println("Comparing " + newEntry._when + " to " + entry._when);
                if (newEntry._when <= entry._when) {
                    //if (_debug) System.out.println("New entry goes before");
                    break;
                }
                previous = entry;
                entry = entry._next;
            }
            if (_debug) {
                if (entry == null) {
                    //System.out.println("Passed all the entries");
                }
            }
            if (previous == null) {
                newEntry._next = entryInfo.topEntry;
                entryInfo.topEntry = newEntry;
            }
            else {
                newEntry._next = previous._next;
                previous._next = newEntry;
            }
            previous = null; entry = null; newEntry = null; // gc screwing up scanning down stack?
        }
    }

    private synchronized void wakeup () {
        try {
            if (_debug) System.out.println("Waking myself up");
            this.notify();
        }
        catch (Throwable ignored) {
            if (_debug) {
                System.out.println("Wakeup caught exception on notify: " + ignored);
            }
        }
    }
    
    private synchronized int _setTimeout (boolean repeat, long millis, Object target, Object arg, 
        EDistributor dist) {
        int tid = ++_tidCount;
        if (entryInfo.topEntry == null) {
            /*
            synchronized(allActiveTimerThreads) {
                if (_debug) System.out.println(this + " adding self to allActiveTimerThreads");
                allActiveTimerThreads.addElement(this);
            }
            */
        }
        RtTQEntry entry = new RtTQEntry (repeat, millis, target, arg, dist, tid);
        if (_debug) 
            System.out.println("Adding entry (now is " + (entry._when - millis) + ") for " + millis + " millis, will go off " + entry._when);
        this.insertEntry(entry);
        if (entryInfo.topEntry == entry) {
            this.wakeup();
        }
        entry = null; // In case gc scans down Java stack too far ...
        return tid;
    }
    
    int setTimeout (boolean repeat, long millis, RtTimeoutHandling target, Object arg) {
        return _setTimeout (repeat, millis, target, arg, null);
    }
    
    int setTimeout (boolean repeat, long millis, EObject target, EDistributor dist) {
        return _setTimeout (repeat, millis, target, null, dist);
    }
        
    synchronized boolean cancelTimeout (int tid) {
        synchronized(_externalLock)  {
            RtTQEntry entry;
            RtTQEntry previous;
            if (entryInfo.topEntry == null) {
                return false;
            }
            if (entryInfo.topEntry._tid == tid) {
                entry = entryInfo.topEntry;
                entryInfo.topEntry = entryInfo.topEntry._next;
            }
            else {
                previous = entryInfo.topEntry;
                entry = entryInfo.topEntry._next;
                while (entry != null) {
                    if (entry._tid == tid) {
                        previous._next = entry._next;
                        break;
                    }
                    previous = entry;
                    entry = entry._next;
                }
            }
            if (entry != null) {
                entry._next = null;
                return true;
            }
            else {
                return false;
            }
        }
    }
    
    synchronized void shutdown() {
        if (_debug) System.out.println("TimerThread " + this + " shutting down");
        _running = false;
        wakeup();
    }
    
    // XXX - unfortunately, until Java learns more better about weak pointers,
    // it appears something keeps references to all threads, and this won't get called!
    protected void finalize () {
        RtRun.queueReallyFinalize(this);    
    }
    
    public void reallyFinalize () {
        if (_debug) System.out.println(this + " shutting down in reallyFinalize");
        shutdown();
    }
}

class JavaIsStupidTimerThread extends RtTimerThread implements JavaIsStupidFinalizer
{
    JavaIsStupidTimerThread() {
    }
    
    public void stupidFinalize() {
        super.finalize();
    }
    public void reallyFinalize() {
    }
}

class RtTQEntry 
{
    boolean _repeat;
    long _delta;
    long _when;
    Object _nobj;
    Object _arg;
    EDistributor _dist;
    int _tid;
    RtTQEntry _next;
    
    RtTQEntry (boolean repeat, long delta, Object nobj, Object arg, EDistributor dist, int tid) {
        _repeat = repeat;
        _delta = delta;
        _when = System.currentTimeMillis() + delta;
        _arg = arg;
        _dist = dist;
        _nobj = nobj;
        _tid = tid;
        _next = null;
        //System.out.println(this + " created");
    }
    
    protected void finalize () {
        //System.out.println(this + " being finalized");
    } 
}

