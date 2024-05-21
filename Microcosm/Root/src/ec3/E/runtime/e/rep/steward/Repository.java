package ec.e.rep;

import ec.trace.Trace;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Dictionary;
import java.util.Enumeration;

import ec.cert.CryptoHash;

import ec.e.hold.ReposableMarker;
import ec.e.openers.*;
import ec.e.openers.AllowingClassRecipe;
import ec.e.serial.*;
import ec.e.rep.SimpleRepository;
import ec.e.rep.RepositoryAccessException;
import ec.e.rep.RepositoryKeyNotFoundException;
import ec.e.rep.RepositoryNeedParameterException;

// JAY import ec.regexp.RegularExpression;

import ec.util.ReadOnlyHashtable;
import ec.tables.PEHashtable;
import ec.util.NestedException;
import ec.util.SortUtilities;
import ec.e.hold.DataHolderRecipe;

import ec.tables.SimTable;

import ec.e.inspect.Inspector;

// For Repository performance measurements
/* REPTIMESTART */
import ec.util.NativeSteward;
/* REPTIMEEND */


    /**

     * A Repository is a subsystem that manages access to a small
     * number of data files known as RepositoryFiles. Each Repository
     * can thus have several files, and each running system may use
     * several Repositories implementing different policies. <p>

     * The most common policy is to use Repositories as a permanent
     * storage and distribution medium. As an example, a virtual world
     * could be distributed on a CD-ROM, and parts of the data in the
     * world could be kept in a Repository file on the CD-ROM. (The
     * other main part in a CD-ROM distribution would be a persistent
     * checkpoint image file) <p>

     * A variant of this is to use RepositoryFiles as download units
     * for medium-size packages of data such as realms created by
     * third parties. A user could download a RepositoryFile over the
     * web and by simply placing it into the appropriate directory the
     * data in it would become available to a Repository that manages
     * import of such modules to the running system. These files could
     * be used to distribute patches, to speed up access to entire
     * turfs, and (later) to add new functionality to the system. <p>

     * Yet another variation is to use a Repository to implement a
     * network cache, storing downloaded objects in case they are
     * needed again. These caches are pruned on an Least-Recently-Used
     * basis by discarding old repositoryfiles, after all recently
     * referenced objects have been copied to later-generation
     * repositoryfiles. This is (or will be) implemented in
     * CacheRepository.java <p>

     * A running system could thus have several different
     * repositories, each implementing one of these roles. It is
     * necessary to keep these as separate Repositories since the
     * policies don't mix. <p>

     * Each Repository has one or more RepositoryFiles associated with
     * it.  These RepositoryFiles form a searchpath.  Only the first
     * RepositoryFile can be (but need not be) opened for write
     * acccess. <p>

     * A record in a repository will override the records in
     * Repositories behind it. As a special case, a record with a
     * valid key but a zero-length value will pretend to be
     * "nonexistent", possibly overriding real values in
     * RepositoryFiles behind it. This is necessary since there would
     * otherwise be no way to delete values found in files late in the
     * RepositoryFile chain.<p>

     * Two common patterns receive extra support: Opening all files in
     * a directory as a single repository, sorting them by filename -
     * This is used to collect add-on feature, patch, and speedup
     * files; and opening all files in a directory by filename, adding
     * one writable file with a name "beyond" the last-sorting such
     * filename. This is used to support cache directories - look for
     * this in CacheRepository.java.

     * All rights to a repository are defined to be equal to the
     * rights you have to the repository directory itself and the
     * files therein and (if applicable) to the explicitly specified
     * firstFile. <p>

     */

public class Repository implements FilenameFilter, SimpleRepository {

    final static String DEFAULT_REPOSITORY_NAME = "Repository";
    static final Trace myRepositoryTiming = new Trace("repositorytiming"); // package access

    // Our first repository file. Probably the only writable one.

    protected RepositoryFile myFrontMostFile = null; // Always used.

    protected long myFileSizeLimit = 2 * 1024 * 1024; // 2 MB limit for each cache/published file

    // The name of this repository, mostly used in error messages.
    protected String myRepositoryName = "<unknown>";

    // If we have more than one file to search through, then we keep
    // them all in a vector in repositoryFiles.  This includes the one
    // in myFrontMostFile which would be at position 0. If we don't have
    // more than one repositoryfile, then we don't bother creating the
    // vector at all.

    protected Vector myRepositoryFiles = null;
    protected Dictionary mySymbolTable = RepositoryFile.makeDictionary(1);

    // XXX SSS is myMaker a protected variable a security leak if
    // someone subclasses repository?  It is needed by
    // CacheRepository, as an optimization.

    protected static OpenerRecipe myMaker = defaultRecipe();

    protected Dictionary myKeys = null; // The master Dictionary for this repository

    public static long setupTimer = 0; // Cumulative time spent in setups of various kinds
    public static long initTimer = 0; // Initializing upon startup
    public static long hashTimer = 0; // Cumulative time spent in Dictionary lookups
    public static long diskTimer = 0; // Disk accesses
    public static long unsTimer = 0; // Unserializing
    public static long fileTimer = 0; // file access from non-repository files
    public static long objectDecodeTimer = 0;
    public static long byteArrayDecodeTimer = 0;

    public static long keyTableReadTimer = 0;
    public static long keyTableDecodeTimer = 0;
    public static long keyTableCollectionTimer = 0; // Merge keys from our files into single table

    public static long symbolTableReadTimer = 0; // Read in symbol tables
    public static long symbolTableCollectionTimer = 0; // Decode symbol tables (below)
    private static boolean gathered = false;

    /* REPTIMESTART */
    public static Dictionary counters = RepositoryFile.makeDictionary(20); // Count data types in repository
    public static Dictionary loadTimeTotals = RepositoryFile.makeDictionary(20); // Count load time by data type
    /* REPTIMEEND */

    public static void clearTimers() {
        hashTimer = 0;
        setupTimer = 0;
        diskTimer = 0;
        unsTimer = 0;
        objectDecodeTimer = 0;
        byteArrayDecodeTimer = 0;
        keyTableReadTimer = 0;
        keyTableDecodeTimer = 0;
        keyTableCollectionTimer = 0;
        initTimer = 0;
        fileTimer = 0;
    }

    /**

     * Dump all timers used by the Repository. Only available in
     * development releases of The Product.

     */

    // XXX For backward compatibility. Delete later if not used.

    public static void dumpTimers() {
        dumpTimers("          Repository timers");
    }

    /**

     * Dump all timers used by the Repository. Only available in
     * development releases of The Product.

     */

    public static void dumpTimers(String header) {
        /* REPTIMESTART */
        if (!gathered) {
            gathered = true;
            Inspector.gather(counters,"Repository",
                                          "Number of instances loaded, by class");
            Inspector.gather(loadTimeTotals, "Repository",
                                          "Instance load time totals, by class");
        }

        String msg = header + '\n';
        if (myRepositoryTiming.debug && Trace.ON) {
            msg +=
              "\nTime spent in hashes: " + hashTimer/1000.0 +
              "\n              setups: " + setupTimer/1000.0 +
              "\n       disk accesses: " + diskTimer/1000.0 +
              "\n        Total decode: " + unsTimer/1000.0 +
           // "\n       Object decode: " + objectDecodeTimer/1000.0 +
           // "\n    ByteArray decode: " + byteArrayDecodeTimer/1000.0 +
              "\n     Key table reads: " + keyTableReadTimer/1000.0 +
              "\n    Key table decode: " + keyTableDecodeTimer/1000.0 +
              "\n     Key table merge: " + keyTableCollectionTimer/1000.0 +
           // "\n  Repository startup: " + initTimer/1000.0 +
           // "\n         file access: " + fileTimer/1000.0 +
              "\n";

            Enumeration e = loadTimeTotals.keys();

            msg += "==========================================================\n";
            msg += "Repository-stored object class names, # instances and load time totals:\n\n";

            while (e.hasMoreElements()) {
                Object key = e.nextElement();
                long[]time = (long[]) loadTimeTotals.get(key);
                String keyString = key.toString();
                if (keyString.equals("class [B")) keyString = "Byte Arrays";
                else
                    if (keyString.length() >6)
                        keyString = keyString.substring(6); // Skip "class " part.
                keyString = (keyString +
                             " ..................................................").substring(0,50);
                Object count = counters.get(key);
                String numberString = "N/A";
                if (count != null) numberString = Long.toString(((long[])count)[0]);
                numberString = "....... " + numberString;
                numberString = numberString.substring(numberString.length() - 6);
                String timeString = "............ " + time[0]/1000.0 + " ms";
                timeString = timeString.substring(timeString.length() - 12); // use 12 last chars
                msg += keyString + numberString + " " + timeString + '\n';
            }
            msg += "==========================================================\n";
            myRepositoryTiming.debugm(msg);

        }
        clearTimers();
        /* REPTIMEEND */
    }

    /**
     * Constructor to create a Repository given a directory and
     * (optionally) a distinguished, possibly writable file. <p>
     *
     * @param repositoryDir notNull - an EDirectoryBase. Could be (and
     * most often is) a File object representing a directory..
     *
     * @param firstFile nullOK - a File object. If given, and if
     * writable, then this file is opened as a writable RepositoryFile
     * and becomes the frontmost file in this repository. <p>
     *
     * If the firstFile cannot be opened as a writable file, then it
     * <b>still</b> becomes the first file but it is opened
     * read-only. <p>
     *
     * All other RepositoryFiles in the directory are opened and added
     * to the set of searched files in reverse order by name - files
     * with names that sort first are searched last. This is
     * consistent with CacheRepository files which are named in
     * chronological order, since we want the latest files to be
     * searched first. All files in the vector are opened for
     * read-only access, except if there is a duplicate entry of
     * firstFile, in which case it will not be opened a second time.
     */
    public Repository(File repositoryDir, File firstFile) throws IOException {
        if (repositoryDir.isDirectory()) {
            myRepositoryName = repositoryDir.getName();
            String[] dirContents = repositoryDir.list(this);
            SortUtilities.quickSortStringArray(dirContents, 0, dirContents.length - 1, true);
            myRepositoryFiles = addRepFilesInOrder(repositoryDir, dirContents, firstFile);

             if (myRepositoryFiles == null) {
                 throw new IOException("No Repository files in directory " + repositoryDir);
             }

             myFrontMostFile = (RepositoryFile)myRepositoryFiles.elementAt(0);

             // If we only have one file, don't keep the vector around.

             if (myRepositoryFiles.size() <= 1) {
                 myRepositoryFiles = null;
             }
             myKeys = collectAllEntries();
        }
    }

    /**

     * Constructor to create a Repository given a java.io.File. This
     * is the only constructor of a single-file repository that can
     * open a writable repository. In-vat objects don't have access to
     * class java.io.File

     * @param file notNull - a java.io.File
     * @param writable - This Repository will be writable if this flag is set

     */

    public Repository(File file, boolean writable) throws IOException {
        myRepositoryName = file.getName();
        myFrontMostFile = new RepositoryFile(file, writable);
        myKeys = collectAllEntries();
    }

    /**

     * Constructor to create a Repository given a vector of
     * files.

     * @param files - an array of strings.  All files are opened for
     * read access only. Directories in the vector are ignored.

     * XXX This has turned into a security hole since it allows anyone
     * in the vat to open a read-only repository file given nothing
     * but a pathname. This should move into a crew class immediately.

     */

    public Repository(String[] files) throws IOException {
        myKeys = null;
        myRepositoryFiles = addRepFilesInOrder(null, files, null);
        if (myRepositoryFiles == null) {
            throw new IOException("No Repository files in vector" + files);
        }
        myFrontMostFile = (RepositoryFile)myRepositoryFiles.elementAt(0);

        // If we only have one file, don't keep the vector around.

        if (myRepositoryFiles.size() <= 1) {
            myRepositoryFiles = null;
        }
        myKeys = collectAllEntries();
    }

    /**

     * Constructor to create a worthless dummy Repository to use as a
     * parimeter argument in the Curator and possibly other places.

     */

    public Repository() {
    }

    /**

     * Constructor to create a read-only Repository given a File object.

     * @param aFile notNull - A File object.

     */

    public Repository(File aFile) throws IOException {
        this(aFile, false);
    }

    public boolean accept(File dir, String name) {
        if (new File(dir, name).isFile()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**

     * Internal utility method that throws the right kind of exception
     * when a key lookup fails

     */

    private RepositoryHandle getRepositoryHandle(Dictionary dict, Object key)
         throws RepositoryKeyNotFoundException {
             RepositoryHandle result = (RepositoryHandle)dict.get(key);
             if (result == null)
                 throw new RepositoryKeyNotFoundException
                   ("Key " + key.toString() + " not found in Repository " + myRepositoryName);
             else return result;
    }

    /**

     * Increment a string by treating each character as a number
     * so that "FILE000" would return "FILE001"

     */

    public static String incrementString(String s) {
        if (Trace.repository.debug && Trace.ON)
            Trace.repository.debugm
              ("Incrementstring for file name '" + s + "'");
        int last = s.length() - 1;
        if (last < 0) return ""; // Time to wrap to a new name!
        String firstPart = s.substring(0,last);
        char c = s.charAt(last);
        if (c == '9') return firstPart + 'A'; // Wrap from digits to letters
        else if (c >= 'Z') return incrementString(firstPart) + '0'; // Wrap to next digit
        return firstPart + (char)(c + 1);
    }

    /**

     * Increment a filename, such as a DOS-compatible filename, by
     * incrmenting the characters in the name. Stay DOS-compatible by
     * using only 0-9 and A-Z .

     * Extension, the part after the last dot, if any, is left intact.

     */

    public static String incrementName(String s) {
        String ext = "";
        String base;
        if (s == null) s = "0000000.REP"; // Default name
        int dotPos = s.lastIndexOf('.');
        if (dotPos < 0) return incrementString(s); // No extension
        return incrementString(s.substring(0,dotPos)) + s.substring(dotPos); // Don't touch extension.
    }

    /**

     * Flush changes to the Repository if it is writable, i.e call
     * flush() on its frontmostfile.

     */

    public void flush() throws IOException {
        if (myFrontMostFile != null) myFrontMostFile.flush();
    }

    /**

     * Close the Repository. Closes all of its RepositoryFiles. You
     * cannot use the repository after you close it.

     */

    public void close() throws IOException {
        RepositoryFile rf = null;
        if (myRepositoryFiles != null) {
            Enumeration repositoryFilesEnumeration = myRepositoryFiles.elements();
            while (repositoryFilesEnumeration.hasMoreElements()) {
                rf = (RepositoryFile)repositoryFilesEnumeration.nextElement();
                rf.close();
            }
            myRepositoryFiles = null;
            myKeys = null;
        } else {            // If repositoryFiles exists,
            rf = myFrontMostFile; // then this is first element also
            if (rf != null) rf.close();     // so don't close it twice.
        }
        myFrontMostFile = null;
        myKeys = null;
    }

    /**

     * A Writability predicate.

     */

    public boolean isWritable() {
        return (myFrontMostFile != null && myFrontMostFile.isWritable());
    }

    /**

     * Simplest way to store an object in the Repository using a
     * specific key. It will throw an IOException if the
     * frontMost RepositoryFile is not writable.

     */

    public void put(Object key, Object object) throws IOException {
        if (! isWritable())
            throw new IOException("Attempt to put object " + object +
                                  "with key  " + key +
                                  " into closed or read-only Repository");
        try {

            // RepositoryFile.put with three arguments returns a
            // RepositoryHandle object.  Keep our own Dictionary
            // (myKeys) in sync by putting it there also.

            myKeys.put(key,myFrontMostFile.put(key, object, null));
        } catch (IOException e) {
            String msg = "Repository put of " + object +
              " under key " + key +
              " caused IOException: " + e.getMessage();
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            throw new IOException(msg);
        }
    }

    /**

     * Stores an object into the Repository under the given key
     * object. The parimeters (a PEHashtable)
     * and properties (a Hashtable) arguments are
     * used when encoding the object.  It will throw an
     * IOException if the frontMost RepositoryFile is
     * not writable.

     * @Param key A key to store the object under, any non-null object
     * but most often a String.

     * @Param object The value to store in the Repository.

     * @param parimeters A PEHashtable collection of Objects
     * (as keys) and token objects (typically Strings) as values. When
     * encoding the object, any object that can be found in the
     * parimeters collection gets replaced by its corresponding token.

     */

    public void put(Object key, Object object, PEHashtable parimeters) throws IOException {
        if (! isWritable())
            throw new IOException("Attempt to put object " + object +
                                  "with key  " + key +
                                  " into closed or read-only Repository");
        try {

            // RepositoryFile.put with three arguments returns a
            // RepositoryHandle object.  Keep our own Dictionary
            // (myKeys) in sync by putting it there also.

            myKeys.put(key, myFrontMostFile.put(key, object,parimeters));
        } catch (IOException e) {
            String msg = "Repository put of " + object +
              " under key " + key +
              " caused IOException: " + e.getMessage();
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            throw new IOException(msg);
        }
    }

    /**

     * Stores an object into the Repository using the cryptohash of
     * the data as a key.  It returns that key, an instance of
     * CryptoHash. It will throw a IOException if the
     * Repository is not writable.

     * @param object The object to save in the Repository using an
     * automatically generated key.

     * @return The generated hash key used (an instance of class CryptoHash).

     */

    public CryptoHash putHash(Object object) throws IOException {
        if (! isWritable())
            throw new IOException("Attempt to put object " + object +
                                  " into closed or read-only Repository");

        try {
            CryptoHash result = myFrontMostFile.putHash(object);

            // We know the hashtable lookup in the next line will work
            // since we just added it. We need to do this lookup
            // because we need the RepositoryHandle to update our own
            // key Dictionary.

            Object repositoryHandle = myFrontMostFile.myKeys.get(result); // Retrieve handle
            myKeys.put(result,repositoryHandle); // Keep own Dictionary in sync
            return result;
        } catch (IOException e) {
            String msg = "Repository putHash of " + object +
              " caused IOException: " + e.getMessage();
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            throw new IOException(msg);
        }
    }

    /**

     * Stores an object into the Repository using the cryptohash of
     * the data as a key.  It returns that key, an instance of
     * CryptoHash. It will throw a IOException if the
     * Repository is not writable.

     * @param object The object to save in the Repository using an
     * automatically generated key.

     * @return The generated hash key used (an instance of class CryptoHash).

     */

    public CryptoHash putHash(Object object, PEHashtable parimeters) throws IOException {
        if (! isWritable())
            throw new IOException("Attempt to put object " + object +
                                                " into closed or read-only Repository");

        try {
            CryptoHash result = myFrontMostFile.putHash(object,parimeters);

            // We know the hashtable lookup in the next line will work
            // since we just added it. We need to do this lookup
            // because we need the RepositoryHandle to update our own
            // key Dictionary.

            Object repositoryHandle = myFrontMostFile.myKeys.get(result); // Retrieve handle
            myKeys.put(result,repositoryHandle); // Keep own Dictionary in sync
            return result;
        } catch (IOException e) {
            String msg = "Repository putHash of " + object +
              " caused IOException: " + e.getMessage();
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            throw new IOException(msg);
        }
    }

    /**

     * Stores an already encoded object, given as a byte array,
     * into the Repository using the cryptohash of
     * the data as a key.  It returns that key, an instance of
     * CryptoHash. It will throw a IOException if the
     * Repository is not writable.

     * @param bytes The bytes (a byte array) to save in the Repository
     * using an automatically generated key.

     * @return The generated hash key used (an instance of class CryptoHash).

     */

    public CryptoHash putHashBytes(byte[] bytes, Object data) throws IOException {
        if (! isWritable())
            throw new IOException ("Attempt to put byte array " + bytes +
                                   " into closed or read-only Repository");
        try {
            CryptoHash result = myFrontMostFile.putHashBytes(bytes, data);

            // We know the hashtable lookup in the next line will work
            // since we just added it. We need to do this lookup
            // because we need the RepositoryHandle to update our own
            // key Dictionary.

            Object repositoryHandle = myFrontMostFile.myKeys.get(result); // Retrieve handle
            myKeys.put(result,repositoryHandle); // Keep own Dictionary in sync
            return result;
        } catch (IOException e) {
            String msg = "Repository putHashBytes of " + data +
              " caused IOException: " + e.getMessage();
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            throw new IOException(msg);
        }
    }

    /**

     * Return the RepositoryHandle for a given key if one exists in
     * the Repository keys Dictionary.

     * @param key A key to search for, any non-null object but most
     * often a String or cryptohash object.

     * @return The requested RepositoryHandle if record exists and false otherwise<p>

     */

    RepositoryHandle getHandle(Object key) {
        /* REPTIMESTART */
        long startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */
        RepositoryHandle handle = (RepositoryHandle)myKeys.get(key);
        /* REPTIMESTART */
        long endTime = NativeSteward.queryTimer();
        hashTimer += NativeSteward.deltaTimerUSec(startTime,endTime);
        /* REPTIMEEND */

        // RepositoryDirectDataHandles have inscrutable lengths This
        // gives us problems below when requesting %SymbolTable% so we
        // fix it by checking for it in the most unusual case (for
        // efficiency).

        if (handle == null || handle.size() <= 0) {
            if (handle instanceof RepositoryDirectDataHandle) return handle;
            return null;
        }
        return handle;
    }

    /**

     * Determine whether an object with a given key exists in the
     * Repository.

     * @param key A key to search for, any non-null object but most
     * often a String or cryptohash object.

     * @return true if record exists and false otherwise<p>

     */

    public boolean exists(Object key) {
        RepositoryHandle handle = (RepositoryHandle)myKeys.get(key);
        if (handle == null || handle.size() <= 0) return false;
        return true;
    }

    /**

     * Reports on the size of the data for the given key.
     * If any error occurs, returns -1.

     */

    public int size(Object key) {
        try {
            RepositoryHandle handle = (RepositoryHandle)myKeys.get(key);
            if (handle == null) return 0;
            return handle.size();
        } catch (Exception e) {
        }
        return -1;
    }

    public int numberFiles() {
        if (myRepositoryFiles != null) return myRepositoryFiles.size();
        return 1;
    }

    public Object getCryptoHash(Object symbol) throws RepositoryKeyNotFoundException {
        Object result = mySymbolTable.get(symbol);
        if (result == null)
            throw new RepositoryKeyNotFoundException("No symbol " + symbol + " in Repository");
        return result;
    }

    /**

     * Simplest way to retrieve an object stored under a given
     * key. Returns null if the key could not be found in any
     * of our RepositoryFiles, or if the found object could not be
     * completely decoded for any reason. If you want to know what went
     * wrong, use another get() method.

     * @param key notNull - A key to search for, any non-null object
     * but most often a String or cryptohash object.

     * @return The retrieved object, or null.

     */

    public Object get(Object key) {
        try {
            /* REPTIMESTART */
            long startTime = NativeSteward.queryTimer();
            /* REPTIMEEND */
            RepositoryHandle handle = (RepositoryHandle)myKeys.get(key);
            /* REPTIMESTART */
            hashTimer += NativeSteward.deltaTimerUSec(startTime);
            /* REPTIMEEND */
            if (handle == null) return null;// Not there.
            return handle.getObject(key,null);
        } catch (Exception e) {
            // We explicitly declare that we don't return any exceptions from here
            // If you want to catch exceptions, use another get() method.
        }
        return null;
    }

    /**

     * Retrieve an object stored under a given key. If found, returns
     * the object stored under that key. Throws an exception if the
     * key could not be found in any of our RepositoryFiles, or if the
     * found object could not be completely decoded for any reason. <p>

     * The parimeterArguments argument (a Hashtable),
     * if given, is used when decoding the object. If the object in
     * the Repository requires a parameter token/value pair (to be
     * substituted for an object that was pruned when encoding the
     * object) but either no parimeterArguments collection was given, or it
     * does not contain an entry for the parameter, then a
     * RepositoryNeedParameterException is thrown. <p>

     * If the object could not be decoded for any other reason, then a
     * IOException is thrown.

     * @param key notNull - A key to search for, any non-null object but most
     * often a String.

     * @param parimeterArguments nullOK - A Hashtable collection
     * of token objects (typically Strings) as keys and Objects as
     * values. When decoding the object, any parameter token that is
     * encountered in the Repository is looked up in the parimeterArguments
     * collection and gets replaced with the value found there.<p>

     * @return The retrieved object. You may need to cast the return
     * object to your target data type. Never returns null.

     */

    public Object get(Object key, Hashtable parimeterArguments) throws IOException {
        /* REPTIMESTART */
        long startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */
        RepositoryHandle handle = (RepositoryHandle)myKeys.get(key);
        /* REPTIMESTART */
        hashTimer += NativeSteward.deltaTimerUSec(startTime);
        /* REPTIMEEND */
        if (handle == null)
            throw new RepositoryKeyNotFoundException
              ("Key " + key.toString() + " not found in Repository " + myRepositoryName);
        try {
            return handle.getObject(key,parimeterArguments);
        } catch (RepositoryNeedParameterException rnpe) {
            String msg = "Repository get() RepositoryNeedParameterException - key=" + key;
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, rnpe);
            throw new RepositoryNeedParameterException(msg);
        } catch (IOException rae) {
            String msg = "Repository get() exception - key=" + key +
              " - Error is " + rae.getMessage();
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg);
            throw new IOException(msg);
        }
    }

    /**

     * Retrieve an object stored under a given key.
     * Does not use a parimeter table.<p>
     * Warning - Throws no exceptions at all!
     * Returns null if key could not be found or ir anything else went wrong.

     * @param key - Any object, but most often a CryptoHash instance.
     * @return Requested Object or null if object could not be found.

     */

    public Object maybeGet(Object key) {
        if (key != null) {
            try {
                return get(key,null);
            } catch (IOException iox) {
            }
        }
        return null;
    }

    /**

     * Retrieve an object stored under a given symbol, i.e. the symbol
     * is looked up in the symbol table and if it exists there, the
     * key in the table is used to retrieve the object. Note that this
     * implies that the Repository search path may cause certain
     * occurrences of the symbol to be shadowed by others.<p>

     * Does not use a parimeter table.<p>
     * Warning - Throws no exceptions at all!
     * Returns null if key could not be found or ir anything else went wrong.

     * @param key - Any object, but most often a CryptoHash instance.
     * @return Requested Object or null if object could not be found.

     */

    public Object maybeGetBySymbol(Object symbol) {    // Get object by symbolic name or null
        try {
            Object key = getCryptoHash(symbol);
            return maybeGet(key);
        } catch (RepositoryKeyNotFoundException rknfx) {
        }
        return null;
    }

    /**

     * Retrieve an object stored under a given symbol, i.e. the symbol
     * is looked up in the symbol table and if it exists there, the
     * key in the table is used to retrieve the object. Note that this
     * implies that the Repository search path may cause certain
     * occurrences of the symbol to be shadowed by others.<p>

     * Does not use a parimeter table.<p>

     * @param key - Any object, but most often a CryptoHash instance.
     * @return Requested Object

     * @exception IOException

     */

    public Object getBySymbol(Object symbol) throws IOException {
        return getBySymbol(symbol,null); // We don't have a parimeter table
    }

    /**

     * Stores an object into the Repository using the cryptohash of
     * the data as a key, and updates the symbol table to contain an
     * entry for the new Cryptohash under a given symbol.

     * It returns that key, an instance of CryptoHash. It will throw a
     * IOException if the Repository is not writable.

     * @param object The object to save in the Repository using an
     * automatically generated key.

     * @return The generated hash key used (an instance of class CryptoHash).

     */

    public CryptoHash putHashBySymbol(Object object, Object symbol,
                                      Vector keywords,
                                      PEHashtable parimeters)
         throws IOException {
             CryptoHash result = putHash(object,parimeters);
             if (Trace.repository.debug && Trace.ON)
                 Trace.repository.debugm
                   ("puthashbysymbol - Object is " + object +
                    " and symbol is " + symbol + " and crh is " +
                    result);
             myFrontMostFile.addSymbolAndKeywords(result,symbol,keywords);
             if (symbol != null) mySymbolTable.put(symbol,result);
             return result;
    }

    /**

     * Retrieve an object stored under a given symbol, i.e. the symbol
     * is looked up in the symbol table and if it exists there, the
     * key in the table is used to retrieve the object. Note that this
     * implies that the Repository search path may cause certain
     * occurrences of the symbol to be shadowed by others.<p>

     * @param key - Any object, but most often a CryptoHash instance.


     * @param parimeterArguments - A Hashtable containing the required
     * arguments for deparimeterization, which is the replacement of
     * parimeter object in the decoding object graph with locally
     * available objects. <p>

     * If the object in the Repository requires a parameter
     * token/value pair (to be substituted for an object that was
     * pruned when encoding the object) but either no parimeterArguments
     * collection was given, or it does not contain an entry for the
     * parameter, then a RepositoryNeedParameterException is
     * thrown. <p>

     * @return Requested Object

     * @exception RepositoryNeedParameterException
     * @exception RepositoryKeyNotFoundException (a subclass of IOException)
     * @exception IOException

     */

    public Object getBySymbol(Object symbol, Hashtable parimeterArguments)
         throws IOException {
             Object key =  getCryptoHash(symbol); // May throw RepositoryKeyNotFoundException
             return get(key,parimeterArguments); // May throw IOExcception.
    }

    /**

     * Get all values for a given key from all repositoryfiles we
     * have.  This method overrides the shadowing effect and really
     * collects all values for the key.

     * @param symbol A symbol to be looked up in alll symbol tables.
     */

    public Vector getAll(Object key) {
        Vector result = new Vector();
        getAll(key,null,result);
        return result;
    }

    /**

     * Get all values for a given key from all repositoryfiles we
     * have.  This method overrides the shadowing effect and really
     * collects all values for the key.

     * @param parimeterArguments - A Hashtable containing the required
     * arguments for deparimeterization, which is the replacement of
     * parimeter object in the decoding object graph with locally
     * available objects. <p>

     * If the object in the Repository requires a parameter
     * token/value pair (to be substituted for an object that was
     * pruned when encoding the object) but either no parimeterArguments
     * collection was given, or it does not contain an entry for the
     * parameter, then a RepositoryNeedParameterException is
     * thrown. <p>

     * @exception RepositoryNeedParameterException

     * @param symbol A symbol to be looked up in alll symbol tables.
     */

    public Vector getAll(Object key, Hashtable parimeterArguments) {
        Vector result = new Vector();
        getAll(key,parimeterArguments,result);
        return result;
    }

    /**

     * Get all values for a given key from all repositoryfiles we
     * have.  This method overrides the shadowing effect and really
     * collects all values for the key.

     * @param symbol A symbol to be looked up in alll symbol tables.

     * @param parimeterArguments - A Hashtable containing the required
     * arguments for deparimeterization, which is the replacement of
     * parimeter object in the decoding object graph with locally
     * available objects. <p>

     * In order to not discard all objects when some of them have
     * problems with missing parimeters (since they may be added by
     * third parties without total knowledge of the set of parimeters
     * available at decode time) any thrown
     * RepositoryNeedParameterException objects get collected to the
     * result vector also. This means that when examining the result
     * vector you may encounter RepositoryNeedParameterException
     * objects among the data uou want. Note that since users may add
     * anything they want under any symbol you want you can never
     * guarantee that all objcts you collect in this manner are what
     * you expect anyway so you will have to excercise caution when
     * using this feature.

     * Somewhat unorthogonally we throw IOException and
     * RepositoryKeyNotFoundException if something else goes wrong
     * since these are much more surprising in this situation - All
     * keys in the keyword tables should normally exist. And all
     * repository accesses should happen without I/O errors.

     * @return A Vector of objects, some of which may well be
     * RepositoryNeedParameterException objects.

     */

    public void getAll(Object key, Hashtable parimeterArguments, Vector result) {
        Vector repFiles = myRepositoryFiles;
        if (repFiles == null) { // No vector, only one file. Fake it.
            repFiles = new Vector(1);
            repFiles.addElement(myFrontMostFile);
        }

        int nrFiles = repFiles.size(); // Number of files we have;
        int i;

        for (i = 0; i < nrFiles; i++) {
            RepositoryFile repFile = (RepositoryFile)(repFiles.elementAt(i));
            RepositoryHandle h = (RepositoryHandle)repFile.myKeys.get(key);
            if (h != null) {
                try {
                    Object o;
                    try {
                        o = h.getObject(key,parimeterArguments);
                    } catch (RepositoryNeedParameterException rnpx) {
                        o = rnpx; // Collect the exception since we can't get a real object
                    }
                    if (o != null) result.addElement(o);
                } catch (IOException iox) {
                    throw new NestedException("IO error retrieveing " + key, iox);
                }
            }
        }
    }

    /**

     * For all RepositoryFiles in the whole SuperRepository, find
     * all keyword tables and look for a given keyword.  Return a
     * Vector with the keys for all such entries, removing duplicates.
     * Note that this includes entries added by users to Extras
     * directory and the Publish directory.<p>

     * <b>Note</b> At the moment, keywords added to a
     * PublishRepository while running won't be available until the
     * PublishRepository file is closed. This is surprisingly hard to
     * fix. In current usage this should not be a problem, but the
     * keyword lookup mechansim should be reimplemented.

     * @param keyword - A symbol (often a String) to search for.
     * @return A Vector containing all unique keys that had that keyword.

     */

    public Vector getKeywordSet(Object keyword) {
        return SuperRepository.mergeKeywordVectors
          (getAll(SuperRepository.GROUP_TABLE, null), keyword);
    }

    /**

     * Determine the existence of a record with a given key in the
     * repositoryFiles vector starting with index 1. This method is
     * only called from delete() - If you need to use this then you
     * are likely doing something wrong.

     */

    private boolean deepSearch(Object key) {
        int nrFiles = myRepositoryFiles.size(); // Number of files we have
        int i;

        for (i = 1; i < nrFiles; i++) { // ignore the first file, it has been checked
            RepositoryFile repFile = (RepositoryFile)(myRepositoryFiles.elementAt(i));
            if (repFile.myKeys.get(key) != null) return true;
        }
        return false;
    }

    /**

     * Deletes a record (key and its value) from the Repository.

     * Adds a voiding record to myFrontMostFile if all entries in path
     * could not be deleted (since they may be in a read-only
     * repository file)

     * @param key notNull - A key object identifying the record to be deleted.

     * @return true if something was successfully deleted and false
     * otherwise.

     */

    public boolean delete(Object key) throws IOException {
        boolean result = false;  // True if we deleted something
        if (! isWritable())
            throw new IOException("Attempt to delete record using key " + key +
                                                " from closed or read-only Repository");

        try {
            result = myFrontMostFile.deleteIfExists(key);
            myKeys.remove(key); // Update our Dictionary - forget the handle.

            // We don't know whether we need a voiding record unless we look for the key
            // in all the remaining repository files using a deep search.

            if (myRepositoryFiles == null) return result; // handle the easy case first.
            if (deepSearch(key)) { // Do the deep analysis
                myFrontMostFile.voidRecord(key); // Void old record or write a fresh voiding record.
                result = true;
            }
        } catch (IOException e) {
            String msg = "Repository delete() exception - key=" + key;
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            throw new IOException(msg);
        }
        return result;
    }

    /**

     * Return the total number of records in this entire
     * repository. This is exactly the number of records in our
     * shallow binding key Dictionary

     */

    public int size() {
        if (myKeys == null) return 0;
        return myKeys.size();
    }

    /**

     * Analyze all repositoryfiles to ensure they are
     * consistent. Results are output to EStdIO.err(). Available to
     * allow interactive Repository tools to analyze problems. Does
     * not modify any files <p>

     * We catch all exceptions so we can display results from all
     * files in the repository. <p>

     */

    public void analyzeFiles() {
        Exception lastException = null;
        if (myRepositoryFiles != null) {
            int nrFiles = myRepositoryFiles.size(); // Number of files we have
            for (int i = 0; i < nrFiles; i++) {
                try {
                    ((RepositoryFile)myRepositoryFiles.elementAt(i)).analyzeFile();
                } catch (Exception e) {
                    lastException = e;
                    String msg = "File analysis caught an exception: " + e.getMessage();
                    if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg);
                }
            }
        } else {
            try {
                if (myFrontMostFile != null) {
                    myFrontMostFile.analyzeFile();
                }
            } catch (Exception e) {
                String msg = "File analysis caught an exception: " + e.getMessage();
                if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg);
            }
        }
    }

    /**

     * Attempt to repair damage in all repositoryfiles.  For
     * non-writable files we will just update the myKeys Dictionary.
     * If the file is writable we will also repair the file by
     * rewriting the reconstructed keys Dictionary to it. <p>

     * We catch and save exceptions so that we will at least try to
     * repair all files. If one or more exceptions were caught, we
     * re-throw the last one when we're done. <p>

     * @args report - is given as true, then we will report all
     * inconsistencies and some progress info to EStdio.err(). <p>

     */

    public void repairFiles(boolean report) throws IOException {
        IOException lastException = null;
        if (myRepositoryFiles != null) {
            int nrFiles = myRepositoryFiles.size(); // Number of files we have
            for (int i = 0; i < nrFiles; i++) {
                try {
                    ((RepositoryFile)myRepositoryFiles.elementAt(i)).repairFile(true,report);
                } catch (IOException e) {
                    lastException = e;
                    if (report) {
                        System.err.println("File repair caught an exception: " + e);
                    }
                }
            }
        } else {
            try {
                if (myFrontMostFile != null) {
                    myFrontMostFile.repairFile(true,report);
                }
            } catch (IOException e) {
                lastException = e;
                if (report) {
                    System.err.println("File repair caught an exception: " + e);
                }
            }
        }
        if (lastException != null) throw lastException;
        return;
    }

    /**

     * Collect all key entries from the individual repositoryfiles
     * into one master key Dictionary. This is called a "shallow
     * binding" Dictionary since it resembles the shallow binding
     * variable scheme in certain computer language interpreter
     * implementations. its main advantage is speed at retrieval time
     * since if the item is not in this table, then it is not in any
     * repository file in this repository. <p>

     * Every entry in this table is already marked with the
     * repositoryfile it comes from. it is important that this master
     * key hastable be built from all repositoryfiles in reverse order
     * so that duplicate keys will get overwritten with their "first"
     * definition, the same one that would have been found in a deep
     * search.

     * The main disadvantage is that it needs to be updated in
     * parallel whenever the frontmost file is written to but this is
     * normally rare. <p>

     */

    protected Dictionary collectAllEntries() {
        if (myRepositoryFiles != null) {
            return collectAllEntries(myRepositoryFiles);
        } else {                // Fake a vector...
            Vector fileVector = new Vector(1);
            fileVector.addElement(myFrontMostFile);
            return collectAllEntries(fileVector);
        }
    }

    /**

     * Create and return one single Dictionary that contains all the
     * key entries for all Dictionarys for all files in the given
     * Vector

     * Also merge all entries in all repositoryfiles that have the key
     * "%SymbolTable%" that are Dictionarys and put the result of that
     * merge (a large Dictionary) into the resulting key table.

     * @param repFiles notNull - a Vector of RepositoryFiles.

     */

    protected Dictionary collectAllEntries(Vector repFiles) {
        /* REPTIMESTART */
        long startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */

        int nrFiles = repFiles.size(); // Number of files we have
        int recordCount = 0;    // Number of records in all files
        int i;
        Vector symbolTables = new Vector(repFiles.size());
        int symbolTableSize = 0;
        Dictionary bigSymbolTable = null;
        RepositoryFile rf;

        // It pays to special case the single file case.  We keep
        // doing it in here since we want all this code in one place.

        if (nrFiles == 1) {
            RepositoryFile myRepFile = (RepositoryFile)repFiles.elementAt(0);
            Dictionary result = myRepFile.myKeys;
            if (result == null)
                throw new RuntimeException("No keys - Corrupt RepositoryFile");

            try {
                Object symTab = myRepFile.get(SuperRepository.SYMBOL_TABLE);
                if (symTab != null && symTab instanceof Dictionary) {
                    if (Trace.repository.debug && Trace.ON) {
                        Trace.repository.debugm("Repository cosists of a single file with " +
                                               ((Dictionary)symTab).size() + " symbols");
                    }
                    mySymbolTable = (Dictionary)symTab;
                }
            } catch (RepositoryKeyNotFoundException rknfx) {
                String msg = "Could not find a %SymbolTable% when collecting entries: " +
                  rknfx.getMessage();;
                if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg);
                mySymbolTable = RepositoryFile.makeDictionary(1);
            } catch (IOException iox) {
                String msg = "Problem collecting symbol table entries: " + iox.getMessage();
                if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg);
                mySymbolTable = RepositoryFile.makeDictionary(1);
            }
            result.put(SuperRepository.SYMBOL_TABLE,
                       new RepositoryDirectDataHandle(mySymbolTable));
            /* REPTIMESTART */
            symbolTableReadTimer += NativeSteward.deltaTimerUSec(startTime);
            /* REPTIMEEND */
            return result;
        }


        for (i = 0; i < nrFiles; i++) {
            rf = (RepositoryFile)repFiles.elementAt(i);
            if (rf == null) continue;
            recordCount += rf.myKeys.size();
            try {
                Object symTab =((RepositoryFile)repFiles.elementAt(i)).get
                  (SuperRepository.SYMBOL_TABLE);
                if (symTab != null) {
                    if (symTab instanceof Dictionary) {
                        symbolTables.addElement(symTab);
                        symbolTableSize += ((Dictionary)symTab).size();
                        if (Trace.repository.debug && Trace.ON) {
                            Trace.repository.debugm("Found symboltable, size is " +
                                              ((Dictionary)symTab).size());
                        }
                    } else symbolTables.addElement(null);
                }
            } catch (RepositoryKeyNotFoundException rknfx) {
                String msg = "Could not find a %SymbolTable% when collecting entries: " +
                  rknfx.getMessage();
                if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg);
                symbolTables.addElement(null);
            } catch (IOException iox) {
                String msg = "Problem collecting symbol table entries: " + iox.getMessage();
                if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg);
                symbolTables.addElement(null);
            }
        }

        Dictionary result =
          RepositoryFile.makeDictionary(recordCount + 10); // Likely an overestimate
        if (symbolTableSize > 0) bigSymbolTable =
                                   RepositoryFile.makeDictionary(symbolTableSize + 100);

        /* REPTIMESTART */
        symbolTableReadTimer += NativeSteward.deltaTimerUSec(startTime);
        /* REPTIMEEND */

        // We must handle shadowing here. Work backwards.  That way
        // entries early on in the repFiles vector will overwrite
        // entries from later Dictionarys, mirroring the key shadowing
        // behavior.


        for (i = nrFiles - 1; i >= 0; i--) { // Go through each file in vector
            RepositoryFile repFile = (RepositoryFile)(repFiles.elementAt(i));
            if (repFile == null) continue;

            /* REPTIMESTART */
            startTime = NativeSteward.queryTimer();
            /* REPTIMEEND */
            Enumeration keysEnumeration = repFile.myKeys.keys(); // Get the keys from this file

            while (keysEnumeration.hasMoreElements()) { // Go through them
                Object key = keysEnumeration.nextElement();
                result.put(key,repFile.myKeys.get(key)); // and collect records to result
            }

            /* REPTIMESTART */
            keyTableCollectionTimer += NativeSteward.deltaTimerUSec(startTime);
            startTime = NativeSteward.queryTimer();
            /* REPTIMEEND */

            // Handle symbol table also. Once this code works,
            // generalize it to allow any symbol to e merged, as long
            // as the value is a Dictionary.

            Dictionary fileSymbols = (Dictionary)symbolTables.elementAt(i);
            if (fileSymbols != null) {
                Enumeration symbolEnumeration = fileSymbols.keys();
                if (Trace.repository.debug && Trace.ON) {
                    Trace.repository.debugm("Collecting " + fileSymbols.size() +
                                      " symbols from %SymbolTable%");
                }
                while (symbolEnumeration.hasMoreElements()) { // Go through them
                    Object key = symbolEnumeration.nextElement();
                    bigSymbolTable.put(key,fileSymbols.get(key)); // and collect values
                }
            }
            /* REPTIMESTART */
            symbolTableCollectionTimer += NativeSteward.deltaTimerUSec(startTime);
            /* REPTIMEEND */
        }

        // We add the symboltable as a piece of direct data to the key
        // table. The RepositoryDirectDataHandle subclass was created
        // specifically to handle this case.

        // It would be bad to save the merged symboltable as part of
        // the data in the Repository since each file's symboltable is
        // already stored in its file and we always must merge them at
        // startup. But this is exactly what would happen if we store
        // out the key dictionary of a single-file Repository when
        // closing it since the SymbolTable is kept in the KeyTable
        // under the key %SymbolTable%. The fix: Data held by a
        // RepositoryDirectDataHandle are not saved to a Repository
        // because they have a null recipe, so this should be safe.

        if (bigSymbolTable == null) bigSymbolTable = RepositoryFile.makeDictionary(1);
        if (Trace.repository.debug && Trace.ON) {
            Trace.repository.debugm("Repository master symbol table has " +
                              bigSymbolTable.size() + " entries");
        }
        mySymbolTable = bigSymbolTable;
        result.put(SuperRepository.SYMBOL_TABLE,new RepositoryDirectDataHandle(mySymbolTable));
        return result;
    }

    /**

     * Returns an enumerator to access all records by key, for
     * debugging purposes. In the future it will either be removed
     * using a CPP directive or will require some certain debugging
     * capability.

     * @return An enumeration containing all keys in the
     * Repository.

     */

    public synchronized Enumeration keys() {
        if (myKeys == null) return null;
        return myKeys.keys();
    }

    /*

     * This method has been disabled since it is quite expensive.  You
     * can write it using keys() and get() and you will indeed access
     * every record already when building a vector of values. <p>

     * Returns an enumerator to access all records for debugging
     * purposes, by element. In the future it will either be removed
     * using a CPP directive or will require some certain debugging
     * capability.

     * @return An enumeration containing all elements in the
     * Repository.

     */

//     public synchronized Enumeration elements() {
//         if (myKeys == null) return null;
//         myKeys.elements();    // WRONG - returns RepositoryHandles, not stored objects
//     }


    /**

     * Compute a cryptohash for a given object using the given
     * OpenerRecipe. The result is an instance of class CryptoHash.

     */


    public static CryptoHash computeCryptoHash(Object data, OpenerRecipe recipe) {
             if (data == null) {     // Special case this since Dictionaries cannot handle it.
                 return CryptoHash.nullCryptoHash();
             }
             ByteArrayOutputStream dataByteStream = new ByteArrayOutputStream(10000);
             try {
                 Serializer dataSerializer = Serializer.make(dataByteStream, recipe);
                 dataSerializer.encodeGraph(data);
                 byte[] dataAsBytes = dataByteStream.toByteArray();
                 if (dataAsBytes.length > 100000) {
                     Trace.repository.debugm("Warning: ComputeCryptoHash encountered large (" +
                                             dataAsBytes.length + " bytes) object: " + data);
                 }
                 return new CryptoHash(dataAsBytes);
             } catch (IOException iox) {
                 throw new NestedException("Could not compute cryptohash for object - " +
                                            "possibly because of outward references from it. " +
                                            " Object is " + data + " and error message is " + 
                                            iox.getMessage(), iox);
             }
    }

    /**

     * Compute a cryptohash for an object using our all-powerful
     * RootClassRecipe ObjOpener Recipe.  It's OK to have a static
     * function do this, since it conveys no authority.

     */

    public static CryptoHash computeCryptoHash(Object data) {
        return computeCryptoHash(data,myMaker);
    }

    /**

     * An OpenerRecipe suitable for a Repository.

     */

    static private OpenerRecipe defaultRecipe() {
        ClassRecipe classRecipe
          = AllowingClassRecipe.make(RootClassRecipe.THE_ONE,
                                     "ec.e.hold.ReposableMarker");

        return new OpenerRecipe(ReposableMarker.DECODER_MAKERS,
                                ReposableMarker.ENCODER_MAKERS,
                                classRecipe);
    }

    /**

     * Attempt to collect keys from all files in vector.  For now at
     * least we ignore directories. Arguments could be made both ways
     * about whether to allow subdirectories so I'm going with the
     * solution that's easiest to implement. <p>

     * If no valid files could be found we return null rather than an
     * empty vector. <p>

     * This method is static since we want to safely call it from
     * constructors.

     */
    protected static Vector addRepFilesInOrder(String[] fileNames) throws IOException {
        return addRepFilesInOrder(null, fileNames, null);
    }

    protected static Vector addRepFilesInOrder(File directory, String[] fileNames, File firstFile)
         throws IOException
    {
        int nrFiles = 0;
        Vector result = null;

        if (fileNames != null) {
            nrFiles += fileNames.length;
        }
        if (firstFile != null) {
            nrFiles += 1;
        }
        if (nrFiles == 0) {
            return null; // Nothing there, might as well quit now.
        }
        result = new Vector(nrFiles);

        try {
            if (firstFile != null) {
                if (firstFile.canWrite()) {
                    result.addElement(new RepositoryFile(firstFile, true)); // Writable!
                }
                else if (firstFile.canRead()) {
                    result.addElement(new RepositoryFile(firstFile,false)); // Not so
                }
            }
        }
        catch (NotRepositoryFileException nrpx) { // Not a Repository file. Ignore quietly.
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm(nrpx.getMessage());
        }

        if (fileNames != null) {
            for (int i = 0; i < fileNames.length; i++) {
                // Don't open firstfile twice
                if ((firstFile != null) && fileNames[i].equals(firstFile.getName())) {
                    continue;
                }

                RepositoryFile newFile = null;
                File aFile = directory == null?
                    new File(fileNames[i]) : new File(directory, fileNames[i]);

                try {
                    if (aFile.canWrite()) {
                        newFile = new RepositoryFile(aFile, true);
                        result.addElement(newFile);
                    }
                    else if (aFile.canRead()) {
                        newFile = new RepositoryFile(aFile, false);
                        result.addElement(newFile);
                    }
                    else {
                        if (Trace.repository.debug && Trace.ON) {
                            Trace.repository.debugm
                                ("Repository found an UFO (Unknown File Object) " + aFile);
                        }
                    }
                }
                catch (NotRepositoryFileException nrpx) { // Not a Repository file. Ignore quietly.
                    if (Trace.repository.debug && Trace.ON)
                        Trace.repository.debugm(nrpx.getMessage());
                }
            }
        }

        if ((result == null) || (result.size() == 0)) {
            return null;
        }

        return result;
    }

    /**

     * Gather statistics on per-class unserializing times.

     */

    public static void gatherDecodeStatistics(Object result,long deltaTime) {
        if (result == null) return;
        Class resultClass = result.getClass();
        long[] counter = (long[])counters.get(resultClass);
        if (counter != null) counter[0]++;
        else {
            counter = new long[] {1};
            counters.put(resultClass,counter);
        }

        long[] timeByClass = (long[])loadTimeTotals.get(resultClass);
        if (timeByClass != null) timeByClass[0] += deltaTime;
        else {
            timeByClass = new long[] {deltaTime};
            loadTimeTotals.put(resultClass, timeByClass);
        }
    }
}
