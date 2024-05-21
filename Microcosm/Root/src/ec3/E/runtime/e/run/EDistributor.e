package ec.e.run;

eclass EDistributor
implements EResult, RtFinalizer
{
    static private final int INITIAL_ENVELOPES_SIZE = 10;

    private eParty myParty;
    private RtEnvelope[] myEnvelopes;
    private int myEnvelopesSize;

    EDistributor(eParty party) {
        myParty = party;
        myEnvelopes = new RtEnvelope[INITIAL_ENVELOPES_SIZE];
        myEnvelopesSize = 0;
    }

    emethod forward(Object targ) {
        if (targ == null) {
            // ignore a null target
            return;
        }

        // make sure the forward is okay before proceeding
        RtTether target;
        try {
            target = (RtTether) targ;
        } catch (ClassCastException e) {
            ethrow new RtRuntimeException("cannot forward a non-E object (" +
                targ + ") to an EDistributor");
            return;
        }

        // pick up new envelopes from a non-null party
        if (myParty != null) {
            int extrasz = myParty.myExtraEnvelopesSize;
            if (extrasz != 0) {
                int newsz = myEnvelopesSize + extrasz;
                if (newsz > myEnvelopes.length) {
                    // must grow our array
                    int newlen = newsz * 3 / 2;
                    RtEnvelope[] newenvs = new RtEnvelope[newlen];
                    System.arraycopy(myEnvelopes, 0, newenvs, 0, 
                        myEnvelopesSize);
                    myEnvelopes = newenvs;
                }
                RtEnvelope[] extras = myParty.myExtraEnvelopes;
                System.arraycopy(extras, 0, myEnvelopes, myEnvelopesSize,
                    extrasz);
                myEnvelopesSize += extrasz;

                // clear out party's envelopes--don't want garbage!
                for (int i = 0; i < extrasz; i++) {
                    extras[i] = null;
                }
                myParty.myExtraEnvelopesSize = 0;
            }

            if (myParty.myChannelIsAlive) {
                // tell the party about the new target if the
                // channel is still around to care
                myParty.addTarget(target);
            } else {
                // and if the channel is gone, then there's no
                // point in holding onto the party anymore, since we
                // know we won't ever see any more new envelopes
                myParty = null;
            }
        }

        // loop over all envelopes, sending them to the new target
        for (int i = 0; i < myEnvelopesSize; i++) {
            // to maintain our pseudo-POE ordering guarantee,
            // we use invokeNow() instead of invoke().

            // this keeps badly implemented tethers from inadvertantly
            // using a previous exception env
            RtRun.CurrentExceptionEnv = null;

            RtEnvelope e = myEnvelopes[i];
            target.invokeNow(e.mySealer, e.myArgs, e.myEE);
        }
    }

    emethod forwardException(Throwable t) {
        if (RtRun.tr.debug && Trace.ON) {
            RtRun.tr.debugm("Distributor " + this + " ignoring " +
                "forwardException(" + t + ")");
        }
        // goes into the bit bucket
    }
    
    protected void finalize() {
        RtRun.queueReallyFinalize(this);
    }

    public void reallyFinalize() {
        if (myParty != null) {
            myParty.distributorGotCollected();
        }
    }

    // this stuff is needed to keep Java from deadlocking in
    // the middle of finalization
    static {
        RtRun.beReadyToFinalize();
        new JavaIsStupidDistributor_$_Impl();
    }
}

class JavaIsStupidDistributor_$_Impl extends EDistributor_$_Impl
{
    JavaIsStupidDistributor_$_Impl() {
        super(null);
        try {
            super.finalize();
        } catch (Throwable t) {
            // ignore
        }
    }

    public void reallyFinalize() {
    }
}
