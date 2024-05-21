package ec.e.rep;

import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import java.io.*;
import java.util.*;
import ec.e.file.*;
import ec.e.openers.*;
import ec.e.serial.*;
import ec.util.ReadOnlyHashtable;
import ec.util.PEHashtable;
import ec.util.NestedException;
import ec.e.util.DiscreteEnumeration;
import ec.cert.CryptoHash;
import ec.e.rep.steward.SimpleRepository;
import ec.e.rep.steward.RepositoryAccessException;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.rep.steward.RepositoryNeedParameterException;

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

     * We may or may not use a repository to store Certificates. This
     * Repository would grow as new Certificates were automatically
     * added by downloading them but would not delete them like a
     * Cache would.<p>

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

public class Repository implements SimpleRepository {

    final static String DEFAULT_REPOSITORY_NAME = "Repository";
    static final Trace repositoryTrace = new Trace("repository"); // package access
    static final Trace repositoryTiming = new Trace("repositorytiming"); // package access

    // Our first repository file. Probably the only writable one.

    private RepositoryFile frontMostFile = null; // Always used.

    // If we have more than one file to search through, then we keep
    // them all in a vector in repositoryFiles.  This includes the one
    // in frontMostFile which would be at position 0. If we don't have
    // more than one repositoryfile, then we don't bother creating the
    // vector at all.

    private Vector repositoryFiles = null;
    private Hashtable mySymbolTable = null;
    private static OpenerRecipe myMaker = defaultRecipe();

    private Hashtable myKeys = null; // The master hashtable for this repository

    public static long setupTimer = 0; // Cumulative time spent in setups of various kinds
    public static long initTimer = 0; // Initializing upon startup
    public static long hashTimer = 0; // Cumulative time spent in hashtable lookups
    public static long diskTimer = 0; // Disk accesses
    public static long unsTimer = 0; // Unserializing
    public static long fileTimer = 0; // file access from non-repository files
    public static long objectDecodeTimer = 0;
    public static long byteArrayDecodeTimer = 0;

    public static void dumpTimers() {
        /* REPTIMESTART */
        ec.e.inspect.Inspector.gather(RepositoryHandle.counters,
                                      "Repository","Number of instances loaded by class");

        ec.e.inspect.Inspector.gather(RepositoryHandle.loadTimeTotals,
                                      "Repository","Instance load time totals by class");

        repositoryTiming.debugm("\nTime spent in hashes: " + hashTimer/1000 +
                                "\n              setups: " + setupTimer/1000 +
                                "\n       disk accesses: " + diskTimer/1000 +
                                "\n        Total decode: " + unsTimer/1000 + 
                                "\n       Object decode: " + objectDecodeTimer/1000 + 
                                "\n    ByteArray decode: " + byteArrayDecodeTimer/1000 + 
                                "\n    Repository TOTAL: " +
                                ((hashTimer + setupTimer + diskTimer + unsTimer)/1000) + 
//                                "\n  Repository startup: " + initTimer/1000 + 
//                                "\n         file access: " + fileTimer/1000 +
                                "");

        Enumeration e = RepositoryHandle.loadTimeTotals.keys();

        EStdio.out().println("==========================================================");
        EStdio.out().println
          ("Repository-stored object class names, # instances and load time totals:\n");

        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Long time = (Long) RepositoryHandle.loadTimeTotals.get(key);
            String keyString = key.toString();
            if (keyString.equals("class [B")) keyString = "Byte Arrays";
            else keyString = keyString.substring(6); // Skip "class " part.
            keyString = (keyString +
                         " ..................................................").substring(0,50);
            Object count = RepositoryHandle.counters.get(key);
            String numberString = "N/A";
            if (count != null) numberString = ((Long)count).toString();
            numberString = "....... " + numberString;
            numberString = numberString.substring(numberString.length() - 6);
            String timeString = "............ " + time.longValue()/1000 + " ms";
            timeString = timeString.substring(timeString.length() - 12); // use 12 last chars
            EStdio.out().println(keyString + numberString + " " + timeString);
        }
        EStdio.out().println("==========================================================");
        /* REPTIMEEND */
    }

    /** 

     * Constructor to create a Repository given a directory and
     * (optionally) a distinguished, possibly writable file. <p>

     * @param repositoryDir notNull - an EDirectoryBase. Could be (and
     * most often is) an EEditableDirectory.

     * @param firstFile nullOK - an EEditableFile. If given, and if
     * writable, then this file is opened as a writable RepositoryFile
     * and becomes the frontmost file in this repository. <p>

     * If the firstFile cannot be opened as a writable file, then it
     * <b>still</b> becomes the first file but it is opened
     * read-only. <p>

     * All other RepositoryFiles in the directory are opened and added
     * to the set of searched files in reverse order by name - files
     * with names that sort first are searched last. This is
     * consistent with CacheRepository files which are named in
     * chronological order, since we want the latest files to be
     * searched first. All files in the vector are opened for
     * read-only access, except if there is a duplicate entry of
     * firstFile, in which case it will not be opened a second time.

     */

    public Repository(EDirectoryBase repositoryDir, EEditableFile firstFile) 
         throws RepositoryAccessException, IOException {
             ReadOnlyHashtable unsorted = repositoryDir.contents();
             Vector sortedNames = sortNamesOfFilesOnly(unsorted);
             Vector sortedFiles = null;
                 
             if (sortedNames != null) {
                 int nrElements = sortedNames.size();
                 sortedFiles = new Vector(nrElements);
                 for (int i = nrElements - 1; i >= 0; i--) {  // Add in reverse order!
                     sortedFiles.addElement(unsorted.get(sortedNames.elementAt(i)));
                 }
             }
             repositoryFiles = addRepFilesInOrder(sortedFiles, firstFile);

             if (repositoryFiles == null) {
                 throw new IOException("No Repository files in directory " + repositoryDir);
             }

             frontMostFile = (RepositoryFile)repositoryFiles.elementAt(0);

             // If we only have one file, don't keep the vector around.
                 
             if (repositoryFiles.size() <= 1) {
                 repositoryFiles = null;
             }
             myKeys = collectAllEntries();
    }

    /** 

     * Constructor to create a Repository given an EEditableFile. This
     * is the only constructor of a single-file repository that can
     * open a writable repository.

     * @param file notNull - an EEditableFile.
     * @param writable - This Repository will be writable if this flag is set

     */

    public Repository(EEditableFile file, boolean writable) throws IOException {
        EFileReader reader = (writable)?file.editor():file.asReadableFile().reader();
        frontMostFile = new RepositoryFile(reader, writable);
        myKeys = collectAllEntries();
    }

    /** 

     * Constructor to create a Repository given an
     * EEditableFile. Note: The Repository will *not* be writable if
     * you use this method.

     * @param file notNull - an EEditableFile.

     */

    public Repository(EEditableFile file) throws IOException {
        this(file.asReadableFile().reader());
    }

    /** 

     * Constructor to create a Repository given an EReadableFile.
     * @param file notNull - an EReadableFile. This repository will be read-only.

     */

    public Repository(EReadableFile file) throws IOException {
        this(file.reader());
    }

    /** 

     * Constructor to create a Repository given a an EFileEditor or
     * EFileReader. <p>

     * @param reader notNull - an EFileReader. Could be a EFileEditor, in
     * which case this Repository is writable.

     */

    public Repository(EFileReader reader) throws IOException {
        frontMostFile = new RepositoryFile(reader, false);
        myKeys = collectAllEntries();
    }

    /** 

     * Constructor to create a Repository given a an EFileEditor or
     * EFileReader. <p>

     * @param reader notNull - an EFileReader. 
     * @param writable - If set, and if the reader is an instance of
     * EFileEditor, then the Repository is opened for Write.

     */

    public Repository(EFileReader reader, boolean writable) throws IOException {
        frontMostFile = new RepositoryFile(reader, writable);
        myKeys = collectAllEntries();
    }

    /**

     * Constructor to create a Repository given a vector of
     * files.

     * @param files - a vector of EDirectoryElement's.  All files are
     * opened for read access only, whether or not they are instances
     * of EEditableFile. Directories in the vector are ignored. Other
     * items will throw an IOException.

     */

    public Repository(Vector files) throws IOException {
        myKeys = null;
        repositoryFiles = addRepFilesInOrder(files, null);
        if (repositoryFiles == null) {
            throw new IOException("No Repository files in vector" + files);
        }
        frontMostFile = (RepositoryFile)repositoryFiles.elementAt(0);

        // If we only have one file, don't keep the vector around.
                 
        if (repositoryFiles.size() <= 1) {
            repositoryFiles = null;
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

     * Close the Repository. Closes all of its RepositoryFiles. You
     * cannot use the repository after you close it.

     */

    public void close() throws IOException {
        RepositoryFile rf = null;
        if (repositoryFiles != null) {
            Enumeration repositoryFilesEnumeration = repositoryFiles.elements();
            while (repositoryFilesEnumeration.hasMoreElements()) {
                rf = (RepositoryFile)repositoryFilesEnumeration.nextElement();
                rf.close();
            }
            repositoryFiles = null;
            myKeys = null;
        } else {            // If repositoryFiles exists, 
            rf = frontMostFile; // then this is first element also
            rf.close();     // so don't close it twice.
        }
        frontMostFile = null;
        myKeys = null;
    }

    /**

     * A Writability predicate.

     */

    public boolean isWritable() {
        return (frontMostFile != null && frontMostFile.isWritable());
    }

    /**

     * Simplest way to store an object in the Repository using a
     * specific key. It will throw an RepositoryAccessException if the
     * frontMost RepositoryFile is not writable.

     */

    public void put(Object key, Object object) throws RepositoryAccessException {
        if (! isWritable())
            throw new RepositoryAccessException("Attempt to put object " + object +
                                                "with key  " + key +
                                                " into closed or read-only Repository");
        try {

            // RepositoryFile.put returns a RepositoryHandle object.
            // Keep our own hashtable in sync by putting it there also.

            myKeys.put(key,frontMostFile.put(key, object, null)); 
        } catch (IOException e) {
            throw new RepositoryAccessException("Repository put of " + object +
                                                " under key " + key +
                                                " caused IOException: " + e.getMessage());
        }
    }

    /**

     * Stores an object into the Repository under the given key
     * object. The parimeters (a PEHashtable)
     * and properties (a Hashtable) arguments are
     * used when encoding the object.  It will throw an
     * RepositoryAccessException if the frontMost RepositoryFile is
     * not writable.

     * @Param key A key to store the object under, any non-null object
     * but most often a String.

     * @Param object The value to store in the Repository.

     * @param parimeters A PEHashtable collection of Objects
     * (as keys) and token objects (typically Strings) as values. When
     * encoding the object, any object that can be found in the
     * parimeters collection gets replaced by its corresponding token.

     */

    public void put(Object key, Object object, PEHashtable parimeters)
         throws RepositoryAccessException {
             if (! isWritable())
                 throw new RepositoryAccessException("Attempt to put object " + object +
                                                     "with key  " + key +
                                                     " into closed or read-only Repository");
             try {
                 myKeys.put(key, frontMostFile.put(key, object,parimeters));
             } catch (IOException e) {
                 throw new RepositoryAccessException("Repository put of " + object +
                                                     " under key " + key +
                                                     " caused IOException: " + e);
             }
    }

    /**

     * Stores an object into the Repository using the cryptohash of
     * the data as a key.  It returns that key, an instance of
     * CryptoHash. It will throw a RepositoryAccessException if the
     * Repository is not writable.

     * @param object The object to save in the Repository using an
     * automatically generated key.

     * @return The generated hash key used (an instance of class CryptoHash).

     */

    public CryptoHash putHash(Object object)  throws RepositoryAccessException {
        if (! isWritable())
            throw new RepositoryAccessException("Attempt to put object " + object +
                                                " into closed or read-only Repository");
        
        try {
            CryptoHash result = frontMostFile.putHash(object);
            Object repositoryHandle = frontMostFile.myKeys.get(result); // Retrieve handle
            myKeys.put(result,repositoryHandle); // Keep own hashtable in sync
            return result;
        } catch (IOException e) {
            throw new RepositoryAccessException("Repository putHash of " + object +
                                                " caused IOException: " + e);
        }
    }

    /**

     * Stores an object into the Repository using the cryptohash of
     * the data as a key.  It returns the CryptoHash object.<p>

     * The parimeters (a PEHashtable) and
     * properties (a Hashtable) arguments are used when encoding the
     * object.  It will throw an RepositoryAccessException if the
     * Repository is not writable.

     * @param object The object to save in the Repository

     * @param parimeters A PEHashtable of Objects
     * (as keys) and token objects (typically Strings) as values. When
     * encoding the object, any object that can be found in the
     * parimeters collection gets replaced by its corresponding token.

     * @param properties A hashtable of information that is made
     * accessible to the Encode() routine of object that Encode and
     * Decode themselves rather than using the standard encoder.

     * @return The generated key used (an instance of class CryptoHash).

     */

    /**

     * Stores an object into the Repository using the cryptohash of
     * the data as a key.  It returns that key, an instance of
     * CryptoHash. It will throw a RepositoryAccessException if the
     * Repository is not writable.

     * @param object The object to save in the Repository using an
     * automatically generated key.

     * @return The generated hash key used (an instance of class CryptoHash).

     */

    public CryptoHash putHash(Object object, PEHashtable parimeters)  throws RepositoryAccessException {
        if (! isWritable())
            throw new RepositoryAccessException("Attempt to put object " + object +
                                                " into closed or read-only Repository");
        
        try {
            CryptoHash result = frontMostFile.putHash(object,parimeters);
            Object repositoryHandle = frontMostFile.myKeys.get(result); // Retrieve handle
            myKeys.put(result,repositoryHandle); // Keep own hashtable in sync
            return result;
        } catch (IOException e) {
            throw new RepositoryAccessException("Repository putHash of " + object +
                                                " caused IOException: " + e);
        }
    }

    /**

     * Return the RepositoryHandle for a given key if one exists in
     * the Repository keys hashtable.

     * @param key A key to search for, any non-null object but most
     * often a String or cryptohash object.

     * @return true if record exists and false otherwise<p>

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
        if (handle == null || handle.size() <= 0) return null;
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

    /**

     * Private utility function

     */

    private Object unserialize(byte[] dataAsBytes) throws IOException {
        /* REPTIMESTART */        
        long startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */
        ByteArrayInputStream byteStream = new ByteArrayInputStream(dataAsBytes);
        Unserializer unserializer = Unserializer.make(byteStream,myMaker);
        /* REPTIMESTART */
        setupTimer += NativeSteward.deltaTimerUSec(startTime);
        startTime = NativeSteward.queryTimer();
        /* REPTIMEEND */
        Object result = unserializer.decodeGraph();
        /* REPTIMESTART */
        unsTimer += NativeSteward.deltaTimerUSec(startTime);
        /* REPTIMEEND */
        return result;
    }

    private Object unserialize(byte[] dataAsBytes, Hashtable parimeters) throws IOException {
        /* REPTIMESTART         
           long startTime = NativeSteward.queryTimer();
           REPTIMEEND */
        ByteArrayInputStream byteStream = new ByteArrayInputStream(dataAsBytes);
        Unserializer unserializer = ParamUnserializer.make(byteStream, myMaker, parimeters);
        /* REPTIMESTART 
           long endTime = NativeSteward.queryTimer();
           setupTimer += NativeSteward.deltaTimerUSec(startTime,endTime);
           startTime = NativeSteward.queryTimer();
           REPTIMEEND */
        Object result = unserializer.decodeGraph();
        /* REPTIMESTART 
           endTime = NativeSteward.queryTimer();
           unsTimer += NativeSteward.deltaTimerUSec(startTime,endTime);
           REPTIMEEND */
        return result;
    }

    public Object getCryptoHash(Object symbol)
        throws RepositoryKeyNotFoundException {
            return mySymbolTable.get(symbol);
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

     * The parimeters argument (a Hashtable),
     * if given, is used when decoding the object. If the object in
     * the Repository requires a parameter token/value pair (to be
     * substituted for an object that was pruned when encoding the
     * object) but either no parimeters collection was given, or it
     * does not contain an entry for the parameter, then a
     * RepositoryNeedParameterException is thrown. <p>

     * If the object could not be decoded for any other reason, then a
     * RepositoryAccessException is thrown.

     * @param key notNull - A key to search for, any non-null object but most
     * often a String.

     * @param parimeters nullOK - A PEHashtable collection
     * of token objects (typically Strings) as keys and Objects as
     * values. When decoding the object, any parameter token that is
     * encountered in the Repository is looked up in the parimeters
     * collection and gets replaced with the value found there.<p>

     * @return The retrieved object. You may need to cast the return
     * object to your target data type. Never returns null.

     */

    public Object get(Object key, Hashtable parimeters) throws
      RepositoryAccessException,
      RepositoryNeedParameterException,
      RepositoryKeyNotFoundException,
      IOException {
          /* REPTIMESTART */
          long startTime = NativeSteward.queryTimer();
          /* REPTIMEEND */
          RepositoryHandle handle = (RepositoryHandle)myKeys.get(key);
          /* REPTIMESTART */
          hashTimer += NativeSteward.deltaTimerUSec(startTime);
          /* REPTIMEEND */
          if (handle == null) {
              throw new RepositoryKeyNotFoundException
                ("Key " + key + " not found in Repository" + this);
          }
          try {
              return handle.getObject(key,parimeters);
          } catch (RepositoryAccessException rae) {
              throw new RepositoryAccessException(rae.getMessage() + ", key=" + key);
          } catch (RepositoryNeedParameterException rnpe) {
              throw new RepositoryNeedParameterException(rnpe.getMessage() + ", key=" + key);
          }
    }

    /**
 
     * Collect all objects that match a cetain key from all
     * RepositoryFiles into a single Vector.

     */

    public Vector getAll(Object key, Hashtable parimeters) {
        Vector result = new Vector();
        getAll(key,parimeters,result);
        return result;
    }

    public void getAll(Object key, Hashtable parimeters, Vector result) {
        Vector repFiles = repositoryFiles;
        if (repFiles == null) { // No vector, only one file. Fake it.
            repFiles = new Vector(2);
            repFiles.addElement(frontMostFile); // This shouldn't be necessary.
            repFiles.addElement(frontMostFile);
        }

        int nrFiles = repFiles.size(); // Number of files we have;
        int i;

        for (i = 1; i < nrFiles; i++) { // ignore the first file, it has been checked
            RepositoryFile repFile = (RepositoryFile)(repFiles.elementAt(i));
            RepositoryHandle h = (RepositoryHandle)repFile.myKeys.get(key);
            if (h != null) {
                try {
                    Object o = h.getObject(key,parimeters);
                    if (o != null) result.addElement(o);
                } catch (IOException iox) {
                    throw new NestedException("IO error retrieveing " + key, iox);
                }
            }
        }
    }

    // XXX This is defined in Curator at the moment. Move it here.
    // private static final String GROUP_TABLE = "%GroupTable%";

    public Vector getKeywordSet(Object keyword, Hashtable parimeters) {
        return StandardRepository.mergeKeywordVectors
          (getAll("%GroupTable%", parimeters), keyword);
    }

    /**
 
     * Determine the existence of a record with a given key in the
     * repositoryFiles vector starting with index 1. This method is
     * only called from delete() - If you need to use this then you
     * are likely doing something wrong.

     */

    private boolean deepSearch(Object key) {
        int nrFiles = repositoryFiles.size(); // Number of files we have
        int i;

        for (i = 1; i < nrFiles; i++) { // ignore the first file, it has been checked
            RepositoryFile repFile = (RepositoryFile)(repositoryFiles.elementAt(i));
            if (repFile.myKeys.get(key) != null) return true;
        }
        return false;
    }

    /**

     * Deletes a record (key and its value) from the Repository.

     * Adds a voiding record to frontMostFile if all entries in path
     * could not be deleted (since they may be in a read-only
     * repository file)

     * @param key notNull - A key object identifying the record to be deleted.

     * @return true if something was successfully deleted and false
     * otherwise.

     */

    public boolean delete(Object key) throws RepositoryAccessException {
        boolean result = false;  // True if we deleted something
        if (! isWritable())
            throw new RepositoryAccessException("Attempt to delete record using key " + key +
                                                " from closed or read-only Repository");
        
        try {
            result = frontMostFile.deleteIfExists(key);
            myKeys.remove(key); // Update our hashtable - forget the handle.

            // We don't know whether we need a voiding record unless we look for the key
            // in all the remaining repository files using a deep search.

            if (repositoryFiles == null) return result; // handle the easy case first.
            if (deepSearch(key)) { // Do the deep analysis
                frontMostFile.voidRecord(key); // Void old record or write a fresh voiding record.
                result = true;
            }
        } catch (IOException e) {
            throw new RepositoryAccessException("Repository delete caused IOException: " + e);
        }
        return result;
    }

    /**

     * Return the total number of records in this entire
     * repository. This is exactly the number of records in our
     * shallow binding key hashtable

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
        if (repositoryFiles != null) {
            int nrFiles = repositoryFiles.size(); // Number of files we have
            for (int i = 0; i < nrFiles; i++) {
                try {
                    ((RepositoryFile)repositoryFiles.elementAt(i)).analyzeFile();
                } catch (Exception e) {
                    lastException = e;
                    EStdio.err().println("File analysis caught an exception: " + e);
                }
            }
        } else {
            try {
                if (frontMostFile != null) {
                    frontMostFile.analyzeFile();
                }
            } catch (Exception e) {
                EStdio.err().println("File analysis caught an exception: " + e);
            }
        }
    }

    /**

     * Attempt to repair damage in all repositoryfiles.  For
     * non-writable files we will just update the myKeys hashtable.
     * If the file is writable we will also repair the file by
     * rewriting the reconstructed keys hashtable to it. <p>
     
     * We catch and save exceptions so that we will at least try to
     * repair all files. If one or more exceptions were caught, we
     * re-throw the last one when we're done. <p>

     * @args report - is given as true, then we will report all
     * inconsistencies and some progress info to EStdio.err(). <p>

     */

    public void repairFiles(boolean report) throws IOException {
        IOException lastException = null;
        if (repositoryFiles != null) {
            int nrFiles = repositoryFiles.size(); // Number of files we have
            for (int i = 0; i < nrFiles; i++) {
                try {
                    ((RepositoryFile)repositoryFiles.elementAt(i)).repairFile(true,report);
                } catch (IOException e) {
                    lastException = e;
                    if (report) {
                        EStdio.err().println("File repair caught an exception: " + e);
                    }
                }
            }
        } else {
            try {
                if (frontMostFile != null) {
                    frontMostFile.repairFile(true,report);
                }
            } catch (IOException e) {
                lastException = e;
                if (report) {
                    EStdio.err().println("File repair caught an exception: " + e);
                }
            }
        }
        if (lastException != null) throw lastException;
        return;
    }

    /**

     * Collect all key entries from the individual repositoryfiles
     * into one master key hashtable. This is called a "shallow
     * binding" hashtable since it resembles the shallow binding
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

    private Hashtable collectAllEntries() { 
        if (repositoryFiles != null) {
            return collectAllEntries(repositoryFiles);
        }
        return (Hashtable)frontMostFile.myKeys.clone();
    }

    /**
 
     * Create and return one single Hashtable that contains all the
     * key entries for all hashtables for all files in the given
     * Vector

     * Also merge all entries in all repositoryfiles that have the key
     * "%SymbolTable%" that are hashtables and put the result of that
     * merge (a large hashtable) into the resulting key table.

     * @param repFiles notNull - a Vector of RepositoryFiles.

     */

    private Hashtable collectAllEntries(Vector repFiles) {
        int nrFiles = repFiles.size(); // Number of files we have
        int recordCount = 0;    // Number of records in all files
        int i;
        Vector symbolTables = new Vector(repFiles.size());
        int symbolTableSize = 0;
        Hashtable bigSymbolTable = null;
        RepositoryFile rf;

        for (i = 0; i < nrFiles; i++) {
            rf = (RepositoryFile)repFiles.elementAt(i);
            if (rf == null) continue;
            recordCount += rf.myKeys.size();
            try {
                Object symTab =((RepositoryFile)repFiles.elementAt(i)).get("%SymbolTable%");
                if (symTab != null) {
                    if (symTab instanceof Hashtable) {
                        symbolTables.addElement(symTab);
                        symbolTableSize += ((Hashtable)symTab).size();
                        EStdio.out().println("Found symboltable, size is " +
                                           ((Hashtable)symTab).size());
                    } else symbolTables.addElement(null);
                }
            } catch (IOException iox) {
                symbolTables.addElement(null);
            } catch (RepositoryKeyNotFoundException rknfx) {
                symbolTables.addElement(null);
            }
        }

        Hashtable result = new Hashtable(recordCount); // Possibly an overestimate
        if (symbolTableSize > 0) bigSymbolTable = new Hashtable(symbolTableSize + 100);

        // We must handle shadowing here. Work backwards.  That way
        // entries early on in the repFiles vector will overwrite
        // entries from later hashtables, mirroring the key shadowing
        // behavior.

        for (i = nrFiles - 1; i >= 0; i--) { // Go through each file in vector
            RepositoryFile repFile = (RepositoryFile)(repFiles.elementAt(i));
            if (repFile == null) continue;
            Enumeration keysEnumeration = repFile.myKeys.keys(); // Get the keys from this file

            while (keysEnumeration.hasMoreElements()) { // Go through them
                Object key = keysEnumeration.nextElement();
                result.put(key,repFile.myKeys.get(key)); // and collect records to result
            }

            // Handle symbol table also. Once this code works,
            // generalize it to allow any symbol to e merged, as long
            // as the value is a hashtable.

            Hashtable fileSymbols = (Hashtable)symbolTables.elementAt(i);
            if (fileSymbols != null) {
                Enumeration symbolEnumeration = fileSymbols.keys();
                EStdio.out().println("Collecting " + fileSymbols.size() +
                                   " symbols from %SymbolTable%");
                while (symbolEnumeration.hasMoreElements()) { // Go through them
                    Object key = symbolEnumeration.nextElement();
                    bigSymbolTable.put(key,fileSymbols.get(key)); // and collect values
                }
            }
        }

        // We add the symboltable as a piece of direct data to the key
        // table. The RepositoryDirectDataHandle subclass was created
        // specifically to handle this case.

        EStdio.out().println("Repository master symbol table has " +
                           bigSymbolTable.size() + " entries");
        result.put("%SymbolTable%",new RepositoryDirectDataHandle(bigSymbolTable));
        mySymbolTable = bigSymbolTable;
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


    public static CryptoHash computeCryptoHash(Object data, OpenerRecipe recipe)
        throws RepositoryAccessException {
            if (data == null) {     // Special case this since hashtables cannot handle it.
                return CryptoHash.nullCryptoHash();
            }
            ByteArrayOutputStream dataByteStream = new ByteArrayOutputStream(10000);
            try {
                Serializer dataSerializer = Serializer.make(dataByteStream, recipe);
                dataSerializer.encodeGraph(data);
            } catch (IOException e) {
                throw new RepositoryAccessException("Repository computeCryptoHash of " + data +
                    " caused IOException: " + e);
            }
            return new CryptoHash(dataByteStream.toByteArray());
    }

    /**

     * Compute a cryptohash for an object using our all-powerful
     * RootClassRecipe Opener Recipe.  It's OK to have a static
     * function do this, since it conveys no authority.

     */

    public static CryptoHash computeCryptoHash(Object data) {
        return computeCryptoHash(data,myMaker);
    }

    /**

     * An OpenerRecipe suitable for a Repository.

     */

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

     * Given a hashtable of files and directories, return a vector
     * consisting only of the names of files in the hashtable (as
     * Strings), sorted by name.

     */

    private Vector sortNamesOfFilesOnly(ReadOnlyHashtable unsorted) {
        Enumeration dirObjects = unsorted.keys();
        Vector filenamesOnly = new Vector(unsorted.size());

        while (dirObjects.hasMoreElements()) {
            Object name = dirObjects.nextElement();
            Object value = unsorted.get(name);
            if (value instanceof EReadableFile || value instanceof EEditableFile) {
                filenamesOnly.addElement(name);
            }
        }
        if (filenamesOnly.size() == 0) return null;
        return ec.util.SortUtilities.quickSortStringVector(filenamesOnly);
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

    private static Vector addRepFilesInOrder(Vector fileObjects, EDirectoryEntry firstFile)
         throws RepositoryAccessException {
             int nrFiles = 0;
             Vector result = null;
             try {
                 if (fileObjects != null) nrFiles += fileObjects.size();
                 if (firstFile != null) nrFiles += 1;
                 if (nrFiles == 0) return null; // Nothing there, might as well quit now.
                 result = new Vector(nrFiles);

                 if (firstFile != null) {
                     if (firstFile instanceof EEditableFile) 
                         result.addElement(new RepositoryFile(((EEditableFile)firstFile).editor(), true)); // Writable!
                     else
                         result.addElement(new RepositoryFile(((EReadableFile)firstFile).reader(),false)); // Not so
                 }
                 Enumeration e = fileObjects.elements();

                 while (e.hasMoreElements()) {
                     Object object = e.nextElement();
                     if (object == firstFile) continue; // Don't open firstfile twice
                     if (object instanceof EEditableFile) {
                         result.addElement(new RepositoryFile(((EEditableFile)object).asReadableFile().reader(),false));
                     } else if (object instanceof EReadableFile) {
                         result.addElement(new RepositoryFile(((EReadableFile)object).reader(),false));
                     } else EStdio.err().println("Repository found an UFO (Unknown File Object) " + object);
                 }
             } catch (IOException iox) {
                 throw new RepositoryAccessException("RepositoryFile creation caused IOException: " + iox);
             }
             if (result == null || result.size() == 0) return null;
             return result;
    }
}

/**

 * Our class recipe - used to encode/decode all objects to the file.
 * XXX We need to independently verify that this does the right thing.
 * XXX Is the recipe the place to add Perimeter Objects Tables and
 * (possibly) decoding properties tables??

 */

class RepositoryClassRecipe extends ClassRecipe {

    static private final Class THROWABLE_TYPE = new Throwable().getClass();

    private ClassRecipe myWrapped;

    /**

     * The new Recipe subsets the authority of 'wrapped' by
     * disallowing instance variables known to be fragile.
     *
     * @param wrapped Where we delegate requests we approve 

     */

    public RepositoryClassRecipe(ClassRecipe wrapped) {
        myWrapped = wrapped;
    }

    /**

     * XXX Should filter out CREW classes.

     */

    public Class forSignature(String sig) {
        return myWrapped.forSignature(sig);
    }

    /**
     * Filters out instance variables known to be fragile.  These will
     * be skipped in encoding to a Repository, and will come back as
     * null or zero when reviving.  The current list is: <p>
     *
     * Tether.myHeld, Throwable.backtrace
     */

    public void approve(FieldKnife knife) {
        if ((knife.baseType() == Tether.TYPE)
            && knife.name().equals("myHeld")) {
            return;
        }
        if ((knife.baseType() == THROWABLE_TYPE)
            && knife.name().equals("backtrace")) {
            return;
        }
        myWrapped.approve(knife);
    }
}
