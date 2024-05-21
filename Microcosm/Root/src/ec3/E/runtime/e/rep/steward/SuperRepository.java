package ec.e.rep;

import ec.trace.Trace;
import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Dictionary;
import java.util.Properties;
import java.util.Vector;

import ec.cert.CryptoHash;

import ec.e.hold.DataHolder;
import ec.e.hold.DataHolderSteward;
import ec.e.hold.DataRequestor;
import ec.e.hold.Fulfiller;
import ec.e.hold.RemoteRetriever;
import ec.e.rep.RepositoryKeyNotFoundException;
import ec.e.rep.SimpleRepository;
import ec.e.run.CrewCapabilities;
import ec.util.NestedException;
import ec.tables.PEHashtable;

import ec.util.NativeSteward;

/**

 * The SuperRepository is a kind of umbrella Repository that
 * aggregates several other Repositories with different
 * characterisitics and uses and Implements a SuperRepository policy,
 * to be used globally as a fundamental service to anyone in or out of
 * vat.

 * We keep one extras Repository, one release/distribution Repository,
 * one Publish repository, one Cache repository, and one CDROM
 * repository and search these in some order (see below for
 * details). A separate Repository named Cert manages certificates and
 * is accessed through a separate API. The policy dictates default
 * names for most of these (ECHome/Extras, ECHome/Dist,
 * ECHome/Publish, ECHome/Cache and ECHome/Cert) but you can override
 * these names by using environment variables named Extras, Dist,
 * Publish, and Cache. The CDROM repository does not have a default
 * directory - there can only be one Repository file there (in fact,
 * the CDROM symbol has a value that is a file, not a directory), it
 * is read-only, and it is assumed to be slower than the other
 * Repositories. It is consulted last, i.e. symbols in it will be
 * overridden by symbols anywhere else. <p>

 * Note that you *must* define ECHome (preferred value: "ECHome") to
 * get a SuperRepository at all. This is to allow test programs to
 * start without complaints if they don't need a Repository. <p>

 * The Cert repository contains Certificates only and has its own
 * API. It is not included in the regular search mechanism.  The Cert
 * Repository uses ECHome/Cert directory and can be redirected using
 * the Cert environment variable. <p>

 * The SuperRepository constructor gets called every time the system
 * is started, like at system startup time or when reviving from a
 * checkpoint. The SuperRepository is made globally available to CREW
 * classes through the class CrewRepository and its get() method.<p>

 * The SuperRepository (in CREW) can be accessed from the Vat using
 * several different classes that provide different access
 * capabilities:
 * <ul>

 * <li><b>StandardRepository</b> provides read-only access and no
 * parimeterization.

 * <li><b>ParimeterizedRepository</b> provides read-only access with
 * parimeterization. ParimeterRepository is normally hidden from vat
 * denizens by being encapsulated in Fulfillers which are accessed
 * only in controlled ways from DataHolders. Therefore the
 * parimeterization functionality is not given out freely to vat
 * denizens.

 * <li><b>PublishRepository</b> allows the capability holder to
 * publish any object to anywone who needs it.

 * <li><b>CacheRepository</b> is the capability to add an object to
 * the local Cache. This is not currently given out to vat denizens -
 * it is just held by the network downloading code to store downloaded
 * results in the local cache.

 *</ul>

 * Anything that gets published or stored in the cache will
 * immediatley become part of the regular Repository key lookup so
 * anything that is added to one of these can immediately be retrieved
 * using the various SuperRepository get() methods.

 * If you need a Repository to contain capability-giving objects or if
 * you want a Repository you can write to at runtime (besides the
 * PublishRepository), then you should open a regular but distinct
 * Repository for your own purposes - Do not attempt to use the
 * SuperRepository. To open a Repository you need write access to a
 * file or directory to put the Repository in, but that's all you need.<p>

 * The search order for the SuperRepository is not cast in concrete
 * but the following seems to work as well as anything else:

 * First we search Extras so patches (if we ever provide any) and
 * speedups (if user has downloaded any) always get used. These are
 * all under user control.  Next we search the distribution disk. This
 * means that everything that's normally in the distribution cannot be
 * overridden by anything except Extras.  Then we search Publish
 * because this is still under user control and should yield the same
 * result on two machines in a publish-import relationship. Then we
 * search CDROM, since it's the slowest if we actually have to get the data, 
 * and finally we search the Cache.

 * The philosophy is that nothing that is provided as part of the
 * system in Dist can be overridden, and that the same objects should
 * be found by a publishing and an importing party. Also, the Cache
 * has to be searched last so that entries in the Cache that duplicate
 * entries elsewhere (i.e. on the CD-ROM) won't get stuck in the
 * Cache.

 */

public class SuperRepository implements SimpleRepository, RemoteRetriever {

    private static Trace tr_timer = new Trace("StartupTimer"); 

    private static SuperRepository theOne         = null;
    private Properties myProps                    = null;
    private Repository myExtras                   = null;
    private Repository myDist                     = null;
    private RuntimeExtensibleRepository myPublish = null;
    private Repository myCDROM                    = null;
    private CacheRepository myCache               = null;
    private RuntimeExtensibleRepository myCert    = null;

    private RemoteRetriever myRemoteDownloader = null;

    private static String DEFAULT_HOMEPATH    = "ECHome";

    private static String DEFAULT_EXTRASPATH  = "Extras";
    private static String DEFAULT_DISTPATH    = "Dist";
    private static String DEFAULT_PUBLISHPATH = "Publish";
    private static String DEFAULT_CACHEPATH   = "Cache";
    private static String DEFAULT_CERTPATH    = "Cert";

    public static final String SYMBOL_TABLE   = "%SymbolTable%";
    public static final String KEYWORDS_TABLE = "%Keywords%";
    public static final String GROUP_TABLE    = "%GroupTable%";

    private Dictionary extrasSymbols = null;
    private Dictionary distSymbols = null;
    private Dictionary publishSymbols = null;
    private Dictionary cdromSymbols = null;

    protected long maxCacheSizeInBytes = 50 * 1024 * 1024; // 50 MB

    // This may not be a good idea later but for now we want to make sure we only have one.
    private static boolean issued = false;

    // private Dictionary myRepCacheHash = RepositoryFile.makeDictionary(1000); // XXX Experimental
    private long cacheHits = 0;
    private long cacheMisses = 0;

    /**

     * Construct a (the) SuperRepository

     * @exception IOException
     * @exception ClassNotFoundException
     * @exception IllegalAccessException
     * @exception InstantiationException
     * @exception OnceOnlyException

     */

    private SuperRepository(Properties props) throws IOException,
           ClassNotFoundException, IllegalAccessException, InstantiationException, OnceOnlyException 
    {
        if (issued) throw new OnceOnlyException("SuperRepository already issued");
        myProps = props;
        long stime = 0;
        if (tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Initing superRepository ..."); 
            stime = NativeSteward.queryTimer();
        }
        initialize();
        if (tr_timer.debug && Trace.ON) {
            tr_timer.debugm("   !!Done initing superRepository: "+
                            NativeSteward.deltaTimerMSec(stime) + " mSec");
        }

        issued = true;   // Set this last - if we throw, we don't set it.

        // register bytearray protocol with URL to read gifs and jpegs
        //               URL.setURLStreamHandlerFactory(new ByteArrayFactory());
    }

    public static SuperRepository makeSuperRepository(Properties props)
    {
        if (theOne == null) {
            try {
                theOne = new SuperRepository(props);
            }
            catch (IOException e) {
                System.out.println(e);
            }
            catch (ClassNotFoundException e) {
                System.out.println(e);
            }
            catch (IllegalAccessException e) {
                System.out.println(e);
            }
            catch (InstantiationException e) {
                System.out.println(e);
            }
            catch (OnceOnlyException e) {
                System.out.println(e);
            }
        }

        return theOne;
    }

    public static SuperRepository summon() {
        return theOne;
    }

    /**

     * Open a file given an arbitrary path, and create all directories
     * leading down to that path as needed.

     * @exception IOException

     */

    private File ensureDirectory(String path, String dirName) throws IOException {
        File dir = new File(path, dirName);

        if (dir.exists()) {
            if (dir.isDirectory() && dir.canWrite()) {
                return dir;
            } 
            else {
                String msg = "Directory " + path + " is not writable";
                if (Trace.repository.debug && Trace.ON) {
                    Trace.repository.debugm(msg);
                }

                throw new IOException(msg);
            }
        }
        else {
            try {
                if (!dir.mkdir()) {
                    throw new IOException("Could not create directory: " + path);
                }
                else {
                    return dir;
                }
            }
            catch (SecurityException e) {
                throw new IOException("Could not create directory: " + path);
            }
        }
    }

    /**

     * Initialize SuperRepository.

     * @exception IOException
     * @exception ClassNotFoundException
     * @exception IllegalAccessException
     * @exception InstantiationException 

     */

    public void initialize() throws IOException, ClassNotFoundException,
           IllegalAccessException, InstantiationException {

               extrasSymbols = null;
               distSymbols = null;
               publishSymbols = null;
               cdromSymbols = null;

               String ecHomePath = (String)myProps.get("ECHome");
               if (ecHomePath == null)
                   throw new IllegalAccessException("Environment variable 'ECHome' must be defined to use a SuperRepository");

               String extrasPath = (String)myProps.get("Extras"); // additions, downloaded speedups
               String distPath = (String)myProps.get("Dist");     // EC distribution files
               String publishPath = (String)myProps.get("Publish"); // Objects we are exporting
               String cachePath = (String)myProps.get("Cache");   // Cache files
               String cdromPath = (String)myProps.get("CDROM");     // CD-ROM dir. May be null.

               String certPath = (String)myProps.get("Cert");     // Certificate files.

               // Set max repository cache size.

               String repositoryCacheSize = (String)myProps.get("RepositoryCacheSize");
               if (repositoryCacheSize != null) {
                   long multiplier = 1;
                   repositoryCacheSize = repositoryCacheSize.trim().toLowerCase();
                   if (repositoryCacheSize.endsWith("m") ||
                       repositoryCacheSize.endsWith("mb")) {
                       multiplier = 1024 * 1024;
                   } else if (repositoryCacheSize.endsWith("k") ||
                              repositoryCacheSize.endsWith("kb")) {
                       multiplier = 1024;
                   } else if (repositoryCacheSize.endsWith("g") ||
                              repositoryCacheSize.endsWith("gb")) {
                       multiplier = 1024 * 1024 * 1024;
                   }

                   long  number = maxCacheSizeInBytes;
                   try {
                       number = Long.parseLong(repositoryCacheSize);
                       maxCacheSizeInBytes = number * multiplier;
                       if (Trace.repository.debug && Trace.ON) {
                           Trace.repository.debugm
                             (" Max Repository Cache size is " + maxCacheSizeInBytes);
                       }
                   } catch (NumberFormatException nfe) {
                       if (Trace.repository.debug && Trace.ON) {
                           Trace.repository.debugm
                             ("Property for max Repository Cache size is not a valid number:" +
                              repositoryCacheSize);
                       }
                   }
               }

               // XXX These paths should be absolute, if given. Here they aren't since we are
               // still having problems opening deep paths. Once we can do that, fix this.

               if (extrasPath == null) extrasPath = DEFAULT_EXTRASPATH;
               if (distPath == null) distPath = DEFAULT_DISTPATH;
               if (publishPath == null) publishPath = DEFAULT_PUBLISHPATH;
               if (cachePath == null) cachePath = DEFAULT_CACHEPATH;
               if (certPath == null) certPath = DEFAULT_CERTPATH;
        
               // The ECHome directory and ECHome/Dist directory must both exist or we throw.
               // The calls to lookupDirectory may well throw IOExceptions out of here.
               // distDir is read-only.

               // We will want to write to these directories:

               File publishDir = ensureDirectory(ecHomePath, publishPath);
               File cacheDir   = ensureDirectory(ecHomePath, cachePath);
               File certDir    = ensureDirectory(ecHomePath, certPath);

               // These are read-only:

               File distDir    = ensureDirectory(ecHomePath, distPath);
               File extrasDir  = ensureDirectory(ecHomePath, extrasPath);
               File cdRomDir = null;
               if (cdromPath != null)
                   cdRomDir = ensureDirectory(ecHomePath, cdromPath);

               int nrRepositories = 0; // Count Repositories we open successfully. Need at least one.

               try {
                   myExtras = new Repository(extrasDir, null);
                   if (myExtras != null) {
                       extrasSymbols = (Dictionary)myExtras.maybeGet(SYMBOL_TABLE);
                       nrRepositories++;
                   }
               } catch (IOException eex) {
                   myExtras = null;
                   if (Trace.repository.debug && Trace.ON) {
                       Trace.repository.debugm
                         ("IO Exception " + eex.getMessage() +
                          " ocurred when trying to open Extras Repository in directory " +
                          ecHomePath + "/" + extrasPath + " : " + eex.getMessage());
                   }
               }

               try {
                   myDist = new Repository(distDir, null);
                   if (myDist != null) {
                       distSymbols = (Dictionary)myDist.maybeGet(SYMBOL_TABLE);
                       nrRepositories++;
                   }
               } catch (IOException eex) {
                   myDist = null;
                   if (Trace.repository.debug && Trace.ON) {
                       Trace.repository.debugm
                         ("IO Exception " + eex.getMessage() + 
                          " ocurred when attempting to open Dist Repository in directory " +
                          ecHomePath + "/" + distPath + " : " + eex.getMessage());
                   }
               }

               try {
                   myPublish = new RuntimeExtensibleRepository(publishDir, 'P');
                   if (myPublish != null) {
                       publishSymbols = (Dictionary)myPublish.maybeGet(SYMBOL_TABLE);
                       nrRepositories++;
                   }
               } catch (IOException eex) {
                   myPublish = null;
                   if (Trace.repository.debug && Trace.ON) {
                       Trace.repository.debugm
                         ("IO Exception " + eex.getMessage() + 
                          " ocurred when attempting to open Publish Repository in directory " +
                          ecHomePath + "/" + publishPath + " : " + eex.getMessage());
                   }
               }

               if (cdromPath != null) {
                   try {
                       myCDROM = new Repository(cdRomDir, null);
                       if (myCDROM != null) {
                           cdromSymbols = (Dictionary)myCDROM.maybeGet(SYMBOL_TABLE);
                           nrRepositories++;
                       }
                   } catch (IOException eex) {
                       myCDROM = null;
                       if (Trace.repository.debug && Trace.ON) {
                           Trace.repository.debugm
                             ("IO Exception " + eex.getMessage() + 
                              " ocurred when attempting to open CDROM Repository dir in " +
                              ecHomePath + "/" + cdromPath + " : " + eex.getMessage());
                       }
                   }
               }

               try {
                   myCache = CacheRepository.makeCacheRepository(cacheDir, maxCacheSizeInBytes);
                   if (myCache != null) {
                       // CacheRepositories don't need symboltables.
                   }
               } catch (IOException eex) {
                   myCache = null;
                   if (Trace.repository.debug && Trace.ON) {
                       Trace.repository.debugm
                         ("IO Exception " + eex.getMessage() +  
                          " ocurred when attempting to open Cache Repository in directory " +
                          ecHomePath + "/" + cachePath + " : " + eex.getMessage());
                   }
               }

               try {
                   myCert = new RuntimeExtensibleRepository(certDir,'C');
                   if (myCert != null) {
                       // CertRepositories don't need symboltables.
                       nrRepositories++;
                   }
               } catch (IOException eex) {
                   myCert = null;
                   if (Trace.repository.debug && Trace.ON) {
                       Trace.repository.debugm
                         ("IO Exception " + eex.getMessage() + 
                          " ocurred when attempting to open Cert Repository in directory " +
                          ecHomePath + "/" + certPath + " : " + eex.getMessage());
                   }
               }

               // XXX Remove before Flight - i.e. remove comment brackets below.
               // This should be an error, since we shouldn't be able to run without Repositories.

               /*
                 if (nrRepositories == 0) {
                 throw new RuntimeException("Need at least one Repository file somewhere");
                 }
                 */

               if (Trace.repository.debug && Trace.ON) {
                   String msg = "Repository initialization summary:\n";
                   if (myExtras == null) msg += "No Extras repository\n";
                   else {
                       msg += "Extras repository contains " + myExtras.size() +
                         " entries in " + myExtras.numberFiles() + " files\n";
                   }
                   if (myDist == null) msg += "No Dist repository\n";
                   else {
                       msg += "Dist repository contains " + myDist.size() +
                         " entries in " + myDist.numberFiles() + " files\n";
                   }
                   if (myPublish == null) msg += "No Publish repository\n";
                   else {
                       msg += "Publish repository contains " + myPublish.size() +
                         " entries in " + myPublish.numberFiles() + " files\n";
                   }
                   if (myCDROM == null) msg += "No CDRom repository\n";
                   else {
                       msg += "CDRom repository contains " + myCDROM.size() +
                         " entries in " + myCDROM.numberFiles() + " files\n";
                   }
                   if (myCache == null) msg += "No Cache repository\n";
                   else {
                       msg += "Cache repository contains " + myCache.size() +
                         " entries in " + myCache.numberFiles() + " files\n";
                   }
                   if (myCert == null) msg += "No Cert repository\n";
                   else {
                       msg += "Cert repository contains " + myCert.size() +
                         " entries in " + myCert.numberFiles() + " files\n";
                   }
                   //    private RemoteRetriever myRemoteDownloader = null;
                   Trace.repository.debugm(msg);
               }
    }

    /**

     * Return a CryptoHash object (or other key, though that should be
     * rare) for a given key from any of our Repositories. Never
     * throws, just returns null if key cannot be found.

     */

    public Object getCryptoHash(Object symbol) {
        Object result;
        if (extrasSymbols != null) {
            result = extrasSymbols.get(symbol);
            if (result != null) return (CryptoHash)result;
        }
        if (distSymbols != null) {
            result = distSymbols.get(symbol);
            if (result != null) return (CryptoHash)result;
        }

        if (publishSymbols != null) {
            result = publishSymbols.get(symbol);
            if (result != null) return (CryptoHash)result;
        }

        if (cdromSymbols != null) {
            result = cdromSymbols.get(symbol);
            if (result != null) return (CryptoHash)result;
        }

        // Ignore CacheRepository - No symbols.

        return (CryptoHash)null;
    }

    /**

     * Look for a given key in all our repositories. Throw a
     * RepositoryKeyNotFoundException if we cannot find the key.

     * @exception IOException

     */

    public Object get(Object key) throws IOException {
        return get(key,null);
    }

    /**

     * Look for a given key in all our repositories. Throw a
     * RepositoryKeyNotFoundException if we cannot find the key.

     * @param parimeterArguments - a Hashtable which, if given, is used when
     * decoding the object.<p>

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
     * @exception RepositoryKeyNotFoundException (a subclass of IOException)
     * @exception IOException

     */

    public Object get(Object key, Hashtable parimeterArguments)
         throws RepositoryKeyNotFoundException, IOException {

        Object result;
        //        result = myRepCacheHash.get(key); // XXX Experimental
        //        if (result != null) {   // XXX Experimental
        //            System.out.println("SuperRepository cache hit for " + key.toString());
        //            cacheHits++;        // XXX Exerimental
        //            return result; // XXX Experimental
        //        } // XXX Experimental
        //        cacheMisses++;          // XXX Experimental
        //        System.out.println("SuperRepository cache miss for " + key.toString());

        RepositoryHandle handle = null;
        if (myExtras != null) {
            handle = myExtras.getHandle(key);
            if (handle != null) return handle.getObject(key,parimeterArguments);
        }
        if (myDist != null) {
            handle = myDist.getHandle(key);
            if (handle != null) return handle.getObject(key,parimeterArguments);
        }
        if (myPublish != null) {
            handle = myPublish.getHandle(key);
            if (handle != null) return handle.getObject(key,parimeterArguments);
        }

        // Note that the getHandle() call is as fast on the CD-ROM as
        // on any other device since the call only accesses the
        // in-memory Hashtable.

        if (myCDROM != null) {
            handle = myCDROM.getHandle(key);
            if (handle != null) return handle.getObject(key,parimeterArguments);
        }
        if (myCache != null) {
            handle= myCache.getHandle(key);
            if (handle != null) {

                // We call a special version of getObject in myCache
                // so that the CacheRepository can update its LRU info
                // as needed.

                if (Trace.repository.debug && Trace.ON) {
                    Trace.repository.debugm
                      ("Found " + key + " in CacheRepository file " +
                       handle.myRepositoryFile.myFileName);
                }
                return myCache.getObject(handle,key,parimeterArguments);
            }
        }
        throw new RepositoryKeyNotFoundException("Key " + key + " not found");
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
            } catch (RepositoryKeyNotFoundException rknfx) {
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
        return maybeGet(getCryptoHash(symbol));
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

     * @exception RepositoryKeyNotFoundException
     * @exception IOException

     */

    public Object getBySymbol(Object symbol) throws IOException { // Get object by symbolic name or throw
        return getBySymbol(symbol,null); // We don't have a parimeter table
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
     * @exception RepositoryKeyNotFoundException
     * @exception IOException

     */

    public Object getBySymbol(Object symbol, Hashtable parimeterArguments)
         throws IOException { // Get object by symbolic name or throw
             Object key =  getCryptoHash(symbol); // May throw RepositoryKeyNotFoundException
             return get(key,parimeterArguments); // May throw IOException
    }
        
    /**

     * Get all values for a given key from all repositoryfiles we
     * have.  This method overrides the shadowing effect and really
     * collects all values for the key.

     * @param symbol A symbol to be looked up in all symbol tables.
     */

    public Vector getAll(Object symbol) {
        return getAll(symbol,null);
    }

    /**

     * Get all values for a given key from all repositoryfiles we
     * have.  This method overrides the shadowing effect and really
     * collects all values for the key.

     * @param symbol A symbol to be looked up in all symbol tables.

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
     * @return A Vector of objects

     */

    public Vector getAll(Object symbol, Hashtable parimeterArguments) {
        Vector result = new Vector();
        if (myPublish != null) myPublish.getAll(symbol,parimeterArguments,result);
        if (myCache != null)   myCache.getAll(symbol,parimeterArguments,result);
        if (myExtras != null)  myExtras.getAll(symbol,parimeterArguments,result);
        if (myDist != null)    myDist.getAll(symbol,parimeterArguments,result);
        if (myCDROM != null)   myCDROM.getAll(symbol,parimeterArguments,result);
        return result;
    }

    // XXX This is defined in Curator at the moment. Move it here.
    // private static final String GROUP_TABLE = "%GroupTable%";
    
    /**

     * For all RepositoryFiles in the whole SuperRepository, find
     * all keyword tables and look for a given keyword.  Return a
     * Vector with the keys for all such entries, removing duplicates.
     * Note that this includes entries added by users to Extras
     * directory and the Publish directory.

     * @param keyword - A symbol (often a String) to search for.
     * @return A Vector containing all unique keys that had that keyword.

     */

    public Vector getKeywordSet(Object keyword) {
        return mergeKeywordVectors(getAll("%GroupTable%", null), keyword);
    }

    /**

     * Merge all vectors containing entries for a certain keyword
     * given a Vector of Hashtables of Vectors of keys hashed by
     * keyword, such as those generated by the Curator.

     */

    public static Vector mergeKeywordVectors(Vector keywordTables, Object keyword) {
        Vector keywordVectors = new Vector();

        for (int i = 0; i < keywordTables.size(); i++) {
            Object o = keywordTables.elementAt(i);
            if (o instanceof Dictionary) {
                Object v = ((Dictionary)o).get(keyword);
                if (v != null && v instanceof Vector)
                    keywordVectors.addElement(v);
            }
        }
        return mergeVectors(keywordVectors);
    }

    /**

     * Merge all non-null Objects in a Vector of Vectors into one
     * result vector, removing duplicates.<p>
     * XXX This one belongs in some util library.

     */

    public static Vector mergeVectors(Vector vectorVector) {
        if (vectorVector == null || vectorVector.size() == 0) return null;

        // Use a dictionary as a duplicate-removing intermediate bucket.

        Dictionary set = RepositoryFile.makeDictionary(100);
        for (int i = 0; i < vectorVector.size(); i++) {
            Vector v = (Vector)vectorVector.elementAt(i);
            for (int j = 0; j < v.size(); j++) {
                Object o = v.elementAt(j);
                // Vectors can have null elements. Hashtables cannot.
                if (o != null) set.put(o,o);
            }
        }

        if (set.size() == 0) return null;
        Vector result = new Vector(set.size());
        Enumeration e = set.keys();
        while (e.hasMoreElements()) result.addElement(e.nextElement());
        return result;
    }

    /**

     * Determine whether an object for a given key exists in the
     * PublishRepository.  Similar to exists(), except this
     * specifically tells you the object came from the
     * PublishRepository.

     */

    public boolean isPublished(Object key) {
        if (myPublish == null) return false;
        return myPublish.exists(key);
    }

    /**

     * Retrieve data from the PublishRepository. Note that we return
     * an array of bytes, the raw data, rather than decoding the data
     * into an object. We'd need to encode them again to go over the
     * wire and then decode them in the receiving end. So we just ship
     * the bytes. This also means that we don't care (in this end)
     * whether the data was stored using parimeterization or not.

     * @exception IOException
     * @exception RepositoryKeyNotFoundException

     */

    public byte[] getPublished(Object key) throws IOException , RepositoryKeyNotFoundException {
        if (myPublish == null) throw new IOException("Publish repository has not been initialized getPublished()");
        RepositoryHandle handle = myPublish.getHandle(key);
        if (handle == null)
            throw new RepositoryKeyNotFoundException("Key " + key +
                                                     " not found in publish repository");
        System.out.println("getPublished(" + key + ") returns data");
        return handle.getDataBytes(key);
    }

    /**

     * Save an object in the PublishRepository and return a CryptoHash key for it.

     * @exception IOException

     */

    public CryptoHash putHashInPublishRepository(Object object) throws IOException {
        if (myPublish == null) throw new IOException("Publish repository has not been initialized (puthash())");
        return (CryptoHash)myPublish.putHash(object);
    }

    /**

     * Save an object in the CacheRepository and return a CryptoHash key for it.

     * @exception IOException

     */

    public CryptoHash putHashInCacheRepository(Object object) throws IOException {
        System.out.println("putHashInCacheRepository(" + object + ")");
        if (myCache == null) throw new IOException("Cache repository has not been initialized");
        CryptoHash result = (CryptoHash)myCache.putHash(object);
        myCache.flush();        // Always flush cache after we put something in it.
        return result;
    }

    /**

     * Save an object in the PublishRepository and return a DataHolder for it.

     * @exception IOException

     */

    public DataHolder makeDataHolder(Object object,
                                     Fulfiller fulfiller,
                                     Hashtable certificates)
         throws IOException {
             return makeDataHolder(object,fulfiller,certificates,null,null,null);
    }

    /**

     * Save an object in the PublishRepository and return a DataHolder for it.
     * Also updates symbol and keywords arguments if necessary.

     * @exception IOException

     */


    public DataHolder makeDataHolder(Object object,
                                     Fulfiller fulfiller,
                                     Hashtable certificates,
                                     Object symbol,
                                     Vector keywords,
                                     PEHashtable parimeters)
         throws IOException {
             if (myPublish == null)
                 throw new IOException("Publish repository has not been initialized (makeDataHolder())");
             if (fulfiller == null) throw new IllegalArgumentException("Fulfiller is null");
             CryptoHash hash = myPublish.putHashBySymbol
               (object, symbol, keywords, parimeters);
             return new DataHolderSteward(hash,fulfiller,certificates);
    }

    /**

     * Return a certificate hashtable for a given key.  Returns null
     * if the key could not be found, but may throw an IOException.

     * @exception IOException

     */

    public Hashtable getCertificateHashtable(Object key) throws IOException {
        try {
            if (myCert != null) return (Hashtable)myCert.get(key, (Hashtable)null);
        } catch (IOException iox) {}
        return null;
    }

    /*
      
     * Forward requests for retrieval of data to our remote
     * downloader.  This method should possibly never be used and
     * should probably be removed.  We want all remote retrieval to
     * request byte arrays to save ourselves one encode and one
     * decode.


    public void requestRetrieval(CryptoHash hash, Hashtable parimeterArguments,
                                 Vector hints, DataRequestor requestor) {
        if (myRemoteDownloader == null)
            myRemoteDownloader = (RemoteRetriever) CrewCapabilities.getTheRemoteRetriever();
        myRemoteDownloader.requestRetrieval(hash, parimeterArguments, hints, requestor);
    }
     */

    /**
      
     * Forward requests for retrieval of bytes (to decode later to
     * data) to our remote downloader.

     */

    public void requestByteRetrieval(CryptoHash hash, Vector hints,
                                     DataRequestor requestor)
         throws IOException {
             if (hints == null) {
                 requestor.handleFailure(new RepositoryKeyNotFoundException
                                         ("No data in repository for " + hash +
                                          " and no hints to specify where to get it"),
                                         requestor);
             } else {
                 if (myRemoteDownloader == null)
                     myRemoteDownloader =
                       (RemoteRetriever)CrewCapabilities.getTheRemoteRetriever();
                 myRemoteDownloader.requestByteRetrieval(hash, hints, requestor);
             }
    }

    /**

     * Flush those of our repositories that can be open for write.

     */

    public void flush() throws IOException {
        if (myPublish != null) myPublish.flush();
        if (myCache != null) myCache.flush();
        if (myCert != null) myCert.flush();
    }
}

