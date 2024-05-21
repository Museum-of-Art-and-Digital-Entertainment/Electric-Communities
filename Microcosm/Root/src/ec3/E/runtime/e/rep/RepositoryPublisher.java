package ec.e.rep;

import ec.trace.Trace;
import ec.e.file.EStdio;
import ec.e.net.Registrar;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.SturdyRefMaker;
import ec.e.rep.ERepositoryPublisher;
import ec.e.rep.RepositoryKeyNotFoundException;
import ec.e.rep.RepositoryTether;
import ec.e.rep.SuperRepository;
import ec.e.run.EEnvironment;
import ec.e.run.Tether;
import ec.util.NestedException;
import java.io.IOException;

/**

 * The RepositoryPublisher is an object used by other
 * processes/machines to access the PublishRepository in this
 * process/machine. We publish a SturdyRef to ourselves and place that
 * SturdyRef in the hints vector of all objecst we publish, which
 * allows users of these objects to request the actual data from us
 * and then cache the data locally in their own machines.<p>

 * Once you have a RepositoryPublisher capability it's good
 * forever. It is self-healing after quakes.

 * Noone in a vat should normally need this capability for anything.
 * We place it in the vat just to make it persistent since entities in
 * other vats will have a sturdyref to it.

 */

public class RepositoryPublisher {
    RepositoryTether myPublishRepository = null;
    private SturdyRef myRef = null;
    private EEnvironment myEnv = null;

    static public Object summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException {
             String echomePath = eEnv.getProperty("ECHome");
             if (echomePath == null) return null;
             return eEnv.magicPower
               ("ec.e.rep.RepositoryPublisherMaker");
    }

    public RepositoryPublisher(EEnvironment env) {
        myEnv = env;
        initialize();
    }

    /**

     * Initialize RepositoryPublisher. Can be called anytime for any
     * reason, like when reviving from a quake. It does not need any
     * additional capabilities besides those it was handed in its
     * constructor.

     * @exception IOException
     * @exception ClassNotFoundException
     * @exception IllegalAccessException
     * @exception InstantiationException 

     */

    private void initialize() {
        EStdio.out().println("RepositoryPublisher init");
        if (Trace.repository.debug && Trace.ON)
            Trace.repository.debugm("RepositoryPublisher initialization");
        try {
            myPublishRepository = new RepositoryTether(myEnv.vat());
        } catch (Exception e) {
            String msg = "Could not initialize RepositoryPublisher";
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            throw new NestedException(msg, e);
        }
        EStdio.out().println("About to register repository publisher");
        registerRepositoryPublisher();
    }

    public byte[] getBytes(Object key) throws RepositoryKeyNotFoundException, IOException {
        return ((SuperRepository)myPublishRepository.held()).getPublished(key);
    }

    /**
     * Return our SturdyRef so that we can include it in hints vectors
     * in DataHolders.
     */

    public SturdyRef getSturdyRef() {
        if (myRef == null) registerRepositoryPublisher();
        EStdio.out().println("Returning my sturdyref: " + myRef);
        return myRef;
    }

    /**
     * Register ourselves as the RepositoryPublisher so that anyone
     * with the SturdyRef to us can get any of our published data if they have
     * the key.
     */

    private void registerRepositoryPublisher() {
        Registrar registrar = Registrar.summon(myEnv);
        /* 
           // XXX This hangs - try without it for now.
        try {
            registrar.onTheAir();
        }
        catch (RegistrarException e) {
        // It's OK to be here - we're on the air already, s'all
        }
        */
        SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
        SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(myEnv);
        ERepositoryPublisher ePublisher = new ERepositoryPublisher(this);
        myRef = refMaker.makeSturdyRef(ePublisher); // Register the publisher
    }
}
