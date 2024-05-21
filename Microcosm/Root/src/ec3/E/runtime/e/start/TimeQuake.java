package ec.e.run;

/**
 *
 */
public class TimeQuake {
    private int myDamage;
    private long myQuakeCount;
    private TimeQuake myNext;
    private Seismologist myMulticast;
    private EResult myNotifier;
    
    /** quake damage severity */
    static public final int QUAKE_DRILL = 1;
    /** quake damage severity */
    static public final int REINCARNATION = 2;
   
    /**
     * Used by TimeMachine to report the next quake
     */
    TimeQuake(int damage, long quakeCount) {
        myDamage = damage;
        myQuakeCount = quakeCount;
        myNext = null;
        Seismologist s = (Seismologist) EUniChannel.construct(Seismologist.class);
        EUniDistributor s_dist = EUniChannel.getDistributor(s);
        myNotifier = s_dist;
        myMulticast = s;
    }
    
    /**
     * Informs the quake of its next quake.  The quake in turn must
     * tell all Seismologists that have been waiting for this.
     */
    void setNextQuake(TimeQuake next) {
        myNext = next;
        myMulticast <- noticeQuake(next);
        myMulticast = null;
    }
    
    /**
     * Add this Seismologist to those that will be told about the
     * quake following this one.  If a Seismologist is added multiple
     * times, it will receive multiple reports. <p>
     *
     * If 'watcher' is null, does nothing.
     */
    public void waitForNext(Seismologist watcher) {
        if (watcher != null) {
            myNotifier <- forward(watcher);
        }
    }
    
    /**
     * What kind of quake damage does this quake represent?
     */
    public int damage() {
        return myDamage;    
    }
    
    /**
     * Which quake is this?
     */
    public long quakeCount() {
        return myQuakeCount;
    }
}


