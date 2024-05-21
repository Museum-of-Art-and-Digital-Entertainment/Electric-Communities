package ec.e.rep;

import ec.e.rep.Repository;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.rep.steward.DataRequestor;
import ec.e.rep.DataHolder;
import ec.e.rep.steward.CryptoHash;
import ec.e.rep.Fulfiller;
import ec.e.start.Tether;
import ec.e.start.Vat;
import ec.e.start.SmashedException;
import java.util.Vector;
import java.util.Hashtable;
import ec.e.run.steward.OnceOnlyException;
import java.io.IOException;
import ec.cert.Certificate;
import ec.cert.Verifier;

/**

 * DataHolder - An object that knows how to attempt to
 * retrieve some data that it is a placeholder for.

 * myFulfiller is the capability needed to resolve to an actual
 * reference. <p>

 * myDataLeaf is a tether to the perishable data itself and may be
 * null or smashed. <p>

 * cryptoHash is the cryptographic hash of the data. <p>

 * myDataRequestor is either an object that implements the Requestor
 * interface or a Vector of objects that do. We check which one it is
 * before calling any methods on it. We make sure the myDataLeaf holds
 * valid data (fetching it as appropriate) and then call held() on the
 * tether and hand the result to the DataRequestor object (or objects)

 * These objects are intermediates (or rather, switchboards or
 * redirectors, given multiple repositories, network sites to retrieve
 * data from, and multiple vats) between the Repository and WWWeb on
 * one side and the users of data on the other. As such intermediaries
 * they implement both the DataHolder and DataRequestor interfaces.

 */

public class DataHolder implements DataRequestor {
    private Tether myDataLeaf;
    private Fulfiller myFulfiller;
    private CryptoHash myCryptoHash;
    private Hashtable myCertificates = null;
    private Object myDataRequestor = null;    // One or more DataRequestor objects

    /**

     * Constructs an DataHolder with a smashed Tether. The
     * most popular constructor, since you don't normally want your
     * dataholders to already contain their data.

     */

    public DataHolder(Vat vat, CryptoHash cryptoHash, Fulfiller fulfiller) {
        myDataLeaf = new Tether(vat); // Create a smashed tether
        myCryptoHash = cryptoHash;
        myFulfiller = fulfiller;
    }

    /**

     * Constructor that allows you to specify a certificates table at creation time.

     */

    public DataHolder(Vat vat, Object data, Fulfiller fulfiller, Hashtable certificates, boolean retainTheData) {

        if (! retainTheData) {
            myDataLeaf = new Tether(vat);       // Create a smashed tether
        } else {
            myDataLeaf = new Tether(vat,data);  // Create a tether containing the data
        }
        myCryptoHash = Repository.computeCryptoHash(data);
        myFulfiller = fulfiller;
        myCertificates = certificates;
    }

    /**

     * Apply a given verifier to our certificates set
     * and return pass or fail

     */

    public boolean certifiedBy(Verifier verifier) {

        // If verifier is null, then every object passes
        if (verifier == null) return true;

        // If hashtable is null, then we pass no tests.
        if (myCertificates == null) return false;

        Certificate certificate = (Certificate)myCertificates.get(verifier);

        // If we don't have a certificate, then we fail
        if (certificate == null) return false;

        // Let verifier decide
        return verifier.verify(myCryptoHash.getCopyOfHashBytes(), certificate.getSignedBlob());
    }

    /**

     * Add a certificate to our certificate set

     */

    public void addCertificate(Certificate certificate) {
        if (myCertificates == null) {
            myCertificates = new Hashtable();
        }
        myCertificates.put(certificate.getVerifier(), certificate);
    }

    /**

     * Retrieve the data, if necessary and possible. Returns the data
     * synchronously. If the data cannot be retrieved synchronously,
     * throws an error.

     */

    public Object held() throws RepositoryKeyNotFoundException, IOException {
        Object result;

        try {
            result = myDataLeaf.held(); // Check if we have it already
        } catch (SmashedException smx) {

            // If we cannot find it in Repository, then the lookup
            // will throw a RepositoryKeyNotFoundException but this
            // should be really unusual. Normally we retrieve the
            // data, update our Tether, and return the data.

            result = myFulfiller.getFromRepository(myCryptoHash); // Look in caches and releases
            myDataLeaf = new Tether(myDataLeaf.vat(), result); // Our vat reference is in our old tether.
        }
        return result;
    }

    /**
      
     * Add a DataRequestor object to this DataHolder.  Whenever the
     * data arrives (or fails), it will call the appropriate method in
     * the DataRequestor object. This could happen synchronously if we are
     * lucky, otherwise it will happen when data arrives.

     */

    public void giveDataTo(DataRequestor requestor) {
        Object result;

        try {
            try {
                result = held();
                requestor.acceptData(result, this);
            } catch (RepositoryKeyNotFoundException rknfx) {

                // We did not have it in memory, we don't have it in a
                // Repository (cache or distribution medium such as CD-ROM).
                // Must go get it over the net. Add ourselves to the list of
                // requestors.

                if (myDataRequestor == null) {
                    myDataRequestor = requestor;
                } else {
                    if (myDataRequestor instanceof Vector) {
                        ((Vector)myDataRequestor).addElement(requestor);
                    } else {
                        Object old = myDataRequestor;
                        myDataRequestor = new Vector(5);
                        ((Vector)myDataRequestor).addElement(old);
                        ((Vector)myDataRequestor).addElement(requestor);
                    }
                }
                myFulfiller.requestURLRetrieval(myCryptoHash, (DataHolder)this);

                // Since the caller has a callback interface to handle
                // errors we'll call back with whatever exception we
                // are given from the fulfiller, or with any
                // unexpected exception from the Repository.

            }
        } catch (Exception x) {
            requestor.handleFailure(x,(DataHolder)this);
        }
    }

    /**

     * We implement the DataRequestor interface ourselves since we
     * will be called from the WWWeb retrieval code.

     * XXX If the URL code calls back from another thread, then either
     * it or this method must synchronize on the VatLock.

     */

    public void acceptData(Object data, Object dataHolder) {

        // Assert(dataHolder == this);

        CryptoHash receivedHash = Repository.computeCryptoHash(data);
        if (myCryptoHash.equals(receivedHash)) {
            myDataLeaf = new Tether(myDataLeaf.vat(), data); // Our vat reference is in our old tether.
            if (myDataRequestor instanceof Vector) {
                for (int i = 0; i < ((Vector)myDataRequestor).size(); i++) {
                    ((DataRequestor)((Vector)myDataRequestor).elementAt(i)).acceptData(data,this);
                }
            } else {
                ((DataRequestor)myDataRequestor).acceptData(data,(DataHolder)this);
            }
            myDataRequestor = null;

        } else {

            // XXX This probably should not be a securityexception but
            // I can't decide what it should be or even what it should
            // be a subclass of.

            java.lang.SecurityException cryptoException =
              new java.lang.SecurityException("Cryptohash mismatch - requested=" +
                                                     myCryptoHash + ", received=" + receivedHash);
            if (myDataRequestor instanceof Vector) {
                for (int i = 0; i < ((Vector)myDataRequestor).size(); i++) {
                    ((DataRequestor)((Vector)myDataRequestor).elementAt(i)).handleFailure(cryptoException, this);
                }
            } else {
                ((DataRequestor)myDataRequestor).handleFailure(cryptoException, this);
            }

            // XXX We *could* hang on to the requestor(s) and add a
            // method retry() that attempted delivery again.  This
            // means exporting network unreliability issues all the
            // way to the consumers of the data. Maybe this is
            // reasonable.

            myDataRequestor = null;
        }
    }

    /**

     * Part of DataRequestor interface

     */

    public void handleFailure(Exception exception, Object dataHolder) {
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
}

