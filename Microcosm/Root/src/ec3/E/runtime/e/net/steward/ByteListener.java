package ec.e.net.steward;

import ec.e.start.EEnvironment; // killerhack

import ec.e.start.Seismologist;
import ec.e.start.Vat;
import ec.e.start.Tether;
import ec.e.start.FragileRootHolder;
import ec.e.start.TimeQuake;
import ec.e.net.crew.ListenThread;

/**
   Counterpart to ListenThread.  ByteListener is just inside the vat,
   ListenThread is just outside.  ListenThread holds onto a
   ByteListener through a FragileRootHolder, that ByteListener holds
   onto the same ListenThread through a Tether.<p>

sequence of events for this class on incoming connection:

ListenThread calls noticeConnection(newByteConnection, outerSender)
  The ByteConnection and ByteReceiver have been constructed and plumbed
  to their out of vat counterparts.
  The SendThread is ready to be plugged into a new ByteSender.
We call incomingSetup on the ByteConnection, giving it the SendThread.
  At this point, the entire Byte* layer is built and plumbed.
We notify our inner.

 */

public eclass ByteListener implements NetworkListener, ByteListenerKludge, Seismologist {
    private Vat myVat;
    private NetworkListener myInnerListener;
    private String myListenAddr;
    private Tether myOuterListener;

    private boolean killerhack;

    local ByteListener(Vat thisVat, String listenAddr, NetworkListener inner) {
        myVat = thisVat;
        myInnerListener = inner;
        myListenAddr = listenAddr;

        EEnvironment env = myVat.myEEnvironment;
        killerhack = "true".equals(env.getProperty("killerhack", "false"));
        makeOuterListener();
    }

    private void makeOuterListener() {
        FragileRootHolder holderOfMe = myVat.makeFragileRoot((Object) this, (Seismologist) this);
        ListenThread outer = new ListenThread(myListenAddr, holderOfMe, killerhack);
        myOuterListener = new Tether(myVat, outer);
        outer.startup();
    }

    local void listening(String listenAddr) {
        try {
            myInnerListener.listening(listenAddr);
        } catch (Throwable t) {
            noticeProblem(t, true);
        }
    }

    // not used, ByteListenerKludge.noticeConnection() is used instead
    //  because of vat boundry crossing
    local void noticeConnection(NetworkConnection outer, String localAddr, String remoteAddr) {
        throw new RuntimeException("unimplemented, should use Tether version");
    }

    local void noticeProblem(Throwable t, boolean listenProblem) {
        if (t instanceof NetworkConnectionError) {
            t = ((NetworkConnectionError) t).getNestedThrowable();
        }
        myInnerListener.noticeProblem(t, listenProblem);
    }

    local void noticeConnection(ByteConnection newByteConnection, String localAddr, String remoteAddr, Tether outerSender) {
        try {
            ((ByteConnectionKludge) newByteConnection).incomingSetOuterSender(outerSender);
            myInnerListener.noticeConnection((NetworkConnection) newByteConnection, localAddr, remoteAddr);
        } catch (Throwable t) {
            noticeProblem(t, true);
        }
    }

    public void shutdown() {
        try {
            ((ListenThread) myOuterListener.held()).shutdown();
        } catch (Throwable t) {
            noticeProblem(t, true);
        } finally {
            myOuterListener = null;
        }
    }

    emethod noticeCommit() {}

    emethod noticeQuake(TimeQuake quake) {
        makeOuterListener();
    }

    /**
     * Restart a suspended listener.
     */
    public void resume() {
        try {
            ((ListenThread) myOuterListener.held()).resumeListenThread();
        } catch (Throwable t) {
            noticeProblem(t, true);
        }
    }

    /**
     * Suspend listener prior to an orderly shutdown.
     */
    public void suspend() {
        try {
            ((ListenThread) myOuterListener.held()).suspendListenThread();
        } catch (Throwable t) {
            noticeProblem(t, true);
        }
    }
}


/**
   Something for ByteListener to implement so this java call can be
   made on it.  ByteListener must be an eclass so it can be a
   Seismologist.  All calls of java methods on an eclass require the
   object to be cast into a java interface.  This is that interface.
  */
public interface ByteListenerKludge {
    public void noticeConnection(ByteConnection newByteConnection, String localAddr, String remoteAddr, Tether outerSender);
}
