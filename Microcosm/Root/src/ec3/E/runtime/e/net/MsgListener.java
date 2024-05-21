package ec.e.net;

import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.NetworkListener;
import ec.e.net.steward.ByteListener;
import ec.e.start.Vat;

/**
 * Listens for new message-level connections.
 */
public class MsgListener implements NetworkListener {
    private NetworkListener myOuterListener;
    private EListener myInnerListener;
    private Registrar myRegistrar;
    
    /**
     * Construct a new listener to listen for message-level connections on a
     * particular local address.
     *
     * @param listenAtAddr The local address to listen on
     */
    public MsgListener(Vat vat, String listenAtAddr, EListener innerListener, Registrar registrar) {
        myRegistrar = registrar;
        myInnerListener = innerListener;
        myOuterListener = new ByteListener(vat, listenAtAddr, this);
    }
    
    /**
     * Callback when we actually start listening.
     */
    public void listening(String listenAddr) {
        myInnerListener.listening(listenAddr);
    }
    
    /**
     * Callback for when a new incoming connection is made.
     *
     * @param outerConnection The lower level connection (in this case, a
     *  ByteConnection).
     */
    public void noticeConnection(NetworkConnection outer, String localAddr, String remoteAddr) {
        try {
            MsgConnection newMsgConnection = new MsgConnection(outer, localAddr, remoteAddr, myRegistrar);
        } catch (Throwable t) {
            noticeProblem(t, true);
        }
    }

    /**
     * Callback for when an exception condition arises while listening.
     *
     * @param problem The exception that is the problem
     */
    public void noticeProblem(Throwable problem, boolean listenProblem) {
        myInnerListener.noticeProblem(problem, listenProblem);
    }

    /**
     * Restart a suspended listener.
     */
    public void resume() {
        myOuterListener.resume();
    }

    /**
     * Stop listening for new incoming connections.
     */
    public void shutdown() {
        myOuterListener.shutdown();
        myOuterListener = null;
    }

    /**
     * Suspend listener prior to an orderly shutdown.
     */
    public void suspend() {
        myOuterListener.suspend();
    }
}
