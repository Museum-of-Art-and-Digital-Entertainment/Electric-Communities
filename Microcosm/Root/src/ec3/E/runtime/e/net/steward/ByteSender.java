package ec.e.net.steward;

import ec.e.start.Tether;
import java.io.IOException;

/**
   Counterpart to SendThread.  SendThread is just outside the vat,
   ByteSender is just inside.  We're constructed with a Tether to a
   SendThread.  We basically pass everything through that Tether.
  */
public class ByteSender extends NetworkSender {
    Tether myOuterSender;
    String myRemoteAddr;

    public ByteSender(Tether outerSender, String remoteAddr) {
        myOuterSender = outerSender;
        myRemoteAddr = remoteAddr;
    }

    public void write(int b) throws IOException {
        ((NetworkSender) myOuterSender.held()).write(b);
    }

    public void write(byte b[]) throws IOException {
        ((NetworkSender) myOuterSender.held()).write(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        ((NetworkSender) myOuterSender.held()).write(b, off, len);
    }

    public void flush() throws IOException {
        ((NetworkSender) myOuterSender.held()).flush();
    }

    public void close() throws IOException {
        ((NetworkSender) myOuterSender.held()).close();
        myOuterSender = null;
    }

    public void enable() throws IOException {
        ((NetworkSender) myOuterSender.held()).enable();
    }

    public void changeProtocol(
                boolean isAggragating,
                String macType,
                byte[] macKey, 
                String encryptionType,
                byte[] encryptionKey,
                byte[] outIV, 
                byte[] inIV,
                boolean isCompressing,
                boolean useSmallZip) throws IOException {
        ((NetworkSender) myOuterSender.held()).changeProtocol(
                                    isAggragating, macType, macKey, 
                                    encryptionType, encryptionKey, outIV, inIV,
                                    isCompressing, useSmallZip);
    }

    public String toString() {
        return "ByteSender(" + myRemoteAddr + ")" ;
    }
}
