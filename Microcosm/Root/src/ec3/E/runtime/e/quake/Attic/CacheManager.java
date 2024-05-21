package ec.e.quake;


/**
 *
 */
class CacheManager {
    
    /**
     *
     */
    static public void register(PerishableDataHolder pdh) {
        //XXX
    }
    
    /**
     *
     */
    static public void drop(PerishableDataHolder pdh) {
        //XXX
    }
}


/**
 * a PerishableDataHolder loses its data during a quake but should
 * transparently refetch it on demand.  The data itself is not
 * considered e-reachable. <p>
 *
 * @see ec.e.quake.Vat#makePerishableDataHolder
 */
public class PerishableDataHolder extends Tether {
    private String myUrl;
    private long[] myHash;
    
    /**
     * 
     */
    public PerishableDataHolder(Vat vat, String url, long[] hash) {
        super(vat, null);
        myUrl = url;
        myHash = hash;
        CacheManager.register(this); //so memory pressure can cause
        //old data to be dropped 
    }
    
    /**
     *
     */
    public Object held() {
        if (myHeld == null || vat().quakeCount() != birthQuake()) {
            myHeld = fault(myUrl, myHash);
        }
        return super.held();
    }
    
    /**
     * Brings the object in from the next levels of caching based on
     * the url and the hash. 
     */
    protected Object fault(String url, long[] hash) {
        //XXX
        return null;
    }
    
    /**
     * Called by the CacheManager to drop cache memory when running
     * short. 
     */
    public void purge() {
        myHeld = null;
    }
    
    /**
     *
     */
    protected void finalize() {
        CacheManager.drop(this);
    }
}
