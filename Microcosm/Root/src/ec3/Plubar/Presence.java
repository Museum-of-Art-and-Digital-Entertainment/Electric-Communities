package ec.plubar;

/**
 * A presence of an unum
 *
 * @author Chip Morningstar
 * @author Karl Schumaker
 * @version 1.0
 */
public class Presence {
    /** The Unum which this Presence represents. */
    private Unum myUnum;
    /** A flag indicating if this Presence is the host. */
    protected boolean IAmTheHost = false;

    /**
     * Constructor callable only from within package. The only caller should be
     * the class Unum. The result is a new presence of a given unum.
     *
     * @param unum  The unum we are to be a presence of
     * @param isHost  true=>create a host presence, false=>create a client
     */
    /* package */ Presence(Unum unum, boolean isHost) {
        myUnum = unum;
        IAmTheHost = isHost;
    }

    /**
     * Invoke a presence-level method
     *
     * @param message  Message to deliver
     * @param args  Message arguments
     *
     * @exception UnumException from Unum.presenceSend()
     *
     * @see Unum#presenceSend
     */
    public void send(String message, Object[] args) throws UnumException {
        myUnum.presenceSend(IAmTheHost, message, args);
    }
}
