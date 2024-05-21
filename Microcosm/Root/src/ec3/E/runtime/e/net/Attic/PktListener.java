package ec.e.net;

import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.NetworkListener;
import ec.e.net.steward.ByteListener;
import ec.e.start.Vat;

public class PktListener implements NetworkListener {
    NetworkListener myInnerListener;
    String myListenAddr;
    NetworkListener myOuterListener;

    public PktListener(Vat thisVat, String listenAddr, NetworkListener inner) {
        myInnerListener = inner;
        myListenAddr = listenAddr;
        myOuterListener = (NetworkListener) 
                new ByteListener(thisVat, listenAddr, (NetworkListener) this);
    }


    public void listening(String listenAddr) {
        try {
            myInnerListener.listening(listenAddr);
        } catch (Throwable t) {
            noticeProblem(t, true);
        }
    }

    public void noticeConnection(NetworkConnection outer, 
                                 String localAddr, String remoteAddr) {
        try {
            PktConnection newPktConnection = 
                    new PktConnection(outer, localAddr, remoteAddr);
            myInnerListener.noticeConnection(
                    (NetworkConnection) newPktConnection, 
                    localAddr, remoteAddr);
        } catch (Throwable t) {
            noticeProblem(t, true);
        }
    }

    public void noticeProblem(Throwable t, boolean listenProblem) {
        myInnerListener.noticeProblem(t, listenProblem);
    }

    /**
     * Stop listening for new incoming connections.
     */
    public void shutdown() {
        try {
            myOuterListener.shutdown();
        } catch (Throwable t) {
            noticeProblem(t, true);
        }
    }

    /**
     * Restart a suspended listener.
     */
    public void resume() {
        myOuterListener.resume();
    }

    /**
     * Suspend listener prior to an orderly shutdown.
     */
    public void suspend() {
        myOuterListener.suspend();
    }
}
