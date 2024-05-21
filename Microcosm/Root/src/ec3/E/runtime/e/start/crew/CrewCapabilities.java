package ec.e.run;

import java.util.Properties;

import ec.vcache.ClassCache;
import ec.e.run.EEnvironment;
import ec.e.run.Vat;
import ec.e.run.Tether;
import ec.e.run.SmashedException;
import ec.e.run.MagicPowerMaker;
import ec.e.run.RepositoryMaker;
import ec.util.NestedException;

/**

 * CrewCapabilities provides easy access to capabilities for CREW
 * code. The foremost reason for the existence of this class is the
 * need for CREW classes to access the EEnvironment in a controlled
 * manner. <p>

 * Once EEnvironment access is available, all the derivable
 * capabilities are available using summon() calls. No additional
 * capabilities would thus be granted if these summons calls were made
 * here, and made only once.

 * All of these capabilities get reconstructed whenever we start or
 * restart a checkpoint. They are then never changed for the lifetime
 * of this crew environment. <p>

 * This class uses "Object" or Tethers (also typeless) for all
 * instances that have been summoned since the summoned objects need
 * to be typecast anyway. The typecast might as well happen in a class
 * where we need to import the package because we are going to use
 * it. If we don't have to import it here then we won't introduce more
 * compiletime dependencies.

 */

public class CrewCapabilities {

    private static CrewCapabilities theOne = null; // Just give me one tiny little static

    private EEnvironment theEnvironment;
    private Vat theVat = null;

    // The following capability objects are not in the Vat. They are
    // CREW or STEWARD.  We declare them as Object so we don't need to
    // know what they really are, to break compile-time dependencies.

    private Object theSuperRepository = null;
    private Object theRemoteRetriever = null;

    // The following are Tethers since they hold on to in-vat objects.

    private Tether theParimeterizedRepository = null;
    private Tether theStandardRepository = null;
    private Tether theNullFulfiller = null;
    private Tether thePublishFulfiller = null;
    private Tether theRepositoryPublisher = null;

    /**

     * Initialize. Called at system startup (EBoot, ELogin) and at
     * revival time (Revive) as soon as we have the EEnvironment set
     * up.

     * @param env The EEnvironment - declared as Object to break a
     * compile dependencies.

     */

    public static void establishCrewCapabilities(Object env) throws OnceOnlyException {
        if (theOne != null)
            throw new OnceOnlyException("Crew capabilities have already been established");
        theOne = new CrewCapabilities((EEnvironment)env);
    }

    /** 

     * Initialize. Called at system startup: ELogin

     * @param env The EEnvironment - declared as Object to break a
     * @param rep The SuperRepository - declared as Object to break a
     * compile dependencies.

     */

    public static void establishCrewCapabilities(Object env, Object rep) throws OnceOnlyException {
        if (theOne != null)
            throw new OnceOnlyException("Crew capabilities have already been established");
        theOne = new CrewCapabilities((EEnvironment)env);
        theOne.theSuperRepository = rep;
    }

    /**
     * Constructor
     */

    private CrewCapabilities(EEnvironment env) {
        theEnvironment = env;
        theVat = env.vat();
    }

    /**

     * Connect to the in-vat StandardRepository and
     * ParimeterizedRepository. This is done by using
     * EEnvironment.magicPower(). If we are starting, then these
     * repositories are created and saved in the EEnvironment. If we
     * are reviving, then these magicPower calls will return to us
     * pointers to the in-vat repositories already in use. These
     * in-vat repositories may well be inoperative following a quake
     * but they can (and will) be repaired when we try to use them.
     * For now, we just want to get pointers to them so that we can
     * pass them to any crew code that needs to access them.

     */

    private void initializeRepositories() {
        String echomePath = theEnvironment.getProperty("ECHome");
        if (echomePath == null)
            throw new RuntimeException
              ("StandardRepository unavailable - Property 'ECHome' is undefined");
        try {                   // Create Tethers for the in-vat objects
            theParimeterizedRepository =
              new Tether(theVat,theEnvironment.magicPower
                         ("ec.e.rep.ParimeterizedRepositoryMaker"));
            theStandardRepository =
              new Tether(theVat,theEnvironment.magicPower
                         ("ec.e.rep.StandardRepositoryMaker"));
        } catch (ClassNotFoundException ex) {
            throw new NestedException("Repository could not initialize" , ex);
        } catch (IllegalAccessException ex) {
            throw new NestedException("Repository could not initialize", ex);
        } catch (InstantiationException ex) {
            throw new NestedException("Repository could not initialize", ex);
        }
    }

    /**

     * Connect to one (in-vat) NullFulfiller and
     * another (in-vat) PublishFulfiller. This is done by using
     * EEnvironment.magicPower(). If we are starting, then these
     * Fulfillers are created and saved in the EEnvironment. If we
     * are reviving, then these magicPower calls will return to us
     * pointers to the in-vat Fulfillers already in use. These
     * in-vat Fulfillers may well be inoperative following a quake
     * but they can (and will) be repaired when we try to use them.
     * For now, we just want to get pointers to them so that we can
     * pass them to any crew code that needs to access them.

     */

    private void initializeNullFulfiller() {
        try {                   // Create Tethers for the in-vat objects
            if (theParimeterizedRepository == null)
                initializeRepositories();
            theNullFulfiller =
              new Tether(theVat,theEnvironment.magicPower
                         ("ec.e.hold.NullFulfillerMaker"));
        } catch (ClassNotFoundException ex) {
            throw new NestedException("Fulfillers could not initialize" , ex);
        } catch (IllegalAccessException ex) {
            throw new NestedException("Fulfillers could not initialize", ex);
        } catch (InstantiationException ex) {
            throw new NestedException("Fulfillers could not initialize", ex);
        }
    }

    private void initializePublishFulfiller() {
        try {                   // Create Tethers for the in-vat objects
            if (theParimeterizedRepository == null)
                initializeRepositories();

            // XXX This is not quite right. The PublishFulfiller may
            // not always be available - As an example, in the curator
            // we are not interested in publishing anything and have
            // no running Registrar. In order to make
            // DataHolderSteward work at all we will make the
            // nullFulfiller stand in for the PublishFulfiller
            // whenever the PublishFulfiller cannot be started. If
            // there in fact is a problem with the comm system or the
            // Registrar, then something else is likely to throw an
            // error shortly.

            if (getTheEnvironment().getProperty("SearchPath") != null) {
                thePublishFulfiller =
                  new Tether(theVat,theEnvironment.magicPower
                             ("ec.e.hold.PublishFulfillerMaker"));
            } else {

                // Call getTheNullFulfiller() for side effect to
                // guarantee that the tether named theNullFulfiller
                // has a value.  We cannot use the return value form
                // this call since it returns the actual fulfiller,
                // not a tether to it, and we want to share the
                // tether.

                getTheNullFulfiller();
                thePublishFulfiller = theNullFulfiller; // XXX Use nullfulfiller as a standin
                System.out.println("No 'SearchPath' property, no Registrar. " +
                                   "Using NullFulfiller, not PublishFulfiller");
            }
        } catch (ClassNotFoundException ex) {
            throw new NestedException("PublishFulfiller could not initialize" , ex);
        } catch (IllegalAccessException ex) {
            throw new NestedException("PublishFulfiller could not initialize", ex);
        } catch (InstantiationException ex) {
            throw new NestedException("PublishFulfiller could not initialize", ex);
        }
    }

    /**
     * Return the environment to any CREW code that wants it.
     */

    public static EEnvironment getTheEnvironment() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");
        return theOne.theEnvironment;
    }

    /**
     * Return the vat to any CREW code that wants it.
     */

    public static Vat getTheVat() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");
        return theOne.theVat;
    }

    /**
     * Return the SuperRepository (a CREW object) to any CREW code
     * that wants it. If it does not already exist, then we create it
     * here and now. Note: This must be (and is) the only call to the
     * SuperRepository constructor anywhere in the system. Everyone
     * else should get it by calling thei method from CREW space. If
     * you are in the vat, then you cannot access the SuperRepository
     * directly.
     */

    public static Object getTheSuperRepository() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");
        if (theOne.theSuperRepository == null) {
            try {
                // Make a SuperRepository. Do it by hand to avoid
                // compile time dependencies.  Also don't use
                // EEnvironment.magicPower() since we don't want this
                // entered in the MagicPowers hashtable.
                Class makerClass = ClassCache.forName("ec.e.rep.SuperRepositoryMaker");
                MagicPowerMaker maker = (MagicPowerMaker)makerClass.newInstance();
                theOne.theSuperRepository = maker.make(theOne.theEnvironment);
            } catch (ClassNotFoundException x) {
                throw new NestedException("SuperRepository class missing??", x);
            } catch (IllegalAccessException x) {
                throw new NestedException("SuperRepository access exception??", x);
            } catch (InstantiationException x) {
                throw new NestedException("SuperRepository did not instantitate??", x);
            }
        }
        if (theOne.theSuperRepository == null)
            throw new RuntimeException("BAD THING: SuperRepository is null");
        return theOne.theSuperRepository;
    }

    /**
     * Returns the StandardRepository, an in-vat object, to any CREW
     * code that needs it. This code is self-initializing and
     * self-repairing after quakes and quakedrills.
     */

    public static Object getTheParimeterizedRepository() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");

        while (true) {
            if (theOne.theParimeterizedRepository == null)
                theOne.initializeRepositories(); // Delay this until needed
            try {
                return theOne.theParimeterizedRepository.held();
            } catch (SmashedException smex) { // This cannot happen in a real quake,
                theOne.initializeRepositories(); // but we want to handle quake drills also.
            }
        }
    }

    /**
     * Returns the StandardRepository, an in-vat object, to any CREW
     * code that needs it. This code is self-initializing and
     * self-repairing after quakes and quakedrills.
     */

    public static Object getTheStandardRepository() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");

        while (true) {
            if (theOne.theStandardRepository == null)
                theOne.initializeRepositories(); // Delay this until needed
            try {
                return theOne.theStandardRepository.held();
            } catch (SmashedException smex) { // This cannot happen in a real quake
                theOne.initializeRepositories(); // but we want to handle quake drills also.
            }
        }
    }

    public static Object getTheNullFulfiller() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");
        while (true) {
            if (theOne.theNullFulfiller == null)
                theOne.initializeNullFulfiller(); // Delay this until needed
            try {
                Object result = theOne.theNullFulfiller.held();
                if (result == null) throw new Error("Shouldn't happen - NullFulfiller");
                return result;
            } catch (SmashedException smex) { // This cannot happen in a real quake
                theOne.initializeNullFulfiller(); // but we want to handle quake drills also.
            }
        }
    }

    public static Object getThePublishFulfiller() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");
        while (true) {
            if (theOne.thePublishFulfiller == null)
                theOne.initializePublishFulfiller(); // Delay this until needed
            try {
                return theOne.thePublishFulfiller.held();
            } catch (SmashedException smex) { // This cannot happen in a real quake
                theOne.initializePublishFulfiller(); // but we want to handle quake drills also.
            }
        }
    }

    public static Object getTheRemoteRetriever() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");
        if (theOne.theRemoteRetriever == null) {
            try {
                // Make a RemoteRetriever. Do it by hand to avoid
                // compile time dependencies.  Also don't use
                // EEnvironment.magicPower() since we don't want this
                // entered in the MagicPowers hashtable.
                Class makerClass = ClassCache.forName("ec.e.rep.RemoteDownloaderMaker");
                MagicPowerMaker maker = (MagicPowerMaker)makerClass.newInstance();
                theOne.theRemoteRetriever = maker.make(theOne.theEnvironment);
            } catch (ClassNotFoundException x) {
                throw new NestedException("RemoteRetriever class missing??", x);
            } catch (IllegalAccessException x) {
                throw new NestedException("RemoteRetriever access exception??", x);
            } catch (InstantiationException x) {
                throw new NestedException("RemoteRetriever did not instantitate??", x);
            }
        }
        return theOne.theRemoteRetriever;
    }

    public static Object getTheRepositoryPublisher() {
        if (theOne == null)
            throw new RuntimeException("CrewCapabilities have not been initialized yet");
        while (true) {
            try {
                if (theOne.theRepositoryPublisher == null) {
                    try {
                        // Make a RepositoryPublisher. Do not use a summon()
                        // method to avoid compoile time dependencies.
                        theOne.theRepositoryPublisher = new Tether
                          (getTheVat(),getTheEnvironment().magicPower("ec.e.rep.RepositoryPublisherMaker"));
                    } catch (ClassNotFoundException x) {
                        throw new NestedException("RemoteRetriever class missing??", x);
                    } catch (IllegalAccessException x) {
                        throw new NestedException("RemoteRetriever access exception??", x);
                    } catch (InstantiationException x) {
                        throw new NestedException("RemoteRetriever did not instantitate??", x);
                    }
                }
                return theOne.theRepositoryPublisher.held();
            } catch (SmashedException smex) { // This only happens in quakedrills
            }
        }
    }

    public static Object createCrewRepository(Properties props) {
        try {
            // Make a SuperRepository. Do it by hand to avoid
            // compile time dependencies.  Also don't use
            // EEnvironment.magicPower() since we don't want this
            // entered in the MagicPowers hashtable.
            Class makerClass = ClassCache.forName("ec.e.rep.SuperRepositoryMaker");
            RepositoryMaker maker = (RepositoryMaker)makerClass.newInstance();
            return maker.make(props);
        } catch (ClassNotFoundException x) {
            throw new NestedException("SuperRepository class missing??", x);
        } catch (IllegalAccessException x) {
            throw new NestedException("SuperRepository access exception??", x);
        } catch (InstantiationException x) {
            throw new NestedException("SuperRepository did not instantitate??", x);
        }
    }

    public static void setCrewRepository(Object repository) {
        if (theOne == null) {
            throw new RuntimeException("CrewCapabilities have not been initialized yet");
        }
        else {
            theOne.theSuperRepository = repository;
        }
    }

    // Feel free to add more capabilities here. Use getTheRemoteRetriever as a model for
    // CREW objects and getTheRepositoryPublisher as a model for in-vat objects.

}
