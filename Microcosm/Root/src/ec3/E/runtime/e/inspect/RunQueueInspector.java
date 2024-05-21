package ec.e.inspect;
import java.util.*;
import ec.e.openers.*;
import ec.e.run.OnceOnlyException;
import ec.e.run.RtRun;
import ec.e.run.RtQObj;
import ec.e.run.RtQueueTimer;

/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 970710
 */

/**    
           
 * Run Queue Debugger low level support Handles uninteresting
 * events as quickly as possible.  Detects interesting events and
 * creates Runlets for them so that Runlet can take care of them.<p>

 * An interesting event here is always a minor surprise and it's
 * saved in the runQueueInspectorTopLevel vector since we cannot
 * figure out more than one level of parentage for the event (and
 * even that one may not be correct).

 */

public class RunQueueInspector implements RunQueueDebugger, RtQueueTimer {

    // Hold state values - given to setHold()

    public static final int HOLD_NONE = 0;
    public static final int HOLD_SOME = 1;
    public static final int HOLD_ALL = 2;
    public static final int PROFILE_RUNQUEUE = 3;
    public static final int PROFILE_EXECUTION = 4;
    public static int STEP_DEPTH = 200;
    public static int FINISH_DEPTH = 1000;
    public static int CHRONO_VECTOR_SIZE = 600;

    private Hashtable watchedSources = new Hashtable(200);
    private Hashtable watchedMessages = new Hashtable(200);
    private Hashtable watchedTargets = new Hashtable(200);
    private RtQ heldQ = new RtQ(); // Interesting (marked) events
    private RtQ freeQ = new RtQ(); // Uninteresting (all other) events
    private boolean holdSomeEvents = true; // If false, then we are free running
    private boolean holdAllEvents = false; // If true, then we hold all events
    public int stepCounter = 0; // Number of steps left to do, counting down.
    public int runletStepsThisInteraction = 0; // Number of steps user requested
    public Runlet stepSentinelRunlet = null; // Don't single-step above this one
    public static Runlet endOfStepsDisplayedRunlet = null; // Display this when done stepping
         
    private InspectorUI myInspectorUI = null; // Singlet instance of Inspector UI
    private static RunQueueInspector theOne = null; // Singlet instance of ourselves
    private Vector runQueueInspectorTopLevel; // Vector to hold all heldQ runlets at top level
    private boolean causalityTrace = false;
    private String prevCausalityTrace = null;
    private Object prevTarget = null; // Previous target.
    private int myHoldState = HOLD_SOME;

    private Hashtable myTimes = new Hashtable();

    public static void setHoldState(int newstate) {
        if (theOne != null) theOne.setHold(newstate);
    }

    public RunQueueInspector(InspectorUI ui, Vector topLevel) {
        myInspectorUI = ui;
        runQueueInspectorTopLevel = topLevel;
        theOne = this;
    }


    public static void showChronologically(boolean flag) {
        if (theOne == null) return;
        theOne.chronoList(flag);
    }

    void chronoList(boolean flag) {
        if (myInspectorUI == null) return;
        System.out.println("Chronological display requested");
        if (flag && runQueueInspectorTopLevel != null) {
            Vector allEvents = new Vector(CHRONO_VECTOR_SIZE + 10);
            int count = Runlet.collectEventsFromVector(runQueueInspectorTopLevel,allEvents,
                                                       CHRONO_VECTOR_SIZE);
            myInspectorUI.refreshRunqueueDisplay(allEvents);
        } else myInspectorUI.refreshRunqueueDisplay(runQueueInspectorTopLevel); // Null OK
    }

    public static void setCausalityTrace(boolean value) {
        if (theOne != null) theOne.setCausalityTraceFlag(value);
    }

    void setCausalityTraceFlag(boolean value) {
        causalityTrace = value;
    }

    public static boolean watchedSource(Object o) {
        if (theOne != null)
            return (theOne.watchedSources.get(o) != null);
        return false;
    }

    public static boolean watchedTarget(Object o) {
        if (theOne != null)
            return (theOne.watchedTargets.get(o) != null);
        return false;
    }

    /**

     * Determine whether the message has been marked as a watched
     * message. Message watch marks are one-shot deals - we discard
     * them after we detect them

     */

    public static boolean watched(RtQObj o) {

        if (theOne == null) return false; // OOO remove??
        if (theOne.watchedTargets.get(o.getTarget()) != null) {
            return true;
        }

        //         if (watchedMessages.get(o) != null) {
        //             watchedMessages.remove(o);
        //             return true;
        //         }

        return false;
    }

    public static void watchSource(Object o) {
        if (theOne != null) {
            theOne.watchedSources.put(o,o);
            System.out.println("Setting source break on " + o);
        }
    }

    public static void dontWatchSource(Object o) {
        if (theOne != null) {
            theOne.watchedSources.remove(o);
            System.out.println("Removing source break on " + o);
        }
    }

    public static void watchTarget(Object o) {
        if (theOne != null) {
            theOne.watchedTargets.put(o,o);
            System.out.println("Setting target break on " + o);
        }
    }

    public static void dontWatchTarget(Object o) {
        if (theOne != null) {
            theOne.watchedTargets.remove(o);
            System.out.println("Removing target break on " + o);
        }
    }

    public void refreshRunqueueDisplay(Object target) {

        // Some object wants to get refreshed in runqueue
        // display. Look up its runlet and then call QueueView to
        // refresh itself using the Runlet as top.

        System.out.println("refreshRunqueueDisplay in rqi is not implemented");
    }

    // XXX Synchronize on something!

    public static boolean step(Runlet runlet, Runlet displayRunlet, int steps) {
        if (theOne != null) return theOne.doStep(runlet,displayRunlet,steps);
        else return false;
    }

    public boolean doStep(Runlet runlet, Runlet displayRunlet, int steps) {
        if (stepSentinelRunlet == null) {
            stepCounter = steps;
            runletStepsThisInteraction = steps; // helps determining what to show at end
            if (runlet != null) {
                stepSentinelRunlet = runlet; // Find next to run in loop
            } else {
                stepSentinelRunlet = Runlet.nextUnRunInVector
                  (runQueueInspectorTopLevel,null,FINISH_DEPTH);
            }
            if (displayRunlet != null) endOfStepsDisplayedRunlet = displayRunlet;
            else endOfStepsDisplayedRunlet = null;
            return true;
        }
        return false;
    }

    public void setHoldUpdateUI(int level) {
        setHold(level);
        if (myInspectorUI != null) myInspectorUI.refreshHoldState(level);
    }

    public void setHold(int level) {
        myHoldState = level;

        switch (level) {
            case HOLD_NONE:
                holdSomeEvents = false;
                break;
            case PROFILE_RUNQUEUE:
                holdSomeEvents = false;
                holdAllEvents = false;
                break;
            case HOLD_ALL:
                holdSomeEvents = true;
                holdAllEvents = true;
                break;
            default:
                holdSomeEvents = true;
                holdAllEvents = false;
        }
    }

    public static void releaseRunlet(Runlet runlet) {
        if (theOne != null) {
            theOne.releaseAllDescendants(runlet);
        }
    }

    private void releaseAllDescendants(Runlet runlet) {
        runlet.releaseAllDescendants(freeQ);
    }

    private boolean moveToQ(RtQ target, RtQ source) {
        if (source.empty()) return false;
        while (! source.empty()) target.enqueue(source.dequeue());
        return true;
    }

    private boolean splitQ(RtQ realQ, RtQ markedQ, RtQ freeQ) {
        if (realQ.empty()) return false; // Optimize a common case

        boolean result = false;
        while (! realQ.empty()) {
            RtQObj o = realQ.dequeue();
            if (RunQueueInspector.watched(o)) { // Detect interesting events
                result = true;
                markedQ.enqueue(o); // and put them on a queue of their own.
            }
            else freeQ.enqueue(o);
        }
        return result;
    }

    public void preDeliver(RtQObj queueObject, long timeInQueue, int queueLength) {
        prevTarget = queueObject.getTarget();
        if (myHoldState == PROFILE_RUNQUEUE) {
            myInspectorUI.profileRunqueue(queueObject.toString(), timeInQueue, queueLength);
            queueObject.run();
        }
        else if (myHoldState == PROFILE_EXECUTION) {
            long timeToExecute = ec.util.Native.queryTimer();
            String objName = queueObject.toString();
            queueObject.run();
            timeToExecute = ec.util.Native.queryTimer() - timeToExecute;
            myInspectorUI.profileExecution(objName, timeToExecute);
        }
        else {
            queueObject.run();
        }
    }

    
    /**

     * Implement RunQueueDebugger.

     * This code gets called only from the E Runqueue thread.  We
     * don't do anything complicated in this thread. Specifically, we
     * don't use IFC. To request display updates we send events to the
     * UI (most likely written in IFC) at the end of this method.

     */

    public void preRunOne(RtQ theQ, Object vatLock) {
        Object refreshObject = null; // Current Runlet when stepping/finishing is done
        boolean refreshTopLevel = false;

        if (myHoldState == PROFILE_RUNQUEUE) {
            if (!theQ.isTimed()) {
                theQ.startTiming(this);
            }
        }
        else {
            theQ.stopTiming();
        }

        while (true) {
            if (stepSentinelRunlet == null &&
                theQ.empty() &&
                freeQ.empty() &&
                heldQ.empty())
                return; // let this thread die
            if (holdSomeEvents) {
                boolean heldQueueChanged;

                // Start by processing any events in freeQ.  These are
                // events marked as uninteresting either by the triage
                // below, or by the user (by executing a "release" command
                // on them). So even though they may have been interesting
                // once, once they are on the freeq we just deliver them
                // without collecting Consequences.

                while (! freeQ.empty()) {
                    synchronized (vatLock) {
                        if (freeQ.empty()) break;
                        RtQObj qobj = freeQ.dequeue();
                        preDeliver(qobj, -1, 0);
                    }
                }

                // Sort events into watched and unwatched (uninteresting)
                // events.  Some of these watched events came from the
                // freeQ processing above, others came from who knows
                // where. They all rate an entry in the top level runlet
                // vector. Unwatched events are put back on the freeq and
                // are executed next time through the loop.
            
                if (holdAllEvents) heldQueueChanged = moveToQ(heldQ,theQ);
                else heldQueueChanged = splitQ(theQ,heldQ,freeQ); // Sort theQ to heldQ and freeQ

                if (heldQueueChanged) { // There are new events. Put them in the top vector.
                    while (! heldQ.empty()) {
                        Runlet newRunlet = new Runlet(heldQ.dequeue(),null);
                        synchronized(runQueueInspectorTopLevel) {
                            runQueueInspectorTopLevel.addElement(newRunlet);
                            refreshTopLevel = true; // top level display may have changed
                        }
                    }
                }

                Runlet stepRunlet = stepSentinelRunlet;

                // If stepRunlet is non-null here, run it.
                // Collect all consequences into the object.

                while (stepRunlet != null) {

                // stepSentinelRunlet contains the top of a tree of runlets
                // that we want to run to completion, subject to
                // available steps in stepCounter.  We send this
                // runlet in to nextUnRun() to stop recursion from
                // going to its parent Runlets when recursing upwards.

                    stepRunlet = stepRunlet.nextUnRun(stepSentinelRunlet, FINISH_DEPTH);

                    // If nextUnRun() returns null, then we're done.
                    // We're also done if stepCounter reaches 0.

                    if (stepRunlet == null) {
                        stepSentinelRunlet.allHaveBeenRun();
                        refreshObject = stepSentinelRunlet;
                        stepSentinelRunlet = null; // Done
                    } else if (stepCounter > 0) {
                        synchronized (vatLock) {
                            preDeliver(stepRunlet.myQObj, -1, 0);
                            stepRunlet.myQObj = null; // it's no longer valid
                            stepRunlet.addConsequences(theQ);
                            stepCounter--;
                        }
                    }
                    if (stepSentinelRunlet == null || stepCounter <= 0) {
                        
                        // Special case to detect single-step into a leaf object
                        // i.e. an object that did not generate any consequent messages
                    
                        if (endOfStepsDisplayedRunlet != null &&
                            runletStepsThisInteraction == 1 &&
                            endOfStepsDisplayedRunlet.consequences().size() == 0) {
                            endOfStepsDisplayedRunlet.allHaveBeenRun(); // make it green
                            refreshObject = endOfStepsDisplayedRunlet.parent();
                            if (refreshObject == null) refreshTopLevel = true;
                        } else {
                            if (endOfStepsDisplayedRunlet != null) {
                                endOfStepsDisplayedRunlet.allHaveBeenRun();
                                refreshObject = endOfStepsDisplayedRunlet;
                            } else refreshTopLevel = true;
                        }
                        stepRunlet = null;
                        stepSentinelRunlet = null;
                        break;
                    }
                }
            } else {                // Not holding any events. Clean out all queues

                // When cleaning out the queues, we do it round robin
                // to attempt to avoid problems of the kind where
                // events in a queue end up waiting for events yet to
                // be delivered from other queues.

                if (! theQ.empty()) {
                    synchronized (vatLock) {
                        if (! theQ.empty()) {
                            RtQObj qobj = theQ.dequeue();
                            preDeliver(qobj, timeInQueue(qobj), theQ.length());
                        }
                    }
                }
                if (! freeQ.empty()) {
                    synchronized (vatLock) {
                        if (! freeQ.empty()) {
                            preDeliver(freeQ.dequeue(), -1, 0);
                        }
                    }
                }
                if (! heldQ.empty()) {
                    synchronized (vatLock) {
                        if (! heldQ.empty()) {
                            preDeliver(heldQ.dequeue(), -1, 0);
                        }
                    }
                }
            }

            // Send updates to our UI, as appropriate, if it exists.

            if (myInspectorUI != null) {
                if (refreshObject != null)
                    myInspectorUI.refreshRunqueueDisplay(refreshObject); // Change focus to this
                if (refreshTopLevel) 
                    myInspectorUI.refreshRunqueueDisplay(runQueueInspectorTopLevel);
            }
        }
    }

    /**
     * Responsibility from RtQueueTimer interface
     */
    public void putNow(RtQObj obj) {
        long startTime = ec.util.Native.queryTimer();
        myTimes.put(obj, new Long(startTime));
    }

    public long timeInQueue(RtQObj obj) {
        long timeElapsed = -1;

        Long startTime = (Long)myTimes.get(obj);
        if (startTime != null) {
            timeElapsed = ec.util.Native.queryTimer() - startTime.longValue();
            myTimes.remove(obj);
        }
        
        return timeElapsed;
    }

}
