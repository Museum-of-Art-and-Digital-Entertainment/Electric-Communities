package ec.pl.runtime;

/**
 * A presence of an unum
 */
public class Presence {
    private Unum myUnum;
    private boolean amHost;

    /**
     * Constructor callable only from within package. The only caller should be
     * the class Unum. The result is a new presence of a given unum.
     *
     * @param unum  The unum we are to be a presence of
     * @param isHost  true=>create a host presence, false=>create a client
     */
    /* package */ Presence(Unum unum, boolean isHost) {
        myUnum = unum;
        amHost = isHost;
    }

    /**
     * Invoke a presence-level method
     *
     * @param message  Message to deliver
     * @param args  Message arguments
     *
     * @exception UnumException from Unum.presenceSend()
     */
    public void send(String message, Object[] args) throws UnumException {
        myUnum.presenceSend(amHost, message, args);
    }
}
