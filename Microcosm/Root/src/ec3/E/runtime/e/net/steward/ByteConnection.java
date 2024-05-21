package ec.e.net.steward;

import ec.util.NestedException;
import ec.e.net.crew.RawConnection;
import ec.e.start.Seismologist;
import ec.e.start.Vat;
import ec.e.start.Tether;
import ec.e.start.FragileRootHolder;
import ec.e.start.TimeQuake;
import ec.e.start.SmashedException;
import java.io.OutputStream;
import java.io.IOException;

/**
   Counterpart to RawConnection.  ByteConnection is just inside the
   vat, RawConnection is just outside.  RawConnection holds a
   FragileRootHolder to a ByteConnection through which it sends
   various notifications.<p>

   <pre>
sequence of events seen by this class:

incoming connection:

ListenThread does new ByteConnection(remoteAddress);
We make a new inactive receiver (unplumbed).
ListenThread calls getReceiver();
 [ new RawConnection is created with plumbing to ByteConnection and ByteReceiver ]
 [ ByteListener is informed of this ByteConnection and the new SendThread ]
ByteListener calls incomingSetup(SendThread)
We create a new ByteSender, giving it the SendThread.
  At this point, the entire Byte* layer is built and plumbed
  to the out of vat layer.
 [ ByteListener continues, calling noticeConnection on its innerListener
   with this ByteConnection as argument. ]
 [ the inner listener constructs an innerConnection handing it this ByteConnection ]
the inner connection calls incomingSetup, handing us the inner connection
 and inner receiver.
We plumb ourselves to the inner connection, tell the ByteReceiver to plumb
 itself to the innerReceiver, and return the ByteSender so the inner sender
 can plumb itself to ours.
At this point, the inner layer should be complete, and everything should
 be plumbed to the inner layer.

outgoing connection:

inner connection does new ByteConnection(vat, remoteAddr, innerConnection, innerReceiver);
We plumb ourselves to the inner connection.
We create and plumb a ByteReceiver.
We create a new RawConnection, passing it the address to connect to, and
 FragileRootHolder to us and our ByteReceiver.
 [ RawConnection plumbs itself and starts a ConnectThread ]
 [ when the ConnectThread succeeds at connecting, it prods the
   RawConnection with the new socket and our ByteReceiver ]
 [ a new SendThread and RecvThread are built, with the RecvThread
   plumbed to the ByteReceiver ]
RawConnection calls outgoingSetup, giving us a Tether to the new SendThread.
We create a new ByteSender, plumbing it to the SendThread.
At this point the Byte* layer is complete, and plumbed to the out
 of vat layer.
We call outgoingSetup on the inner connection, handing it our ByteSender.
At this point, the inner layer is complete, and plumbing is complete
 to that layer.

 </pre>
*/

public eclass ByteConnection implements NetworkConnection, ByteConnectionKludge, Seismologist {
    private NetworkConnection myInnerConnection;
    private ByteReceiver myReceiver;
    private ByteSender mySender;
    private String myRemoteAddr;
    private String myLocalAddr;

    /**
       Create a new ByteConnection for an incoming connection.

       @param remoteAddr where the connection is coming from.
      */
    public ByteConnection(String remoteAddr) {
        myRemoteAddr = remoteAddr;
        myReceiver = new ByteReceiver(remoteAddr);
    }

    /**
       Create a new ByteConnection for an outgoing connection.

       @param thisVat our vat, needed to make crew and Tethers, etc...
       @param remoteAddr where to connect to.
       @param inner who to notify of events on this connection.
       @param innerReceiver who to send bytes to.
      */
    public ByteConnection(Vat thisVat, String remoteAddr, NetworkConnection inner, OutputStream innerReceiver) {
        myRemoteAddr = remoteAddr;
        myInnerConnection = inner;
        myReceiver = new ByteReceiver(remoteAddr, innerReceiver);
        FragileRootHolder byteConnectionHolder = thisVat.makeFragileRoot((Object) this, (Seismologist) this);
        FragileRootHolder byteReceiverHolder = thisVat.makeFragileRoot((Object) myReceiver, (Seismologist) this);
        new RawConnection(remoteAddr, byteConnectionHolder, byteReceiverHolder);
    }

    /**
       Plug in things that couldn't be set on creation for an incoming
       connection.

       @param inner who to notify of events on this connection.
       @param innerReceiver who to send bytes to.

       @return who our inner layer should send bytes to us through.
      */
    local NetworkSender incomingSetup(NetworkConnection inner, OutputStream innerReceiver) {
        myInnerConnection = inner;
        myReceiver.setInnerReceiver(innerReceiver);
        return mySender;
    }

    /**
       Just here for the inner layer interface.  Not used or
       implemented.
      */
    local void outgoingSetup(NetworkSender outerSender, String localAddr) {
        throw new RuntimeException("unimplemented");
    }

    /**
       Send a problem report farther along the chain (farther into the
       vat).  On an incoming connection, there may not yet be an
       innerConnection, so we encapsulate the problem in a
       NetworkConnectionError and throw it.  It should ultimately get
       caught in the Listen chain.  There it is unencapsulated and
       passed up that chain.

       @param t the problem.
      */
    local void noticeProblem(Throwable t) {
        if (myInnerConnection != null) {
            myInnerConnection.noticeProblem(t);
        } else {
            throw new NetworkConnectionError("problem on partially set up connection", t);
        }
    }

    /**
      Send line statistics up the protocol stack
     
      @param messageLength the length of the comm protocol or E message.
      @param compressedLength the length sent on the socket.
     */
    local void updateSendCounts(int messageLength, int compressedLength) {
        if (myInnerConnection != null) {
            myInnerConnection.updateSendCounts(messageLength, compressedLength);
        }
    }

    /**
      Send line statistics up the protocol stack
     
      @param messageLength the length of the comm protocol or E message.
      @param compressedLength the length received on the socket.
     */
    local void updateReceivedCounts(int messageLength, int compressedLength) {
        if (myInnerConnection != null) {
            myInnerConnection.updateReceivedCounts(messageLength, compressedLength);
        }
    }

    /**
       Notify our innerConnection of the connection going away.  If
       there is no innerConnection, noone is interested, so just
       ignore it.
      */
    local void noticeShutdown(byte[] sendIV, byte[] receiveIV) {
        if (myInnerConnection != null) {
            myInnerConnection.noticeShutdown(sendIV, receiveIV);
        }
        // and if it IS null???
        myInnerConnection = null;
        mySender = null;
        myReceiver = null;
    }

    /**
       Return our receiver, allowing an incoming connection chain to
       be built.

       @return our ByteReceiver.
      */
    local OutputStream getReceiver() {
        return myReceiver;
    }

    emethod noticeCommit() {}

    emethod noticeQuake(TimeQuake quake) {
        try {
            noticeProblem(new SmashedException("post quake network partition: " + quake));
        }
        catch (NetworkConnectionError e) {
            // ignore
        }
            
        try {
            if (myReceiver != null) {
                myReceiver.close();
            }
        }
        catch (IOException e) {
            throw new NestedException("exception closing myReceiver", e);
        }
        
        noticeShutdown(null, null);
    }

    /**
       Replacement for outgoingSetup from NetworkConnection.  Takes a
       Tether so it works across the vat boundry.  Called after
       outgoing connection open returns.  Creates the ByteSender and
       passes outgoingSetup out the chain.

       @param outerSender Tether to the SendThread that we should send
        outgoing bytes to.
       @param localAddr the address and port we actually connected from.
      */
    local void outgoingSetOuterSender(Tether outerSender, String localAddr) {
        mySender = new ByteSender(outerSender, myRemoteAddr);
        myInnerConnection.outgoingSetup(mySender, localAddr);
    }

    /**
       Replacement for incomingSetup from NetworkConnection.  Takes a
       Tether so it works across the vat boundry.  Just makes the
       ByteSender.

       @param outerSender Tether to the SendThread that we should send
        outgoing bytes to.
      */
    local void incomingSetOuterSender(Tether outerSender) {
        mySender = new ByteSender(outerSender, myRemoteAddr);
    }

    local String toString() {
        return "ByteConnection(" + myLocalAddr + ", " + myRemoteAddr + ")" ;
    }
}


/**
   Something for ByteConnection to implement so these java calls can
   be made on it.  ByteConnection must be an eclass so it can be a
   Seismologist.  All calls of java methods on an eclass require the
   object to be cast into a java interface.  This is that interface.
  */
public interface ByteConnectionKludge {
    public void incomingSetOuterSender(Tether outerSender);
    public void outgoingSetOuterSender(Tether outerSender, String localAddr);
}
