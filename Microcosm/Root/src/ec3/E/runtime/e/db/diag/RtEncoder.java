package ec.e.db;

import ec.e.stream.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import ec.e.net.*;
import ec.e.upgrade.*;
import ec.util.*;

import java.text.DecimalFormat;   // COMMSPAM
import ec.e.file.EStdio;          // COMMSPAM

/** To copy an object over a network connection or into a disk file or
  databse, the in-memory structure of the object needs to be converted
  into a sequence of bytes in a process known as encoding. The opposite
  reconstruction of in-memory objects from a sequence of bytes is known
  as decoding. <p>
  
  Primitive data types have unique type markers and direct
  representations of their data. As an example, a long integer is
  encoded as the constant number RtCodingSystem.kcDataLong followed by
  the bits that maek up the long integer itself.<p>
  
  Arrays are encoded by encoding the signature of the array followed by
  the size of the array, followed by encodings for the data in the
  array. For details of the implementation, see StreamDB.java and
  StreamDB.c<p>
  
  A class can define an overriding encoding method to handle encoding in
  specialized ways, or to disallow it. <p>
  
  RtStandardEncoder is used to encode every object except the few types
  that use RtSpecialEncoder and those types that define their own
  overriding encoding methods.  It works by first encoding the class of
  the object as an integer (the index to a dynamically built Type Table
  of class names) and then the instance variables of the object,
  recursively.
  
  */

public class RtStandardEncoder implements RtEncoder {
    
    private static final int DEFAULT_SIZE = 2000;

    private static final Trace tr = new Trace("ec.e.db.RtStandardEncoder");
    private static final Trace trEnvelopes = new Trace("ec.e.db.RtStandardEncoder.envelopes");


    // I encapsulated all the spamming code in static final boolean
    // if blocks so that javac -O would optimize them out. Alas,
    // javac hung when I tried to build them in the e/db directory.
    public static Trace trSpam = new Trace("commspam"); // COMMSPAM
    public static final boolean isSpamming = true;              // COMMSPAM
    public static final boolean isSpammingVerbose = false;      // COMMSPAM

    private RtEncodingManager manager;
    private TypeTable typeTable;
    private ObjKeyTable objectToIdTable = new ObjKeyTable(0, 10, false);
    private int indexes = 0;
    private DataOutput os;
    private RtByteArrayOutputStream stream = null;
    private boolean trusted;
    private boolean encodingRoot = false;
    private Object rootObject;
    private String className = null;
    private ReadOnlyHashtable props;
    private RtEncodingParameters parameters;
    private UpgradeTable upgradeTable;

    private static boolean isDebugVersion = true; // set to false and compile -O
                                              // to remove verbose spamming from
                                              // bytecode for performance

    private static Object theCurrentEnvelope = null;

    // These are for the comm spamm code.
    public int encodingDepth = 0;                // COMMSPAM
    public Stack commStack = new Stack();        // COMMSPAM
    private static char myIndentChar = '|';      // COMMSPAM
    private static DecimalFormat myDecimalFormat // COMMSPAM
           = new DecimalFormat("00000");         // COMMSPAM
    private String mySpamOutputString = "";      // COMMSPAM


    // DEBUG
    public static String currentEnvelopeSpam() {
        String spam = null;
        if (theCurrentEnvelope != null) {
            spam = theCurrentEnvelope.toString();
        }
        return spam;
    }

    static {
        RtSpecialCoder.initialize();
    }
    
    private void setup (RtEncodingManager ship, TypeTable table,
                        RtEncodingParameters parameters,
                        Hashtable properties,
                        UpgradeTable upgradeTable) {
        if (ship == null)
            manager = RtEncodingManagerDefault.theManager;
        else
            manager = ship;
        typeTable = table;
        trusted = true;
        
        if ((properties != null) &&
            (properties.size() > 0))
            props = new ReadOnlyHashtable(properties);
        else
            props = null;
        if ((parameters != null) &&
            (parameters.size() > 0))
            this.parameters = parameters;
        else
            this.parameters = null;
            
        this.upgradeTable = upgradeTable;
    }
    
    public RtStandardEncoder (RtEncodingManager ship, TypeTable table) {
        this(ship, table, null, null, null);
    }
    
    public RtStandardEncoder (RtEncodingManager ship, TypeTable table,
                              RtEncodingParameters parameters,
                              Hashtable properties) {
        this(ship, table, parameters, properties, null);
    }
    
    /** Main constructor to create an encoder.
      
      @Param ship The encoding manager, typically the calling entity. it
      may be consulted for some policy decisions.
      
      @Param table The typetable used for this stream
      (connection/file/database). All objects in the stream need to be
      encoded using the same typetable.
      
      @param parameters An RtEncodingParameters collection of Objects (as
      keys) and token objects (typically Strings) as values. When
      encoding objects, any object that can be found in the parameters
      collection gets replaced by its corresponding token, effectively
      pruning the object graph at that point. Note, that as a special
      case, the root object will *not* be replaced even if it is in the
      table.
      
      @param properties An RtEncodingProperties hashtable of information
      that is made accessible to the Encode() routine of object that
      Encode and Decode themselves rather than using the standard
      encoder.
      
      @param UpgradeTable An UpgradeTable used if coexistence is being
      maintained between two separate systems and different versions
      of classes are being maintained across the systems.
      */
    
    public RtStandardEncoder (RtEncodingManager ship, TypeTable table,
                              RtEncodingParameters parameters,
                              Hashtable properties,
                              UpgradeTable upgradeTable) {
        setup(ship, table, parameters, properties, upgradeTable);
        stream = new RtByteArrayOutputStream(DEFAULT_SIZE);
        os = new DataOutputStream(stream);
    }
    
    public void resetStream () {
        if (stream == null) {
            stream = new RtByteArrayOutputStream(DEFAULT_SIZE);
            os = new DataOutputStream(stream);
        } else {
            stream.reset(); // Clean out the ByteArrayOutputStream
        }
        trusted = true; /* Shouldn't be necessary, but what the heck */
        indexes = 0;
        encodingDepth = 0;
    }
    
    /** Access the previously given properties table, if any. This is
      used by objects that encode themselves and need special external
      information to do the encoding correctly. As an example, some
      objects need information about the receiving end of a network
      connection to encode special variants of themselves, such as
      client entities with differing capabilities */
    
    public ReadOnlyHashtable getProperties() {
        return props;
    }
    
    public byte[] getBytes () {
        return stream.toByteArray();
    }
    
    public byte[] getBytesAndClose () {
        byte[] ret = stream.toByteArray();
        if (ret.length > DEFAULT_SIZE) {
            stream = null;          // Free up the space
            os = null;              // and the pointer in the DataOutputStream
        } else {
            stream.reset();         // Keep the byte array and objects
        }
        return ret;
    }
    
    public long getPosition () {
        return stream.position();
    }
    
    private void setPosition (long pos) {
        stream.seek(pos);
    }
    
    public String classNameBeingEncoded () {
        return className;
    }

    public void encodeGraph(Object obj) throws IOException {
        //XXX for now
        encodeObject(obj);
    }
    
    /** 
     * Encode an object. Called to encode bona fide objects (as opposed
     * to primitive data types and arrays).
     */
    public void encodeObject (Object object) throws IOException {
        boolean savedTrusted;

        long myStartPosition = getPosition(); // COMMSPAM
        
        if (encodingDepth == 0) {
            /* We're encoding a root object, since this is the */
            /* first call into here. Set up context for root call. */
            rootObject = object;
            trusted = true;
            if (isSpamming && trSpam.debug && Trace.ON) {
                commStack.push("Root Object");
            }
        }
        if (trusted == false) {
            try {
                if (isSpammingVerbose && trSpam.debug && Trace.ON) {
                    ++encodingDepth;
                    commStack.push("Data Type Byte");
                    commBytes(1);
                    --encodingDepth;
                }
                os.writeByte(RtCodingSystem.kcDataObject);
            } catch (IOException e) {
                throw new NestedException("Couldn't write data type byte", e);
            }
        }
        
        Throwable throwable = null;     
        if (object == null) {
            if (isSpamming && trSpam.debug && Trace.ON) {
                ++encodingDepth;
                commStack.push("Null Object");
                commBytes(4); // size of NullObject
                --encodingDepth;
            }
            encodeNullObject();
        }        
        else {  
            savedTrusted = trusted;
            trusted = true; /* Assume the best */
            try {
                ++encodingDepth;
                reallyEncodeObject(object);
                --encodingDepth;
            } catch (Throwable t) {
                // This will give us a nice serial trace of the Object graph
                // leading up to this exception, useful to find out what
                // we were encoding that lead to this error.
                if (tr.error) tr.errorm("Exception encoding " + object);
                throwable = t;
            }
            trusted = savedTrusted;
        }
            
        if (encodingDepth == 0) {
            /* This was a top level call into here, */
            /* so clean up after encoding root object */
            objectToIdTable.clear();
            if (isDebugVersion) {
                theCurrentEnvelope = null;
            }
            if (isSpamming && trSpam.debug && Trace.ON) {
                commBytes((getPosition()-myStartPosition));
                reportSpam() ;
            }

        }
        
        if (throwable != null)  {
            if (throwable instanceof NestedException)  {
                NestedException nestedException = (NestedException)throwable;
                throw nestedException;
            }
            else {
                throw new NestedException ("Encoding error", throwable);
            }
        }       
    }
    
    private void encodeNullObject () {
        if (tr.tracing)
            tr.$("Encoding null object");
        try {
            os.writeInt(RtCodingSystem.kcNull);
        } catch (IOException e) {
            throw new NestedException("Encoding null object", e);
        }
    }   
    
    private void reallyEncodeObject (Object object) throws IOException {
        int classCode;
        int index = 0;  /* Stupid compiler */
        String savedClassName = className;
        boolean managerWillEncode = false;
        boolean objectWillEncode = false;
        int alreadyEncodedId = 0;
        Class theClass = null;
        boolean isRtEncodeable = (object instanceof RtEncodeable);
        UpgradeConverter converter = null;
        
        // If the Object is DelegatingEncodeable, it might want
        // us to encode another Object in its place. We'll give it
        // first crack at that before we check anything else.
        while ((object != null) && 
               (object instanceof RtDelegatingEncodeable))  {
            Object delegate = ((RtDelegatingEncodeable)object).delegateToEncode();
            if (delegate == object)  {
                break;
            } 
            object = delegate;
        }
        /* Handle the case of a Null Object */      
        if (object == null) {
            encodeNullObject();
            return;
        }
        
        /* used to catch&ignore rest of method */
        if (tr.tracing) {
            tr.$("Checking to see if object already in table (" +
                 object + ")");
        }
        if ((alreadyEncodedId = objectToIdTable.get(object)) != 0) {
            if (tr.tracing) {
                tr.$("Already wrote object for id " + alreadyEncodedId +
                     " sending Object ID");
            }
        } else {
            index = ++indexes;
            if (tr.tracing) {
                tr.$("New object for id " + indexes +
                     " will encode the object");
            }
            objectToIdTable.put(object, index);
        }
        
        /* Check if we want to prune object by replacing it with some
           parameter token */
        
        if ((parameters != null) && object != rootObject) {
            /* Don't replace out root object (!) */
            Object borderObject = parameters.get(object);
            if (borderObject != null) {
                if (tr.tracing)
                    tr.$("Encoding parameter object");
                /*used to catch&ignore */
                /* Remember that it is a parameter */
                if (isSpamming && trSpam.debug && Trace.ON) {
                    commStack.push("Parameter Object");
                    commBytes(4);
                }
                os.writeInt(RtCodingSystem.kcParameterObject);
                /*used to catch&ignore */
                /* Now write out the parameter token object */
                encodeObject(borderObject);
                return;
            }
        }
        
        if (isRtEncodeable) {
            objectWillEncode = true;
            className =
                ((RtEncodeable)object).classNameToEncode((RtEncoder)this);
        } else {
            className = manager.willEncodeObjectAsClass(object);
            if (className != null)
                managerWillEncode = true;
        }
        
        if (className == null) {
            theClass = object.getClass();
            className = theClass.getName();
        }
        
        if ((classCode = typeTable.indexForClassName(className)) == 0) {
            if (tr.tracing) {
                tr.$("Class " + className + " not in table, adding");
            }
            classCode = typeTable.registerClass(className, theClass);
            if (tr.tracing) {
                tr.$("Class code " + classCode + " for " + className);
            }
            manager.noteNewClass(className, classCode);
        } else if (tr.tracing) {
            tr.$("Using class code " + classCode + " for " + className);
        }
        
        if (alreadyEncodedId != 0) {
            if (isSpamming && trSpam.debug && Trace.ON) {
                commStack.push("Already Encoded Object");
                commBytes(8);
            }
            os.writeInt(RtCodingSystem.kcObjectID);
            os.writeInt(alreadyEncodedId);
            className = savedClassName;
            return;
        }

        if (isSpammingVerbose && trSpam.debug && Trace.ON) { 
            commStack.push("[Header Info for: " + className);
            commBytes(12);
        } 

        os.writeInt(RtCodingSystem.kcObjectInfo);
        os.writeInt(index);
        os.writeInt(classCode);

        long startPosition = 0;        // COMMSPAM


        // See if the manager is maintaining coexistence with another
        // system and wants to wrap the object before it is sent out
        if (upgradeTable != null) {
            converter = upgradeTable.getConverter(object.getClass());
        }

/*
   we don't encode envelope instances anymore; MsgSender.send(MsgSender.java) sends the bits.
   so to do the equivalent of Trace_ec.e.db.RtStandardEncoder.envelopes now,
   do Trace_ec.e.net.MsgSender=debug instead.
*/   
/*
        if (isDebugVersion) {
            if (trEnvelopes.debug && Trace.ON) trEnvelopes.debugm("AAAAA  ENCODING "+object+" className '"+className+"'");
            if (className.equals("ec.e.run.RtEnvelope")) { 
                theCurrentEnvelope = object;
                if (trEnvelopes.debug && Trace.ON) {
                    trEnvelopes.debugm("Encoding: " + object);
                }
            }
        }
*/

        if (isSpamming && trSpam.debug && Trace.ON) {  
            startPosition = getPosition(); 
            if (className.equals("ec.e.run.RtEnvelope")) {
                commStack.push("ClassName: " + object.toString());
            } else {
                commStack.push("ClassName: " + className);
            }
        }
        
        if (managerWillEncode) {
            if (isSpammingVerbose && trSpam.debug && Trace.ON) { 
                commStack.push("[Manager Encoded]");
            }

            os.writeInt(RtCodingSystem.kcManagerEncoded);
            if (converter != null)  {
                InterfaceConverter interfaceConverter =
                    (InterfaceConverter)converter;
                object = interfaceConverter.wrapOut(object);
            }
            manager.encodeObject(object, this);
            if (isSpammingVerbose && trSpam.debug && Trace.ON) {  
                commBytes(getPosition() - startPosition);
            }
        } else {
            if (objectWillEncode) {
                os.writeInt(RtCodingSystem.kcObjectEncoded);
                boolean needToEncode = true;
                if (object instanceof RtUniquelyCodeable) {
                    int xid = manager.idForUniqueExportedObject(object);
                    if (xid == 0) {
                        os.writeInt(RtCodingSystem.kcNewUniqueObjectId);
                        xid = manager.uniqueExportedObject(object);
                    } else {
                        os.writeInt(RtCodingSystem.kcOldUniqueObjectId);
                        needToEncode = false;
                    }
                    /* Might still be 0 if manager doesn't support */
                    os.writeInt(xid);
                    if (isSpammingVerbose && trSpam.debug && Trace.ON) {
                        commStack.push("[Uniquely Codeable]");
                        commBytes(getPosition() - startPosition);
                    }
                } else {
                    os.writeInt(RtCodingSystem.kcStandardObjectId);
                    if (isSpammingVerbose && trSpam.debug && Trace.ON) {
                         commStack.push("[Standard Object]");
                         commBytes(getPosition() - startPosition);
                    }
                }
                if (needToEncode) {
                    long endpos;
                    long startpos = getPosition();
                    if (isSpammingVerbose && trSpam.debug && Trace.ON) {
                         commStack.push("Object: " + object.toString());
                    }
                    os.writeInt(0);
                    if (tr.tracing) {
                        tr.$("Class " + className + " (" + classCode +
                             ") is codeable");
                    }
                    /* Don't trust objects that code themselves */
                    trusted = false;
                    if (converter != null)  {
                        StateConverter stateConverter =
                            (StateConverter)converter;
                        stateConverter.encode(object, this);
                    }
                    else {
                        /*used to catch&ignore */
                        RtEncodeable codeable = (RtEncodeable) object;
                        codeable.encode(this);
                    }
                    
                    endpos = getPosition();
                    setPosition(startpos);
                    os.writeInt((int)endpos);
                    if (tr.tracing) {
                        tr.$("Object (" + object + ") encoded itself for " +
                             (endpos - startpos) + " bytes, to boundary " +
                             endpos);
                    }
                    setPosition(endpos); 
                    if (isSpammingVerbose && trSpam.debug && Trace.ON) {
                         commBytes(getPosition() - startPosition);
                    } 
                }
            } else {
                if (isSpammingVerbose && trSpam.debug && Trace.ON) {
                    startPosition = getPosition();
                    commStack.push("[Special Object]");
                }
                if (object instanceof RtDoNotEncode)  {
                    String s = ("Attempt to encode RtDoNotEncode object: " + object);
                    if (tr.error) tr.errorm(s);
                    throw new RtRuntimeException(s); 
                }
                os.writeInt(RtCodingSystem.kcStreamEncoded);
                theClass = object.getClass();
                Object[] info =
                    RtCodingSystem.specialObjectCoderForClass(theClass);
                if (info != null) {
                    /* XXX - Probably want to insert paranoia as with
                       RtEncodeable */
                    /* XXX - Might want to ask SPC what class to encode as
                       well */
                    RtSpecialObjectCoder coder =
                        (RtSpecialObjectCoder)info[0];
                    coder.encodeSpecialObject(object, theClass, info[1],
                                              this);
                } else {
                    if (converter != null)  {
                        StateConverter stateConverter =
                            (StateConverter)converter;
                        stateConverter.encode(object, this);
                    }
                    else {
                        StreamDB.store(object, this);
                    }
                }
                if (isSpammingVerbose && trSpam.debug && Trace.ON) {
                     commBytes(getPosition() - startPosition);
                }
            }
        }
        className = savedClassName;
        if (isSpamming && trSpam.debug && Trace.ON) {
            commBytes(getPosition() - startPosition);
        }
    }
    
    public synchronized void write(int b) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataByte);
        os.write(b);
    }
    
    public synchronized void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }
    
    public synchronized void write(byte b[], int off, int len)
    throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataBytes);
        os.writeInt(len);
        os.write(b, off, len);
    }
    
    public final void writeBoolean(boolean v) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataBoolean);
        os.writeBoolean(v);
    }
    
    public final void writeByte(int v) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataByte);
        os.writeByte(v);
    }
    
    public final void writeShort(int v) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataShort);
        os.writeShort(v);
    }
    
    public final void writeChar(int v) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataChar);
        os.writeChar(v);
    }
    
    public final void writeInt(int v) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataInt);
        os.writeInt(v);
    }
    
    public final void writeLong(long v) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataLong);
        os.writeLong(v);
    }
    
    public final void writeFloat(float v) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataFloat);
        os.writeFloat(v);
    }
    
    public final void writeDouble(double v) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataDouble);
        os.writeDouble(v);
    }
    
    public final void writeBytes(String s) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataBytes);
        os.writeBytes(s);
    }
    
    public final void writeChars(String s) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataChars);
        os.writeChars(s);
    }
    
    public final void writeUTF(String str) throws IOException {
        if (trusted == false)
            os.writeByte(RtCodingSystem.kcDataUTF);
        os.writeUTF(str);
    }

    /**
     * Get the keeper for this encoder. Eventually we may want
     * make this something other than always null.
     */
    public RtExceptionEnv getKeeper() {
        return null;
    }

    /**
     * Verbosity mode: On!  COMMSPAMM
     */
    public void commBytes(long l) {  // COMMSPAM
        if (isSpamming && trSpam.debug && Trace.ON) {
            commSpam(l, (String)commStack.pop());
        }
    }

    public void commSpam(long bytes, String s) {  // COMMSPAM
        if (isSpamming && trSpam.debug && Trace.ON) {
            String thisLine = myDecimalFormat.format(bytes) + " ";
            for (int i = 0; i < encodingDepth; i++) {
                thisLine += myIndentChar;
            }
            thisLine += s;
            mySpamOutputString = thisLine + '\n' + mySpamOutputString;
            //EStdio.out().println(thisLine);
        }
    }

    public void reportSpam() {  // COMMSPAM
        if (isSpamming && trSpam.debug && Trace.ON) {
            EStdio.out().print(mySpamOutputString);
            mySpamOutputString = "";
        }
    }
}

/** RtEncodingmanagerDefault implements a default encoding
  manager with trivial policies. */

class RtEncodingManagerDefault implements RtEncodingManager {
    static final RtEncodingManagerDefault theManager =
        new RtEncodingManagerDefault();
    
    /** null constructor */
    
    private RtEncodingManagerDefault () {
    }
    
    /** By returning a non-null String to be used as a class name,
      willEncodeObjectAsClass() can override the normal object
      encoding for some or all objects. The trivial default encoder
      returns null. */
    
    public String willEncodeObjectAsClass (Object obj) {
        return null;
    }
    
    /** If this class name string returned from
      willEncodeObjectAsClass() is non-null, then the manager will be
      asked to encode the object using the encodeObject() method.  In
      this trivial default encoding manager, encodeObject() can be
      empty since willEncodeObjectAsClass declared we would let the
      encoder do it directly. */
    
    public void encodeObject(Object obj, RtEncoder coder) {
    }
    
    /** If idForUniqueExportedObject() returns a nonzero integer then
      that number becomes a type identifier to be used for this (kind
      of) object. This manager returns 0, which instructs the encoder
      to use its own mechanisms. */
    
    public int idForUniqueExportedObject (Object object) {
        return 0;
    }
    
    /** If you returned a nonzero identifier from
      idForUniqueExportedObject() above, then you can maintain your
      own unique object identification scheme for objects of that
      type. This method should then return an integer to identify its
      object argument. */
    
    public int uniqueExportedObject (Object object) {
        return 0;
    }
    
    /** noteNewClass() gets called whenever a class has not been
      previously encountered by the encoder. This method gives the
      encoding manager a chance to keep track of all class types.  The
      trivial default encoder relies on the regular encoding
      mechansims and therefore does nothing. */
    
    public void noteNewClass(String className, int classCode) {
    }
}

