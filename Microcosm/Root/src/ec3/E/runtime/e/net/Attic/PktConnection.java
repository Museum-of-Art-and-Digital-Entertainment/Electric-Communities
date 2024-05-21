package ec.e.net;

import java.io.OutputStream;
import java.io.IOException;
import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.NetworkSender;
import ec.e.net.steward.NetworkConnectionError;
import ec.e.net.steward.ByteConnection;
import ec.e.start.Vat;


public class PktConnection implements NetworkConnection {
    private String myLocalAddr;
    private String myRemoteAddr;
    private PktReceiver myReceiver;
    private PktSender mySender;
    private NetworkConnection myInnerConnection;

    // incoming
    public PktConnection(NetworkConnection outerConnection, 
                         String localAddr, String remoteAddr) {
        myRemoteAddr = remoteAddr;
        myReceiver = new PktReceiver(this, remoteAddr);
        NetworkSender outerSender 
                = outerConnection.incomingSetup(this, myReceiver);
        mySender = new PktSender(this, outerSender, remoteAddr);
    }

    // outgoing
    public PktConnection(Vat thisVat, String remoteAddr, 
                         NetworkConnection inner, OutputStream innerReceiver) {
        myRemoteAddr = remoteAddr;
        myInnerConnection = inner;
        myReceiver = new PktReceiver(this, remoteAddr);
        myReceiver.setInnerReceiver(innerReceiver);
        new ByteConnection(thisVat, remoteAddr, (NetworkConnection) this, 
                           (OutputStream) myReceiver);
    }

    public NetworkSender incomingSetup(NetworkConnection inner, 
                                       OutputStream innerReceiver) {
        myInnerConnection = inner;
        myReceiver.setInnerReceiver(innerReceiver);
        return mySender;
    }

    public void outgoingSetup(NetworkSender outerSender, String localAddr) {
        myLocalAddr = localAddr;
        mySender = new PktSender(this, outerSender, myRemoteAddr);
        myInnerConnection.outgoingSetup(mySender, localAddr);
    }

    public void noticeProblem(Throwable t) {
        if (myInnerConnection != null) {
            myInnerConnection.noticeProblem(t);
        } else {
            throw new NetworkConnectionError("problem on partially set up connection", t);
        }
    }

    public void noticeShutdown() {
        if (myInnerConnection != null) {
            myInnerConnection.noticeShutdown();
        }
        // and if it IS null???
    }

    public OutputStream getReceiver() {
        return (OutputStream) myReceiver;
    }
    
    /*package*/ void startEncrypting(byte[] macKey, byte[] desKey,
                                     byte[] inIV, boolean isCompressing) 
    throws IOException {
        myReceiver.startEncrypting(macKey, desKey, inIV, isCompressing);
    }

    public String toString() {
        return "PktConnection(" + myLocalAddr + ", " + myRemoteAddr + ")" ;
    }
}
