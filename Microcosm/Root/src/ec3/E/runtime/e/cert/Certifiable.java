package ec.e.cert;

/**
 * Encapsulation for a static representation of an object graph and a
 * set of signatures obtained for that representation.  When you wish to
 * collect signatures on a collection of objects, first you have to take
 * a snapshot of the objects.  You can't expect someone to sign a
 * potentially mutable object graph.  Creating the Certifiable takes the
 * snapshot, which it remembers.
 */
final public class Certifiable {
    /* package */ byte[] myEncodedBlob; // immutable after construction
    private HashTable myCertificateTable = new HashTable(5);

    /**
     * Create a new Certifiable.
     *  Takes a snapshot of the Object passed in.
     * @param blob the object graph to be saved for certifying.
     */
    public Certifiable(Object blob)
    {
        myEncodedBlob = /* encode blob */ ???;
    }

    /**
     * Regenerate the object graph from the snapshot.
     * This will only succeed if an verifier acceptable to you corresponds
     * to a certifier used to sign this certifiable.
     * @param verifiers Hashtable of acceptable verifiers.
     *  Note:  the verifiers are the KEYs in this Hashtable; the values
     *  are never examined.
     * @exception NoAcceptableVerifiersException if no acceptable verifier
     *  can be found.
     */
    // I'm worried about synchronization here.  If either hashtable
    //  is modified, there could be problems.
    public Object verify(Hashtable verifiers) throws NoAcceptableVerifiersException
    {
        Hashtable tolookup ;
        Hashtable toenumerate ;

        // pick the shortest Hashtable to enumerate, just for efficiency
        if (verifiers.size() > myCertificateTable.size()) {
            tolookup = myCertificateTable ;
            toenumerate = verifiers ;
        }
        else {
            tolookup = verifiers ;
            toenumerate = myCertificateTable ;
        }

        Enumeration en = toenumerate.keys ;
        while (en.hasMoreElements()) {
            Verifier verifiertotry = en.nextElement();
            if (tolookup.containsKey(verifiertotry)) {
                Certificate certificate = myCertificateTable.get(verifiertotry);
                if (certificate.isVerified || certificate.myVerifier.verify(myEncodedBlob, certificate.mySignature)) {
                    certificate.isVerified = true;
                    return /* decode myEncodedBlob */ ???;
                }
                // verify failed, but so what?  just try another certificate...
            }
        }
        throw new NoAcceptableVerifiersException();
    }

    // should there be a way to remove certificates from this certifiable?
    // should this be public, allowing the user to add new certificates from parallel sources?
    /* package */ addCertificate(Certificate certificate)
    {
        myCertificateTable.put(certificate.myVerifier, certificate);
    }
}
