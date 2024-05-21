package ec.e.net;

import ec.util.HexStringUtils;
import ec.e.db.RtEncodingManager;
import ec.e.db.RtEncodingParameters;
import ec.e.db.RtStandardEncoder;
import ec.e.db.RtEncoderDataOutputStream;
import ec.e.db.TypeTable;
import ec.e.net.steward.NetworkSender;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.net.steward.Proxy;

import java.util.Enumeration;

import ec.util.NativeSteward; // XXX for spam only

/**
 * Object that will send network communications, accepting them at the message
 * level and actually sending them at the packet level.
 */
public class MsgSender implements RtEncodingManager {
    private static final Trace tr = new Trace("ec.e.net.MsgSender");

    // for spam only
    private static long theTotalTime = 0;
    
    private MsgConnection myMsgConnection;
    private EConnection myConnection;
    private NetworkSender myOuterSender;
    private RtStandardEncoder myEncoder;
    private TypeTable myTypeTable;
    private ImportExportTables myTables;
    private Vector myNewClasses;
    private byte myNewClassesBytes[];
    private Hashtable myFastStrings;
    
    //private Hashtable myStrings = new Hashtable();

    /**
     * Construct a new MsgSender.
     *
     * @param connection The MsgConnection we are servicing
     * @param innerConnection The EConnection from which we derive
     * @param outerSender The NetworkSender that will actually transmit for us
     */
    public MsgSender(MsgConnection connection,
                     NetworkSender outerSender, String remoteAddr) {
        myMsgConnection = connection;
        myOuterSender = outerSender;
        myNewClasses = null;
        myNewClassesBytes = null;
    }

    public void setInnerConnection(EConnection innerConnection, RtEncodingParameters parameters) {
        myConnection = innerConnection;
        myTables = innerConnection.importExportTables();
        myTypeTable = new TypeTable();
        myFastStrings = myConnection.getTheFastStrings();
        myEncoder = new RtStandardEncoder(this, myTypeTable, 
                            parameters, null,
                            !myMsgConnection.myAgreededProtocol.equals("2"),
                            myFastStrings);
    }
    

    /**
       Enable the protocol stack, allowing bytes to be sent.
    */
    public void enable() throws IOException {
        myOuterSender.enable();
    }

    void disown() {
        myMsgConnection.disown();
    }

    /**
       close this connection.
    */
    public void close(Throwable cause) throws IOException {
        // cause will be null on suspension
        if (myTables != null && cause != null) {
            myTables.exports().unregisterAll(cause);
            myTables.imports().unregisterAll();
        }
        //dumpStringTable();
        myOuterSender.close();
    }

    /**
      * Send a raw packet over a connection to a remote machine.
      *
      * @param packet A filled-in ByteArrayOutputStream containing the packet.
      */
    void sendPacket(ByteArrayOutputStream packet) throws IOException {
        byte[] msg = packet.toByteArray();
        if (msg.length > NetworkSender.MAX_PACKET_LENGTH || msg.length < 0) {
            throw new IOException("Packet too large: " + msg.length +
                                  " > " + NetworkSender.MAX_PACKET_LENGTH);
        }
        myOuterSender.write(msg);
        updateByteStats(msg.length);
    }

    /**
      * Change the protocol on the connection.
      *
        @param isAggragating whether to combine message for compression etc.
      * @param macType the Message Authentication Code algorithm to use.
      * @param macKey the key for the MAC.
      * @param encryptionType the symmetric encryption algorithm to use.
      * @param encryptionKey the key for the symmetric encryption.
      * @param outIV the Initialization Vector for encrypting the outbound stream.
      * @param inIV the Initialization Vector for decrypting the inbound stream.
      * @param isCompressing whether to use ZIP compression.
      * @param useSmallZip whether to generate ZIP headers and checksums.
      * <p>
      * @see java.util.zip.Deflator
      */

    /*package*/ void changeProtocol(
                boolean isAggragating,
                String macType,
                byte[] macKey, 
                String encryptionType,
                byte[] encryptionKey,
                byte[] outIV, 
                byte[] inIV,
                boolean isCompressing,
                boolean useSmallZip) throws IOException {
        myOuterSender.changeProtocol(isAggragating, macType, macKey, 
                                    encryptionType, encryptionKey, outIV, inIV,
                                    isCompressing, useSmallZip);
    }

    /**
     * Send an E message by encoding it into bytes and giving it to the outer
     * sender for transmission.
     *
     * @param proxyDest The proxy to which the e message was sent on this side (for spam).
     * @param toObjectID The remote object ID of the object being sent to
     * @param message An envelope containing the message to send.
     */
    public void send(Object proxyDest, long toObjectID, RtEnvelope message) {
        myEncoder.resetStream();
        myNewClasses = null;
        myNewClassesBytes = null;

/*
        if (tr.event && Trace.ON) {
            tr.eventm('\n' + NativeSteward.formatNow() + 
                      " sending " + proxyDest + " <- " + message + "\nto proxyID " + 
                      toObjectID + " registrarID "+ 
                      myMsgConnection.remoteRegistrarID()+ 
                      " exceptionenv " + message.getKeeper());
        }
*/
        String startTime = "0";
        if (tr.event && Trace.ON) {
            startTime = NativeSteward.formatNow();
        }

        try {
            myEncoder.writeByte(Msg.ENVELOPE);
            myEncoder.writeLong(toObjectID);
            //myEncoder.encodeGraph(message);
            myEncoder.encodeGraph(message.getSealer());
            myEncoder.encodeGraph(message.getKeeper());
            myEncoder.encodeGraph(message.cloneArgs());
            if (myNewClasses != null) {
                int size = myNewClasses.size();
                ByteArrayOutputStream classBytes =
                    new ByteArrayOutputStream(size*32); /* Guess as to size */
                DataOutput classesOut;
                if (!myMsgConnection.myAgreededProtocol.equals("2")) {
                    classesOut = new RtEncoderDataOutputStream(classBytes, myFastStrings);
                } else {
                    classesOut = new DataOutputStream(classBytes);
                }
                classesOut.writeByte(Msg.NEW_CLASSES);
                classesOut.writeInt(size);
                for (int i = 0; i < size; i++) {
                    ClassMapping mapping =
                        (ClassMapping) myNewClasses.elementAt(i);
                    classesOut.writeUTF(mapping.name());
                    classesOut.writeInt(mapping.code());
                }
                myNewClassesBytes = classBytes.toByteArray();
            }
        } catch (Exception e) {
            if (tr.debug && Trace.ON) tr.debugReportException(e, "exception encoding message " + message);
            // @@@ XXX what exceptions can really happen here?
            myNewClasses = null;
            myNewClassesBytes = null;
            myMsgConnection.noticeProblem(e);
            return;
        }


        if (tr.event && Trace.ON) {
            String endTime = NativeSteward.formatNow();
            theTotalTime += ((new Long(endTime)).longValue() - (new Long(startTime)).longValue());
            tr.eventm('\n' + startTime + 
                      " sending " + proxyDest + " <- " + message + "\nto proxyID " + 
                      toObjectID + " registrarID "+ 
                      myMsgConnection.remoteRegistrarID()+ 
                      " exceptionenv " + message.getKeeper() + 
                      "\n" + endTime + " is end time of encode, total time in encoding is "+theTotalTime+".");
        }

        try {
            if (myNewClasses != null) {
                if (myNewClassesBytes.length > NetworkSender.MAX_PACKET_LENGTH 
                        || myNewClassesBytes.length < 0) {
                    throw new IOException("Packet too large: " + myNewClassesBytes.length +
                                          " > " + NetworkSender.MAX_PACKET_LENGTH);
                }
                myOuterSender.write(myNewClassesBytes);
                updateByteStats(myNewClassesBytes.length);
            }
            byte msgBytes[] = myEncoder.getBytesAndClose();
            if (tr.verbose && Trace.ON) tr.verbosem("sending message: " + HexStringUtils.byteArrayToReadableHexString(msgBytes, 0, msgBytes.length));
            if (msgBytes.length > NetworkSender.MAX_PACKET_LENGTH || msgBytes.length < 0) {
                throw new IOException("Packet too large: " + msgBytes.length +
                                      " > " + NetworkSender.MAX_PACKET_LENGTH);
            }
            myOuterSender.write(msgBytes);
            updateMsgStats(msgBytes.length);
        } catch (Exception e) {
            if (tr.debug && Trace.ON) tr.debugReportException(e, "exception sending message " + message);
            // @@@ XXX what exceptions can really happen here?
            myNewClasses = null;
            myNewClassesBytes = null;
            myMsgConnection.noticeProblem(e);
        }
    }

    /**
     * This method is part of RtEncodingManager interface.
     * @see ec.e.db.RtEncodingManager
     */
    public void encodeObject(Object object, RtEncoder stream) {
        if (tr.debug && Trace.ON) {
            tr.debugm("doing encodeObject(" + object + ", " + stream + ")");
        }
        long proxyID = 0;
        long handoffID = 0;
        byte whoseTable;
        Exportable exportable = (Exportable)object;
        EConnection proxyConnection = null;
        Proxy proxy = Proxy.getProxyTarget(exportable);

        if (proxy != null) {
            proxyConnection = proxy.getConnection();
            proxyID = proxy.getIdForConnection(proxyConnection);
            if (proxyConnection == myConnection) {
                whoseTable = MsgConnection.INBOUND_ID;
                if (tr.debug && Trace.ON) {
                    tr.debugm("sending remote id " + proxyID);
                }
            } else {
                whoseTable = MsgConnection.HANDOFF_ID;
                handoffID = getExportedID(exportable);
                if (tr.debug && Trace.ON) {
                    tr.debugm("sending handoff; path: " +
                        proxyConnection.remoteSearchPath() + "; registrar: " +
                        proxyConnection.remoteRegistrarID());
                }
            }
        } else {
            // Not a Proxy, must be an Object
            proxyID = getExportedID(exportable);
            whoseTable = MsgConnection.OUTBOUND_ID;
            if (tr.debug && Trace.ON) {
                tr.debugm("sending local id " + proxyID);
            }
        }
        try {
            stream.writeByte(whoseTable);
            stream.writeLong(proxyID);
            if (whoseTable == MsgConnection.HANDOFF_ID) {
                stream.writeLong(handoffID);
                stream.encodeGraph(proxyConnection.remoteSearchPath());
                stream.encodeGraph(proxyConnection.remoteRegistrarID());
            }
        } catch (Exception e) {
            tr.errorm("exception encoding object", e);
        }
    }

    /**
     * This method is part of RtEncodingManager interface.
     * @see ec.e.db.RtEncodingManager
     */
    public int idForUniqueExportedObject(Object obj) {
        Integer objectID = null;
        objectID = (Integer) myTables.uniqueExports().get(obj);
        if (objectID == null) {
            objectID = (Integer) myTables.uniqueImports().get(obj);
        }
        if (objectID != null) {
            return objectID.intValue();
        } else {
            return 0;
        }
    }

    /**
     * This method is part of RtEncodingManager interface.
     * @see ec.e.db.RtEncodingManager
     */
    public void noteNewClass(String className, int classCode) {
        if (myNewClasses == null) {
            myNewClasses = new Vector(10);
        }
        // this transformation avoids a comm system flag day
        if (className.endsWith("_$_Deflector")) {
            className = className.substring(0, className.length() - 9) +
                "Proxy";
        }
        // end transformation
        myNewClasses.addElement(new ClassMapping(className, classCode));
    }

    /**
     * This method is part of RtEncodingManager interface.
     * @see ec.e.db.RtEncodingManager
     */
    public int uniqueExportedObject(Object object) {
        int id = myTables.nextUniqueID();
        myTables.uniqueExports().put(object, new Integer(id));
        myTables.uniqueExportsByID().put(new Integer(-id), object);
        return id;
    }

    /**
     * This method is part of RtEncodingManager interface.
     * @see ec.e.db.RtEncodingManager
     */
    public String willEncodeObjectAsClass(Object obj) {
        String className = null;
        if (obj instanceof Exportable) {
            Proxy proxy = Proxy.getProxyTarget(obj);
            if (proxy != null) {
                if (proxy.getConnection() == myConnection) {
                    /* Object is on the other side, so class will actually */
                    /* be ignored - indicate that we'll code it though */
                    className = "java.lang.Object";
                } else {
                    /* This is a proxy on some other connection, so */
                    /* send the deflector class */
                    className = obj.getClass().getName();
                }
            } else {
                /* This is an object, so we'll build the proxy class name */
                className = proxyClassName(obj.getClass());
            }
        }
        return className;
    }

    /**
     * Send a raw packet provided by the caller.
     *
     * @param packetBytes The bytes to send
     */
    void sendPacket(byte packetBytes[]) throws IOException {
        int len = packetBytes.length;
        byte[] bufB = new byte[len];    // Get buffer for output
        System.arraycopy(packetBytes, 0, bufB, 0, len);
        if (len > NetworkSender.MAX_PACKET_LENGTH || len < 0) {
            throw new IOException("Packet too large: " + len +
                                  " > " + NetworkSender.MAX_PACKET_LENGTH);
        }
        myOuterSender.write(bufB);
        updateByteStats(len);
    }

    /**
     * Return the export ID of an object on our connection, giving it such an
     * ID if it doesn't already have one.
     *
     * @param obj The object of interest.
     * @returns The export ID of obj on the this sender's connection.
     */
    private long getExportedID(Exportable obj) {
        long idCode = myTables.exports().alreadyThere(obj);
        if (idCode == 0) {
            idCode = myTables.exports().register(obj);
        }
        return idCode;
    }

    /**
     * Compute the name of the proxy class that corresponds to a given eclass.
     *
     * @param theClass The class whose proxy we are interested in.
     * @returns The class name of the proxy class for theClass.
     */
    private String proxyClassName(Class theClass) {
        // XXX - Has to get smarter to handle Exportables
        // that aren't something ending in _$_blah
        String name = theClass.getName();
        int index = name.lastIndexOf('_');
        if (index == 0) {
            /* XXX - Should raise since not valid E class! */
            return null;
        }
        // turn it into _$_Deflector
        return name.substring(0, index + 1)  + "Deflector";
    }

    /**
     * Update the message statistics.
     *
     * @param the length of the message being sent.
     */
    private void updateMsgStats(int msgLen) {
        myConnection.messagesSent++;
        if (msgLen > myConnection.maxMessageSize) {
            myConnection.maxMessageSize = msgLen;
        }
    }

    /**
     * Update the byte statistics.
     *
     * @param the length of the message being sent.
     */
    private void updateByteStats(int msgLen) {
        if (null == myConnection) return;   // No place for them yet
        if (msgLen > myConnection.maxMessageSize) {
            myConnection.maxMessageSize = msgLen;
        }
    }
/* Comment out code until we need to re-do the string table in ConnectionsManager
    private void dumpStringTable() {
        StringData[] sd = new StringData[myStrings.size()];
        StringBuffer output = new StringBuffer(1024);
        Enumeration en = myStrings.keys();

        for (int i=0; i<sd.length; i++) {
            String s = (String)en.nextElement();
            int n =  ((Integer)(myStrings.get(s))).intValue();
            sd[i] = new StringData(s, n);
        }
        sortStringData(sd);
        
        for (int i=0; i<sd.length; i++) {
            output.append("\n\"").append(sd[i].getString()).append("\",");
            output.append("\t// Count=").append(sd[i].getCount());
        }
        tr.errorm("Dump of strings sent:" + output.toString());
    }

    private static void sortStringData(StringData[] ma) {
    //Sorts on name and types of parameters and return type with a Shell sort
        int h = ma.length >> 1;
        while (h>0) {
            for (int j=h; j<ma.length; j++) {
                int i = j-h;
                StringData r = ma[j];
                while (true) {
                    if (stringDataCompare(r, ma[i]) < 0) {
                        ma[i+h] = ma[i];
                        i -= h;
                        if (i >= 0) continue;
                    } 
                    ma[i+h] = r;
                    break;
                }
            }
            h>>=1;
        }
    }

    // Returns 1:m1>m2, 0:m1==m2, -1:m1<m2
    private static int stringDataCompare(StringData m1, StringData m2) {
        if (null == m1) {
            if (null == m2) return 0;
            return 1;
        } else if (null == m2) return -1;

        int m1Count = m1.getCount();
        int m2Count = m2.getCount();
        int result = m1Count<m2Count ? -1 : (m1Count > m2Count ? 1 : 0);
        //if (result != 0) return result;

        result = m1.getString().compareTo(m2.getString());
        if (0 != result) {
            if (result < 0) return -1;
            return 1;
        }

        return 0;
    }
*/ //End Comment out dumpStringTable


}

class StringData {
    String str;
    int count;

    StringData(String s, int c) {
        str = s;
        count = c;
    }

    String getString() {
        return str;
    }

    int getCount() {
        return count;
    }
}

class ClassMapping {
    private String myName;
    private int myCode;

    ClassMapping(String name, int code) {
        myName = name;
        myCode = code;
    }

    String name() {
        return myName;
    }

    int code() {
        return myCode;
    }
}
