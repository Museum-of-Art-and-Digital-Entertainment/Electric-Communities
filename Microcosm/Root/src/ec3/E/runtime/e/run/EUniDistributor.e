package ec.e.run;

public eclass EUniDistributor
implements EResult, RtFinalizer
{
    private EUniChannel myChannel;

    public String toString () {
        return super.toString() + "[channel "+myChannel+"]";
    }

    public EUniDistributor(EUniChannel chan) {
        myChannel = chan;
    }

    emethod forward(Object value) {
        myChannel.forward(value);
    }

    emethod forwardException(Throwable t) {
        myChannel.forwardException(t);
    }

    protected void finalize() {
        RtRun.queueReallyFinalize(this);
    }

    public void reallyFinalize() {
        if (myChannel != null) {
            myChannel.distributorGotCollected();
        }
    }

    // this stuff is needed to keep Java from deadlocking in
    // the middle of finalization
    static {
        RtRun.beReadyToFinalize();
        ((EUniDistributor_$_Impl) new EUniDistributor(null)).finalize();
    }
}
