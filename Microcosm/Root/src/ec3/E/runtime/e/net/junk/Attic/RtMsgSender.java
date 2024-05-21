package ec.e.net;

import ec.e.db.RtEncodingManager;
import ec.e.db.RtStandardEncoder;
import ec.e.db.TypeTable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Vector;

class RtMsgSender implements RtEncodingManager {
    private int myCounter = 0;
    
    static Trace tr = new Trace(false,"[RtMsgSender]");
    public static Trace ptr = new Trace(false,"[RtMsgSenderProfile]");
    
    private TypeTable myTypeTable = new TypeTable(false);
    private RtConnection myConnection;
    private RtStandardEncoder myEncoder;
    private Vector myNewClasses = null;
    private byte myNewClassesBytes[] = null;
    
    static final byte kOutboundID = 0;
    static final byte kInboundID = 1;
    static final byte kHandoffID = 2;
    
    private static Class ourObjectClass = null;
    
    static {
        if (ourObjectClass == null) {
            Object objRef = new Object();
            ourObjectClass = objRef.getClass();
        }
    }
    
    RtMsgSender(RtConnection connection) {
        myConnection = connection;
    }
    
    TypeTable getTypeTable() {
        return myTypeTable;
    }

    public int idForUniqueExportedObject(Object object) {
        Integer oid = null;
        tr.$("entered for " + object);
        synchronized (myConnection) {
            tr.$("Synchronized on connection");
            oid = (Integer)myConnection.getUniqueExports().get(object);
            tr.$("Unique id in export table is " + oid);
            if (oid == null) {
                oid = (Integer) myConnection.getUniqueImports().get(object);
                tr.$("Unique id in import table is " + oid);
            }
        }
        if (oid != null)
            return(oid.intValue());
        else
            return(0);
    }
    
    public int uniqueExportedObject(Object object) {
        /* XXX this should be in RtConnection */
        int id = 0;
        synchronized (myConnection) {
            id = myConnection.nextUniqueId();
            Integer oid = new Integer(id);
            myConnection.getUniqueExports().put(object, new Integer(id));
            myConnection.getUniqueExportsById().put(new Integer(-id), object);
        }
        return(id);
    }
    
    public String willEncodeObjectAsClass(Object obj) {
        String className = null;
        if (obj instanceof EObject_$_Impl) {
            if (obj instanceof EProxy_$_Impl) {
                if (((EProxy_$_Impl)obj).getConnection() == myConnection) {
                    /* Object is on the other side, so class will actually */
                    /* be ignored - indicate that we'll code it though */
                    className = "java.lang.Object";
                } else {
                    /* This is a proxy to elsewhere, send the proxy class */
                    className = obj.getClass().getName();
                }
            } else {
                /* This is an object, so we'll build the proxy class name */
                className = proxyClassName(obj.getClass());
            }
        }
        if (className != null) {
            if (tr.tracing)
                tr.$("Will encode object as " + className);
        } else {
            if (tr.tracing)
                tr.$("Won't encode object");
        }
        return(className);
    }
    
    public void noteNewClass(String className, int classCode) {
        if (myNewClasses == null)
            myNewClasses = new Vector(10);
        myNewClasses.addElement(new ClassMapping(className, classCode));
    }   
    
    private long getExportedID(Object obj) {
        if (tr.tracing)
            tr.$("Checking to see if we've exported object");
        long idCode = myConnection.getExports().alreadyThere(obj);
        if (idCode == 0) {
            idCode = myConnection.getExports().register((EObject)obj);
            if (tr.tracing)
                tr.$("Added object to table as " + idCode);
        } else {
            if (tr.tracing)
                tr.$("Using object code " + idCode);
        }
        return(idCode);
    }
    
    public void encodeObject(Object object, RtEncoder stream) {
        long proxyID = 0;
        long handoffID = 0;
        byte whoseTable;
        EObject_$_Impl obj = (EObject_$_Impl)object;
        RtConnection proxyConnection = null;
        
        if (obj instanceof EProxy_$_Impl) {
            EProxy_$_Impl proxy = (EProxy_$_Impl)obj;
            proxyConnection = proxy.getConnection();
            proxyID = proxy.getIdForConnection(proxyConnection);
            if (proxyConnection == myConnection) {
                if (tr.tracing)
                    tr.$("Got proxy from other side of this connection, proxyID is " + proxyID);
                whoseTable = kInboundID;
            } else {
                if (tr.tracing)
                    tr.$("Got a proxy from another connection, proxyID is " +
                         proxyID);
                whoseTable = kHandoffID;
                handoffID = getExportedID(object);
            }
        } else {
            proxyID = getExportedID(object);
            if (tr.tracing)
                tr.$("Got an object to export, proxyID is " + proxyID);
            whoseTable = kOutboundID;
        }
        try {
            stream.writeByte(whoseTable);
            stream.writeLong(proxyID);
            if (whoseTable == kHandoffID) {
                if (tr.tracing) tr.$("Handoff from originator " + proxyConnection.getRemoteRegistrarId()
                                     + ", sender " + myConnection.getLocalRegistrarId());
                stream.writeLong(handoffID);
                stream.encodeObject(proxyConnection.getRemoteSearchPath());
                stream.encodeObject(proxyConnection.getRemoteRegistrarId());
            }
        } catch (Exception e) {
            /* XXX bad exception usage -- fix */
            tr.$("Exception occured trying to encode EObject");
            e.printStackTrace();
        }
    }
    
    private String proxyClassName(Class theClass) {
        String name = theClass.getName();
        int index = name.lastIndexOf('_');
        if (index == 0) {
            /* XXX - Should raise since not valid E class! */
            return(null);
        }
        String suffix = name.substring(index);
        if (suffix.compareTo("_Closure") == 0) {
            return("ec.e.run.EClosure_Proxy");
        } else {
            return(name.substring(0, index)  + "_Proxy");
        }
    }
    
    void sendEnvelope (long toId, RtEnvelope env) throws Exception {
        long time1 = 0;
        long time2 = 0;
        int i;
        
        if (myEncoder == null) {
            myEncoder = new RtStandardEncoder(this, myTypeTable);
        } else {
            myEncoder.resetStream();
        }
        
        if (tr.tracing)
            tr.$("Sending envelope " + env + " to id " + toId);
        
        /*
        if (++myCounter > 5) {
            try {
                if (extrabytes == null) extrabytes = new byte[200]; 
                myEncoder.writeLong(toId);
                myEncoder.write(extrabytes);
                byte msgBytes[] = myEncoder.getBytes();
                myConnection.sendBlobBytes(msgBytes, RtMsg.kcEnvelope);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        */
    
        /* Assume no new taxes */
        myNewClasses = null;
        myNewClassesBytes = null;
        
        try {
            myEncoder.writeLong(toId);
            if (ptr.tracing)
                time1 = System.currentTimeMillis();
            myEncoder.encodeObject(env);
            if (ptr.tracing) {
                time2 = System.currentTimeMillis();
                ptr.$("Encoding env took " + (time2-time1) + " milliseconds");
            }
            RtExceptionEnv exEnv = RtRun.exceptionEnv();
            if (exEnv != RtRun.NULL_EXCEPTION_ENV) {
                if (tr.tracing)
                    tr.$("Encoding exception environment");
                myEncoder.writeBoolean(true);
                myEncoder.encodeObject(exEnv);
            } else {
                if (tr.tracing)
                    tr.$("Encoding null indication for exception environment");
                myEncoder.writeBoolean(false);
            }
            if (ptr.tracing) {
                time1 = System.currentTimeMillis();
                ptr.$("Encoding exceptionEnv took " + (time1 - time2) +
                      " milliseconds");
            }
            if (myNewClasses != null) {
                int size = myNewClasses.size();
                ByteArrayOutputStream baos =
                    new ByteArrayOutputStream(size*32); /* Guess as to size */
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeInt(size);
                for (i = 0; i < size; i++) {
                    ClassMapping mapping =
                        (ClassMapping)myNewClasses.elementAt(i);
                    dos.writeUTF(mapping.getName());
                    dos.writeInt(mapping.getCode());
                }
                myNewClassesBytes = baos.toByteArray();
                if (ptr.tracing) {
                    time2 = System.currentTimeMillis();
                    ptr.$("Encoding new classes took " + (time2 - time1) +
                          " milliseconds");
                }
            }
        } catch (Exception e) {
            tr.$("Caught exception encoding envelope");
            myNewClasses = null;
            myNewClassesBytes = null;
            throw e;
        }
        try {
            if (ptr.tracing)
                time1 = System.currentTimeMillis();
            if (myNewClasses != null) {
                myConnection.sendBlobBytes(myNewClassesBytes,
                                           RtMsg.kcNewClasses);
                if (ptr.tracing) {
                    time1 = System.currentTimeMillis();
                    ptr.$("Sending new classes bytes took " + (time1 - time2) +
                          " milliseconds");
                }
            }           
            byte msgBytes[] = myEncoder.getBytes();
            myConnection.sendBlobBytes(msgBytes, RtMsg.kcEnvelope);
            if (ptr.tracing) {
                time2 = System.currentTimeMillis();
                ptr.$("Sending message bytes took " + (time2 - time1) +
                      " milliseconds");
            }
        } catch (Exception e) {
            tr.$("Caught exception sending envelope");
            myNewClasses = null;
            myNewClassesBytes = null;
            throw e;
        }
    }
}

class ClassMapping {
    private String myName;
    private int myCode;
    
    ClassMapping(String name, int code) {
        myName = name;
        myCode = code;
    }

    String getName() {
        return myName;
    }

    int getCode() {
        return myCode;
    }
}
