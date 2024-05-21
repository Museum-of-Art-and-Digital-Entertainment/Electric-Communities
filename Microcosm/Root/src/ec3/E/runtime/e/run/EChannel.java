package ec.e.run;

public class EChannel
implements RtAssignedTether, RtFinalizer
{
    static private final int INITIAL_TARGETS_SIZE = 10;

    private eParty myParty;
    private EDistributor myDistributor;
    private RtTether[] myTargets;
    private int myTargetsSize;
    private RtDeflector myDeflector;

    private static final Trace theTrace = new Trace("ec.e.run.EChannel");

    public EChannel() {
        myParty = new eParty();
        myDistributor = new EDistributor(myParty);
        myTargets = new RtTether[INITIAL_TARGETS_SIZE];
        myTargetsSize = 0;
        myDeflector = null;
    }

    public void invoke(RtSealer seal, Object[] args, RtExceptionEnv ee) {
        if (theTrace.debug && Trace.ON) theTrace.debugm("enqueuing sealer "+seal+" args "+args);

        RtEnqueue.enq(this, seal, ee, args);
    }

    public void invokeNow(RtSealer seal, Object[] args, RtExceptionEnv ee) {
        if (theTrace.debug && Trace.ON) theTrace.debugm("invoking for sealer "+seal+" args "+args+
          "\n  myTargets is "+myTargets+", size "+myTargetsSize);

        // pick up new targets from a non-null party
        if (myParty != null) {
            int extrasz = myParty.myExtraTargetsSize;
            if (extrasz != 0) {
                int newsz = myTargetsSize + extrasz;
                if (newsz > myTargets.length) {
                    // must grow our array
                    int newlen = newsz * 3 / 2;
                    RtTether[] newtargs = new RtTether[newlen];
                    System.arraycopy(myTargets, 0, newtargs, 0, myTargetsSize);
                    myTargets = newtargs;
                }
                RtTether[] extras = myParty.myExtraTargets;
                System.arraycopy(extras, 0, myTargets, myTargetsSize, extrasz);
                myTargetsSize += extrasz;

                // clear out party's targets--don't want garbage!
                for (int i = 0; i < extrasz; i++) {
                    extras[i] = null;
                }
                myParty.myExtraTargetsSize = 0;
            }

            if (myParty.myDistributorIsAlive) {
                // tell the party about the new message if the
                // distributor is still around to care
                myParty.addMessage(seal, args, ee);
            } else {
                // and if the distributor is gone, then there's no
                // point in holding onto the party anymore, since we
                // know we won't ever see any more new targets
                myParty = null;

                // furthermore, if we have zero or one target, then
                // we can short-circuit our deflector and avoid
                // all this code in the future
                if (myTargetsSize == 1) {
                    // size is one, point deflector directly at
                    // the unique target
                    if (RtRun.tr.debug && Trace.ON) {
                        RtRun.tr.debugm("Doing the size one trick. " +
                            "This is a reasonably normal thing to do.");
                    }
                    RtDeflector.setTarget(myDeflector, this, myTargets[0]);
                    RtDeflector.setKey(myDeflector, this, null);
                } else if (myTargetsSize == 0) {
                    // size is zero, point deflector directly at
                    // the null tether
                    if (RtRun.tr.debug && Trace.ON) {
                        RtRun.tr.debugm("Doing the size zero trick. " +
                            "This is a reasonably normal thing to do.");
                    }
                    RtDeflector.setTarget(myDeflector, this, null);
                    RtDeflector.setKey(myDeflector, this, null);
                }
            }
        }

        // loop over all targets, sending them the new message
        for (int i = 0; i < myTargetsSize; i++) {
            // to maintain our pseudo-POE ordering guarantee,
            // we use invokeNow() instead of invoke().

            // this keeps badly implemented tethers from inadvertantly
            // using a previous exception env
            RtRun.CurrentExceptionEnv = null;

            myTargets[i].invokeNow(seal, args, ee);
        }

        // XXX - Possible optimization - if only one recipient which
        // is channel and it has at least one recipient, we can short
        // circuit and set our recipients to our recipient's
        // recipients. Might be too expensive to check on every
        // message send.
    }

    public EDistributor distributor() {
        EDistributor result = myDistributor;
        myDistributor = null;
        return result;
    }

    public void assignDeflector(RtDeflector d) {
        myDeflector = d;
    }

    public void unassignDeflector(RtDeflector d) {
        if (myDeflector == d) {
            myDeflector = null;
        }
    }

    public boolean encodeMeForDeflector() {
        return false;
    }   

    protected void finalize() {
        RtRun.queueReallyFinalize(this);
    }

    public void reallyFinalize() {
        if (myParty != null) {
            myParty.channelGotCollected();
        }
    }

    // this stuff is needed to keep Java from deadlocking in
    // the middle of finalization
    static {
        RtRun.beReadyToFinalize();
        new JavaIsStupidChannel();
    }
}

class JavaIsStupidChannel extends EChannel
{
    JavaIsStupidChannel() {
        super.finalize();
    }

    public void reallyFinalize() {
    }
}
