package ec.e.rep;

import ec.trace.Trace;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.FileDescriptor;

import java.util.Vector;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;
import ec.e.hold.DataHolderRecipe;
import ec.e.hold.ReposableMarker;

import ec.cert.CryptoHash;

import ec.e.hold.ReposableMarker;
import ec.e.openers.*;
import ec.e.openers.AllowingClassRecipe;
import ec.e.rep.RepositoryKeyNotFoundException;
import ec.e.serial.Serializer;
import ec.e.serial.Unserializer;
import ec.e.serial.ParamSerializer;
import ec.e.serial.ParamUnserializer;
import ec.e.util.DiscreteEnumeration;
import ec.e.util.ArrayEnumeration;
import ec.tables.PEHashtable;
import ec.tables.SimTable;

/* REPTIMESTART */
import ec.util.NativeSteward;
/* REPTIMEEND */

    /** 

     * A RepositoryFile contains encoded (Java) objects, encoded using
     * Openers.  First in the RepositoryFile, in fixed file-positions,
     * are some special-treated data in what is called the FileInfo
     * block, such as the RepositoryFile Version number, and
     * information that allow us to retrieve the keys for the file.<p>

     * Each record in the file contains both the key and the data,
     * encoded using Openers. This allows us to recover the keys in a
     * file by reading the whole file sequentially. However, each file
     * normally contains a Dictionary of keys, that is stored at the end
     * of the file. This Vector is saved under the reserved key
     * "%LocalKeyVector%", just to keep all data in the file conform
     * to a key-data pairing scheme. At startup, the Vector is reada
     * in and the values are distributed into a Dictionary (we use a
     * Hashtable or a SimTable).  Note that when we want to add more
     * data to the file, which is done by appending, then we will
     * overwrite this Vector and will re-write it only when we close
     * the file. The special information (in the beginning of the
     * file) is updated to reflect this in such a way that it is never
     * inconsistent (See below).<p>

     */

public class RepositoryFile {

    /*
     * Lengths of various primitive datatypes when written out to a
     * file.  These should be defined somewhere (by some set of names)
     * systemwide but I could not find them anywhere, not even in
     * /usr/local/java/src/java/DataOutputStream.java .

     */

    public static final int SIZEOF_INT = 4;
    public static final int SIZEOF_LONG = 8;
    static public Trace repLowLevelTrace = new Trace("replowlevel");

    // Other constants and pseudoconstants

    public  static final long REPOSITORYFILE_MAGIC_NUMBER = 0x1717171710001717L; // Swedish cusswords
    public  static final long NO_COMPRESSION = 0;
    private static       long  CURRENT_REPOSITORYFILE_VERSION = 1; // Not final to avoid recompile problems.
    private static final String KEY_VECTOR_PSEUDO_KEY = "%LocalKeyVector%";
    private static int defaultTableType = 0;
    private static int keySaveFormat = 0;

    /*

     * The following constants define file offsets to various long
     * integers, collectively known as the File Info, that are written
     * out to the beginning of the file.

     */

    static int MAGIC_NUMBER_POS =  0 * SIZEOF_LONG; // Offset in file to the file format magic number.
    static int VERSION_POS =       1 * SIZEOF_LONG; // Offset in file to the file format version number.
    static int NR_RECORDS_POS =    2 * SIZEOF_LONG; // Number of records excluding key Dictionary, if any.
    static int FILE_LENGTH_POS =   3 * SIZEOF_LONG; // Our opinion of file length (OS may think file is longer)
    static int KEY_TABLE_POS_POS = 4 * SIZEOF_LONG; // Key Dictionary start. If 0, then there is no table in file.
    static int COMPRESSION_POS =   5 * SIZEOF_LONG; // Compression scheme identifier. 0 means no compression.
    static int DUMMY_VALUE_POS =   6 * SIZEOF_LONG; // Gratuituous filler, use for future expansion
    static int DUMMY2_VALUE_POS =  7 * SIZEOF_LONG; // Gratuituous filler, use for future expansion
    static int FIRST_DATA_POS =    8 * SIZEOF_LONG; // First key/data pair. Beginning of file contents.

    RandomAccessFile myFileToReadFrom = null;
    RandomAccessFile myFileToWriteTo = null;
    String           myFileName = "";

    // the File Info variables themselves

    long myMagicNumber;         // Magig number in file. Must match, if not then not a rep. file.
    long myFileVersion;         // Version number of the file.
    long myNumberRecords = 0;   // Number of records in file excluding key dictionary if any
    long myFileLength;          // Next true data record will be written starting here
    long myKeyTablePos = -1;    // Position of key table in file. If -1, then not in file.
    long myKeyTableLength = 0;  // Length of the encoded key table, in bytes.
    long myCompressionScheme;   // File-wide compression scheme ID. If 0, then no compression.
    long myDummy;               // Not used.

    boolean fileInfoDirty = false; // File info - the above variables - need saving out to file.

    Dictionary myKeys = null;    // Package scope!
    boolean keyHashDirty = false;      // myKeys dictionary has been updated, needs saving to file.

    // Symbols are used a lot. They are kept in two places: One set is
    // the set of all symbols in all repositoryfiles in a given
    // repository, and those are kept in the Repository object.

    // The other one is the new symbols added to the frontmost file
    // and those are kept here, but only for the frontmost file
    // (read-only files have a null newSymbols table). When a writable
    // RepositoryFile is closed, all the symbosl in the newSymbols
    // table are merged with the existing symbol table (if it exists)
    // and re-written out to disk.

    // When a new symbol is added, it's therefore added in two
    // separate operations to the symbol table in the Repository
    // (which is used for all lookups) and to the frontmostfile's
    // newSymbols table (to be flushed out to disk when closing the
    // file).

    protected Dictionary newSymbols = null; // New symbol table entries
    protected boolean symbolsDirty = false;

    // Keywords are not used as much, and typically we use them in a
    // special way (using getAll()) that means the results are
    // computed at runtime rather than just looked up. Here we use a
    // different strategy. All RepositoryFiles keep their own keywords
    // tables and they are written out to disk when closing, iff they
    // are dirty. There's no need for a cached keywords file at the
    // Repository level, they are all kept at the RepositoryFile level.

    // XXX This code is still incomplete. Specifically, we'd like for
    // keywords to be immediately visible to getAll() after we store
    // something. As it is now, you must close and re-open the
    // repository to make the new keywords available.

    protected Dictionary keywordsTable = null; // New keyword table entries
    protected boolean keywordsDirty = false;

    /**

     * Create a Dictionary to be used with new Repositories. These are
     * intially Hashtables but may be other objects implementing the
     * Hashtable's Dictionary interface.

     */

    public static Dictionary makeDictionary(int size) {
        if (defaultTableType == 1) return new SimTable(size);
        else if (defaultTableType == 0) return new Hashtable(size);
        return null;
    }

    /**

     * Set the type of key table we will be using. Currently we
     * support two types - Hashtables (0) and SimTables (1). We also
     * allow saving these as either the actual data or linearized into
     * an array of key-value-key-value and rehashing these when
     * reading them in. You get these by using the type codes 2 and 3
     * for Hashtables and SimTables, respectively.

     * This method will print information to System.out. Only
     * Curator should normally call this so this should be OK.

     */

    public static void setTableType(int newtype) {
        if (newtype == 0) {
            defaultTableType = 0;
            keySaveFormat = 0;
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Using Hashtables for keys in new Repositories");
        }
        else if (newtype == 1) {
            defaultTableType = 1;
            keySaveFormat = 0;
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Using SimTables for keys in new Repositories");
        }
        if (newtype == 2) {
            defaultTableType = 0;
            keySaveFormat = 1;
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm
                  ("Using Hashtables saved as arrays for keys in new Repositories");
        }
        else if (newtype == 3) {
            defaultTableType = 1;
            keySaveFormat = 1;
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm
                  ("Using SimTables saved as arrays for keys in new Repositories");
        }
    }

    private OpenerRecipe myMaker = defaultRecipe(); // Allows us to look inside arbitrary objects

    /**

     * Constructor to create a RepositoryFile given a File object. If
     * the writable flag is given as true, then the RepositoryFile can
     * be written or appended to, otherwise not.

     * @param aFile notNull - a File object.
     * @param writable - Specifies whether the Repository should be writable

     */

    // For backwards compatibility.

    RepositoryFile(File aFile, boolean writable) throws IOException {
        myFileName = aFile.getAbsolutePath();  // Saved for better error messages only
        if (writable) {
            myFileToWriteTo = new RandomAccessFile(aFile, "rw");
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Repository file " +
                                        aFile.getAbsolutePath() +
                                        " is writable");
        }

        if (aFile.length() >= FIRST_DATA_POS) { // Long enough to contain our file info?
            myFileToReadFrom = new RandomAccessFile(aFile, "r");
            readFileInfo();                      // Y/ read it in.
            if (myMagicNumber != REPOSITORYFILE_MAGIC_NUMBER) {
                // Not a RepositoryFile, ignore it. Thor a subclass of IOException
                throw new NotRepositoryFileException("File " + myFileName +
                                                     " exists but is not a RepositoryFile" +
                                                     " - Ignoring it");
            }
            if (myKeyTablePos != -1) {
                myFileLength = myKeyTablePos; // Indicate we want to overwrite dictionary in file
            }
            if (myNumberRecords > 0) {           // If there seem to be records
                try {
                    myKeys = readMyKeys(); // Attempt reading the keys Dictionary from file end.
                    if (myKeys == null) { // Did not work; We have data in file but no keys??
                        if (Trace.repository.debug && Trace.ON)
                            Trace.repository.debugm
                                ("Repository file " + myFileName +
                                 " lacks an index. Reconstructing one.");
                        myKeys = regenerateKeys(myNumberRecords, false, null);
                    }
                } 
                catch (IOException e) {
                    if (Trace.repository.debug && Trace.ON) {
                        Trace.repository.debugm
                          ("Repository file " + myFileName + " has problems: " +
                           e.getMessage() + " - repairing file...");
                    }
                    repairFile(true,true); // Repair the file; report on progress. May throw IOX.
                }
                if (myKeys == null) {
                    throw new IOException("Repository file '" + myFileName +
                                          "' contains " + myNumberRecords +
                                          " records but no key table," +
                                          " and could not be repaired");
                }
                if (Trace.repository.debug && Trace.ON) {
                    Trace.repository.debugm
                        ("Repository file " + myFileName + " contains " +
                         myKeys.size() + " items");
                }
            }
            else {                             // Old file, but it contains no records
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("Repository " + myFileName + " is valid but empty");
                myKeys = makeDictionary(50);      // If no records in file, use a fresh Dictionary.
            }
        } 
        else {

            // This is a new file. Initialize all file info, then write it out.

            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Repository is new and therefore empty");
            myMagicNumber = REPOSITORYFILE_MAGIC_NUMBER;
            myFileVersion = CURRENT_REPOSITORYFILE_VERSION;
            myNumberRecords = 0;
            myFileLength = FIRST_DATA_POS;        // We will start appending to file here.
            myKeyTablePos = -1;                   // If no records, don't write key table.
            myKeyTableLength = 0;                 // It will then be zero length also.
            myCompressionScheme = NO_COMPRESSION; // Default, for now.
            myDummy = 0;                          // Not used, for future expansion.
            fileInfoDirty = true; // IF you don't set this, then saveFileInfo won't write!
            saveFileInfo();                       // Write out file header info
            myKeys = makeDictionary(50);           // Create a fresh keys file
        }
    }

    /**

     * Tells whether this file is writable

     */

    public boolean isWritable() {
        return myFileToWriteTo == null ? false : true;
    }

    /**

     * Read in our file info block. In the very beginning of the file
     * we allocate space for some un-encoded long integers. These
     * describe trivial details about the formatting of the rest of
     * the file, such as the version of the file layout (the current
     * one is CURRENT_REPOSITORYFILE_VERSION), the number of records
     * in the file (excluding the key Dictionary itself), the logical
     * file length (We cannot truncate files using Java) and the byte
     * offset to the key Dictionary (if one exists; if it does not
     * exist, then this number - myKeyTablePos - is set to -1). We
     * further leave room for one long integer for possible future
     * expansion. This method moves the file pointer.

     * XXX This file could be encrypted. Would that be a good idea,
     * eventually?

     */

    private void readFileInfo() throws java.io.IOException {
        synchronized(myFileToReadFrom.getFD()) {
            myFileToReadFrom.seek(MAGIC_NUMBER_POS);

            myMagicNumber = myFileToReadFrom.readLong();

            if (myMagicNumber != REPOSITORYFILE_MAGIC_NUMBER) {
                throw new IOException
                  ("File " + myFileName + " is not a valid Repository file - Bad magic number");
            }

            myFileVersion = myFileToReadFrom.readLong();
            myNumberRecords = myFileToReadFrom.readLong();
            myFileLength = myFileToReadFrom.readLong();
            myKeyTablePos = myFileToReadFrom.readLong();
            myKeyTableLength = myFileToReadFrom.readLong();
            myCompressionScheme = myFileToReadFrom.readLong();
            myDummy = myFileToReadFrom.readLong();
        }

        if (Trace.repository.debug && Trace.ON)
            Trace.repository.debugm
              ("Read of File Info: vers = " + myFileVersion +
               " number records = " + myNumberRecords +
               " File length = " + myFileLength +
               " Key table pos = " + myKeyTablePos +
               " Key table length = " + myKeyTableLength);
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
        if (myFileToWriteTo == null) {
            throw new IOException("Attempt to write to read-only Repository file '" +
                                  myFileName + "'");
        }
        if (Trace.repository.debug && Trace.ON)
            Trace.repository.debugm
              ("Writing File Info: vers = " + myFileVersion +
               " number records = " + myNumberRecords +
               " File length = " + myFileLength +
               " Key table pos = " + myKeyTablePos);

        FileDescriptor fd = myFileToWriteTo.getFD();
        synchronized(fd) {      // Attempt to fix gray square bug
            myFileToWriteTo.seek(MAGIC_NUMBER_POS);
            myFileToWriteTo.writeLong(REPOSITORYFILE_MAGIC_NUMBER);
            myFileToWriteTo.writeLong(myFileVersion);
            myFileToWriteTo.writeLong(myNumberRecords);
            myFileToWriteTo.writeLong(myFileLength);
            myFileToWriteTo.writeLong(myKeyTablePos);
            myFileToWriteTo.writeLong(myKeyTableLength);
            myFileToWriteTo.writeLong(myCompressionScheme);
            myFileToWriteTo.writeLong(myDummy);

            // Hack to see if this will fix seek() problem when file is ne and/or short.
            if (myFileLength == FIRST_DATA_POS) myFileToWriteTo.writeLong(0);
        }
        fd.sync();
        fileInfoDirty = false;
    }

    /**

     * Save a data object under a given key. NOTE: This is a
     * package-level help function. Others should do the corresponding
     * operation on a Repository or SuperRepository. <p>

     * The data is appended to the logical end of the RepositoryFile
     * as indicated by myFileLength. Note that this may not be the
     * actual end of file once the file is closed.  When we write the
     * key Dictionary to the file (before closing file) we will not
     * update myFileLength since we don't regard the Dictionary as
     * part of the real data. But since the Dictionary is of nonzero
     * length, the file is actually longer than myFileLength
     * indicates. <p>

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
     * Dictionary. Note that all put() and encode() methods use
     * PEHashtables, but all get () methods use regular Dictionarys
     * since this is the semantics we want.

     * @return RepositoryHandle - an object to retrieve the saved
     * object by. These objects are typically stored in a Dictionary by
     * the caller under the same key as the one given to this method,
     * and used to retrieve the object later. The local Dictionary is
     * maintained only so it can be written out to the file at closing
     * or checkpointing time.

     */

    /* package */ RepositoryHandle put(Object key, Object data, PEHashtable parimeters)
         throws IOException {
             if (myFileToWriteTo == null) {                    // This equals myFile if file is editable.
                 throw new IOException("Attempt to write to read-only Repository file '" +
                                  myFileName + "'");
             }

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

             RepositoryHandle entry =
               putBytes(keyByteStream.toByteArray(),dataByteStream.toByteArray(), key);

             if (data != myKeys) { // Don't store key table in key table.
                 myKeys.put(key,entry); // Add to our Dictionary of keys
                 keyHashDirty = true; // Remember to write out the myKeys Dictionary
             }
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
     * special case since java Dictionarys cannot store null and the
     * serializers store the data in Dictionarys to detect
     * circularities.

     */

    public CryptoHash putHash(Object data)
         throws IOException {
             return putHash(data,null);
    }


    public CryptoHash putHash(Object data, PEHashtable parimeters) throws IOException {
        if (data == null) { // Special case this since serializers cannot handle it.
            return CryptoHash.nullCryptoHash();
        }
        if (myFileToWriteTo == null) { // This copies myFile if file is editable.
            throw new IOException("Attempt to write to read-only Repository file '" +
                                  myFileName + "'");
        }

        // Encode the data

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(10000);
        Serializer dataSerializer = ParamSerializer.make(byteStream, myMaker, parimeters);
        dataSerializer.encodeGraph(data);
        byte[] dataBytes = byteStream.toByteArray();
        return putHashBytes(dataBytes, data);
    }


    /**

     * Stores an already-encoded object, given as a byte array, into
     * the Repository using the cryptographic hash of the data as a
     * key.  It returns that key as an instance of class CryptoHash.

     * @param dataBytes nullOK - The data to save in the Repository
     * using an automatically generated key.

     * @param data nullOK - The data, if known, otherwise null. Used
     * in error messages and in one Repository-internal special case.

     * @return The cryptographic hash of the data (an instance of
     * class CryptoHash), that was used as the key to store the data
     * under.

     * <p><b>NOTE</b> If the data is null, then a special object, the
     * nullCryptoHash object, is returned. This is treated as a
     * special case since java Dictionarys cannot store null and the
     * serializers store the data in Dictionarys to detect
     * circularities.

     */

    public CryptoHash putHashBytes(byte[] dataBytes, Object data) throws IOException {

        if (dataBytes == null) { // Special case this since serializers cannot handle it.
            return CryptoHash.nullCryptoHash();
        }

        if (repLowLevelTrace.debug && Trace.ON)
            repLowLevelTrace.debugm
              ("databytes size is " + dataBytes.length);
             
        CryptoHash key = new CryptoHash(dataBytes);

        if (exists(key)) return key; // Record already exists. We are done...

        ByteArrayOutputStream keyByteStream = new ByteArrayOutputStream(300);
        Serializer keySerializer = Serializer.make(keyByteStream, myMaker);
        keySerializer.encodeGraph(key);

        // Call low-level record writer

        RepositoryHandle entry = putBytes(keyByteStream.toByteArray(), dataBytes, key);

        if (data == myKeys) {
            return null;
        }

        myKeys.put(key,entry);                  // Add to our Dictionary
        keyHashDirty = true;    // Remember to write out the myKeys Dictionary
        return key;
    }

    /**

     * Write a "nonexistent" shadowing record - one which has
     * zero-length data.  A record like this will immediately return
     * the result "key not found" even though other records with the
     * same key may be available in other repositoryfile later in the
     * repository file chain.

     */

    void voidRecord(Object key) throws IOException {
        if (repLowLevelTrace.debug && Trace.ON)
            repLowLevelTrace.debugm
              ("Voiding record with key " + key);

        if (myFileToWriteTo == null) { // This equals myFile if file is editable.
            throw new IOException("In voiding a record, attempting to write " +
                                  "to closed or read-only Repository file '" + myFileName + "'");
        }

        // Encode the key
        // XXX Tune max key size?
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(300);
        Serializer keySerializer = Serializer.make(byteStream, myMaker);
        keySerializer.encodeGraph(key);

        // Write out a voided record.

        putBytes(byteStream.toByteArray(),null, key);
    }

    /**

     * Low-level record write. Key and data are now byte arrays and
     * will be written to disk without further conversions.

     * @param keyBytes The byte array of the serialized key
     * @param dataBytes The byte array of the serialized data
     * @param key The key itself, used only in error messages and may
     * be given as null.

     */

    private RepositoryHandle putBytes(byte[] keyBytes, byte[] dataBytes, Object key)
         throws IOException {
             int dataLength = 0;
             int keyBytesWritten;
             int dataBytesWritten;

             if (repLowLevelTrace.debug && Trace.ON)
                 repLowLevelTrace.debugm
                   ("Low level write - seek to " + myFileLength);

             synchronized(myFileToWriteTo.getFD()) { // Attempt to fix gray square bug
                 myFileToWriteTo.seek(myFileLength); // Seek to start of new record

                 if (keyBytes == null)
                     throw new IOException("Attempt to write null key to file '" +
                                           myFileName + "'");

                 keyBytesWritten = keyBytes.length;
                 myFileToWriteTo.writeInt(keyBytesWritten); // Write length of key
                 myFileToWriteTo.write(keyBytes);

                 if (repLowLevelTrace.debug && Trace.ON) {
                     repLowLevelTrace.debugm
                       ("Low level write - key length is " + keyBytesWritten);
                     repLowLevelTrace.debugm
                       ("Low level write - data is " + dataBytes);
                     if (dataBytes != null)
                         repLowLevelTrace.debugm
                           ("Low level write - data length is " + dataBytes.length);
                 }

                 if (dataBytes != null) {
                     dataBytesWritten = dataBytes.length;
                     myFileToWriteTo.writeInt(dataBytesWritten); // Write length of data
                     myFileToWriteTo.write(dataBytes); // Write encoded data
                 } else {

                     // We are writing a void record (a "pumpkin"). Write
                     // no data, but still write a (zero) data length.

                     dataBytesWritten = 0;
                     if (repLowLevelTrace.debug && Trace.ON)
                         repLowLevelTrace.debugm
                           ("Low level write - Writing a pumpkin");
                     myFileToWriteTo.writeInt(dataBytesWritten); // Write length of data (0 in this case)
                 }
             }
             // Update state for this file - Compute new file length etc.

             // Note that if anything goes wrong and throws anywhere
             // above, then we're quite happy to keep myFileLength and
             // myNumberRecords unchanged since that at least keeps
             // the file consistent. We may have lost the data we just
             // wrote but any half-finished disk data will (hopefully)
             // be overwritten next time we call put (barring hard
             // disk write errors), and the key Dictionary is not yet
             // updated (and therefore stays consistent).

             // Our data start position is the position of start of data.
             long dataStartPosition = myFileLength + keyBytesWritten + 2 * SIZEOF_INT;

             // Update file length so next record is written after our data
             myFileLength = dataStartPosition +  dataBytesWritten;

             myNumberRecords++; // Update number of records written
             keyHashDirty = true; // Save Dictionary in file when exiting or checkpointing
             fileInfoDirty = true;   // We changed several of the variables kept here

             if (repLowLevelTrace.debug && Trace.ON)
                 repLowLevelTrace.debugm
                   ("Low level write returning handle with position " + dataStartPosition +
                     " for key " + key );

             // Create a RepositoryHandle record that points to us (so we
             // know which file the data is in) and also contains the file
             // position the data starts at.

             RepositoryHandle entry =
               new RepositoryHandle(this, dataStartPosition, dataBytesWritten);
             return entry;
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
     * All other Repository get () primitives will eventually call
     * this one.

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
                    " not found, because repository not initialized, " +
                    "no key table available in file '" +
                                  myFileName + "'");
             }
             if (key == null) {
                 throw new RepositoryKeyNotFoundException
                   ("Repository get failed, key was null in file '" +
                                  myFileName + "'");
             }
             RepositoryHandle entry = (RepositoryHandle)myKeys.get(key);

             if (entry == null) {
                 throw new RepositoryKeyNotFoundException
                   ("Key " + key + " not found in file '" + myFileName + "'");
             }
             /* REPTIMESTART */
             Repository.hashTimer += NativeSteward.deltaTimerUSec(startTime);
             /* REPTIMEEND */
             return entry.getObject(key,parimeters);
    }

    /**

     * Retrieve data as a byte array, given a file position and data length.
     * Package scope help function only.

     */

    byte[] getDataBytes(Object key, long filePos, int dataLength)
         throws IOException, RepositoryKeyNotFoundException {

        /* REPTIMESTART */        
        long startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */

        if (dataLength < 0) {
            throw new IOException("Negative data length in Repository file at file position " +
                                  filePos + " in file " + myFileName + 
                                  " when retrieving data for key " + key);
        }

        if (filePos >= myFileLength || filePos < FIRST_DATA_POS) {
            if (filePos != myKeyTablePos) { // Special case - don't fail in initial code!
                throw new IOException("Attempt to seek to " + filePos + 
                                      " which is out of range of file of length " + myFileLength +
                                      " in Repository file " + myFileName +
                                      " when retrieving data for key " + key);
            }
        }

        byte[] result = new byte[dataLength];
        synchronized(myFileToReadFrom.getFD()) {        // Attempt to fix gray square bug
            myFileToReadFrom.seek(filePos);             // May throw IOException
            myFileToReadFrom.read(result);
        }
        /* REPTIMESTART */
        Repository.diskTimer += NativeSteward.deltaTimerUSec(startTime);
        /* REPTIMEEND */
        return result;
    }

    /**

     * Delete an object, given a key. 

     * @param key notNull untrusted - An object to use as a key.

     */

    public void delete(Object key) throws IOException, RepositoryKeyNotFoundException {

        if (myKeys == null) {
            throw new RepositoryKeyNotFoundException
              ("Delete failed, key " + key +
               " not found, because repository not initialized, " +
               "no key table available in file '" + myFileName + "'");
        }
        if (key == null) {
            throw new RepositoryKeyNotFoundException
              ("Repository delete failed, key was null in file '" + myFileName + "'");
        }

        RepositoryHandle entry = (RepositoryHandle)myKeys.get(key);
        if (entry == null) {
            throw new RepositoryKeyNotFoundException
              ("delete() - Key " + key + " not found in file '" +
                                  myFileName + "'");
        }
        else {

            // The file record layout is "key length, key, data length, data".
            // The entries in the key table point to the start of
            // data, not the start of the record. We have no way to
            // find the start of the data except to compute key
            // length. This is quite painful. We don't want to store
            // the length in the key table since we only need it for
            // delete operations which should be extremely rare. So we
            // pay for it here.
            
            ByteArrayOutputStream keyByteStream = new ByteArrayOutputStream(300);
            Serializer keySerializer = Serializer.make(keyByteStream, myMaker);
            keySerializer.encodeGraph(key);

            byte[] keyBytes = keyByteStream.toByteArray();
            int keyLength = keyBytes.length; // This is all we need - the length!
            entry.delete(keyLength);
        }
        myKeys.remove(key);     // Update our key Dictionary.
        keyHashDirty = true;    // Remember to write out Dictionary to file.
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
     * doesn't exist. Returs true if something was deleted.<p>

     * Note - This used to be a cheap operation but now it calls
     * exists() and delete() and is retained only for compatibility
     * with existing code.

     * @param key notNull untrusted - An object to use as a key.

     */

    public boolean deleteIfExists(Object key) {
        if (! exists(key)) return false;
        try {
            delete(key);
            return true;
        } catch (IOException iox) {
        }
        return false;
    }

    /**

     * Delete an entry (key and data), given a file position to the
     * first byte of the key length (note: the numbers in the key
     * table are offsets to the actual data bytes, not the start of
     * the key length, so we need to back-compute the start of the key
     * beginning position before calling this method).  This is done
     * by replacing the key-data pair with a key that is of zero
     * length and to set the data length to be the same as the old key
     * + data lengths, thus occupying the same number of bytes in the
     * file.

     */

    void deleteByFilePosition(long filePos) throws IOException {
        if (myFileToWriteTo == null) {
            throw new IOException("deleteByFilePosition(): RepositoryFile " + myFileName +
                                  " is not open for write access");
        }

        if (filePos > myFileLength || filePos < (FIRST_DATA_POS + 2 * SIZEOF_INT)) {
            throw new IOException("deleteByFilePosition attempted to seek to " + filePos + 
                                  " which is out of range for file of length " + myFileLength +
                                  " in Repository file " + myFileName);
        }

        synchronized(myFileToWriteTo.getFD()) { // Attempt to fix gray square bug

            myFileToWriteTo.seek(filePos);
            int keyLength = myFileToWriteTo.readInt();
            if (keyLength <= 0) {
                throw new IOException("Non-positive key length in Repository at file position " +
                                      filePos + " in file " + myFileName);
            }

            myFileToWriteTo.seek(filePos + keyLength + SIZEOF_INT);
            int dataLength = myFileToWriteTo.readInt();
            if (dataLength <= 0) {
                throw new IOException("Non-positive data length in Repository at file position " +
                                      filePos + " in file " + myFileName);
            }

            if (repLowLevelTrace.debug && Trace.ON)
                repLowLevelTrace.debugm
                  ("RepositoryFile: Low level delete record at " + filePos);

            myFileToWriteTo.seek(filePos);
            myFileToWriteTo.writeInt(0);     // Zero length key - marks deleted record.
            myFileToWriteTo.writeInt(keyLength + dataLength); // Claim deleted record is all data.
        }
    }

    public void addSymbolAndKeywords(CryptoHash key,Object symbol,Vector keywords) {
        if (symbol == null && keywords == null) return; // Nothing to do.

        if (! isWritable()) 
            throw new RuntimeException
              ("Attempt to update symbol table with symbol " + 
               symbol + " and/or keywords list in read-only file '" + myFileName + "'");

        if (symbol != null) {
            if (newSymbols == null) newSymbols = makeDictionary(20);
            newSymbols.put(symbol,key);
            symbolsDirty = true;
        }
        addKeywords(key,keywords);
    }

    /**
     * Add given keywords to keywords table for given cryptohash
     */

    private void addKeywords(CryptoHash cryptoHash, Vector keywords) {
        if (keywords == null || keywords.size() == 0) return; // Don't bother adding a null or empty vector

        if (keywordsTable == null) {
            try {
                keywordsTable = (Dictionary)get(SuperRepository.KEYWORDS_TABLE);
            } catch (IOException iox) {
            }
            if (keywordsTable == null) keywordsTable = makeDictionary(50);
        }
        keywords = ec.util.SortUtilities.quickSortStringVector(keywords); // sort new one
        Vector oldKeywords = (Vector)keywordsTable.get(cryptoHash); // Already sorted, if exists
        keywords = ec.util.SortUtilities.mergeSortedStringVectors(oldKeywords, keywords);
        keywordsTable.put(cryptoHash, keywords); // Add - now sorted.
        keywordsDirty = true;
    }

    /**

     * Close this RepositoryFile.  Flush the changes to the myKeys
     * Dictionary and the file info, then close the repository file.

     */

    public void close() throws IOException {
        flushChanges(true);
        myKeys = null;
        if (myFileToWriteTo != null) {
            myFileToWriteTo.close();
            myFileToWriteTo = null;
        }
        if (myFileToReadFrom != null) {
            myFileToReadFrom.close();
            myFileToReadFrom = null;
        }
    }

    /**

     * Flush changes to this RepositoryFile. As part of this, flush
     * the changes to the myKeys Dictionary and the file info. Warning
     * - As currently defined, this is a quite expensive operation,
     * not at all like the cheap semantics of flush() in most
     * filesystems. This may need some redesign.

     */

    public void flush() throws IOException {
        System.out.println("Flushing changes to writable repository file " + myFileName);
        flushChanges(true);     // XXX This may be way too expensive for this cheap operation.
    }

    /** 

     * Save out some info, possibly including our file-local keys
     * Dictionary (myKey) to the file.
     
     * @param saveEverything - If set then we save out the keys
     * Dictionary also. If not set, then we save out only the more
     * important fileinfo variables. The keys table can be recovered
     * at startup time so it may make sense not to write it out except
     * when quitting, and to accept the work of repairing the file if
     * a crash happens.

     */

    public void flushChanges(boolean saveEverything) throws java.io.IOException {
        if (myFileToWriteTo == null) return;              // Not writable, ignore request.

        if (Trace.repository.debug && Trace.ON)
            Trace.repository.debugm
              ("In flushChanges: keyHashDirty = " + keyHashDirty +
               " saveEverything = " + saveEverything + 
               " fileInfoDirty = " + fileInfoDirty + 
               " number records = " + myNumberRecords +
               " myFileToWriteTo.length() = " + myFileToWriteTo.length() +
               " File length = " + myFileLength +
               " Key table pos = " + myKeyTablePos + 
               " Key table length = " + myKeyTableLength);

        if (!keyHashDirty) {
            saveFileInfo();     // Write out the file info, if needed.
            return;             // but don't write out Dictionary
        }

        if (saveEverything) {

            // Symbol tables and keywords tables look similar but are
            // trated slightly differently below and
            // elsewhere. There's a big comment higher up in this file
            // that explains why.

            if (symbolsDirty) {
                Dictionary oldSymbols = (Dictionary)get(SuperRepository.SYMBOL_TABLE);
                if (oldSymbols == null) oldSymbols = newSymbols;
                else {
                    Enumeration newKeys = newSymbols.keys();
                    while (newKeys.hasMoreElements()) {
                        Object key = newKeys.nextElement();
                        oldSymbols.put(key, newSymbols.get(key));
                    }
                }
                // Write out updated symbol table to Repository on disk.
                put(SuperRepository.SYMBOL_TABLE, oldSymbols,null);
            }

            if (keywordsDirty) { // Write out keywords table to Repository on disk.
                put(SuperRepository.KEYWORDS_TABLE, keywordsTable,null);
            }

            RepositoryHandle keyHandle = null;

            // We add the key table as the last entry in the file.

            if (keySaveFormat == 1) { // Save as Dictionary or Object array, depending on value.
                int numberKeys = myKeys.size();
                Object[] keyArray = new Object[numberKeys * 2];
                Enumeration keys = myKeys.keys();
                int i = 0;
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    Object value = myKeys.get(key);
                    keyArray[i++] = key;
                    keyArray[i++] = value;
                }
                if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Saving key array");
                keyHandle = put(KEY_VECTOR_PSEUDO_KEY, keyArray, null);
            } else {
                if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Saving key Dictionary");
                keyHandle = put(KEY_VECTOR_PSEUDO_KEY, myKeys, null);
            }

            // We must compensate for the autoincrement of the number of records in put()
            // since we defined the number of records to exclude the key table.

            myNumberRecords--;

            if (keyHandle == null) {
                // Couldn't save keys. Rebuild them when opening instead.
                // This will be very slow but it may be the best we can do.
                Trace.repository.debugm
                  ("Repository could not save keys - they will be regenerated when reopened");
                return;
            }

            // We must extract the data position and length from the handle
            // since we store this information explicitly in the header

            myKeyTablePos = keyHandle.myDataPos;
            myKeyTableLength = keyHandle.myDataLength;

            keyHashDirty = false;                      // Not keyHashDirty anymore
            fileInfoDirty = true;               // We changed myKeyTablePos (at least)
            saveFileInfo();     // Write out the file info, if needed.
        } else {

            // if saveEverything is false we do a much cheaper save
            // even though the Dictionary has been changed. We only
            // save out the file info since we can (and will)
            // recompute the keys Dictionary at startup time, losing
            // some time but no data.

            /* Don't do this without thinking this through. The number
               of records must be nonzero for this to work, it seems.

            // As a further optimization, if we've already saved out
            // the key info once we've already marked the file as
            // having no key table.  Further updates of the file info
            // would serve only to update the number of records and
            // file length data, which can be derived at key
            // regeneration time should we quit without updating all
            // information. This optimization saves us one seek and
            // write per object stored in the file and may turn one
            // more seek into a zero-length seek.

            if (myKeyTablePos == -1 && myKeyTableLength == 0) return; // Optimization

            */

            myKeyTablePos = -1; // mark file as having no Dictionary. Don't clear keyHashDirty.
            myKeyTableLength = 0;
            fileInfoDirty = true; // We changed myKeyTablePos
            saveFileInfo();     // Write out the file info, if needed.
        }
    }

    /**

     * Read in the keys Dictionary from the file, if it's there. It
     * can be any datatype that implements the Dictionary interface,
     * such as java.util.Hashtable or ec.util.tables.SimTable . It is
     * up to the curator to decide on what dictionary type to use for
     * the Repository when creating it. <b>We</b> decide on what type
     * o Dictionary to use for freshly created repositories based on
     * what is decided by the factory
     * ec.e.rep.RepositoryFile.makeDictionary(int size)

     */

    private Dictionary readMyKeys() throws IOException {
        if (keyHashDirty) {            // If keyHashDirty, then table is already overwritten by us
            return null;        // and can't be read in anymore.
        }
        if (myKeyTablePos == -1) return null; // Not there to read for other reasons.
        Trace.repository.debugm("About to read in the keys");

        /* REPTIMESTART */
        long startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */

        Dictionary result = null;
        Object tempResult = null;
        byte[] bytes = null;

        try {
            int length = (int) myKeyTableLength;
            bytes = getDataBytes(KEY_VECTOR_PSEUDO_KEY, myKeyTablePos, length);
            if (bytes == null) {
                Trace.repository.debugm("No error thrown, but could not read keys from " +
                                        myFileName);
                return null;
            }
        } catch (Throwable t) {
            throw new IOException("Problem reading keys for Repository file named '" +
                                  myFileName + "' :" + t.getMessage());
        }

        /* REPTIMESTART */
        Repository.keyTableReadTimer += NativeSteward.deltaTimerUSec(startTime);
        startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */

        try {
            RepositoryHandleRecipe.setTheRepositoryFile(this);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            Unserializer unserializer = ParamUnserializer.make(byteStream,myMaker,null);
            tempResult = unserializer.decodeGraph();
        } catch (Throwable t) {
            RepositoryHandleRecipe.setTheRepositoryFile(null);
            throw new IOException("Problem decoding keys for Repository file named '" +
                                  myFileName + "' :" + t.getMessage());
        }
        RepositoryHandleRecipe.setTheRepositoryFile(null);

        String storageTypeString = "<unknown>";

        if (tempResult instanceof Dictionary) {
            result = (Dictionary)tempResult;
            storageTypeString = "Dictionary";
        }
        else {                  // Not a dictionary - stored as an array. Decode by hand.
            Object[] keyArray = (Object[]) tempResult;
            int arrayLength = keyArray.length;
            result = makeDictionary(arrayLength/2 + 50); // Makes a SimTable or Hashtable
            for (int i=0; i<arrayLength;) {
                result.put(keyArray[i++], keyArray[i++]);
            } 
            storageTypeString = "array";
        }

        long decodeMilliSeconds = 0;
        /* REPTIMESTART */
        long decodeMicroSeconds = NativeSteward.deltaTimerUSec(startTime);
        Repository.keyTableDecodeTimer += decodeMilliSeconds;
        decodeMilliSeconds = decodeMicroSeconds / 1000;
        /* REPTIMEEND */

        if (Trace.repository.debug && Trace.ON) {
            if (result instanceof Hashtable) {
                Trace.repository.debugm("Repositoryfile key " + storageTypeString +
                                        " in file " + myFileName +
                                        " decoded in " + decodeMilliSeconds/1000.0 +
                                        " and is a Hashtable of size " + result.size());
            } else if (result instanceof SimTable) {
                Trace.repository.debugm("Repositoryfile key " + storageTypeString +
                                        " in file " + myFileName +
                                        " decoded in " + decodeMilliSeconds/1000.0 +
                                        " and is a SimTable of size " + result.size());
            } else {    
                Trace.repository.debugm("Repositoryfile key " + storageTypeString +
                                        " in file " + myFileName +
                                        " decoded in " + decodeMilliSeconds/1000.0 +
                                        " and is a " + result.getClass().getName() + 
                                        " of size " + result.size());
            }
        }

        // XXX Remove the spam below before flight.

        if (result instanceof Hashtable) {
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Repositoryfile key " + storageTypeString +
                               " in file " + myFileName +
                               " decoded in " + decodeMilliSeconds/1000.0 +
                               " and is a Hashtable of size " + result.size());
        } else if (result instanceof SimTable) {
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Repositoryfile key " + storageTypeString +
                               " in file " + myFileName +
                               " decoded in " + decodeMilliSeconds/1000.0 +
                               " and is a SimTable of size " + result.size());
        } else {    
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Repositoryfile key " + storageTypeString +
                               " in file " + myFileName +
                               " decoded in " + decodeMilliSeconds/1000.0 +
                               " and is a " + result.getClass().getName() + 
                               " of size " + result.size());
        }
        //        if (Trace.repository.debug && Trace.ON) {
        //            (new Error("FYI - NOT AN ERROR - call chain to readmykeys " +
        //                       "for finding double Repository opening")).printStackTrace();
        //        }
        return result;              // and return it.
    }

    /**

     * If we get into trouble, so that we have no saved Dictionary in
     * the file, but believe the file may be otherwise consistent,
     * then we can regenerate the Dictionary information doing a
     * sequential read of the file. This will analyze the file and
     * return a Dictionary if it believes the file to be internally
     * consistent to a certain point. Only records believed to be
     * valid are entered into the Dictionary. <p>

     * One problem is recognizing the end-of-file. If the file length
     * information at the beginning of the file is incorrect, then we
     * may read in garbage data when we get past the consistent
     * portion of the file. This code should be independently verified
     * to always leave the fileLength information in the beginning of
     * the file such that all data in the file is consistent and
     * recoverable. The only unsafe operations are those that shorten
     * the file - writing records over the old image of the key
     * Dictionary (at file end), and deleting records, and rebuilding
     * the file by in-situ compacting (but his last alternative is not
     * likely to get implemented since file-to-file copying compaction
     * is so much easier).

     * @return a keys Dictionary of retrieved keys believed to point
     * to valid data records.  Returns null if no records could be
     * retrieved safely.

     */

    private Dictionary regenerateKeys(long probableNumberRecords,
                                     boolean reportOnly,
                                     Vector badKeys) throws IOException {
        Dictionary result = makeDictionary((int)probableNumberRecords + 20);

        long fileLength = myFileToReadFrom.length();

        if (fileLength < FIRST_DATA_POS) {
            if (reportOnly) {
                System.err.println("Repository File " + myFileToReadFrom +
                                     " is too short, only " + fileLength + " bytes");
                return null;
            }
            throw new IOException("Repository File " + myFileName +
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
                recordStartPos = filePos;               // Update ptr to valid record start pos
                synchronized(myFileToReadFrom.getFD()) {
                    myFileToReadFrom.seek(recordStartPos); // Seek to start of data record.
                    keyLength = myFileToReadFrom.readInt();             // Read length of key.
                    //             System.err.println("key length is " + keyLength +
                    // " filePos is " + filePos);
                    if (keyLength < 0) {
                        if (reportOnly) {
                            System.err.println
                              ("Negative keylength in Repository at FilePosition " +
                               filePos + " in file " + myFileToReadFrom);
                            return null;                           // Nothing we can do about this.
                        }
                        throw new IOException
                          ("Negative key length in Repository at FilePosition " +
                           filePos + " in file " + myFileName);
                    }
                    if (keyLength > fileLength) {             // Impossible.
                        if (reportOnly) {
                            System.err.println("Impossible key length (" + keyLength +
                                                 ") in Repository file at file position " +
                                                 filePos + " in file " + myFileToReadFrom);
                            return null;
                        } else {
                            throw new IOException("Impossible key length (" + keyLength +
                                                  ") in Repository file at file position " +
                                                  filePos + " in file " + myFileName);
                        }
                    }

                    // A key of length 0 indicates the record has been deleted.

                    if (keyLength == 0) {

                        dataLength = myFileToReadFrom.readInt();

                        // Record contains some overhead - key and
                        // data lengths as integers.  Key length is 0,
                        // and the rest is data length.

                        recordLength = dataLength + SIZEOF_INT + SIZEOF_INT;

                        if (longestRecord < recordLength) longestRecord = recordLength;

                        // For a deleted record, the key length is set
                        // to 0 and the data length is set to make it
                        // look like data occupies the entire deleted
                        // record in the file.

                        if (reportOnly) {
                            System.err.println("Record at position " + filePos +
                                                 " has been deleted. Deleted record occupies " +
                                                 recordLength + " bytes");
                        }

                        deletedRecords++;

                        // Skip record.  The data is of course just
                        // garbage bits, so don't read it in.

                    } else {

                        // Attempt to read in the key.

                        Object key;
                        byte[] keyAsBytes;

                        try {
                            keyAsBytes = new byte[keyLength];
                            myFileToReadFrom.read(keyAsBytes);
                        } catch (Exception ex1) {
                            if (reportOnly) {           // Catch errors, just report nicely.
                                System.err.println("Read of key of length " + keyLength +
                                                     " from position " + filePos +
                                                     " in file " + myFileToReadFrom +
                                                     " caused an Exception: " + ex1);
                                return null;                          // Not much left to do now.
                            }
                            // Pretend we got EOF - truncate file
                            throw new EOFException("Key read failed in file '" + myFileName + "'");
                        }

                        try {
                            ByteArrayInputStream keyByteStream = new ByteArrayInputStream
                              (keyAsBytes);
                            Unserializer unserializer = Unserializer.make(keyByteStream,myMaker);
                            key = unserializer.decodeGraph();
                        } catch (Exception ex2) {
                            if (reportOnly) {           // Catch errors, just report nicely.
                                System.err.println("Decode of key of length " + keyLength +
                                                     " from position " + filePos +
                                                     " in file " + myFileToReadFrom +
                                                     " caused an Exception: " + ex2);
                                corruptedRecords++;
                                key = null;             // Attempt to proceed without a key.
                            } else {
                                // Pretend we got EOF - truncate file
                                throw new EOFException("Key decode failed in file '" +
                                                       myFileName + "'");
                            }
                        }

                        dataLength = myFileToReadFrom.readInt();

                        // Record contains some overhead - key and data lengths as integers.
                        // So the whole record length is :

                        recordLength = keyLength + dataLength + SIZEOF_INT + SIZEOF_INT;

                        if (longestRecord < recordLength) longestRecord = recordLength;

                        if (dataLength < 0) {
                            if (reportOnly) {
                                System.err.println
                                  ("Negative data length in Repository file at file position " +
                                   filePos + " in file " + myFileToReadFrom);
                                return null;            // Nothing we can do about this.
                            }
                            throw new IOException
                              ("Negative data length in Repository file at file position " +
                               filePos + " in file " + myFileName);
                        }
                        if (dataLength > fileLength) {             // Impossible.
                            if (reportOnly) {
                                System.err.println("Impossible data length (" + dataLength +
                                                     ") in Repository file at file position " +
                                                     filePos + " in file " + myFileToReadFrom);
                                return null;
                            } else {
                                throw new IOException("Impossible data length (" + dataLength +
                                                      ") in Repository file at file position " +
                                                      filePos + " in file " + myFileName);
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

                            if (key == null) {          // If there was no key either. This is bad.
                                if (reportOnly) {
                                    System.err.println
                                      ("No key, and data length is 0 in Repository file " +
                                       myFileToReadFrom + " at file position " + filePos);
                                    return null;        // Nothing we can do about this.
                                }
                                throw new IOException
                                  ("No key, and data length is 0 in Repository file " +
                                   myFileToReadFrom + " at file position " +
                                   filePos + " in file '" + myFileName + "'");
                            }
                        }

                        // This looks like a good key and a reasonable data record -
                        // either actual data or a "nonexistent" entry. 

                        // If the key is a CryptoHash, then we can
                        // verify the actual data to be correct!

                        if (key instanceof CryptoHash) {
                            try {
                                byte[] dataAsBytes = new byte[dataLength];
                                myFileToReadFrom.read(dataAsBytes);
                                CryptoHash dataHash = new CryptoHash(dataAsBytes);
                                if (! (dataHash.equals(key))) {
                                    if (badKeys != null) badKeys.addElement(key);
                                    corruptedRecords++;
                                    System.err.println
                                      ("Detected corrupted data record for key " + key +
                                       " - Data cryptohash is " + dataHash);
                                } else verifiedRecords++;
                            } catch (IOException criox) {
                                if (reportOnly) {
                                    System.err.println("Error when reading data for key " +
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
                            long dataStartPosition = recordStartPos + keyLength + (2 * SIZEOF_INT);
                            RepositoryHandle entry =
                              new RepositoryHandle(this, dataStartPosition, dataLength);
                            result.put(key,entry); // Add to our reconstructed-key collection
                        }
                    }
                    foundRecords++;
                    filePos = recordStartPos + recordLength;
                }
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
            if (reportOnly) System.err.println("Truncated file after " + myNumberRecords +
                                                 " records (" + myFileLength + " bytes)");
        }

        long foundSymbols = 0;
        long missingSymbols = 0;
        Dictionary symbolTable = null;
        try {
            symbolTable = (Dictionary)get(SuperRepository.SYMBOL_TABLE);
        } catch (RepositoryKeyNotFoundException rknfx) {
            if (Trace.repository.debug && Trace.ON) 
                Trace.repository.debugm("Symbol table could not be found after repair attempt");
        }

        if (symbolTable != null) {
            Enumeration e = symbolTable.keys();
            while (e.hasMoreElements()) {
                Object key = e.nextElement();
                Object cryptoHash = symbolTable.get(key);
                Object value = null;
                try {
                    value = get(cryptoHash);
                    //if (value == null) {
                    //System.err.println("Retrieval of CRH for key " + key +
                    //" returned a null result");
                    //}                    
                } catch (Throwable t) {
                    //System.err.println("Retrieval of CRH for key " + key +
                    //" threw an error: " + t.getMessage());
                }
                if (value == null) missingSymbols++;
                else foundSymbols++;
            }
        }

        if (reportOnly) {
            System.err.println("Data      records: " + dataRecords);
            System.err.println("Deleted   records: " + deletedRecords);
            System.err.println("Total     records: " + foundRecords);
            System.err.println("Verified  records: " + verifiedRecords);
            System.err.println("Corrupted records: " + corruptedRecords);
            System.err.println("Longest   record : " + longestRecord + " bytes");
        }
        return result;
    }

    /**

     * An OpenerRecipe suitable for a Repository.
     * Based on a recipe from Openers.

     */

    // Note: This code is also duplicated in Repository.java

    static private OpenerRecipe defaultRecipe() {
        ClassRecipe classRecipe
          = AllowingClassRecipe.make(RootClassRecipe.THE_ONE,
                                     "ec.e.hold.ReposableMarker");

        return new OpenerRecipe(ReposableMarker.DECODER_MAKERS,
                                ReposableMarker.ENCODER_MAKERS,
                                classRecipe);
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

     * @param report - If set, also report what we find to System.err

     */

    public void repairFile(boolean repairIt, boolean report) throws IOException {
        Dictionary myOldKeys = myKeys;
        Vector badKeys = new Vector(100);

        myKeys = regenerateKeys(myNumberRecords, false, badKeys);
        if (report) {
            if (myKeys == null) {
                System.err.println("Could not regenerate keys");
            }
            else {
                System.err.println("Regenerated " + myKeys.size() + " keys");
            }
        }
        if (myKeys != null && badKeys.size() > 0) {
            Enumeration baddies = badKeys.elements();
            while (baddies.hasMoreElements()) {
                Object badKey = baddies.nextElement();
                if (isWritable() && repairIt) {
                    if (report) System.err.println("Deleting corrupt record " + badKey);
                    deleteIfExists(badKey); // Delete record from file.
                } else {
                    if (report) System.err.println("Temporarily ignoring corrupt record " + badKey);
                    myKeys.remove(badKey); // Remove from local Dictionary at least
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
                if (report) System.err.println("Updating file" + myFileToReadFrom);
                fileInfoDirty = true;
                flushChanges(true);
            }
        }
    }

    /**
      
     * Analyze file sequentially and print out detailed information
     * about every reccord in the file to System.err

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
        out.println("   --- Dump of keys from RepositoryFile " + myFileToReadFrom + " ---");
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
