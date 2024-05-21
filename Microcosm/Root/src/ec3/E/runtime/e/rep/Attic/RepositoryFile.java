package ec.e.rep;

import ec.e.start.Tether;
import ec.e.file.*;
import ec.e.serial.Serializer;
import ec.e.serial.Unserializer;
import ec.e.serial.ParamSerializer;
import ec.e.serial.ParamUnserializer;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import ec.e.util.DiscreteEnumeration;
import ec.util.PEHashtable;
import java.io.IOException;

import java.io.EOFException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import ec.e.openers.*;
import ec.cert.CryptoHash;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.rep.steward.RepositoryAccessException;

import ec.e.run.Trace;
import ec.e.file.EStdio;
/* REPTIMESTART */
import ec.util.NativeSteward;
/* REPTIMEEND */

    /**

     * RepositoryHandle instances are used in repository key
     * Hashtables. They contain the RepositoryFile object associated
     * with the file that contains the data, and the file position in
     * that file. <p>

     */

class RepositoryHandle {

    private static OpenerRecipe myMaker = defaultRecipe();
    long myFilePos;
    RepositoryFile myRepositoryFile;
    private static final int VOIDED_RECORD = -1; // Pseudo file position for deleted records.

    /* REPTIMESTART */
    public static Hashtable counters = new Hashtable(20); // Count data types in repository
    public static Hashtable loadTimeTotals = new Hashtable(20); // Count load time by data type
    /* REPTIMEEND */

    RepositoryHandle() {
        myFilePos = -1;
        myRepositoryFile =null;
    }

    /**

     * Constructor. Only used by RepositoryFile.put()

     */

    // XXX Make this a factory and hand one to RepositoryFile

    RepositoryHandle(RepositoryFile repositoryFile, long filePos) {
        myFilePos = filePos;
        myRepositoryFile = repositoryFile;
    }

    static private OpenerRecipe defaultRecipe() {
        Enumeration recipes 
            = new DiscreteEnumeration(new HashtableRecipe(),
                                      new PEHashtableRecipe(),
                                      new ObjKeyTableRecipe());
        ClassRecipe classRecipe 
            = new RepositoryClassRecipe(RootClassRecipe.THE_ONE);

        return new OpenerRecipe(recipes, classRecipe);
    }    

    /**

     * Returns the data in this record as a byte array.
     * This is a package-scope help method.

     */

    byte[] getDataBytes(Object key)
         throws IOException, RepositoryKeyNotFoundException {
             return myRepositoryFile.getDataBytes(key, myFilePos);
    }

    /**

     * Get the size of the object that this RepositoryHandle object
     * stands for. If record has been voided or deleted, or if
     * something throws an error, returns 0; This behaviour is
     * appropriate since the most common use of this method is to use
     * it to determine whether a record actually exists, and in a
     * system using voided and deleted records this may sometimes be
     * hard to determine.

     */

    public int size() {
        try {
            return myRepositoryFile.sizeOfObject(myFilePos);
        } catch (Exception e) {
        }
        return 0;
    }

    /**

     * After reading in the key table, update repository file in all
     * entries to reference the current file instance.

     */

    void updateFile(RepositoryFile newFile) {
        myRepositoryFile = newFile;
    }

    /**

     * Delete the object that this RepositoryHandle object stands for

     */

    public void delete() throws IOException, RepositoryKeyNotFoundException {
        myRepositoryFile.deleteByFilePosition(myFilePos);
        myFilePos = VOIDED_RECORD; // Void this record (Shouldn't be necessary)
    }

    /**

     * Retrieve the data as a byte array and create an object by decoding this.
     * Also note that the key is only there to improve error messages

     * @param parimeters nullOK untrusted - A Hashtable. Note that all
     * put() and encode() methods use PEHashtables, but all get(0
     * methods use regular Hashtables since this is the semantics we
     * want.

     */

    public Object getObject(Object key, Hashtable parimeters) 
         throws IOException, RepositoryKeyNotFoundException {
             /* REPTIMESTART */
             long startTime = NativeSteward.queryTimer();
             /* REPTIMEEND */
             byte[] bytes = myRepositoryFile.getDataBytes(key, myFilePos);
             /* REPTIMESTART */
             Repository.diskTimer += NativeSteward.deltaTimerUSec(startTime);
             startTime = NativeSteward.queryTimer();
             /* REPTIMEEND */
             ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             Object result = (ParamUnserializer.make(byteStream,myMaker,parimeters)).decodeGraph();
             /* REPTIMESTART */
             Repository.unsTimer += NativeSteward.deltaTimerUSec(startTime);
             /* REPTIMEEND */
             long deltaTime = NativeSteward.deltaTimerUSec(startTime);
             /* REPTIMESTART */
             Repository.unsTimer += deltaTime;

             // Gather some statistics on per-class unserializing times.

             String resultClassName = "null";
             if (result != null) resultClassName = result.getClass().toString();
             Long counter = (Long)counters.get(resultClassName);
             if (counter == null) counter = new Long(1);
             else counter = new Long(counter.longValue() + 1);
             counters.put(resultClassName, counter);

             Long timeByClass = (Long)loadTimeTotals.get(resultClassName);
             if (timeByClass == null) timeByClass = new Long(deltaTime);
             else timeByClass = new Long(timeByClass.intValue() + deltaTime);
             loadTimeTotals.put(resultClassName, timeByClass);

             /* REPTIMEEND */
             return result;
    }
}

/**

 * Small subclass to handle tricky data such as %SymbolTable%

 */

class RepositoryDirectDataHandle extends RepositoryHandle {

    private Object myData;

    public RepositoryDirectDataHandle(Object data) {
        myData = data;
    }

    public Object getObject(Object key, Hashtable parimeters) 
         throws IOException, RepositoryKeyNotFoundException {
             return myData;
    }
}

/**

 * KeyPosition is a small utility class that is used when writing and
 * reading out the myKeys hashtable.

 */

class KeyPosition {

    Object myKey;
    long myPosition;

    KeyPosition(Object key, long position) {
        myKey = key;
        myPosition = position;
    }
}

    /** 

     * A RepositoryFile contains encoded (Java) objects, encoded using
     * Openers.  First in the RepositoryFile, in fixed file-positions,
     * are some special-treated data in what is called the FileInfo
     * block, such as the RepositoryFile Version number, and
     * information that allow us to retrieve the keys for the file.<p>

     * Each record in the file contains both the key and the data,
     * encoded using Openers. This allows us to recover the keys in a
     * file by reading the whole file sequentially. However, each file
     * normally contains a hashtable of keys, that is stored at the
     * end of the file. This hashtable is saved under the reserved key
     * "%LocalKeyVector%", just to keep all data in the file conform
     * to a key-data pairing scheme. Note that when we want to add
     * more data to the file, which is done by appending, then we will
     * overwrite this hashtable and will re-write it only when we
     * close the file. The special information (in the beginning of
     * the file) is updated to reflect this in such a way that it is
     * never inconsistent (See below).<p>

     */

public class RepositoryFile {

    /*
     * Lengths of various primitive datatypes when written out to a
     * file.  These should be defined somewhere (by some set of names)
     * systemwide but I could not find them anywhere, not even in
     * /usr/local/java/src/java/DataOutputStream.java .

     */

    static int SIZEOF_INT = 4;
    static int SIZEOF_LONG = 8;
    
    // Other constants and pseudoconstants

    public  static final long REPOSITORYFILE_MAGIC_NUMBER = 0x1717171710001717L; // Swedish cusswords
    public  static final long NO_COMPRESSION = 0;
    private static       long  CURRENT_REPOSITORYFILE_VERSION = 1; // Not final to avoid recompile problems.
    private static final String KEY_VECTOR_PSEUDO_KEY = "%LocalKeyVector%";

    /** Tracing, for debugging */
    //    private static Trace myDebugTrace = Debug.getTrace("RepositoryFile", Debug.DEBUG);
    boolean repositoryFileDebug = false;// for now - couldn't include the above trivially.

    /*

     * The following constants define file offsets to various long
     * integers, collectively known as the File Info, that are written
     * out to the beginning of the file.

     */

    static int MAGIC_NUMBER_POS =  0 * SIZEOF_LONG; // Offset in file to the file format magic number.
    static int VERSION_POS =       1 * SIZEOF_LONG; // Offset in file to the file format version number.
    static int NR_RECORDS_POS =    2 * SIZEOF_LONG; // Number of records excluding kesHashtable, if any.
    static int FILE_LENGTH_POS =   3 * SIZEOF_LONG; // Our opinion of file length (OS may think file is longer)
    static int KEY_TABLE_POS_POS = 4 * SIZEOF_LONG; // Key Hashtable start. If 0, then there is no table in file.
    static int COMPRESSION_POS =   5 * SIZEOF_LONG; // Compression scheme identifier. 0 means no compression.
    static int DUMMY_VALUE_POS =   6 * SIZEOF_LONG; // Gratuituous filler, use for future expansion
    static int DUMMY2_VALUE_POS =  7 * SIZEOF_LONG; // Gratuituous filler, use for future expansion
    static int FIRST_DATA_POS =    8 * SIZEOF_LONG; // First key/data pair. Beginning of file contents.

    EFileReader myFile = null;
    EFileEditor eFile = null;   // Same as myFile, if file is writable - To avoid many typecasts.

    // the File Info variables themselves

    long myMagicNumber;         // Magig number in file. Must match, if not then not a rep. file.
    long myFileVersion;         // Version number of the file.
    long myNumberRecords = 0;   // Number of records in file excluding key hashtable if any
    long myFileLength;          // Next true data record will be written starting here
    long myKeyTablePos = -1;    // Position of key table in file. If -1, then not in file.
    long myCompressionScheme;   // File-wide compression scheme ID. If 0, then no compression.
    long myDummy;               // Not used.
    long myDummy2;              // Not used.

    boolean fileInfoDirty = false; // File info - the above variables - need saving out to file.

    Hashtable myKeys = null;    // Package scope!
    boolean keyHashDirty = false;      // myKeys hashtable has been updated, needs saving to file.

    private OpenerRecipe myMaker = defaultRecipe(); // Allows us to look inside arbitrary objects

    /**

     * Constructor to create a RepositoryFile given an EFileReader or
     * EFileEditor. If the argument is an EFileEdior and the writable
     * flag is given as true, then the RepositoryFile can be written
     * to, otherwise not. 

     * @param aFile notNull - an EFileReader or an EFileEditor
     * @param writable - Specifies whether the Repository should be writable

     */

    RepositoryFile(EFileReader aFile, boolean writable) throws IOException {
        myFile = aFile;
        if (myFile instanceof EFileEditor && writable) {
            eFile = (EFileEditor) myFile;
            EStdio.err().println("Repository is writable");
        }
        if (myFile.length() >= FIRST_DATA_POS) { // Long enough to contain our file info?
            readFileInfo();                      // Y/ read it in.
            if (myKeyTablePos != -1) myFileLength = myKeyTablePos; // Indicate we want to overwrite hashtable in file
            if (myNumberRecords > 0) {           // If there seem to be records
                try {
                    myKeys = readMyKeys(); // Attempt reading the keys hashtable from file end.
                    if (myKeys == null) { // Did not work; We have data in file but no keys??
                        EStdio.err().println("Repository file lacks index. Reconstructing one.");
                        myKeys = regenerateKeys(myNumberRecords, false, null);
                    }
                } catch (IOException e) {
                    EStdio.err().println("Repository file has problems - repairing file...");
                    repairFile(true,true); // Repair the file; report on progress. May throw IOX.
                }
                if (myKeys == null) {
                    throw new IOException("Repository file is corrupted beyond repair");
                }
            } else {                             // Old file, but it contains no records
                myKeys = new Hashtable(50);      // If no records in file, use a fresh hastable.
            }
        } else {

            // This is a new file. Initialize all file info, then write it out.

            myMagicNumber = REPOSITORYFILE_MAGIC_NUMBER;
            myFileVersion = CURRENT_REPOSITORYFILE_VERSION;
            myNumberRecords = 0;
            myFileLength = FIRST_DATA_POS;        // We will start appending to file here.
            myKeyTablePos = -1;                   // If we don't write any records, don't write key table.
            myCompressionScheme = NO_COMPRESSION; // Default, for now.
            myDummy = 0;                          // Not used, for future expansion.
            myDummy2 = 0;                         // Not used, for future expansion.
            fileInfoDirty = true;                 // IF you don't set this, then saveFileInfo won't write!
            saveFileInfo();                       // Write out file header info
            myKeys = new Hashtable(50);           // Create a fresh keys file
        }
    }

    /**

     * Tells whether this file is writable

     */

    public boolean isWritable() {
        return (eFile != null);
    }

    /**

     * Read in our file info block. In the very beginning of the file
     * we allocate space for some un-encoded long integers. These
     * describe trivial details about the formatting of the rest of
     * the file, such as the version of the file layout (the current
     * one is CURRENT_REPOSITORYFILE_VERSION), the number of records
     * in the file (excluding the key hashtable itself), the logical
     * file length (We cannot truncate files using Java) and the byte
     * offset to the key hashtable (if one exists; if it does not
     * exist, then this number - myKeyTablePos - is set to -1). We
     * further leave room for one long integer for possible future
     * expansion. This method moves the file pointer.

     * XXX This file could be encrypted. Would that be a good idea,
     * eventually?

     */

    private void readFileInfo() throws java.io.IOException {
        myFile.seek(MAGIC_NUMBER_POS);

        myMagicNumber = myFile.readLong();

        if (myMagicNumber != REPOSITORYFILE_MAGIC_NUMBER) {
            throw new IOException
              ("File " + myFile + " is not a valid Repository file - Bad magic number");
        }

        myFileVersion = myFile.readLong();
        myNumberRecords = myFile.readLong();
        myFileLength = myFile.readLong();
        myKeyTablePos = myFile.readLong();
        myCompressionScheme = myFile.readLong();
        myDummy = myFile.readLong();
        myDummy2 = myFile.readLong();
        
        if (repositoryFileDebug) {
            EStdio.err().println("Read of File Info: vers = " + myFileVersion +
                           " number records = " + myNumberRecords +
                           " File length = " + myFileLength +
                           " Key table pos = " + myKeyTablePos);
        }
    }

    /**

     * Save out our file info block. Seeks to the beginning oif the
     * file and writes some important variables that describe the file
     * format. This method moves the file pointer.

     */

    private void saveFileInfo() throws java.io.IOException {
        if (! fileInfoDirty) {  // Don't do this unless needed
            return;
        }
        if (eFile == null) {
            throw new IOException("Attempt to write to read-only Repository file");
        }
        if (repositoryFileDebug) {
            EStdio.err().println("Writing File Info: vers = " + myFileVersion +
                                 " number records = " + myNumberRecords +
                                 " File length = " + myFileLength +
                                 " Key table pos = " + myKeyTablePos);
        }

        eFile.seek(MAGIC_NUMBER_POS);
        eFile.writeLong(REPOSITORYFILE_MAGIC_NUMBER);
        eFile.writeLong(myFileVersion);
        eFile.writeLong(myNumberRecords);
        eFile.writeLong(myFileLength);
        eFile.writeLong(myKeyTablePos);
        eFile.writeLong(myCompressionScheme);
        eFile.writeLong(myDummy);
        eFile.writeLong(myDummy2);
        if (myFileLength == FIRST_DATA_POS) eFile.writeLong(0); // Hack to see if this will fix seek() problem.
        fileInfoDirty = false;
    }

    /**

     * Save a data object under a given key. The data is appended to
     * the logical end of the RepositoryFile as indicated by
     * myFileLength. Note that this may not be the actual end of file
     * once the file is closed.  When we write the key hashtable to
     * the file (before closing file) we will not update myFileLength
     * since we don't regard the hashtable as part of the real
     * data. But since the hashtable is of nonzero length, the file is
     * actually longer than myFileLength indicates. <p>

     * First we seek to the end of the file, then the length of the
     * encoded key is written, then the encoded key. Then the
     * length of the encoded data is written, then the enccoded data.
     * The file pointer is left at the (new) end of the file, but this
     * is not important since we actually don't trust it to stay
     * there between calls. <p>

     * A deleted record can cause one of two kinds of entries. An
     * "empty" entry is an entry where the key has the length 0 and
     * the data has the length of the deleted record and deleted key
     * (so as to occupy the same number of bytes). <p>

     * In contrast, a "Voided" entry has a normal key but the data are
     * of length 0. These are used to shadow un-deleted and
     * un-deletable entries in other repositoryfiles. <p>

     * Note that deleting an existing record in the frontmost file
     * smashes the existing record to "empty" first. After that we may
     * or may not add a "Voided" entry to the end of the file, just as
     * any regular record. We cannot just replace a record we are
     * deleting with a voided one since the data length would not be 0
     * and we need to always be able to skip a record, deleted or not,
     * so we can read the file linearly in case we lose the hastable
     * due to file corruption. <p>

     * Note that we can tell the difference between a null stored in
     * the database and a voided record. The real null is encoded to a
     * non-empty byte array, whereas the voided record contains no
     * data bytes at all. <p>

     * @param key untrusted notNull - the key to store the data under.

     * @param data untrusted - the data to store. If null, then an
     * explicit null is stored.

     * @param parimeters nullOK untrusted - A Pointer-Equality
     * Hashtable. Note that all put() and encode() methods use
     * PEHashtables, but all get(0 methods use regular Hashtables
     * since this is the semantics we want.

     * @return RepositoryHandle - an object to retrieve the saved
     * object by. These objects are typically stored in a hashtable by
     * the caller under the same key as the one given to this method,
     * and used to retrieve the object later. The local hashtable is
     * maintained only so it can be written out to the file at closing
     * or checkpointing time.

     */

    public RepositoryHandle put(Object key, Object data, PEHashtable parimeters)
         throws IOException, RepositoryAccessException {
             if (eFile == null) {                    // This equals myFile if file is editable.
                 throw new IOException("Attempt to write to read-only Repository file");
             }

             if (repositoryFileDebug)
                 EStdio.err().println("deleteifexists of " + key);
             deleteIfExists(key);    // Delete old record if there. XXX We are encoding key twice - inefficient?
             // Encode the key

             ByteArrayOutputStream keyByteStream = new ByteArrayOutputStream(300);
             Serializer keySerializer = Serializer.make(keyByteStream, myMaker);
             keySerializer.encodeGraph(key);

             // Encode the data

             ByteArrayOutputStream dataByteStream = new ByteArrayOutputStream(10000);
             Serializer dataSerializer = ParamSerializer.make(dataByteStream, myMaker, parimeters);
             dataSerializer.encodeGraph(data);

             // Call low-level record writer

             if (repositoryFileDebug)
                 EStdio.err().println("Low level write of " + key);
             long newRecordStartPosition = putBytes(keyByteStream.toByteArray(),dataByteStream.toByteArray());

             if (data == myKeys) {
                 return null;
             }

             // Create a RepositoryHandle record that points to us (so we
             // know which file the data is in) and also contains the file
             // position the data starts at.

             if (repositoryFileDebug)
                 EStdio.err().println("Create rephandle for data for " + key);
             RepositoryHandle entry = new RepositoryHandle(this, newRecordStartPosition);
             myKeys.put(key,entry);                  // Add to our hashtable
             keyHashDirty = true; // Remember to write out the myKeys hashtable
             return entry;
    }

    /**

     * Stores an object into the Repository using the cryptographic
     * hash of the data as a key.  It returns that key as an instance
     * of class CryptoHash.

     * @param data suspect nullOK - The object to save in the
     * Repository using an automatically generated key.

     * @return The cryptographic hash of the data (an instance of
     * class CryptoHash), that was used as the key to store the data
     * under.

     * <p><b>NOTE</b> If the data is null, then a special object, the
     * nullCryptoHash object, is returned. This is treated as a
     * special case since java hashtables cannot store null and the
     * serializers store the data in hashtables to detect
     * circularities.

     */

    public CryptoHash putHash(Object data)
         throws IOException, RepositoryAccessException {
             return putHash(data,null);
    }


    public CryptoHash putHash(Object data, PEHashtable parimeters)
         throws IOException, RepositoryAccessException {

             if (data == null) { // Special case this since serializers cannot handle it.
                 return CryptoHash.nullCryptoHash();
             }
             if (eFile == null) {                    // This copies myFile if file is editable.
                 throw new IOException("Attempt to write to read-only Repository file");
             }

             // Encode the data

             ByteArrayOutputStream byteStream = new ByteArrayOutputStream(10000);
             Serializer dataSerializer = ParamSerializer.make(byteStream, myMaker, parimeters);
             dataSerializer.encodeGraph(data);
             byte[] dataBytes = byteStream.toByteArray();

             if (repositoryFileDebug)
                 EStdio.err().println("databytes size is " + dataBytes.length);

             CryptoHash key = new CryptoHash(dataBytes);

             if (exists(key)) return key; // Record already exists. We are done...

             ByteArrayOutputStream keyByteStream = new ByteArrayOutputStream(300);
             Serializer keySerializer = Serializer.make(keyByteStream, myMaker);
             keySerializer.encodeGraph(key);

             // Call low-level record writer

             long newRecordStartPosition = putBytes(keyByteStream.toByteArray(),dataBytes);

             if (data == myKeys) {
                 return null;
             }

             // Create a RepositoryHandle record that points to us (so we
             // know which file the data is in) and also contains the file
             // position the data starts at.

             RepositoryHandle entry = new RepositoryHandle(this, newRecordStartPosition);
             myKeys.put(key,entry);                  // Add to our hashtable
             keyHashDirty = true; // Remember to write out the myKeys hashtable
             return key;
    }

    /**

     * Write a "nonexistent" shadowing record - one which has
     * zero-length data.  A record like this will immediately return
     * the result "key not found" even though other reconrds with the
     * same key may be available in other repositoryfile later in the
     * repository file chain.

     */

    void voidRecord(Object key)
         throws IOException, RepositoryAccessException {
             if (repositoryFileDebug)
                 EStdio.err().println("Voiding record with key " + key);

             if (eFile == null) {                    // This equals myFile if file is editable.
                 throw new IOException("In voiding a record, attempting to write " +
                                       "to closed or read-only Repository file");
             }

             // Encode the key

             ByteArrayOutputStream byteStream = new ByteArrayOutputStream(300); // XXX Tune - max key size
             Serializer keySerializer = Serializer.make(byteStream, myMaker);
             keySerializer.encodeGraph(key);

             // Write out a voided record.

             putBytes(byteStream.toByteArray(),null);
    }

    /**

     * Low-level record write. Key and data are now byte arrays and
     * will be written to disk without further conversions.

     */

    private long putBytes(byte[] keyBytes, byte[] dataBytes)
         throws IOException {
             long newRecordStartPosition = myFileLength; // We write records at logical end of file
             if (repositoryFileDebug)
                 EStdio.err().println("Low level write - seek to " + newRecordStartPosition);
             eFile.seek(newRecordStartPosition); // Seek to start of new record

             if (keyBytes == null)
                 throw new IOException("Low level write - Attempt to write null key");

             int keyBytesWritten = keyBytes.length;
             int dataBytesWritten = 0;

             if (repositoryFileDebug)
                 EStdio.err().println("Low level write - key length is " + keyBytesWritten);
             eFile.writeInt(keyBytesWritten); // Write length of key
             eFile.write(keyBytes);


             if (repositoryFileDebug) {
                 EStdio.err().println("Low level write - data is " + dataBytes);
                 if (dataBytes != null) EStdio.err().println("Low level write - data length is " + dataBytes.length);
             }

             if (dataBytes != null) {
                 dataBytesWritten = dataBytes.length;
                 eFile.writeInt(dataBytesWritten); // Write length of data
                 eFile.write(dataBytes); // Write encoded data
             } else {

                 // We are writing a void record (a "pumpkin"). Write
                 // no data, but still write a (zero) data length.

                 if (repositoryFileDebug)
                     EStdio.err().println("Low level write - Writing a pumpkin");
                 eFile.writeInt(dataBytesWritten); // Write length of data (zero in this case)
             }

             // Update state for this file - Compute new file length etc.

             // Note that if anything goes wrong and throws anywhere
             // above, then we're quite happy to keep myFileLength and
             // myNumberRecords unchanged since that at least keeps
             // the file consistent. We may have lost the data we just
             // wrote but any half-finished disk data will (hopefully)
             // be overwritten next time we call put (barring hard
             // disk write errors), and the key hashtable is not yet
             // updated (and therefore stays consistent).

             myFileLength +=  keyBytesWritten + dataBytesWritten +
               2 * SIZEOF_INT;  // Update file length after write to end.
             myNumberRecords++;                      // Update number of records written
             keyHashDirty = true;                           // Save hashtable in file when exiting or checkpointing
             fileInfoDirty = true;   // We changed several of the variables kept here
             if (repositoryFileDebug)
                 EStdio.err().println("Low level write returning position as " + newRecordStartPosition);
             return newRecordStartPosition;
    }

    /**

     * Retrieve an object, given a key. 

     * @param key notNull untrusted - An object to use as a key.

     */

    Object get(Object key) throws IOException, RepositoryKeyNotFoundException {
        return get(key,null);
    }

    /**

     * Retrieve an object, given a key and a decoding parameter table.

     * @param key notNull untrusted - An object to use as a key.

     * @param parimeters nullOK untrusted - A Hashtable.

     */

    public Object get(Object key, Hashtable parimeters)
         throws IOException, RepositoryKeyNotFoundException {
             /* REPTIMESTART */
             long startTime = NativeSteward.queryTimer();
             /* REPTIMEEND */
             if (myKeys == null) {
                 throw new RepositoryKeyNotFoundException
                   ("Get failed, Key " + key +
                    " not found, because repository not initialized, no key table available");
             }
             if (key == null) {
                 throw new RepositoryKeyNotFoundException("Repository get failed, key was null");
             }
             RepositoryHandle entry = (RepositoryHandle)myKeys.get(key);

             if (entry == null) {
                 throw new RepositoryKeyNotFoundException("Key " + key + " not found");
             }
             /* REPTIMESTART */
             Repository.hashTimer += NativeSteward.deltaTimerUSec(startTime);
             /* REPTIMEEND */
             return entry.getObject(key,parimeters);
    }

    private Object unserialize(byte[] dataAsBytes, Hashtable parimeters) throws IOException {
        /* REPTIMESTART */
        long startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */
        ByteArrayInputStream byteStream = new ByteArrayInputStream(dataAsBytes);
        Unserializer unserializer = ParamUnserializer.make(byteStream,myMaker,parimeters);
        /* REPTIMESTART */
        Repository.setupTimer += NativeSteward.deltaTimerUSec(startTime);
        startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */
        Object result = unserializer.decodeGraph();
        /* REPTIMESTART */
        Repository.unsTimer = NativeSteward.deltaTimerUSec(startTime);
        /* REPTIMEEND */
        return result;
    }

    /**

     * Retrieve data as a byte array, given a file position.
     * Package scope help function only

     */

    byte[] getDataBytes(Object key, long filePos)
         throws IOException, RepositoryKeyNotFoundException {
        /* REPTIMESTART */        
        long startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */
        if (filePos >= myFileLength || filePos < FIRST_DATA_POS) {
            if (filePos != myKeyTablePos) { // Special case - don't fail in initial code!
                throw new IOException("Attempt to seek to " + filePos + 
                                      " which is out of range of file of length " + myFileLength +
                                      " in Repository file " + myFile);
            }
        }

        myFile.seek(filePos);   // May throw IOException

        int keyLength = myFile.readInt();
        if (keyLength < 0) {
            throw new IOException("Negative key length in Repository file at file position " +
                                  filePos + " in file " + myFile);
        }

        // A key of length 0 indicates the record has been deleted.
        // XXX We tell the caller this - Is this a security leak?

        if (keyLength == 0) {
            throw new RepositoryKeyNotFoundException("Record at position " + filePos + " has been deleted");
        }

        myFile.seek(filePos + keyLength + SIZEOF_INT); // Skip key and length of integer, for key length

        int dataLength = myFile.readInt();
        if (dataLength < 0) {
            throw new IOException("Negative data length in Repository file at file position " +
                                  filePos + " in file " + myFile);
        }

        // A data length of 0 means the record is written as null,
        // possibly to shadow some other non-null value in some other
        // RepositoryFile that may be later in the Repository's file
        // vector and thus read-only.

        if (dataLength == 0) {
            throw new RepositoryKeyNotFoundException("Key " + key + " not found");
        }

        byte[] result = new byte[dataLength];
        myFile.read(result);
        /* REPTIMESTART */
        Repository.diskTimer += NativeSteward.deltaTimerUSec(startTime);
        /* REPTIMEEND */
        return result;
    }

    /**

     * Returns the size of an object given a file position. If a
     * record has been deleted or voided to "nonexistent", returns 0.

     */

    int sizeOfObject(long filePos) throws IOException, RepositoryKeyNotFoundException {
        if (filePos >= myFileLength || filePos < FIRST_DATA_POS) {
            if (filePos != myKeyTablePos) { // Special case - don't fail in initial code!
                throw new IOException("Attempt to seek to " + filePos + 
                                      " which is out of range of file of length " + myFileLength +
                                      " in Repository file " + myFile);
            }
        }

        myFile.seek(filePos);
        int keyLength = myFile.readInt();

        if (keyLength <= 0) {

            // A key of length 0 indicates the record has been deleted. Return 0.
            if (keyLength == 0) return 0;

            // Otherwise we have a problem that needs reporting.
            throw new IOException("Negative key length in Repository file at file position " +
                                  filePos + " in file " + myFile);
        }
        myFile.seek(filePos + keyLength + SIZEOF_INT); // Skip key and length of integer, for key length
        int dataLength = myFile.readInt(); // May be 0 for voided records.
        return dataLength;
    }

    /**

     * Delete an object, given a key. 

     * @param key notNull untrusted - An object to use as a key.

     */

    public void delete(Object key) throws IOException, RepositoryKeyNotFoundException {

        if (myKeys == null) {
            throw new RepositoryKeyNotFoundException("Delete failed, key " + key +
                   " not found, because repository not initialized, no key table available");
        }
        if (key == null) {
            throw new RepositoryKeyNotFoundException("Repository delete failed, key was null");
        }

        RepositoryHandle entry = (RepositoryHandle)myKeys.get(key);
        if (entry == null) {
            throw new RepositoryKeyNotFoundException("delete() - Key " + key + " not found");
        }
        else entry.delete();
        myKeys.remove(key);     // Update our key hashtable also.
        keyHashDirty = true;           // Remember to write out the myKeys hashtable
    }

    /**

     * Determine whether a record for a given key exists.

     * @param key notNull untrusted - An object to use as a key.

     */

     public boolean exists(Object key) {
        if (myKeys == null || key == null) return false;
        RepositoryHandle entry = (RepositoryHandle)myKeys.get(key);
        if (entry == null) return false;
        if (entry.size() <= 0) return false; // 0 for deleted or voided records
        return true;
    }

    /**

     * Quietly delete a record if it exists but don't throw if key
     * doesn't exist. Returs true if something was deleted.

     * @param key notNull untrusted - An object to use as a key.

     */

    public boolean deleteIfExists(Object key) {
        try {
            if (myKeys == null || key == null) return false;
            RepositoryHandle entry = (RepositoryHandle)myKeys.get(key);
            if (entry == null) return false;
            myKeys.remove(key);     // Remove entry from myKeys hashtable
            keyHashDirty = true;           // Remember to write out the myKeys hashtable
            entry.delete();         // Delete space in file itself
            return true;
        } catch (IOException iox) {
        }
        return false;
    }

    /**

     * Delete an entry (key and data), given a file position.  This is
     * done by replacing the key-data pair with a key that is of zero
     * length and to set the data length to be the same as the old key
     * + data lengths, thus occupying the same number of bytes in the
     * file.

     */

    void deleteByFilePosition(long filePos) throws IOException {
        if (eFile == null) {
            throw new IOException("deleteByFilePosition(): RepositoryFile " + eFile +
                                  " is not open for write access");
        }

        if (filePos > myFileLength || filePos < FIRST_DATA_POS) {
            throw new IOException("deleteByFIlePosition attempted to seek to " + filePos + 
                                  " which is out of range of file of length " + myFileLength +
                                  " in Repository file " + eFile);
        }

        eFile.seek(filePos);
        int keyLength = eFile.readInt();
        if (keyLength <= 0) {
            throw new IOException("Non-positive key length in Repository file at file position " +
                                  filePos + " in file " + eFile);
        }

        eFile.seek(filePos + keyLength + SIZEOF_INT);
        int dataLength = eFile.readInt();
        if (dataLength <= 0) {
            throw new IOException("Non-positive data length in Repository file at file position " +
                                  filePos + " in file " + eFile);
        }

        // EStdio.err().println("RepositoryFile: Low level delete record at " + filePos);

        eFile.seek(filePos);
        eFile.writeInt(0);     // Zero length key - marks deleted record.
        eFile.writeInt(keyLength + dataLength); // Pretend deleted record is all data nowadays.
    }

    /**

     * Close this RepositoryFile.  Flush the changes to the myKeys
     * hashtable and the file info, then close the repository file.

     */

    public void close() throws IOException {
        flushChanges(true);
        myKeys = null;
        eFile = null;
        myFile.close();
        myFile = null;
    }

    /** 

     * Save out some info, possibly including our file-local keys
     * hashtable (myKey) to the file.
     
     * @param saveEverything - If set then we save out the keys
     * hashtable also. If not set, then we save out only the more
     * important fileinfo variables. The keys table can be recovered
     * at startup time so it may make sense not to write it out except
     * when quitting, and to accept the work of repairing the file if
     * a crash happens.

     */

    public void flushChanges(boolean saveEverything) throws java.io.IOException {

        if (repositoryFileDebug) {
        EStdio.err().println("In flushChanges: keyHashDirty = " + keyHashDirty +
                           " saveEverything = " + saveEverything + 
                           " fileInfoDirty = " + fileInfoDirty + 
                           " efile.length() = " + eFile.length() +
                           " number records = " + myNumberRecords +
                           " File length = " + myFileLength +
                           " Key table pos = " + myKeyTablePos);

        }

        if (eFile == null) return;              // Not writable, ignore request.
        if (!keyHashDirty) {
            saveFileInfo();     // Write out the file info, if needed.
            return;             // but don't write out hashtable
        }

        if (saveEverything) {
            myKeyTablePos = myFileLength;       // We will write key table to current end of file.
            Vector savedKeys = new Vector(myKeys.size());
            Enumeration keys = ((Hashtable)myKeys).keys();
            // EStdio.err().println("Saving Repository index to file");

            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                RepositoryHandle repositoryHandle = (RepositoryHandle)myKeys.get(key);
                KeyPosition kp = new KeyPosition(key,repositoryHandle.myFilePos);
                savedKeys.addElement(kp);
            }
            put(KEY_VECTOR_PSEUDO_KEY, savedKeys, null);  // Save keys vector as the last record in file
            keyHashDirty = false;                      // Not keyHashDirty anymore
            fileInfoDirty = true;               // We changed myKeyTablePos (at least)
        } else {

            // if saveEverything is false we do a much cheaper save
            // even though the hashtable has been changed. We only
            // save out the file info since we can (and will)
            // recompute the keys hashtable at startup time, losing
            // some time but no data.

            myKeyTablePos = -1; // mark file as having no hashtable. Don't clear keyHashDirty.
        }
        fileInfoDirty = true;               // We changed myKeyTablePos (at least)
        saveFileInfo();                         // Write out the file info, if needed.
    }

    /**

     * Read in the keys hashtable from the file, if it's there.

     */

    private Hashtable readMyKeys() throws IOException {
        if (keyHashDirty) {            // If keyHashDirty, then table is already overwritten by us
            return null;        // and can't be read in anymore.
        }
        if (myKeyTablePos == -1) return null; // Not there to read for other reasons.

        Vector newKeys = (Vector) unserialize(getDataBytes(KEY_VECTOR_PSEUDO_KEY, myKeyTablePos), null);

        if (newKeys == null) {
            return null;        // Some problem. Rebuild index?
        }

        int hashSize = (int)myNumberRecords;
        if (hashSize < 10) hashSize = 10;
        Hashtable result = new Hashtable(hashSize);
        Enumeration e = newKeys.elements();
        while (e.hasMoreElements()) {
            KeyPosition kp = (KeyPosition)(e.nextElement());
            RepositoryHandle repositoryHandle = new RepositoryHandle(this, kp.myPosition);
            result.put(kp.myKey, repositoryHandle);
        }
        return result;              // and return it.
    }

    /**

     * If we get into trouble, so that we have no saved hashtable in
     * the file, but believe the file may be otherwise consistent,
     * then we can regenerate the hashtable information doing a
     * sequential read of the file. This will analyze the file and
     * return a hashtable if it believes the file to be internally
     * consistent to a certain point. Only records believed to be
     * valid are entered into the hashtable. <p>

     * One problem is recognizing the end-of-file. If the file length
     * information at the beginning of the file is incorrect, then we
     * may read in garbage data when we get past the consistent
     * portion of the file. This code should be independently verified
     * to always leave the fileLength information in the beginning of
     * the file such that all data in the file is consistent and
     * recoverable. The only unsafe operations are those that shorten
     * the file - writing records over the old image of the key
     * hashtable (at file end), and deleting records, and rebuilding
     * the file by in-situ compacting (but his last alternative is not
     * likely to get implemented since file-to-file copying compaction
     * is so much easier).

     * @return a keys hashtable of retrieved keys believed to point
     * to valid data records.  Returns null if no records could be
     * retrieved safely.

     */

    private Hashtable regenerateKeys(long probableNumberRecords,
                                     boolean reportOnly,
                                     Vector badKeys) throws IOException {

        Hashtable result = new Hashtable((int)probableNumberRecords + 20);

        long fileLength = myFile.length();

        if (fileLength < FIRST_DATA_POS) {
            if (reportOnly) {
                EStdio.err().println("Repository File " + myFile +
                                     " is too short, only " + fileLength + " bytes");
                return null;
            }
            throw new IOException("Repository File " + myFile +
                                  " is too short, only " + fileLength + " bytes");
        }

        readFileInfo();                               // Refresh file info, just to make sure.
        long filePos = FIRST_DATA_POS;                // Start here
        long recordStartPos = filePos;                // Back pointer to valid record start pos

        // These are int's not long's, since that's what they are in the file.

        int keyLength;
        int dataLength;
        int recordLength;
        int dataRecords = 0;
        int deletedRecords = 0;
        int foundRecords = 0;
        int corruptedRecords = 0;
        int verifiedRecords = 0;
        int longestRecord = 0;

        // We will trust the file length info somewhat.
        // Subtract an integer length to avoid fencepost errors
        // since there must be at least two integers in a valid record.
        
        
        long lastByte = myFileLength;
        if (myKeyTablePos > FIRST_DATA_POS && lastByte > myKeyTablePos)
            lastByte = myKeyTablePos; // This is a better limit, if it's lower.
        lastByte -= SIZEOF_INT;

        try {
            while (filePos < lastByte) {
                recordStartPos = filePos;                 // Update back pointer to valid record start pos
                myFile.seek(recordStartPos);              // Seek to start of data record.
                keyLength = myFile.readInt();             // Read length of key.
                //             EStdio.err().println("key length is " + keyLength + " filePos is " + filePos);
                if (keyLength < 0) {
                    if (reportOnly) {
                        EStdio.err().println("Negative key length in Repository file at file position " +
                                             filePos + " in file " + myFile);
                        return null;                           // Nothing we can do about this.
                    }
                    throw new IOException("Negative key length in Repository file at file position " +
                                          filePos + " in file " + myFile);
                }
                if (keyLength > fileLength) {             // Impossible.
                    if (reportOnly) {
                        EStdio.err().println("Impossible key length (" + keyLength +
                                             ") in Repository file at file position " +
                                             filePos + " in file " + myFile);
                        return null;
                    } else {
                        throw new IOException("Impossible key length (" + keyLength +
                                              ") in Repository file at file position " +
                                              filePos + " in file " + myFile);
                    }
                }

                // A key of length 0 indicates the record has been deleted.

                if (keyLength == 0) {

                    dataLength = myFile.readInt();

                    // Record contains some overhead - key and data lengths as integers.
                    // Key length is 0, and the rest is data length.

                    recordLength = dataLength + SIZEOF_INT + SIZEOF_INT;

                    if (longestRecord < recordLength) longestRecord = recordLength;

                    // For a deleted record, the key length is set to 0 and the data length is set
                    // to make it look like data occupies the entire deleted record in the file.

                    if (reportOnly) {
                        EStdio.err().println("Record at position " + filePos +
                                             " has been deleted. Deleted record occupies " +
                                             recordLength + " bytes");
                    }

                    deletedRecords++;

                    // Skip record.
                    // The data is of course just garbage bits, so don't read it in.

                } else {

                    // Attempt to read in the key.

                    Object key;
                    byte[] keyAsBytes;

                    try {
                        keyAsBytes = new byte[keyLength];
                        myFile.read(keyAsBytes);
                    } catch (Exception ex1) {
                        if (reportOnly) {                             // Catch errors, just report nicely.
                            EStdio.err().println("Read of key of length " + keyLength +
                                                 " from position " + filePos +
                                                 " in file " + myFile +
                                                 " caused an Exception: " + ex1);
                            return null;                          // Not much left to do now.
                        }
                        throw new EOFException("Key read failed"); // Pretend we got EOF - truncate file
                    }

                    try {
                        ByteArrayInputStream keyByteStream = new ByteArrayInputStream(keyAsBytes);
                        Unserializer unserializer = Unserializer.make(keyByteStream,myMaker);
                        key = unserializer.decodeGraph();
                    } catch (Exception ex2) {
                        if (reportOnly) {                             // Catch errors, just report nicely.
                            EStdio.err().println("Decode of key of length " + keyLength +
                                                 " from position " + filePos +
                                                 " in file " + myFile +
                                                 " caused an Exception: " + ex2);
                            corruptedRecords++;
                            key = null;                           // Attempt to proceed without a key.
                        } else {
                            throw new EOFException("Key decode failed"); // Pretend we got EOF - truncate file
                        }
                    }

                    dataLength = myFile.readInt();

                    // Record contains some overhead - key and data lengths as integers.
                    // So the whole record length is :

                    recordLength = keyLength + dataLength + SIZEOF_INT + SIZEOF_INT;

                    if (longestRecord < recordLength) longestRecord = recordLength;

                    if (dataLength < 0) {
                        if (reportOnly) {
                            EStdio.err().println("Negative data length in Repository file at file position " +
                                                 filePos + " in file " + myFile);
                            return null;                           // Nothing we can do about this.
                        }
                        throw new IOException("Negative data length in Repository file at file position " +
                                              filePos + " in file " + myFile);
                    }
                    if (dataLength > fileLength) {             // Impossible.
                        if (reportOnly) {
                            EStdio.err().println("Impossible data length (" + dataLength +
                                                 ") in Repository file at file position " +
                                                 filePos + " in file " + myFile);
                            return null;
                        } else {
                            throw new IOException("Impossible data length (" + dataLength +
                                                  ") in Repository file at file position " +
                                                  filePos + " in file " + myFile);
                        }
                    }

                    if (dataLength == 0) {

                        // A data length of 0 means the record is written as
                        // "nonexistent", possibly to shadow some other value in
                        // some other RepositoryFile that may be later in the
                        // Repository's file vector and thus read-only.

                        // Note the difference that a deleted record does not
                        // cause any shadowing since it has no key at all.
                        // Finding a nonexistent record cancels search in other
                        // repositoryfiles in the same repository but will NOT
                        // abort searching for it in other repositories.

                        if (key == null) {                        // If there was no key either. This is bad.
                            if (reportOnly) {
                                EStdio.err().println("No key, and data length is 0 in Repository file " +
                                                     myFile + " at file position " + filePos);
                                return null;                           // Nothing we can do about this.
                            }
                            throw new IOException("No key, and data length is 0 in Repository file " + myFile +
                                                  " at file position " + filePos);
                        }
                    }

                    // This looks like a good key and a reasonable data record -
                    // either actual data or a "nonexistent" entry. 

                    // If the key is a CryptoHash, then we can verify the actual data to be correct!

                    if (key instanceof CryptoHash) {
                        try {
                            byte[] dataAsBytes = new byte[dataLength];
                            myFile.read(dataAsBytes);
                            CryptoHash dataHash = new CryptoHash(dataAsBytes);
                            if (! (dataHash.equals(key))) {
                                if (badKeys != null) badKeys.addElement(key);
                                corruptedRecords++;
                                EStdio.err().println("Detected corrupted data record for key " + key +
                                                     " - Data cryptohash is " + dataHash);
                            } else verifiedRecords++;
                        } catch (IOException criox) {
                            if (reportOnly) {
                                EStdio.err().println("Error when reading data for key " +
                                                     key + ":" + criox);
                            }
                            if (badKeys != null) badKeys.addElement(key);
                            corruptedRecords++;
                        }
                        
                    }
                    dataRecords++;

                    // Create a RepositoryHandle record that points to us (so we
                    // know which file the data is in) and also contains the file
                    // position the data starts at.

                    if (!reportOnly) {
                        RepositoryHandle entry = new RepositoryHandle(this, recordStartPos);
                        result.put(key,entry);                  // Add to our reconstructed-key collection
                    }
                }
                foundRecords++;
                filePos = recordStartPos + recordLength;
            }
        } catch (EOFException eofx) {

            // EOF found when we did not expect it.
            // Truncate file at beginning of first invalid record.
            // Accept what we have in result table right now.

            // For simplicity we update fileinfo variables here instead of in the caller.,
            // Caller should call flushChanges(true) for us
            // after assigning the our return result to myKeys.

            myFileLength = recordStartPos;
            myKeyTablePos = -1;
            myNumberRecords = foundRecords;
            fileInfoDirty = true;
            keyHashDirty = true;
            if (reportOnly) EStdio.err().println("Truncated file after " + myNumberRecords +
                                                 " records (" + myFileLength + " bytes)");
        }

        long foundSymbols = 0;
        long missingSymbols = 0;
        Hashtable symbolTable = (Hashtable)get("%SymbolTable%");

        if (symbolTable != null) {
            Enumeration e = symbolTable.keys();
            while (e.hasMoreElements()) {
                Object key = e.nextElement();
                Object cryptoHash = symbolTable.get(key);
                Object value = null;
                try {
                    value = get(cryptoHash);
                    //if (value == null) {
                    //EStdio.err().println("Retrieval of CRH for key " + key +
                    //" returned a null result");
                    //}                    
                } catch (Throwable t) {
                    //EStdio.err().println("Retrieval of CRH for key " + key +
                    //" threw an error: " + t.getMessage());
                }
                if (value == null) missingSymbols++;
                else foundSymbols++;
            }
        }

        if (reportOnly) {
            EStdio.err().println("Data      records: " + dataRecords);
            EStdio.err().println("Deleted   records: " + deletedRecords);
            EStdio.err().println("Total     records: " + foundRecords);
            EStdio.err().println("Verified  records: " + verifiedRecords);
            EStdio.err().println("Corrupted records: " + corruptedRecords);
            EStdio.err().println("Longest   record : " + longestRecord + " bytes");
        }
        return result;
    }

    /**

     * An OpenerRecipe suitable for a Repository.
     * Based on a recipe from Openers.

     */

    // Note: This code is also duplicated in Repository.java

    static private OpenerRecipe defaultRecipe() {
        Enumeration recipes 
            = new DiscreteEnumeration(new HashtableRecipe(),
                                      new PEHashtableRecipe(),
                                      new ObjKeyTableRecipe());
        ClassRecipe classRecipe 
            = new RepositoryClassRecipe(RootClassRecipe.THE_ONE);

        return new OpenerRecipe(recipes, classRecipe);
    }    

//     /**
      
//      * Verify that a record for a given key exists or is voided.  If
//      * the record exists, and if the key is a cryptohash key, also
//      * verifies that the record is uncorrupted by comparing the
//      * cryptohash of the actual data with the given (CryptoHash) key.

//      */

//     public boolean verifyRecord(Object key) throws RepositoryKeyNotFoundException {
//         if (myKeys == null) {
//             throw new RepositoryKeyNotFoundException
//               ("Get failed, Key " + key +
//                " not found, because repository not initialized, no key table available");
//         }
//         if (key == null) {
//             throw new RepositoryKeyNotFoundException("Repository get failed, key was null");
//         }

//         RepositoryHandle entry = (RepositoryHandle)myKeys.get(key);

//         if (entry == null) {
//             throw new RepositoryKeyNotFoundException("Key " + key + " not found");
//         }

//         if (! (key instanceof CryptoHash)) return true; // Can't verify non-cryptohash records.
//         byte[] dataAsBytes = entry.getDataBytes(key);
//         CryptoHash dataHash = Repository.computeCryptoHash(dataAsBytes);
//         return dataHash.equals(key);
//     }


    /**

     * Force a key regeneration and update file using the results

     * @param report - If set, also report what we find to
     * EStdio.err

     */

    public void repairFile(boolean repairIt, boolean report) throws IOException {
        Hashtable myOldKeys = myKeys;
        Vector badKeys = new Vector(100);

        myKeys = regenerateKeys(myNumberRecords, false, badKeys);
        if (report) {
            if (myKeys == null) {
                EStdio.err().println("Could not regenerate keys");
            }
            else {
                EStdio.err().println("Regenerated " + myKeys.size() + " keys");
            }
        }
        if (myKeys != null && badKeys.size() > 0) {
            Enumeration baddies = badKeys.elements();
            while (baddies.hasMoreElements()) {
                Object badKey = baddies.nextElement();
                if (isWritable() && repairIt) {
                    if (report) EStdio.err().println("Deleting corrupt record " + badKey);
                    deleteIfExists(badKey); // Delete record from file.
                } else {
                    if (report) EStdio.err().println("Temporarily ignoring corrupt record " + badKey);
                    myKeys.remove(badKey); // Remove from local hashtable at least
                }
            }

            if (isWritable() && repairIt) {

                // In case the KEY_VECTOR_PSEUDO_KEY record has become
                // a regular record - e.g. if we have lost track of
                // its byte position - delete it explicitly.  Don't
                // even attempt to delete it in the Repository itself
                // since we don't want to void the record - a voided
                // keytable might do harm, who knows.

                deleteIfExists(KEY_VECTOR_PSEUDO_KEY);
                myKeys.remove(KEY_VECTOR_PSEUDO_KEY);
                if (report) EStdio.err().println("Updating file" + myFile);
                fileInfoDirty = true;
                flushChanges(true);
            }
        }
    }

    /**
      
     * Analyze file sequentially and print out detailed information
     * about every reccord in the file to EStdio.err()

     * @exception Exception - Throws many kinds of exceptions. This
     * method is only intended to be called from non-runtime tools
     * such as the Curator.

     */

    public void analyzeFile() throws Exception {
        regenerateKeys(myNumberRecords, true, null);
    }

    /**

     * Dump all keys. For debugging only.

     */

    void dumpKeys(PrintStream out) {
        out.println("   --- Dump of keys from RepositoryFile " + myFile + " ---");
        if (myKeys == null) {
            out.println("Keys are null");
            return;
        }

        Enumeration keyEnumeration = myKeys.keys();

        while (keyEnumeration.hasMoreElements()) {
            Object key = keyEnumeration.nextElement();
            Object value = myKeys.get(key);
            if (key instanceof byte[]) {
                String keyAsHex = ec.util.HexStringUtils.byteArrayToHexString((byte[])key);
                out.println("byte array key " + key + " (" + keyAsHex + ") contains " + value);
            }
            else {
                out.println(key + " contains " + value);
            }
        }
    }
}
