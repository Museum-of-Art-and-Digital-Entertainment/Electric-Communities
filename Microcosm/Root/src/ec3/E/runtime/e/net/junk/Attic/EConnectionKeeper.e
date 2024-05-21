package ec.e.net;

public eclass EConnectionKeeper {
    static Trace tr = new Trace(false, "[EConnectionKeeper]");
    private RtConnection myConnection;
    private EConnectionKeeper myPeerKeeper;
    private int myShutdownCount = 0;

    EConnectionKeeper(RtConnection connection, EConnectionKeeper peerKeeper) {
        myConnection = connection;
        myPeerKeeper = peerKeeper;
    }

    /* XXX This used to be in RtConnectionKeeper, but I saw no need for that
       class. This method still exists so that maybe it will deal with tethers
       rather than relying on the caller to do the 'new' operation and tether
       the result correctly. However, this method remains a candidate for
       elimination. */
    static public EConnectionKeeper makeKeeper(RtConnection connection,
                                               EConnectionKeeper peerkeeper) {
        return(new EConnectionKeeper(connection, peerkeeper));
    }

    emethod initiateShutdown() {
        String remoteId = myConnection.getRemoteRegistrarId();
        String localId = myConnection.getLocalRegistrarId();
        int remoteHash = remoteId.hashCode();
        int localHash = localId.hashCode();
        boolean activeShutdown = (remoteHash > localHash);
        if (tr.tracing)
            tr.$("remoteId = " + remoteId + " remoteHash = " + remoteHash +
                 " localId = " + localId + " localHash = " + localHash +
                 " activeShutdown = " + activeShutdown);
        if (activeShutdown || myShutdownCount++ > 3) {
            if (tr.tracing)
                tr.$("initiateShutdown active");
            myPeerKeeper <- doShutdown();
            this <- doShutdown(); /* XXX need to be ordered message sends!!! */
        } else {
            if (tr.tracing)
                tr.$("initiateShutdown passive");
            myPeerKeeper <- initiateShutdown();
        }
    }

    emethod doShutdown() {
        if (tr.tracing)
            tr.$("doShutdown");
        myConnection.killConnection();
    }

    emethod handleIncomingMessage(RtMsgQEntry msg) {
        myConnection.handleIncomingMessage(msg);
    }

    emethod handleHandoffFailure() {
        ethrow new RtEHandoffException();
    }
}

public class RtEHandoffException extends RtEException {
    RtEHandoffException() {
        super("Handoff failed");
    }
}
