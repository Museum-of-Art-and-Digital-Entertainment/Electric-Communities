package ec.e.rep;

import ec.trace.Trace;
import ec.e.file.EStdio;
import ec.e.rep.SimpleRepository;
import ec.e.rep.SuperRepository;
import ec.e.rep.RepositoryTether;
import ec.cert.CryptoHash;
import ec.e.run.EEnvironment;
import ec.e.run.SmashedException;
import ec.e.hold.DataRequestor;
import ec.util.NestedException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.run.Tether;

/**

 * The ParimeterizedRepository is a variation of the
 * StandardRepository that implements additional methods to decode
 * objects using parimeters (perimeter object parameter tables).
 * In contrast to SuperRepository,

 * <ul>
 * <li>ParimeterizedRepository is self-repairing after a quake.
 * <li>ParimeterizedRepository is Read-Only.
 * <li>ParimeterizedRepository supports access to the CertificateRepository.
 * <li>ParimeterizedRepository supports downloading of data over the network.
 * </ul>

 * ParimeterizedRepository is intended to be used mostly inside of
 * Fulfillers to allow DataHolders to contain parimeterized data
 * without leaking the parameter objects to the rest of the vat.

 * Once you have a ParimeterizedRepository capability it's good
 * forever. It will work just as fine after a quake, since it's
 * self-repairing.

 */

public class ParimeterizedRepository extends StandardRepository {
    static public Object summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException {
             String echomePath = eEnv.getProperty("ECHome");
             if (echomePath == null) return null;
             return (ParimeterizedRepository)eEnv.magicPower
               ("ec.e.rep.ParimeterizedRepositoryMaker");
    }

    public ParimeterizedRepository() {} // Make a dummy for Curator etc.

    public ParimeterizedRepository(EEnvironment env) throws IOException {
        super(env);
    }

    /**

     * Initializes ParimeterizedRepository. Can be called anytime for any
     * reason, like when reviving from a quake. It does not need any
     * additional capabilities besides those it was handed in its
     * constructor.

     * @exception IOException
     * @exception ClassNotFoundException
     * @exception IllegalAccessException
     * @exception InstantiationException 

     */

    public void initialize() {
        if (Trace.repository.debug && Trace.ON) {
            Trace.repository.debugm("ParimeterizedRepository initialization for " + this);
        }
        try {
            myRepository = new RepositoryTether(myEnv.vat());
        } catch (Exception e) {
            String msg = "Could not initialize parimeterizedRepository";
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            throw new NestedException(msg, e);
        }
    }

    /**

     * Creates a StandardRepository.  Anyone that has access to a
     * ParimeterizedRepository should be able to subset this
     * capability to a StandardRepository. We return an Object since
     * other restricters of this kind return Objects so as not to
     * introduce various compile time dependencies between classes
     * that have no other dependencies.

     */

    public Object makeStandardRepository()
         throws ClassNotFoundException, IllegalAccessException, InstantiationException {
             return myEnv.magicPower("ec.e.rep.StandardRepositoryMaker");
    }
     
    /**

     * Retrieve an object stored under a given key.
     * Throws Exceptions that require declarations.<p>
     * Repairs this ParimeterizedRepository automatically after a quake.

     * @param key - Any object, but most often a CryptoHash instance.

     * @param parimeterArguments - A Hashtable containing the required
     * arguments for deparimeterization, which is the replacement of
     * parimeter object in the decoding object graph with locally
     * available objects.

     * @exception RepositoryNeedParameterException
     * @exception RepositoryKeyNotFoundException (a subclass of IOException)
     * @exception IOException

     */

    public Object get(Object key, Hashtable parimeterArguments) throws IOException {
        while (true) {
            try {
                return ((SuperRepository)(myRepository.held())).get(key,parimeterArguments);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("ParimeterizedRepository reopening after Quake (PG)");
                initialize();
            }
        }
    }

    /**

     * Return a CryptoHash object (or other key, though that should be
     * rare) for a given key from any of our Repositories. Never
     * throws, just returns null if key cannot be found.

     */

    public Object getCryptoHash(Object symbol) {
        while (true) {
            try {
                return (Object) ((SuperRepository)(myRepository.held())).getCryptoHash(symbol);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("ParimeterizedRepository reopening after Quake (GCH)");
                initialize();
            }
        }
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
     * available objects.

     * @return Requested Object

     * @exception RepositoryNeedParameterException
     * @exception RepositoryKeyNotFoundException (a subclass of IOException)
     * @exception IOException

     */

    public Object getBySymbol(Object symbol, Hashtable parimeterArguments) throws IOException {
        while (true) {
            try {
                return ((SuperRepository)(myRepository.held())).getBySymbol(symbol,parimeterArguments);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("ParimeterizedRepository reopening after Quake (GS)");
                initialize();
            }
        }
    }

    /**

     * Get all values for a given key from all repositoryfiles we
     * have.  This method overrides the shadowing effect and really
     * collects all values for the key.

     */

    public Vector getAll(Object key, Hashtable parimeterArguments) {
        while (true) {
            try {
                return (Vector) ((SuperRepository)(myRepository.held())).getAll(key,parimeterArguments);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("ParimeterizedRepository reopening after Quake (GA)");
                initialize();
            }
        }
    }

    /**

     * Returns a certificate hashtable for a given key.  Returns null
     * if the key could not be found, but may throw an IOException in
     * case of serious trouble and various instantion-related
     * exceptions if a post-quake revival fails for some reason.

     * @exception IOException

     */

    public Hashtable getCertificateHashtable(Object key) throws IOException {
        while (true) {
            try {
                return ((SuperRepository)myRepository.held()).getCertificateHashtable(key);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (GCHT)");
                initialize();
            }
        }
    }

    /**

     * Determines whether an object given a certain key (typically a
     * Cryptohash) has been published in the local Publish Repository.

     * @param key - Any object, but most often a CryptoHash instance.

     */

    public boolean isPublished(Object key) {
        while (true) {
            try {
                return ((SuperRepository)(myRepository.held())).isPublished(key);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("ParimeterizedRepository reopening after Quake (IP)");
                initialize();
            }
        }
    }

    /**

     * Requests that an attempt be made to find an object on the
     * network identified by a given CryptoHash and using the hints
     * (such as SturdyRefs and URL's and URL fragments and other hints
     * in yet to be determined formats). The object, if found, is
     * handed to a given DataRequestor (WITHOUT first being decoded to
     * an object) using a callback routine. The reason for letting the
     * requestor do its own decoding is that it might have parimeter
     * tables that it wants to use but does not want to hand them out
     * to anyone, and that the object may have come from a Repository
     * or even a web server with no parimeter environment
     * available. Besides, we save one decoding from the Repository
     * and one encoding to the wire in the server end.

     */

    public void requestByteRetrieval(CryptoHash hash, Vector myHints, DataRequestor requestor) 
         throws IOException {
             while (true) {
                 try {
                     ((SuperRepository)myRepository.held()).
                       requestByteRetrieval(hash, myHints, requestor);
                     return;
                 } catch (SmashedException smex) {
                     if (Trace.repository.debug && Trace.ON)
                         Trace.repository.debugm
                           ("StandardRepository reopening after Quake (RUR)");
                     initialize();
                 }
             }
    }

    /**

     * Save an object in the CacheRepository and return a CryptoHash
     * key for it.  <p>

     * XXX This is a late addition to the system and may not be in
     * line with other capabiity philosophies. Specifically, it allows
     * vat denizens to write to the disk - In a controlled fashion,
     * but still.<p>

     * @exception IOException

     */

    public CryptoHash putHashInCacheRepository(Object object) throws IOException {
        while (true) {
            try {
                return ((SuperRepository)myRepository.held()).putHashInCacheRepository(object);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (PHICR)");
                initialize();
            }
        }
    }
}
