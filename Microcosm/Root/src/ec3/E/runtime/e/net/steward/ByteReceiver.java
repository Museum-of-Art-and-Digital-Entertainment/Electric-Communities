package ec.e.net.steward;

import java.io.OutputStream;
import java.io.IOException;

/**
   Counterpart to RecvThread.  ByteReceiver is just inside the vat,
   RecvThread is just outside.  RecvThread holds a FragileRootHolder
   to a ByteReceiver, through which it calls write().  Exceptions get
   thrown back out to RecvThread where they are caught and sent to
   RawConnection, and thence back into the vat via
   ByteConnection.noticeProblem().<p>
  */
public class ByteReceiver extends OutputStream {
    String myRemoteAddr;
    OutputStream myInnerReceiver;

    /**
       Create a ByteReceiver for an incoming connection.

       @param remoteAddr address our bytes are ultimately coming from
        (just for toString).
      */
    public ByteReceiver(String remoteAddr) {
        myRemoteAddr = remoteAddr;
    }


    /**
       Create a ByteReceiver for an outgoing connection.

       @param remoteAddr address our bytes are ultimately coming from
        (just for toString).
       @param innerReceiver who to send bytes on to.
      */
    public ByteReceiver(String remoteAddr, OutputStream innerReceiver) {
        myRemoteAddr = remoteAddr;
        myInnerReceiver = innerReceiver;
    }

    public void write(int b) throws IOException {
        myInnerReceiver.write(b);
    }

    public void write(byte b[]) throws IOException {
        myInnerReceiver.write(b);
    }

    /**
       Write some bytes.  Just pass them through.  Other write() calls
       are similar.

       @param b the bytes to write.
       @param off where in b to start.
       @param len how many bytes to pass through.
      */
    public void write(byte b[], int off, int len) throws IOException {
        myInnerReceiver.write(b, off, len);
    }

    public void flush() throws IOException {
        myInnerReceiver.flush();
    }

    public void close() throws IOException {
        if (myInnerReceiver != null) {
            myInnerReceiver.close();
        }
        myInnerReceiver = null;
    }

    /**
       On an incoming connection, we get created before our
       innerReceiver.  This tells us about it when it's known.

       @param inner who to send bytes to.
      */
    public void setInnerReceiver(OutputStream inner) {
        myInnerReceiver = inner;
    }

    public String toString() {
        return "ByteReceiver(" + myRemoteAddr + ")" ;
    }
}
