package ec.e.openers;


/**
 * Same idea as RtAwakeAfterDecoding but for coming back from
 * checkpoint rather than arriving over a comm connection.  Should
 * only be implemented by stewards.
 */
public interface AwakeAfterRevival {
    
    /**
     * WARNING: This is called while the Vat is in a very delicate
     * state.  The runQ has not been set in motion yet and the vatLock
     * is not held.  Try to do only very local state changes. 
     *
     * @see ec.e.start.EBoot
     */
    void awakeAfterRevival();
}


