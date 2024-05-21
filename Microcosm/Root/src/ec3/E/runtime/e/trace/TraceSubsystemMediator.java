/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;

import java.util.Vector;
import java.util.Enumeration;
import ec.util.assertion.*;

/**
 * This class mediates between the Trace objects that send messages
 * and the TraceMessageAcceptors that accept them.  It has two key
 * roles:
 * <p>
 * Determining which of the acceptors should get a particular message.
 * <p>
 * Setting the thresholds used to answer the above question.
 */

class TraceSubsystemMediator implements TraceConstants
{
    /** The subsystem that uses this mediator. */
    private String mySubsystem;

    /**
     * True if this subsystem's threshold is whatever
     * "default"'s is.  False if the user specified a specific
     * threshold.  There are NUM_ACCEPTORS elements.
     * <p>
     * Note that this variable can be accessed outside this class.
     * <p>
     * Having knowledge of who's tracking the default be shared between
     * this class and TraceController is kind of slimy, but there's
     * not enough of a gain in fiddling with it.
     */
    protected boolean deferToDefaultThreshold[];

    /**
     * The threshold to use.  Either defined by the user or set from
     * the default.  There are NUM_ACCEPTORS elements.  This is
     * initially a <em>copy</em> of the default thresholds from
     * TraceController.  After that, the elements may change
     * independently, including going back to tracking the default.
     */
    private int myThresholds[];


    /**
     * Whether timing has been turned on for a particular acceptor.
     * NUM_ACCEPTORS elements.  The initial value is false.  There is
     * no system-wide default, because that doesn't seem particularly
     * useful.
     */
    private boolean myTimingBooleans[];

    /**
     * The acceptors all mediators communicate with.  (Could be
     * static, as it is always identical to those stored in
     * TraceController.)
     */
    private TraceMessageAcceptor[][] myAcceptors;

    /**
     * Specific Trace objects act as caches.  There may be multiple
     * trace objects for one subsystem.
     */
    private Vector myCaches;

    /**
     * Create a TraceSubsystemMediator.  It tracks the default
     * priority thresholds.
     * @param aSubsystem the subsystem this mediates.
     * @param starting priority thresholds, used to filter messages.
     * @param the message acceptors that are governed by the thresholds.
     */
    TraceSubsystemMediator(String aSubsystem,
                           int[] someThresholds,
                           TraceMessageAcceptor[][] someAcceptors) {
        mySubsystem = aSubsystem;
        try {
            myThresholds = (int[]) someThresholds.clone();
        } catch (Exception e) {
            Assertion.fail("CloneNotSupported should be impossible.");
        }

        myTimingBooleans = new boolean[NUM_ACCEPTORS];
        for (int i = 0; i < NUM_ACCEPTORS; i++) {
            myTimingBooleans[i] = false;
        }

        myAcceptors = someAcceptors;
        deferToDefaultThreshold = new boolean[someThresholds.length];
        for (int acceptorIndex = 0;
             acceptorIndex < deferToDefaultThreshold.length;
             acceptorIndex++) {
           deferToDefaultThreshold[acceptorIndex] = true;
        }
        myCaches = new Vector();
    }

    /**
     * Accept a message and distribute it to all the acceptors who
     * accept messages of that level.
     * <p>
     * This method represents the second level of filtering by priority.
     * The first level is by the original user code, which uses the
     * Trace object booleans to do its own filtering.
     * TraceMessageAcceptors don't do any filtering.
     * <p>
     * Note:  This method represents the transition from inside the
     * vat to outside the vat.  Therefore the message's timestamp is
     * added here.
     */
    public void accept(TraceMessage message) {
        message.date = new TraceDate();

        for (int acceptorIndex = 0; acceptorIndex < myThresholds.length; acceptorIndex++) {
            if (   myThresholds[acceptorIndex] <= message.level
                || (   message.level == TIMING
                    && myTimingBooleans[acceptorIndex])) {
                for (int variant = 0;
                     variant < myAcceptors[acceptorIndex].length;
                     variant++) {
                    myAcceptors[acceptorIndex][variant].accept(message);
                }
            }
        }
    }

    /**
     * Set a particular acceptor's threshold, either to track the default
     * or to have its own particular value.
     * @param acceptorIndex  which acceptor to change
     * @param newValue to what value.  Note that default values are no
     * different from specific values.
     * @reason whether the acceptor is being set to the default or a specific
     * value.
     */
    protected void setOneThreshold(int acceptorIndex, int newValue,
                                int reason) {
        Trace.trace.eventm("Subsystem " + mySubsystem + "'s " +
            acceptorNames[acceptorIndex] + " threshold is being set to " +
            TraceLevelTranslator.terse(newValue) + " because of " +
            reasonNames[reason] + " change.");
        deferToDefaultThreshold[acceptorIndex] = (reason == FROM_DEFAULT);

        // We don't need to go through the following rigamarole
        // (save for setting the new threshold) if there are no Trace
        // elements attached to this mediator.
        // Since changing the threshold is uncommon, this rare path
        // needn't be optimized.

        // The common threshold could be cached, but it's only
        // relevant when the acceptor's threshold changes or
        // a new trace object is added.  Those are uncommon operations.
        int previousCommonThreshold = commonThreshold();

        myThresholds[acceptorIndex] = newValue;

        int currentCommonThreshold = commonThreshold();

        if (currentCommonThreshold != previousCommonThreshold) {
            Trace.trace.debugm("Updating caches from " +
                               TraceLevelTranslator.terse(previousCommonThreshold) +
                               " to " +
                               TraceLevelTranslator.terse(currentCommonThreshold));
            Enumeration e = myCaches.elements();
            while (e.hasMoreElements()) {
                Trace tr = (Trace)e.nextElement();
                updateOneThresholdCache(tr, currentCommonThreshold);
            }
        }
    }

    /**
     * Set a particular acceptor's timing value.  Unlike setOneThreshold,
     * there's no notion of tracking a default.
     * @param acceptorIndex  which acceptor to change
     * @param newValue to what value.
     */
    protected void setTiming(int acceptorIndex, boolean newValue) {
        Trace.trace.eventm("Subsystem " + mySubsystem + "'s " +
            acceptorNames[acceptorIndex] + " timing value is being set to " +
            newValue + ".");

        boolean previousCommonTiming = commonTiming();
        myTimingBooleans[acceptorIndex] = newValue;
        boolean currentCommonTiming = commonTiming();

        if (currentCommonTiming != previousCommonTiming) {
            Trace.trace.debugm("Updating timing caches from " +
                               previousCommonTiming +
                               " to " +
                               currentCommonTiming);
            Enumeration e = myCaches.elements();
            while (e.hasMoreElements()) {
                Trace tr = (Trace)e.nextElement();
                updateOneTimingCache(tr, currentCommonTiming);
            }
        }
    }

    /** The common threshold is the most detailed (lowest). */
    private int commonThreshold() {
        int result = MAX_THRESHOLD;

        for (int i = 0; i < myThresholds.length; i++) {
            if (myThresholds[i] < result) {
                result = myThresholds[i];
            }
        }
        return result;
    }

    /** The common timing is true if any acceptor times. */
    private boolean commonTiming() {
        for (int i = 0; i < myThresholds.length; i++) {
            if (myTimingBooleans[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update tracing thresholds.
     * <p>
     * A trace object is used as a cache for quick checking of tracing
     * thresholds.  This routine updates that cache.
     * <P>
     * Why, you ask, does this method rudely bash on variables within
     * the Trace object, instead of calling a method to set them?  It's
     * because the Trace object is inside the vat, and no object inside
     * the vat is allowed to change those values, because they are
     * reachable through static state.
     * @param tr the trace object itself.
     * @param commonThreshold  the lowest threshold of all the acceptors.
     */
    private void updateOneThresholdCache(Trace tr, int commonThreshold) {
        if (Trace.trace != null) {
            Trace.trace.debugm("Updating " + tr + " to " +
                              TraceLevelTranslator.terse(commonThreshold));
        }
        tr.verbose = tr.debug = tr.event = tr.usage = tr.warning =
            tr.error = false;
        switch (commonThreshold) {
            case VERBOSE:
                tr.verbose = true;  // fallthrough intentional
            case DEBUG:
                tr.debug = true;  // fallthrough intentional
            case EVENT:
                tr.event = true;  // fallthrough intentional
            case USAGE:
                tr.usage = true;  // fallthrough intentional
            case WORLD:
                tr.world = true;  // fallthrough intentional
            case WARNING:
                tr.warning = true;  // fallthrough intentional
            case ERROR:
                tr.error = true;
                break;
            default:
                Assertion.fail("updateOneThresholdCache switch is missing a case.");
        }
        tr.tracing = tr.debug;  // This is a deprecated variable.
    }

    /**
     * Update timing thresholds.  See updateOneThresholdCache.
     * @param tr the trace object itself.
     * @param commonTiming the value to set.
     */
    private void updateOneTimingCache(Trace tr, boolean commonTiming) {
        if (Trace.trace != null) {
            Trace.trace.debugm("Updating " + tr + " to " + commonTiming);
        }
        tr.timing = commonTiming;
    }

    /**
     * This routine is called when a new Trace object is to be added
     * to this mediator.  We update the cache's state to reflect the
     * state of this mediator.  (Note that we reach into the object
     * and twiddle its fields.  That's because its an in-vat object and
     * not allowed to set its own fields.  Since it's a static object,
     * such would allow a covert channel.
     */
    protected void newCache(Trace newCache) {
        newCache.myMediator = this;
        if (Trace.trace != null) {
            Trace.trace.debugm("The common Threshold is " +
                TraceLevelTranslator.terse(commonThreshold()));
        }
        updateOneThresholdCache(newCache, commonThreshold());
        updateOneTimingCache(newCache, commonTiming());
        myCaches.addElement(newCache);
    }
}
