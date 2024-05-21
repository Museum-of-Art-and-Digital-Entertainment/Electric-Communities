package ec.e.rep;

import ec.trace.Trace;
import ec.e.rep.SimpleRepository;
import ec.e.rep.SuperRepository;
import ec.e.rep.RepositoryTether;
import ec.e.run.EEnvironment;
import ec.e.run.SmashedException;
import ec.util.NestedException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.run.Tether;
import ec.e.file.EStdio;

/**

 * The StandardRepository is the most commonly used in-vat capability
 * to the SuperRepository, the main Repository access Policy Object, a
 * STEWARD. The StandardRepository differs from the SuperRepository in
 * several important ways:<p>

 * <ul>
 * <li>StandardRepository is self-repairing after a quake.
 * <li>StandardRepository is Read-Only.
 * <li>StandardRepository does not allow the use of parimeter tables
 * </ul>

 * The StandardRepository is supposed to only contain immutable
 * objects that convey no capabilities. To write to a
 * SuperRepository's (potentially) writable parts, the
 * PublishRepository and the Internet cache in CacheRepository (not
 * yet implemented), you will need a different capability.

 * You can give the StandardRepository as a capability to all in-vat
 * objects without security implications.<p>

 * If you need a Repository to contain capability-giving objects or if
 * you want a Repository you can write to at runtime, then you should
 * open a regular but distinct Repository for your own purposes - Do
 * not attempt to use the StandardRepository.

 * Once you have a StandardRepository capability it's good forever. It
 * will work just as fine after a quake, since it's self-repairing.

 */

public class StandardRepository implements SimpleRepository {
    /*package*/ RepositoryTether myRepository = null;
    /*package*/ EEnvironment myEnv = null;

    static public Object summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException {
             String echomePath = eEnv.getProperty("ECHome");
             if (echomePath == null) return null;
             return (StandardRepository)eEnv.magicPower
               ("ec.e.rep.StandardRepositoryMaker");
    }

    public StandardRepository() {} // Make a dummy for Curator etc.

    /**
     * Construct a (the) StandardRepository
     */

    public StandardRepository(EEnvironment env) {
            myEnv = env;
            initialize();
    }

    /**
     * Initialize StandardRepository. Can be called anytime for any
     * reason, like when reviving from a quake. It does not need any
     * additional capabilities besides those it was handed in its
     * constructor.
     */

    public void initialize() {
        if (Trace.repository.debug && Trace.ON)
            Trace.repository.debugm("StandardRepository initialization");
        try {
            myRepository = new RepositoryTether(myEnv.vat());
        } catch (Exception e) {
            EStdio.reportException(e);
            throw new NestedException("Could not initialize StandardRepository", e);
        }
    }

    /**
     * Return a CryptoHash object for a given key from
     * Repository. Never throws, just returns null if key cannot
     * be found. <p>

     * Repairs this StandardRepository automatically after a quake.
     */

    public Object getCryptoHash(Object symbol) {
        while (true) {
            try {
                return ((SimpleRepository)(myRepository.held())).getCryptoHash(symbol);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (GCH)");
                initialize();
            } catch (IOException iox) {
                return null;
            }
        }
    }

    /**
     * Retrieve an object stored under a given key.
     * Throws no exceptions that require declarations.<p>
     * Returns null if key could not be found or ir anything else went wrong.
     * Repairs this StandardRepository automatically after a quake.

     * @param key - Any object, but most often a CryptoHash instance.
     */

    public Object maybeGet(Object key) {
        while (true) {
            try {
                return ((SimpleRepository)(myRepository.held())).maybeGet(key);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (MG)");
                initialize();
            }
        }
    }

    /**

     * Retrieve an object stored under a given key.
     * Does not require a parimeter table.
     * Throws Exceptions that require declarations.<p>
     * Repairs this StandardRepository automatically after a quake.

     * @param key - Any object, but most often a CryptoHash instance.
     * @exception RepositoryKeyNotFoundException (a subclass of IOException)
     * @exception IOException

     */

    public Object get(Object key) throws IOException {
        while (true) {
            try {
                return ((SimpleRepository)(myRepository.held())).get(key);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (G)");
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

     * Warning - Throws no exceptions at all!
     * Returns null if key could not be found or ir anything else went wrong.

     * @param key - Any object, but most often a CryptoHash instance.
     * @return Requested Object or null if object could not be found.

     */

    public Object maybeGetBySymbol(Object symbol) {
        while (true) {
            try {
                return ((SimpleRepository)(myRepository.held())).maybeGetBySymbol(symbol);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (MGS)");
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
     * @return Requested Object

     * @exception RepositoryKeyNotFoundException
     * @exception IOException

     */

    public Object getBySymbol(Object symbol) throws IOException {
        while (true) {
            try {
                return ((SimpleRepository)(myRepository.held())).getBySymbol(symbol);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (GS)");
                initialize();
            }
        }
    }


    /**

     * Get all values for a given key from all repositoryfiles we
     * have.  This method overrides the shadowing effect and really
     * collects all values for the key.

     */

    public Vector getAll(Object key) {
        while (true) {
            try {
                return (Vector) ((SimpleRepository)(myRepository.held())).getAll(key);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (GA)");
                initialize();
            }
        }
    }

    /**

     * For all RepositoryFiles in the whole StandardRepository, find
     * all keyword tables and look for a given keyword.  Return a
     * Vector with the keys for all such entries, removing duplicates.
     * Note that this includes entries added by users to Extras
     * directory and the Publish directory.

     * @param keyword - A symbol (often a String) to search for.
     * @return A Vector containing all unique keys that had that keyword.

     */

    public Vector getKeywordSet(Object keyword) {
        while (true) {
            try {
                return ((SimpleRepository)(myRepository.held())).getKeywordSet(keyword);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("StandardRepository reopening after Quake (GKS)");
                initialize();
            }
        }
    }
}
