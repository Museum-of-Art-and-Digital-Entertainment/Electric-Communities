package ec.e.net;

import ec.e.net.steward.NetworkConnection;
import ec.e.start.Vat;
import ec.e.file.EStdio;
import java.io.IOException;

/**
 * Listens for new E-level connections.
 */
class EListener {
    private MsgListener myOuterListener;
    private ConnectionsManager myConnectionsManager;

    private EListener() {}
    
    /**
     * Construct a new listener to listen for E-level connections on a
     * particular local address.
     *
     * @param listenAtAddr The local address to listen on
     */
    EListener(Vat vat, ConnectionsManager connectionsManager,
                     String listenAtAddr, Registrar registrar) {
        myConnectionsManager = connectionsManager;
        myOuterListener = new MsgListener(vat, listenAtAddr, this, registrar);
    }

    /**
     * Callback when we actually start listening.
     */
    public void listening(String listenAddr) {
        myConnectionsManager.listening(listenAddr);
    }

    /**
     * Callback for when an exception condition arises while listening.
     *
     * @param problem The exception that is the problem
     */
    public void noticeProblem(Throwable problem, boolean listenProblem) {
        /* XXX For now, our action at this level is to just give up. In the
           fullness of time we will revise this method to actually look at the
           exception and cope more intelligently. */
        if (listenProblem) {
            try {
                EStdio.err().println("problem in listener, shutting down:");
                EStdio.reportException(problem, true);
                myConnectionsManager.stopListeningForConnections();
            } catch (RegistrarException e) {
                /* Swallow exception, else we'll have infinite loop here. */
            }
        }
        else {
            myConnectionsManager.noticeProblem(null, problem);
        }
    }

    /**
     * Restart a suspended listener.
     */
    void resume() {
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
    void suspend() {
        myOuterListener.suspend();
    }
}
