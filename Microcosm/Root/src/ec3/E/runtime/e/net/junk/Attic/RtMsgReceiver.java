package ec.e.net;

import ec.e.db.RtDecodingException;
import ec.e.db.RtDecodingManager;
import ec.e.db.RtDecodingParameters;
import ec.e.db.RtStandardDecoder;
import ec.e.db.TypeTable;

class RtMsgReceiver implements RtDecodingManager {
    private static final int DECODING_NOTHING               = 0;
    private static final int DECODING_ENVELOPE              = 1;
    private static final int DECODING_EXCEPTION_ENVIRONMENT = 2;
    private static final int DECODING_FAILED                = 3;
    private int myCounter = 0;
    private int myDecodingState = DECODING_NOTHING;

    static Trace tr = new Trace(false,"[RtMsgReceiver]");
    public static Trace ptr = new Trace(false,"[RtMsgReceiverProfile]");

    private TypeTable myTypeTable = new TypeTable();
    private RtConnection myConnection;
    private RtStandardDecoder myDecoder;

    public RtMsgReceiver (RtConnection connection) {
        myConnection = connection;
    }

    TypeTable getTypeTable() {
        return myTypeTable;
    }

    public Object decodeObject(Class theClass, RtDecoder stream, int objectId){
        long proxyID;
        byte whichSide;

        try {
            whichSide = stream.readByte();
            proxyID = stream.readLong();
        } catch (Exception e) {
            /* XXX bad exception handling -- fix */
            tr.$("Exception trying to read Proxy ID");
            e.printStackTrace();
            return(null);
        }
        /* If the ID was from the sender's inbound side, i.e. my export ... */
        if (whichSide == RtMsgSender.kInboundID) {
            /* Represents an object on this side of the connection */
            return(myConnection.getExports().get(proxyID));
        } else if (whichSide == RtMsgSender.kHandoffID) {
            /* Represents a Handoff */
            long handoffID;
            Object handoff;
            String originatorPath;
            String originatorRegistrarId;
            try {
                handoffID = stream.readLong();
                originatorPath = (String) stream.decodeObject();
                originatorRegistrarId = (String) stream.decodeObject();
            } catch (Exception e) {
                /* XXX bad exception handling -- fix */
                tr.$("Exception trying to read handoff Proxy info");
                e.printStackTrace();
                return(null);
            }
            tr.$("Handoff from originator " + originatorPath + "/" +
                 originatorRegistrarId);
            handoff = myConnection.getProxyForHandoff(proxyID, theClass,
                originatorRegistrarId, originatorPath,
                myConnection.getRemoteRegistrarId());
            if (handoff == null) {
                if (myDecodingState != DECODING_ENVELOPE) {
                    if (tr.tracing)
                        tr.$("Couldn't decode Exception Environment");
                    myDecodingState = DECODING_FAILED;
                    /* We either failed during high priority message handling,
                       or when decoding the Exception Environment.  We can use
                       a proxy to a proxy in these cases, only because they are
                       all within code executing in the TCB. */
                    /* XXX - Might want to reconsider doing this in some cases,
                       but we have to at least do it when we can't get the
                       Exception Environment, so we can tell it we couldn't
                       decode it! */
                    return(myConnection.getImports().registerProxy(theClass,
                                                                   handoffID));
                } else {
                    throw new RtHandoffException();
                }
                /*return(myConnection.getImports().registerProxy(theClass,
                                                                handoffID)); */
            } else {
                return(handoff);
            }
        } else {
            /* EObject belongs to the other side, so build a proxy */
            Object object = myConnection.getImports().get(proxyID);
            if (object == null)
                object = myConnection.getImports().registerProxy(theClass,
                                                                 proxyID);
            return(object);
        }
    }

    public Object getUniqueObject (int id) {
        Object obj = null;
        synchronized (myConnection) {
            Integer oid = new Integer(id);
            if (id < 0) {
                /* Negative number means we exported this first */
                obj = myConnection.getUniqueExportsById().get(oid);
            } else {
                obj = myConnection.getUniqueImportsById().get(oid);
            }
        }
        return(obj);
    }

    public void uniqueImportedObject (Object object, int id) {
        synchronized (myConnection) {
            myConnection.getUniqueImports().put(object, new Integer(-id));
            myConnection.getUniqueImportsById().put(new Integer(id), object);
        }
    }

    /*
    public Class handleClassFault (String className) {
        Vector classes;
        Class theClass = null;
        try {
            if (RtConnection.loaderReq != null)
                theClass = RtConnection.loaderReq.loadClass(className);
            if (theClass != null) return(theClass);
        } catch (Throwable t) {
            // OK, we'll just have to initiate remote class load
        }
        Vector classNameVector = new Vector(1);
        classNameVector.addElement(new ClassInfo(className, null));
        classes = myConnection.getClassesRemotely(classNameVector);
        if ((classes == null) || (classes.size() < 1)) {
            tr.$("Couldn't get class " + className);
            return(null);
        }
        // XXX - Assumes element at index 0 is the class we wanted
        // to load in the first place (className)
        Class loadedClass = (Class)classes.elementAt(0);
        if (loadedClass != null) {
        } else {
            tr.$("Loaded class returned for " + className + " is null!");
        }
        return(loadedClass);
    }
    */

    public void handleEnvelopeMessage (RtMsgQEntry thisMsg) {
        long forId;
        long time1 = 0;
        long time2 = 0;

        myDecodingState = DECODING_NOTHING;

        if (myDecoder == null) {
            myDecoder = new RtStandardDecoder(this, myTypeTable,
                                              thisMsg.getMsg());
        } else {
            myDecoder.resetStreamWithBytes(thisMsg.getMsg());
        }

        try {
            switch(thisMsg.getMsgCode()) {

                case RtMsg.kcEnvelope:
                    long toId = myDecoder.readLong();
                    EObject_$_Impl obj = myConnection.getExports().get(toId);
                    if (tr.tracing) {
                        if (obj != null) {
                            tr.$("Located target object of class: " +
                                 obj.getClass().getName() + ", id " + toId);
                        } else {
                            tr.$("Could not locate object for id " + toId);
                            tr.$("In message from " +
                                 myConnection.getRemoteRegistrarId());
                            return;
                        }
                    }
                    
                    boolean handoffFailed = false;
                    RtEnvelope env = null;
                    myDecodingState = DECODING_ENVELOPE;
                    
                    if (ptr.tracing) time1 = System.currentTimeMillis();
                    try {
                        env = (RtEnvelope) myDecoder.decodeObject();
                    } catch (RtHandoffException e) {
                        /* XXX bad exception handling -- fix */
                        if (tr.tracing)
                            tr.$("Exception trying to decode handoff");
                        e.printStackTrace();
                        handoffFailed = true;
                    }
                    
                    if (ptr.tracing) {
                        time2 = System.currentTimeMillis();
                        ptr.$("Decoding env took " + (time2 - time1) +
                              " milliseconds");
                    }
                    RtExceptionEnv exEnv = null;
                    myDecodingState = DECODING_EXCEPTION_ENVIRONMENT;
                    boolean haveExceptionEnv = myDecoder.readBoolean();
                    if (haveExceptionEnv) {
                        if (tr.tracing)
                            tr.$("Decoding exception environment");
                        exEnv = (RtExceptionEnv) myDecoder.decodeObject();
                    } else {
                        if (tr.tracing)
                            tr.$("No exception env sent, using top level");
                        exEnv = null;
                    }
                    if (ptr.tracing) {
                        time1 = System.currentTimeMillis();
                        ptr.$("Decoding exceptionEnv took " + (time1 - time2) +
                              " milliseconds");
                    }
                    
                    if (handoffFailed || (myDecodingState == DECODING_FAILED)) {
                        myConnection.throwHandoffFailed(exEnv);
                    } else if (obj != null) {
                        if (RtRun.isCausalityTracing())
                            RtRun.internalDispatch(obj, env, exEnv,
                                "\t[Comm message received on " + myConnection +
                                "]\n");
                        else
                            RtRun.internalDispatch(obj, env, exEnv, null);
                    } else {
                        System.out.println("[RtMsgReceiver] handleEnvelopeMessage: message sent to null target, envelope: " + env);
                    }
                    break;

                /* ----------------------------------------------------------*/
                /*          Distributed Garbage Collection messages          */
                /* ----------------------------------------------------------*/

                case RtMsg.kcSuspectTrash:
                    forId = myDecoder.readLong();
                    int referenceCount = myDecoder.readInt();
                    myConnection.getExports().dgcSuspectTrash(forId,
                                                              referenceCount);
                    break;

                case RtMsg.kcWRemoveMe:
                    forId = myDecoder.readLong();
                    myConnection.getImports().unRegister(forId);
                    break;
            }
        } catch (Exception e) {
            /* XXX bad exception handling -- fix */
            System.out.println(
                "Exception occured processing incoming envelope");
            e.printStackTrace();
        }
        myDecodingState = DECODING_NOTHING;
    }

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
}
