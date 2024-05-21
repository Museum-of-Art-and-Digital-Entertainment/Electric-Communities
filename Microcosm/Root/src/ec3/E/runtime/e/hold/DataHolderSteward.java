package ec.e.hold;

import ec.cert.Certificate;
import ec.cert.CryptoHash;
import ec.cert.Verifier;
import ec.e.hold.DataHolder;
import ec.e.hold.DataRequestor;
import ec.e.hold.Fulfiller;
import ec.e.rep.steward.Repository;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.rep.ParimeterizedRepository;
import ec.e.start.crew.CrewCapabilities;
import ec.e.start.SmashedException;
import ec.e.start.Tether;
import ec.e.start.EEnvironment;
import ec.e.start.Vat;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Dictionary;
import java.util.NoSuchElementException;
import java.io.Serializable;

/**

 * DataHolderSteward is the foremost class to implement the DataHolder
 * interface - An object that knows how to attempt to retrieve some
 * data that it is a placeholder for.

 * myFulfiller is the capability needed to retrieve the data and is
 * not exposed to the users - all they can do is ask for the one piece
 * of data the DataHolder stands for. <p>

 * cryptoHash is the cryptographic hash of the data. <p>

 * myDataRequestor is either an object that implements the Requestor
 * interface or a Vector of objects that do. We check which one it is
 * before calling any methods on it. We do this after we retrieve the
 * data we stand for, either synchronously (if the data was in the
 * Repository) or asynchronously (if we had to retrieve it over the
 * network).

 * These objects are intermediates (or rather, switchboards or
 * redirectors, given multiple repositories, network sites to retrieve
 * data from, and multiple vats) between the Repository and
 * RepositoryPublishers/WWWeb on one side and the users of data on the
 * other. As such intermediaries they implement both the DataHolder
 * and DataRequestor interfaces.

 */

/* 

 * The design bears some traces (including the name!) of an earlier
 * effort to cache data in the DataHolders themselves. This is not
 * currently the plan, but it may well be determined that this in fact
 * is a good place to cache data so the code has not been cleaned up
 * to remove the cache stuff, mainly the Tether holding on to the data
 * after retrieval.

 */

public class DataHolderSteward
  implements DataHolder, DataRequestor, Serializable, RtStateUpgradeable
{

    // Make something that openerrecipes can recognize us by
    //    static public final Class TYPE = new DataHolderSteward(null,null,null).getClass();

    /*package*/ Fulfiller myFulfiller;

    private CryptoHash myCryptoHash;
    private Hashtable myCertificates = null;
    private transient Object myDataRequestor = null;    // One or more DataRequestor objects

    // The following variables are reinitialized by
    // delayedInitialize() if any of them are null, which they will be
    // post-quake.

    private static ParimeterizedRepository theRepository = null;
    private static Hashtable theCertTable = null;
    private static Fulfiller theNullFulfiller = null;
    private static Fulfiller thePublishFulfiller = null;

    // This boolean controls a performance experiment conducted by Kari and  Dima
    // Remove it before flight unless we decide to use it.
    private static boolean makeImmediates = false;

    /**
     * Constructs a DataHolderSteward.
     */

    public DataHolderSteward(CryptoHash cryptoHash, Fulfiller fulfiller, Hashtable certs) {
        myCryptoHash = cryptoHash;
        myFulfiller = fulfiller;
        myCertificates = certs; // XXX Is it a security risk to share this?
    }

    /**
     * Constructs an DataHolderSteward. This version requires no
     * explicit fulfiller or repository capabilities and looks up your
     * certificates for you.
     */

    public DataHolderSteward(CryptoHash cryptoHash) {
        myCryptoHash = cryptoHash;

        if (theRepository == null) {
            delayedInitialize();
        }

        if (theRepository.isPublished(myCryptoHash)) {
            if (thePublishFulfiller == null || thePublishFulfiller == theNullFulfiller) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm
                      ("Attempting to create a publishfulfiller for " + myCryptoHash);
                thePublishFulfiller = (Fulfiller)CrewCapabilities.getThePublishFulfiller();
                if (thePublishFulfiller == theNullFulfiller)
                    Trace.repository.debugm
                      ("Could not create a publishFulfiller for" + cryptoHash);
            }
            myFulfiller = thePublishFulfiller;
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm
                  ("Used publishfulfiller for " + myCryptoHash);
        }
        else {
            myFulfiller = theNullFulfiller;
        }

        if (theCertTable != null)
            myCertificates = (Hashtable)theCertTable.get(myCryptoHash);
        else myCertificates = null;
    }

    /**
     * A factory that makes a DataHolderSteward for you.
     */

    public static DataHolder makeDataHolderFromCryptoHash(CryptoHash cryptoHash) {
        //        if (makeImmediates) return new ImmediateDataHolder(cryptoHash);
        return new DataHolderSteward(cryptoHash);
    }

    /**
     * A factory that computes your CryptoHash for you.
     */

    public static DataHolder makeDataHolder(Object object) {
        //if (makeImmediates) return new ImmediateDataHolder(Repository.computeCryptoHash(object));
        return new DataHolderSteward(Repository.computeCryptoHash(object));
    }

    /**
    
     * A factory that looks up a symbol for you. It returns null if
     * the symbol does not exist.<p>

     * ctg - modified to throw a RepositoryKeyNotFoundException (a
     * subclass of IOException) instead of returning null if symbol
     * does not exist (1.26.98) <p>

     * mla - Modified declaration to IOException (a widening of
     * RepositoryKeyNotFoundException) to remove compile-time
     * dependencies <p>

     */

    public static DataHolder makeDataHolderFromSymbol(Object symbol) throws IOException {
        if (theRepository == null) {
            delayedInitialize();
        }
        if(symbol instanceof String)  {
            symbol = ((String)symbol).toLowerCase();
        }
        CryptoHash hash = (CryptoHash)theRepository.getCryptoHash(symbol);
        //if (hash == null) return null;
        if (hash == null)  {
            throw new RepositoryKeyNotFoundException
              ("Request to make DataHolder from symbol: " + symbol + 
               " failed.  Symbol not found.");
        }
        else {
 //        if (makeImmediates) return new ImmediateDataHolder(hash);
            DataHolder result = new DataHolderSteward(hash);
            return result;
        }
    }

    public static DataHolder makeDataHolderFromSymbol
    (Object s,
     Dictionary symbolTable,
     Dictionary baseSymbolTable) throws RepositoryKeyNotFoundException {

         if (s == null || symbolTable == null) {
             if (s == null)
                 throw new RepositoryKeyNotFoundException
                   ("Attempt to make dataholder from null symbol");
             else {
                 RuntimeException rte = new RuntimeException("Null symboltable");
                 rte.printStackTrace();
                 throw rte;
             }
         }
             
         DataHolder result = null;
         CryptoHash c_hash = null;
         Object key = null;
        
         if(s instanceof String) { 
             key = ((String)s).toLowerCase();
         }
         try {
             c_hash = (CryptoHash)symbolTable.get(key);
         } catch (NoSuchElementException nsex) {
         }

         // Search the base symbols also in case we are curating
         // incrementally

         if (c_hash == null && baseSymbolTable != null) {
             c_hash = (CryptoHash) baseSymbolTable.get(key); // May throw NoSuchElementException
         }

         if (c_hash != null) {
             return new DataHolderSteward(c_hash);
         }
         else {
             throw new NoSuchElementException
               ("Request to make DataHolder from symbol: " + s + 
                "in symbolTable failed.  Symbol not found.");
         }
    }

    /**
     * Constructs a DataHolderSteward. This version requires no
     * explicit fulfiller or repository capabilities.
     */

    public DataHolderSteward(CryptoHash cryptoHash, Hashtable certs) {
        myCryptoHash = cryptoHash;
        if (theRepository == null) {
            delayedInitialize();
        }
        if (theRepository.isPublished(myCryptoHash)) {
            if (thePublishFulfiller == null) {
                thePublishFulfiller = (Fulfiller)CrewCapabilities.getThePublishFulfiller();
            }
            myFulfiller = thePublishFulfiller;
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm
                  ("Used publishfulfiller for " + myCryptoHash);
        }
        else {
            myFulfiller = theNullFulfiller;
        }

        myCertificates = certs; // XXX Is it a security risk to share this?
    }

    /**
     * Constructor that computes your CryptoHash for you.
     */

    public DataHolderSteward(Vat vat, Object data, Fulfiller fulfiller,
                             Hashtable certificates) {
        if (vat == null) vat = CrewCapabilities.getTheVat();
        myCryptoHash = Repository.computeCryptoHash(data);
        myFulfiller = fulfiller;
        myCertificates = certificates; // XXX Is it a security risk to share this?
    }

    /**
     * Writes the essence of a DataHolder, CryptoHash and 
     * Fulfiller hints vector.
     */ 

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {

      // Write the bytes from the CryptoHash
      out.writeObject(myCryptoHash);
      // Write hints from the fulfiller
      out.writeObject(myFulfiller.getHints());
    }

    /**
     * Reads in and recreates the DataHolderSteward from the CryptoHash and
     * fulfiller hints vector.
     */ 

    private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException {

             // Read in my CryptoHash
             myCryptoHash = (CryptoHash)in.readObject();

             // Read hints to put into Fulfiller.
             Vector fulfillerHints = (Vector)in.readObject();

             delayedInitialize();
             // Instantiate fulfiller

             // This may break art downloading

             setFulfiller(fulfillerHints, null);
    }

    /**

     * Returns our CryptoHash object.  Deprecated, use the other one
     * below. This one is just miscapitalized.

     */

    public CryptoHash getCryptohash() {
        return myCryptoHash;
    }

    /**
     * Returns our CryptoHash object
     */

    public CryptoHash getCryptoHash() {
        return myCryptoHash;
    }

    private static void delayedInitialize() {
        theRepository = (ParimeterizedRepository)CrewCapabilities.getTheParimeterizedRepository();
        theNullFulfiller = (Fulfiller)CrewCapabilities.getTheNullFulfiller();
        EEnvironment env = (EEnvironment)CrewCapabilities.getTheEnvironment();

        // The "maketurf" property is a speedup experiment
        

        if ("true".equals(env.getProperty("maketurf"))) {
            makeImmediates = true;
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm("Creating Immediate DataHolders");
        }

        try {
            theCertTable = (Hashtable)theRepository.get("%Certificates%");
        } catch (IOException iox) {
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm
                  ("Warning - Could not retrieve Certificates from Repository", iox);
            theCertTable = null;
        }
    }

    private static void delayedPublishedInitialize() {
        thePublishFulfiller = (Fulfiller)CrewCapabilities.getThePublishFulfiller();
    }

    /**
     * Apply a given verifier to our certificates set
     * and return pass or fail
     */

    public byte certifiedBy(Verifier verifier) {

        // If verifier is null, then every object passes
        if (verifier == null) return Verifier.FAIL;

        // If hashtable is null, then we pass no tests.
        if (myCertificates == null) return Verifier.FAIL;

        Certificate certificate = (Certificate)myCertificates.get(verifier.getCryptoHash());

        // If we don't have a certificate matching this verifier, then we fail
        if (certificate == null) return Verifier.FAIL;

        // Let verifier decide
//         int tzOffset = java.util.TimeZone.getDefault().getOffset(0,0,0,0,0,0);
//         long currentDate = System.currentTimeMillis();
//         long expirationDate = certificate.getExpirationDate();
//         if (expirationDate < (currentDate-tzOffset)) { 
//           System.out.println("Certificate has expired!");
//           // Do someting to automaticaly re-certify here
//           return Verifier.EXPIRED;
//         }
        return verifier.verify(myCryptoHash.getCopyOfHashBytes(), certificate);
    }

    /**
     * Add a certificate to our certificate set
     */

    public void addCertificate(Certificate certificate) {
        if (myCertificates == null) {
            myCertificates = new Hashtable(1); // KJD - Added size 1
        }
        myCertificates.put(certificate.getVerifierID(), certificate);
    }


    /**
     * Get the hashtable of certificates. The keys are the certificate type
     * ID's (cryptohashes).
     */
    public Hashtable getCertificates() {
        return myCertificates;
    }

    /**

     * Retrieve the data, if necessary and possible. Returns the data
     * synchronously. If the data cannot be retrieved synchronously,
     * throws an error.

     * @exception IOException 

     */

    public Object held() throws IOException {
        return myFulfiller.getFromRepository(myCryptoHash); // Look in caches and releases
    }

    /**
     * Add a DataRequestor object to this DataHolderSteward.  Whenever the
     * data arrives (or fails), it will call the appropriate method in
     * the DataRequestor object. This could happen synchronously if we are
     * lucky, otherwise it will happen when data arrives.
     */

    public void giveDataTo(DataRequestor requestor) {
        Object result = null;
        boolean is_local = false;

        if (requestor == null)
            throw new NullPointerException("Null Requestor in call to giveDataTo");

        try {
            try {
                result = held();
                requestor.acceptData(result, this);

                // Should really catch RepositoryKeyNotFoundException here
                // but that adds to our compile-time circular dependency problems
                // RepositoryKeyNotFoundException is a subclass of IOException
                // which should be close enough here.

            } catch (IOException iox) {

                // We didn't have it in a Repository (cache or
                // distribution medium such as CD-ROM).  Must go get
                // it over the net. Add ourselves to the list of
                // requestors.

                synchronized(this) {
                    if (myDataRequestor == null) {

                        // We haven't requested this data before.

                        myDataRequestor = requestor;
                        myFulfiller.requestByteRetrieval(this,myCryptoHash);

                    } else {

                        // We only request the retrieval of the data
                        // once.  If dataRequestor isn't null, then
                        // we've already requested this to be
                        // donwloaded. In this case we only add
                        // ourselves to the list of requestors. This
                        // list is a vector, but the most common case
                        // is the single requestor so we special case
                        // that. We only create the vector if we get a
                        // second request for the data.

                        if (myDataRequestor instanceof Vector) {
                            ((Vector)myDataRequestor).addElement(requestor);
                        } else {
                            Object old = myDataRequestor;
                            myDataRequestor = new Vector(5);
                            ((Vector)myDataRequestor).addElement(old);
                            ((Vector)myDataRequestor).addElement(requestor);
                        }
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Problems in DataHolderSteward.giveDataTo()";
            if (Trace.repository.debug && Trace.ON) Trace.repository.debugm(msg, e);
            requestor.handleFailure(e,this);
        }
    }

    /**
     * We implement the DataRequestor interface since we
     * will be called from the WWWeb retrieval code.

     * XXX If the URL code calls back from another thread, then either
     * it or this method must synchronize on the VatLock.
     */

    public void acceptData(Object data, DataRequestor dataHolder) {

        // Assert(dataHolder == this);

        // We cannot afford to compute cryptohashes on objects that are not byte arrays.
        //        CryptoHash receivedHash = Repository.computeCryptoHash(data);
        //        if (myCryptoHash.equals(receivedHash)) {

        // If we want to cache the received data in memory, we could...
        // Vat vat = CrewCapabilities.getTheVat();
        // myDataLeaf = new Tether(vat, data);

        if (myDataRequestor instanceof Vector) {
            for (int i = 0; i < ((Vector)myDataRequestor).size(); i++) {
                ((DataRequestor)((Vector)myDataRequestor).elementAt(i)).acceptData(data,this);
            }
        } else {
            ((DataRequestor)myDataRequestor).acceptData(data,this);
        }
        myDataRequestor = null;
//         } else {

//             // XXX This probably should not be a securityexception but
//             // I can't decide what it should be or even what it should
//             // be a subclass of.

//             java.lang.SecurityException cryptoException =
//               new java.lang.SecurityException("Cryptohash mismatch - requested=" +
//                                                      myCryptoHash + ", received=" + receivedHash);
//             if (myDataRequestor instanceof Vector) {
//                 for (int i = 0; i < ((Vector)myDataRequestor).size(); i++) {
//                     ((DataRequestor)((Vector)myDataRequestor).elementAt(i)).handleFailure
//                       (cryptoException, this);
//                 }
//             } else {
//                 ((DataRequestor)myDataRequestor).handleFailure(cryptoException, this);
//             }

//             // XXX We *could* hang on to the requestor(s) and add a
//             // method retry() that attempted delivery again.  This
//             // means exporting network unreliability issues all the
//             // way to the consumers of the data. Maybe this is
//             // reasonable.

//             myDataRequestor = null;
//         }
    }

    /**
     * Part of DataRequestor interface.
     * We implement the DataRequestor interface since we
     * will be called from the Remote Retrieval code when downloading
     * data from PublishRepositories or WWW servers.

     * XXX If the URL code calls back from another thread, then either
     * it or this method must synchronize on the VatLock.
     */

    public void acceptByteData(byte[] data, DataRequestor dataHolder) {
        Object result;
        if (Trace.repository.debug && Trace.ON) {
            Trace.repository.debugm("In acceptdata in DHS - Dataholder is " +
                                    dataHolder);
        }
        try {
            result = myFulfiller.decodeByteData(dataHolder, this, data);
        } catch (IOException iox) {
            // We got this far and then couldn't decode the results. Pity.
            // Just call handlefailure() on ourselves.
            handleFailure(iox,dataHolder);
            return;
        }
        
        if (myDataRequestor instanceof Vector) {
            for (int i = 0; i < ((Vector)myDataRequestor).size(); i++) {
                ((DataRequestor)((Vector)myDataRequestor).elementAt(i)).acceptData(result,this);
            }
        } else {
            ((DataRequestor)myDataRequestor).acceptData(result,this);
        }
        myDataRequestor = null;
    }

    /**
     * Part of DataRequestor interface
     */

    public void handleFailure(Exception exception, DataRequestor dataHolder) {
        if (myDataRequestor instanceof Vector) {
            for (int i = 0; i < ((Vector)myDataRequestor).size(); i++) {
                ((DataRequestor)((Vector)myDataRequestor).elementAt(i)).
                  handleFailure(exception, this);
            }
        } else {
            ((DataRequestor)myDataRequestor).handleFailure(exception, this);
        }

        // XXX We *could* hang on to the requestor(s) and add a
        // method retry() that attempted delivery again.  This
        // means exporting network unreliability issues all the
        // way to the consumers of the data. Maybe this is
        // reasonable.

        myDataRequestor = null;
    }

    /**

     * Attempt to find an existing fulfiller with an indentical hints
     * vector If one is found then we can use that one and save some
     * memory.  This is potentially dangerous so we need to think
     * about whether we want to do this before we actually do
     * it. until then, this hook here exists but will not in fact
     * unify any fulfillers.

     * This method should probably move to the Fulfiller class and
     * become an instance method there,

     */

    public Fulfiller getUnifiedFulfiller(Vector givenHints,
                                         Vector inheritedHints,
                                         Fulfiller aFulfiller) {
        Vector result = null;

        if (givenHints == null && inheritedHints == null) return theNullFulfiller;
        if (inheritedHints == null) result = givenHints;
        else if (givenHints == null) result = inheritedHints;
        else {                  // We have both inherited and given hints. Merge them.

            // Use a hashtable to quickly find the union of these two vectors.

            Hashtable theSet = new Hashtable();
            for (int i = 0; i < givenHints.size(); i++) 
                theSet.put(givenHints.elementAt(i),givenHints.elementAt(i));
            for (int i = 0; i < inheritedHints.size(); i++)
                theSet.put(inheritedHints.elementAt(i),inheritedHints.elementAt(i));
            result = new Vector(theSet.size());
            Enumeration e = theSet.keys();
            while (e.hasMoreElements()) result.addElement(e.nextElement());
        }

        if (result.size() == 1) { // See if we have one for this hint already

            // We could look up the hint in a hashtable and return an
            // existing fulfiller but for now, just return the one we have
        }
        return aFulfiller.makeFulfiller(result);
    }

    /**

     * Set our fulfiller. Used by our persistence recipe.

     */

    /* package */ void setFulfiller(Fulfiller fulfiller) {
        myFulfiller = fulfiller;
    }

    /**

     * Replace the DataHolder to a publishing fulfiller if the object
     * is published.  This may chage from session to session and in
     * mid-session. An important case is when you create a Repository
     * file using the Curator, and then decide to put the file in the
     * Published folder. All DataHolders created using art from the
     * PublishRepository needs to have Fulfillers that use the
     * published SturdyRef of the RepositoryPublisher in this
     * process. This is easy to specify when creating new
     * DataHolders. However, the Repository may already contain nested
     * DataHolders as part of appearance objects. The Fulfillers in
     * these DataHolders bust be *updated* to have hints that point to
     * the current RepositoryPublisher since that publisher is not
     * known at Curation time. <p>

     * As a side bonus we have the option of using this opportunity to
     * (wherever possible) unify the fulfillers to a small set of
     * known fulfillers which we share. This sharing will cut down a
     * bit on network traffic. This optimization is not implemented
     * yet.

     * givenHints is a Vector that is given us at decode time. It is
     * often null, but when it is not null then it contains hints that
     * we'd better put into the hints vector of our fulfiller. This
     * means we should probably (in the medium term) create one
     * special fulfiller for this DataHolder but ultimately we'd like
     * to unify all received dataholders to a few "uniquely encoded"
     * ones in our address space.<p>

     * <b>XXX</b> We ignore givenHints but really we should use it.

     */

    public void setFulfiller(Vector givenHints, Vector inheritedHints) {
        if (theNullFulfiller == null)
            theNullFulfiller = (Fulfiller)CrewCapabilities.getTheNullFulfiller();

        // Fix for our use of empty vectors in parimeter tables
        // where we cannot store nulls.

        if (givenHints != null && givenHints.size() == 0) givenHints = null;
        if (inheritedHints != null && inheritedHints.size() == 0) inheritedHints = null;

        if (theRepository.isPublished(myCryptoHash)) {
            String fulfillerString;

            if (thePublishFulfiller == null)
                thePublishFulfiller = (Fulfiller)CrewCapabilities.getThePublishFulfiller();

            if (thePublishFulfiller == theNullFulfiller) fulfillerString = "nullFulfiller";
            else fulfillerString = "publishFulfiller";

            //if (givenHints == null)
            // System.out.println("In SetFulfiller - Hints are null, fulfiller is " +
            // fulfillerString);
            // else System.out.println("in SetFulfiller - Hints[0] is " +
            // ((Vector)givenHints).elementAt(0) + " and fulfiller is " +
            // fulfillerString);

            myFulfiller = thePublishFulfiller;
            // System.out.println("SF Published: " + myCryptoHash +
            // " and fulfiller is " + myFulfiller);
        } else {
            // if (givenHints != null) 
            // System.out.println("**** In SetFulfiller, Hints[0] is " +
            // ((Vector)givenHints).elementAt(0));
            // if (inheritedHints != null) 
            // System.out.println("**** In SetFulfiller, inheritedHints[0] is " +
            // ((Vector)inheritedHints).elementAt(0));

            // So we were given hints but we are not publishing
            // them.  This means we are in an import situation. We
            // cannot use the regular nullfulfiller, we must
            // create one that contains these hints.

            myFulfiller = getUnifiedFulfiller(givenHints, inheritedHints, theNullFulfiller);
        }
    }
}
