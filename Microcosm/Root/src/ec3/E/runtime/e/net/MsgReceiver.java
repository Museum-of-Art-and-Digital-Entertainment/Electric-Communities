package ec.e.net;

import ec.util.HexStringUtils;
import ec.e.db.RtDecodingException;
import ec.e.db.RtDecodingManager;
import ec.e.db.RtDecodingParameters;
import ec.e.db.RtStandardDecoder;
import ec.e.db.RtDecoderDataInputStream;
import ec.e.db.TypeTable;
import ec.e.net.steward.Proxy;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import ec.util.NativeSteward; // XXX used by spam only

/**
 * Object that will receive network communications, receiving them at the
 * packet level and translating them into the message level.
 */
public class MsgReceiver extends OutputStream implements RtDecodingManager {
    static private final Trace tr = new Trace("ec.e.net.MsgReceiver");
    
    // XXX TAKE THIS OUT
    // This isn't even race-safe fer cryin' out loud!!!
    static private RtSealer myCurrentlyReceivedSealer;
    // XXX TAKE THIS OUT

    // for spam only
    private static long theTotalTime = 0;
    
    private EReceiver myInnerReceiver;
    private EConnection myInnerConnection;
    private MsgConnection myConnection;
    private RtStandardDecoder myDecoder;
    private TypeTable myTypeTable;
    private ImportExportTables myTables;
    private Hashtable myFastStringsInverse;

    private static final int DECODING_NOTHING               = 0;
    private static final int DECODING_ENVELOPE              = 1;
    private static final int DECODING_EXCEPTION_ENVIRONMENT = 2;
    private static final int DECODING_FAILED                = 3;
    private int myDecodingState = DECODING_NOTHING;

    /**
     * Construct a new MsgReceiver.
     *
     * @param connection The MsgConnection we are servicing
     * @param innerReceiver The EReceiver that will get out messages
     */
    public MsgReceiver(MsgConnection connection, String remoteAddr) {
        myConnection = connection;
    }

    public void setInnerReceiver(EReceiver innerReceiver, RtDecodingParameters parameters) {
        myInnerReceiver = innerReceiver;
        myTables = innerReceiver.importExportTables();
        myInnerConnection = innerReceiver.connection();
        myTypeTable = new TypeTable();
        myFastStringsInverse = myInnerConnection.getTheFastStringsInverse();
        myDecoder = new RtStandardDecoder(this, myTypeTable, new byte[0],
                        parameters, 
                        !myConnection.myAgreededProtocol.equals("2"),
                        myFastStringsInverse);
    }

    /**
     * Object decode method.
     * This method is part of the RtDecodingManager interface.
     * @see ec.e.db.RtDecodingManager
     */
    public Object decodeObject(Class theClass, RtDecoder stream, int objectId){
        long proxyID;
        byte whichSide;

        if (tr.debug && Trace.ON) {
            tr.debugm("doing decodeObject(" + theClass + ", " +
                stream + ", " + objectId + ")");
        }

        try {
            whichSide = stream.readByte();
            proxyID = stream.readLong();
        } catch (Exception e) {
            tr.errorm("exception decoding object header", e);
            return null;
        }
        /* If the ID was from the sender's inbound side, i.e. my export ... */
        if (whichSide == MsgConnection.INBOUND_ID) {
            /* Represents an object on this side of the connection */
            if (tr.debug && Trace.ON) {
                tr.debugm("got local id " + proxyID);
            }
            return myTables.exports().get(proxyID);
        } else if (whichSide == MsgConnection.HANDOFF_ID) {
            /* Represents a Handoff */
            long handoffID;
            Object handoff;
            String originatorPath;
            String originatorRegistrarId;
            try {
                handoffID = stream.readLong();
                originatorPath = (String) stream.decodeGraph();
                originatorRegistrarId = (String) stream.decodeGraph();
                if (tr.debug && Trace.ON) {
                    tr.debugm("got handoff; path: " + originatorPath +
                        "; registrar: " + originatorRegistrarId);
                }
            } catch (Exception e) {
                tr.errorm("exception decoding handoff", e);
                return null;
            }
            handoff = myInnerConnection.getProxyForHandoff(proxyID, theClass,
                originatorRegistrarId, originatorPath,
                myInnerConnection.remoteRegistrarID());
            if (handoff == null) {
                if (myDecodingState != DECODING_ENVELOPE) {
                    myDecodingState = DECODING_FAILED;
                    /* We failed when decoding the Exception Environment.  We
                       can use a proxy to a proxy in this case, only because
                       they are all within code executing in the TCB. */
                    /* XXX - Might want to reconsider doing this in some cases,
                       but we have to at least do it when we can't get the
                       Exception Environment, so we can tell it we couldn't
                       decode it! */
                    Proxy p = myTables.imports().registerProxy(
                        theClass, handoffID);
                    return p.getPrimeDeflector();
                } else {
                    throw new HandoffException();
                }
            } else {
                return handoff;
            }
        } else /* if (whichSide == MsgConnection.OUTBOUND_ID) */ {
            /* Represents an object on the other side of the connection */
            if (tr.debug && Trace.ON) {
                tr.debugm("got remote id " + proxyID);
            }
            Object object = myTables.imports().get(proxyID);
            if (object == null) {
                Proxy p = myTables.imports().registerProxy(theClass, proxyID);
                object = p.getPrimeDeflector();
            }
            if (tr.debug && Trace.ON) {
                tr.debugm("resolved to " + object);
            }
            return object;
        }
    }

    /**
     * This method is part of the RtDecodingManager interface.
     * @see ec.e.db.RtDecodingManager
     */
    public Object getUniqueObject(int id) {
        Object result;
        Integer oid = new Integer(id);
        if (id < 0) {
            /* Negative number means we exported this first */
            result = myTables.uniqueExportsByID().get(oid);
        } else {
            result = myTables.uniqueImportsByID().get(oid);
        }
        return result;
    }
        
    /**
     * Handle missing parameter.
     *
     * This method is part of the RtDecodingManager interface.
     * @see ec.e.db.RtDecodingManager
     */
    public Object handleMissingParameter(Object paramObj,
            RtDecodingParameters parameters) throws RtDecodingException {
        if (parameters == null) {
            throw new RtDecodingException(
                "RtDecoder was not given a parameterObjects set for '" +
                paramObj + "'");
        } else {
            throw new RtDecodingException(
                "RtDecoder was not given a parameter for '" + paramObj + "'");
        }
    }

    /**
     * Receive a message in the form of bytes, decode it into a proper E
     * message and deliver it to the inner receiver.
     *
     * @param msgBytes The encoded message that was received for processing
     */
    public void write(byte packetBytes[]) throws IOException {
        switch (packetBytes[0]) {
            case Msg.STARTUP:
                myConnection.processStartupPacket(packetBytes);
                break;
            case Msg.ENVELOPE:
                myInnerConnection.messagesReceived++;
                receiveEnvelope(packetBytes);
                break;
            case Msg.NEW_CLASSES:
                receiveNewClasses(packetBytes);
                break;

            case Msg.PROTOCOL_VERSION:
                myConnection.checkProtocolVersion(packetBytes);
                break;

            case Msg.PROTOCOL_ACCEPTED:
                processProtocolAccepted(packetBytes);
                break;

            case Msg.EXPORT_OBJECT_REQUEST:
            case Msg.EXPORT_OBJECT_REPLY:
            case Msg.SUSPEND:
                
            case Msg.W_RESPONSE:
            case Msg.W_REMOVE_ME:
            case Msg.W_ARE_YOU_LR:
            case Msg.SUSPECT_TRASH:
            case Msg.UNREGISTER_IMPORT:
            case Msg.UNREGISTER_UNIQUE:
            case Msg.UNREGISTER_EXPORT:

                myInnerReceiver.receivePacket(packetBytes);
                break;

            case Msg.PROTOCOL_ERROR:
                myConnection.sendPacket(Msg.PROTOCOL_ERROR, MsgConnection.TOK_ERR_PROTOCOL, "incoming protocol version 0.0 is not supported, use version " + Msg.Version, null, null);
                throw new IOException("incoming protocol version 0[0] is not supported, use version " + Msg.Version + "[" + Msg.VersionInt + "]");
            default:
                throw new IOException("unknown packet type: " + packetBytes[0]);
        }
    }

    
    /**
     * Process the protocol the receipent of the connection selected.
     */

    void processProtocolAccepted(byte packetBytes[]) throws IOException {
        DataInputStream packetIn = new DataInputStream(new ByteArrayInputStream(packetBytes));
        byte header = packetIn.readByte(); /* already seen, discard */
        packetIn.readInt();     // Discard integer.
        String theirVersion = packetIn.readUTF();
        for (int i=0; i<Msg.Version.length; i++) {
            if (theirVersion.equals(Msg.Version[i])) {
                myConnection.myAgreededProtocol = Msg.Version[i];
                return;
            }
        }
        String err = "incoming protocol version " + theirVersion;
        err += " are not supported, use versions ";
        for (int i=0; i<Msg.Version.length-1; i++) {
            err += (Msg.Version[i] + ", ");
        }
        err += Msg.Version[Msg.Version.length-1];
        myConnection.sendPacket(Msg.STARTUP, MsgConnection.TOK_ERR_PROTOCOL, err, null, null);
        throw new IOException(err);
    }

    public void write(byte packetBytes[], int offset, int length)
    throws IOException {
        throw new Error("this should not be called");
    }
    public void write(int b) throws IOException {
        throw new Error("this should not be called");
    }
    public void flush() throws IOException {
    }

    public void close() throws IOException {
        //myInnerReceiver.close();
        myInnerReceiver = null;
    }

    /**
     * This method is part of the RtDecodingManager interface.
     * @see ec.e.db.RtDecodingManager
     */
    public void uniqueImportedObject (Object object, int id) {
        myTables.uniqueImports().put(object, new Integer(-id));
        myTables.uniqueImportsByID().put(new Integer(id), object);
    }
    
    // XXX TAKE THIS OUT
    /**
     * XXX TAKE THIS OUT GROSS ROBJ HACK
     * Let us see the sealer of the currently-received envelope.
     */
    static public RtSealer getCurrentlyReceivedSealer () {
      return myCurrentlyReceivedSealer;
    }
    // XXX TAKE THIS OUT

    /**
     * Process a received envelope, i.e., an E-message. It gets decoded and
     * delivered onto the run queue.
     *
     * @param packetBytes The encoded message
     */
    private void receiveEnvelope(byte packetBytes[]) {
        myDecoder.resetStreamWithBytes(packetBytes);
        try {
            byte header = myDecoder.readByte(); /* Old news, discard */
            long toObjectID = myDecoder.readLong();
            RtEnvelope envelope;

            Exportable toObject = myTables.exports().get(toObjectID);
            if (toObject == null) {
                tr.errorm("toObject==null in receiveEnvelope, toObjectID==" + toObjectID
                          + ", 0x"+Long.toHexString(toObjectID));
            }

            String startTime = "0";
            if (tr.event && Trace.ON) {
                startTime = NativeSteward.formatNow();
            }

            myDecodingState = DECODING_ENVELOPE;
            boolean handoffFailed = false;
            try {
                RtSealer seal = (RtSealer) myDecoder.decodeGraph();
                
                // XXX TAKE THIS OUT
                myCurrentlyReceivedSealer = seal;
                // XXX TAKE THIS OUT

                RtExceptionEnv ee = (RtExceptionEnv) myDecoder.decodeGraph();
                Object[] args = (Object[]) myDecoder.decodeGraph();
                envelope = new RtEnvelope(seal, args, ee);

                // XXX TAKE THIS OUT
                myCurrentlyReceivedSealer = null;
                // XXX TAKE THIS OUT
                
            } catch (HandoffException e) {
                envelope = null;
                handoffFailed = true;
            }

            myDecodingState = DECODING_EXCEPTION_ENVIRONMENT;
      
            if (handoffFailed || myDecodingState == DECODING_FAILED) {
                if (envelope == null) {
                    tr.errorm("Handoff failure with null envelope");
                }
                else {
                    envelope.sendException(new EHandoffException());
                }
            } else if (toObject != null) {
                if (tr.event && Trace.ON) {
                    String endTime = NativeSteward.formatNow();
                    theTotalTime += ((new Long(endTime)).longValue() - (new Long(startTime)).longValue());
                    tr.eventm('\n' + startTime + 
                              " received " + toObject + " <- " + envelope + 
                              " {" + envelope.getKeeper() + "}\n" + endTime + " is when decode completed, total time is "+theTotalTime+".");
                }
                myInnerReceiver.receive(toObject, envelope);
            } else {
                tr.errorm("toObject still null");
            }
        } catch (Exception e) {
            // @@@ XXX some kind of error
            tr.errorm("exception decoding envelope", e);
        }
        
        myDecodingState = DECODING_NOTHING;
    }

    /**
     * Receive notification of new classes for the type table.
     *
     * @param packetBytes The message received
     */
    private void receiveNewClasses(byte packetBytes[]) {

        DataInput msgIn;
        if (!myConnection.myAgreededProtocol.equals("2")) {
            msgIn = new RtDecoderDataInputStream(new ByteArrayInputStream(packetBytes
                            ), myFastStringsInverse);
        } else {
            msgIn = new DataInputStream(new ByteArrayInputStream(packetBytes));
        }
        try {
            byte header = msgIn.readByte(); /* Seen it, discard */
            int size = msgIn.readInt();
            for (int i = 0; i < size; i++) {
                String name = msgIn.readUTF();
                // transformation to avoid comm-system flag day
                if (name.endsWith("_$_Proxy")) {
                    name = name.substring(0, name.length() - 5) + "Deflector";
                }
                int code = msgIn.readInt();
                if (!myTypeTable.registerClassByName(name, code)) {
                    /* XXX Invoke remote class load getClassesRemotely() here*/
                    throw new ClassNotFoundException(" Can't find class " +
                                                     name);
                }
            }
        } catch (Exception e) {
            tr.errorm("exception receiving class", e);
        }
    }
}

public class EHandoffException extends RuntimeException {
    EHandoffException() { super("Handoff failed"); }
}

public class HandoffException extends RuntimeException {
    HandoffException() { }
}
