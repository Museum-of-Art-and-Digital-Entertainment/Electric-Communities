package ec.e.db;

import ec.tables.IntKeyTable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.upgrade.UpgradeTable;
import java.io.IOException;
import ec.util.NestedException;
import ec.e.upgrade.UpgradeConverter;
import ec.e.upgrade.InterfaceConverter;
import ec.e.upgrade.StateConverter;
import java.io.RandomAccessFile;
import ec.e.stream.StreamDB;
import ec.util.HexStringUtils;

public class RtStandardDecoder implements RtDecoder {
    
    public static Trace tr = new Trace("ec.e.db.RtStandardDecoder");
    public static Trace trEnvelopes = new Trace("ec.e.db.RtStandardDecoder.envelopes");
    
    private RtDecodingManager manager;
    private TypeTable typeTable;
    private IntKeyTable idToObjectTable = new IntKeyTable(10, false);
    private DataInput is;
    private RtByteArrayInputStream stream;
    private Hashtable myFastStrings;
    private boolean myUseSpecialDataStream;
    private boolean decodingRoot = false;
    private long objectBoundary;
    private boolean trusted;
    private Vector objectsToAwake;
    private RtDecodingParameters parameters;
    private UpgradeTable upgradeTable;
    private static boolean isSpamming = true; // set to false and compile -O
                                              // to remove from bytecode for
                                              // for performance
    
    static {
        RtSpecialCoder.initialize();
    }
    
    private void setup (RtDecodingManager man, TypeTable table,
                        RtDecodingParameters parameters,
                        UpgradeTable upgradeTable) {
        if (man == null)
            manager = RtDecodingManagerDefault.theManager;
        else
            manager = man;
        typeTable = table;
        trusted = true;
        this.parameters = parameters;
        this.upgradeTable = upgradeTable;
    }
    
    public RtStandardDecoder (RtDecodingManager man, TypeTable table,
                              byte bytes[], RtDecodingParameters parameters,
                              UpgradeTable upgradeTable, 
                              boolean useSpecialDataStream,
                              Hashtable fastStrings) {
        setup(man, table, parameters, upgradeTable);
        stream = new RtByteArrayInputStream(bytes);
        myUseSpecialDataStream = useSpecialDataStream;
        myFastStrings = fastStrings;
        if (useSpecialDataStream) {
            is = new RtDecoderDataInputStream(stream, myFastStrings);
        } else {
            is = new DataInputStream(stream);
        }
    }
    
    public RtStandardDecoder (RtDecodingManager man, TypeTable table,
                              byte bytes[], RtDecodingParameters parameters, 
                              boolean useSpecialDataStream, Hashtable fastStrings) {
        this(man, table, bytes, parameters, null, useSpecialDataStream, fastStrings);
    }
        
    public RtStandardDecoder (RtDecodingManager man, TypeTable table,
                              byte bytes[], 
                              boolean useSpecialDataStream, 
                              Hashtable fastStrings) {
        this(man, table, bytes, null, null, useSpecialDataStream, fastStrings);
    }
    
    public void resetStreamWithBytes (byte bytes[]) {
        if (stream == null)
            return; /* XXX: Squawk about this */
        stream = new RtByteArrayInputStream(bytes);
        if (myUseSpecialDataStream) {
            is = new RtDecoderDataInputStream(stream, myFastStrings);
        } else {
            is = new DataInputStream(stream);
        }
        trusted = true; /* Shouldn't be necessary, but what the heck */
    }
    
    public void replaceObjectInTable (Object old, Object obj) {
        idToObjectTable.replace(old, obj);
    }
    
    public void insertObjectInTable (int index, Object obj) {
        idToObjectTable.put(index, obj);
    }

    public Object decodeGraph() throws IOException {
        //XXX for now
        return decodeObject();
    }

    public void delay(int delayCategory, Runnable thunk) {
        //XXX for now
        thunk.run();
    }
    
    public Object decodeObject () throws IOException {
        int i;
        int awakeSize;
        Object object;
        boolean decodedRoot = false;
        
        if (decodingRoot == false) {
            /* We're decoding a root object, since this is the */
            /* first call into here. Set up context for root call. */
            decodingRoot = true;
            decodedRoot = true;
            trusted = true;
            objectBoundary = 0;
            objectsToAwake = null;
        }
        
        
        boolean savedTrusted = trusted;
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataObject);
        trusted = true; /* Until proven otherwise */
        object = reallyDecodeObject();
        trusted = savedTrusted;

        if (isSpamming && trEnvelopes.debug && (object != null)) {
            Class theClass = object.getClass();
            if (theClass != null) {
                if (theClass.getName().equals("ec.e.run.RtEnvelope")) {
                    trEnvelopes.debugm("Decoded Envelope: " + object);
                }
            }
        }
        
        if (decodedRoot) {
            /* This was a top level call into here, */
            /* so clean up after decoding root object */
            if (objectsToAwake != null) {
                awakeSize = objectsToAwake.size();
                for (i = 0; i < awakeSize; i++) {
                    RtAwakeAfterDecoding awakee =
                        (RtAwakeAfterDecoding)objectsToAwake.elementAt(i);
                    awakee.awakeAfterDecoding();
                }
            }
            idToObjectTable.clear();
            objectsToAwake = null; /* Clean up */
            decodingRoot = false;
        }
        return(object);
    }
    
    private Object reallyDecodeObject () {
        int index;
        int code;
        int type;
        int classCode;
        String className;
        Class theClass;
        Object obj;
        verifyPosition();
        if (tr.tracing)
            tr.$("Verified position before reading in Object");
        
        try {
            while (true) {
                code = is.readInt();
                if (tr.tracing)
                    tr.$("Read in code " + code);
                
                switch (code) {
                    
                    case RtCodingSystem.kcNull:
                        if (tr.tracing)
                            tr.$("Read in null value for Object");
                        return(null);
                    
                    case RtCodingSystem.kcParameterObject:
                        Object paramObj = decodeObject();
                        if (parameters == null)
                            return(manager.handleMissingParameter(paramObj,
                                                              parameters));
                        obj = parameters.get(paramObj);
                        if (obj == null)
                            return(manager.handleMissingParameter(paramObj,
                                                              parameters));
                        return(obj);
                    
                        
                    case RtCodingSystem.kcObjectInfo:
                        Object preobj = null;
                        UpgradeConverter converter = null;
                        index = is.readInt();
                        theClass = getTheClass();
                        if (theClass == null)
                            return(null);
                        if (tr.tracing)
                            tr.$("ObjectInfo - will read " + theClass +
                             ", index " + index);
                        type = is.readInt();
                        if (type == RtCodingSystem.kcManagerEncoded) {
                            obj = manager.decodeObject(theClass, this, index);
                            if (tr.tracing)
                                tr.$("DecodingManager read in object");
                            if (upgradeTable != null) {
                                converter = upgradeTable.getConverter(theClass);
                                if (converter != null) {
                                    InterfaceConverter interfaceConverter =
                                        (InterfaceConverter)converter;
                                    obj = interfaceConverter.wrapIn(obj);
                                }
                            }
                        } else {
                            obj = null; /* Shut java up */
                            Object[] info = RtCodingSystem.
                                specialObjectCoderForClass(theClass);
                            if (info != null) {
                                // XXX - Probably want to insert paranoia like RtDecodeable
                                RtSpecialObjectCoder coder =
                                    (RtSpecialObjectCoder)info[0];
                                if (tr.tracing)
                                    tr.$("Calling decodeSpecialObject to read in object");
                                obj = coder.decodeSpecialObject(theClass,
                                                            info[1], this, index);
                                if (tr.tracing)
                                    tr.$("DecodeSpecialObject read in object " + obj);
                            } else {
                                if (tr.tracing)
                                    tr.$("Calling StreamDB to read in object");
                                preobj = StreamDB.preload(theClass, this);
                                idToObjectTable.put(index, preobj);
                                if (type == RtCodingSystem.kcObjectEncoded) {
                                    obj = decodeSafely(preobj); // Will check for UpgradeWrapper
                                } else {
                                    if (upgradeTable != null) {
                                        converter = upgradeTable.getConverter(theClass);
                                    }
                                    if (converter != null) {
                                        StateConverter stateConverter =
                                            (StateConverter)converter;
                                        obj = stateConverter.decode(preobj, this);
                                    }
                                    else {
                                        obj = StreamDB.load(preobj, this);
                                    }
                                    if (tr.tracing) {
                                        if (obj == null)
                                            tr.$("StreamDB read in null object ");
                                        else
                                            tr.$("StreamDB read in object of class " + obj.getClass());
                                    }
                                }
                            }
                        }
                        if (obj != null) {
                            if (obj instanceof RtAwakeAfterDecoding) {
                                if (objectsToAwake == null)
                                    objectsToAwake = new Vector();
                                objectsToAwake.addElement(obj);
                            }
                            if (obj != preobj) {
                                idToObjectTable.put(index, obj);
                            }
                        }
                        return(obj);
                    
                    case RtCodingSystem.kcObjectID:
                        index = is.readInt();
                        obj = idToObjectTable.get(index);
                        if (obj == null) {
                            if (tr.tracing)
                                tr.$("*** Object NOT in table for index " + index);
                            return(null);
                        } else {
                            if (tr.tracing)
                                tr.$("Object already in table for index " + index);
                            return(obj);
                        }
                    
                    default:
                        tr.$("Unknown stream code: " + code);
                        return(null);
                }
            }
        } catch (Exception e) {
            throw new NestedException("Exception decoding object", e);
        }
    }
    
    Object decodeSafely (Object object) throws IOException {
        RtDecodeable codeable = (RtDecodeable)object;
        trusted = false;
        long savedPosition = objectBoundary;
        int type = is.readInt();
        int id = 0;
        if (type != RtCodingSystem.kcStandardObjectId) {
            id = is.readInt();
            if (type == RtCodingSystem.kcOldUniqueObjectId) {
                return(manager.getUniqueObject(id));
            }
        }
        if (tr.tracing)
            tr.$("Setting savedPosition to " + savedPosition);
        objectBoundary = (long)(is.readByte() << 24) // save the sign
                        | ((long)(is.readUnsignedByte()) << 16) 
                        | ((long)(is.readUnsignedByte()) << 8) 
                        | ((long)(is.readUnsignedByte()) );
        if (tr.tracing)
            tr.$("Boundary is " + objectBoundary);
        try {
            UpgradeConverter converter = null;
            if (upgradeTable != null) {
                converter = upgradeTable.getConverter(object.getClass());
            }
            if (converter != null) {
                StateConverter stateConverter = (StateConverter)converter;
                object = stateConverter.decode(object, this);
            }
            else {
                object = codeable.decode(this);
            }
        } catch (Exception e) {
            tr.errorReportException(e, "Exception decoding object");
        }
        if (tr.tracing)
            tr.$("Object decoded itself as " + object + ", boundary is " +
                 objectBoundary);
        if (objectBoundary != 0)
            setPosition(objectBoundary);
        objectBoundary = savedPosition;
        if ((type == RtCodingSystem.kcNewUniqueObjectId) && (id != 0)) {
            manager.uniqueImportedObject(object, id);
        }
        return(object);
    }
    
    private Class getTheClass () {
        Class theClass = null;
        int index;
        try {
            index = is.readInt();
            if (tr.tracing)
                tr.$("Read in class Index " + index);
            theClass = typeTable.classForIndex(index);
            if (theClass == null) {
                tr.$("Received index for unregistered class: " + index);
            }
        } catch (Exception e) {
            tr.errorReportException(e, "Error trying to read from stream");
            theClass = null;
        }
        return(theClass);
    }
    
    private void verifyDataType (int type) {
        if (tr.tracing)
            tr.$("Verifying data type for " + type);
        int streamType;
        try {
            streamType = is.readByte();
        } catch (IOException e) {
            throw new NestedException("Couldn't read data type", e);
        }
        if (streamType != type) {
            throw new RtDecodingException("Stream type mismatch, expected " +
                                          type + " got type " + streamType);
        }
    }
    
    private void verifyPosition () {
        long pos;
        if (objectBoundary == 0)
            return;
        if (stream != null) {
            pos = stream.position();
        } else {
            try {
                RandomAccessFile file = (RandomAccessFile) is;
                pos = file.getFilePointer();
            } catch (IOException e) {
                throw new NestedException("Couldn't get position in file", e);
            }
        }
        if (pos >= objectBoundary)
            throw new RtDecodingException(
                "Object tried to decode beyond it's ending boundary"
                +"\nobjectBoundary=0x" + Long.toHexString(objectBoundary)
                +" pos=0x" + Long.toHexString(pos)
                +HexStringUtils.byteArrayToReadableHexString(stream.returnBytes()));
    }
    
    private void setPosition (long pos) {
        try {
            if (stream != null) {
                stream.seek(pos);
            } else {
                RandomAccessFile file = (RandomAccessFile) is;
                file.seek(pos);
            }
        } catch (IOException e) {
            throw new NestedException("Couldn't seek", e);
        }
    }
    
    public final void readFully(byte b[], int off, int len)
    throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataBytes);
        verifyPosition();
        int length = is.readInt();
        if (len > length) {
            throw new IOException("Attempt to read beyond end of byte array");
        }
        is.readFully(b, off, len);
    }
    
    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }
    
    public final boolean readBoolean() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataBoolean);
        verifyPosition();
        return(is.readBoolean());
    }
    
    public final byte readByte() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataByte);
        verifyPosition();
        return(is.readByte());
    }
    
    public final int readUnsignedByte() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataByte);
        verifyPosition();
        return(is.readUnsignedByte());
    }
    
    public final short readShort() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataShort);
        verifyPosition();
        return(is.readShort());
    }
    
    public final int readUnsignedShort() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataShort);
        verifyPosition();
        return(is.readUnsignedShort());
    }
    
    public final char readChar() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataChar);
        verifyPosition();
        return(is.readChar());
    }
    
    public final int readInt() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataInt);
        verifyPosition();
        return(is.readInt());
    }
    
    public final long readLong() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataLong);
        verifyPosition();
        return(is.readLong());
    }
    
    public final float readFloat() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataFloat);
        verifyPosition();
        return(is.readFloat());
    }
    
    public final double readDouble() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataDouble);
        verifyPosition();
        return(is.readDouble());
    }
    
    public final String readUTF() throws IOException {
        if (trusted == false)
            verifyDataType(RtCodingSystem.kcDataUTF);
        verifyPosition();
        return(is.readUTF());
    }

    /**
     * Get the keeper for this decoder. Eventually we may want
     * make this something other than always null.
     */
    public RtExceptionEnv getKeeper() {
        return null;
    }
}

class RtDecodingManagerDefault implements RtDecodingManager {
    static final RtDecodingManagerDefault theManager =
        new RtDecodingManagerDefault();
    
    private RtDecodingManagerDefault () {
    }
    
    public Object decodeObject(Class theClass, RtDecoder coder, int index) {
        return(null);
    }
    
    public Object handleMissingParameter(Object paramObj,
                                         RtDecodingParameters parameters)
    {
        if (parameters == null) {
            throw new RtDecodingException(
                "RtDecoder was not given a parameterObjects set for '" +
                paramObj + "'");
        } else {
            throw new RtDecodingException(
                "RtDecoder was not given a parameter for '" + paramObj + "'");
        }
    }
    
    public Object getUniqueObject (int id) {
        return(null);
    }
        
    public void uniqueImportedObject (Object object, int id) {
    }
}

