package ec.e.hold;

import ec.cert.CryptoHash;
import java.io.IOException;
import java.util.Vector;
import java.util.Hashtable;
import ec.cert.Verifier;
import ec.cert.Certificate;

/**

 * DataHolder is the interface implemented by DataHolderStewards.

 */

public interface DataHolder {
    public byte certifiedBy(Verifier verifier);
    public void addCertificate(Certificate certificate);
    public Hashtable getCertificates();
    public Object held() throws IOException;
    public void giveDataTo(DataRequestor requestor);
    void acceptData(Object data, DataRequestor yourRequestor);
    void acceptByteData(byte[] data, DataRequestor yourRequestor);
    void handleFailure(Exception failure, DataRequestor yourRequestor);

    public CryptoHash getCryptohash();
}

/**

 * Anyone using DataHolders needs to define a callback object that
 * implements the DataRequestor interface. It will get called with the
 * data when the data becomes available. <p>

 * If there is a problem, including timeout, then the handleFailure
 * method gets called with an Exception object.

 */

public interface DataRequestor {
    void acceptData(Object data, DataRequestor yourRequestor);
    void acceptByteData(byte[] data, DataRequestor yourRequestor);
    void handleFailure(Exception failure, DataRequestor yourRequestor);
}

/**

 * To request data from a remote location you use some capability implementing 
 * RemoteRetrievalRequestor.

 */

public interface RemoteRetriever {
    public void requestByteRetrieval(CryptoHash hash, Vector myHints, DataRequestor requestor)
         throws IOException;
}

