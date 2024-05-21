package ec.e.rep;

import ec.trace.Trace;
import ec.e.file.EStdio;
import ec.e.rep.SimpleRepository;
import ec.e.rep.SuperRepository;
import ec.e.rep.RepositoryTether;
import ec.e.hold.DataHolder;
import ec.e.hold.Fulfiller;
import ec.cert.CryptoHash;
import ec.e.run.EEnvironment;
import ec.e.run.SmashedException;
import ec.e.net.SturdyRef;
import ec.util.NestedException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.run.Tether;
import ec.tables.PEHashtable;

/**

 * The PublishRepository is an in-vat capability to the
 * SuperRepository, the main Repository access Policy Object, a
 * STEWARD. The PublishRepository has the following features:<p>

 * <ul>
 * <li>PublishRepository is self-repairing after a quake.
 * <li>PublishRepository is writeable - it implements putHash()
 * <li>PublishRepository can generate instantly published DataHolders.
 * </ul>

 * Once you have a PublishRepository capability it's good forever. It
 * will work just as fine after a quake, since it's self-repairing.

 * The PublishRepository may not point to the same RepositoryFile
 * between successive invocations of putHash() or put(). You should
 * not care.<p>

 * Items added to the PublishRepository are immediately accesible from
 * the regular StandardRepository and/or ParimetereizedRepository. The
 * added object is also published using the RepositoryPublisher, which
 * means that DataHolders returned from makeDataHolder() can be given
 * to objects in other vats and immediately work.

 */

public class PublishRepository {
    private RepositoryTether myRepository = null;
    private EEnvironment myEnv = null;
    private Vector myHints = new Vector(1);
    private Fulfiller myFulfiller = null;

    // Dang. A circular reference to ../hold, compiled later!


    private RepositoryPublisher myPublisher = null;
    private SturdyRef mySturdyRef = null;

    static public Object summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException {
             String echomePath = eEnv.getProperty("ECHome");
             if (echomePath == null) return null;
             return (PublishRepository)eEnv.magicPower
               ("ec.e.rep.PublishRepositoryMaker");
    }

    /**
     * Construct a (the) PublishRepository
     */

    public PublishRepository(EEnvironment env) {
            myEnv = env;
            initialize();
    }

    /**

     * Initialize this PublishRepository. Can be called anytime for
     * any reason, like when reviving from a quake. It does not need
     * any additional capabilities besides those we were handed in the
     * constructor.

     */

    public void initialize() {
        if (Trace.repository.debug && Trace.ON)
            Trace.repository.debugm("PublishRepository initialization");
        try {
            myRepository = new RepositoryTether(myEnv.vat());
            Object pRep = ParimeterizedRepository.summon(myEnv); // Use appropriate (different) repository for fulfillers
            myFulfiller = new Fulfiller((ParimeterizedRepository)pRep,null,myHints); // Only need to do this ONCE EVER

            myPublisher = (RepositoryPublisher)RepositoryPublisher.summon(myEnv);
            mySturdyRef = myPublisher.getSturdyRef();
            if (myHints.size() == 0) myHints.addElement(mySturdyRef);
            else myHints.setElementAt(mySturdyRef,0);
        } catch (Exception e) {
            EStdio.reportException(e);
            throw new NestedException("Could not initialize PublishRepository", e);
        }
    }

    /**

     * Store an object in the PublishRepository and return a CryptoHash object for it.
     * Repairs this PublishRepository automatically after a quake.

     */

    public CryptoHash putHash(Object object) throws IOException {
        while (true) {
            try {
                return (CryptoHash)((SuperRepository)myRepository.held()).putHashInPublishRepository(object);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("PublishRepository reopening after Quake (PH)");
                initialize();
            }
        }
    }

    /**

     * Store an object in the PublishRepository and return a DataHolder for it.
     * Repairs this PublishRepository automatically after a quake.

     */

    public DataHolder makeDataHolder(Object object) throws IOException {
        return makeDataHolder(object,myFulfiller,null); // Use our standard fulfiller and ignore certificates
    }

    /**

     * Store an object in the PublishRepository return a CryptoHash object for it.
     * Repairs this PublishRepository automatically after a quake.

     */

    public DataHolder makeDataHolder(Object object, Fulfiller fulfiller,
                                     Hashtable certificates) throws IOException {
        if (fulfiller == null) fulfiller = myFulfiller;
        while (true) {
            try {
                return (DataHolder)((SuperRepository)myRepository.held()).makeDataHolder
                  (object,fulfiller,certificates);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("PublishRepository reopening after Quake (MDH)");
                initialize();
            }
        }
    }

    /**

     * Store an object in the PublishRepository return a CryptoHash object for it.
     * Also add a given to the PublishRepository symbol table (creating it if it doesn't exist)
     * and add a set of keywords to the keywordstable (creating it if it doesn't exist).
     * Repairs this PublishRepository automatically after a quake.
     */

    public DataHolder makeDataHolder(Object object, Fulfiller fulfiller,
                                     Hashtable certificates, Object symbol,
                                     Vector keywords, PEHashtable parimeters)
         throws IOException {
             if (fulfiller == null) fulfiller = myFulfiller;
        while (true) {
            try {
                EStdio.out().println("pubr.mkdh - Symbol is " + symbol);
                return (DataHolder)((SuperRepository)myRepository.held()).makeDataHolder
                  (object,fulfiller,certificates, symbol,keywords,parimeters);
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("PublishRepository reopening after Quake (MDH6A)");
                initialize();
            }
        }
    }
    /**

     * Flush our changes to the PublishRepository.  There's never a
     * need to close this Repository but Flush will guarantee that the
     * Repository updates make it all the way to the disk.

     */

    public void flush() throws IOException {
        while (true) {
            try {
                EStdio.out().println("pubr.flush");
                ((SuperRepository)myRepository.held()).flush();
                return;
            } catch (SmashedException smex) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm("PublishRepository reopening after Quake (FL)");
                initialize();
            }
        }
    }
}
