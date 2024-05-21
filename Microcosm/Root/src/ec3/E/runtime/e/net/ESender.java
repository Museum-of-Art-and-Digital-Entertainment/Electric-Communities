package ec.e.net;

import ec.util.HexStringUtils;
import ec.util.assertion.Assertion;
import ec.util.NestedException;
import ec.e.util.SimpleQueue;
import ec.e.util.SimpleQueueReader;
import ec.e.util.SimpleQueueWriter;
import ec.e.start.SmashedException;
import ec.e.run.ConnectionDeadEException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import ec.e.file.EStdio;

/**
 * Object that will send network communications, accepting them at the E
 * level and actually sending them at the message level.
 */
public class ESender {
    // obtained by proxies from EConnections
    
    static private final Trace tr = new Trace("ec.e.net.ESender");
    
    private MsgSender myOuterSender;
    private SimpleQueueReader myQueueReader;
    private SimpleQueueWriter myQueueWriter;
    private Throwable myCause;
    private String myRemoteRegistrarID;
    private ConnectionsManager myConnectionsManager;
    private EConnection myConnection;
    private boolean myPreparingToHibernate;
    private Throwable myEnabler;

    private ESender() {}
    
    /**
     * Construct a new (unconnected) ESender.
     */
    ESender(String remoteRegistrarID, EConnection connection, ConnectionsManager connectionsManager) {
        myRemoteRegistrarID = remoteRegistrarID;
        myConnection = connection;
        myConnectionsManager = connectionsManager;
        enableQueueing();
    }

    void enableQueueing() {
        SimpleQueue queue = new SimpleQueue();
        myQueueReader = queue.reader();
        myQueueWriter = queue.writer();
        if (tr.debug && Trace.ON) myEnabler = new Error("where?");
    }

    /**
     * Enable this ESender by giving it its outer sender.
     *
     * @param outerSender The MsgSender that will actually send messages for us
     */
    public void enable(MsgSender outerSender) throws IOException {
        if (tr.debug && Trace.ON) tr.$(this + ": enable()");
        if (outerSender == null) {
            throw new NullPointerException();
        }
        myOuterSender = outerSender;
        //myOuterSender.enable(); already enabled in order to do startup protocol.
        if (tr.debug && myQueueReader == null) tr.debugm("previous call to ESender.enable()",
                                                         myEnabler);
        Assertion.test(myQueueReader != null, "myQueueReader == null, ESender.enable() called twice!");
        while (myQueueReader.hasMoreElements()) {
            MsgQEntry queueEntry = (MsgQEntry) myQueueReader.nextElement();
            if (queueEntry.rawBitsFlag) {
                if (tr.verbose && Trace.ON) tr.$(this + ": sending queued packet");
                myOuterSender.sendPacket(queueEntry.packet);
            }
            else {
                if (tr.verbose && Trace.ON) tr.$(this + ": sending queued envelope");
                myOuterSender.send(queueEntry.dest, queueEntry.toObjectID, queueEntry.message);
            }
        }
        // only queue messages while we're connecting, not after we've died.
        myQueueWriter = null;
        myQueueReader = null;
    }

    // depricated.  exceptionEnv should be in envelope now.
    //public void sendEnvelope(long toObjectID, RtEnvelope message, RtExceptionEnv exceptionEnv) throws IOException {
    //    sendEnvelope(toObjectID, message);
    //}
    
    /**
     * Send an E message over a connection to a remote machine. If the
     * connection is not yet functional, queue message for later.
     *
     * @param dest The object being sent to.
     * @param toObjectID The remote object ID of the object being sent to
     * @param message The message to send.
     */
    public void sendEnvelope(Object dest, long toObjectID, RtEnvelope message) throws IOException {
        myConnectionsManager.updateMessageSerialNumber(myConnection);
        if (myOuterSender == null) {
            if (myQueueWriter != null) {
                if (tr.verbose && Trace.ON) tr.$(this + ": queueing envelope");
                MsgQEntry queueEntry = new MsgQEntry(dest, toObjectID, message);
                myQueueWriter.enqueue(queueEntry);
                if (myPreparingToHibernate) {
                    myConnection.killConnection(new Error("Message arrived while preparing to hibernate"));
                }
                else {
                    myConnection.resume();
                }
            }
            else {
                tr.debugReportException(new NestedException("sendEnvelope on dead connection", myCause), this + ": smashed connection");
                throw new SmashedException("This EConnection has been disabled: " + myCause);
            }
        } else {
            if (tr.verbose && Trace.ON) tr.$(this + ": sending envelope directly");
            myOuterSender.send(dest, toObjectID, message);
        }
    }

    /**
      * Send a raw packet over a connection to a remote machine.
      *
      * @param packet A filled-in ByteArrayOutputStream containing the packet.
      */
    void sendPacket(ByteArrayOutputStream packet) throws IOException {
        if (myOuterSender == null) {
            if (myQueueWriter != null) {
                MsgQEntry queueEntry = new MsgQEntry(packet);
                myQueueWriter.enqueue(queueEntry);
                // don't try to resume, as this might just be a
                // delayed dgcSuspectTrash.
            }
            else {
                if (tr.debug && Trace.ON) tr.$(this 
                        + ": attempted sendPacket on dead connection: " 
                        + myCause + " Sending"
                        + HexStringUtils.byteArrayToReadableHexString(packet.toByteArray()));
                throw new SmashedException("This EConnection has been disabled: " + myCause);
            }
        }
        else {
            myOuterSender.sendPacket(packet);
        }
    }
    
    /**
       Relay close() to outerSender.
    */
    void disable(Throwable cause) throws IOException {
        if (tr.debug && Trace.ON) tr.$(this + ": disable(" + cause + ")");
        myCause = cause;
        if (myOuterSender == null) {
            if (tr.debug && Trace.ON) tr.$(this + ": burning send queue");
            Throwable why = (Throwable) new ConnectionDeadEException("Failed to connect", cause);
            if (myQueueReader != null) {
                while (myQueueReader.hasMoreElements()) {
                    MsgQEntry queueEntry = (MsgQEntry) myQueueReader.nextElement();
                    if (!queueEntry.rawBitsFlag) {
                        RtExceptionEnv eenv = queueEntry.message.getKeeper();
                        if (eenv != null && eenv != RtRun.NULL_EXCEPTION_ENV) {
                            if (tr.debug && Trace.ON) tr.$(this + ": doing ethrow to queued exception environment: " + eenv + ": " + why);
                            eenv.doEThrow(why);
                        }
                    }
                }
            }
        }
        else {
            if (tr.debug && Trace.ON) tr.$(this + ": closing outer sender");
            try {
                myOuterSender.close(new ConnectionDeadEException("This EConnection has been disabled", myCause));
            }
            finally {
                myOuterSender = null;
                myConnectionsManager.noticeConnectionInactive(myConnection);
            }
        }
    }

    /*packsge*/ void sendSuspend(long outgoingSuspendID) throws IOException {
        if (outgoingSuspendID >= NetIdentityMaker.MIN_ID) {
            // If it is not a shutdown packet
            Assertion.test(myQueueReader == null, "myQueueReader is not null in sendSuspend");
            enableQueueing();
        }
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(outbuf);
        try {
            msgOut.writeByte(Msg.SUSPEND);
            msgOut.writeLong(outgoingSuspendID);
        } catch (Exception e) {
            throw new NestedException("constructing SUSPEND packet", e);
        }
        sendPacket(outbuf);
    }

    void prepareToHibernate() {
        myPreparingToHibernate = true;
    }

    void reviveFromHibernation() {
        myPreparingToHibernate = false;
    }

    void doSuspend() {
        doShutdown(null);
    }

    void doShutdown(Throwable cause) {
        Assertion.test(myOuterSender != null, "myOuterSender is null in doShutdown");
        try {
            myOuterSender.close(cause);
        }
        catch (IOException e) {
            throw new NestedException("problem shutting down connection", e);
        }
        myOuterSender = null;
        myConnectionsManager.noticeConnectionInactive(myConnection);
    }

    public String toString() {
        return "ESender(" + myRemoteRegistrarID + ((myQueueReader==null) ? "" : ", queueing") + ")" ;
    }

    /*package*/ boolean haveQueuedMessages() {
        if (myQueueReader != null) {
            return myQueueReader.hasMoreElements();
        }
        return false;
    }
}

class MsgQEntry {
    boolean rawBitsFlag;
    long toObjectID;
    RtEnvelope message;
    ByteArrayOutputStream packet;
    Object dest;
    
    MsgQEntry(Object dest, long toObjectID, RtEnvelope message) {
        this.rawBitsFlag = false;
        this.toObjectID = toObjectID;
        this.message = message;
        this.dest = dest;
    }

    MsgQEntry(ByteArrayOutputStream packet) {
        this.rawBitsFlag = true;
        this.packet = packet;
    }
}
