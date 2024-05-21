package ec.e.hold;

import java.util.Vector;
import java.util.Hashtable;
import java.io.IOException;
import ec.e.rep.steward.RepositoryAccessException;
import ec.e.rep.steward.RepositoryNeedParameterException;
import ec.e.rep.ParimeterizedRepository;
import ec.e.start.crew.CrewCapabilities;
import ec.e.start.Tether;
import ec.e.start.EEnvironment;
import ec.e.serial.Unserializer;
import ec.e.serial.ParamUnserializer;
import ec.e.net.SturdyRef;
import ec.cert.CryptoHash;
import java.io.ByteArrayInputStream;
import ec.e.openers.*;
import ec.e.openers.guest.AllowingClassRecipe;
import ec.util.NestedException;
import java.util.Enumeration;
import ec.e.util.DiscreteEnumeration;
import ec.e.rep.steward.Repository;
// JAY import ec.regexp.RegularExpression;
import ec.e.openers.guest.SerializableMarker;
import ec.e.openers.OpenerRecipe;;

/**

 * A Fulfiller is a Guest object that knows how to resolve DataHolders
 * to their specified objects. Fulfillers are created for you at
 * startup and when you connect to a new host. The appropriate
 * Fulfiller is added through parimeterization to every DataHolder you
 * get from local repositories or from the remote host by your own
 * Comm system, upon decode of the DataHolder.<p>

 * The URL to use to lookup or search for the data is constructed of
 * pieces that get appended together: The hints (several may be
 * available), and The cryptohash key (as a hex integer), and possibly
 * other stuff. The design of this is open-ended - Hints are specified
 * to be an array of strings but we don't specify (yet) a format for
 * them or how we will interpret them. <p>

 * For the Beta release, we will support only the most fundamental
 * hint: The hint is a SturdyRef to a RepositoryPublisher in some COSM
 * client or server that publishes the object. Frequently this will be
 * the srver or client that hosts the unum using the object in
 * question.

 */

// Design Reminder: We must spend some effort in examining all constructed URL's
// to make sure that they look "normal" , so that noone can sneak in a
// URL that terminates before the cryptohash key is appended. This
// parser may need to detect other URL terminator strings besides "?".

public class Fulfiller implements DataRequestor {
    private Vector myHints = null;
    private ParimeterizedRepository myRepository = null;
    private Hashtable myParimeterArguments = new Hashtable(3);
    private static OpenerRecipe myMaker = defaultRecipe();
    private static Object thePublisher = null;

    // The vector below is used at curate time. It becomes the vector
    // to replace with a parimeter table entry when encoding and will
    // be replaced back at decode time with an appropriate hints
    // vector which may not be empty - it may come from a "parent" or
    // "upper" dataholder that is decoding contained ("lower")
    // dataholders.

    // Recursive dataholders are causing all kinds of problems
    // here. maybe we should get rid of them?

    // It's not very nice to usurp the Parimeterized decode to pass
    // parameters in this fashion but it achieves exactly what we
    // want: All lower-level DataHolder decodes that happen when we
    // are requesting the data for a given (upper-level) dataholder
    // will inherit the hints from the upper dataholder.

    public static Vector defaultEmptyHints = new Vector(0);
    public static String HINTS = "Hints"; // HINTPARIMS XXX Make this a non-string!

    static private OpenerRecipe defaultRecipe() {
        ClassRecipe classRecipe
            = AllowingClassRecipe.make(RootClassRecipe.THE_ONE,
                                       "ec.e.hold.ReposableMarker");

        return new OpenerRecipe(ReposableMarker.DECODER_MAKERS,
                                ReposableMarker.ENCODER_MAKERS,
                                classRecipe);
    }

    /**
     * No-argument constructor. Creates a useless Fulfiller. Used in
     * mainly as a dummy in Parimeter tables, such as the one in the Curator.
     */

    public Fulfiller() {
    }

    public Fulfiller makeFulfiller(Vector newHints) {
        return new Fulfiller(myRepository, newHints);
    }

    /**
     * Deprecated Constructor - use two argument version.
     */

    public Fulfiller(ParimeterizedRepository repository, Object urlFetcher, Vector hints) {
        this(repository,hints);
    }

    /**
     * Constructor.
     */

    public Fulfiller(ParimeterizedRepository repository, Vector hints) {
        myRepository = repository;
        myHints = hints;

        if (myHints == null) myHints = defaultEmptyHints;

        // We build our parimeters table here. Add lines below if more needed.
        myParimeterArguments.put("Repository",myRepository);
        myParimeterArguments.put(HINTS,myHints); // HINTPARIMS
    }

    static public Object summonNullFulfiller(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException {
             return eEnv.magicPower("ec.e.hold.NullFulfillerMaker");
    }

    static public Object summonPublishFulfiller(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException {
             return eEnv.magicPower("ec.e.hold.PublishFulfillerMaker");
    }

    /* package */ Vector getHints() {
        if (myHints == null) return null;
        return (Vector)myHints.clone();
    }

    /**
     * Retrieve an object from our Repositories.

     * @param key untrusted notNull - A object key to use to retrieve
     * data from our Repositories.
     */

    public Object getFromRepository(Object key) throws IOException {
        return myRepository.get(key,myParimeterArguments);
    }

    /**
     * We need to retrieve the data over the network.  Ask the
     * ParimeterRepository to do it and then forward the results to
     * the DataHolder using the acceptData() callback.

     * We ask for the data as bytes to avoid an extra decode/encode
     * in transit and to avoid giving out our parimetertable.

     * We pass ourselves into the call since we want to intercept the
     * data coming back (so we can decode them using our secret
     * parimeter table) and the Repository will call us instead of the
     * original caller if we call the 4-argument version of
     * requerstByteRetrieval();

     */

    public void requestByteRetrieval(DataHolder yourHolder, CryptoHash hash) throws IOException {
        myRepository.requestByteRetrieval(hash, myHints, (DataRequestor)yourHolder);
    }

    /**

     * We implement the DataRequestor interface ourselves since we
     * will be called from the WWWeb retrieval code.

     * XXX If the URL code calls back from another thread, then either
     * it or this method must synchronize on the VatLock.  */

    public void acceptData(Object data, DataRequestor dataHolder) {

        // Verify the cryptohash of the data before we pass it on.
        // Note that this may be quite expensive but at this point we
        // have no choice since it came in from the network.

        if (dataHolder instanceof DataHolder) {
            CryptoHash receivedHash = Repository.computeCryptoHash(data);
            if (receivedHash.equals(((DataHolder)dataHolder).getCryptohash())) {
                if (receivedHash.equals(((DataHolder)dataHolder).getCryptohash())) {
                    ((DataRequestor)dataHolder).acceptData(data,dataHolder);
                } else {
                    ((DataRequestor)dataHolder).handleFailure
                      (new SecurityException
                       ("Fulfiller detected a CryptoHash mismatch - hints: " + myHints),
                       (DataRequestor)dataHolder);
                }
            } else {                // Don't attempt verification if we can't get at the CryptoHash
                ((DataRequestor)dataHolder).acceptData(data,dataHolder);
            }
        }
    }

    /**

     * XXX Is this method a security leak? I did not have it in the
     * original design but now it looks necessary. - KJD

     */

    public Object decodeByteData(DataRequestor requestor, DataHolder holder, byte[] data)
         throws IOException {

        // Verify the cryptohash of the data before we decode it, if we can.
        // We can get the cryptohash for a DataHolder but not for anything else.

        if (holder != requestor)
            throw new SecurityException("Remote data request was returned to wrong DataHolder");

        CryptoHash receivedHash = new CryptoHash(data);
        if (receivedHash.equals(holder.getCryptohash())) {
            return updateCache(unserialize(data, myParimeterArguments));
        }
        throw new SecurityException
          ("Fulfiller detected a CryptoHash mismatch - hints: " + myHints);
    }

    private Object updateCache(Object result) {
        // Put the downloaded data into the cache

        // XXX We should write a version of this method that accepted
        // the encoded data as a byte array, since that would be more
        // efficient.

        if (myRepository != null) {
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Storing object " + result +
                                        " in Cache Repository");
            try {
                myRepository.putHashInCacheRepository(result);
            } catch (IOException iox) {
                String msg = "Save of object " + result.toString() +
                  " in Cache repository caused IOException:" +
                  iox.getMessage();
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm(msg);
                throw new NestedException(msg,iox);
            }
        } else throw new RuntimeException("No parimeterRepository in DataHolder");
        return result;
    }

    /**

     * We implement the DataRequestor interface ourselves since we
     * will be called from the WWWeb retrieval code and will forward
     * the results (after decoding) to the original DataHolder.

     * We cannot forward the results from the WWW downloader directly
     * to the DataHolder because the decoding requires our
     * parimetertable and we don't want to export it to the
     * DataHolders.

     * XXX If the URL code calls back from another thread, then either
     * it or this method must synchronize on the VatLock.  */

    public void acceptByteData(byte[] data, DataRequestor dataHolder) {

        // Verify the cryptohash of the data before we decode it, if we can.
        // We can get the cryptohash for a DataHolder but not for anything else.
        if (dataHolder instanceof DataHolder) {
            CryptoHash receivedHash = new CryptoHash(data);
            Object result = null;
            if (receivedHash.equals(((DataHolder)dataHolder).getCryptohash())) {
                try {
                    result = unserialize(data, myParimeterArguments);
                } catch (IOException iox) {
                    ((DataRequestor)dataHolder).handleFailure(iox, (DataRequestor)dataHolder);
                    return;
                }
                updateCache(result);
                ((DataRequestor)dataHolder).acceptData(result,dataHolder);
            } else {
                ((DataRequestor)dataHolder).handleFailure
                  (new SecurityException
                   ("Fulfiller detected a CryptoHash mismatch - hints: " + myHints),
                   (DataRequestor)dataHolder);
            }
        } else {
            // Don't attempt verification if we can't get at the CryptoHash
            Object result = null;
            try {
                // We'll just have to trust the results. XXX Analyze if this is a problem
                result = unserialize(data, myParimeterArguments);
                if (Trace.repository.debug && Trace.ON) {
                    Trace.repository.debugm
                      ("Updating cache with unverified data retrieved by non-DataHolder:" +
                       result);
                }
            } catch (IOException iox) {
                ((DataRequestor)dataHolder).handleFailure(iox, (DataRequestor)dataHolder);
                return;
            }
            updateCache(result);
            ((DataRequestor)dataHolder).acceptData(result,dataHolder);
        }
    }

    /**
     * Part of DataRequestor interface
     */

    public void handleFailure(Exception exception, DataRequestor dataHolder) {
        ((DataRequestor)dataHolder).handleFailure(exception, dataHolder);
    }

    private Object unserialize(byte[] dataAsBytes, Hashtable parimeters) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(dataAsBytes);
        Unserializer unserializer = ParamUnserializer.make(byteStream,myMaker,parimeters);
        Object result = unserializer.decodeGraph();
        return result;
    }
}

